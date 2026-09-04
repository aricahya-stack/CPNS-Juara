#!/bin/bash
set -euo pipefail
cd "$(dirname "$0")/admin-web"

echo "========================================"
echo " CPNS JUARA v2.5 - INSTALL ADMIN WEB"
echo "========================================"

if ! command -v python3 >/dev/null 2>&1; then
  echo "ERROR: python3 belum terinstall."
  exit 1
fi

python3 --version

if [ -d ".venv" ]; then
  echo "Virtual environment lama di folder ini ditemukan; menghapus untuk fresh install..."
  rm -rf .venv
fi

python3 -m venv .venv
source .venv/bin/activate
python -m pip install --upgrade pip
pip install -r requirements.txt

if [ ! -f .env ]; then
  cp .env.example .env
fi

python manage.py migrate
python manage.py seed_demo
python manage.py reset_dev_admin
python manage.py check
python manage.py collectstatic --noinput
python manage.py verify_install

echo ""
echo "INSTALL SELESAI."
echo "Username: admin"
echo "Password: CpnsJuara2026!"
echo "Lanjutkan dengan menjalankan: 02_RUN_ADMIN_MAC.command"
read -r -p "Tekan ENTER untuk selesai..." _
