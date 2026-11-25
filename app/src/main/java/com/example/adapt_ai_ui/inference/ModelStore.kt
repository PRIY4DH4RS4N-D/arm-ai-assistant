package com.example.adapt_ai_ui.inference

import android.content.Context
import android.net.Uri
import org.json.JSONObject
import java.io.File
import java.security.MessageDigest
import java.util.UUID
import java.util.zip.ZipInputStream
import java.util.zip.ZipException

data class InstalledModel(val folder: File, val hash: String, val quantization: String, val bytes: Long, val tokenizerHash: String, val revision: String)

/** Bundles contain exactly manifest.json, model.pte and tokenizer.model. */
class ModelStore(private val context: Context) {
    private val root = File(context.noBackupFilesDir,"models").apply { mkdirs() }
    val availableBytes get() = root.usableSpace
    fun delete() {
        if (pointer.exists()) check(pointer.delete()) { "Could not remove the active model" }
        recover()
    }
    private val pointer = File(root,"active")
    fun installed(): InstalledModel? {
        if(!pointer.isFile) return null
        val name = pointer.readText().trim()
        require(runCatching { UUID.fromString(name).toString()==name }.getOrDefault(false)) { "Invalid model pointer" }
        return validate(File(root,name), false)
    }
    private fun hash(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().buffered().use { stream -> val buf=ByteArray(1024*1024); while(true) { val n=stream.read(buf); if(n<0) break; digest.update(buf,0,n) } }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
    private fun validate(folder: File, verifyHashes: Boolean): InstalledModel {
        val manifest = JSONObject(File(folder,"manifest.json").readText())
        require(manifest.keys().asSequence().toSet()==setOf("version","model","runtime","backend","contextLength","quantization","files","provenance")) { "Unexpected manifest fields" }
        require(manifest.get("version")==1 && manifest.get("runtime")=="1.0.0" && manifest.get("provenance") is JSONObject) { "Bundle/runtime version mismatch" }
        require(manifest.getString("model")=="Llama-3.2-1B-Instruct") { "Unsupported model" }
        require(manifest.get("contextLength")==2048 && manifest.get("backend")=="xnnpack") { "Expected XNNPACK / 2048-token export" }
        val quant = manifest.getString("quantization")
        require(quant in listOf("8da4w","int8","spinquant-int4-eo8"))
        val files=manifest.getJSONObject("files")
        require(files.keys().asSequence().toSet()==setOf("model.pte","tokenizer.model"))
        for(name in listOf("model.pte","tokenizer.model")) {
            val file = File(folder,name)
            require(file.isFile && file.length()>0) { "Missing $name" }
            val metadata=files.getJSONObject(name)
            require(metadata.keys().asSequence().toSet()==setOf("sha256","bytes"))
            val bytes=metadata.get("bytes")
            require((bytes is Int || bytes is Long) && file.length()==(bytes as Number).toLong()) { "Size mismatch: $name" }
            val expected=manifest.getJSONObject("files").getJSONObject(name).getString("sha256")
            require(expected.matches(Regex("[a-f0-9]{64}")))
            if(verifyHashes) require(hash(file)==expected) { "Checksum mismatch: $name" }
        }
        val modelHash = files.getJSONObject("model.pte").getString("sha256")
        val tokenizerHash = files.getJSONObject("tokenizer.model").getString("sha256")
        if (quant == "spinquant-int4-eo8") {
            require(modelHash == "8715cdba9e91f6bede00cc5f2d6b12397b95225da5630c4972a8da03001cda3b" &&
                tokenizerHash == "82e9d31979e92ab929cd544440f129d9ecd797b69e327f80f17e1c50d5551b55") {
                "Unknown SpinQuant model or tokenizer"
            }
        }
        return InstalledModel(folder, modelHash, quant, File(folder,"model.pte").length(), tokenizerHash,
            manifest.getJSONObject("provenance").optString("revision", "Local export"))
    }
    fun verify() = installed()?.let { validate(it.folder,true) }
    fun recover() {
        val active=if(pointer.isFile) pointer.readText().trim() else null
        root.listFiles()?.filter { it.isDirectory && it.name!=active }?.forEach { it.deleteRecursively() }
        File(root,"active.tmp").delete()
    }
    fun importBundle(uri: Uri,checkActive: ()->Unit={},onProgress: (Long)->Unit={},verifyRuntime: (InstalledModel)->Unit): InstalledModel {
        recover()
        require(root.usableSpace > 2_000_000_000L) { "Free at least 2 GB internal storage before import" }
        val folder = File(root,UUID.randomUUID().toString()).apply { mkdirs() }
        var committed=false
        try {
            val seen=mutableSetOf<String>(); var total=0L
            context.contentResolver.openInputStream(uri).use { raw ->
                requireNotNull(raw) { "Cannot open bundle" }
                ZipInputStream(raw.buffered()).use { zip ->
                    while(true) {
                        val entry=zip.nextEntry ?: break
                        require(entry.name in listOf("manifest.json","model.pte","tokenizer.model") && seen.add(entry.name) && !entry.isDirectory) { "Unexpected or duplicate bundle entry" }
                        var entryBytes=0L
                        File(folder,entry.name).outputStream().buffered().use { out ->
                            val buffer=ByteArray(1024*1024)
                            while(true) {
                                val count=zip.read(buffer); if(count<0) break
                                checkActive()
                                total+=count; entryBytes+=count
                                onProgress(total)
                                require(total<=1_800_000_000L && (entry.name!="manifest.json" || entryBytes<=65536)) { "Bundle exceeds size limit" }
                                out.write(buffer,0,count)
                            }
                        }
                    }
                }
            }
            require(seen.size==3) { "Incomplete bundle" }
            val result=validate(folder,true)
            checkActive()
            verifyRuntime(result)
            checkActive()
            val temp=File(root,"active.tmp"); temp.writeText(folder.name)
            java.nio.file.Files.move(temp.toPath(),pointer.toPath(),java.nio.file.StandardCopyOption.REPLACE_EXISTING,java.nio.file.StandardCopyOption.ATOMIC_MOVE)
            committed=true
            // The caller releases the native module before import; old model folders can now be removed.
            runCatching { root.listFiles()?.filter { it.isDirectory && it!=folder }?.forEach { it.deleteRecursively() } }
            return result
        } catch(t: Throwable) {
            if(!committed) folder.deleteRecursively()
            if(t is ZipException) throw IllegalArgumentException("Invalid or unsafe model bundle ZIP",t)
            throw t
        }
    }
}
