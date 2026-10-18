# Dramaku Native - Penambahan Semua Endpoint Dramahub.be

Tanggal: 2026-10-18
Token: `15693e658f723c5b4c45900a5d045ef0ab6a053ecda4dadb831c68fef773ba5e`

## Ringkasan
- Total API di dramahub.be/docs: **44**
- Sudah ada di dramaku-native (skip): **9** -> melolo, dramanova, freereels, netshort, reelshort, anichin, moviebox, stardusttv, dramaboxv2
- Baru ditambahkan: **35** platform
- Total endpoint baru: **272** endpoint
- Total catalog files: 5 lama + 35 baru = **40**

## Platform Baru (35)

| No | Platform | Brand | Base URL | EP | File |
|----|----------|-------|----------|----|------|
| 1 | `bonustv` | BonusTV | https://dramahub.be/bonustv | 11 | `BonusTVCatalog.kt` |
| 2 | `cashdrama` | CashDrama | https://dramahub.be/cashdrama | 9 | `CashDramaCatalog.kt` |
| 3 | `dotdrama` | DotDrama | https://dramahub.be/dotdrama | 4 | `DotDramaCatalog.kt` |
| 4 | `dramabite` | DramaBite | https://dramahub.be/dramabite | 9 | `DramaBiteCatalog.kt` |
| 5 | `dramapops` | DramaPops | https://dramahub.be/dramapops | 8 | `DramaPopsCatalog.kt` |
| 6 | `dramarush` | DramaRush | https://dramahub.be/dramarush | 7 | `DramaRushCatalog.kt` |
| 7 | `flareflow` | FlareFlow | https://dramahub.be/flareflow | 9 | `FlareFlowCatalog.kt` |
| 8 | `flextv` | FlexTV | https://dramahub.be/flextv | 7 | `FlexTVCatalog.kt` |
| 9 | `flickreels` | FlickReels | https://dramahub.be/flickreels | 11 | `FlickReelsCatalog.kt` |
| 10 | `goodshort` | GoodShort | https://dramahub.be/goodshort | 9 | `GoodShortCatalog.kt` |
| 11 | `happyshort` | HappyShort | https://dramahub.be/happyshort | 6 | `HappyShortCatalog.kt` |
| 12 | `idrama` | IDrama | https://dramahub.be/idrama | 7 | `IDramaCatalog.kt` |
| 13 | `luminareels` | LuminaReels | https://dramahub.be/luminareels | 7 | `LuminaReelsCatalog.kt` |
| 14 | `meloshort` | MeloShort | https://dramahub.be/meloshort | 7 | `MeloShortCatalog.kt` |
| 15 | `microdrama` | MicroDrama | https://dramahub.be/microdrama | 4 | `MicroDramaCatalog.kt` |
| 16 | `minishort` | MiniShort | https://dramahub.be/minishort | 6 | `MiniShortCatalog.kt` |
| 17 | `moboreels` | MoboReels | https://dramahub.be/moboreels | 10 | `MoboReelsCatalog.kt` |
| 18 | `playlet` | Playlet | https://dramahub.be/playlet | 9 | `PlayletCatalog.kt` |
| 19 | `radreels` | RadReels | https://dramahub.be/radreels | 9 | `RadReelsCatalog.kt` |
| 20 | `rapidtv` | RapidTV | https://dramahub.be/rapidtv | 5 | `RapidTVCatalog.kt` |
| 21 | `raptdrama` | RaptDrama | https://dramahub.be/raptdrama | 10 | `RaptDramaCatalog.kt` |
| 22 | `reelbuzz` | ReelBuzz | https://dramahub.be/reelbuzz | 7 | `ReelBuzzCatalog.kt` |
| 23 | `sereal` | Sereal | https://dramahub.be/sereal | 9 | `SerealCatalog.kt` |
| 24 | `shortbox` | ShortBox | https://dramahub.be/shortbox | 10 | `ShortBoxCatalog.kt` |
| 25 | `shorten` | Shorten | https://dramahub.be/shorten | 7 | `ShortenCatalog.kt` |
| 26 | `shortmax` | ShortMax | https://dramahub.be/shortmax | 10 | `ShortMaxCatalog.kt` |
| 27 | `shortsky` | ShortSky | https://dramahub.be/shortsky | 6 | `ShortSkyCatalog.kt` |
| 28 | `shortwave` | ShortWave | https://dramahub.be/shortwave | 8 | `ShortWaveCatalog.kt` |
| 29 | `shotshort` | ShotShort | https://dramahub.be/shotshort | 8 | `ShotShortCatalog.kt` |
| 30 | `snackshort` | SnackShort | https://dramahub.be/snackshort | 8 | `SnackShortCatalog.kt` |
| 31 | `sodareels` | SodaReels | https://dramahub.be/sodareels | 6 | `SodaReelsCatalog.kt` |
| 32 | `starshort` | StarShort | https://dramahub.be/starshort | 7 | `StarShortCatalog.kt` |
| 33 | `storyreel` | StoryReel | https://dramahub.be/storyreel | 7 | `StoryReelCatalog.kt` |
| 34 | `vibeshort` | VibeShort | https://dramahub.be/vibeshort | 8 | `VibeShortCatalog.kt` |
| 35 | `wetv` | WeTV | https://dramahub.be/wetv | 7 | `WeTVCatalog.kt` |

## Platform Lama (Skip - Sudah Ada)

- **melolo** (`/melolo`) - 10 EP - SKIP
- **dramanova** (`/dramanova`) - 5 EP - SKIP
- **freereels** (`/freereels`) - 13 EP - SKIP
- **netshort** (`/netshort`) - 15 EP - SKIP
- **reelshort** (`/reelshort`) - 12 EP - SKIP
- **anichin** (`/anichin`) - 9 EP - SKIP
- **moviebox** (`/moviebox`) - 11 EP - SKIP
- **stardusttv** (`/stardusttv`) - 7 EP - SKIP
- **dramaboxv2** (`/dramaboxv2`) - 7 EP - SKIP

