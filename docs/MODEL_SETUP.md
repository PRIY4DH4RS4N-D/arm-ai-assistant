# Llama setup

The Android runtime is pinned to `org.pytorch:executorch-android:1.0.0`. The expected backend is XNNPACK CPU on ARM64. The app accepts a version-one ZIP with `manifest.json`, `model.pte` and `tokenizer.model`. It checks both file sizes and SHA-256 values, then tests native loading before activating the bundle. To confirm generation, run a prompt through the ordinary Tutor screen.

## Pinned publisher candidate

The [publisher revision](https://huggingface.co/executorch-community/Llama-3.2-1B-Instruct-SpinQuant_INT4_EO8-ET/tree/911221e0f49d3354cbd4697a9c09feee25cadec7) provides a Llama 3.2 1B Instruct SpinQuant INT4 EO8 PTE and original-format tokenizer. The publisher recipe references an ExecuTorch 0.6 release candidate. Android compatibility with runtime 1.0.0 requires a successful load and generation test. Both local byte counts and SHA-256 values also match the publisher’s metadata at the pinned revision. The verified local model is 1,135,951,488 bytes. The publisher export date is unverified. See [the manifest](../models/spinquant-manifest.json).

After acquiring both files under the Llama 3.2 Community License, start by checking them independently. To stage a ZIP, add the bundle option.

```powershell
python models/package_spinquant.py PATH_TO_PTE PATH_TO_TOKENIZER
python models/package_spinquant.py PATH_TO_PTE PATH_TO_TOKENIZER --bundle .tools/adapt-llama-spinquant.zip
adb push .tools/adapt-llama-spinquant.zip /sdcard/Download/
```

Import from Profile. A failure preserves the previously activated bundle. Leave at least 2 GB free in app storage for staging, plus space for the transfer ZIP if it lives on the same volume.

## Matched export

If the publisher candidate fails to load or generate, use authorized original Llama 3.2 1B Instruct checkpoints, parameters and tokenizer with the tagged ExecuTorch `v1.0.0` source checkout. Install the exporter environment on Linux/WSL using [the setup script](../models/setup-exporter.sh) and run `python models/export_model.py --source EXECUTORCH_CHECKOUT --weights AUTHORIZED_ORIGINAL_WEIGHTS --weights-revision CHECKPOINT_REVISION --output LOCAL_OUTPUT`. The script records the pinned exporter commit, dependency versions, original input hashes and output manifest. Keep original checkpoints, generated weights, credentials and bundle files outside Git.

Model formats and prior artifacts are tracked independently. The historical 848,159,808- and 807,694,464-byte files and reported prefixes could not be tied to a candidate in this checkout. Use the full hashes in the candidate manifest for current verification.
