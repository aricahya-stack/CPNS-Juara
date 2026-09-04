from pathlib import Path
import ast
import re
import sys
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
errors = []
oks = []


def check(condition, ok, fail):
    if condition:
        oks.append(ok)
    else:
        errors.append(fail)

# Python syntax without requiring Django to be installed.
for path in (ROOT / "admin-web").rglob("*.py"):
    try:
        ast.parse(path.read_text(encoding="utf-8"), filename=str(path))
    except SyntaxError as exc:
        errors.append(f"Python syntax: {path.relative_to(ROOT)} -> {exc}")
check(not any(x.startswith("Python syntax") for x in errors),
      "Python source syntax", "Python source syntax gagal")

# Android XML validity.
xml_errors_before = len(errors)
for path in (ROOT / "android-app" / "app" / "src" / "main" / "res").rglob("*.xml"):
    try:
        ET.parse(path)
    except Exception as exc:
        errors.append(f"Android XML: {path.relative_to(ROOT)} -> {exc}")
try:
    ET.parse(ROOT / "android-app" / "app" / "src" / "main" / "AndroidManifest.xml")
except Exception as exc:
    errors.append(f"AndroidManifest.xml -> {exc}")
check(len(errors) == xml_errors_before, "Android XML", "Android XML gagal")

models = (ROOT / "android-app" / "app" / "src" / "main" / "java" / "com" / "cpnsjuara" / "app" / "Models.kt").read_text()
db = (ROOT / "android-app" / "app" / "src" / "main" / "java" / "com" / "cpnsjuara" / "app" / "DatabaseHelper.kt").read_text()
api = (ROOT / "android-app" / "app" / "src" / "main" / "java" / "com" / "cpnsjuara" / "app" / "ApiClient.kt").read_text()
quiz = (ROOT / "android-app" / "app" / "src" / "main" / "java" / "com" / "cpnsjuara" / "app" / "QuizActivity.kt").read_text()
cat = (ROOT / "android-app" / "app" / "src" / "main" / "java" / "com" / "cpnsjuara" / "app" / "CatActivity.kt").read_text()
gradle = (ROOT / "android-app" / "app" / "build.gradle.kts").read_text()
settings = (ROOT / "admin-web" / "config" / "settings.py").read_text()
views = (ROOT / "admin-web" / "core" / "views.py").read_text()

check('SQLiteOpenHelper(context, "cpnsjuara.db", null, 5)' in db,
      "SQLite Android v5", "Database Android belum v5")
for field in ["stimulusType", "stimulusText", "stimulusImageUrl", "stimulusImageLocalPath", "stimulusSource"]:
    check(field in models, f"Model Android: {field}", f"Field Android hilang: {field}")
check('downloadStimulusForOffline' in db and 'cacheRichImagesForOffline' in db,
      "Download stimulus + rich option images offline", "Downloader rich media offline belum lengkap")
check('stimulus?.optString("image_url")' in api,
      "API client membaca image_url", "API client belum membaca image_url")
check('StimulusRenderer.render' in quiz and 'StimulusRenderer.render' in cat and 'RichOptionSelector' in quiz and 'RichOptionSelector' in cat,
      "Quiz + CAT merender stimulus + rich options", "Renderer rich content belum lengkap di Quiz/CAT")
check('if (file("google-services.json").exists())' in gradle,
      "Firebase opsional untuk Guest build", "Firebase masih wajib saat first build")
check('10.0.2.2' in settings,
      "Django mengizinkan emulator 10.0.2.2", "10.0.2.2 belum ada di ALLOWED_HOSTS")
check('_absolute_stimulus_image_url' in views,
      "API menormalkan URL stimulus", "Normalisasi URL stimulus belum ada")
check('content_format' in views and 'rendered_html' in views and 'content_html' in views,
      "API rich HTML + MathML contract", "Kontrak rich content API belum lengkap")
check('contentHtml' in models and 'contentHtml' in api,
      "Android rich option content", "Model/API Android belum memakai rich option")
check((ROOT / 'admin-web' / 'static' / 'rich-editor.js').exists(),
      "WYSIWYG editor assets", "WYSIWYG editor assets hilang")
admin_py = (ROOT / 'admin-web' / 'core' / 'admin.py').read_text()
check('jodit@4.13.23' in admin_py and 'katex@0.18.4' in admin_py,
      "Jodit 4.13.23 + KaTeX 0.18.4 pinned", "Versi editor/LaTeX Web tidak sesuai")
models_py = (ROOT / 'admin-web' / 'core' / 'models.py').read_text()
check('content_html = models.TextField' in models_py,
      "Option satu field content_html", "Option belum memakai content_html tunggal")
check((ROOT / "admin-web" / "static" / "sample_stimulus_chart.png").exists(),
      "Aset grafik demo", "Aset grafik demo hilang")
check((ROOT / "admin-web" / "static" / "templates" / "CPNS-JUARA-Template-Import-Soal.xlsx").exists(),
      "Template Excel", "Template Excel hilang")

# No sensitive/runtime files should be shipped.
for forbidden in [
    ROOT / "admin-web" / ".env",
    ROOT / "admin-web" / "db.sqlite3",
    ROOT / "android-app" / "app" / "google-services.json",
]:
    check(not forbidden.exists(), f"Tidak membawa {forbidden.name}", f"File sensitif/runtime ikut: {forbidden}")

print("CPNS JUARA v2.4 - SOURCE VERIFICATION")
print("=" * 46)
for item in oks:
    print(f"[OK] {item}")
if errors:
    print("\nERROR:")
    for item in errors:
        print(f"[FAIL] {item}")
    sys.exit(1)
print("\nSEMUA CHECKPOINT SOURCE v2.4 LULUS.")

# v2.5 unified admin source checks
unified_shell = root / "admin-web" / "templates" / "admin_shell.html"
if unified_shell.exists():
    text = unified_shell.read_text(encoding="utf-8")
    assert "ADMIN WORKSPACE" in text
    assert "Content Studio" in text
    assert "Bank Soal" in text
