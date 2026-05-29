from __future__ import annotations

HIGH_RISK_TERMS = ("auth", "payment", "billing", "security", "secret", "token", "migration", "deploy", "production")


def looks_high_risk(command: str) -> bool:
    text = command.lower()
    return any(term in text for term in HIGH_RISK_TERMS)
