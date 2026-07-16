# Dramaku v4.7.1

## Stabilization

- Memisahkan progress playback berdasarkan platform, drama ID, dan episode.
- Mencegah history/progress tertukar ketika dua platform mempunyai drama ID yang sama.
- Menambahkan migrasi baca untuk progress yang dibuat sebelum v4.7.1.
- Menambahkan Gradle Wrapper 8.4 dengan checksum distribusi.
- Menggunakan Android Gradle Plugin 8.3.2 yang kompatibel dengan workflow Gradle 8.4.
- Menaikkan `compileSdk`/`targetSdk` ke API 35.
- Menambahkan unit test untuk key progress dan parser remote config.
- Mempertahankan workflow build APK yang sudah berjalan di `main`.

## Build dan validasi lokal

```bash
./gradlew testDebugUnitTest lintDebug assembleDebug
```
