# Dramaku Versioning

## Source of truth
- `version.properties` → Android `versionName` + `versionCode`
- `assets/js/core.js` → `APP_VERSION` (update checker UI)
- `remote-config.json` → `latestVersion` / `update.latestVersion` (remote prompt)

## Release flow
1. Bump `VERSION_NAME` and `VERSION_CODE` in `version.properties`
2. Set `APP_VERSION` in `assets/js/core.js` to the same name
3. Update `remote-config.json` changelog + latestVersion
4. Commit to `main` (CI builds artifact only)
5. Create and push tag: `git tag v4.3.0 && git push origin v4.3.0`
6. CI builds signed/debug APK and publishes GitHub Release

## Notes
- CI may raise `versionCode` to `github.run_number` if higher, so codes stay monotonic.
- Avoid creating a GitHub Release on every push to `main`.
