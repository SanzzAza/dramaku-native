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

## v4.6.8 premium copy and platform logos

- Header Home mengganti copywriting developer menjadi teks user-facing premium.
- Dropdown platform dan status platform memakai logo/brand mark platform dengan fallback wordmark.
- Platform picker lebih mirip pengalaman WebView lama yang menonjolkan identitas platform.

## v4.6.7 compact full player overlay

- Full player overlay dibuat lebih mirip WebView lama.
- Top bar menampilkan judul kecil + info episode.
- Bottom overlay memakai poster mini, badge episode, judul compact, dan sinopsis pendek.
- Gradient bawah diperingan agar video tidak terlalu tertutup.

## v4.6.6 platform resolver fix

- FreeReels memakai raw HLS URL di native, bukan proxy WebView, plus MIME HLS.
- GoodShort resolver fallback ke `detail.list[].multiVideos/cdnList` saat `/stream` hanya metadata.
- DramaNova home diberi fallback sementara kalau endpoint sedang 503/kosong agar app tidak blank total.
- Fallback `PlayerActivity` juga set MIME HLS untuk `.m3u8`.

## v4.6.5 clips compact overlay fix

- Overlay Cuplikan diperkecil drastis agar tidak menutupi video.
- Poster mini 48x70, judul 1 baris, chip `Ep.1 | Judul` compact.
- CTA dipendekkan menjadi `Tonton` dan `Detail`.
- Gradient bawah dibuat lebih ringan/transparan.

## v4.6.4 clips audio handoff fix

- Fix suara bentrok saat tombol Tonton Semua ditekan dari Cuplikan.
- Player cuplikan sekarang pause, stop, dan clear media sebelum membuka full player/detail.

## v4.6.3 clean clips UI

- Hapus section Jelajah Cepat dari Home karena Cuplikan sudah menjadi bottom tab utama.
- Overlay Cuplikan dibuat compact: poster kecil/card di kiri, judul di samping, dan label `Ep.1 | Judul` di bawah.
- Tombol CTA dipersingkat menjadi `Tonton Semua`.

## v4.6.2 bottom nav clips

- Cuplikan dipindahkan ke bottom navigation sebagai tab utama.
- Bottom nav dibuat lebih premium/minimal tanpa emoji.
- Menu bawah: Cuplikan, Temukan, Hadiah, Daftar Saya, Profil.
- Tab Cuplikan langsung membuka feed fullscreen Episode 1 antar drama.

## v4.6.1 cuplikan feed

- Tambah fitur Cuplikan dari Home/Jelajah Cepat.
- Feed fullscreen ala TikTok/Reels: swipe atas/bawah untuk ganti drama.
- Tiap slide autoplay Episode 1 sebagai cuplikan.
- Overlay menampilkan judul, platform, total episode, dan tombol Tonton Semua Episode.
- Tombol Tonton Semua Episode membuka full native vertical episode player dari episode 1.

## v4.6.0 infinite home feed

- Home memakai `LazyListState` + `snapshotFlow` untuk progressive/infinite reveal saat user mendekati bawah.
- Prefetch API dinaikkan sampai 5 page untuk platform yang support pagination.
- Footer home menampilkan loading/remaining content saat konten berikutnya dibuka.

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
