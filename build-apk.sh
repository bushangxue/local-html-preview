#!/usr/bin/env bash
set -euo pipefail
if ! command -v gradle >/dev/null 2>&1; then
  echo '未找到 gradle。请用 Android Studio 打开本目录，或安装 Gradle 8.7+ 与 Android SDK 35。' >&2
  exit 2
fi
gradle :app:assembleDebug
mkdir -p dist
cp -f app/build/outputs/apk/apk/debug/app-debug.apk dist/local-html-preview-debug.apk
echo "APK: dist/local-html-preview-debug.apk"
