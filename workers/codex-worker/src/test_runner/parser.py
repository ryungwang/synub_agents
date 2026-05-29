from __future__ import annotations


def normalize_test_result(value: str) -> str:
    value = value.upper()
    if value in {"PASSED", "FAILED"}:
        return value
    return "UNKNOWN"
