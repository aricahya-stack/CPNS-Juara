# START HERE — CPNS JUARA v2.5 UNIFIED ADMIN

Versi ini menyatukan Dashboard dan Content Studio ke dalam satu Admin Workspace.
Android dan kontrak API rich content tetap sama seperti baseline v2.4.1.

## 1. Install Admin Web di MacBook

```bash
cd ~/Downloads/CPNS-JUARA-v2.5-UNIFIED-ADMIN
chmod +x *.command
./01_INSTALL_ADMIN_MAC.command
```

Login development:
- Username: `admin`
- Password: `CpnsJuara2026!`

## 2. Verifikasi

```bash
./03_VERIFY_ADMIN_MAC.command
```

Target akhir: semua test `OK`.

## 3. Jalankan

```bash
./02_RUN_ADMIN_MAC.command
```

Buka:
- Admin Workspace: http://127.0.0.1:8000/
- Content Studio: http://127.0.0.1:8000/content/
- Import Excel: http://127.0.0.1:8000/content/import/
- Advanced Admin: http://127.0.0.1:8000/admin/

Dashboard `/` dan Content Studio `/content/` sekarang memakai sidebar dan topbar yang sama.
