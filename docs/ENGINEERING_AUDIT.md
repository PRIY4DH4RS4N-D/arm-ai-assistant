# Recovery audit

The local handover directory was absent. This workspace was initially empty. The public reference exposed both `llama-integration` and `ui-redesign` at `6547ba13502f38754d34055baa47e133f076f298`. The recovered branch and `backup/llama-integration-6547ba1` preserve that baseline. The original `origin` remote remains available. The destination was checked separately and was empty at recovery.

- The redesigned branch has one Kotlin/Compose app at application ID `com.example.adapt_ai_ui`. The five core destinations, domain explorer, onboarding, lesson UI, masteries and practice/revision were already present.
- The active checkout stored learning history with application-private JSON and serialized asynchronous writes. The earlier `main` used a different package, Room, and a mature but separate learning model.
- The Tutor entry point used `PrototypeTutorEngine`. This branch had no ExecuTorch Maven dependency, model manager, model binaries, CMake, or project-specific JNI. The earlier main branch’s `LlmModule` bridge and model tooling supplied the migration starting point.
- The branch included JVM tests for curriculum, state and persistence and an example instrumentation test. The baseline Gradle wrapper download timed out. A retry with locally provisioned Gradle and JDK reached Android SDK provisioning and initially stopped on missing licenses. Build and test results for the delivery are tracked in [the test report](TEST_REPORT.md).
- The validated local PTE and tokenizer came from a separate local project’s publisher cache. The older historical GGUF identities remain unresolved by this checkout. Their file sizes differ from the pinned PTE.
- The pre-existing CI workflow referenced `:core`, `:inference` and `:data`, modules removed on the redesigned branch. The delivery workflow targets the recovered app and its current toolchain.
