# EMBER engineering baseline

The audit started from commit `51937c6` with the previously verified Compose redesign in the working tree. The public submission repository was on that commit. The realme RMX5116 was connected over ADB and already provisioned with the verified local model.

- The Android app uses Kotlin, Compose, a profile-scoped Room database, a legacy local storage adapter, and a single shared ExecuTorch runtime. The package name stays stable so the installed model and learner records survive upgrades.
- The current native runtime is ExecuTorch Android 1.0.0 for ARM64. The provisioned Llama 3.2 1B Instruct PTE uses SpinQuant INT4 weights with INT8 dynamic activations and the XNNPACK CPU backend. The importer stages the bundle, verifies its manifest and SHA-256 hashes, then activates it. See [provisioning](MODEL_SETUP.md).
- The redesigned Home, profile selection, subject explorer, topic workspace, Tutor, practice, analytics, settings and model manager are connected to the real repositories and services. The previous phone run demonstrated streamed inference, a generated Algebra exercise, deterministic scoring, and progress persistence.
- Existing unit coverage includes assessment and mastery, local persistence, and prompt construction. The target-device and benchmark boundaries are detailed in the existing [test report](TEST_REPORT.md) and [benchmark](PERFORMANCE_BENCHMARK.md).
- The baseline lacked an EMBER introduction, expanded ARM coursework, a Scan & Learn workflow, and profile-wide activity and achievements. Arbitrary code execution, full OCR validation, signed release credentials, and a current public video also had no evidence.

Implementation sequence

1. Rebrand the existing application and add the first-run and replayable introduction.
2. Extend the offline catalogue, add evidence-based activity, and connect coding and OCR to the existing learning workflow.
3. Build debug and release artifacts, exercise the core path on RMX5116 with radios off, and record the public demonstration.
4. Validate the final report, publish the submission artifacts, and verify anonymous access.
