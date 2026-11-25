package com.example.adapt_ai_ui.inference

import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test

class OfflineModelDeviceTest {
    @Test
    fun verifiesAndGeneratesWithTheProvisionedBundle() = runBlocking {
        assumeTrue(InstrumentationRegistry.getArguments().getString("emberPhysicalOffline") == "1")
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val resolver = context.contentResolver
        assertTrue("ARM64 required", "arm64-v8a" in Build.SUPPORTED_ABIS)
        assertEquals(1, Settings.Global.getInt(resolver, Settings.Global.AIRPLANE_MODE_ON))
        assertEquals(0, Settings.Global.getInt(resolver, "wifi_on"))
        assertEquals(0, Settings.Global.getInt(resolver, "mobile_data"))
        val runtime = LocalTutor(context)
        runtime.initialize()
        val installed = requireNotNull(runtime.installedModel.value) { "Provision a verified Llama bundle first" }
        val prompt = "<|start_header_id|>user<|end_header_id|>\n\n" +
            "Explain in one sentence what a CPU register stores.<|eot_id|>" +
            "<|start_header_id|>assistant<|end_header_id|>\n\n"
        val output = StringBuilder()
        var measurement: TutorEvent.Complete? = null
        try {
            withTimeout(100_000) {
                runtime.generate(prompt, 256).collect { event ->
                    when (event) {
                        is TutorEvent.Token -> output.append(event.text)
                        is TutorEvent.Complete -> measurement = event
                    }
                }
            }
            val stats = requireNotNull(measurement)
            assertEquals(installed.hash, stats.modelHash)
            assertTrue(output.isNotBlank())
            assertTrue(stats.loadMs > 0)
            assertTrue(stats.elapsedMs > 0)
            assertNotNull(stats.ttftMs)
            Log.i("EmberDeviceTest", "loadMs=${stats.loadMs} ttftMs=${stats.ttftMs} elapsedMs=${stats.elapsedMs}")
            Log.i("EmberDeviceTest", stats.nativeStats)
        } finally {
            runtime.unload()
        }
        assertEquals(ModelState.INSTALLED, runtime.state.value)
    }
}
