# Aurora development delivery

## Personal learning platform

- Added local profiles and guests with Room-backed learner, topic and conversation records.
- Added offline subject search, multiple interests, custom topics and editable study goals and schedules.
- Added a topic workspace for local AI lessons, model-generated assessments, deterministic scoring, mastery, revision and note import.
- Refreshed Home and added profile and topic navigation inspired by the supplied Aurora mockup.
- Corrected welcome-screen text contrast and fit the Home greeting and orb on the connected phone.

- Added a minimal Aurora Home with a lifecycle-aware Canvas orb, glass text input and on-device speech capability detection.
- Updated shared dark and light colors, animated bottom-tab selection and added an editable learner name.
- Kept five core destinations and made the prior dashboard accessible through Learning overview.
- Replaced the production simulated generation path with ExecuTorch 1.0.0, streamed responses, cancellation, retry, context reset and local lesson grounding.
- Added private transactional model import with checksum and size checks, native load validation, status, unload and confirmed deletion.
- Added a daily time plan and practice recommendations based on scored attempts. Fresh installations start with unassessed topic statistics.
- Fixed a persistence flush race when asynchronous writes complete during the job snapshot.
- Fixed the native loader on Android by using the ExecuTorch 1.0.0 text-only constructor for a self-contained PTE.
- Added model packaging, validation and submission documentation. See the [test report](TEST_REPORT.md) for current evidence.
