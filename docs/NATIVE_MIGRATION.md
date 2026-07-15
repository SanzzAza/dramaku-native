# Dramaku Native Migration

Project ini sudah mulai dipindahkan dari WebView shell ke Android full native berbasis **Kotlin + Jetpack Compose**.

## Yang sudah diganti

- `MainActivity` sekarang Compose native, bukan `WebView`.
- WebView lama dipertahankan sebagai `LegacyWebViewActivity` untuk referensi source, tetapi tidak dipakai di manifest dan web assets tidak lagi dipaketkan ke APK.
- Native screen MVP:
  - Beranda native
  - Platform selector 10 platform
  - Search gabungan lintas platform
  - Detail drama native
  - Episode grid native
  - Favorit native via `SharedPreferences`
  - Riwayat/progress native via `SharedPreferences`
  - Settings native
  - Share sheet Android native
- Player tetap memakai `PlayerActivity` + Media3 ExoPlayer native.
- Resolusi stream sudah dipindahkan dari `assets/js/player.js` ke Kotlin repository untuk platform:
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
- `app/src/main/java/com/dramaku/app/LegacyWebViewActivity.java` — WebView lama, hanya referensi.
- `app/build.gradle` — Compose/Kotlin/Coil/OkHttp dependencies.

## Build

Di CI project ini membuat Gradle wrapper otomatis lewat workflow. Lokal:

```bash
gradle wrapper --gradle-version 8.4
./gradlew :app:assembleDebug
```

Jika `gradle` belum ada di mesin lokal, build via GitHub Actions tetap bisa jalan karena workflow memakai `gradle/actions/setup-gradle`.

## Next improvement

MVP ini sudah native, tapi improvement lanjutan yang disarankan:

1. Pisahkan `MainActivity.kt` menjadi package `data/`, `ui/`, `player/`, `storage/`.
2. Pindah storage dari `SharedPreferences` ke Room/DataStore.
3. Tambahkan ViewModel + retry/cache layer yang lebih rapi.
4. Buat player TikTok-style native penuh jika ingin mengganti `PlayerActivity` fullscreen standar.
5. Port remote config, platform maintenance, update checker, dan error log native.
