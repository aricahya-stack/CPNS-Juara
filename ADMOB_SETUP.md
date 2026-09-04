# AdMob — CPNS JUARA

Source menggunakan official TEST ad IDs untuk development.

Fitur:
- Interstitial setelah jeda natural (contoh setelah 5 soal)
- Rewarded hint
- Premium tidak menampilkan iklan

Sebelum release:
1. Buat app CPNS JUARA di AdMob.
2. Buat Banner / Interstitial / Rewarded ad units sesuai kebutuhan.
3. Ganti `admob_app_id`, `admob_banner_id`, `admob_interstitial_id`, `admob_rewarded_id` di `android-app/app/src/main/res/values/strings.xml`.
4. Jangan gunakan test IDs di production dan jangan klik iklan sendiri.
5. Atur Blocking Controls dan consent/privacy sesuai wilayah pengguna.
