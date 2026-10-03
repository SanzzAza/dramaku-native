# Dramaku Native Versioning

## Source of truth

- `version.properties` → Android `versionName` + `versionCode`

```properties
VERSION_NAME=4.7.1
VERSION_CODE=62
```

## Release flow

1. Bump `VERSION_NAME` and `VERSION_CODE` in `version.properties`.
2. Commit to `main`.
3. Push tag for release, for example:

```bash
git tag v4.7.1
git push origin v4.7.1
```

4. GitHub Actions membangun APK dan membuat GitHub Release. Jika signing secrets tersedia, artifact memakai signing key resmi; jika tidak, artifact diberi nama `release-debugkey` sebagai fallback pengujian.

## Notes

- CI may raise `versionCode` to `github.run_number` if higher, so codes stay monotonic.
- Avoid creating a GitHub Release on every push to `main`.
