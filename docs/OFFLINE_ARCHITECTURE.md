# Offline architecture

Room, the topic index, the assessment engine and the OCR model reside on the device. Text generation requires a separately authorised and imported Llama bundle. Provisioning can use local storage or USB transfer. The Android app manifest removes INTERNET.

Core learning tests run in airplane mode with Wi-Fi and mobile data disabled. Voice uses system-provided local recognition and offline TTS when available. See [offline checks](OFFLINE_VERIFICATION.md), [device evidence](DEVICE_VALIDATION.md) and [camera OCR](CAMERA_OCR.md).
