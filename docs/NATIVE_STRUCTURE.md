# Dramaku Native Structure

Repo native ini mulai dipisah dari pola satu-file MVP menuju struktur production.

## Struktur saat ini

```txt
app/src/main/java/com/dramaku/app/
  MainActivity.kt                 # UI Compose utama, repository MVP, storage lokal
  PlayerActivity.java             # fallback/simple ExoPlayer activity
  SplashActivity.java             # native splash
  data/RemoteConfigRepository.kt  # remote config native
  storage/ProgressKeys.kt          # platform-aware playback progress keys

app/src/test/java/com/dramaku/app/
  data/                            # remote config parser tests
  storage/                         # storage key isolation/migration tests
```

## Struktur target next refactor

```txt
app/src/main/java/com/dramaku/app/
  data/
    ApiClient.kt
    DramakuRepository.kt
    StreamResolver.kt
    RemoteConfigRepository.kt
    Models.kt
  storage/
    HistoryStore.kt
    FavoriteStore.kt
    SettingsStore.kt
  ui/
    home/
    search/
    detail/
    player/
    library/
    settings/
```

## Kenapa belum semua dipisah sekaligus?

Agar perubahan tetap aman dan build release tidak pecah mendadak. Refactor besar berikutnya sebaiknya dilakukan setelah fitur native parity stabil di user testing.

## Native remote config

Remote config sekarang dipanggil native dari:

```txt
https://raw.githubusercontent.com/SanzzAza/dramaku/main/remote-config.json
```

Dipakai untuk:

- announcement/banner
- status platform
- maintenance/disable platform
- latest version metadata
