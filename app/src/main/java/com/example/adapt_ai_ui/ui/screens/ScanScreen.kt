package com.example.adapt_ai_ui.ui.screens

import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.os.SystemClock
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.adapt_ai_ui.data.platform.*
import com.example.adapt_ai_ui.ui.components.*
import com.example.adapt_ai_ui.ui.theme.AdaptTheme
import java.io.File
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
internal fun ScanScreen(platform: LearningPlatform, onBack: () -> Unit, onExplain: (String) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var original by remember { mutableStateOf<Bitmap?>(null) }
    var preview by remember { mutableStateOf<Bitmap?>(null) }
    var text by rememberSaveable { mutableStateOf("") }
    var busy by remember { mutableStateOf(true) }
    var status by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var hasSavedScan by remember { mutableStateOf(false) }
    val filename = rememberSaveable { "${UUID.randomUUID()}.jpg" }
    val capture = remember(filename, context) {
        File(File(context.cacheDir, "scan-capture"), filename)
    }

    fun work(message: String, action: suspend () -> Unit) {
        scope.launch {
            busy = true
            status = ""
            error = ""
            try {
                action()
            } catch (cancel: CancellationException) {
                throw cancel
            } catch (_: Exception) {
                error = message
            } finally {
                busy = false
            }
        }
    }

    fun load(uri: Uri, fromCamera: Boolean = false) = work("Could not read the photo. Choose a JPEG or PNG.") {
        try {
            val image = readScanImage(context.contentResolver, uri)
            original = image
            preview = image
            text = ""
        } finally {
            if (fromCamera) capture.delete()
        }
    }

    val gallery = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) load(uri)
    }
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { result ->
        if (result) load(FileProvider.getUriForFile(context, "${context.packageName}.scan", capture), fromCamera = true)
        else capture.delete()
    }
    LaunchedEffect(platform) {
        try {
            platform.loadScan()?.let {
                hasSavedScan = true
                if (original == null) {
                    val image = decodeScanImage(it.jpeg)
                    original = image
                    preview = image
                    if (text.isBlank()) text = it.text
                }
            }
        } catch (cancel: CancellationException) {
            throw cancel
        } catch (_: Exception) {
            error = "Could not reopen the saved scan."
        } finally {
            busy = false
        }
    }

    AuroraScene {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 17.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            AuroraHeader("Scan & Learn", onBack)
            Text("Extract printed text on this device. Review the result before saving or asking the tutor.",
                color = AdaptTheme.colors.textSecondary)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AuroraPill("Take photo", {
                    try {
                        capture.parentFile?.mkdirs()
                        camera.launch(FileProvider.getUriForFile(context, "${context.packageName}.scan", capture))
                    } catch (_: Exception) {
                        error = "Camera is unavailable. You can import an existing image."
                    }
                }, enabled = !busy)
                AuroraPill("Import image", { gallery.launch(arrayOf("image/*")) }, enabled = !busy)
            }
            if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())
            if (error.isNotBlank()) Text(error, color = MaterialTheme.colorScheme.error)
            if (status.isNotBlank()) Text(status, color = auroraAccent)
            preview?.let { image ->
                AuroraPanel(Modifier.fillMaxWidth()) {
                    Image(image.asImageBitmap(), "Selected learning document",
                        Modifier.fillMaxWidth().heightIn(max = 315.dp), contentScale = ContentScale.Fit)
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    AuroraPill("Rotate", {
                        val matrix = Matrix().apply { postRotate(90f) }
                        preview = Bitmap.createBitmap(image, 0, 0, image.width, image.height, matrix, true)
                    }, enabled = !busy)
                    AuroraPill("Trim edges", {
                        val x = image.width / 12
                        val y = image.height / 12
                        preview = Bitmap.createBitmap(image, x, y, image.width - 2 * x, image.height - 2 * y)
                    }, enabled = !busy && image.width > 24 && image.height > 24)
                    AuroraPill("Restore photo", { preview = original }, enabled = !busy && image !== original)
                }
                AuroraAction("Recognise text offline", {
                    work("Text recognition could not finish.") {
                        val start = SystemClock.elapsedRealtime()
                        text = recognizeScan(image)
                        status = if (text.isBlank()) "No readable text. Try better lighting or a closer photo."
                        else "Text extracted in ${SystemClock.elapsedRealtime() - start} ms"
                    }
                }, Modifier.fillMaxWidth(), enabled = !busy, icon = AdaptIcons.Search)
            }
            if (preview != null) {
                OutlinedTextField(text, { text = it.take(12_000) }, Modifier.fillMaxWidth(),
                    label = { Text("Extracted text") }, minLines = 3, maxLines = 9, enabled = !busy)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AuroraPill("Save scan", {
                        val source = original ?: return@AuroraPill
                        val note = text.trim()
                        work("Could not save this scan.") {
                            platform.storeScan(SavedScan(encodeScanImage(source), note))
                            hasSavedScan = true
                            status = "Scan saved to this learner profile."
                        }
                    }, enabled = text.isNotBlank() && !busy)
                    AuroraPill("Explain with Ember", { onExplain(text.trim()) }, enabled = text.isNotBlank() && !busy)
                }
            }
            if (hasSavedScan) TextButton({
                work("Could not delete the saved scan.") {
                    platform.storeScan(null)
                    hasSavedScan = false
                    original = null
                    preview = null
                    text = ""
                    status = "Saved scan deleted."
                }
            }, enabled = !busy) { Text("Delete saved scan") }
            Spacer(Modifier.height(12.dp))
        }
    }
}
