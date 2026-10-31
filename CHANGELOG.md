# Changelog

Semua perubahan penting pada project Dramaku Native dicatat di dokumen ini.

## 4.9.20 (2026-10-31)

- Fix FilmKu tidak bisa diputar: respons `/api/source` Vidrift sering memberi
  URL HLS lewat proxy (`edge.reelvault.click/proxy?u=…`) yang **tidak punya
  ekstensi `.m3u8`**. `PlaybackPolicy.isEmbeddedStream` menganggap semua URL
  FilmKu non-`.m3u8` sebagai iframe, sehingga playlist HLS dibuka di WebView
  (layar hitam/teks). Kini resolver menandai hasil secara eksplisit
  (`isEmbed` + `mimeType`), ExoPlayer menerima MIME `application/x-mpegURL`
  langsung, dan hanya halaman provider yang dikenal yang dialihkan ke WebView.
- Pemilihan stream FilmKu mengutamakan HLS https (`pickStream`), dengan unit
  test regresi untuk URL proxy tanpa ekstensi.

## 4.9.19 (2026-10-19)

- Fix pergantian episode DramaBox: resolver sebelumnya mengirim parameter
  `episode`, yang diabaikan proxy v2 sehingga stream selalu kembali ke EP 1.
  Request kini memakai parameter `index` 0-based sesuai chapterIndex API
  (episode 1 → index 0, episode 2 → index 1), dengan unit test regresi.

## 4.9.18 (2026-10-19)

- Fix DramaBox "Sumber video gagal dibuka": ternyata file CDN dramaboxdb
  (`.encrypt.mp4`) memakai enkripsi Aliyun private — header kontainer polos
  (durasi tampil, mis. 3:07) tapi bitstream h264 tersandi sehingga decoder
  selalu gagal. Diverifikasi dengan decoder ffmpeg: CDN langsung 0 frame,
  output gateway 30 frame bersih (gateway men-decrypt on-the-fly, area mdat
  berbeda). Bypass CDN dari 4.9.17 dibatalkan; playback kembali lewat gateway.
- Kualitas DramaBox dibatasi 720p (hemat data: 540p) supaya relay gateway
  tidak keteteran, plus timeout HTTP pemutar dilonggarkan (connect 20 dtk,
  read 45 dtk) untuk sumber yang byte pertamanya lambat.

## 4.9.17 (2026-10-19)

- DramaBox mulai diputar jauh lebih cepat. Gateway relay `iniapi` yang
  dibalas endpoint /play ternyata sangat lambat (TTFB terukur ~12 detik,
  throughput belasan KB/s) — itulah kenapa docs terasa cepat (hanya JSON)
  tapi video lama bufferingnya. Param `u=` di URL gateway berisi base64 URL
  CDN dramaboxdb bertanda tangan CloudFront dengan konten byte-per-byte
  identik (TTFB ~0,1 dtk, ~4 MB/s); aplikasi kini men-decode dan memutar
  CDN-nya langsung, dengan fallback otomatis ke gateway bila CDN menolak.

## 4.9.16 (2026-10-19)

- DramaBox hidup kembali 🎉 — migrasi ke proxy v2 (`dramahub.be/dramaboxv2/api/v1`)
  setelah proxy lama (`dramaboxbaru`) mati total: home ber-page + rank,
  detail `book?id=`, stream `play?bookId=&episode=` (videoPathList
  540/720/1080 — hemat data memilih 540, normal 1080), search tetap jalan.
- Episode berformat `chapterIndex` 0-based didukung; episode berbayar tetap
  diputar lewat proxy (tidak dikunci di UI).
- Buku katalog prefix `41…` yang selalu dijawab "tidak ditemukan" oleh
  upstream difilter dari home/rank/pencarian agar tidak ada judul zonk.
- Rak genre DramaBox menyesuaikan: tanpa /categories & /hidden-gems di v2,
  beranda memakai satu rak "Jelajahi katalog".

## 4.9.15 (2026-10-19)

- Audit 42 platform via smoke test otomatis (`scripts/smoke_test_platforms.py`)
  — hasil lengkap di `docs/PLATFORM_STATUS.md`. Terkonfirmasi mati di sisi
  server (bukan bug aplikasi): dramanova, dramabox, sodareels, sereal,
  shortsky, goodshort, flickreels; CDN bonustv & dramarush menolak akses (403).
