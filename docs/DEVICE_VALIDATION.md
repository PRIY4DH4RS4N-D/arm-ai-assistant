# EMBER target-device validation

The tested device is the realme P4 Pro 5G RMX5116, Snapdragon SM7750, ARM64, Android 16. ADB installed EMBER 1.1.0 in place and kept the existing learner and locally provisioned Llama bundle.

During capture, ADB reported airplane mode on, Wi-Fi off and mobile data off. The recording shows the replayed introduction, profile, ARM Architecture search, ARM fundamentals, Algebra Journey, a fresh Tutor response, quiz generation, deterministic feedback, the result and updated mastery. The generated equation had solution x = 3. The result showed 1 of 1 correct, and the topic's mastery reached 56% across stored attempts.

A separate radio-disabled check recognised a 1220 x 740 printed fixture, saved it and reopened it through Scan & Learn. This OCR run took 389 ms. A C tracing exercise accepted 10 and saved its solved count. The radio settings were restored after the checks.

The opt-in ARM64 instrumentation test verified the installed model bundle, streamed a fresh response, and unloaded the runtime. One cold run recorded a 3,996 ms native load, 181 ms to the first token after load, and 2,289 ms generation. It emitted 32 tokens. The Room profile-isolation test passed in the same run.

[Two-minute phone recording](../submission/demo/EMBER_PS4_DEMO_120S.mp4) · [Phone screenshots](ui-verification/) · [Model benchmark](MODEL_BENCHMARKS.md)
