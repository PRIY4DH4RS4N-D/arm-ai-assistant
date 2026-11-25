# Aurora submission files

- [Debug APK](ADAPT-AI-debug.apk) for ARM64 Android. Verify its SHA-256 against [SHA256SUMS.txt](SHA256SUMS.txt).
- [Technical report](../docs/TECHNICAL_REPORT.md)
- [Compliance matrix](../docs/COMPLIANCE_MATRIX.md)
- [Model setup](../docs/MODEL_SETUP.md)
- [Physical phone demonstration](demo/ADAPT_AI_PS4_Demo_120s.mp4), [demo narration outline](demo/DEMO_NARRATION.md), and [recording checklist](demo/RECORDING_CHECKLIST.md)
- [Aurora Home](screenshots/aurora-home.png), [model manager](screenshots/model-manager.png), [Journey](screenshots/learning-journey.png), [offline Tutor](screenshots/offline-tutor.png)

The debug-signed APK uses a separately provisioned model. The silent demonstration shows the application running on the realme RMX5116 while airplane mode is enabled. The model makes an arithmetic error in the live example. The correct raw payload sizes are 1,000 bytes for INT8 and 4,000 bytes for FP32.