- Header video kini di-scope per host: Bearer dramahub & Referer cdreader
  hanya dikirim ke dramahub.be / *.cdreader.com, tidak lagi ke semua CDN.
  Mencegah 400/403 di URL presigned (S3/GCS) dan menghentikan kebocoran token
  ke pihak ketiga. Probe kedaluwarsa memakai header yang sama dengan pemutar
  sehingga link sehat tidak salah divonis expired.
- Pesan error jaringan 5xx/404 kini menyebut gangguan server sumber secara
  eksplisit, supaya jelas bukan kerusakan aplikasi.

## 4.9.14 (2026-10-19)

- Pencarian: riwayat kini hanya tercatat saat hasil benar-benar dibuka —
  tidak lagi dipenuhi potongan ketikan dari debounce. Daftar “Pencarian
  terakhir” tampil hingga 8 entri dengan tombol “Hapus semua”.
- Notifikasi pengingat “Lanjutkan nonton?”: opsi baru di Profil → Preferensi.
  Sehari setelah sesi nonton terakhir, bila episode belum selesai, muncul
  notifikasi berisi judul, nomor episode, dan persentase progres. Memakai
  WorkManager; izin POST_NOTIFICATIONS diminta saat opsi diaktifkan (Android
  13+), dan episode yang sudah tamat tidak diingatkan.

## 4.9.13 (2026-10-19)

- Toast “Dilanjutkan dari 04:32” saat episode diputar kembali dari posisi
  tersimpan (>3 detik), termasuk setelah muat ulang link.
- Getaran halus saat double tap tengah menambahkan Suka.
- Petunjuk sekali-tayang “Geser ke atas untuk episode berikutnya” dengan
  chevron memantul; hilang setelah swipe pertama atau 5 detik, tersimpan
  agar tidak muncul lagi.

## 4.9.12 (2026-10-19)

- Player makin mirip Melolo: judul `Ep.n · Judul ›` satu baris, cuplikan
  sinopsis 2 baris di bawah judul, jumlah penonton asli dari API di bawah ikon
  Suka, dan pill episode kini memuat label platform.
- Double tap tengah memunculkan burst hati melayang (pop + 6 hati kecil
  menyebar), menggantikan satu ikon hati statis.
- Home: rail “Lagi Trending” dengan angka peringkat besar ala Top-10 di atas
  poster plus badge jumlah tayang, diambil dari daftar populer sumber.
- Home: kartu “Lanjut Nonton” mengambang di bawah — satu ketuk kembali ke
  episode dan posisi terakhir, bisa ditutup, muncul lagi saat riwayat berganti.

## 4.9.11 (2026-10-19)

- Fix: overlay player (judul, tombol Suka/Koleksi, panel episode) kembali
  auto-hide ±3,5 detik setelah video berjalan, seperti perilaku sebelum
  redesign Melolo. Ketuk sekali menampilkan kontrol lagi tanpa men-jeda;
  jeda, seek, error, panel terbuka, atau ganti episode memunculkan kontrol.
- Saat kontrol tersembunyi, garis progres tipis 2dp tetap tampil di tepi
  bawah layar agar posisi tontonan tetap terbaca.

## 4.9.10 (2026-10-18)

- Player mengikuti susunan resource Melolo 5.3.6, tetap dengan identitas Dramaku:
  header Ep., ikon kanan tanpa bubble besar, judul satu kali, panel episode
  bawah, seekbar putih 2dp, dan layar bersih. Kontrol tengah hanya muncul saat jeda.
- Sheet native dengan drag-to-dismiss, poster, grid 5 kolom, rentang 30 episode,
  indikator progres, serta status episode aktif/terkunci.
- Ketuk jeda/putar, double tap sisi ±10 detik atau tengah untuk Suka, tahan 2x
  dengan kecepatan dipulihkan saat gestur selesai/dibatalkan. Preferensi speed tersimpan.
- Suka kini dipisahkan dari Koleksi; tab Suka tidak lagi sekadar membalik daftar favorit.
- Episode di-resolve setelah pager settled; media lama dihentikan sebelum request,
  respons yang dibatalkan tidak boleh mengikat player, dan TextureView mengikuti swipe.
- Progres disimpan memakai identitas media yang benar, tidak tertimpa nol saat loading,
  serta dipertahankan saat refresh/kualitas berubah. Auto-retry dibatasi satu kali
  per episode/percobaan manual, bukan di-reset setiap frame berhasil diputar.