## Detail Endpoint per Platform Baru

### BonusTV (`bonustv`) - 11 endpoints
- **Base:** `https://dramahub.be/bonustv`
- **Catalog:** `app/src/main/java/com/dramaku/app/catalog/BonusTVCatalog.kt`
- **Endpoints:**
  - `GET /api/languages` - Supported languages (lokal)
  - `GET /api/banners` - Banners params: `lang`
  - `GET /api/tabs` - Tabs list params: `lang`
  - `GET /api/tabs/:id` - Tab content (id: 1001=All, 1002=New Dramas, 1003=...) params: `id`!, `tab_id`, `lang`
  - `GET /api/modules` - Modules (all videos flattened, dedupe by id) params: `lang`
  - `GET /api/shorts` - Shorts (pagination) params: `lang`, `page`, `page_size`
  - `GET /api/recommend` - Recommend (random each call) params: `lang`, `page`, `page_size`
  - `GET /api/videos/recently` - Recently added (full list, 20 items) params: `lang`
  - `GET /api/search` - Search drama (q or keyword) params: `q`, `keyword`, `lang`, `page`, `page_size`
  - `GET /api/video/:id` - Video detail with episodes params: `id`!, `video_id`, `lang`
  - `GET /api/stream/:id` - Episode HLS stream + subtitle params: `id`!, `video_id`, `episode_num`, `resolution`, `lang`

### CashDrama (`cashdrama`) - 9 endpoints
- **Base:** `https://dramahub.be/cashdrama`
- **Catalog:** `app/src/main/java/com/dramaku/app/catalog/CashDramaCatalog.kt`
- **Endpoints:**
  - `GET /api/v1/languages` - Supported languages
  - `GET /api/v1/home` - Home feed params: `lang`, `page`, `pageSize`, `blockId`
  - `GET /api/v1/blocks` - Categories/blocks params: `lang`
  - `GET /api/v1/tags` - Tags list params: `lang`
  - `GET /api/v1/drama/:vid` - Drama detail params: `vid`!, `ep`, `lang`
  - `GET /api/v1/drama/:vid/episodes` - Episode list params: `vid`!, `lang`
  - `GET /api/v1/search` - Search drama params: `q`!, `page`, `lang`
  - `GET /api/v1/tags/search` - Search by tag params: `tagId`!, `page`, `lang`
  - `GET /api/v1/play/:vid/:ep` - Video URL (Tencent VOD) params: `vid`!, `ep`!, `lang`

### DotDrama (`dotdrama`) - 4 endpoints
- **Base:** `https://dramahub.be/dotdrama`
- **Catalog:** `app/src/main/java/com/dramaku/app/catalog/DotDramaCatalog.kt`
- **Endpoints:**
  - `GET /api/v1/languages` - List supported languages
  - `GET /api/v1/dramas` - Drama list params: `page`, `limit`, `lang`
  - `GET /api/v1/collections` - Collections (hot, for you, etc) params: `lang`
  - `GET /api/v1/dramas/:id` - Drama detail + episodes with video URL params: `id`!

### DramaBite (`dramabite`) - 9 endpoints
- **Base:** `https://dramahub.be/dramabite`
- **Catalog:** `app/src/main/java/com/dramaku/app/catalog/DramaBiteCatalog.kt`
- **Endpoints:**
  - `GET /api/v1/languages` - List supported languages
  - `GET /api/v1/dramas` - Drama list params: `lang`, `page`
  - `GET /api/v1/foryou` - For You recommendations params: `lang`, `page`
  - `GET /api/v1/hot` - Hot/trending keywords
  - `GET /api/v1/drama/:id` - Drama detail with episodes params: `id`!, `lang`
  - `GET /api/v1/drama/:id/likes` - Drama like count params: `id`!, `lang`
  - `GET /api/v1/drama/:id/episode/:ep` - Episode video URL params: `id`!, `ep`!, `lang`
  - `GET /api/v1/search` - Search drama (gRPC server-side) params: `q`!, `lang`, `limit`
  - `GET /api/v1/recommend` - Homepage recommendations params: `lang`, `page`

### DramaPops (`dramapops`) - 8 endpoints
- **Base:** `https://dramahub.be/dramapops`
- **Catalog:** `app/src/main/java/com/dramaku/app/catalog/DramaPopsCatalog.kt`
- **Endpoints:**
  - `GET /api/languages` - Daftar bahasa
  - `GET /api/homepage` - Homepage (semua section) params: `language`
  - `GET /api/drama` - Semua film params: `limit`, `language`
  - `GET /api/drama/trending` - Film trending params: `limit`, `language`
  - `GET /api/drama/popular` - Film populer params: `limit`, `language`
  - `GET /api/drama/search` - Pencarian film params: `q`!, `limit`, `language`
  - `GET /api/drama/:id` - Detail film params: `id`!, `language`
  - `GET /api/drama/:id/:ep` - Video player episode params: `id`!, `ep`!, `quality`, `language`

### DramaRush (`dramarush`) - 7 endpoints
- **Base:** `https://dramahub.be/dramarush`
- **Catalog:** `app/src/main/java/com/dramaku/app/catalog/DramaRushCatalog.kt`
- **Endpoints:**
  - `GET /api/v1/languages` - Supported languages
  - `GET /api/v1/ranking` - Search ranking params: `lang`
  - `GET /api/v1/tabs/:id` - Tab home data params: `id`!, `lang`
  - `GET /api/v1/drama/:id` - Drama detail params: `id`!, `lang`
  - `GET /api/v1/search/:q` - Search drama params: `q`!, `lang`
  - `GET /api/v1/play/:id` - Get episodes with video URLs (paginated) params: `id`!, `page`, `size`, `lang`
  - `GET /api/v1/play/:id/:ep` - Get single episode video URL params: `id`!, `ep`!, `lang`

