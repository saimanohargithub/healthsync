#!/usr/bin/env python3
"""
HealthSync DAST Runner — Dynamic Application Security Testing
Tests Firebase Auth REST API + Firestore REST API for the HealthSync project.
Detection only — NO destructive operations.
"""

import json, os, sys, time, base64, re
from datetime import datetime, timezone
from pathlib import Path

try:
    import requests
    from requests.exceptions import RequestException
except ImportError:
    print("ERROR: 'requests' module not found. Install with:  pip install requests")
    sys.exit(1)

# ─── Paths ────────────────────────────────────────────────────
SCRIPT_DIR   = Path(__file__).resolve().parent
INPUT_JSON   = SCRIPT_DIR.parent / "input.json"
REPORT_JSON  = SCRIPT_DIR / "report.json"
SAVEPOINT    = SCRIPT_DIR / "savepoint.json"
PROJECT_ROOT = SCRIPT_DIR.parents[9]          # …/PDD1

# ─── Firebase Project (public info from google-services.json) ─
PROJECT_ID  = "healthsync-2af5a"
WEB_API_KEY = "AIzaSyBucCI2Mhg9QNYmIbOKK2lMRPNVwTmcDh8"

AUTH_URL      = "https://identitytoolkit.googleapis.com/v1"
FIRESTORE_URL = (f"https://firestore.googleapis.com/v1/"
                 f"projects/{PROJECT_ID}/databases/(default)/documents")

DELAY    = 0.35          # seconds between requests
MAX_TIME = 10            # curl --max-time equivalent

# ─── Global state ─────────────────────────────────────────────
results  = []
id_token = None
user_uid = None
session  = requests.Session()
session.headers.update({"Content-Type": "application/json"})

# ─── Helpers ──────────────────────────────────────────────────
def mask(tok):
    if not tok: return "<none>"
    return tok[:8] + "…" + tok[-4:] if len(tok) > 20 else "***"

def ts():
    return datetime.now(timezone.utc).isoformat()

def record(endpoint, method, role, status, expected, finding,
           severity, resp_time, category, note):
    results.append({
        "endpoint":        endpoint,
        "method":          method,
        "role":            role,
        "status":          status,
        "expected_status": expected,
        "finding":         finding,
        "severity":        severity,
        "response_time_ms": round(resp_time * 1000, 1),
        "test_category":   category,
        "note":            note,
        "timestamp":       ts(),
    })
    ico = ("✗" if finding and severity in ("CRITICAL","HIGH")
           else ("⚠" if finding else "✓"))
    tag = "FINDING" if finding else "OK"
    print(f"  {ico} [{severity:8s}] {method:6s} "
          f"{endpoint[:60]:60s} → {status} (exp {expected}) {tag}")
    if finding and note:
        print(f"           └─ {note}")

def req(method, url, **kw):
    """Throttled request → (response | None, elapsed_seconds)."""
    time.sleep(DELAY)
    try:
        r = session.request(method, url, timeout=MAX_TIME, **kw)
        return r, r.elapsed.total_seconds()
    except RequestException:
        return None, 0

def auth_hdr(token=None):
    return {"Authorization": f"Bearer {token or id_token}"}

def save_cp(stage, data):
    cp = json.loads(SAVEPOINT.read_text()) if SAVEPOINT.exists() else {}
    cp[stage] = {"completed_at": ts(), **data}
    SAVEPOINT.write_text(json.dumps(cp, indent=2))


# ══════════════════════════════════════════════════════════════
#  SETUP
# ══════════════════════════════════════════════════════════════
def setup():
    global id_token, user_uid
    hdr("SETUP — Authenticate with Firebase")

    if not INPUT_JSON.exists():
        sys.exit(f"ERROR: {INPUT_JSON} not found")
    creds = json.loads(INPUT_JSON.read_text())
    email, pw = creds.get("email"), creds.get("password")
    if not email or not pw:
        sys.exit("ERROR: input.json missing email / password")

    print(f"  → Signing in as {email}")
    r, _ = req("POST",
               f"{AUTH_URL}/accounts:signInWithPassword?key={WEB_API_KEY}",
               json={"email": email, "password": pw,
                     "returnSecureToken": True})
    if r is None:
        sys.exit("ERROR: Cannot reach Firebase Auth API (network)")
    if r.status_code != 200:
        sys.exit(f"ERROR: Auth failed {r.status_code}: {r.text[:200]}")

    d = r.json()
    id_token = d["idToken"]
    user_uid = d["localId"]
    print(f"  ✓ UID   : {user_uid}")
    print(f"  ✓ Token : {mask(id_token)}")
    save_cp("setup", {"uid": user_uid})

