# Dramaku Native — UI Update: Editorial Cinema

## Arah desain

Tampilan diganti menjadi gaya **editorial cinema**: gelap netral, sederhana, dan fokus pada poster serta judul drama.

- Tidak memakai kuning, mint elektrik, cyan terang, glow, atau efek neon.
- Warna dasar: charcoal (`#111214`), surface abu gelap, dan teks off-white.
- Aksen dipakai sangat terbatas dalam warna dusty rose yang desaturasi.
- Sudut, border, dan spacing dibuat konsisten agar terlihat rapi tanpa dekorasi berlebihan.
- Tipografi tetap memakai Fraunces untuk judul penting dan Plus Jakarta Sans untuk antarmuka agar tetap mudah dibaca.

## Area UI yang diperbarui

1. **Splash screen & system bar** — palet awal aplikasi menjadi charcoal netral.
2. **Layar kategori** — layout dipadatkan menjadi daftar kategori yang lebih tenang, tanpa glow atau kartu berwarna terang.
3. **Navigasi bawah** — menjadi panel sederhana dengan active state yang halus.
4. **Header beranda & pemilih platform** — struktur informasi diperjelas, dengan card dan border yang lebih ringan.
5. **Hero, shelf, poster, pencarian** — poster diberi layer gelap untuk keterbacaan; kartu dan badge konsisten.
6. **Detail drama** — backdrop menjadi panel poster yang rapi; CTA, daftar episode, dan status nonton memakai sistem visual yang sama.
7. **Koleksi, profil, pengaturan, loading, empty/error state, dan player overlay** — mengikuti token warna, typography, dan komponen baru yang sama.

## Batas perubahan

Perubahan ini hanya berada pada layer tampilan:

- `MainActivity.kt` pada Compose UI dan design token.
- `SplashActivity.java` untuk tampilan splash.
- `styles.xml` untuk warna window/system bar.

Bagian berikut tidak diubah:

- Endpoint, request header, parser JSON, repository, cache stream, dan resolver API.
- State/alur pencarian, detail, episode, player, remote config, dan storage lokal.
- Model data serta test API/catalog yang ada.