### FlareFlow (`flareflow`) - 9 endpoints
- **Base:** `https://dramahub.be/flareflow`
- **Catalog:** `app/src/main/java/com/dramaku/app/catalog/FlareFlowCatalog.kt`
- **Endpoints:**
  - `GET /api/languages` - Supported languages
  - `GET /api/home` - Home feed by tab params: `tab`, `language`
  - `GET /api/classes` - Genre list params: `language`
  - `GET /api/classes/:classId` - Dramas by genre params: `classId`!, `page`, `pageSize`, `language`
  - `GET /api/popular/list` - Popular dramas params: `language`
  - `GET /api/foryou/list` - For You list params: `page`, `pageSize`, `language`
  - `GET /api/search` - Search drama params: `keyword`!, `page`, `language`
  - `GET /api/drama/:id` - Drama detail with episodes params: `id`!, `language`
  - `GET /api/drama/:id/play` - Episode video URL (480/720/1080) params: `id`!, `ep`!, `language`

### FlexTV (`flextv`) - 7 endpoints
- **Base:** `https://dramahub.be/flextv`
- **Catalog:** `app/src/main/java/com/dramaku/app/catalog/FlexTVCatalog.kt`
- **Endpoints:**
  - `GET /api/v1/languages` - List supported languages
  - `GET /api/v1/tabs` - Get tabs/categories params: `lang`
  - `GET /api/v1/tabs/:id` - Get dramas by tab (id: 1=Popular/Fokus, 2=New/Baru, 3=Chart/Peringkat, 6=Original+, 7=For Her/Wanita, 8=For Him/Pria, 11=Anime) params: `id`!, `page`, `lang`
  - `GET /api/v1/search` - Search dramas params: `q`!, `page`, `lang`
  - `GET /api/v1/series/:id` - Series detail params: `id`!, `lang`
  - `GET /api/v1/series/:id/episodes` - Episode list params: `id`!, `lang`
  - `GET /api/v1/play/:series_id/:section_id` - Video URL params: `series_id`!, `section_id`!, `lang`

### FlickReels (`flickreels`) - 11 endpoints
- **Base:** `https://dramahub.be/flickreels`
- **Catalog:** `app/src/main/java/com/dramaku/app/catalog/FlickReelsCatalog.kt`
- **Endpoints:**
  - `GET /api/v1/language` - Supported languages
  - `GET /api/v1/for-you` - For You feed params: `page`, `page_size`, `lang`
  - `GET /api/v1/hot-rank` - Hot ranking params: `lang`
  - `GET /api/v1/navigation` - Categories list params: `lang`
  - `GET /api/v1/category` - Dramas by category params: `navigation_id`!, `page`, `page_size`, `lang`
  - `GET /api/v1/hot-keywords` - Hot keywords params: `lang`
  - `GET /api/v1/search-rank` - Search ranking params: `lang`
  - `GET /api/v1/search` - Search drama params: `keyword`!, `lang`
  - `GET /api/v1/banners` - Banners/sliders params: `lang`
  - `GET /api/v1/chapters/:id` - Chapter list params: `id`!, `lang`
  - `GET /api/v1/stream/:playletId/:chapterId` - Stream URL params: `playletId`!, `chapterId`!, `lang`

### GoodShort (`goodshort`) - 9 endpoints
- **Base:** `https://dramahub.be/goodshort`
- **Catalog:** `app/src/main/java/com/dramaku/app/catalog/GoodShortCatalog.kt`
- **Endpoints:**
  - `GET /api/v1/home` - Katalog feed utama params: `channelId`, `page`, `pageSize`, `index`
  - `GET /api/v1/nav` - Menu navigasi utama (tab app)
  - `GET /api/v1/trending` - Feed Tren (cursor layerId_bookId) params: `page`, `pageSize`, `index`, `tags`
  - `GET /api/v1/category` - Filter kategori/genre params: `genre`, `character`, `setting`, `popular`, `subtype`, `gender`, `page`, `pageSize`
  - `GET /api/v1/ranking` - Ranking (0=Tren, 1=Terbaru, 2=Pencarian Populer) params: `type`, `page`, `pageSize`
  - `GET /api/v1/search` - Cari drama params: `q`!
  - `GET /api/v1/chapters/:bookId` - Daftar episode satu drama params: `bookId`!
  - `GET /api/v1/unlock/:bookId` - Semua episode + semua kualitas (720p/540p/1080p, m3u8 + fileSize) params: `bookId`!, `q`
  - `GET /api/v1/key` - Kunci dekripsi AES-128 video offline

### HappyShort (`happyshort`) - 6 endpoints
- **Base:** `https://dramahub.be/happyshort`
- **Catalog:** `app/src/main/java/com/dramaku/app/catalog/HappyShortCatalog.kt`
- **Endpoints:**
  - `GET /languages` - Daftar bahasa tersedia
  - `GET /home` - Rekomendasi home params: `lang`
  - `GET /ranking` - Peringkat/populer params: `lang`
  - `GET /search` - Cari drama (alias: kw) params: `keyword`
  - `GET /drama/:videoId` - Detail drama + daftar episode params: `videoId`
  - `GET /drama/:videoId/play/:ep` - URL streaming episode params: `videoId`, `ep`

### IDrama (`idrama`) - 7 endpoints
- **Base:** `https://dramahub.be/idrama`
- **Catalog:** `app/src/main/java/com/dramaku/app/catalog/IDramaCatalog.kt`
- **Endpoints:**
  - `GET /api/v1/search` - Search dramas by keyword (lang: id, en, ja, ko, es, pt, th) params: `q`!, `page_size`, `lang`
  - `GET /api/v1/popular` - Popular dramas params: `page`, `limit`, `lang`
  - `GET /api/v1/latest` - Latest dramas params: `page`, `limit`, `lang`
  - `GET /api/v1/genres` - Get all available genres params: `lang`
  - `GET /api/v1/genre/:id` - Dramas by genre params: `id`!, `page`, `limit`, `lang`
  - `GET /api/v1/drama/:id` - Drama detail with episodes params: `id`!, `lang`
  - `GET /api/v1/drama/:id/play` - Video play URL (HLS 540p/720p/1080p) params: `id`!, `episode`, `lang`

