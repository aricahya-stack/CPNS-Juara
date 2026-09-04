# CPNS JUARA v2.4 — Android Studio

## Prasyarat
- Android Studio
- Android SDK Platform 36
- JDK 17 atau JBR Android Studio yang kompatibel
- Internet untuk dependency Gradle pertama kali

AGP project: 8.13.2. Gradle yang dipin: 8.13.

## Cara paling mudah di Mac

Dari folder root project:

```bash
./06_PREPARE_ANDROID_MAC.command
./07_BUILD_ANDROID_GUEST_MAC.command
```

`06_PREPARE...` akan mengunduh Gradle 8.13 resmi bila dibutuhkan, membuat wrapper, dan menulis `local.properties` jika SDK ditemukan di `~/Library/Android/sdk`.

## Firebase tidak wajib untuk test pertama

Guest Mode dapat dibuild tanpa `app/google-services.json`.
Tombol Google Login akan nonaktif sampai Firebase dikonfigurasi.

Setelah Firebase siap, taruh file asli di:

`android-app/app/google-services.json`

lalu Sync/Build ulang.

## Jalankan dengan Django lokal

1. Jalankan Django: `02_RUN_ADMIN_MAC.command`.
2. Buka project `android-app` di Android Studio.
3. Buat emulator.
4. Run app dan pilih `Lanjut tanpa akun`.
5. Pengaturan Server: `http://10.0.2.2:8000`.
6. Download `Paket Demo CPNS JUARA v2.4`.
7. Coba TWK/TIU/TKP dan stimulus grafik.
8. Setelah download selesai, matikan internet emulator dan coba soal grafik lagi; Android menggunakan file stimulus yang tersimpan di internal storage jika download visual sebelumnya berhasil.

## SQLite v5

Soal offline menyimpan:
- kisi-kisi
- stimulus type
- stimulus text
- stimulus image remote URL
- stimulus image local path
- stimulus source
- prompt/explanation/scoring
- opsi A-E sebagai rich HTML tunggal (`content_html`)
- gambar di dalam rich HTML dicache untuk offline
- LaTeX diterima dari API sebagai HTML + MathML dan dirender via WebView

## Build APK

```bash
./gradlew :app:assembleDebug
```

Output:
`app/build/outputs/apk/debug/app-debug.apk`
