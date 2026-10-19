# Status platform — smoke test 2026-10-19

Hasil `scripts/smoke_test_platforms.py` (alur: home → ambil id drama → resolve
stream episode 1 → probe URL video dengan Range request). Dijalankan dua kali
untuk memisahkan platform mati vs sekadar flaky. Catatan: probe dijalankan dari
server di luar Indonesia, jadi CDN yang geo-lock ke ID bisa salah terdeteksi 403.

## ✅ Terverifikasi sehat (stream 200/206)

melolo¹, netshort¹, **dramabox³**, freereels, stardusttv, cashdrama, dramabite,
flextv, happyshort², minishort, microdrama¹, moboreels², raptdrama, reelbuzz,
shorten², shortmax, shotshort, starshort, vibeshort, wetv

¹ awalnya salah vonis oleh script (ekstraksi id/URL), diverifikasi manual OK.
² lolos di salah satu run — proxy-nya flaky (lihat bawah).
³ dipulihkan 2026-10-19 dengan migrasi ke proxy v2 (`dramaboxv2/api/v1`):
  home/rank/book/play/search terverifikasi. PENTING: file CDN dramaboxdb
  (`.encrypt.mp4`) terenkripsi Aliyun private — durasi terbaca tapi frame tidak
  bisa didecode; playback WAJIB lewat gateway `dramabox.iniapi.web.id` yang
  men-decrypt on-the-fly (lebih lambat; kualitas dibatasi 720p). Buku prefix
  `41…` mati di upstream ("书籍找不到了") dan difilter dari daftar; hanya `42…`
  tampil.

## 🔴 Mati di sisi server (konsisten di semua run — bukan bug aplikasi)

| Platform | Gejala |
|---|---|
| dramanova | Semua endpoint 404 (`discover/feed`, `home/modules`, `drama/list`) |
| sodareels | Home `api/v1/dramas` 404 |
| sereal | `api/watch/{id}/{ep}` selalu HTTP 500 |
| shortsky | Episode dijawab `"Serial tidak ada"` (cuowuma 401) dari upstream |
| goodshort | `unlock` membalas objek error (message/path/region/status) |
| flickreels | Home `for-you` HTTP 400 |
| bonustv | Stream resolve OK tapi CDN `api.bonusdrama.com` 403 (mungkin geo-lock) |
| dramarush | Stream resolve OK tapi CDN `akamai-static.shorttv.live` 403 (mungkin geo-lock) |

## 🟡 Flaky (kadang 5xx, kadang normal — retry biasanya menolong)

dramapops (502/timeout), happyshort (502), shortbox (502), shorten (500),
minishort (502). `getJson` di aplikasi sudah retry 3× dengan backoff.

## Tidak tuntas diuji oleh script (perlu cek di aplikasi)

reelshort, anichin (embed), playlet, radreels, luminareels, dotdrama, idrama,
flareflow, storyreel, meloshort, shortwave, snackshort — ekstraksi generik
script tidak menemukan id/chapter; alur di aplikasi memakai parser khusus yang
lebih akurat, jadi sebagian besar kemungkinan normal.

## Perbaikan sisi klien yang menyertai laporan ini (4.9.15)

1. Bearer dramahub & Referer cdreader tidak lagi dikirim ke semua CDN —
   hanya ke host yang memerlukannya (dramahub.be, *.cdreader.com). URL
   presigned S3/GCS menolak Authorization asing, dan probe `streamIsReachable`
   kini memakai header yang sama dengan pemutar sehingga link sehat tidak
   divonis kedaluwarsa.
2. Pesan error 5xx/404 dari proxy kini menjelaskan bahwa gangguan ada di
   server sumber, bukan aplikasi.

Jalankan ulang kapan saja: `python3 scripts/smoke_test_platforms.py`
(atau `--platform <id> <id>...` untuk subset).
