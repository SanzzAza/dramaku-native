# Changelog

Semua perubahan penting pada project Dramaku Native dicatat di dokumen ini.

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
