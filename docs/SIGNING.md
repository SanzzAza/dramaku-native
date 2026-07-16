# Dramaku Android Release Signing

Workflow **Build APK** mendukung dua jenis artifact:

- `Dramaku-v4.7.1-release.apk` jika signing secrets resmi tersedia dan valid.
- `Dramaku-v4.7.1-release-debugkey.apk` sebagai fallback pengujian jika signing belum tersedia.

Gunakan hanya APK dengan signing key resmi untuk distribusi production. Debug-key fallback dibuat agar APK tetap dapat dipasang saat pengujian dan bukan identitas rilis production.

## 1. Buat release keystore

Jalankan secara lokal, bukan di GitHub:

```bash
bash scripts/create-release-keystore.sh
```

Script membuat:

```txt
local/dramaku-release.jks
```

Script juga mencetak empat nilai GitHub secret. Simpan backup keystore dan password di tempat aman. Keystore yang hilang tidak dapat digunakan kembali untuk menandatangani update dengan identitas yang sama.

## 2. Tambahkan GitHub Actions secrets

Buka:

```txt
Settings > Secrets and variables > Actions > New repository secret
```

Tambahkan seluruh secret berikut:

```txt
ANDROID_KEYSTORE_BASE64
ANDROID_KEYSTORE_PASSWORD
ANDROID_KEY_ALIAS
ANDROID_KEY_PASSWORD
```

## 3. Build APK

Buka:

```txt
Actions > Build APK > Run workflow
```

Atau push tag `v*`, misalnya `v4.7.1`, untuk build sekaligus membuat GitHub Release.

Workflow akan decode dan memvalidasi keystore sebelum menjalankan `assembleRelease`. Nama artifact menunjukkan apakah APK memakai signing resmi atau debug-key fallback.

## 4. Membuat keystore secara manual

```bash
mkdir -p local
keytool -genkeypair \
  -v \
  -keystore local/dramaku-release.jks \
  -storetype PKCS12 \
  -alias dramaku \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -dname "CN=Dramaku, OU=Mobile, O=SanzzAza, L=Jakarta, ST=Jakarta, C=ID"
```

Encode ke base64 satu baris:

Linux:

```bash
base64 -w 0 local/dramaku-release.jks
```

macOS:

```bash
base64 local/dramaku-release.jks | tr -d '\n'
```

Untuk keystore PKCS12 yang dibuat oleh helper Dramaku, `ANDROID_KEY_PASSWORD` sama dengan `ANDROID_KEYSTORE_PASSWORD`.

## 5. File yang tidak boleh dikomit

`.gitignore` sudah mengecualikan:

```txt
local/
*.jks
*.keystore
keystore.properties
release-secrets.txt
app/release.keystore
```

Jangan menaruh keystore atau secret di source code, log, artifact publik, maupun percakapan.
