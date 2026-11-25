# Release guide

Build the debug and release variants using the pinned project toolchain. Install the debug APK with adb install -r and import an authorised model ZIP locally if Model Management shows it is missing. The target ABI is arm64-v8a.

A competition reviewer needs the repository, public two-minute demonstration, and final PDF in submission. A production APK requires team-controlled signing. Never store team signing material or Llama assets in Git. See the [provisioning guide](MODEL_PROVISIONING.md) and [submission instructions](../submission/README.md).