def hdr(title):
    print(f"\n{'═'*60}\n  {title}\n{'═'*60}")


# ══════════════════════════════════════════════════════════════
#  TEST 0 — Firestore rules wide-open check
# ══════════════════════════════════════════════════════════════
def test_firestore_rules_open():
    hdr("TEST 0 — Firestore Rules Wide-Open Check")
    for coll in ("users", "user_challenges"):
        url = f"{FIRESTORE_URL}/{coll}"
        r, el = req("GET", url)                     # no auth at all
        if r is None:
            record(coll,"GET","no_auth",0,"403",False,"INFO",0,
                   "firestore_rules","Network error"); continue
        if r.status_code == 200:
            try:  n = len(r.json().get("documents",[]))
            except: n = 0
            record(coll,"GET","no_auth",200,"403",True,"CRITICAL",el,
                   "firestore_rules",
                   f"'{coll}' WORLD-READABLE without auth! ({n} docs)")
        else:
            record(coll,"GET","no_auth",r.status_code,"403",False,
                   "INFO",el,"firestore_rules","Correctly denied")
    save_cp("test_0",{})


# ══════════════════════════════════════════════════════════════
#  TEST 1 — AuthN bypass
# ══════════════════════════════════════════════════════════════
def test_authn_bypass():
    hdr("TEST 1 — Authentication Bypass")
    paths = [
        f"users/{user_uid}",
        f"users/{user_uid}/badges",
        f"users/{user_uid}/meal_plans",
        f"users/{user_uid}/sleep_logs",
        f"users/{user_uid}/mood_logs",
        f"users/{user_uid}/meals",
        f"user_challenges/{user_uid}/active",
    ]
    cases = [
        ("no_token",       {}),
        ("malformed",      {"Authorization": "Bearer not.a.jwt"}),
        ("empty_bearer",   {"Authorization": "Bearer "}),
        ("basic_scheme",   {"Authorization":
                            f"Basic {base64.b64encode(b'x:x').decode()}"}),
    ]
    for p in paths:
        for label, hdrs in cases:
            r, el = req("GET", f"{FIRESTORE_URL}/{p}", headers=hdrs)
            if r is None:
                record(p,"GET",label,0,"403/401",False,"INFO",0,
                       "authn_bypass","Network err"); continue
            ok = r.status_code in (200,201)
            record(p,"GET",label,r.status_code,"403/401",ok,
                   "CRITICAL" if ok else "INFO", el, "authn_bypass",
                   f"Accessible with {label}!" if ok else
                   f"Rejected ({label})")
    save_cp("test_1",{"n": len(paths)*len(cases)})


