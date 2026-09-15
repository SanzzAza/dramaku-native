# Changelog

Semua perubahan penting pada project Dramaku Native dicatat di dokumen ini.

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
