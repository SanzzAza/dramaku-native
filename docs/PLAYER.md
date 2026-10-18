# Player streaming 4.9.11

## Acuan dan batas kesesuaian

Susunan player diperiksa dari resource APK Melolo **5.3.6** yang diberikan
pemilik proyek: rail aksi selebar 56dp dengan ikon 32dp, judul ringkas di
bawah, track progres 2dp, panel katalog berposter, dan panel kecepatan.
Implementasi baru ditulis sendiri dalam Kotlin/Compose. Tidak ada dex, kode,
logo, video, atau aset proprietary APK referensi yang dimasukkan ke repo.
Identitas Dramaku (font Plus Jakarta Sans dan aksen kuning) dipertahankan.

Ini bukan klaim pixel-perfect yang sudah dibandingkan di perangkat. Resource
APK tidak menentukan seluruh tampilan runtime (misalnya eksperimen server,
ukuran layar, font scaling, dan posisi subtitle dari sumber). Perbandingan
visual akhir masih perlu dilakukan dengan APK referensi di perangkat Android.

## Interaksi

- Header hanya `Ep. n`, tombol kembali, dan menu; judul tidak diduplikasi.
- Ketuk video untuk jeda/putar. Ikon play tengah hanya terlihat saat jeda.
- Swipe vertikal berganti episode setelah halaman berhenti (`settledPage`).
- Double tap sisi kiri/kanan mundur/maju 10 detik; tengah menambahkan Suka.
- Tahan untuk 2x. Lepas, pembatalan gestur, atau background memulihkan speed.
- Suka dan Koleksi memakai penyimpanan lokal terpisah, tanpa angka sosial palsu.
- Tombol Episode membuka panel native 5 kolom, rentang 30 episode, poster,
  progres, penanda aktif, dan episode terkunci yang tidak dapat dipilih.
- Chrome auto-hide ±3,5 detik setelah video berjalan; ketuk sekali untuk
  menampilkannya lagi (tidak men-jeda). Jeda, seek, error, panel terbuka, atau
  ganti episode selalu menampilkan chrome kembali. Saat tersembunyi, garis
  progres tipis 2dp tetap terlihat di tepi bawah.
- Layar bersih menyembunyikan chrome; tombol keluar dan Back memulihkannya.
- Menu berisi speed 0,75x–2x, kualitas, subtitle, rasio, auto-next, dan refresh.
- Auto-next aktif secara default pada instalasi/preferensi yang belum disimpan;
  pilihan pengguna lama tetap dihormati. Episode terkunci tidak dilompati.

## Ketahanan playback

`MainActivity.kt` menangani sesi dan repository. UI ada di `player/`:

- `EpisodePlayerUi.kt`: chrome, seekbar, panel episode/pengaturan.
- `EpisodePlayerGestures.kt`: tap/double tap/hold yang tidak saling memicu.
- `EpisodeVideoSurface.kt`: TextureView native dan fallback embed terbatas.
- `PlaybackPolicy.kt`: resume, seek, range, next, dan retry budget murni Kotlin.

Hanya satu ExoPlayer aktif. Media lama disimpan lalu dihentikan **sebelum**
request episode berikutnya. Request yang dibatalkan diperiksa lagi sebelum
mengikat media. Poster menutupi transisi sampai frame pertama episode benar
tersedia. Progres memakai ID item media, bukan halaman yang sedang di-drag;
durasi tidak valid/loading tidak boleh menimpa progres tersimpan dengan nol.

Refresh link dan pergantian kualitas mempertahankan posisi. Auto-retry diberi
satu kesempatan per episode/percobaan manual, tidak di-reset ketika video
sempat memutar beberapa frame. Prefetch hanya URL episode selanjutnya, bukan
mengunduh seluruh episode. Buffer native memakai konfigurasi start 800ms dan
rebuffer 1.500ms, dengan decoder fallback, cache, audio focus, dan headset-noisy.

