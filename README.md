# ADAPT-AI · Aurora

Aurora is an Android learning assistant for ARM Bharat AI-SoC Challenge PS4. Set a learning goal, follow a curriculum, practise offline, and use quiz evidence to choose what to review. With a verified model bundle installed, the Tutor uses the device’s ExecuTorch runtime for Llama responses.

## Build and run

Install JDK 25, Android SDK Platform 37.0, Build Tools 36.0.0, and Android Platform Tools. From this repository on Windows:

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug :app:assembleDebugAndroidTest
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.example.adapt_ai_ui/.MainActivity
```

The APK targets ARM64. The Model Manager is at the top of Profile. Import an authorized Llama 3.2 1B Instruct bundle using the [model setup guide](docs/MODEL_SETUP.md). Curriculum, assessments, local records and revision work without a model. Voice input and read-aloud require the device’s offline speech services.

## Architecture

Native Kotlin and Jetpack Compose provide the five destinations. The Tutor uses a ViewModel, repository, and a pinned ExecuTorch 1.0.0 adapter. The current redesigned branch stores learning data in application-private JSON. Assessments update progress directly from answer keys, independently of inference. See the [architecture](docs/ARCHITECTURE.md) and [technical report](docs/TECHNICAL_REPORT.md).

## Validation and submission

- [Debug APK and submission files](submission/README.md)
- [Host and device test report](docs/TEST_REPORT.md) and [compliance matrix](docs/COMPLIANCE_MATRIX.md)
- [Offline verification](docs/OFFLINE_VERIFICATION.md) and [benchmark protocol](docs/PERFORMANCE_BENCHMARK.md)
- [Two-minute phone demo script](docs/DEMO_SCRIPT_120S.md) and [recording checklist](submission/demo/RECORDING_CHECKLIST.md)
- [Known limitations](docs/KNOWN_LIMITATIONS.md) and [release notes](docs/RELEASE_NOTES.md)

On-device evidence from realme P4 Pro 5G, Android 16: [two-minute demo](submission/demo/ADAPT_AI_PS4_Demo_120s.mp4), [Aurora Home](submission/screenshots/aurora-home.png), [Journey](submission/screenshots/learning-journey.png), and [offline Tutor](submission/screenshots/offline-tutor.png).

## Sources and notices

Source history originates in [Surya10-cse/arm-ps4-learning-assistant](https://github.com/Surya10-cse/arm-ps4-learning-assistant). See the [third-party and model notices](docs/THIRD_PARTY_NOTICES.md).