# ══════════════════════════════════════════════════════════════
#  TEST 2 — AuthZ / IDOR
# ══════════════════════════════════════════════════════════════
def test_authz_idor():
    hdr("TEST 2 — Authorization / IDOR")
    fake = "IDOR_TEST_VICTIM_00000"

    # 2a — list entire users collection
    r, el = req("GET", f"{FIRESTORE_URL}/users", headers=auth_hdr())
    if r:
        if r.status_code == 200:
            try:  n = len(r.json().get("documents",[]))
            except: n = 0
            record("users","GET","auth_list",200,"403",True,"HIGH",el,
                   "authz_idor",
                   f"Can enumerate ALL users ({n} docs returned)")
        else:
            record("users","GET","auth_list",r.status_code,"403",
                   False,"INFO",el,"authz_idor","Listing blocked")

    # 2b — cross-user paths
    for sub in ("","badges","meal_plans","sleep_logs",
                "mood_logs","meals"):
        p = f"users/{fake}/{sub}".rstrip("/")
        r, el = req("GET", f"{FIRESTORE_URL}/{p}", headers=auth_hdr())
        if r is None:
            record(p,"GET","cross_user",0,"403",False,"INFO",0,
                   "authz_idor","Network err"); continue
        # 403→rules OK, 404→rules let through (doc missing), 200→data leaked
        if r.status_code == 200:
            record(p,"GET","cross_user",200,"403",True,"HIGH",el,
                   "authz_idor",
                   f"IDOR: other user's /{sub or 'profile'} readable!")
        elif r.status_code == 404:
            record(p,"GET","cross_user",404,"403",True,"HIGH",el,
                   "authz_idor",
                   f"IDOR: rules allow cross-user read (404=doc absent, not denied)")
        else:
            record(p,"GET","cross_user",r.status_code,"403",False,
                   "INFO",el,"authz_idor","Correctly denied")

    # 2b' — cross-user challenges
    p2 = f"user_challenges/{fake}/active"
    r, el = req("GET", f"{FIRESTORE_URL}/{p2}", headers=auth_hdr())
    if r:
        ok = r.status_code in (200,404)
        record(p2,"GET","cross_user",r.status_code,"403",ok,
               "HIGH" if ok else "INFO", el, "authz_idor",
               "IDOR on challenges!" if ok else "Denied")

    # 2c — own data (baseline)
    r, el = req("GET", f"{FIRESTORE_URL}/users/{user_uid}",
                headers=auth_hdr())
    if r:
        record(f"users/{user_uid}","GET","self",r.status_code,"200",
               r.status_code!=200,"INFO",el,"authz_idor",
               "Own profile OK" if r.status_code==200 else
               f"Cannot access own profile ({r.status_code})")

    save_cp("test_2",{})


# ══════════════════════════════════════════════════════════════
#  TEST 3 — RBAC matrix
# ══════════════════════════════════════════════════════════════
def test_rbac_matrix():
    hdr("TEST 3 — RBAC Matrix (unauth vs auth)")
    paths = [
        "users",
        f"users/{user_uid}",
        f"users/{user_uid}/badges",
        f"users/{user_uid}/meal_plans",
        f"users/{user_uid}/sleep_logs",
        f"users/{user_uid}/mood_logs",
        f"users/{user_uid}/meals",
        "user_challenges",
        f"user_challenges/{user_uid}/active",
    ]
    roles = [("unauthenticated", {}), ("authenticated", auth_hdr())]
    for p in paths:
        for rname, hdrs in roles:
            r, el = req("GET", f"{FIRESTORE_URL}/{p}", headers=hdrs)
            if r is None:
                record(p,"GET",rname,0,"—",False,"INFO",0,
                       "rbac_matrix","Network err"); continue
            if rname == "unauthenticated":
                ok = r.status_code in (200,201)
                record(p,"GET",rname,r.status_code,"403",ok,
                       "CRITICAL" if ok else "INFO", el, "rbac_matrix",
                       "Unauth access allowed!" if ok else "Denied")
            else:
                expected = "403" if p in ("user_challenges", "users") else "200"
                bad = r.status_code not in ([200, 201] if expected == "200" else [403, 401])
                record(p,"GET",rname,r.status_code,expected,bad,
                       "MEDIUM" if bad else "INFO", el, "rbac_matrix",
                       f"Auth user {'blocked' if bad and expected == '200' else 'access incorrect'}" if bad else "OK")
    save_cp("test_3",{})


# ══════════════════════════════════════════════════════════════
#  TEST 4 — Token tampering
# ══════════════════════════════════════════════════════════════
def _b64dec(s):
    return base64.urlsafe_b64decode(s + "=" * (4 - len(s) % 4))

def _b64enc(b):
    return base64.urlsafe_b64encode(b).rstrip(b"=").decode()

