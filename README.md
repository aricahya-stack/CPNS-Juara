# CPNS JUARA v2.5 — UNIFIED ADMIN

Baseline ini meneruskan seluruh fitur v2.4.1 Rich Content Sync dan menambahkan **Unified Admin Workspace**.

## Web Admin

`/` dan `/content/` kini merupakan satu pengalaman admin dengan:
- sidebar yang sama
- topbar yang sama
- shortcut tambah soal
- Dashboard
- Content Studio
- Kisi-kisi
- Bank Soal WYSIWYG
- Paket Soal
- Import Excel + Riwayat
- Pengguna
- Premium
- Progres & Ranking
- Pengaturan
- Advanced Django Admin
- API Health

Dashboard sekarang staff-only dan memakai login Django Admin yang sama.

## Kontrak data tetap

Tidak ada perubahan pada kontrak rich content v2.4.1:
- stimulus tetap terstruktur
- pertanyaan tetap terpisah
- setiap opsi A–E hanya satu `content_html`
- HTML + LaTeX / MathML tetap sinkron dengan Android
- SQLite Android tetap baseline yang sama

Baca juga:
- `CHANGELOG-v2.5.md`
- `RICH-CONTENT-DATA-CONTRACT.md`
- `START-HERE-FIRST.md`
