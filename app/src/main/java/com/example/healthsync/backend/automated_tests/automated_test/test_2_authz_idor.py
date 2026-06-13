#!/usr/bin/env python3
"""TEST 2 — AuthZ / IDOR: access other users' data with a valid token."""

import sys, os
sys.path.insert(0, os.path.dirname(__file__))
from dast_config import *

def run(token, uid):
    print("\n" + "═"*60 + "\n  TEST 2 — Authorization / IDOR\n" + "═"*60)
    fake = "IDOR_TEST_VICTIM_00000"
    hdrs = auth_hdr(token)

    # List entire users collection
    r, el = req("GET", f"{FIRESTORE_URL}/users", headers=hdrs)
    if r and r.status_code == 200:
        try: n = len(r.json().get("documents",[]))
        except: n = 0
        record("users","GET","auth_list",200,"403",True,"HIGH",el,
               "authz_idor",f"Can enumerate ALL users ({n} docs)")
    elif r:
        record("users","GET","auth_list",r.status_code,"403",False,"INFO",el,
               "authz_idor","Listing blocked")

    # Cross-user paths
    for sub in ("","badges","meal_plans","sleep_logs","mood_logs","meals"):
        p = f"users/{fake}/{sub}".rstrip("/")
        r, el = req("GET", f"{FIRESTORE_URL}/{p}", headers=hdrs)
        if r is None: continue
        if r.status_code == 200:
            record(p,"GET","cross_user",200,"403",True,"HIGH",el,
                   "authz_idor",f"IDOR: other user's /{sub or 'profile'} readable!")
        elif r.status_code == 404:
            record(p,"GET","cross_user",404,"403",True,"HIGH",el,
                   "authz_idor","IDOR: rules allow cross-user (404=doc absent)")
        else:
            record(p,"GET","cross_user",r.status_code,"403",False,"INFO",el,
                   "authz_idor","Correctly denied")

    # Cross-user challenges
    p = f"user_challenges/{fake}/active"
    r, el = req("GET", f"{FIRESTORE_URL}/{p}", headers=hdrs)
    if r:
        ok = r.status_code in (200,404)
        record(p,"GET","cross_user",r.status_code,"403",ok,
               "HIGH" if ok else "INFO",el,"authz_idor",
               "IDOR on challenges!" if ok else "Denied")

    # Own data baseline
    r, el = req("GET", f"{FIRESTORE_URL}/users/{uid}", headers=hdrs)
    if r:
        record(f"users/{uid}","GET","self",r.status_code,"200",
               r.status_code!=200,"INFO",el,"authz_idor",
               "Own profile OK" if r.status_code==200 else "Cannot access own")

    findings = [r for r in results if r["finding"]]
    print(f"\n  Summary: {len(findings)} IDOR findings")
    return results

if __name__ == "__main__":
    token, uid = authenticate()
    run(token, uid)
    save_results(SCRIPT_DIR / "results_authz_idor.json")