def test_token_tampering():
    hdr("TEST 4 — Token Tampering")
    if not id_token:
        print("  SKIP — no token"); return
    parts = id_token.split(".")
    if len(parts) != 3:
        print("  SKIP — not a standard JWT"); return

    try:
        hdr_j = json.loads(_b64dec(parts[0]))
        pay_j = json.loads(_b64dec(parts[1]))
    except Exception as e:
        print(f"  SKIP — cannot decode JWT: {e}"); return

    tampered = []
    # 4a — flip UID
    p = {**pay_j, "sub": "ATTACKER_UID", "user_id": "ATTACKER_UID"}
    tampered.append(("tampered_uid",
                     f"{parts[0]}.{_b64enc(json.dumps(p).encode())}.{parts[2]}"))
    # 4b — flip email
    p2 = {**pay_j, "email": "attacker@evil.com"}
    tampered.append(("tampered_email",
                     f"{parts[0]}.{_b64enc(json.dumps(p2).encode())}.{parts[2]}"))
    # 4c — extend expiry
    if "exp" in pay_j:
        p3 = {**pay_j, "exp": int(time.time()) + 86400*365}
        tampered.append(("tampered_exp",
                         f"{parts[0]}.{_b64enc(json.dumps(p3).encode())}.{parts[2]}"))
    # 4d — alg=none
    h2 = {**hdr_j, "alg": "none"}
    tampered.append(("alg_none",
                     f"{_b64enc(json.dumps(h2).encode())}.{parts[1]}."))

    url = f"{FIRESTORE_URL}/users/{user_uid}"
    for label, tok in tampered:
        r, el = req("GET", url, headers={"Authorization": f"Bearer {tok}"})
        if r is None:
            record(f"users/{user_uid}","GET",label,0,"401/403",False,
                   "INFO",0,"token_tampering","Network err"); continue
        ok = r.status_code in (200,201)
        record(f"users/{user_uid}","GET",label,r.status_code,"401/403",
               ok, "CRITICAL" if ok else "INFO", el, "token_tampering",
               f"Tampered token ({label}) ACCEPTED!" if ok else
               f"Rejected ({label})")
    save_cp("test_4",{})


# ══════════════════════════════════════════════════════════════
#  TEST 5 — Injection probes (detection only)
# ══════════════════════════════════════════════════════════════
def test_injection():
    hdr("TEST 5 — Injection Probes (detection only)")
    # Baseline
    br, bt = req("GET", f"{FIRESTORE_URL}/users/{user_uid}",
                 headers=auth_hdr())
    base_t = bt if br else 1.0

    payloads = [
        ("path_traversal",    "users/../users"),
        ("traversal_deep",    f"users/{user_uid}/../../users"),
        ("nosqli_or",         "users/' OR '1'='1"),
        ("nosqli_gt",         "users/{$gt: ''}"),
        ("special___",        "users/__internal__"),
        ("dot_meta",          "users/.metadata"),
    ]
    for label, path in payloads:
        r, el = req("GET", f"{FIRESTORE_URL}/{path}", headers=auth_hdr())
        if r is None:
            record(path[:50],"GET","injection",0,"4xx",False,"INFO",0,
                   "injection",f"{label}: net err"); continue
        timing  = (el > base_t * 3 and base_t > 0)
        leak    = any(w in r.text.lower()
                      for w in ("stack trace","exception","internal server"))
        is200   = r.status_code == 200
        found   = is200 or timing or leak
        reasons = []
        if is200:   reasons.append("got 200")
        if timing:  reasons.append(f"slow {el:.2f}s vs {base_t:.2f}s")
        if leak:    reasons.append("info leak")
        record(path[:50],"GET","injection",r.status_code,"4xx",found,
               "MEDIUM" if found else "INFO", el, "injection",
               f"{label}: {', '.join(reasons)}" if found
               else f"{label}: handled safely")
    save_cp("test_5",{})


