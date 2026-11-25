# ADAPT-AI · Aurora

Aurora is an Android learning assistant for ARM Bharat AI-SoC Challenge PS4. Set a learning goal, follow a curriculum, practise offline, and use quiz evidence to choose what to review. With a verified model bundle installed, the Tutor uses the device’s ExecuTorch runtime for Llama responses.

Create a local profile or continue as a guest. Explore 88 offline topics, save several interests, or define a custom subject and topic. Open a topic to set a goal, generate lessons and quizzes, maintain notes, or review its study schedule and progress. Profile lets you switch learners and change AI preferences.

## Build and run

Install JDK 25, Android SDK Platform 37.0, Build Tools 36.0.0, and Android Platform Tools. From this repository on Windows:

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug :app:assembleDebugAndroidTest
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.example.adapt_ai_ui/.MainActivity
```

The APK targets ARM64. The Model Manager is in Profile. Import an authorized Llama 3.2 1B Instruct bundle using the [model setup guide](docs/MODEL_SETUP.md). The catalog, saved material, study plan and completed assessments remain available without loading the model. New AI lessons, quizzes and Tutor responses require the installed model. Voice input and read-aloud require the device’s offline speech services.

## Architecture

Native Kotlin and Jetpack Compose provide the five destinations. Tutor, lesson generation and quiz generation reuse the pinned ExecuTorch 1.0.0 runtime. Room stores learning records scoped to the selected local learner and imports data from the previous release. Kotlin scores validated quizzes and updates progress from the recorded answers. See the [architecture](docs/ARCHITECTURE.md), [data model](docs/DATA_MODEL.md), [learning engine](docs/LEARNING_ENGINE.md), [quiz engine](docs/QUIZ_ENGINE.md) and [technical report](docs/TECHNICAL_REPORT.md).

## Validation and submission

- [Debug APK and submission files](submission/README.md)
- [Host and device test report](docs/TEST_REPORT.md) and [compliance matrix](docs/COMPLIANCE_MATRIX.md)
- [Offline verification](docs/OFFLINE_VERIFICATION.md) and [benchmark protocol](docs/PERFORMANCE_BENCHMARK.md)
- [Two-minute phone demo script](docs/DEMO_SCRIPT_120S.md) and [recording checklist](submission/demo/RECORDING_CHECKLIST.md)
- [Known limitations](docs/KNOWN_LIMITATIONS.md) and [release notes](docs/RELEASE_NOTES.md)

On-device evidence from realme P4 Pro 5G, Android 16: [two-minute demo](submission/demo/ADAPT_AI_PS4_Demo_120s.mp4), [Aurora Home](submission/screenshots/aurora-home.png), [Journey](submission/screenshots/learning-journey.png), and [offline Tutor](submission/screenshots/offline-tutor.png).

## Sources and notices

Source history originates in [Surya10-cse/arm-ps4-learning-assistant](https://github.com/Surya10-cse/arm-ps4-learning-assistant). See the [third-party and model notices](docs/THIRD_PARTY_NOTICES.md).