- Audio focus/headset unplug ditangani, video dijeda saat aplikasi di background,
  mode fullscreen dipulihkan saat keluar, dan embed dibersihkan saat dispose/background.
- Hemat data memperhitungkan orientasi video; subtitle dideteksi dari trek native
  maupun file eksternal. Rasio asli menjadi default agar gambar tidak terpotong.
- Tambahkan unit test kebijakan resume, seek, rentang, episode terkunci, embed,
  serta retry budget. APK referensi hanya dianalisis, tidak disalin ke aplikasi.

- Desain ulang seluruh antarmuka dengan latar hitam pekat, kartu abu gelap,
  tipografi sans-serif, dan aksen kuning terang.
- Animasi pembuka baru: logo kuning dengan halo berdenyut, titik orbit,
  wordmark bertahap, progress halus, dan transisi zoom-fade ke aplikasi.
- Perbaikan link video kedaluwarsa untuk seluruh sumber: expiry `auth_key`,
  Bunny `expires`, dan AWS `X-Amz-Date/X-Amz-Expires` kini dibaca; detail
  bertanda tangan dimuat ulang otomatis dan cache detail dibatasi 5 menit.
- Seluruh 35 platform tambahan kini mendahulukan endpoint stream baru daripada
  URL inline dari detail. Enam sumber direct-link ditangani dengan cache detail
  pendek; URL signed divalidasi ke CDN, cache stream dibatasi 45 detik, dan
  player mencoba ulang sekali dengan URL baru sebelum menampilkan error.
- Navigasi bawah kini hanya Cuplikan, Temukan, Perpustakaan, dan Profil;
  menu Hadiah dihapus.
- Pemutar streaming didesain ulang menjadi pengalaman short-drama vertikal:
  video layar penuh, info cerita minimal di kiri bawah, toolbar Simpan/Episode/
  Bagikan/Lainnya di kanan, kontrol play dan seek di tengah, serta progress tipis.
- Sheet episode kini memakai grid 5 kolom. Menu pemutar menyediakan kualitas,
  subtitle, rasio layar, kecepatan 0,75x–2x, dan refresh link terbaru; gestur
  swipe episode, double tap, dan tahan 2x tetap tersedia.
- Aplikasi langsung membuka Temukan, tetap menyediakan feed Cuplikan, dan tidak
  menambahkan onboarding akun, form login, maupun sistem autentikasi.
- Hapus kartu Movie Box dan Bstation dari layar awal.
- Hapus integrasi MovieBox, Shorts MovieBox, dan Bstation beserta endpoint,
  resolver, katalog, test, dan aset logo terkait.
- Preferensi platform lama yang sudah tidak tersedia kini otomatis kembali ke
  platform aktif, tanpa menembak endpoint yang telah dihapus.

## 4.9.9 (2026-09-14)

- Platform baru **NetShort** lewat proxy `dramahub.be/netshort/api/v1` (Bearer):
  tabs, tab/{id}/{page}, feed, explore, new, dubbing, vip, search, search-hint,
  categories, category (region/audio/tagId), detail, similar, dan episode
- Beranda NetShort: tiga slot rak diisi endpoint berbeda (explore, feed, new)
  supaya layar pertama variatif, halaman berikutnya memakai feed/explore dengan
  nomor halaman naik — infinite scroll dapat judul baru
- Rak genre NetShort dirakit dari `/tabs` (tab konten: Dubbing, Pelukan Jakarta,
  Anime) dan `/categories` (tag Bahasa Indonesia), keduanya di-cache per sesi
  dan punya daftar cadangan kalau endpoint katalog sedang kosong
- Detail NetShort memakai `data.episodes` (episodeNo + episodeId) dan
  `totalEpisodes`; episode bertanda `isLocked` tetap bisa dibuka karena
  `/episode` upstream memang melayaninya
- Stream NetShort: `/episode/{id}/{ep}` → pilih kualitas 720p (normal) atau
  540p (mode hemat data), balasan `subtitles` ikut dipakai kalau ada
- Pencarian NetShort: keyword masuk ke path (`search/{keyword}/{page}`), spasi
  di-encode %20 karena proxy menolak "+", dan chip saran pencarian dari
  `/search-hint` dibersihkan dari emoji (API menolak emoji di keyword)