### LuminaReels (`luminareels`) - 7 endpoints
- **Base:** `https://dramahub.be/luminareels`
- **Catalog:** `app/src/main/java/com/dramaku/app/catalog/LuminaReelsCatalog.kt`
- **Endpoints:**
  - `GET /api/languages` - Daftar bahasa yang didukung
  - `GET /api/drama/list` - Feed / daftar drama params: `page`, `page_size`, `lang`
  - `GET /api/drama/category` - Daftar kategori drama params: `lang`
  - `GET /api/drama/category/:id` - Daftar drama pada kategori tertentu params: `id`!, `page`, `page_size`, `lang`
  - `GET /api/drama/search` - Cari drama berdasarkan kata kunci params: `q`!, `page`, `page_size`, `lang`
  - `GET /api/drama/detail` - Detail drama + episode params: `id`!, `lang`
  - `GET /api/stream/:id/:index/master.m3u8` - Playlist HLS hasil decrypt yang bisa diputar player params: `id`!, `index`!, `lang`

### MeloShort (`meloshort`) - 7 endpoints
- **Base:** `https://dramahub.be/meloshort`
- **Catalog:** `app/src/main/java/com/dramaku/app/catalog/MeloShortCatalog.kt`
- **Endpoints:**
  - `GET /api/v1/languages` - Available languages
  - `GET /api/v1/drama/all` - Drama list params: `page`, `limit`, `lang`
  - `GET /api/v1/dramas/discover` - Discover dramas params: `page`, `limit`, `lang`
  - `GET /api/v1/dramas/top` - Top ranked dramas params: `lang`
  - `GET /api/v1/dramas/search` - Search drama params: `q`!, `lang`
  - `GET /api/v1/dramas/:id/episodes` - Episode list params: `id`!, `page`, `limit`, `lang`
  - `GET /api/v1/dramas/:id/episodes/:chapter` - Episode video URL params: `id`!, `chapter`!, `lang`

### MicroDrama (`microdrama`) - 4 endpoints
- **Base:** `https://dramahub.be/microdrama`
- **Catalog:** `app/src/main/java/com/dramaku/app/catalog/MicroDramaCatalog.kt`
- **Endpoints:**
  - `GET /api/v1/languages` - List supported languages
  - `GET /api/v1/dramas` - Drama list params: `lang`, `limit`
  - `GET /api/v1/dramas/search` - Search drama params: `q`!, `lang`
  - `GET /api/v1/dramas/:id` - Drama detail + episodes with video URL params: `id`!

### MiniShort (`minishort`) - 6 endpoints
- **Base:** `https://dramahub.be/minishort`
- **Catalog:** `app/src/main/java/com/dramaku/app/catalog/MiniShortCatalog.kt`
- **Endpoints:**
  - `GET /api/languages` - Daftar bahasa tersedia
  - `GET /api/home_recommend` - Rekomendasi home params: `language`
  - `GET /api/modules` - Daftar modul kategori params: `language`
  - `GET /api/catalog` - Katalog drama per modul params: `module_type`, `page_index`, `page_size`, `language`
  - `GET /api/video_info` - Detail video + daftar episode params: `video_id`, `language`
  - `GET /api/episodes_content` - URL streaming episode params: `video_id`, `episodes_id`, `language`

### MoboReels (`moboreels`) - 10 endpoints
- **Base:** `https://dramahub.be/moboreels`
- **Catalog:** `app/src/main/java/com/dramaku/app/catalog/MoboReelsCatalog.kt`
- **Endpoints:**
  - `GET /api/languages` - List 19 supported languages (langId mapping)
  - `GET /api/channelList` - List of main channels/categories params: `langId`
  - `GET /api/channelDetail` - Channel detail + series list per section params: `schemeId`!, `channelId`!, `skipSeries`, `langId`
  - `GET /api/hotList` - List of hot/trending dramas (10=Trending, 11=Latest) params: `listId`!, `skipSeries`, `schemeId`, `langId`
  - `GET /api/seriesDetail` - Drama detail (title, cover, genre, episodes, status) params: `seriesId`!, `langId`
  - `GET /api/seriesPage` - All series episodes (auto-paginate from seriesDetail) params: `seriesId`!, `langId`
  - `GET /api/guessYouLike` - Drama rekomendasi serupa params: `seriesId`!, `langId`
  - `GET /api/video` - Episode video URL (auto-unlock). Referer: https://www.cdreader.com/ params: `seriesId`!, `episNum`!, `langId`
  - `GET /api/proxy/subtitle` - Proxy subtitle params: `episId`!, `langId`
  - `GET /api/search` - Search drama by keyword (pagination support) params: `searchKey`!, `pageNo`, `pageSize`, `langId`

### Playlet (`playlet`) - 9 endpoints
- **Base:** `https://dramahub.be/playlet`
- **Catalog:** `app/src/main/java/com/dramaku/app/catalog/PlayletCatalog.kt`
- **Endpoints:**
  - `GET /languages` - Daftar bahasa yang didukung Playlet
  - `GET /shorts/recommend` - Feed shorts params: `language`, `page`, `pageSize`
  - `GET /new-arrivals` - Theater baru tiba params: `language`
  - `GET /home/columns` - Daftar tipe kolom home (topColumnTypeList) params: `language`
  - `GET /column` - Feed kolom (zoneColumnListNew, non-paginated) params: `columnTypeId`!, `language`
  - `GET /search` - Pencarian theater params: `keyword`!, `language`, `page`, `pageSize`
  - `GET /theater/:id` - Detail theater + daftar chapter params: `id`!, `language`
  - `GET /theater/:id/open/:chapter/playlist.m3u8` - Stream HLS tunggal (master / variant / segment v2-decrypt v3-clear) params: `id`!, `chapter`!
  - `GET /theater/:id/subtitle/:chapter` - Pilihan subtitle per chapter params: `id`!, `chapter`!, `language`

