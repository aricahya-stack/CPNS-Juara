#!/bin/bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
ANDROID="$ROOT/android-app"

if [ -x "/Applications/Android Studio.app/Contents/jbr/Contents/Home/bin/java" ]; then
  export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
  export PATH="$JAVA_HOME/bin:$PATH"
fi

cd "$ANDROID"
if [ ! -x "./gradlew" ]; then
  echo "Gradle wrapper belum siap. Jalankan 06_PREPARE_ANDROID_MAC.command terlebih dahulu."
  exit 1
fi

if [ ! -f "local.properties" ] && [ -d "$HOME/Library/Android/sdk" ]; then
  printf 'sdk.dir=%s\n' "$HOME/Library/Android/sdk" > local.properties
fi

echo "========================================"
echo " CPNS JUARA v2.5 - BUILD GUEST DEBUG"
echo "========================================"
echo "google-services.json bersifat opsional untuk build Guest Mode pertama."
echo ""

./gradlew :app:assembleDebug

APK="$ANDROID/app/build/outputs/apk/debug/app-debug.apk"
if [ -f "$APK" ]; then
  echo ""
  echo "[OK] APK debug berhasil dibuat:"
  echo "$APK"
  open "$(dirname "$APK")" || true
else
  echo "Build selesai tetapi APK tidak ditemukan di lokasi standar."
  exit 1
fi

read -r -p "Tekan ENTER untuk selesai..." _
