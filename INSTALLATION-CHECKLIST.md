# CPNS JUARA v2.5 — Installation Checklist (MacBook)

## Admin Web
- [ ] Extract `CPNS-JUARA-v2.5-UNIFIED-ADMIN.zip`
- [ ] `cd ~/Downloads/CPNS-JUARA-v2.5-UNIFIED-ADMIN`
- [ ] `chmod +x *.command`
- [ ] `./01_INSTALL_ADMIN_MAC.command`
- [ ] `./03_VERIFY_ADMIN_MAC.command` berakhir `OK`
- [ ] `./02_RUN_ADMIN_MAC.command`
- [ ] Login `admin / CpnsJuara2026!`
- [ ] Dashboard `/` terbuka
- [ ] Content Studio `/content/` terbuka dengan sidebar yang sama
- [ ] Import Excel `/content/import/` terbuka
- [ ] Edit Soal WYSIWYG berjalan

## Android — setelah Web selesai diuji
- [ ] `./06_PREPARE_ANDROID_MAC.command`
- [ ] Buka `android-app` di Android Studio
- [ ] Guest Mode dapat dijalankan
- [ ] Set Server URL emulator ke `http://10.0.2.2:8000`
- [ ] Download paket soal
- [ ] Uji stimulus, opsi rich content, LaTeX, gambar, dan offline
