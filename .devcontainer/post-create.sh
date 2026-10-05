#!/usr/bin/env bash
# Runs once when the codespace/container is created.
# Installs the Android SDK, writes local.properties, and creates .env from .env.example.
set -euo pipefail

SDK="${ANDROID_HOME:-$HOME/android-sdk}"
CMDLINE_TOOLS_ZIP="commandlinetools-linux-11076708_latest.zip"

echo "==> Android SDK at $SDK"
if [ ! -x "$SDK/cmdline-tools/latest/bin/sdkmanager" ]; then
  sudo apt-get update -qq
  sudo apt-get install -y -qq unzip > /dev/null
  mkdir -p "$SDK/cmdline-tools"
  tmp="$(mktemp -d)"
  curl -fsSL "https://dl.google.com/android/repository/$CMDLINE_TOOLS_ZIP" -o "$tmp/tools.zip"
  unzip -q "$tmp/tools.zip" -d "$tmp"
  mv "$tmp/cmdline-tools" "$SDK/cmdline-tools/latest"
  rm -rf "$tmp"
fi
# `yes` exits with SIGPIPE once sdkmanager stops reading, so ignore its status
yes | "$SDK/cmdline-tools/latest/bin/sdkmanager" --licenses > /dev/null || true
# build-tools 34 is what AGP 8.7 uses by default; 35 matches compileSdk
"$SDK/cmdline-tools/latest/bin/sdkmanager" \
  "platforms;android-35" "build-tools;34.0.0" "build-tools;35.0.0" "platform-tools" > /dev/null

echo "sdk.dir=$SDK" > Finance_tracker_Frontend/Finance_Tracker/local.properties

echo "==> .env"
if [ ! -f .env ]; then
  cp .env.example .env
  secret="$(openssl rand -base64 32)"
  sed -i "s|^JWT_SECRET=.*|JWT_SECRET=${secret}|" .env
  echo "Created .env with a generated JWT_SECRET"
fi

echo "==> Done. Java: $(java -version 2>&1 | head -1)"