- Hint MIME player menambahkan `mime_type=video_mp4` supaya URL NetShort tanpa
  ekstensi (video.netshort.com) tetap dianggap mp4, bukan playlist
- Parser list `flat()` kini menembus array bertingkat yang berisi potongan JSON
  (dipakai feed NetShort), tidak cuma array of object
- Helper baru `NetshortCatalog` (tabs/tag/saran/URL/quality picker) + 10 unit
  test

## 4.9.8 (2026-09-14)

- DramaBox pindah ke proxy baru `dramahub.be/dramaboxbaru/api` (host lama
  `captain.sapimu.au` sudah tidak resolve / mati): home (banner + section),
  rank, recommend/book, hidden-gems, categories, search, drama/{id}, dan stream
- Parser stream DramaBox baru: `/stream` kini membalas JSON
  `{ code, video, duration, subtitles }` — yang diputar adalah `video`
  (mp4 ber-token yang redirect 302 ke CDN dramaboxdb), bukan URL endpoint-nya.
  Balasan non-JSON tetap didukung sebagai cadangan
- Hint MIME player diperbaiki: mp4 ber-token `…&seg=video.mp4&k=…` tidak lagi
  ditebak sebagai playlist HLS (sebelumnya semua URL dramaboxbaru/api/stream
  dianggap HLS, jadi video baru gagal dibaca ExoPlayer)
- Subtitle DramaBox ikut dibaca dari balasan stream (`subtitles`) kalau upstream
  mengirimnya
- Rak genre DramaBox sekarang dari `/categories` (label & type id ikut katalog
  upstream, saat ini 39 genre; rak dibatasi 10 genre pertama + hidden-gems, dan
  daftarnya di-cache per sesi). Daftar type id lama tetap dipakai sebagai
  cadangan kalau endpoint itu kosong
- Kartu hasil `/search` DramaBox ikut menampilkan jumlah episode (field
  upstream-nya `totalChapterNum`, sebelumnya terbaca 0)
- Helper `DramaboxCatalog` (parser stream + kategori) beserta unit test-nya,
  mengikuti pola `MovieboxCatalog`

## 4.9.7 (2026-08-02)

- Fix stream Melolo tidak bisa diputar: hint HLS untuk URL "/stream?" ikut
  kena ke MP4 Melolo (tobrutmelolo/inicdn), padahal itu video progresif —
  ExoPlayer gagal baca. Hint sekarang spesifik ke dramaboxbaru/api/stream

## 4.9.6 (2026-08-02)

- Fix search MovieBox: perPage=24 dibalas upstream dengan data kosong —
  radius yang aman memang cuma 10–20, jadi dipaksa perPage=20

## 4.9.5 (2026-08-02)

- MovieBox hidup lagi lewat endpoint baru `captain.sapimu.au/moviebox/api`:
  home-content 237 judul sekali tarik, kategori asli (K-Drama, C-Drama,
  Anime, Action, Romance, Comedy, TOP100, New), search, subject/get dengan
  daftar episode per-season, dan stream mp4 resolusi penuh
- Platform "Shorts" baru di dalam kategori Movie Box — drama vertikal dari
  shorts/reel + shorts/most-trending, detail shorts/info, episode diputar
  lewat shorts/mini-list
- Dukungan request POST di repository (endpoint shorts & search menolak
  GET dengan 500); URL feed boleh ditandai "POST "
- Repository bisa resolve stream MovieBox (stream/{id}?ep&se) dan Shorts
  (mini-list?ep) — dua-duanya mp4 langsung dari CDN
- Rak genre Movie Box: K-Drama, C-Drama, Anime, Action, Romance, Comedy

## 4.9.4 (2026-08-02)

- DramaBox resmi jadi platform kedua di kategori Short Drama, lewat endpoint
  baru `captain.sapimu.au/dramaboxbaru/api` — home (banner + 3 section),
  rank (18), recommend (18), hidden-gems (14), kategori asli (37 genre),
  browse berhalaman, search berhalaman, detail + daftar chapter, dan stream
  m3u8 langsung dari endpoint
- Bahasa Indonesia pakai kode `lang=in` (`lang=id` 500 di upstream-nya)
- Rak genre beranda sekarang beda per platform: DramaBox punya Permata
  tersembunyi, Kekuatan super, Kawin kontrak, Melawan balik, Kelahiran
  kembali, Balas dendam, Cinta pahit, Perjalanan waktu — diambil dari
  kategori asli endpoint, bukan tebakan search
