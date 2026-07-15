# Dramaku 4.3.1 — Quality Watch + Cleaner Search

## Perubahan

### Rating
- Hapus `ratingFor()` hash palsu (angka 8.x–9.x acak)
- Rating hanya tampil jika API menyediakan nilai asli (IMDb/score/rating)
- Card/detail/spotlight tidak memaksa bintang palsu

### Search
- Dedupe hasil:
  - sama platform + id
  - sama platform + judul mirip
  - judul mirip lintas platform (ambil yang metadata-nya lebih lengkap)
- Ranking relevansi: exact title > prefix > contains + bonus rating/thumb/episode
- Toast jika sebagian platform search gagal

### Native player sync (APK)
- `NativePlayer.playFull(url, subtitle, title, dramaId, episode, platform, startPosMs)`
- `PlayerActivity` mengembalikan position/duration/ended ke WebView
- `window.onNativePlayerResult(...)` menyimpan progress + history
- Resume start position dari progress tersimpan
- Auto-next episode (jika setting aktif) setelah native player selesai

## Build / rilis
- `version.properties` → 4.3.1 / code 38
- Tag rilis: `v4.3.1`
