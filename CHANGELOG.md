# Changelog

Semua perubahan penting pada project Dramaku Native dicatat di dokumen ini.

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
