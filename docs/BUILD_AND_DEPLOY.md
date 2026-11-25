# Build and deploy

1. Install JDK 25 and set `JAVA_HOME`. Install Android SDK packages `platforms;android-37.0`, `build-tools;36.0.0` and `platform-tools`. Set `ANDROID_HOME` to the SDK path.
2. Run `gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug :app:assembleDebugAndroidTest`. On Linux or macOS use `./gradlew`.
3. Enable USB debugging on the target ARM64 Android phone. Run `adb devices -l` and confirm the expected entry is authorized. When multiple targets are present, pass `-s SERIAL` to every ADB command.
4. Install with `adb install -r app/build/outputs/apk/debug/app-debug.apk`. Launch `adb shell am start -n com.example.adapt_ai_ui/.MainActivity`.
5. Copy the authorized model bundle into Downloads and open **Profile → Import model bundle**. Exercise all five modes in Tutor before recording evidence.

For UI testing, run `gradlew.bat :app:connectedDebugAndroidTest` with the authorized device attached. The resulting APK is debug-signed. A production release requires the team signing key and release validation.
