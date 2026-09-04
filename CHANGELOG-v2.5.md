# CPNS JUARA v2.5 — Unified Admin Workspace

## Perubahan utama

- Dashboard `/` dan Content Studio `/content/` sekarang memakai satu layout Admin Web yang sama.
- Satu sidebar persistenn untuk Dashboard, Content Studio, Kisi-kisi, Bank Soal, Paket Soal, Import Excel, Pengguna, Premium, Progres, Pengaturan, Advanced Admin, dan API Health.
- Satu topbar dengan breadcrumb, shortcut tambah soal, identitas admin, environment badge, dan logout.
- Dashboard menjadi area staff-only dan memakai login Django Admin yang sama.
- Content Studio tetap memiliki URL lama sehingga bookmark, import flow, dan test tidak rusak.
- Django Admin tetap tersedia sebagai Advanced Admin untuk operasi model teknis.
- Android dan kontrak API v2.4.1 tidak diubah.

## Prinsip navigasi

- `/` = ringkasan seluruh sistem.
- `/content/` = ringkasan pekerjaan konten.
- `/admin/core/...` = editor/model spesifik.
- `/admin/` = Advanced Admin.
