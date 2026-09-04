#!/bin/bash
set -euo pipefail
cd "$(dirname "$0")/admin-web"
source .venv/bin/activate
python manage.py reset_dev_admin
echo "Username: admin"
echo "Password: CpnsJuara2026!"
read -r -p "Tekan ENTER untuk selesai..." _
