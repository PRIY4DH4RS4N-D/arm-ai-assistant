# Test report

## Personal learning platform

The revised ARM64 debug APK and instrumentation APK built. All 122 JVM tests passed. Lint reported zero errors and 36 warnings. A physical Room test passed on the realme RMX5116 and checked profile isolation, per-topic conversation keys and cascading deletion. The installed app preserved the prior learner and verified model during an in-place upgrade. In airplane mode with Wi-Fi and mobile data off, a separate profile selected Algebra and generated `2x + 5 = 11` through ExecuTorch. The app computed `x = 3`, built the choices, scored a submitted wrong answer as 0%, and restored the result after force-stop and restart. A generated Algebra lesson opened and was marked complete. The learner's goal and 25-minute weekly study blocks also appeared on the phone. These checks cover Algebra. The full five-scenario acceptance suite remains open.

## Earlier Aurora release

- **Date** 25 November 2026
- **Recovered baseline** `6547ba13502f38754d34055baa47e133f076f298`

The baseline wrapper download timed out. After provisioning the pinned Gradle, JDK and SDK locally, the host build reached app compilation. Final task results and APK identity are recorded against the delivery build.

A repeat unit-test run reproduced a race in the existing persistence writer. The flush now iterates the concurrent set without relying on its changing size. The phone exposed an empty auxiliary-weight path in the ExecuTorch builder. The text-only constructor fixed the native crash, and the rebuilt APK passed offline model load and generation.

| Check | Result |
| --- | --- |
| `:app:assembleDebug` | PASS |
| `:app:testDebugUnitTest` | PASS. 116 tests, zero failures or skips |
| `:app:assembleDebugAndroidTest` | PASS. Instrumentation APK built |
| `:app:lintDebug` | PASS. Zero errors and 30 warnings |
| APK signature and contents | PASS. Verified debug signature, ARM64 ExecuTorch library, app backup disabled and no Internet permission |
| Publisher model byte count and SHA-256 | PASS |
| Tokenizer byte count, SHA-256 and merge-rank structure | PASS |
| ExecuTorch 1.0.0 AAR Java API inspection | PASS |
| Physical deployment, native inference and airplane-mode testing | PASS. realme RMX5116, Android 16, ARM64 |
| Physical voice input | PARTIAL. The app requests microphone permission and starts the on-device recognizer in airplane mode |
| Tutor modes and persistence | PASS. Each mode generated offline. Previous answer, learner name and installed model survived a cold restart |
| Physical practice and adaptation | PASS. A submitted correct answer changed the Journey recommendation from 0 of 1 to 1 of 2 attempts correct |
| Android instrumentation on phone | INCONCLUSIVE. The initial navigation harness failed. The rebuilt harness did not return a full device test result during the session |

Manual device interaction reached all five destinations. The navigation test exercises Android accessibility nodes on the physical renderer. On-device model acceptance should include import, first generation, cancellation, retry, context, all five modes and an airplane-mode restart. See [offline verification](OFFLINE_VERIFICATION.md).

The packaged debug APK is 26,879,306 bytes. See [APK checksums](../submission/SHA256SUMS.txt).
