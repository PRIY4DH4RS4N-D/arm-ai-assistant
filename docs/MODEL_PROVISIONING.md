# Model provisioning

Follow the [verified publisher bundle procedure](MODEL_SETUP.md). The [manifest](../models/spinquant-manifest.json) records provenance, exact file sizes, hashes, quantisation, backend and context. The [packager](../models/package_spinquant.py) builds the import ZIP. The [export tool](../models/export_model.py) supports an authorised checkpoint and the documented target settings.

Transfer the ZIP to the phone, open Profile, choose Model Status and import the bundle. Wait for verification before sending a Tutor request. Verify a real offline response after importing. See [integration details](LLAMA_EXECUTORCH_INTEGRATION.md).
