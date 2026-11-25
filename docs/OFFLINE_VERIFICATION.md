# Offline verification

The personal learning platform was checked on the same RMX5116 under the same radio settings. The new tests verified local profile creation, Algebra discovery, live model-generated choice practice, deterministic scoring and restoration of the active learner and quiz result after restart. A generated Algebra lesson opened and saved its completion state.

On 25 November 2026, the ARM64 debug APK ran on a realme P4 Pro 5G (RMX5116), Android 16, API 36. ADB reported `airplane_mode_on=1`, `wifi_on=0`, and `mobile_data=0` during model activation, generation and the scored practice flow. See the [APK checksum](../submission/SHA256SUMS.txt) and [model manifest](../models/spinquant-manifest.json).

- **Pass.** Android Build Tools found no Internet permission in the packaged manifest.
- **Pass.** The app loaded the real Llama 3.2 1B Instruct PTE through ExecuTorch 1.0.0 and streamed a response in the normal Tutor. Native telemetry identified 116 prompt tokens and 74 generated tokens for the first measured request.
- **Pass.** Home, Profile, Journey, Tutor and Practice rendered in airplane mode. A practice answer created a Memory Hierarchy weakness. A correct reassessment changed the Journey recommendation from 0 of 1 to 1 of 2 attempts correct.
- **Pass.** The learner name, installed model, previous Tutor answer and scored evidence survived app relaunch.
- **Pass.** All five modes produced a local response across the cold-start and subsequent requests.
- **Partial.** Android displayed the microphone permission dialog and Aurora entered its on-device listening state offline. Transcription accuracy and spoken output remain unverified.

The first answer correctly compared 8- and 32-bit weight sizes and incorrectly claimed that INT8 has a wider numeric range. Scoring uses the local answer key independently of the generated explanation.
