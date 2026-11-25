# EMBER

EMBER is an on-device personalised learning assistant for the ARM Bharat AI-SoC Challenge, PS4. The Android app connects a local catalogue, learner profiles and study plans with an offline Llama tutor, scored practice and revision. The cinematic introduction and animated orb run in Jetpack Compose.

## Submission

- [Public two-minute phone demonstration](https://github.com/PRIY4DH4RS4N-D/arm-ai-assistant/releases/download/ember-v1.1.0/EMBER_PS4_DEMO_120S.mp4)
- [Final project report](submission/EMBER_Final_Project_Report.pdf)
- [APK build and installation guide](docs/RELEASE_GUIDE.md)
- [Phone screenshots](docs/ui-verification/) and [completion matrix](docs/COMPLETION_MATRIX.md)

The recording shows the software running on the realme P4 Pro 5G RMX5116, ARM64, Android 16, in airplane mode with Wi-Fi and mobile data off. It shows live local Tutor output, a generated Algebra quiz, deterministic scoring and persisted progress.

## Build and run

Install JDK 25, Android SDK Platform 37.0, Build Tools 36.0.0 and Android Platform Tools.

~~~powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug :app:assembleDebugAndroidTest :app:assembleRelease
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.example.adapt_ai_ui/.MainActivity
~~~

The APK targets ARM64. In Profile, open Model Status and import an authorised Llama 3.2 1B Instruct bundle from local storage. Local catalogue browsing, saved records and bundled OCR are available before that import. Voice depends on available on-device Android speech services. [Model provisioning](docs/MODEL_PROVISIONING.md)

## Engineering documentation

- [Engineering baseline](docs/ENGINEERING_BASELINE.md), [product overview](docs/PRODUCT_OVERVIEW.md) and [architecture](docs/ARCHITECTURE.md)
- [Offline architecture](docs/OFFLINE_ARCHITECTURE.md), [Llama and ExecuTorch](docs/LLAMA_EXECUTORCH_INTEGRATION.md) and [benchmarks](docs/MODEL_BENCHMARKS.md)
- [Engineering catalogue](docs/ENGINEERING_COURSE_CATALOGUE.md), [learning engine](docs/LEARNING_ENGINE.md) and [camera OCR](docs/CAMERA_OCR.md)
- [Device validation](docs/DEVICE_VALIDATION.md), [test report](docs/TEST_REPORT.md), [UI verification](docs/UI_VERIFICATION.md) and [privacy](docs/PRIVACY_AND_SECURITY.md)
- [Release guide](docs/RELEASE_GUIDE.md), [demo script](docs/DEMO_SCRIPT.md) and [submission instructions](submission/README.md)

The repository builds without licensed model weights. See the [third-party and model notices](docs/THIRD_PARTY_NOTICES.md). Source history originates in [Surya10-cse/arm-ps4-learning-assistant](https://github.com/Surya10-cse/arm-ps4-learning-assistant).
