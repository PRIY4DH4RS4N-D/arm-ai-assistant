package com.example.adapt_ai_ui.ui.components

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import com.example.adapt_ai_ui.ui.theme.AdaptTheme
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
    Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
        AuroraPanel(Modifier.fillMaxWidth(), AuroraBlue) {
            AuroraOrb(if (busy) OrbState.THINKING else OrbState.IDLE, { 0f }, Modifier.size(76.dp))
            Text("Offline AI model", style = AdaptTheme.typography.headlineSmall, color = AdaptTheme.colors.textPrimary)
            Text("Llama 3.2 1B", style = AdaptTheme.typography.titleMedium, color = AdaptTheme.colors.textPrimary)
            Text(state.label, color = auroraAccent, modifier = Modifier.padding(top = 5.dp))
            Text("ExecuTorch 1.0.0  |  XNNPACK CPU", style = AdaptTheme.typography.bodySmall,
                color = AdaptTheme.colors.textSecondary)
            if (busy && state != ModelState.GENERATING) {
                LinearProgressIndicator(Modifier.fillMaxWidth().padding(top = 9.dp), color = auroraAccent)
            }
            if (state == ModelState.IMPORTING) Text("${bytes / 1_048_576} MiB copied")
        }
        AuroraPanel(Modifier.fillMaxWidth()) {
            Text("Device and storage", style = AdaptTheme.typography.titleMedium)
            Text("${Build.MANUFACTURER} ${Build.MODEL}  |  Android ${Build.VERSION.RELEASE}",
                color = AdaptTheme.colors.textSecondary)
            Text(Build.SUPPORTED_ABIS.firstOrNull() ?: "Unknown ABI", color = AdaptTheme.colors.textSecondary)
            Text("${runtime.store.availableBytes / 1_048_576} MiB free storage",
                modifier = Modifier.padding(top = 6.dp))
            Text("${memory.availMem / 1_048_576} MiB RAM available at open",
                color = AdaptTheme.colors.textSecondary)
            if (!arm64) Text("The tutor requires ARM64 Android.")
        }
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        AuroraAction(if (installed == null) "Import model bundle" else "Replace model bundle",
            { import.launch(arrayOf("application/zip", "application/octet-stream")) },
            Modifier.fillMaxWidth(), enabled = !busy && arm64, icon = AdaptIcons.ArrowForward)
        installed?.let { model ->
            AuroraPanel(Modifier.fillMaxWidth()) {
                Text("Verified model bundle", style = AdaptTheme.typography.titleMedium)
                Text("Revision ${model.revision}  |  ${model.quantization}",
                    style = AdaptTheme.typography.bodySmall, color = AdaptTheme.colors.textSecondary)
                Text("${model.bytes / 1_048_576} MiB installed", style = AdaptTheme.typography.bodySmall)
                Spacer(Modifier.height(8.dp))
                Text("Model SHA-256\n${model.hash}", style = AdaptTheme.typography.bodySmall)
                Spacer(Modifier.height(5.dp))
                Text("Tokenizer SHA-256\n${model.tokenizerHash}", style = AdaptTheme.typography.bodySmall)
                Row {
                    TextButton(onClick = { scope.launch { runtime.unload() } }, enabled = state == ModelState.READY) {
                        Text("Unload")
                    }
                    TextButton(onClick = { confirmDelete = true }, enabled = !busy) { Text("Delete model") }
                }
            }
        }
        Text("Inference and learning data stay on this device.",
            style = AdaptTheme.typography.bodySmall, color = AdaptTheme.colors.textSecondary)
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
