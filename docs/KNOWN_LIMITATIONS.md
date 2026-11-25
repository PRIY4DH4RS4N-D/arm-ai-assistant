# Known limitations

- The RMX5116 device passed installation, model loading and offline generation. Device coverage beyond this phone and full speech transcription accuracy remain open.
- The publisher candidate loaded and generated on the tested phone with runtime 1.0.0. The compact model can make factual mistakes, so explanation accuracy needs continued evaluation.
- The recovered UI branch uses versioned local JSON rather than the Room implementation on the source repository’s earlier main branch. Migration between those different Android application IDs is unimplemented.
- Bundled rich lesson examples and keyword grounding primarily cover INT8 quantization. Other domains offer pathway metadata and prompts. Add authored lessons and reviewed diagnostic and coding assessments for wider coverage.
- Mastery represents scored topic accuracy. The revision notebook has interval labels and basic resolution. Calendar-driven notifications and calibrated mastery thresholds remain future work.
- The submitted APK uses the debug signing key. A team-signed production build remains future work.
