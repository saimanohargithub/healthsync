#!/usr/bin/env python3
"""TEST 7 — Hardcoded Credentials: static scan of the codebase for secrets."""

import sys, os, re
sys.path.insert(0, os.path.dirname(__file__))
from dast_config import *

def run(_token=None, _uid=None):
    print("\n" + "═"*60 + "\n  TEST 7 — Hardcoded Credentials Scan\n" + "═"*60)

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

    hits, scanned = [], 0
    for root, dirs, files in os.walk(str(PROJECT_ROOT)):
        dirs[:] = [d for d in dirs if d not in skip]
        for fn in files:
            if os.path.splitext(fn)[1].lower() not in exts: continue
            fp = os.path.join(root, fn)
            rp = os.path.relpath(fp, str(PROJECT_ROOT))
            if any(s in rp for s in ("build\\","build/","generated","automated_test")): continue
            try:
                txt = open(fp, encoding="utf-8", errors="ignore").read()
                scanned += 1
                for pat, lbl, sev in pats:
                    for m in re.finditer(pat, txt, re.I):
                        ln = txt[:m.start()].count("\n")+1
                        match_text = m.group(0)
                        # Skip if matching BuildConfig or properties which are safe
                        if "BuildConfig" in match_text or "getProperty" in match_text:
                            continue
                        hits.append((rp, ln, lbl, sev, match_text[:25]+"…"))
            except Exception:
                pass

    seen = set()
    for rp, ln, lbl, sev, prev in hits:
        key = f"{rp}:{ln}"
        if key not in seen and rp not in seen:
            record(f"{rp}:{ln}","SCAN","static","N/A","clean",True,sev,0,
                   "hardcoded_creds",f"{lbl}: {prev}")
            seen.add(key)

    findings = [r for r in results if r["finding"]]
    print(f"\n  Scanned {scanned} files, {len(findings)} credential issues found")
    return results

if __name__ == "__main__":
    run()
    save_results(SCRIPT_DIR / "results_hardcoded_creds.json")
