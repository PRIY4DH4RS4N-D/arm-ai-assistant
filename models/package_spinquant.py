"""Verify and optionally package the pinned, locally acquired SpinQuant artifacts."""
import argparse
import json
from pathlib import Path
import shutil
import zipfile

from export_model import sha256, verify_tokenizer


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("model", type=Path)
    parser.add_argument("tokenizer", type=Path)
    parser.add_argument("--bundle", type=Path)
    args = parser.parse_args()
    manifest = Path(__file__).with_name("spinquant-manifest.json")
    metadata = json.loads(manifest.read_text(encoding="utf-8"))
    files = {"model.pte": args.model, "tokenizer.model": args.tokenizer}
    for name, path in files.items():
        expected = metadata["files"][name]
        if path.stat().st_size != expected["bytes"] or sha256(path) != expected["sha256"]:
            parser.error(f"Byte count or SHA-256 failed for {name}")
        print(f"{name} verified: {expected['bytes']} bytes, SHA-256 {expected['sha256']}")
    verify_tokenizer(args.tokenizer)
    if args.bundle is None:
        return
    args.bundle.parent.mkdir(parents=True, exist_ok=True)
    required_bytes = sum(path.stat().st_size for path in files.values()) + 65536
    if shutil.disk_usage(args.bundle.parent).free < required_bytes:
        parser.error(f"At least {required_bytes} bytes of free space are needed to stage the bundle")
    with args.bundle.open("xb") as output:
        try:
            with zipfile.ZipFile(output, "w", compression=zipfile.ZIP_STORED) as archive:
                archive.write(manifest, "manifest.json")
                for name, path in files.items():
                    archive.write(path, name)
        except BaseException:
            output.close()
            args.bundle.unlink(missing_ok=True)
            raise
    print(args.bundle)


if __name__ == "__main__":
    main()
