#!/usr/bin/env python3
"""
Verifies elegance and bidirectional traceability across:
  1. docs/specs/functional-spec.md (FR-x.y, NFR-x, AC-x.y)
  2. docs/specs/technical-spec.md  (references to FR/NFR/AC IDs)
  3. Kotlin test suites            (references to AC-x.y / AC-x_y)
"""
from __future__ import annotations

import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent


def main() -> int:
    fs_path = ROOT / "docs/specs/functional-spec.md"
    ts_path = ROOT / "docs/specs/technical-spec.md"
    fs = fs_path.read_text(encoding="utf-8")
    ts = ts_path.read_text(encoding="utf-8")

    errors: list[str] = []

    frs = re.findall(r"^- \*\*(FR-\d+\.\d+)\*\*", fs, re.M)
    nfrs = re.findall(r"^- \*\*(NFR-\d+)\b", fs, re.M)
    acs = re.findall(r"Scenario: (AC-[\d.]+)", fs)

    all_ids = frs + nfrs + acs
    for dup in sorted({x for x in all_ids if all_ids.count(x) > 1}):
        errors.append(f"Duplicate ID in functional-spec.md: {dup}")

    for fr in frs:
        suffix = fr[3:]
        if not any(a[3:] == suffix or a[3:].startswith(suffix + ".") for a in acs):
            errors.append(f"Requirement {fr} has no matching AC-{suffix}* in functional-spec.md")

    for ac in acs:
        suffix = ac[3:]
        if not any(suffix == f[3:] or suffix.startswith(f[3:] + ".") for f in frs):
            errors.append(f"Acceptance criterion {ac} has no parent FR in functional-spec.md")

    forbidden_tech = re.findall(
        r"\b(Kotlin|Compose|SQL\w*|Gradle|Koin|Android SDK|Swift|database|API|framework|Kociemba|IDA)\b",
        fs,
        re.I,
    )
    if forbidden_tech:
        errors.append(f"Technology terms found in functional-spec.md: {sorted(set(forbidden_tech))}")

    known_ids = set(all_ids) | {re.sub(r"\.\d+$", "", f) for f in frs}
    for ref in sorted(set(re.findall(r"\b((?:N?FR|AC)-[\d.]*\d)\b", ts))):
        if ref not in known_ids:
            errors.append(f"technical-spec.md references unknown requirement ID: {ref}")

    # Verify AC references in test files point to valid AC IDs in functional-spec.md
    ac_set = set(acs)
    test_ac_refs: set[str] = set()
    for kt_file in ROOT.glob("shared/**/src/*Test/**/*.kt"):
        text = kt_file.read_text(encoding="utf-8")
        for raw in re.findall(r"\bAC-(\d+[._]\d+(?:[._]\d+)?)\b", text):
            normalized = "AC-" + raw.replace("_", ".")
            test_ac_refs.add(normalized)
            if normalized not in ac_set:
                errors.append(f"{kt_file.relative_to(ROOT)} references unknown {normalized}")

    print(
        f"Traceability summary: FR={len(frs)}, NFR={len(nfrs)}, AC={len(acs)}, "
        f"tested_ACs={sorted(test_ac_refs)}"
    )
    if errors:
        for err in errors:
            print(f"[FAIL] {err}")
        return 1
    print("[PASS] Spec elegance and traceability checks succeeded.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
