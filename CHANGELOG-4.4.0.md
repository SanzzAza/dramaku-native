# Dramaku 4.4.0 — Modular JS + Privacy Polish

## Highlights
- Split monolit `app.js` menjadi 7 modul:
  - `core.js`, `search.js`, `home.js`, `detail.js`, `player.js`, `library.js`, `app.js`
- Setelan: **Privasi & Data** + **Disclaimer** (selain Tentang)
- Label platform picker dibersihkan (hapus angka marketing palsu)
- Docs arsitektur: `docs/ARCHITECTURE.md`

## Tetap ada dari 4.3.x
- API timeout + cache fallback + anti-stuck loading
- Rating hanya jika data asli
- Search dedupe + ranking
- Native ExoPlayer progress sync
- WebView harden + release-on-tag versioning

## Rilis
```bash
git tag v4.4.0
git push origin v4.4.0
```
