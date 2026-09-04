#!/bin/bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
python3 "$ROOT/tools/verify_source.py"
read -r -p "Tekan ENTER untuk selesai..." _
