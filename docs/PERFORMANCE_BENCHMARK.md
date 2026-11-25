# ARM performance benchmark

The personal learning build generated a valid Algebra equation on-device from a 79-token prompt. ExecuTorch reported 26 output tokens and 0.442 s to the first token. One cold launch after force-stop took 1.035 s in Activity Manager. The figures below refer to the earlier Aurora Tutor baseline.

Physical realme P4 Pro 5G (RMX5116), Android 16, ARM64, SM7750, measured on 25 November 2026. The device had 11,512,612 KiB of total RAM and a 1280 × 2800 display. Network state was airplane mode on, Wi-Fi off and mobile data off. The model is the [verified Llama 3.2 1B Instruct SpinQuant PTE](../models/spinquant-manifest.json). APK identity is in [SHA256SUMS](../submission/SHA256SUMS.txt).

| Measure | Observed |
| --- | --- |
| Initial cold app launch after installing the corrected APK | 1.474 s reported by Activity Manager |
| Teach Me prompt and response length | 116 prompt tokens, 74 generated |
| Native time to first token | 0.678 s |
| Native generation throughput | 14.018 generated tokens/s over 5.279 s |
| End-to-end native inference | 5.957 s |
| RSS after model loading, prompt prefill and generation | 1,941.99 MiB at each reported sample |
| PSS and RSS after the measured request | 1,717,196 KiB PSS and 1,869,968 KiB RSS from `dumpsys meminfo` |
| Thermal status | Android status 0 at the spot check |
| Cold model loading, peak memory, sustained thermals, frame timing and battery drain | Pending controlled measurements |

These are individual on-device observations. The measured Teach Me prompt ran after model activation. For a repeatable comparison, keep brightness and battery level stable, separate cold and warm runs, and repeat each fixed prompt at least five times. Capture native token counts, prefill/decode durations, memory samples and failure counts.
