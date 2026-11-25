# Offline verification

- **Date** 25 November 2026
- **Device** ADB returned an empty device list.
- **Model** Publisher candidate verified on the host. Android inference pending. See the [model manifest](../models/spinquant-manifest.json).

| Check | Result |
| --- | --- |
| Main app manifest has no internet permission | PASS. Inspected the packaged APK with Android Build Tools. |
| Home, Profile, Journey and local course navigation in airplane mode | BLOCKED |
| Lessons, scoring, recommendations and revision in airplane mode | BLOCKED |
| Native model load, first response and follow-up in airplane mode | BLOCKED |
| Conversation and learner state after force-stop and restart | BLOCKED |
| Offline transcription and speech output | BLOCKED |

After installing the debug APK and authorized model, verify the phone’s airplane mode and explicitly check that Wi-Fi and mobile data are off. Test the rows above from a demo profile. For Tutor, test once after a cold start to exercise model verification/loading, then use another prompt in the same session. Stop a longer response and verify that retry and subsequent turns work. Record the build hash, model hash and anonymized hardware details.
