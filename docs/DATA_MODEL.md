# Local data model

Aurora stores learner records in `aurora-learning.db` using Room. The SQLite `profiles` table owns the learner identity. `learning_records` uses the composite primary key `(profileId, recordKey)` and a cascading foreign key to `profiles`.

- `legacy/*` holds migrated profile, study, practice, bookmark and original-course records.
- `platform/preferences` holds the learner's selected topics, custom topics, active topic, goal, self-reported level, language, assistant name and study schedule.
- `topic/<topic-id>` holds the lesson, notes, progress, quiz draft and responses, completed attempts and invalidated question IDs.
- `tutor/<topic-id>` stores the Tutor transcript and mode for the active topic. Profiles without a selected topic use their own legacy Tutor transcript.

The upgrade imports existing file records into a legacy profile in a Room transaction. An installation marker prevents duplicate imports. The original files remain available for recovery. Selecting a profile or topic settles pending Tutor work, flushes writes and hydrates the new owner before showing their conversation. The model bundle remains shared and outside profile records. Theme settings are keyed by profile.

Quiz mutations read and write one topic record within a Room transaction. The displayed draft carries its quiz ID and expected response count, so repeated taps and stale submissions cannot record a second score. Partial drafts can resume after reopening. The app uses local profile selection as a convenience boundary within the Android app sandbox.
