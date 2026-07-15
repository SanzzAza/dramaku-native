# Dramaku Remote Config

Remote config lets Dramaku change safe runtime settings without shipping a new APK.

The app loads this URL by default:

```txt
https://raw.githubusercontent.com/SanzzAza/dramaku/main/remote-config.json
```

If loading fails, the app uses the last cached config. If no cached config exists, it uses the hardcoded defaults inside `index.html`.

## What remote config can do

- Override API endpoint URLs.
- Disable a broken platform temporarily.
- Show a home banner announcement.
- Disable/enable safe feature flags.
- Suggest a minimum APK version.

## What remote config must NOT contain

Remote config is public. Never put secrets here:

- GitHub tokens
- API keys
- passwords
- keystore data
- private server credentials

## Example: disable a platform

```json
{
  "platforms": {
    "moviebox": {
      "enabled": false,
      "reason": "MovieBox sedang maintenance sementara"
    }
  }
}
```

The platform will appear disabled in the platform selector and cannot be opened.

## Example: change endpoint without APK update

```json
{
  "api": {
    "dramabox": "https://new-api.example.com/dramabox"
  }
}
```

After users reopen the app or refresh remote config in Settings, the new endpoint is used.

## Example: show announcement

```json
{
  "message": {
    "enabled": true,
    "type": "warning",
    "title": "Maintenance",
    "text": "Beberapa platform mungkin lambat hari ini."
  }
}
```

`type` can be `info` or `warning`.

## Example: minimum app version

```json
{
  "minAppVersion": "3.5"
}
```

If the installed app is older, Dramaku shows an update notice.

## Settings page

Inside the APK/app:

```txt
Setelan > Remote Config > Refresh remote config
```

This manually fetches the latest JSON.


## Support contact / report episode

Remote config can route the **Lapor** button to WhatsApp, Telegram, or email.

```json
{
  "support": {
    "whatsapp": "6281234567890",
    "telegram": "username",
    "email": "admin@example.com"
  }
}
```

Priority: WhatsApp > Telegram > email > native share/clipboard fallback.

## Update checker

```json
{
  "latestVersion": "3.7",
  "downloadUrl": "https://github.com/SanzzAza/dramaku/releases/latest",
  "changelog": [
    "Update checker",
    "Status platform",
    "Report episode ke support"
  ]
}
```

If `latestVersion` is newer than `APP_VERSION`, the app shows an update prompt.
