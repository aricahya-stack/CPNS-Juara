# CPNS JUARA v2.4 — Rich Content Data Contract

## Question
Stimulus tetap terpisah dari pertanyaan. Field utamanya:
- `stimulus_kind`
- `stimulus_text` — WYSIWYG + LaTeX
- `stimulus_image` / `stimulus_image_url` — visual stimulus terstruktur
- `stimulus_source`
- `prompt` — WYSIWYG + LaTeX
- `explanation` — WYSIWYG + LaTeX

## Option A-E
Masing-masing opsi hanya memiliki satu field:
- `content_html` — dapat berisi teks, gambar, LaTeX, atau campuran semuanya
- `score`
- `is_correct`

Tidak ada field `option_text` dan `option_image` yang terpisah.

## LaTeX
- Inline: `\(x^2 + y^2\)`
- Block: `\[\frac{a}{b}\]`

Admin Web memberi preview dengan KaTeX. API membuat MathML untuk Android, sehingga rumus dapat ditampilkan tanpa koneksi saat paket sudah diunduh.

## API
- `content_format = html+latex-v1`
- `render_format = html+mathml-v1`
- Question: raw + `*_rendered_html`
- Option: `content_html` + `rendered_html`

## Android
SQLite v5 menyimpan rich HTML. Semua `<img>` pada question/option rich HTML dicache ketika paket diunduh dan URL-nya ditulis ulang ke file lokal.
