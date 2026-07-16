# Dramaku Native

Dramaku versi **full native Android** berbasis **Kotlin + Jetpack Compose**.

Nonton drama & film dari 10 platform dalam satu aplikasi native Android.

**Versi saat ini: 4.7.1**

## Platform

- Melolo
- FreeReels
- FlickReels
- DramaNova
- ReelShort
- NetShort
- DramaBox
- GoodShort
- MovieBox
- Drakor

## Fitur Native MVP

- Beranda native Compose
- Spotlight / rekomendasi / populer / terbaru
- Platform dropdown dengan placeholder “Pilih platform”
- Load beberapa page per platform supaya list drama lebih banyak
- Search gabungan lintas platform
- Detail drama native
- Episode grid native
- Favorit lokal
- Riwayat tontonan + progress lokal
- Settings native
- Share sheet Android native
- Native player vertical swipe episode ala TikTok memakai Compose VerticalPager + Media3 ExoPlayer
- Bar progress video, seek/drag, auto-hide overlay, episode sheet, double tap seek, long press 2x
- Search filter platform, progress per episode, episode range, Auto Next, dan default mode video
- Library hapus item satuan, settings final, dialog privasi/disclaimer, dan report episode native
- Remote config native, announcement/status platform, home premium, detail info tiles, search trending/filter
- Home infinite/progressive scroll dengan prefetch multi-page
- Cuplikan feed TikTok-style: swipe antar drama, autoplay Episode 1, tombol Tonton Semua Episode
- Bottom navigation premium: Cuplikan, Temukan, Hadiah, Daftar Saya, Profil tanpa emoji
- Cuplikan overlay compact: poster kecil, judul samping, label Ep.1 | Judul
- Bugfix cuplikan audio: preview stop total sebelum masuk full player/detail
- Bugfix cuplikan overlay: compact mini card agar tidak menutupi video
- Bugfix platform resolver: FreeReels raw HLS, GoodShort detail stream fallback, DramaNova fallback home
- Bugfix full player overlay: judul compact, poster mini, sinopsis pendek seperti WebView lama
- Platform logo/brand mark di dropdown dan status platform
- Copywriting Home dibuat lebih premium, bukan teks developer/native
- Melolo stream hardening: streamv2-only, player error listener, browser-like HLS headers
- MovieBox/FlickReels source error fix: safer CDN headers, HLS fallback, multi-resolution fallback
- Progress v2 dipisahkan berdasarkan platform agar ID drama lintas sumber tidak bentrok
- Unit test dasar untuk key progress dan parser remote config
- Target Android API 35 dan Gradle Wrapper 8.4 tersedia di repo
- Home lebih bersih tanpa section quick action yang tidak perlu
- Stream resolver 10 platform di Kotlin
- HTTPS-only network config

## Struktur Utama

```txt
app/src/main/java/com/dramaku/app/MainActivity.kt     # UI native Compose + data repository MVP
app/src/main/java/com/dramaku/app/PlayerActivity.java # Native ExoPlayer
app/src/main/java/com/dramaku/app/SplashActivity.java # Native splash
app/src/main/java/com/dramaku/app/storage/             # Storage key helpers
app/src/test/java/                                     # Unit tests
app/build.gradle                                      # Android/Kotlin/Compose dependencies
```

Repo ini **tidak memakai WebView, HTML, CSS, atau JS web shell** untuk aplikasi Android native.

## Versioning

Sumber versi APK:

```txt
version.properties
```

```properties
VERSION_NAME=4.7.1
VERSION_CODE=62
```

## Build APK

APK otomatis dibuild lewat GitHub Actions.

- Push ke `main`: build artifact APK
- Tag `v*`: build + GitHub Release
- Manual `workflow_dispatch`: bisa memilih create release

Build dan validasi lokal memakai Gradle Wrapper yang sudah disimpan di repo:

```bash
./gradlew testDebugUnitTest lintDebug assembleDebug
```

## Signed release APK

GitHub Actions membuat signed release APK jika seluruh signing secrets ini tersedia. Jika belum tersedia, workflow lama membuat release-variant APK dengan debug-key fallback untuk pengujian; jangan gunakan fallback tersebut sebagai rilis production:

- `ANDROID_KEYSTORE_BASE64`
- `ANDROID_KEYSTORE_PASSWORD`
- `ANDROID_KEY_ALIAS`
- `ANDROID_KEY_PASSWORD`

Panduan lengkap: [`docs/SIGNING.md`](docs/SIGNING.md).

## Migrasi Native

Detail migrasi ada di [`docs/NATIVE_MIGRATION.md`](docs/NATIVE_MIGRATION.md).

Struktur native ada di [`docs/NATIVE_STRUCTURE.md`](docs/NATIVE_STRUCTURE.md).

## Disclaimer & Privasi

Semua konten milik platform masing-masing. Aplikasi ini hanya sebagai aggregator UI/client.
Dramaku tidak meng-host video di server sendiri. Data user seperti history, favorit, dan progress disimpan lokal di perangkat.

Gunakan dengan bijak dan hormati hak cipta pemilik konten.