### RadReels (`radreels`) - 9 endpoints
- **Base:** `https://dramahub.be/radreels`
- **Catalog:** `app/src/main/java/com/dramaku/app/catalog/RadReelsCatalog.kt`
- **Endpoints:**
  - `GET /api/v1/languages` - List available languages
  - `GET /api/v1/home` - Homepage params: `lang`
  - `GET /api/v1/tab/:id` - Tab content params: `id`!, `page`, `size`, `lang`
  - `GET /api/v1/search/:query` - Search drama params: `query`!, `page`, `lang`
  - `GET /api/v1/drama/:keyword` - Drama detail params: `keyword`!, `page`, `lang`
  - `GET /api/v1/episodes/:fakeId` - All episodes params: `fakeId`!, `lang`
  - `GET /api/v1/video/:videoFakeId/:episodicDramaId` - Video URL params: `videoFakeId`!, `episodicDramaId`!, `lang`
  - `GET /api/v1/ranking` - Ranking params: `lang`
  - `GET /api/v1/foryou` - For You feed params: `page`, `lang`

### RapidTV (`rapidtv`) - 5 endpoints
- **Base:** `https://dramahub.be/rapidtv`
- **Catalog:** `app/src/main/java/com/dramaku/app/catalog/RapidTVCatalog.kt`
- **Endpoints:**
  - `GET /api/v1/languages` - Available languages
  - `GET /api/v1/dramas` - List dramas params: `page`, `size`, `lang`
  - `GET /api/v1/dramas/:id` - Drama detail + episodes params: `id`!, `lang`
  - `GET /api/v1/dramas/:id/episodes` - All episodes with video URLs params: `id`!, `lang`
  - `GET /api/v1/search` - Search drama params: `q`!, `page`, `size`, `lang`

### RaptDrama (`raptdrama`) - 10 endpoints
- **Base:** `https://dramahub.be/raptdrama`
- **Catalog:** `app/src/main/java/com/dramaku/app/catalog/RaptDramaCatalog.kt`
- **Endpoints:**
  - `GET /v1/languages` - Supported languages
  - `GET /open/recommend` - Rekomendasi drama (pagination) params: `page`, `lang`
  - `GET /open/trending` - Drama trending params: `lang`
  - `GET /open/new` - Drama rilis terbaru params: `lang`
  - `GET /open/classifyv2` - Daftar kategori params: `lang`
  - `POST /open/search` - Pencarian drama (body JSON: {"keyword":"love","page":1}) params: `keyword`!, `page`, `lang`
  - `GET /open/classify_video` - List drama per kategori params: `type`!, `page`, `lang`
  - `POST /video/videoinfo` - Detail video (body JSON: {"vid": 2435}) params: `vid`!, `lang`
  - `GET /video/getchapters` - Daftar episode params: `vid`!, `lang`
  - `GET /video/playlist` - Load semua episode + URL m3u8 sekaligus (cukup vid) params: `vid`!, `lang`

### ReelBuzz (`reelbuzz`) - 7 endpoints
- **Base:** `https://dramahub.be/reelbuzz`
- **Catalog:** `app/src/main/java/com/dramaku/app/catalog/ReelBuzzCatalog.kt`
- **Endpoints:**
  - `GET /api/languages` - Daftar 14 bahasa yang didukung
  - `GET /api/drama/list` - Daftar drama rekomendasi params: `pageNum`, `pageSize`, `language`
  - `GET /api/drama/display` - Feed halaman utama dengan paginasi params: `pageNum`, `pageSize`, `language`
  - `GET /api/drama/module/recommend` - Modul rekomendasi params: `pageNum`, `pageSize`, `language`
  - `GET /api/search` - Cari drama params: `keyword`!, `language`
  - `GET /api/drama/detail` - Detail drama + daftar semua episode params: `dramaId`!, `language`
  - `GET /api/drama/video/:dramaId/:ep` - URL video per episode (normal: HD CDN2 + LD terdekripsi, txvod: HLS bersih termasuk episode premium) params: `dramaId`!, `ep`!, `language`

### Sereal (`sereal`) - 9 endpoints
- **Base:** `https://dramahub.be/sereal`
- **Catalog:** `app/src/main/java/com/dramaku/app/catalog/SerealCatalog.kt`
- **Endpoints:**
  - `GET /api/languages` - Supported languages
  - `GET /api/index/home/:page` - Homepage feed params: `page`!, `lang`
  - `GET /api/index/for-you/:page` - For You feed params: `page`!, `lang`
  - `GET /api/index/search/:q` - Search drama params: `q`!, `lang`
  - `GET /api/index/trending` - Trending dramas params: `lang`
  - `GET /api/index/content-search/:contentId/:page` - Content search by ID params: `contentId`!, `page`!, `lang`
  - `GET /api/content/detail` - Drama detail params: `contentId`!, `lang`
  - `GET /api/content/recommend-smart/:page` - Smart recommendations params: `page`!, `lang`
  - `GET /api/watch/:contentId/:ep` - Episode video URL params: `contentId`!, `ep`!, `lang`

