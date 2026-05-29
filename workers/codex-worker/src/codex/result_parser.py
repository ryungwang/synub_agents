from __future__ import annotations


def infer_test_result(output: str, return_code: int) -> str:
    text = output.lower()
    if "tests failed" in text or "test failed" in text or "failed test" in text:
        return "FAILED"
    if "tests passed" in text or "test passed" in text or "passing" in text:
        return "PASSED"
    return "UNKNOWN" if return_code == 0 else "FAILED"


def summarize_output(output: str) -> str:
    if not output:
        return ""
    return output[-2500:]
