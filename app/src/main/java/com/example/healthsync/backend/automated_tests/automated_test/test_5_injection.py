#!/usr/bin/env python3
"""TEST 5 — Injection Probes: path traversal, SQLi/NoSQLi detection payloads."""

import sys, os
sys.path.insert(0, os.path.dirname(__file__))
from dast_config import *

def run(token, uid):
    print("\n" + "═"*60 + "\n  TEST 5 — Injection Probes\n" + "═"*60)
    hdrs = auth_hdr(token)

    # Baseline
    br, bt = req("GET", f"{FIRESTORE_URL}/users/{uid}", headers=hdrs)
    base_t = bt if br else 1.0

    payloads = [
        ("path_traversal",  "users/../users"),
        ("traversal_deep",  f"users/{uid}/../../users"),
        ("nosqli_or",       "users/' OR '1'='1"),
        ("nosqli_gt",       "users/{$gt: ''}"),
        ("special___",      "users/__internal__"),
        ("dot_meta",        "users/.metadata"),
    ]
    for label, path in payloads:
        r, el = req("GET", f"{FIRESTORE_URL}/{path}", headers=hdrs)
        if r is None: continue
        timing = (el > base_t * 3 and base_t > 0)
        leak   = any(w in r.text.lower() for w in
                     ("stack trace","exception","internal server"))
        is200  = r.status_code == 200
        found  = is200 or timing or leak
        reasons = []
        if is200:  reasons.append("got 200")
        if timing: reasons.append(f"slow {el:.2f}s vs {base_t:.2f}s")
        if leak:   reasons.append("info leak")
        record(path[:50],"GET","injection",r.status_code,"4xx",found,
               "MEDIUM" if found else "INFO",el,"injection",
               f"{label}: {', '.join(reasons)}" if found
               else f"{label}: handled safely")

    findings = [r for r in results if r["finding"]]
    print(f"\n  Summary: {len(findings)} findings in {len(payloads)} tests")
    return results

if __name__ == "__main__":
    token, uid = authenticate()
    run(token, uid)
    save_results(SCRIPT_DIR / "results_injection.json")
