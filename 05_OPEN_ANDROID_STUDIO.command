#!/bin/bash
set -euo pipefail
cd "$(dirname "$0")"
if ! command -v open >/dev/null 2>&1; then
  echo "Perintah open tidak tersedia."
  exit 1
fi
open -a "Android Studio" "$(pwd)/android-app"
