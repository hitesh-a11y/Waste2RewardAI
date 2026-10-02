"""Prepare a waste dataset arranged as dataset/train, validation and test.
This script intentionally does not claim a particular dataset or accuracy."""
from pathlib import Path
import shutil, random

CLASSES = ["plastic","paper","cardboard","glass","metal","organic","e-waste","hazardous","textile","other"]
ROOT = Path("dataset")
for split in ["train","validation","test"]:
    for cls in CLASSES:
        (ROOT/split/cls).mkdir(parents=True, exist_ok=True)
print("Dataset folders created. Add legally usable images to each class.")
