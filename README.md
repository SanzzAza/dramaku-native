# Dramaku Native

Dramaku versi **full native Android** berbasis **Kotlin + Jetpack Compose**.

Nonton drama & film dari 10 platform dalam satu aplikasi native Android.

**Versi saat ini: 4.6.8**

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
- Home lebih bersih tanpa section quick action yang tidak perlu
- Stream resolver 10 platform di Kotlin
- HTTPS-only network config

## Struktur Utama

```txt
app/src/main/java/com/dramaku/app/MainActivity.kt     # UI native Compose + data repository MVP
app/src/main/java/com/dramaku/app/PlayerActivity.java # Native ExoPlayer
app/src/main/java/com/dramaku/app/SplashActivity.java # Native splash
app/build.gradle                                      # Android/Kotlin/Compose dependencies
```

Repo ini **tidak memakai WebView, HTML, CSS, atau JS web shell** untuk aplikasi Android native.

## Versioning

Sumber versi APK:

```txt
version.properties
```

```properties
VERSION_NAME=4.6.8
VERSION_CODE=59
```

## Build APK

APK otomatis dibuild lewat GitHub Actions.

- Push ke `main`: build artifact APK
- Tag `v*`: build + GitHub Release
- Manual `workflow_dispatch`: bisa pilih create release

Build lokal:

```bash
gradle wrapper --gradle-version 8.4
./gradlew :app:assembleDebug
```

Jika sudah punya wrapper:

```bash
./gradlew :app:assembleDebug
```

## Signed release APK

GitHub Actions otomatis membuat signed release APK jika secrets ini tersedia:

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
