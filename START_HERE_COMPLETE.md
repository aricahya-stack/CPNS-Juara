# CPNS JUARA v2.5 — COMPLETE ARCHITECTURE

## Flow konten

KISI-KISI
→ SOAL
→ stimulus inline (opsional)
→ pilihan A–E / scoring
→ READY
→ PUBLISHED
→ PAKET FREE/PREMIUM
→ API
→ Android SQLite v5
→ latihan offline

## Stimulus inline

Field Admin/API/Android selaras:
- jenis (`NONE/TEXT/IMAGE/GRAPHIC/MIXED`)
- teks
- image URL
- local image path Android
- sumber

Admin upload file lokal dikirim API sebagai URL absolut. URL localhost pada data demo dinormalisasi mengikuti host request, sehingga request Android ke `10.0.2.2` menerima URL `10.0.2.2`, bukan `127.0.0.1`.

## Offline visual Android

Saat download paket:
1. Android menerima metadata soal.
2. Visual stimulus dicoba di-download maksimal 8 MB per file.
3. File disimpan di internal app storage `files/stimuli/`.
4. SQLite v5 menyimpan remote URL + local path.
5. Quiz/CAT memakai local file lebih dulu, remote URL sebagai fallback.

## Firebase optional first-run

Google Services plugin hanya diterapkan jika `app/google-services.json` ada. Karena itu Guest Mode dapat dibuild terlebih dahulu.

## Monetisasi

Source tetap memuat fondasi:
- Google Play Billing
- Premium monthly/yearly
- backend verification + acknowledgement
- AdMob test IDs
- Premium ad-free

Fitur tersebut membutuhkan akun/credential eksternal sebelum pengujian nyata.
