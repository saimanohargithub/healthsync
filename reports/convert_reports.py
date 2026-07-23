import pandas as pd
import os

files = {
    'SecurityTest_Report.xlsx': 'security_report.html',
    'LoadTest_Report.xlsx': 'load_test_report.html',
    'Execution_Report.xlsx': 'web_e2e_report.html'
}

for xlsx_name, html_name in files.items():
    in_path = f'reports/{xlsx_name}'
    out_path = f'reports/{html_name}'
    if os.path.exists(in_path):
        df = pd.read_excel(in_path)
        html_table = df.to_html(index=False)
        html_content = f'''<!DOCTYPE html>
<html>
<head>
<title>{xlsx_name.replace(".xlsx", "")}</title>
<style>
body {{ font-family: -apple-system, sans-serif; padding: 20px; }}
table {{ border-collapse: collapse; width: 100%; }}
th, td {{ border: 1px solid #d0d7de; padding: 8px; text-align: left; }}
th {{ background-color: #f6f8fa; }}
</style>
</head>
<body>
<h2>{xlsx_name.replace(".xlsx", "")}</h2>
{html_table}
</body>
</html>'''
        with open(out_path, 'w', encoding='utf-8') as f:
            f.write(html_content)

# Android E2E
if not os.path.exists('reports/android_e2e_report.html') and os.path.exists('reports/Execution_Report.xlsx'):
    df = pd.read_excel('reports/Execution_Report.xlsx')
    html_table = df.to_html(index=False)
    html_content = f'''<!DOCTYPE html>
<html>
<head>
<title>Android E2E</title>
<style>
body {{ font-family: -apple-system, sans-serif; padding: 20px; }}
table {{ border-collapse: collapse; width: 100%; }}
th, td {{ border: 1px solid #d0d7de; padding: 8px; text-align: left; }}
th {{ background-color: #f6f8fa; }}
</style>
</head>
<body>
<h2>Android E2E Report</h2>
{html_table}
</body>
</html>'''
    with open('reports/android_e2e_report.html', 'w', encoding='utf-8') as f:
        f.write(html_content)
