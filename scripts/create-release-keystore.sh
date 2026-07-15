#!/usr/bin/env bash
set -euo pipefail

# Generate a local Android release keystore and print the GitHub Actions secrets
# required by .github/workflows/build-apk.yml.
#
# Usage:
#   bash scripts/create-release-keystore.sh
#   bash scripts/create-release-keystore.sh local/my-release.jks
#
# Optional env overrides:
#   ANDROID_KEY_ALIAS=dramaku
#   ANDROID_KEYSTORE_PASSWORD=your-password
#   ANDROID_KEY_PASSWORD=your-password

KEYSTORE_PATH="${1:-local/dramaku-release.jks}"
KEY_ALIAS="${ANDROID_KEY_ALIAS:-dramaku}"
STORE_PASS="${ANDROID_KEYSTORE_PASSWORD:-}"
KEY_PASS="${ANDROID_KEY_PASSWORD:-}"

if ! command -v keytool >/dev/null 2>&1; then
  echo "ERROR: keytool not found. Install JDK 17 first." >&2
  exit 1
fi

rand_secret() {
  if command -v openssl >/dev/null 2>&1; then
    openssl rand -base64 36 | tr -d '\n'
  else
    python3 - <<'PY'
import secrets, string
alphabet = string.ascii_letters + string.digits + '_-@#%+=' 
print(''.join(secrets.choice(alphabet) for _ in range(48)), end='')
PY
  fi
}

base64_one_line() {
  if base64 --help 2>&1 | grep -q -- '-w'; then
    base64 -w 0 "$1"
  else
    base64 "$1" | tr -d '\n'
  fi
}

if [ -z "$STORE_PASS" ]; then STORE_PASS="$(rand_secret)"; fi

# PKCS12 keystores should use the same store and key password for Android/Gradle signing.
# If they differ, Gradle may fail with: "Given final block not properly padded".
if [ -n "$KEY_PASS" ] && [ "$KEY_PASS" != "$STORE_PASS" ]; then
  echo "WARNING: PKCS12 uses the store password as key password. ANDROID_KEY_PASSWORD will be set equal to ANDROID_KEYSTORE_PASSWORD." >&2
fi
KEY_PASS="$STORE_PASS"

mkdir -p "$(dirname "$KEYSTORE_PATH")"

if [ -f "$KEYSTORE_PATH" ]; then
  echo "ERROR: Keystore already exists: $KEYSTORE_PATH" >&2
  echo "Refusing to overwrite. Back it up or choose another path." >&2
  exit 1
fi

keytool -genkeypair \
  -v \
  -keystore "$KEYSTORE_PATH" \
  -storetype PKCS12 \
  -alias "$KEY_ALIAS" \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -storepass "$STORE_PASS" \
  -keypass "$KEY_PASS" \
  -dname "CN=Dramaku, OU=Mobile, O=SanzzAza, L=Jakarta, ST=Jakarta, C=ID"

KS_B64="$(base64_one_line "$KEYSTORE_PATH")"

cat <<EOF

============================================================
Dramaku release keystore created:
  $KEYSTORE_PATH

IMPORTANT:
- Backup this keystore and passwords somewhere safe.
- If this keystore is lost, you cannot update the same APK signature later.
- Never commit the keystore or passwords to git.

Add these GitHub repository secrets:
Settings > Secrets and variables > Actions > New repository secret
============================================================

ANDROID_KEYSTORE_BASE64
$KS_B64

ANDROID_KEYSTORE_PASSWORD
$STORE_PASS

ANDROID_KEY_ALIAS
$KEY_ALIAS

ANDROID_KEY_PASSWORD
$KEY_PASS

============================================================
After adding secrets, run GitHub Actions: Build APK.
The release should output Dramaku-v3.4-release.apk.
============================================================
EOF
