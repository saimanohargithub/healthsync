#!/usr/bin/env python3
"""Shared DAST configuration — loads input.json, provides helpers."""

import json, os, sys, time, base64
from datetime import datetime, timezone
from pathlib import Path

try:
    import requests
except ImportError:
    print("ERROR: pip install requests"); sys.exit(1)

SCRIPT_DIR   = Path(__file__).resolve().parent
INPUT_JSON   = SCRIPT_DIR.parent / "input.json"
REPORT_JSON  = SCRIPT_DIR / "report.json"
PROJECT_ROOT = SCRIPT_DIR.parents[7]

PROJECT_ID   = "healthsync-2af5a"
WEB_API_KEY  = "AIzaSyBucCI2Mhg9QNYmIbOKK2lMRPNVwTmcDh8"
AUTH_URL      = "https://identitytoolkit.googleapis.com/v1"
FIRESTORE_URL = (f"https://firestore.googleapis.com/v1/"
                 f"projects/{PROJECT_ID}/databases/(default)/documents")
DELAY = 0.35
MAX_TIME = 10

results = []
session = requests.Session()
session.headers.update({"Content-Type": "application/json"})

def mask(t):
    return t[:8]+"…"+t[-4:] if t and len(t)>20 else "***"

def ts():
    return datetime.now(timezone.utc).isoformat()

def record(endpoint, method, role, status, expected, finding,
           severity, resp_time, category, note):
    r = {"endpoint":endpoint,"method":method,"role":role,
         "status":status,"expected_status":expected,"finding":finding,
         "severity":severity,"response_time_ms":round(resp_time*1000,1),
         "test_category":category,"note":note,"timestamp":ts()}
    results.append(r)
    ico = "✗" if finding and severity in ("CRITICAL","HIGH") else ("⚠" if finding else "✓")
    print(f"  {ico} [{severity:8s}] {method:6s} {endpoint[:60]:60s} → {status} (exp {expected})")
    if finding and note: print(f"           └─ {note}")
    return r

def req(method, url, **kw):
    time.sleep(DELAY)
    try:
        r = session.request(method, url, timeout=MAX_TIME, **kw)
        return r, r.elapsed.total_seconds()
    except Exception:
        return None, 0

def authenticate():
    """Sign in with Firebase Auth, return (id_token, uid)."""
    creds = json.loads(INPUT_JSON.read_text())
    r, _ = req("POST",
               f"{AUTH_URL}/accounts:signInWithPassword?key={WEB_API_KEY}",
               json={"email":creds["email"],"password":creds["password"],
                     "returnSecureToken":True})
    if not r or r.status_code != 200:
        sys.exit(f"Auth failed: {r.status_code if r else 'network'}")
    d = r.json()
    print(f"  ✓ Authenticated as UID {d['localId']}")
    return d["idToken"], d["localId"]

def auth_hdr(token):
    return {"Authorization": f"Bearer {token}"}

def save_results(extra_file=None):
    REPORT_JSON.write_text(json.dumps(results, indent=2))
    if extra_file:
        Path(extra_file).write_text(json.dumps(results, indent=2))

def b64dec(s):
    return base64.urlsafe_b64decode(s + "=" * (4 - len(s) % 4))

def b64enc(b):
    return base64.urlsafe_b64encode(b).rstrip(b"=").decode()
