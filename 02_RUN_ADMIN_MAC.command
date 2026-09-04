#!/bin/bash
set -euo pipefail
cd "$(dirname "$0")/admin-web"

if [ ! -f ".venv/bin/activate" ]; then
  echo "Belum diinstall. Jalankan 01_INSTALL_ADMIN_MAC.command terlebih dahulu."
  read -r -p "Tekan ENTER..." _
  exit 1
fi

source .venv/bin/activate
python manage.py check
python manage.py collectstatic --noinput >/dev/null
python manage.py verify_install

echo ""
echo "Server CPNS JUARA berjalan di:"
echo "Dashboard      http://127.0.0.1:8000/"
echo "Admin          http://127.0.0.1:8000/admin/"
echo "Content Studio http://127.0.0.1:8000/content/"
echo "Import Excel   http://127.0.0.1:8000/content/import/"
echo ""
echo "Login: admin / CpnsJuara2026!"
echo "Stop server dengan Control+C"
echo ""
python manage.py runserver 0.0.0.0:8000
