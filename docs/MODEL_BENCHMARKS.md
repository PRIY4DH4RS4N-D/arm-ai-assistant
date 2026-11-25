# Model benchmarks

Runtime evidence was collected on the realme P4 Pro 5G RMX5116, SM7750, ARM64, Android 16. The backend is XNNPACK CPU. The published warm reference measured 0.678 seconds to the first token and 14.018 generated tokens per second, with roughly 1.94 GiB resident memory at sampled runner points. A prior on-device Algebra request measured 0.442 seconds to the first token.

Those observations predate the EMBER orb, coding and OCR changes and serve as model baseline measurements. On EMBER 1.1.0, one cold instrumentation run measured a 3,996 ms native load, 181 ms to first token after load and 2,289 ms to complete a 32-token response. Native timestamps put the post-prefill decode interval at 2.107 seconds, or about 15.2 generated tokens per second for this run.

Sustained thermal, frame-time, peak-memory and formal OCR accuracy results remain open. See the [protocol and archived results](PERFORMANCE_BENCHMARK.md).
