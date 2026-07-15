# Dramaku Native Migration

Project ini adalah Android full native berbasis **Kotlin + Jetpack Compose**.

## Yang sudah diganti

- `MainActivity` sekarang Compose native, bukan `WebView`.
- WebView lama, `index.html`, `assets/js`, dan `assets/css` sudah dihapus dari repo native ini.
- Native screen MVP:
  - Beranda native
  - Platform dropdown dengan placeholder “Pilih platform”
  - Multi-page loader agar list drama lebih banyak
  - Search gabungan lintas platform
  - Detail drama native
  - Episode grid native
  - Favorit native via `SharedPreferences`
  - Riwayat/progress native via `SharedPreferences`
  - Settings native
  - Share sheet Android native
- Player utama sekarang native vertical swipe episode ala TikTok memakai Compose `VerticalPager` + Media3 ExoPlayer.
- `PlayerActivity` masih ada sebagai fallback/native player sederhana.
- Resolusi stream 10 platform sudah berada di Kotlin repository untuk platform:
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

## File penting

- `app/src/main/java/com/dramaku/app/MainActivity.kt` — app native Compose utama.
- `app/src/main/java/com/dramaku/app/PlayerActivity.java` — native ExoPlayer.
- `app/build.gradle` — Compose/Kotlin/Coil/OkHttp dependencies.

## Build

Di CI project ini membuat Gradle wrapper otomatis lewat workflow. Lokal:

```bash
gradle wrapper --gradle-version 8.4
./gradlew :app:assembleDebug
```

Jika `gradle` belum ada di mesin lokal, build via GitHub Actions tetap bisa jalan karena workflow memakai `gradle/actions/setup-gradle`.

## v4.5.9 premium native pack

- Remote config native via `data/RemoteConfigRepository.kt`.
- Announcement banner dan status/maintenance platform di Home.
- Platform picker membaca remote config.
- Home tambah Status Platform dan Buat Kamu.
- Search tambah trending chips, clear recent, dan filter platform.
- Detail tambah info tiles dan sinopsis expand/collapse.
- Dokumentasi struktur native: `docs/NATIVE_STRUCTURE.md`.

## v4.5.8 final polish

- Hapus riwayat/favorit per item.
- Settings final dengan statistik, clear recent search, About, Privasi, Disclaimer.
- Report episode dari player lewat Android share sheet.
- Release signed APK tetap otomatis lewat GitHub Actions.

## v4.5.7 parity pack

- Search filter platform native.
- Progress video disimpan per episode.
- Episode range di detail/player untuk judul dengan banyak episode.
- Settings Auto Next dan default mode video Full/Asli.

## Player controls parity

Native player sekarang mendukung auto-hide overlay, tap untuk tampilkan kontrol, episode sheet saat nonton, pause saat app background, fit Full/Asli, retry, auto-next episode, progress bar detik, slider seek/drag, double tap seek ±10 detik, dan long press speed 2x.

## Next improvement

Repo ini sekarang bersih native. Improvement lanjutan yang disarankan:

1. Pisahkan `MainActivity.kt` menjadi package `data/`, `ui/`, `player/`, `storage/`.
2. Pindah storage dari `SharedPreferences` ke Room/DataStore.
3. Tambahkan ViewModel + retry/cache layer yang lebih rapi.
4. Buat player TikTok-style native penuh jika ingin mengganti `PlayerActivity` fullscreen standar.
5. Port remote config, platform maintenance, update checker, dan error log native.
