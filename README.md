# Dramaku Native

Dramaku versi **full native Android** berbasis **Kotlin + Jetpack Compose**.

Nonton drama & film dari berbagai platform dalam satu aplikasi native Android.

**Versi saat ini: 4.9.10**

## Platform Aktif

- **Melolo** (Short drama vertikal)
- **DramaNova** (Short drama vertikal)
- **FreeReels** (Short drama vertikal)
- **DramaBox** (Short drama vertikal)
- **NetShort** (Short drama vertikal)
- **ReelShort** (Short drama vertikal)
- **Anichin** (Donghua & anime subtitle Indonesia)

*Slot segera hadir: Drama Asia.*

## Tampilan 4.9

Antarmuka memakai latar **hitam pekat**, kartu abu gelap, teks putih, dan aksen **kuning terang**. Halaman Temukan menggunakan grid poster dua kolom, Perpustakaan memakai tab Histori/Koleksi/Suka, dan seluruh antarmuka menggunakan **Plus Jakarta Sans**. Font tertanam di `app/src/main/res/font/`, jadi tidak bergantung ke font sistem.

## Fitur Native MVP

- Animasi pembuka modern dengan halo, orbit, progress, dan transisi zoom-fade
- Langsung masuk ke layar Temukan tanpa onboarding akun atau login
- Chips platform difilter per kategori + preferensi platform tersimpan per kategori
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
- Player vertikal mengikuti susunan Melolo 5.3.6: fullscreen immersive, header Ep., judul ringkas, ikon kanan tanpa panel besar, dan seekbar putih 2dp
- Ketuk untuk jeda/putar, double tap sisi ±10 detik / tengah untuk Suka, tahan 2x, serta mode Layar bersih
- Panel episode 5 kolom dengan rentang 30 episode, poster, progres, episode aktif, dan status terkunci
- Menu kecepatan persisten 0,75x–2x, kualitas sumber/hemat data, subtitle terpisah, rasio, dan putar otomatis
- Suka terpisah dari Koleksi dan masing-masing tersimpan lokal di tab perpustakaan
- Pergantian episode menggunakan halaman settled, satu player aktif, pembatalan resolver lama, dan TextureView agar overlay/pager tidak bertabrakan
- Resume/progres terikat media episode, pause/resume lifecycle & audio focus, serta satu auto-retry per episode tanpa loop
- Search filter platform, progress per episode, episode range, Auto Next, dan default mode video
- Library hapus item satuan, settings final, dialog privasi/disclaimer, dan report episode native
- Remote config native, announcement/status platform, home premium, detail info tiles, search trending/filter
- Home infinite/progressive scroll dengan prefetch multi-page
- Cuplikan feed TikTok-style: swipe antar drama, autoplay Episode 1, tombol Tonton Semua Episode
- Bottom navigation: Cuplikan, Temukan, Perpustakaan, dan Profil; tanpa menu Hadiah
- Cuplikan overlay compact: poster kecil, judul samping, label Ep.1 | Judul
- Bugfix cuplikan audio: preview stop total sebelum masuk full player/detail
- Bugfix cuplikan overlay: compact mini card agar tidak menutupi video
- Bugfix platform resolver: FreeReels raw HLS, GoodShort detail stream fallback, DramaNova fallback home
- Full player memakai overlay minimal tanpa panel kontrol besar yang menutupi video
- Platform logo/brand mark di dropdown dan status platform
- Copywriting Home dibuat lebih premium, bukan teks developer/native
- Melolo stream hardening: streamv2-only, player error listener, browser-like HLS headers
- Refresh otomatis untuk URL video bertanda tangan yang hampir expired + satu kali auto-retry player
- FlickReels source error fix: safer CDN headers, HLS fallback, multi-resolution fallback
- Progress v2 dipisahkan berdasarkan platform agar ID drama lintas sumber tidak bentrok
- Unit test dasar untuk key progress dan parser remote config
- Target Android API 35 dan Gradle Wrapper 8.4 tersedia di repo
- Home lebih bersih tanpa section quick action yang tidak perlu
- Badge "Segera hadir" pada kartu Drama Asia di layar awal
- Retry otomatis + deteksi error JSON untuk endpoint platform aktif
- Stream resolver 10 platform di Kotlin
- DramaBox pindah ke proxy `dramahub.be/dramaboxbaru/api`; `/stream` kini JSON
  (`video` mp4 ber-token) dan rak genre diambil dari `/categories`
- NetShort masuk sebagai platform penuh lewat `dramahub.be/netshort/api/v1`
  (tabs/feed/explore/new/dubbing/vip, kategori + tag, search, detail, similar,
  stream 540p–1080p), plus chip saran pencarian dari `/search-hint`
- HTTPS-only network config

## Struktur Utama

```txt
app/src/main/java/com/dramaku/app/MainActivity.kt     # UI native Compose + data repository MVP
app/src/main/java/com/dramaku/app/home/HomeCategory.kt # Kategori layar awal + greeting waktu
app/src/main/java/com/dramaku/app/SplashActivity.java # Native splash
app/src/main/java/com/dramaku/app/player/              # Chrome, sheet, video surface, playback policies
app/src/main/java/com/dramaku/app/storage/             # Storage key helpers
app/src/test/java/                                     # Unit tests
app/build.gradle                                      # Android/Kotlin/Compose dependencies
```

UI utama dan video langsung memakai **Kotlin/Compose + ExoPlayer**, bukan web shell. WebView terbatas pada fallback video embed Anichin yang belum menyediakan URL video langsung.

## Versioning

Sumber versi APK:

```txt
version.properties
```

```properties
VERSION_NAME=4.9.10
VERSION_CODE=85
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

Catatan implementasi dan checklist pengujian player: [`docs/PLAYER.md`](docs/PLAYER.md).

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