- Player (Compose & PlayerActivity) selalu pasang Bearer proxy supaya
  playlist stream DramaBox bisa kebaca; hint mime HLS untuk URL tanpa .m3u8
- Auth token proxy kini berlaku untuk semua host captain.sapimu.au

## 4.9.3 (2026-08-02)

- Rak genre ngikutin tab asli aplikasi resmi: Populer, Romansa, Sistem, Harem,
  CEO & harta, Balas dendam, Lintas waktu, Kekuatan super, Wanita kuat,
  Kelahiran kembali — semuanya terverifikasi hidup (50–63 judul per genre)
- Bersih-bersih besar: kode 9 platform yang upstream-nya sudah mati
  (FreeReels, FlickReels, DramaNova, ReelShort, NetShort, DramaBox, GoodShort,
  MovieBox, Drakor) dihapus dari repository — cabang resolveStream, homeUrls,
  detailUrl, loadDetail, header CDN khusus, helper link kedaluwarsa, dan
  fallback dramanova ikut pergi
- Melolo jadi satu-satunya jalur data: bookmall/book/tabs untuk listing,
  multi-video untuk detail & stream, search untuk pencarian dan rak genre
- Pagination disederhanakan: upstream mengabaikan offset/page/session

## 4.9.2 (2026-08-02)

- Beranda Melolo tidak lagi terasa kosong: feed home tetap 18 judul (batas upstream),
  tapi di bawahnya sekarang ada rak genre — Romansa, CEO & harta, Balas dendam,
  Lintas waktu, Wanita kuat — diambil dari katalog search yang jauh lebih dalam
  (~380 judul terverifikasi hidup). Judul di feed 18 tidak diulang di rak.
- Pesan error untuk sumber yang mati diganti yang lebih manusiawi
  ("Sumber ini sedang tidak tersedia. Coba rak lain dulu ya.")
- Test HomeCategoryTest dibetulkan sesuai desain melolo-only saat ini

## 4.9.1 (2026-08-02)

- Fix: rak/genre Melolo ("Trending", "Peringkat", "Time Travel", dll) tidak lagi ikut keparse sebagai drama — filter home sekarang mewajibkan sinyal konten nyata (cover/sinopsis/jumlah episode)
- Fix: infinite scroll Melolo berhenti fetch halaman yang isinya selalu 18 judul sama (proxy mengabaikan offset/session), footer jujur bilang "Itu semua untuk sekarang."
- Hero beranda kembali menampilkan judul asli dengan poster, bukan kartu kosong

## 4.9.0

### Tampilan baru, dari awal

Seluruh lapisan UI ditata ulang. Arahnya: "bioskop malam" — gelap yang hangat,
bukan hitam kebiruan, dengan satu aksen hijau mint dan tombol utama warna krem.

- Palet warm-ink baru: `#12100D / #1A1712 / #211D16` dengan hairline hangat
- Aksen mint `#3CD79E` + krem `#F2EBDD` untuk tombol utama dan pilihan aktif
- Tipografi bundel baru: **Fraunces** untuk judul/angka besar,
  **Plus Jakarta Sans** untuk seluruh teks antarmuka (tidak lagi ikut font sistem)
- Layar awal kategori jadi indeks bergaya majalah: nomor 01/02/03, hairline,
  tanggal hari ini, sapaan waktu
- Header beranda menampilkan tanggal dan wordmark "Dramaku."
- Chip platform terpilih sekarang krem solid, bukan blok hijau
- Hero, kartu lanjutan, grid jelajah, pencarian, koleksi, profil, detail drama
  dan overlay player diselaraskan ke satu bahasa tampilan
- Tombol "Tonton sekarang" konsisten krem; aksen hijau hanya untuk status
- Kotak pengaturan digroup dengan pembatas tipis, bukan kartu bertumpuk
- Splash anyar: watermark "D" serif, garis progres tipis, animasi halus tanpa bounce
- Salinan teks ditulis ulang biar terdengar manusiawi ("Itu semua untuk
  sekarang.", "Pilih rak tontonanmu")
- Font disertakan di `app/src/main/res/font/` (OFL), total ±470 KB

Tidak ada perubahan ke repository, stream resolver, storage, maupun player engine.
