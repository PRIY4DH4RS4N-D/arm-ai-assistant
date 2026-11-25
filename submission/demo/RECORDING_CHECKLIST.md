# Phone recording checklist

- Build and identify the debug APK by its SHA-256. Install on the authorized ARM64 phone using the [deployment guide](../../docs/BUILD_AND_DEPLOY.md).
- Prepare a labelled demo profile, hide notification previews, and verify a live Tutor response in airplane mode before recording.
- Use the app and device sequence in the [demo script](../../docs/DEMO_SCRIPT_120S.md). Enable microphone narration in the native screen recorder if available.
- Save the verified recording as `submission/demo/ADAPT_AI_PS4_Demo_120s.mp4`. The Android `adb shell screenrecord --time-limit 120` route may produce silent video. Check media streams before describing the result.
- Run `ffprobe -v error -show_entries format=duration:stream=codec_type,width,height -of json ADAPT_AI_PS4_Demo_120s.mp4`. Review the full playback, narration, legibility and absence of personal information.
- Record date, build hash, model hash, duration, resolution and audio presence. Use a release asset if the video exceeds normal repository file limits.

## Published capture

Recorded on 25 November 2026 from realme RMX5116 with Android 16 in airplane mode. The H.264 MP4 joins two screen captures from the same phone and build. It runs 119.67 seconds at 720 × 1576 and decoded end to end with FFmpeg. The practice segment shows an actual correct answer and Journey changing from 0 of 1 to 1 of 2 attempts correct. The clip is silent. See the [media and APK checksums](../SHA256SUMS.txt).
