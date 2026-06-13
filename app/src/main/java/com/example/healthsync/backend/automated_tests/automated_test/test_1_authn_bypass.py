#!/usr/bin/env python3
"""TEST 1 — AuthN Bypass: protected endpoints with no/malformed/expired token."""

import sys, os
sys.path.insert(0, os.path.dirname(__file__))
from dast_config import *

def run(token, uid):
    print("\n" + "═"*60 + "\n  TEST 1 — Authentication Bypass\n" + "═"*60)
    paths = [
        f"users/{uid}", f"users/{uid}/badges", f"users/{uid}/meal_plans",
        f"users/{uid}/sleep_logs", f"users/{uid}/mood_logs",
        f"users/{uid}/meals", f"user_challenges/{uid}/active",
    ]
    cases = [
        ("no_token",     {}),
        ("malformed",    {"Authorization": "Bearer not.a.jwt"}),
        ("empty_bearer", {"Authorization": "Bearer "}),
        ("basic_scheme", {"Authorization": f"Basic dGVzdDp0ZXN0"}),
    ]
    for p in paths:
        for label, hdrs in cases:
            r, el = req("GET", f"{FIRESTORE_URL}/{p}", headers=hdrs)
            if r is None:
                record(p,"GET",label,0,"403/401",False,"INFO",0,"authn_bypass","Network err")
                continue
            ok = r.status_code in (200,201)
            record(p,"GET",label,r.status_code,"403/401",ok,
                   "CRITICAL" if ok else "INFO", el, "authn_bypass",
                   f"Accessible with {label}!" if ok else f"Rejected ({label})")
    findings = [r for r in results if r["finding"]]
    print(f"\n  Summary: {len(findings)} findings out of {len(paths)*len(cases)} tests")
    return results

if __name__ == "__main__":
    token, uid = authenticate()
    run(token, uid)
    save_results(SCRIPT_DIR / "results_authn_bypass.json")