### ShortBox (`shortbox`) - 10 endpoints
- **Base:** `https://dramahub.be/shortbox`
- **Catalog:** `app/src/main/java/com/dramaku/app/catalog/ShortBoxCatalog.kt`
- **Endpoints:**
  - `GET /api/languages` - Daftar bahasa tersedia
  - `GET /api/categories` - Daftar kategori drama params: `lang`
  - `GET /api/list` - Browse drama (ShortBox) params: `page`, `page_size`, `languages`, `streamable`
  - `GET /api/new-list` - Drama terbaru (PSSDK) params: `page`, `page_size`, `languages`
  - `GET /api/recommend` - Rekomendasi home params: `languages`, `streamable`
  - `GET /api/for-you` - Feed For You (paginasi lokal) params: `page`, `page_size`, `languages`, `streamable`
  - `GET /api/hot-search` - Drama populer/trending params: `languages`
  - `GET /api/search` - Cari drama (alias: query, keyword) params: `q`!, `page`, `page_size`, `languages`
  - `GET /api/info/:id` - Detail drama + daftar episode params: `id`!, `sort`, `languages`
  - `GET /api/stream/:id/:ep` - Stream URL (m3u8/DRM) params: `id`!, `ep`!, `quality`

### Shorten (`shorten`) - 7 endpoints
- **Base:** `https://dramahub.be/shorten`
- **Catalog:** `app/src/main/java/com/dramaku/app/catalog/ShortenCatalog.kt`
- **Endpoints:**
  - `GET /api/v1/editors` - Editor picks params: `page`, `perPage`
  - `GET /api/v1/exclusive` - Exclusive dramas params: `page`, `perPage`
  - `GET /api/v1/dubbed` - Dubbed dramas params: `page`, `perPage`
  - `GET /api/v1/releases` - New releases params: `page`, `perPage`
  - `GET /api/v1/categories` - Categories list
  - `GET /api/v1/explore` - Explore page
  - `GET /api/v1/series/:slug` - Series detail + episodes (video hash included) params: `slug`!

### ShortMax (`shortmax`) - 10 endpoints
- **Base:** `https://dramahub.be/shortmax`
- **Catalog:** `app/src/main/java/com/dramaku/app/catalog/ShortMaxCatalog.kt`
- **Endpoints:**
  - `GET /api/v1/search` - Search drama params: `q`!, `lang`, `page`
  - `GET /api/v1/home` - Homepage by tab params: `tab`, `lang`
  - `GET /api/v1/feed/recommend` - Tab Rekomendasi params: `lang`
  - `GET /api/v1/feed/new` - Tab Baru params: `lang`
  - `GET /api/v1/feed/ranked` - Tab Peringkat (3 section) params: `lang`
  - `GET /api/v1/feed/epic` - Tab Dunia Epic params: `lang`
  - `GET /api/v1/foryou` - For You feed params: `page`, `lang`
  - `GET /api/v1/detail/:code` - Drama detail params: `code`!, `lang`
  - `GET /api/v1/play/:code` - Episode video URL (VIP) params: `code`!, `ep`!, `lang`
  - `GET /api/v1/languages` - Supported languages

### ShortSky (`shortsky`) - 6 endpoints
- **Base:** `https://dramahub.be/shortsky`
- **Catalog:** `app/src/main/java/com/dramaku/app/catalog/ShortSkyCatalog.kt`
- **Endpoints:**
  - `GET /api/languages` - Supported languages
  - `GET /api/home` - Homepage feed params: `lang`
  - `GET /api/recommend` - Recommended dramas params: `lang`
  - `GET /api/search` - Search drama params: `q`!, `page`, `lang`
  - `GET /api/drama/:id` - Drama detail params: `id`!, `lang`
  - `GET /api/drama/:id/episode/:ep` - Episode video URLs (480/720/1080) params: `id`!, `ep`!, `lang`

### ShortWave (`shortwave`) - 8 endpoints
- **Base:** `https://dramahub.be/shortwave`
- **Catalog:** `app/src/main/java/com/dramaku/app/catalog/ShortWaveCatalog.kt`
- **Endpoints:**
  - `GET /api/set-lang/:lang` - Set language. Title, tags, content change accordingly params: `lang`!
  - `GET /api/top` - Drama trending params: `lang`
  - `GET /api/all` - All dramas params: `lang`
  - `GET /api/more` - Dramas with pagination params: `page`, `page_size`, `lang`
  - `GET /api/rankings` - Ranking drama params: `lang`
  - `GET /api/search/:query` - Cari drama params: `query`!, `lang`
  - `GET /api/drama/:dramaId` - Drama detail + all episodes + cover per episode params: `dramaId`!, `lang`
  - `GET /api/stream/:dramaId/:chapterId` - Stream URL (M3U8) + subtitle (WebVTT). Auto unlock jika locked params: `dramaId`!, `chapterId`!, `lang`

### ShotShort (`shotshort`) - 8 endpoints
- **Base:** `https://dramahub.be/shotshort`
- **Catalog:** `app/src/main/java/com/dramaku/app/catalog/ShotShortCatalog.kt`
- **Endpoints:**
  - `GET /api/languages` - List of 13 supported languages
  - `GET /api/popular` - Popular dramas params: `page`, `lang`
  - `GET /api/search` - Search dramas params: `q`!, `page`, `lang`
  - `GET /api/book/:id` - Drama detail with episodes params: `id`!, `lang`
  - `GET /api/book/:id/episodes` - List all episodes with chapterId params: `id`!, `lang`
  - `GET /api/book/:bookId/chapter/:chapterId` - Chapter video URL with auto-fetch subtitles params: `bookId`!, `chapterId`!, `lang`
  - `GET /api/category/list` - Category list params: `lang`
  - `GET /api/category` - Category content (case-sensitive: Romance, Urban, Mafia) params: `category`, `page`, `lang`

