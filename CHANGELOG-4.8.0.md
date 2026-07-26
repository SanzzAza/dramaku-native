# Dramaku 4.8.0 — Category Home ala SonzaixBox

## Fokus
Layar awal baru: pintu masuk kategori seperti desain referensi SonzaixBox,
brand aplikasi tetap **Dramaku**.

## Layar Awal (Category Home)
- Sapaan dinamis sesuai jam: Selamat Pagi / Siang / Sore / Malam + emoji
- Header besar "Dramaku" + tombol pengaturan (overlay settings native)
- Glow merah lembut di area atas sesuai referensi
- Grid kategori 2x2:
  - **Short Drama** → 8 platform short drama (Melolo, FreeReels, FlickReels, DramaNova, ReelShort, NetShort, DramaBox, GoodShort)
  - **Movie Drama** → endpoint Drakor (Serial Korea & China)
  - **Movie Box** → endpoint MovieBox (film layar lebar)
  - **Anime** → "segera hadir"
- Kartu lebar **Manga** → "segera hadir"
- Tombol ☕ Traktir Kopi untuk Developer (link menyusul)
- Footer credit "Developed by Sonzai X シ"

## Integrasi Kategori
- Chips platform difilter sesuai kategori aktif
- Kategori 1 platform (Movie Drama / Movie Box) menyembunyikan chips dan menampilkan subtitle kategori
- Preferensi platform tersimpan per kategori (`cat_platform_<id>`)
- Tombol kategori di header Home + tombol Back Android kembali ke layar awal
- Detail, player vertikal, cuplikan, search, koleksi tetap berfungsi seperti semula

## Teknis
- Baru: `home/HomeCategory.kt` (enum kategori + `Greetings.forHour`)
- Baru: unit test `home/HomeCategoryTest.kt`
- `LocalStore`: `categoryPlatform()` / `setCategoryPlatform()`
- Versi 4.8.0 (versionCode 68)
