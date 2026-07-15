# Dramaku Native Versioning

## Source of truth

- `version.properties` → Android `versionName` + `versionCode`

```properties
VERSION_NAME=4.5.4
VERSION_CODE=45
```

## Release flow

1. Bump `VERSION_NAME` and `VERSION_CODE` in `version.properties`.
2. Commit to `main`.
3. Push tag for release, for example:

```bash
git tag v4.5.4
git push origin v4.5.4
```

4. GitHub Actions builds signed/debug APK and publishes GitHub Release.

## Notes

- CI may raise `versionCode` to `github.run_number` if higher, so codes stay monotonic.
- Avoid creating a GitHub Release on every push to `main`.