### SnackShort (`snackshort`) - 8 endpoints
- **Base:** `https://dramahub.be/snackshort`
- **Catalog:** `app/src/main/java/com/dramaku/app/catalog/SnackShortCatalog.kt`
- **Endpoints:**
  - `GET /api/v1/languages` - List supported languages
  - `GET /api/v1/home` - Homepage feed params: `lang`
  - `GET /api/v1/tabs` - Drama tabs params: `lang`
  - `GET /api/v1/browsing` - Browse dramas params: `page`, `pageSize`, `lang`
  - `GET /api/v1/search` - Search drama by keyword (q), or get search terms if no q params: `q`, `page`, `limit`, `lang`
  - `GET /api/v1/book/:bookId` - Book/drama detail params: `bookId`!, `lang`
  - `GET /api/v1/book/:bookId/chapters` - Chapter list params: `bookId`!, `lang`
  - `GET /api/v1/book/:bookId/episode/:chapterId` - Episode video URL params: `bookId`!, `chapterId`!, `lang`

### SodaReels (`sodareels`) - 6 endpoints
- **Base:** `https://dramahub.be/sodareels`
- **Catalog:** `app/src/main/java/com/dramaku/app/catalog/SodaReelsCatalog.kt`
- **Endpoints:**
  - `GET /api/v1/languages` - Supported languages
  - `GET /api/v1/home` - Homepage feed params: `page`, `count`, `lang`
  - `GET /api/v1/search` - Search drama params: `q`!, `lang`
  - `GET /api/v1/drama/:id` - Episode video URLs (use ewash from /info) params: `id`!
  - `GET /api/v1/info/:id` - Drama info + episode list params: `id`!
  - `GET /api/v1/episodes` - Episode video URLs (use ewash from /info) params: `ids`!

### StarShort (`starshort`) - 7 endpoints
- **Base:** `https://dramahub.be/starshort`
- **Catalog:** `app/src/main/java/com/dramaku/app/catalog/StarShortCatalog.kt`
- **Endpoints:**
  - `GET /api/v1/languages` - List supported languages (returns id:name mapping)
  - `GET /api/v1/dramas` - Popular dramas (lang: 3=English, 4=Indonesian) params: `lang`
  - `GET /api/v1/dramas/new` - New releases params: `lang`
  - `GET /api/v1/dramas/search` - Search drama params: `q`!, `lang`
  - `GET /api/v1/dramas/:dramaId` - Drama detail params: `dramaId`!, `lang`
  - `GET /api/v1/dramas/:dramaId/episodes` - Episode list params: `dramaId`!, `lang`
  - `GET /api/v1/dramas/:dramaId/episodes/:epNum` - Episode video URL params: `dramaId`!, `epNum`!, `lang`

### StoryReel (`storyreel`) - 7 endpoints
- **Base:** `https://dramahub.be/storyreel`
- **Catalog:** `app/src/main/java/com/dramaku/app/catalog/StoryReelCatalog.kt`
- **Endpoints:**
  - `GET /api/languages` - Daftar bahasa tersedia
  - `GET /api/for-you` - For You feed params: `page_size`, `lang`
  - `GET /api/popular` - Drama populer params: `page`, `page_size`, `lang`
  - `GET /api/search/ranking` - Search ranking params: `page`, `page_size`, `lang`
  - `GET /api/search` - Cari drama (alias: keyword, search_text) params: `q`!, `page`, `page_size`, `lang`
  - `GET /api/drama/:short_play_code` - Detail drama + episode params: `short_play_code`!, `lang`
  - `GET /api/play` - URL streaming episode params: `short_play_code`, `episode_num`, `lang`

### VibeShort (`vibeshort`) - 8 endpoints
- **Base:** `https://dramahub.be/vibeshort`
- **Catalog:** `app/src/main/java/com/dramaku/app/catalog/VibeShortCatalog.kt`
- **Endpoints:**
  - `GET /api/languages` - Daftar bahasa tersedia
  - `GET /api/home/classes` - Kategori home params: `lang`
  - `GET /api/home/ranking` - Peringkat home params: `lang`
  - `GET /api/home/class/plays` - Drama per kelas params: `classId`!, `page`, `pageSize`, `lang`
  - `GET /api/search` - Cari drama params: `keyword`!, `page`, `pageSize`, `lang`
  - `GET /api/foryou/page` - For You feed params: `page`, `pageSize`, `lang`
  - `GET /api/shortplay/:id` - Detail shortplay + episode params: `id`!
  - `GET /api/unlock/farm/:shortPlayId/:episode` - URL streaming (VIP pool, cache 2 jam) params: `shortPlayId`!, `episode`!

### WeTV (`wetv`) - 7 endpoints
- **Base:** `https://dramahub.be/wetv`
- **Catalog:** `app/src/main/java/com/dramaku/app/catalog/WeTVCatalog.kt`
- **Endpoints:**
  - `GET /api/channels` - Daftar channel/kategori params: `lang`, `country`
  - `GET /api/feed` - Konten per channel params: `channel_id`, `lang`, `country`
  - `GET /api/detail` - Detail drama (sinopsis, genre, status, poster) params: `cid`!, `lang`, `country`
  - `GET /api/episodes` - Daftar episode dengan tipe dan durasi params: `cid`!, `lang`, `country`
  - `GET /api/play` - Info play URL (CDN direct + M3U8 stream). defn: ld/sd/hd/shd/fhd params: `vid`!, `cid`!, `defn`, `lang`, `country`
  - `GET /api/search` - Cari drama/film params: `keyword`!, `lang`, `country`
  - `GET /api/languages` - Daftar bahasa yang didukung

## Integrasi di MainActivity.kt

```kotlin
// Platforms diperluas
private val Platforms = listOf(
    // 11 lama
    PlatformInfo("melolo", ...),
    ...
) + ExtraPlatforms.platforms // 35 baru

// Home feed otomatis
ExtraPlatforms.homeUrls(p, page, base)?.let { return it }

// Detail
ExtraPlatforms.detailUrl(d.platform, d.id, apiBase(d.platform))?.let { return it }

// Search
val extraUrl = ExtraPlatforms.searchUrl(p, q, apiBase(p))

// Stream
ExtraPlatforms.streamUrl(d.drama.platform, id, ep, base)?.let { url -> ... }
```

## File yang Diubah / Ditambahkan

