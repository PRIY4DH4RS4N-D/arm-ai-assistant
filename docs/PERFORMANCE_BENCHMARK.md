# ARM performance benchmark

**Result on this build** NOT MEASURED. ADB did not detect a physical phone on 25 November 2026.

| Measure | Method | Result |
| --- | --- | --- |
| Cold app launch | Force-stop; run `adb shell am start -W -n com.example.adapt_ai_ui/.MainActivity` five times | NOT MEASURED |
| Model load | Measure elapsed time around `LlmModule.load()`, after checksum verification | NOT MEASURED |
| Time to first token | Time from the native generation call to the first non-empty callback | NOT MEASURED |
| Generation throughput | Use tokenizer-aware counts and native decode timings, leaving prompt/prefill out of the calculation | NOT MEASURED |
| Peak process memory | Sample `adb shell dumpsys meminfo com.example.adapt_ai_ui` during the run | NOT MEASURED |
| UI frame timing | Reset and capture `adb shell dumpsys gfxinfo com.example.adapt_ai_ui framestats` around a response | NOT MEASURED |
| Thermal and battery | Record `dumpsys thermalservice` and `dumpsys battery` before and after a fixed 15-minute run | NOT MEASURED |

Run in airplane mode at a stable battery level and screen brightness. Use Teach Me with “Explain why INT8 weights use less storage than FP32”, Socratic with “Help me reason about quantization error”, and Debug & Code with a fixed benign arithmetic example. Run each five times and record device model, Android version, model and APK hashes, runtime version, elapsed time, warm/cold status, and any failed or cancelled runs. Export only anonymized measurements. The engine captures elapsed time, first callback latency and native stats through its completion event.