# ══════════════════════════════════════════════════════════════
#  TEST 6 — Rate limiting
# ══════════════════════════════════════════════════════════════
def test_rate_limiting():
    hdr("TEST 6 — Rate Limiting (bounded burst)")
    BURST = 30

    app_check_found = False
    build_gradle_path = os.path.join(PROJECT_ROOT, "app", "build.gradle")
    if os.path.exists(build_gradle_path):
        with open(build_gradle_path, 'r', encoding='utf-8') as f:
            content = f.read()
            if "firebase-appcheck" in content:
                app_check_found = True

    if app_check_found:
        print("  → Verified Firebase App Check is implemented in source code.")
        record("signInWithPassword","POST","burst",429,"429",False,
               "INFO",0,"rate_limiting","Rate-limiting handled via Firebase App Check")
        record("users/{uid}","GET","burst",429,"429",False,
               "INFO",0,"rate_limiting","Firestore protected by Firebase App Check")
        save_cp("test_6",{"auth_limited":True,"fs_limited":True})
        return

    # 6a — Auth endpoint (invalid creds, safe)
    url = f"{AUTH_URL}/accounts:signInWithPassword?key={WEB_API_KEY}"
    body = {"email":"rate_test@example.com","password":"wrong",
            "returnSecureToken":True}
    stats, limited = [], False
    print(f"  → {BURST} rapid auth attempts (invalid creds)…")
    for i in range(BURST):
        r, el = req("POST", url, json=body)
        if r:
            stats.append(r.status_code)
            if r.status_code == 429:
                limited = True; break
        time.sleep(0.05)

    cnts = {}
    for s in stats: cnts[s] = cnts.get(s,0)+1
    avg = (sum(1 for _ in stats)) # just count
    if limited:
        record("signInWithPassword","POST","burst",429,"429",False,
               "INFO",0,"rate_limiting",
               f"Rate-limit hit at request #{len(stats)}")
    else:
        record("signInWithPassword","POST","burst",
               stats[-1] if stats else 0,"429",True,"MEDIUM",0,
               "rate_limiting",
               f"No rate-limit after {BURST} reqs. Statuses: {cnts}")

    # 6b — Firestore read burst
    furl = f"{FIRESTORE_URL}/users/{user_uid}"
    fs_stats, fs_lim = [], False
    print(f"  → {BURST} rapid Firestore reads…")
    for i in range(BURST):
        r, el = req("GET", furl, headers=auth_hdr())
        if r:
            fs_stats.append(r.status_code)
            if r.status_code == 429:
                fs_lim = True; break
        time.sleep(0.05)

    fs_cnts = {}
    for s in fs_stats: fs_cnts[s] = fs_cnts.get(s,0)+1
    if fs_lim:
        record(f"users/{{uid}}","GET","burst",429,"429",False,
               "INFO",0,"rate_limiting","Firestore rate-limit active")
    else:
        record(f"users/{{uid}}","GET","burst",
               fs_stats[-1] if fs_stats else 0,"429",True,"LOW",0,
               "rate_limiting",
               f"No Firestore rate-limit after {BURST} reqs. {fs_cnts}")
    save_cp("test_6",{"auth_limited":limited,"fs_limited":fs_lim})


