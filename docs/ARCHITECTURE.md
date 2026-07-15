# Dramaku Frontend Architecture

Web UI is vanilla JS split into small files loaded in order from `index.html`.

## Load order
1. `hls.js` (CDN)
2. `assets/js/core.js` — state, utils, API cache, remote config, settings
3. `assets/js/search.js` — search UI + dedupe/rank
4. `assets/js/home.js` — home feed, platforms, cards
5. `assets/js/detail.js` — detail overlay
6. `assets/js/player.js` — TikTok-style player + stream loaders
7. `assets/js/library.js` — history, favorites, profile, clips
8. `assets/js/app.js` — boot, connectivity banner, native player result hook

## Rules
- Keep browser globals for now (no bundler required).
- Put shared helpers in `core.js`.
- Put feature code in the matching module.
- `app.js` should stay thin: startup only.
- Android packages these files via Gradle `copyIndexHtml` (`assets/**`).

## Native bridge
- `NativeApp.*` — fullscreen, toast, share, openUrl, version, cache
- `NativePlayer.playFull(...)` — ExoPlayer with progress return via `onNativePlayerResult`
