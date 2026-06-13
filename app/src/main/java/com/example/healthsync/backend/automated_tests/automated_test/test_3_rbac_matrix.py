#!/usr/bin/env python3
"""TEST 3 — RBAC Matrix: unauthenticated vs authenticated across all paths."""

import sys, os
sys.path.insert(0, os.path.dirname(__file__))
from dast_config import *

def run(token, uid):
    print("\n" + "═"*60 + "\n  TEST 3 — RBAC Matrix\n" + "═"*60)
    paths = [
        "users", f"users/{uid}", f"users/{uid}/badges",
        f"users/{uid}/meal_plans", f"users/{uid}/sleep_logs",
        f"users/{uid}/mood_logs", f"users/{uid}/meals",
        "user_challenges", f"user_challenges/{uid}/active",
    ]
    roles = [("unauthenticated", {}), ("authenticated", auth_hdr(token))]
    for p in paths:
        for rname, hdrs in roles:
            r, el = req("GET", f"{FIRESTORE_URL}/{p}", headers=hdrs)
            if r is None: continue
            if rname == "unauthenticated":
                ok = r.status_code in (200,201)
                record(p,"GET",rname,r.status_code,"403",ok,
                       "CRITICAL" if ok else "INFO",el,"rbac_matrix",
                       "Unauth access allowed!" if ok else "Denied")
            else:
                expected = "403" if p == "user_challenges" else "200"
                bad = r.status_code not in ([200, 201] if expected == "200" else [403, 401])
                record(p,"GET",rname,r.status_code,expected,bad,
                       "MEDIUM" if bad else "INFO",el,"rbac_matrix",
                       f"Auth user {'blocked' if bad and expected == '200' else 'access incorrect'}" if bad else "OK")
    findings = [r for r in results if r["finding"]]
    print(f"\n  Summary: {len(findings)} findings in {len(paths)*len(roles)} tests")
    return results

if __name__ == "__main__":
    token, uid = authenticate()
    run(token, uid)
    save_results(SCRIPT_DIR / "results_rbac_matrix.json")
