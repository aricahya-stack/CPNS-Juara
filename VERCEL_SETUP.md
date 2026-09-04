# Deploy Django CPNS JUARA ke Vercel

Deploy folder: `admin-web`

Environment minimal:
- `DJANGO_SECRET_KEY`
- `DJANGO_DEBUG=false`
- `DJANGO_ALLOWED_HOSTS=.vercel.app,domain-anda`
- `DATABASE_URL`
- `FIREBASE_SERVICE_ACCOUNT_JSON`
- `GOOGLE_PLAY_PACKAGE_NAME=com.cpnsjuara.app`
- `GOOGLE_PLAY_SERVICE_ACCOUNT_JSON`

Gunakan PostgreSQL production. Jangan bergantung pada SQLite sebagai database persisten di serverless.

Jalankan migration terhadap DB production:
```bash
python manage.py migrate
```

Buat superuser production sendiri dan jangan pakai password demo.