Aplikasi background dijeda dan mengikuti intent putar/jeda sebelumnya saat
kembali. Membuka panel juga menjeda player native, kemudian melanjutkan hanya
jika sebelumnya memang ingin memutar. Fullscreen/status/navigation bar serta
orientasi dipulihkan setelah keluar.

## Kualitas dan subtitle

- Pilihan kualitas mengikuti kemampuan API sumber yang saat ini menerima
  flag hemat data, bukan daftar resolusi fiktif. Trek HLS/DASH adaptif dibatasi
  saat mode hemat. MP4 satu kualitas tidak bisa di-upscale menjadi HD atau
  dipaksa turun resolusi hanya dengan track selector.
- Batas hemat data menyesuaikan portrait (480×854) atau landscape (854×480).
- Subtitle terpisah dideteksi dari hasil resolver **dan** trek media native.
- Subtitle yang sudah dibakar ke gambar tidak dapat dimatikan.
- Rasio asli menjadi default; preferensi penuhi layar yang sudah disimpan
  sebelumnya tidak dihapus.

## Anichin embed

Video langsung tetap native. Hanya URL embed HTTPS Anichin yang memakai
fallback WebView sumber. Tidak ada file/content access, mixed HTTP content,
JavaScript bridge, atau popup window yang diberikan ke embed. View dibersihkan
saat dibuang dan dibuat blank ketika background agar audio iframe berhenti.

Kontrol iframe tetap milik sumber; speed, seek/progres native, auto-next native,
dan pemulihan posisi iframe tidak dijanjikan. Kembali dari background memuat
ulang iframe. Gunakan Layar bersih bila kontrol sumber tertutup chrome.

## Validasi otomatis

JDK 17, Android SDK 35, Gradle Wrapper 8.4:

```sh
./gradlew testDebugUnitTest lintDebug testReleaseUnitTest assembleRelease
```

- Unit test kebijakan: resume normal/selesai/durasi pendek, seek batas/overflow,
  scrubbing invalid, range terakhir, auto-next terkunci/akhir, embed, retry.
- UI test Compose/Robolectric: judul tunggal, tidak ada transport besar saat
  bermain, callback Koleksi, seek aksesibel, layar bersih, range/lock episode,
  kecepatan, serta hold yang tidak memicu tap dan selalu memulihkan speed.
- Test UI menggambar chrome Android ke `app/build/reports/player/` untuk
  pemeriksaan layout. Background hitam adalah fixture tanpa video, bukan bukti
  bahwa stream atau decoder sudah diuji pada perangkat.
- Tema API 27/28/29 dipisah agar perangkat minSdk 24 tidak membaca atribut baru.
- Media3 unstable API di-opt-in secara eksplisit, bukan mematikan lint error.

## Checklist perangkat sebelum rilis production

1. Bandingkan chrome dan panel dengan Melolo 5.3.6 pada ukuran/font yang sama.
2. Putar sumber MP4, HLS, subtitle terpisah, serta sumber embed Anichin.
3. Swipe cepat 1→2→3 ketika jaringan lambat; pastikan tidak ada audio dobel
   atau respons episode lama yang mengambil alih.
4. Jeda, buka/tutup panel, ganti episode, ubah speed, tahan 2x lalu swipe/batal.
5. Seek awal/akhir; keluar-masuk; refresh/kualitas harus mempertahankan posisi.
6. Home/lock screen/panggilan/headset unplug: pastikan playback dijeda dengan benar.
7. Uji offline, koneksi pulih, link expired berulang; auto-retry tidak boleh loop.
8. Uji notch/gesture navigation, Android 7–15+, layar kecil, dan font besar.
9. Pastikan Suka dan Koleksi tidak tercampur dan dapat dihapus terpisah.

APK lokal tanpa signing secrets memakai debug-key fallback sesuai konfigurasi
repo. APK tersebut untuk pengujian, bukan signed production release. Signature
berbeda dari APK terpasang tidak dapat dipakai sebagai update langsung; jangan
menghapus aplikasi/data pengguna tanpa backup dan persetujuan.
