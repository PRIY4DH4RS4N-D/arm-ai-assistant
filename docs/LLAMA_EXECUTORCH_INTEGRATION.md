# Llama and ExecuTorch integration

Tutor, lesson and quiz requests share one LocalTutor instance. ExecuTorch Android 1.0.0 drives the Llama 3.2 1B Instruct SpinQuant PTE on ARM64 XNNPACK CPU. The PTE and tokenizer are checked against the manifest before activation. A mutex serialises native execution. Cancellation targets the active generation owner. New requests are bounded before calling JNI, and Kotlin validates quiz data before storing answers or progress.

The publisher export was validated with this runtime on the realme RMX5116. See [provisioning](MODEL_PROVISIONING.md), [manifest](../models/spinquant-manifest.json) and [benchmarks](MODEL_BENCHMARKS.md).
