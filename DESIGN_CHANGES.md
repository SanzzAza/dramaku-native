# 🎬 Dramaku — Redesign Summary

## Overview
Perombakan total UI/UX agar terlihat lebih profesional seperti aplikasi drama China premium (WeTV, iQIYI, ShortTV). Semua **business logic** (repository, local store, JSON helpers, API calls, data models) tetap **100% sama** — hanya layer visual yang diubah.

---

## 🎨 Design System (DS)

### Palet Warna Baru
| Elemen | Lama | Baru |
|--------|------|------|
| Background | `#12100D` (gelap hangat) | `#0A0908` (near-black sinematik) |
| Surface/Card | `#1A1712` | `#1A1714` (lebih dalam) |
| Aksen Utama | `#3CD79E` (mint) | `#2EE8A0` (mint elektrik, lebih vibrant) |
| Teks Utama | `#F0EBE0` | `#F5F0E8` (lebih clean) |
| Teks Body | `#C7C1B2` | `#B8B0A0` |
| Error | `#E0684F` | `#EF5350` (merah lebih tegas) |
| Gold | `#D9A85C` | `#D4A853` |
| Baris/Batas | `0x14F2E7D5` | `0x0DF5E8D0` (lebih subtle) |

**Filosofi**: Lebih gelap, lebih kontras, lebih sinematik — seperti bioskop premium.

---

## 📱 Perubahan Per-Layar

### 1. Layar Awal (CategoryHomeScreen)
- ✅ **Card-based categories** — Setiap kategori punya card dengan ikon berwarna, subtitle, dan tombol panah
- ✅ **Color-coded icons** — Short Drama (mint), Drama Asia (gold), Movie Box (purple)
- ✅ **Spacing lebih lega** — 12dp gap antar card
- ✅ **Header lebih compact** — Brand mark lebih kecil, tanggal lebih halus

### 2. Bottom Navigation
- ✅ **Center action button** — Tombol "Cuplikan" di tengah lebih menonjol dengan background mint
- ✅ **Active indicator** — Dot kecil di bawah ikon aktif
- ✅ **Gradient top border** — Garis atas dengan gradient horizontal
- ✅ **Spacing lebih baik** — Ikon lebih besar, teks lebih kecil

### 3. Home Screen
- ✅ **Hero Card lebih besar** — 440dp (dari 380dp), multi-layer gradient
- ✅ **"TRENDING" badge** — Badge hijau di pojok kiri atas hero
- ✅ **Platform logo di pojok** — Badge platform di pojok kanan atas hero
- ✅ **Dual CTA buttons** — "Tonton" (solid mint) + "Detail" (outlined)
- ✅ **Section headers** — Ada tombol "Semua" di kanan setiap section
- ✅ **Platform chips** — Chip dengan logo platform di dalamnya

### 4. Kartu Drama (DiscoverDramaCard)
- ✅ **Aspect ratio 0.68** — Lebih proporsional seperti Netflix
- ✅ **Rounded corners 12dp** — Lebih smooth
- ✅ **Badge overlay** — "BARU" badge di kiri atas
- ✅ **Episode + Views badges** — Bottom-left & bottom-right dengan ikon
- ✅ **Platform logo** — Di pojok kanan atas poster

### 5. Continue Watching Card
- ✅ **Play icon overlay** — Lingkaran semi-transparan dengan ikon play di tengah
- ✅ **Aspect ratio 0.7** — Lebih proporsional
- ✅ **Progress bar** — Di bawah poster, warna mint
- ✅ **Episode badge** — Hijau di pojok kiri atas

### 6. Detail Screen
- ✅ **Backdrop lebih besar** — 420dp dengan multi-layer gradient
- ✅ **Tags di atas title** — Genre pills di atas judul
- ✅ **Action buttons** — Play (solid), Favorite, Share — semua dalam rounded rectangles
- ✅ **Resume card** — Card terpisah untuk melanjutkan menonton dengan progress bar
- ✅ **Episode grid** — Chip range selector lebih profesional dengan border

### 7. Search Screen
- ✅ **SearchDramaCard** — Redesign dengan badge rank, platform logo, episode count
- ✅ **Grid 3 kolom** — Lebih rapat, lebih banyak konten terlihat

### 8. Library Screen
- ✅ **Card-based rows** — Setiap item dibungkus card dengan background
- ✅ **Spacing lebih lega** — 8dp gap antar item
- ✅ **Tab switcher** — Rounded rectangle (12dp) bukan pill

### 9. Player Overlay
- ✅ **Chip design** — Background semi-transparan, rounded 6dp
- ✅ **Error card** — Dengan ikon warning di atas
- ✅ **Retry button** — Hijau mint dengan ikon refresh

### 10. Error & Loading States
- ✅ **Shimmer loader** — Lebih smooth dengan gradient 5-stop, skeleton lebih detail
- ✅ **Error card** — Ikon cloud-off, tombol retry hijau mint
- ✅ **Offline banner** — Background merah wash, tombol "Muat ulang" dengan background

---

## 🔤 Typography
- Tetap menggunakan **Fraunces** (display) + **Plus Jakarta Sans** (body)
- Font weight lebih konsisten: Bold untuk heading, SemiBold untuk subheading, Medium untuk body
- Letter spacing lebih ketat di heading untuk kesan premium

---

## 🎯 Prinsip Desain
1. **Dark cinematic** — Lebih gelap, kontras tinggi, fokus ke konten
2. **Minimal chrome** — Border lebih tipis, spacing lebih lega
3. **Consistent corners** — 12dp untuk card, 50dp untuk pill, 8dp untuk chip
4. **Brand consistency** — Mint hijau tetap jadi aksen utama
5. **Content-first** — Poster lebih besar, teks pendukung lebih kecil

---

## 📁 File yang Diubah
- `app/src/main/java/com/dramaku/app/MainActivity.kt` — UI composables (hanya visual)
- `app/src/main/java/com/dramaku/app/SplashActivity.java` — Warna splash screen
- `app/src/main/res/values/styles.xml` — Window background & bar colors

## 📁 File yang TIDAK Diubah (Business Logic)
- `DramakuRepository` — API calls, parsing, caching
- `LocalStore` — SharedPreferences, history, favorites
- `RemoteConfigRepository` — Remote config
- `ProgressKeys` — Storage keys
- `HomeCategory` — Category definitions
- `PlayerActivity` — Native player
- Semua data models