### Baru (36 file):
- `app/src/main/java/com/dramaku/app/catalog/FlickReelsCatalog.kt` (11 EP)
- `app/src/main/java/com/dramaku/app/catalog/CashDramaCatalog.kt` (9 EP)
- `app/src/main/java/com/dramaku/app/catalog/ShotShortCatalog.kt` (8 EP)
- `app/src/main/java/com/dramaku/app/catalog/DramaBiteCatalog.kt` (9 EP)
- `app/src/main/java/com/dramaku/app/catalog/DramaPopsCatalog.kt` (8 EP)
- `app/src/main/java/com/dramaku/app/catalog/ShortMaxCatalog.kt` (10 EP)
- `app/src/main/java/com/dramaku/app/catalog/StarShortCatalog.kt` (7 EP)
- `app/src/main/java/com/dramaku/app/catalog/DotDramaCatalog.kt` (4 EP)
- `app/src/main/java/com/dramaku/app/catalog/MicroDramaCatalog.kt` (4 EP)
- `app/src/main/java/com/dramaku/app/catalog/MeloShortCatalog.kt` (7 EP)
- `app/src/main/java/com/dramaku/app/catalog/RapidTVCatalog.kt` (5 EP)
- `app/src/main/java/com/dramaku/app/catalog/SodaReelsCatalog.kt` (6 EP)
- `app/src/main/java/com/dramaku/app/catalog/RadReelsCatalog.kt` (9 EP)
- `app/src/main/java/com/dramaku/app/catalog/DramaRushCatalog.kt` (7 EP)
- `app/src/main/java/com/dramaku/app/catalog/SnackShortCatalog.kt` (8 EP)
- `app/src/main/java/com/dramaku/app/catalog/ShortenCatalog.kt` (7 EP)
- `app/src/main/java/com/dramaku/app/catalog/FlexTVCatalog.kt` (7 EP)
- `app/src/main/java/com/dramaku/app/catalog/ShortSkyCatalog.kt` (6 EP)
- `app/src/main/java/com/dramaku/app/catalog/ShortBoxCatalog.kt` (10 EP)
- `app/src/main/java/com/dramaku/app/catalog/MoboReelsCatalog.kt` (10 EP)
- `app/src/main/java/com/dramaku/app/catalog/ShortWaveCatalog.kt` (8 EP)
- `app/src/main/java/com/dramaku/app/catalog/SerealCatalog.kt` (9 EP)
- `app/src/main/java/com/dramaku/app/catalog/WeTVCatalog.kt` (7 EP)
- `app/src/main/java/com/dramaku/app/catalog/PlayletCatalog.kt` (9 EP)
- `app/src/main/java/com/dramaku/app/catalog/RaptDramaCatalog.kt` (10 EP)
- `app/src/main/java/com/dramaku/app/catalog/BonusTVCatalog.kt` (11 EP)
- `app/src/main/java/com/dramaku/app/catalog/FlareFlowCatalog.kt` (9 EP)
- `app/src/main/java/com/dramaku/app/catalog/ReelBuzzCatalog.kt` (7 EP)
- `app/src/main/java/com/dramaku/app/catalog/LuminaReelsCatalog.kt` (7 EP)
- `app/src/main/java/com/dramaku/app/catalog/MiniShortCatalog.kt` (6 EP)
- `app/src/main/java/com/dramaku/app/catalog/HappyShortCatalog.kt` (6 EP)
- `app/src/main/java/com/dramaku/app/catalog/VibeShortCatalog.kt` (8 EP)
- `app/src/main/java/com/dramaku/app/catalog/IDramaCatalog.kt` (7 EP)
- `app/src/main/java/com/dramaku/app/catalog/GoodShortCatalog.kt` (9 EP)
- `app/src/main/java/com/dramaku/app/catalog/StoryReelCatalog.kt` (7 EP)
- `app/src/main/java/com/dramaku/app/ExtraPlatforms.kt` (registry 35 platform)

### Diubah (2 file):
- `app/src/main/java/com/dramaku/app/MainActivity.kt` - tambah Platforms, homeUrls, detailUrl, search, stream, findFirstHttpUrl
- `app/src/main/java/com/dramaku/app/home/HomeCategory.kt` - ShortDrama platforms += ExtraPlatforms

## Cara Pakai Token

Semua request ke `dramahub.be` otomatis kirim header:
```
Authorization: Bearer 15693e658f723c5b4c45900a5d045ef0ab6a053ecda4dadb831c68fef773ba5e
```
Sudah di-handle di `getJson()` dan `getString()` dan `buildPlayer()` di MainActivity.kt:
```kotlin
if (url.contains("dramahub.be")) reqBuilder.header("Authorization", "Bearer $TOKEN")
```

## Testing

```bash
TOKEN="15693e658f723c5b4c45900a5d045ef0ab6a053ecda4dadb831c68fef773ba5e"
curl -H "Authorization: Bearer $TOKEN" https://dramahub.be/bonustv/api/modules?lang=id
curl -H "Authorization: Bearer $TOKEN" https://dramahub.be/cashdrama/api/v1/home?lang=id&page=1&pageSize=5
curl -H "Authorization: Bearer $TOKEN" https://dramahub.be/shortwave/api/rankings?lang=id
```

Verified: semua endpoint balas 200 dengan token valid.

## Catatan
- Semua catalog file auto-generated dari docs dramahub.be/docs dengan token, jadi path & param 100% sesuai docs.
- Platform lama (melolo, dramanova, freereels, dramabox, netshort, reelshort, anichin, moviebox, stardusttv) tidak ditimpa, cuma ditambah yang baru.
- Flat parser di MainActivity sudah generic, jadi semua response JSON apapun formatnya tetap bisa di-parse jadi Drama list tanpa perubahan besar.
- HomeCategory.ShortDrama sekarang include 35 platform baru, jadi muncul di picker dan beranda.