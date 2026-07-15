# Dramaku - Streaming Drama & Film Terbaik

Nonton drama & film dari 10 platform sekaligus dalam 1 app Android native / web demo.

**Versi saat ini: 4.5.2**

## Platform
- Melolo, FreeReels, FlickReels, DramaNova, ReelShort
- NetShort, DramaBox, GoodShort, MovieBox, Drakor

## Fitur Utama
- Home cinematic dengan Spotlight Harian, mood shortcut, rekomendasi, populer, dan terbaru
- Halaman detail ala aplikasi film: resume episode, info tiles, sinopsis, episode grid, favorit/share, dan rekomendasi mirip
- Player TikTok-style: scroll vertikal episode, full/asli fit toggle, seek bar drag, progress tersimpan, subtitle otomatis, dan double-tap like
- Search gabungan 10 platform dengan recent search dan filter platform
- Riwayat tontonan & Favorit via localStorage
- Cache API ringan + timeout 12s + fallback cache saat koneksi/API tidak stabil
- Banner offline/online dan error state yang lebih jelas
- Settings page, error reporting lokal, mode hemat data, dan kontrol cache
- Native splash screen, onboarding pertama kali, dan laporan episode bermasalah
- Crash-safe WebView recovery screen dengan reload dan clear cache
- Remote config untuk endpoint API, status platform, announcement, dan feature flag
- Profile Center “Saya” untuk riwayat, favorit, tools cepat, dan statistik lokal
- Struktur web modular: `assets/js/{core,search,home,detail,player,library,app}.js` + `assets/css/style.css`
- Skeleton/loading state premium untuk home dan detail
- Cuplikan feed: grid dua kolom, quick chips, mini continue player, dan tombol play langsung
- Update checker, status platform, About/Disclaimer, dan report episode ke support
- Player gesture: double tap seek 10 detik dan long press speed 2x
- Platform Drakor: Korea, China, terbaru, trending, ongoing, search, detail, stream
- Visual Discovery: Top 10, Mood Picker, Continue Watching carousel, trending search chips
- Search dedupe + ranking relevansi lintas platform
- Rating hanya ditampilkan jika data aslinya ada (bukan angka palsu)
- Native ExoPlayer progress sync ke history/web
- Visual v4.5: cinematic glass UI, card depth, premium splash/nav
- Paket Gacor: Buat Kamu, progress poster, badge LANJUT/BARU, pull-to-refresh
- Jelajah Cepat: shortcut aksi + chip Lagi Viral (ganti Mood Picker)

## Branding
- Brand kit tersedia di folder `branding/`
- Launcher icon Android sudah diganti dengan monogram Dramaku baru
- Header, splash, favicon, dan app icon memakai identitas visual yang sama

## APK / Native Android
- Main app sudah mulai dipindahkan ke full native: Kotlin + Jetpack Compose
- Beranda, search, detail, koleksi/favorit/riwayat, settings, dan episode grid native
- Native ExoPlayer activity untuk pemutaran video
- Stream resolver 10 platform dipindahkan ke Kotlin repository
- WebView lama masih disimpan sebagai `LegacyWebViewActivity` untuk referensi migrasi
- Release build memakai R8-ready config
- HTTPS-only network config (cleartext diblokir)

Panduan migrasi: [`docs/NATIVE_MIGRATION.md`](docs/NATIVE_MIGRATION.md).

## Versioning
Sumber versi terpusat: [`version.properties`](version.properties)

```
VERSION_NAME=4.5.2
VERSION_CODE=39
```

Jaga agar `APP_VERSION` di `assets/js/app.js` dan `latestVersion` di `remote-config.json` sinkron saat rilis.

## Build APK
APK otomatis dibuild melalui GitHub Actions.

- **Push ke `main`**: build artifact saja (tanpa spam release)
- **Tag `v*`** (contoh `v4.3.0`): build + GitHub Release
- **Manual workflow_dispatch**: bisa pilih create release

Download di tab **Releases**.

Build lokal juga otomatis menyalin `index.html`, `assets/`, dan `branding/` ke asset APK melalui task Gradle `copyIndexHtml`.

### Signed release APK
GitHub Actions otomatis membuat **signed release APK** jika secrets ini tersedia:

- `ANDROID_KEYSTORE_BASE64`
- `ANDROID_KEYSTORE_PASSWORD`
- `ANDROID_KEY_ALIAS`
- `ANDROID_KEY_PASSWORD`

Jika secrets belum ada, workflow akan membuat debug APK fallback.

Panduan lengkap: [`docs/SIGNING.md`](docs/SIGNING.md).

## Remote Config
File default: [`remote-config.json`](remote-config.json).

Panduan: [`docs/REMOTE_CONFIG.md`](docs/REMOTE_CONFIG.md).

## Web demo
- https://dramafeed.vercel.app/

## Struktur JS
Lihat [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md).

## Disclaimer & Privasi
Semua konten milik platform masing-masing. Aplikasi ini hanya sebagai aggregator UI/client.
Dramaku tidak meng-host video di server sendiri. Data user (history/favorit/progress) disimpan lokal di perangkat.
Di dalam app: **Setelan → Tentang / Privasi / Disclaimer**.

Gunakan dengan bijak dan hormati hak cipta pemilik konten.
