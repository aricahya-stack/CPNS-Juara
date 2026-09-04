# CPNS JUARA v2.4 — Kisi-kisi & Import Excel

## Alur
Kisi-kisi → Entri Manual / Import Excel → Preview & Validasi → READY → PUBLISHED → Paket → API → Android.

## Stimulus
Stimulus tetap menjadi bagian terstruktur dari soal dan terpisah dari field pertanyaan. Ia dapat berupa teks/cerita/perintah, gambar/grafik, atau campuran. Di layar Android tidak ada label “Stimulus” atau “Pertanyaan”; keduanya mengalir sebagai satu tampilan soal.

## Opsi jawaban
Setiap A-E hanya satu kolom (`opsi_a` sampai `opsi_e`). Satu kolom tersebut boleh berisi:
- teks
- HTML rich content
- gambar melalui `<img src="...">`
- LaTeX dengan `\(...\)` / `\[...\]`
- kombinasi semuanya

Tidak ada kolom gambar opsi yang terpisah.

## Workbook
Template memiliki dua sheet: `KISI-KISI` dan `SOAL`.

Kolom SOAL:
`kode_soal, kode_kisi, stimulus_jenis, stimulus_teks, stimulus_image_url, stimulus_sumber, pertanyaan, opsi_a, opsi_b, opsi_c, opsi_d, opsi_e, kunci, skor_a, skor_b, skor_c, skor_d, skor_e, pembahasan, kesulitan`.

## WYSIWYG Admin
Pada Edit/Tambah Soal, `stimulus_text`, `pertanyaan`, `pembahasan`, dan isi opsi A-E memakai rich editor. Editor menyediakan format teks, alignment, list, table, link, upload gambar, superscript/subscript, source HTML `<>`, fullscreen, dan tombol LaTeX.

## Import gambar pada opsi dari Excel
Untuk Excel, sisipkan gambar sebagai URL di HTML, contoh:
`<p style="text-align:center"><img src="https://domain/gambar-a.png"></p>`

Setelah paket didownload Android, gambar rich-content akan dicache untuk penggunaan offline.
