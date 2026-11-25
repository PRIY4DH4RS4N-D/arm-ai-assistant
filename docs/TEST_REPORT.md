# Test report

- **Date** 25 November 2026
- **Recovered baseline** `6547ba13502f38754d34055baa47e133f076f298`

The baseline wrapper download timed out. After provisioning the pinned Gradle, JDK and SDK locally, the host build reached app compilation. Final task results and APK identity are recorded against the delivery build.

A repeat unit-test run reproduced a race in the existing persistence writer. The flush now iterates the concurrent set without relying on its changing size.

| Check | Result |
| --- | --- |
| `:app:assembleDebug` | PASS |
| `:app:testDebugUnitTest` | PASS. 116 tests, zero failures or skips |
| `:app:assembleDebugAndroidTest` | PASS. Compiled, awaiting device execution |
| `:app:lintDebug` | PASS. Zero errors and 30 warnings |
| APK signature and contents | PASS. Verified debug signature, ARM64 ExecuTorch library, app backup disabled and no Internet permission |
| Publisher model byte count and SHA-256 | PASS |
| Tokenizer byte count, SHA-256 and merge-rank structure | PASS |
| ExecuTorch 1.0.0 AAR Java API inspection | PASS |
| Physical deployment, native inference and airplane-mode testing | BLOCKED. ADB returned no devices. |
| Physical voice input/output, screenshots and demo | BLOCKED. ADB returned no devices. |

The Android navigation instrumentation opens each of the five destinations and returns to the Aurora prompt. On-device model acceptance should include import, first generation, cancellation, retry, context, all five modes and an airplane-mode restart. See [offline verification](OFFLINE_VERIFICATION.md).

The packaged debug APK is 26,879,306 bytes. See [APK checksums](../submission/SHA256SUMS.txt).
