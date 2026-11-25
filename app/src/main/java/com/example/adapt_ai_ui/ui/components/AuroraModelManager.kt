package com.example.adapt_ai_ui.ui.components

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.adapt_ai_ui.inference.LocalTutor
import com.example.adapt_ai_ui.inference.ModelState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
internal fun AuroraModelManager(runtime: LocalTutor) {
    val context = LocalContext.current
    val state by runtime.state.collectAsState()
    val installed by runtime.installedModel.collectAsState()
    val bytes by runtime.importBytes.collectAsState()
    val scope = rememberCoroutineScope()
    val arm64 = "arm64-v8a" in Build.SUPPORTED_ABIS
    val memory = remember {
        ActivityManager.MemoryInfo().also {
            (context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).getMemoryInfo(it)
        }
    }
    var error by remember { mutableStateOf<String?>(null) }
    var confirmDelete by remember { mutableStateOf(false) }
    val busy = state in setOf(ModelState.IMPORTING, ModelState.VERIFYING, ModelState.LOADING, ModelState.GENERATING)
    val import = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) scope.launch {
            error = null
            try {
                runtime.importBundle(uri)
            } catch (cancel: CancellationException) {
                throw cancel
            } catch (failure: IllegalArgumentException) {
                error = failure.message ?: "The model bundle could not be verified."
            } catch (_: Exception) {
                error = "Could not install the model. Check the bundle format, available storage, and runtime compatibility."
            } catch (_: LinkageError) {
                error = "The ExecuTorch runtime could not be loaded on this device."
            }
        }
    }
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Offline AI model", style = MaterialTheme.typography.titleLarge)
            Text(state.label, style = MaterialTheme.typography.labelLarge)
            Text("Llama 3.2 1B · ExecuTorch 1.0.0 · XNNPACK CPU")
            Text(
                "${Build.MANUFACTURER} ${Build.MODEL} · Android ${Build.VERSION.RELEASE} · ${Build.SUPPORTED_ABIS.firstOrNull() ?: "Unknown ABI"}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                "${runtime.store.availableBytes / 1_048_576} MiB free storage · ${memory.availMem / 1_048_576} MiB RAM available at open",
                style = MaterialTheme.typography.bodySmall
            )
            installed?.let { model ->
                Text("Revision ${model.revision} · ${model.quantization} · ${model.bytes} bytes", style = MaterialTheme.typography.bodySmall)
                Text("Model SHA-256\n${model.hash}", style = MaterialTheme.typography.bodySmall)
                Text("Tokenizer SHA-256\n${model.tokenizerHash}", style = MaterialTheme.typography.bodySmall)
            }
            if (!arm64) Text("The tutor requires ARM64 Android.")
            if (busy && state != ModelState.GENERATING) LinearProgressIndicator(Modifier.fillMaxWidth())
            if (state == ModelState.IMPORTING) Text("${bytes / 1_048_576} MiB copied")
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Button(onClick = { import.launch(arrayOf("application/zip", "application/octet-stream")) }, enabled = !busy && arm64) {
                Text(if (installed == null) "Import model bundle" else "Replace model bundle")
            }
            if (installed != null) {
                Row {
                    TextButton(onClick = { scope.launch { runtime.unload() } }, enabled = state == ModelState.READY) {
                        Text("Unload")
                    }
                    TextButton(onClick = { confirmDelete = true }, enabled = !busy) { Text("Delete model") }
                }
            }
            Text("Inference and learning data stay on this device.", style = MaterialTheme.typography.bodySmall)
        }
    }
    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete the local model?") },
            text = { Text("Import a verified bundle again to resume offline AI tutoring.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    scope.launch {
                        try {
                            runtime.delete()
                            error = null
                        } catch (cancel: CancellationException) {
                            throw cancel
                        } catch (_: Exception) {
                            error = "Could not delete the model. Try again."
                        }
                    }
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } }
        )
    }
}
