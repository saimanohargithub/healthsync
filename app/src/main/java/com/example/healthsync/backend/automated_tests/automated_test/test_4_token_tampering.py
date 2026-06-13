#!/usr/bin/env python3
"""TEST 4 — Token Tampering: modify JWT claims without re-signing."""

import sys, os, json, time
sys.path.insert(0, os.path.dirname(__file__))
from dast_config import *

def run(token, uid):
    print("\n" + "═"*60 + "\n  TEST 4 — Token Tampering\n" + "═"*60)
    parts = token.split(".")
    if len(parts) != 3:
        print("  SKIP — not a standard JWT"); return results

    try:
        hdr_j = json.loads(b64dec(parts[0]))
        pay_j = json.loads(b64dec(parts[1]))
    except Exception as e:
        print(f"  SKIP — cannot decode: {e}"); return results

    tampered = []

    # Flip UID
    p = {**pay_j, "sub": "ATTACKER_UID", "user_id": "ATTACKER_UID"}
    tampered.append(("tampered_uid",
        f"{parts[0]}.{b64enc(json.dumps(p).encode())}.{parts[2]}"))

    # Flip email
    p2 = {**pay_j, "email": "attacker@evil.com"}
    tampered.append(("tampered_email",
        f"{parts[0]}.{b64enc(json.dumps(p2).encode())}.{parts[2]}"))

    # Extend expiry
    if "exp" in pay_j:
        p3 = {**pay_j, "exp": int(time.time()) + 86400*365}
        tampered.append(("tampered_exp",
            f"{parts[0]}.{b64enc(json.dumps(p3).encode())}.{parts[2]}"))

    # alg=none
    h2 = {**hdr_j, "alg": "none"}
    tampered.append(("alg_none",
        f"{b64enc(json.dumps(h2).encode())}.{parts[1]}."))

    url = f"{FIRESTORE_URL}/users/{uid}"
    for label, tok in tampered:
        r, el = req("GET", url, headers={"Authorization": f"Bearer {tok}"})
        if r is None: continue
        ok = r.status_code in (200,201)
        record(f"users/{uid}","GET",label,r.status_code,"401/403",ok,
               "CRITICAL" if ok else "INFO",el,"token_tampering",
               f"Tampered ({label}) ACCEPTED!" if ok else f"Rejected ({label})")

    findings = [r for r in results if r["finding"]]
    print(f"\n  Summary: {len(findings)} findings in {len(tampered)} tests")
    return results

if __name__ == "__main__":
    token, uid = authenticate()
    run(token, uid)
    save_results(SCRIPT_DIR / "results_token_tampering.json")
