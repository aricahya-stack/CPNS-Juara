# CPNS JUARA v2.4.1

Patch stabilitas untuk v2.4.

- Menambahkan `tinycss2` secara eksplisit ke `requirements.txt` karena dibutuhkan `bleach.css_sanitizer.CSSSanitizer`.
- Menambahkan `content_format` dan `render_format` pada setiap objek question API, selain tetap tersedia pada level paket.
- Memperkuat smoke test API agar memeriksa kontrak format pada level paket dan soal.
- Tidak mengubah kontrak database rich option, stimulus, atau Android SQLite v5.
