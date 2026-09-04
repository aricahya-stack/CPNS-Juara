# Firebase Google Login — CPNS JUARA v2.4

> Guest Mode dapat dibuild dan diuji tanpa Firebase. Ikuti dokumen ini setelah Admin/API + Guest Mode lokal sudah berhasil.

Versi source ini sudah memiliki:

- Login dengan Google
- Guest Mode / lanjut tanpa akun
- Firebase Authentication
- Credential Manager
- Profil akun
- Logout
- Sinkronisasi profil Google → Django API
- Backend memverifikasi Firebase ID token
- Status Premium tersedia di model backend
- Latihan offline tetap dapat digunakan oleh Guest

## 1. Buat Firebase Project

Buka Firebase Console dan buat project, misalnya:

`cpns-juara`

Google Analytics boleh diaktifkan atau tidak; tidak dibutuhkan hanya untuk login.

## 2. Tambah Android App

Di Firebase:

Project settings → Your apps → Add app → Android

Isi:

Package name:
`com.cpnsjuara.app`

Nama app:
`CPNS JUARA`

## 3. Tambahkan SHA-1

Setelah Gradle wrapper tersedia, dari folder `android-app`:

```bash
./gradlew signingReport
```

Ambil SHA1 dari variant `debug`.

Alternatif Mac jika debug keystore sudah tersedia:

```bash
keytool -list -v \
  -alias androiddebugkey \
  -keystore ~/.android/debug.keystore \
  -storepass android \
  -keypass android
```

Copy SHA-1 → Firebase Project Settings → Android app → SHA certificate fingerprints.

## 4. Aktifkan Google Login

Firebase Console:

Authentication → Sign-in method → Google → Enable → Save

## 5. Download google-services.json

Download ulang `google-services.json` SETELAH Google provider dan SHA-1 sudah dikonfigurasi.

Taruh di:

```text
android-app/
└── app/
    ├── build.gradle.kts
    ├── google-services.json   ← DI SINI
    └── src/
```

Jangan masukkan file ke `src/main`.

## 6. Sync Android Studio

Android Studio:

File / menu Gradle → Sync Project with Gradle Files

Versi yang digunakan source ini:

- Google services Gradle plugin 4.5.0
- Firebase BoM 34.18.0
- firebase-auth
- Credential Manager 1.3.0
- googleid 1.1.1

## 7. Coba Login

Run aplikasi.

Halaman awal menyediakan:

`Masuk dengan Google`

atau

`Lanjut tanpa akun`

Guest:
- latihan offline
- download soal
- statistik lokal

Google user:
- semua fitur Guest
- siap cloud sync
- siap ranking
- siap Premium
- profil tersimpan di Django setelah backend Firebase Admin dikonfigurasi

## 8. Firebase Admin untuk Django (Local)

Buat service account:

Firebase Console / Google Cloud → Service Accounts → Generate new private key.

Simpan di luar Git/project, misalnya:

`~/Secrets/cpns-juara-firebase-admin.json`

Kemudian:

```bash
export GOOGLE_APPLICATION_CREDENTIALS="$HOME/Secrets/cpns-juara-firebase-admin.json"
python manage.py runserver
```

Jangan upload private key ke Git.

## 9. Firebase Admin di Vercel

Di Vercel Environment Variables buat:

`FIREBASE_SERVICE_ACCOUNT_JSON`

Value = keseluruhan JSON service account dalam satu nilai environment variable.

Jangan menaruh service account JSON di source repository.

## 10. Endpoint Backend

Android akan mengirim Firebase ID token ke:

```text
POST /api/auth/firebase/
Authorization: Bearer <firebase-id-token>
```

Django memverifikasi token menggunakan Firebase Admin lalu membuat/update `AppUser`.

Field:
- firebase_uid
- email
- display_name
- photo_url
- is_premium
- created_at
- last_seen_at

## 11. Perilaku Offline

Google login memerlukan koneksi ketika autentikasi perlu dilakukan.

Namun setelah user sudah login dan paket soal sudah berada di SQLite:

- Latihan tetap dapat berjalan offline.
- CAT lokal tetap dapat berjalan offline.
- Statistik lokal tetap dapat berjalan offline.
- Sync backend dapat menunggu internet kembali.

## Keamanan

- Jangan simpan password Google.
- Jangan simpan Firebase service-account private key di APK.
- `google-services.json` bukan service-account key, tetapi tetap gunakan file dari project milik sendiri.
- Backend selalu memverifikasi Firebase ID token sebelum mempercayai identitas user.