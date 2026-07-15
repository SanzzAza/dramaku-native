# Dramaku 4.3.0 — Stability & Security Hardening

## Yang diubah

### 1. Stabilitas API / offline
- Home memakai `Promise.allSettled` (sebagian endpoint gagal tetap tampil)
- Stream fetch pakai timeout + tombol **Coba Lagi**
- Boot dibungkus try/catch biar app tidak blank total
- Remote config fallback ke file bundled di APK
- `busy` tab di-reset di `finally` (anti stuck loading)

- Timeout fetch API **12 detik** (`AbortController`) agar UI tidak menggantung
- Fallback ke **cache stale** lebih andal + toast rate-limited
- Error state menampilkan detail + tombol **Coba Lagi** / **Bersihkan Cache**
- Banner **offline/online** di bagian atas layar
- Soft health-check API saat boot
- Status platform menandai API yang baru-baru ini gagal/lambat
- Logging error lebih lengkap (home/tab/detail/search/clips)

### 2. Versioning & CI
- Sumber versi terpusat: `version.properties`
- `app/build.gradle` membaca `VERSION_NAME` / `VERSION_CODE` (+ override CI)
- GitHub Actions:
  - push `main` → build artifact saja
  - tag `v*` → build + GitHub Release
  - workflow_dispatch bisa create release manual
- Nama APK dinamis dari version name
- Docs: `docs/VERSIONING.md`

### 3. Security WebView / Android
- Matikan `allowUniversalAccessFromFileURLs` & `allowFileAccessFromFileURLs`
- Mixed content: `COMPATIBILITY_MODE` (bukan always allow)
- Safe Browsing ON
- Cleartext traffic OFF + `network_security_config.xml`
- `allowBackup=false`
- Validasi URL di native bridge (`openUrl`, `share`, native player)
- External link dari WebView dibuka di browser eksternal
- ProGuard rules siap pakai; **minify release dimatikan dulu** biar WebView/bridge stabil (bisa dinyalakan belakangan)

### 4. Polish kecil
- `APP_VERSION` → 4.3.0
- Footer home pakai versi dinamis
- Share drama memakai `cleanText`
- `openExternalUrl` auto-prefix `https://` untuk `t.me/...`
- README & remote-config diselaraskan ke 4.3.0

## Cara rilis
```bash
# setelah review
git add -A
git commit -m "v4.3.0: API timeout/cache, WebView harden, versioning CI"
git push origin main

# publish release
git tag v4.3.0
git push origin v4.3.0
```
