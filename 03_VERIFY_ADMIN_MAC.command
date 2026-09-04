#!/bin/bash
set -euo pipefail
cd "$(dirname "$0")/admin-web"
source .venv/bin/activate
python manage.py check
python manage.py collectstatic --noinput >/dev/null
python manage.py verify_install
python manage.py test core.tests --verbosity 2
read -r -p "Tekan ENTER untuk selesai..." _
