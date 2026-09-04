# Google Play Billing — CPNS JUARA

## Produk
Play Console → Monetize with Play → Subscriptions:
- `cpns_juara_premium_monthly`
- `cpns_juara_premium_yearly`

Aktifkan Base Plan setiap produk.

## Test
Gunakan Internal/Closed Testing + License testers. Billing subscription nyata tidak dapat diuji penuh hanya dari APK sideload biasa.

## Backend verification
Android mengirim:
`POST /api/billing/verify/`

Header:
`Authorization: Bearer <Firebase ID token>`

Body:
```json
{"product_id":"cpns_juara_premium_monthly","purchase_token":"..."}
```

Backend:
1. Verifikasi Firebase user.
2. Panggil Google Play Developer API `purchases.subscriptionsv2.get`.
3. Cocokkan product ID.
4. Cocokkan obfuscated account ID jika tersedia.
5. Cegah token dipakai akun lain.
6. Acknowledge initial subscription jika masih pending acknowledgement.
7. Simpan snapshot + expiry.
8. Baru aktifkan Premium.

## Environment backend
Local:
```bash
export GOOGLE_PLAY_PACKAGE_NAME=com.cpnsjuara.app
export GOOGLE_PLAY_SERVICE_ACCOUNT_FILE="$HOME/Secrets/play-service-account.json"
```

Vercel:
- `GOOGLE_PLAY_PACKAGE_NAME=com.cpnsjuara.app`
- `GOOGLE_PLAY_SERVICE_ACCOUNT_JSON={...}`

Service account private key jangan dimasukkan APK atau Git.
