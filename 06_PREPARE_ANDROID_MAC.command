#!/bin/bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
ANDROID="$ROOT/android-app"
TOOLS="$ROOT/.tools"
GRADLE_VERSION="8.13"
GRADLE_HOME="$TOOLS/gradle-$GRADLE_VERSION"
ZIP="$TOOLS/gradle-$GRADLE_VERSION-bin.zip"
SDK_DEFAULT="$HOME/Library/Android/sdk"

echo "========================================"
echo " CPNS JUARA v2.5 - PREPARE ANDROID MAC"
echo "========================================"

mkdir -p "$TOOLS"

# Prefer Android Studio's bundled JDK when available.
if [ -x "/Applications/Android Studio.app/Contents/jbr/Contents/Home/bin/java" ]; then
  export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
  export PATH="$JAVA_HOME/bin:$PATH"
fi

if ! command -v java >/dev/null 2>&1; then
  echo "ERROR: Java tidak ditemukan. Install/buka Android Studio terlebih dahulu."
  exit 1
fi

echo "Java:"
java -version 2>&1 | head -3

if [ ! -d "$GRADLE_HOME" ]; then
  echo "Gradle $GRADLE_VERSION belum tersedia di folder project."
  echo "Mengunduh distribusi resmi Gradle $GRADLE_VERSION..."
  if [ ! -f "$ZIP" ]; then
    curl -L --fail --retry 3 \
      "https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip" \
      -o "$ZIP"
  fi
  rm -rf "$GRADLE_HOME"
  unzip -q "$ZIP" -d "$TOOLS"
fi

if [ ! -x "$ANDROID/gradlew" ] || [ ! -f "$ANDROID/gradle/wrapper/gradle-wrapper.jar" ]; then
  echo "Membuat Gradle Wrapper 8.13..."
  "$GRADLE_HOME/bin/gradle" -p "$ANDROID" wrapper --gradle-version "$GRADLE_VERSION"
fi
chmod +x "$ANDROID/gradlew"

if [ -d "$SDK_DEFAULT" ]; then
  printf 'sdk.dir=%s\n' "$SDK_DEFAULT" > "$ANDROID/local.properties"
  echo "Android SDK ditemukan: $SDK_DEFAULT"
else
  echo "PERINGATAN: Android SDK belum ditemukan di $SDK_DEFAULT"
  echo "Buka Android Studio -> SDK Manager dan install Android SDK terlebih dahulu."
fi

cd "$ANDROID"
echo ""
echo "Gradle Wrapper siap:"
./gradlew --version

echo ""
echo "SELESAI. Selanjutnya jalankan 07_BUILD_ANDROID_GUEST_MAC.command"
read -r -p "Tekan ENTER untuk selesai..." _
