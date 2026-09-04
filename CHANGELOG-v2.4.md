# CPNS JUARA v2.4 — Rich Content Sync

## Kontrak data final
- Stimulus tetap terstruktur dan terpisah dari pertanyaan.
- Setiap opsi A-E hanya mempunyai satu field `content_html`.
- Opsi dapat berisi teks, gambar, LaTeX, tabel, atau kombinasi semuanya tanpa field gambar terpisah.
- `stimulus_text`, `prompt`, `explanation`, dan `Option.content_html` disanitasi sebelum disimpan.

## Admin Web
- Edit/Tambah Soal tetap tersedia melalui Django Admin.
- WYSIWYG Jodit 4.13.23 untuk stimulus teks, pertanyaan, pembahasan, dan opsi.
- Toolbar mencakup formatting, alignment, list, link, image, table, superscript/subscript, source HTML `<>`, fullscreen.
- Tombol upload gambar langsung ke Django media.
- Tombol LaTeX inline dan block serta live preview dengan KaTeX 0.18.4.

## API
- `content_format: html+latex-v1`.
- `render_format: html+mathml-v1`.
- API mengirim raw rich HTML dan `rendered_html` dengan MathML.
- URL gambar di rich content dinormalisasi ke host API yang dipakai client.

## Android
- SQLite naik ke v5.
- Opsi memakai `contentHtml` tunggal.
- Question, explanation, stimulus teks, dan opsi dirender dengan WebView rich-content.
- Gambar dalam rich HTML dicache ketika paket diunduh dan ditulis ulang menjadi file lokal untuk offline.
- Tampilan soal tetap tanpa label literal “Stimulus” dan “Pertanyaan”.

## Excel
- Sheet tetap `KISI-KISI` + `SOAL`.
- Kolom opsi tetap hanya `opsi_a` sampai `opsi_e`, masing-masing satu rich-content column.
- Tidak ada `opsi_a_image`, `opsi_b_image`, dan seterusnya.