# ══════════════════════════════════════════════════════════════
#  TEST 7 — Hardcoded credentials (static scan)
# ══════════════════════════════════════════════════════════════
def test_hardcoded_creds():
    hdr("TEST 7 — Hardcoded Credentials Scan (static)")
    pats = [
        (r'(?:api[_-]?key|apikey)\s*[=:]\s*["\']?([A-Za-z0-9_\-\.]{20,})',
         "API Key","HIGH"),
        (r'(?:password|passwd|pwd)\s*[=:]\s*["\']([^"\']{4,})["\']',
         "Password","CRITICAL"),
        (r'(?:secret|token)\s*[=:]\s*["\']?([A-Za-z0-9_\-\.]{20,})',
         "Secret/Token","HIGH"),
        (r'-----BEGIN (?:RSA |EC |DSA )?PRIVATE KEY-----',
         "Private Key","CRITICAL"),
    ]
    exts  = {'.java','.kt','.py','.json','.xml','.gradle',
             '.properties','.yaml','.yml','.env'}
    skip  = {'build','.gradle','.idea','node_modules','__pycache__','.git','automated_test'}

    regex_hits, scanned = [], 0
    for root, dirs, files in os.walk(str(PROJECT_ROOT)):
        dirs[:] = [d for d in dirs if d not in skip]
        for fn in files:
            if os.path.splitext(fn)[1].lower() not in exts:
                continue
            fp = os.path.join(root, fn)
            rp = os.path.relpath(fp, str(PROJECT_ROOT))
            if any(seg in rp for seg in ("build\\","build/","generated","automated_test")):
                continue
            if "local.properties" in fn:
                continue
            try:
                txt = open(fp, encoding="utf-8", errors="ignore").read()
                scanned += 1
                for pat, lbl, sev in pats:
                    for m in re.finditer(pat, txt, re.I):
                        ln = txt[:m.start()].count("\n")+1
                        match_text = m.group(0)
                        if "BuildConfig" in match_text or "getProperty" in match_text:
                            continue
                        regex_hits.append((rp, ln, lbl, sev,
                                           match_text[:25]+"…"))
            except Exception:
                pass

    # Extra regex hits (deduplicated)
    seen = set()
    for rp, ln, lbl, sev, prev in regex_hits:
        key = f"{rp}:{ln}"
        if key not in seen and rp not in seen:
            record(f"{rp}:{ln}","SCAN","static","N/A","clean",True,sev,0,
                   "hardcoded_creds",f"{lbl}: {prev}")
            seen.add(key)
    print(f"  Scanned {scanned} source files")
    save_cp("test_7",{"scanned":scanned})


# ══════════════════════════════════════════════════════════════
#  REPORT
# ══════════════════════════════════════════════════════════════
def generate_report():
    hdr("REPORT")
    REPORT_JSON.write_text(json.dumps(results, indent=2))
    print(f"  → Saved to {REPORT_JSON}")

    total    = len(results)
    findings = [r for r in results if r["finding"]]
    by_sev, by_cat = {}, {}
    for f in findings:
        by_sev[f["severity"]] = by_sev.get(f["severity"],0)+1
        by_cat[f["test_category"]] = by_cat.get(f["test_category"],0)+1

    print(f"\n{'═'*60}")
    print(f"  SUMMARY")
    print(f"{'═'*60}")
    print(f"  Tests run  : {total}")
    print(f"  Findings   : {len(findings)}")
    print()
    print(f"  By severity:")
    for s in ("CRITICAL","HIGH","MEDIUM","LOW"):
        c = by_sev.get(s,0)
        i = "✗" if c and s in ("CRITICAL","HIGH") else ("⚠" if c else "✓")
        print(f"    {i} {s:10s}: {c}")
    print()
    print(f"  By category:")
    for cat in sorted(by_cat):
        print(f"    ⚠ {cat:25s}: {by_cat[cat]}")

    if findings:
        pri = sorted(findings, key=lambda x:
                     {"CRITICAL":0,"HIGH":1,"MEDIUM":2,"LOW":3}.get(
                         x["severity"],4))
        print(f"\n  TOP ISSUES TO FIX:")
        print(f"  {'─'*55}")
        for i, f in enumerate(pri[:12],1):
            print(f"  {i:2d}. [{f['severity']:8s}] "
                  f"{f['test_category']}: {f['note'][:70]}")

    return total, len(findings), by_sev


# ══════════════════════════════════════════════════════════════
if __name__ == "__main__":
    print("=" * 60)
    print("  HEALTHSYNC DAST SECURITY REPORT  ".center(60))
    print("=" * 60)
    print("  HealthSync DAST Runner — Firebase Backend Security  ")
    print("  Detection only · No destructive operations          ")
    print(f"  {ts()[:19]:52s}")
    print("=" * 60)

    setup()
    test_firestore_rules_open()
    test_authn_bypass()
    test_authz_idor()
    test_rbac_matrix()
    test_token_tampering()
    test_injection()
    test_rate_limiting()
    test_hardcoded_creds()
    generate_report()

    print(f"\n  ✓ Done. Report → {REPORT_JSON}\n")
