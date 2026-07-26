# Dramaku 4.8.1 — Layar Awal Kategori (final MVP) + Credit SanzzXD

## Fokus
Menyelesaikan layar awal pintu masuk kategori sesuai desain referensi, dengan
API endpoint yang sudah aktif: **MovieBox** dan **Drakor**. Anime & Manga
sengaja belum diintegrasikan (ditandai "Segera hadir").

## Layar Awal (Category Home)
- Sapaan dinamis sesuai jam: Selamat Pagi / Siang / Sore / Malam + emoji
- Header besar "Dramaku" + tombol pengaturan (overlay settings native)
- Grid kategori 2x2 + kartu lebar Manga
  - **Short Drama** → 8 platform short drama vertikal
  - **Movie Drama** → endpoint Drakor (Serial Korea & China) — aktif
  - **Movie Box** → endpoint MovieBox (film layar lebar) — aktif
  - **Anime** → badge "Segera hadir", belum ada endpoint
  - **Manga** → badge "Segera hadir", belum ada endpoint
- Badge "Segera hadir" baru pada kartu Anime & Manga supaya jelas belum aktif
- Tombol ☕ Traktir Kopi untuk Developer
- Footer credit: **Developed by SanzzXD**

## Perbaikan Jaringan (MovieBox & Drakor)
- `getJson()` sekarang retry 3x dengan backoff singkat untuk error sementara
  (Cloudflare 5xx / origin bad gateway) yang sering muncul di endpoint
  MovieBox & Drakor
- Response JSON dengan field `code >= 400` sekarang dianggap error dan
  memunculkan pesan dari server, bukan list kosong yang membingungkan
- Header `Accept: application/json` ditambahkan pada semua request API

## Teknis
- `MainActivity.kt`: `ComingSoonBadge()` composable baru, dipakai di
  `CategoryMenuCard` dan `WideMenuCard`
- Credit footer diganti ke `Developed by SanzzXD`
- Versi 4.8.1 (versionCode 69)
- Diverifikasi lokal: `compileDebugKotlin`, `testDebugUnitTest`, `assembleDebug`
  semua BUILD SUCCESSFUL
