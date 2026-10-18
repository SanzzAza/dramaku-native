package com.dramaku.app

import com.dramaku.app.catalog.*
import java.net.URLEncoder

/**
 * Extra platforms auto-generated dari docs dramahub.be
 * Berisi 35 platform baru yang belum ada di dramaku-native
 * Semua endpoint sudah ditambahkan via catalog masing-masing
 */
object ExtraPlatforms {

    val platforms = listOf(
        PlatformInfo("bonustv", "BonusTV", "https://dramahub.be/bonustv", logoUrl = "https://dramahub.be/logos/bonustv.png", logoRes = R.drawable.logo_bonustv),
        PlatformInfo("cashdrama", "CashDrama", "https://dramahub.be/cashdrama", logoUrl = "https://dramahub.be/logos/cashdrama.png", logoRes = R.drawable.logo_cashdrama),
        PlatformInfo("dotdrama", "DotDrama", "https://dramahub.be/dotdrama", logoUrl = "https://dramahub.be/logos/dotdrama.png", logoRes = R.drawable.logo_dotdrama),
        PlatformInfo("dramabite", "DramaBite", "https://dramahub.be/dramabite", logoUrl = "https://dramahub.be/logos/dramabite.png", logoRes = R.drawable.logo_dramabite),
        PlatformInfo("dramapops", "DramaPops", "https://dramahub.be/dramapops", logoUrl = "https://dramahub.be/logos/dramapops.png", logoRes = R.drawable.logo_dramapops),
        PlatformInfo("dramarush", "DramaRush", "https://dramahub.be/dramarush", logoUrl = "https://dramahub.be/logos/dramarush.png", logoRes = R.drawable.logo_dramarush),
        PlatformInfo("flareflow", "FlareFlow", "https://dramahub.be/flareflow", logoUrl = "https://dramahub.be/logos/flareflow.png", logoRes = R.drawable.logo_flareflow),
        PlatformInfo("flextv", "FlexTV", "https://dramahub.be/flextv", logoUrl = "https://dramahub.be/logos/flextv.png", logoRes = R.drawable.logo_flextv),
        PlatformInfo("flickreels", "FlickReels", "https://dramahub.be/flickreels", logoUrl = "https://dramahub.be/logos/flickreels.png", logoRes = R.drawable.logo_flickreels),
        PlatformInfo("goodshort", "GoodShort", "https://dramahub.be/goodshort", logoUrl = "https://dramahub.be/logos/goodshort.png", logoRes = R.drawable.logo_goodshort),
        PlatformInfo("happyshort", "HappyShort", "https://dramahub.be/happyshort", logoUrl = "https://dramahub.be/logos/happyshort.png", logoRes = R.drawable.logo_happyshort),
        PlatformInfo("idrama", "IDrama", "https://dramahub.be/idrama", logoUrl = "https://dramahub.be/logos/idrama.png", logoRes = R.drawable.logo_idrama),
        PlatformInfo("luminareels", "LuminaReels", "https://dramahub.be/luminareels", logoUrl = "https://dramahub.be/logos/luminareels.png", logoRes = R.drawable.logo_luminareels),
        PlatformInfo("meloshort", "MeloShort", "https://dramahub.be/meloshort", logoUrl = "https://dramahub.be/logos/meloshort.png", logoRes = R.drawable.logo_meloshort),
        PlatformInfo("microdrama", "MicroDrama", "https://dramahub.be/microdrama", logoUrl = "https://dramahub.be/logos/microdrama.png", logoRes = R.drawable.logo_microdrama),
        PlatformInfo("minishort", "MiniShort", "https://dramahub.be/minishort", logoUrl = "https://dramahub.be/logos/minishort.png", logoRes = R.drawable.logo_minishort),
        PlatformInfo("moboreels", "MoboReels", "https://dramahub.be/moboreels", logoUrl = "https://dramahub.be/logos/moboreels.png", logoRes = R.drawable.logo_moboreels),
        PlatformInfo("playlet", "Playlet", "https://dramahub.be/playlet", logoUrl = "https://dramahub.be/logos/playlet.png", logoRes = R.drawable.logo_playlet),
        PlatformInfo("radreels", "RadReels", "https://dramahub.be/radreels", logoUrl = "https://dramahub.be/logos/radreels.png", logoRes = R.drawable.logo_radreels),
        PlatformInfo("rapidtv", "RapidTV", "https://dramahub.be/rapidtv", logoUrl = "https://dramahub.be/logos/rapidtv.png", logoRes = R.drawable.logo_rapidtv),
        PlatformInfo("raptdrama", "RaptDrama", "https://dramahub.be/raptdrama", logoUrl = "https://dramahub.be/logos/raptdrama.png", logoRes = R.drawable.logo_raptdrama),
        PlatformInfo("reelbuzz", "ReelBuzz", "https://dramahub.be/reelbuzz", logoUrl = "https://dramahub.be/logos/reelbuzz.png", logoRes = R.drawable.logo_reelbuzz),
        PlatformInfo("sereal", "Sereal", "https://dramahub.be/sereal", logoUrl = "https://dramahub.be/logos/sereal.png", logoRes = R.drawable.logo_sereal),
        PlatformInfo("shortbox", "ShortBox", "https://dramahub.be/shortbox", logoUrl = "https://dramahub.be/logos/shortbox.png", logoRes = R.drawable.logo_shortbox),
        PlatformInfo("shorten", "Shorten", "https://dramahub.be/shorten", logoUrl = "https://dramahub.be/logos/shorten.png", logoRes = R.drawable.logo_shorten),
        PlatformInfo("shortmax", "ShortMax", "https://dramahub.be/shortmax", logoUrl = "https://dramahub.be/logos/shortmax.png", logoRes = R.drawable.logo_shortmax),
        PlatformInfo("shortsky", "ShortSky", "https://dramahub.be/shortsky", logoUrl = "https://dramahub.be/logos/shortsky.png", logoRes = R.drawable.logo_shortsky),
        PlatformInfo("shortwave", "ShortWave", "https://dramahub.be/shortwave", logoUrl = "https://dramahub.be/logos/shortwave.png", logoRes = R.drawable.logo_shortwave),
        PlatformInfo("shotshort", "ShotShort", "https://dramahub.be/shotshort", logoUrl = "https://dramahub.be/logos/shotshort.png", logoRes = R.drawable.logo_shotshort),
        PlatformInfo("snackshort", "SnackShort", "https://dramahub.be/snackshort", logoUrl = "https://dramahub.be/logos/snackshort.png", logoRes = R.drawable.logo_snackshort),
        PlatformInfo("sodareels", "SodaReels", "https://dramahub.be/sodareels", logoUrl = "https://dramahub.be/logos/sodareels.png", logoRes = R.drawable.logo_sodareels),
        PlatformInfo("starshort", "StarShort", "https://dramahub.be/starshort", logoUrl = "https://dramahub.be/logos/starshort.png", logoRes = R.drawable.logo_starshort),
        PlatformInfo("storyreel", "StoryReel", "https://dramahub.be/storyreel", logoUrl = "https://dramahub.be/logos/storyreel.png", logoRes = R.drawable.logo_storyreel),
        PlatformInfo("vibeshort", "VibeShort", "https://dramahub.be/vibeshort", logoUrl = "https://dramahub.be/logos/vibeshort.png", logoRes = R.drawable.logo_vibeshort),
        PlatformInfo("wetv", "WeTV", "https://dramahub.be/wetv", logoUrl = "https://dramahub.be/logos/wetv.png", logoRes = R.drawable.logo_wetv),
    )

    // Fallback genre untuk platform baru (dipakai kalau /categories gagal)
    val fallbackGenres: Map<String, List<Pair<String, String>>> = mapOf(
        "bonustv" to listOf("Populer" to "popular", "Terbaru" to "new", "Rekomendasi" to "recommend"),
        "cashdrama" to listOf("Populer" to "popular", "Terbaru" to "new", "Rekomendasi" to "recommend"),
        "dotdrama" to listOf("Populer" to "popular", "Terbaru" to "new", "Rekomendasi" to "recommend"),
        "dramabite" to listOf("Populer" to "popular", "Terbaru" to "new", "Rekomendasi" to "recommend"),
        "dramapops" to listOf("Populer" to "popular", "Terbaru" to "new", "Rekomendasi" to "recommend"),
        "dramarush" to listOf("Populer" to "popular", "Terbaru" to "new", "Rekomendasi" to "recommend"),
        "flareflow" to listOf("Populer" to "popular", "Terbaru" to "new", "Rekomendasi" to "recommend"),
        "flextv" to listOf("Populer" to "popular", "Terbaru" to "new", "Rekomendasi" to "recommend"),
        "flickreels" to listOf("Populer" to "popular", "Terbaru" to "new", "Rekomendasi" to "recommend"),
        "goodshort" to listOf("Populer" to "popular", "Terbaru" to "new", "Rekomendasi" to "recommend"),
        "happyshort" to listOf("Populer" to "popular", "Terbaru" to "new", "Rekomendasi" to "recommend"),
        "idrama" to listOf("Populer" to "popular", "Terbaru" to "new", "Rekomendasi" to "recommend"),
        "luminareels" to listOf("Populer" to "popular", "Terbaru" to "new", "Rekomendasi" to "recommend"),
        "meloshort" to listOf("Populer" to "popular", "Terbaru" to "new", "Rekomendasi" to "recommend"),
        "microdrama" to listOf("Populer" to "popular", "Terbaru" to "new", "Rekomendasi" to "recommend"),
        "minishort" to listOf("Populer" to "popular", "Terbaru" to "new", "Rekomendasi" to "recommend"),
        "moboreels" to listOf("Populer" to "popular", "Terbaru" to "new", "Rekomendasi" to "recommend"),
        "playlet" to listOf("Populer" to "popular", "Terbaru" to "new", "Rekomendasi" to "recommend"),
        "radreels" to listOf("Populer" to "popular", "Terbaru" to "new", "Rekomendasi" to "recommend"),
        "rapidtv" to listOf("Populer" to "popular", "Terbaru" to "new", "Rekomendasi" to "recommend"),
        "raptdrama" to listOf("Populer" to "popular", "Terbaru" to "new", "Rekomendasi" to "recommend"),
        "reelbuzz" to listOf("Populer" to "popular", "Terbaru" to "new", "Rekomendasi" to "recommend"),
        "sereal" to listOf("Populer" to "popular", "Terbaru" to "new", "Rekomendasi" to "recommend"),
        "shortbox" to listOf("Populer" to "popular", "Terbaru" to "new", "Rekomendasi" to "recommend"),
        "shorten" to listOf("Populer" to "popular", "Terbaru" to "new", "Rekomendasi" to "recommend"),
        "shortmax" to listOf("Populer" to "popular", "Terbaru" to "new", "Rekomendasi" to "recommend"),
        "shortsky" to listOf("Populer" to "popular", "Terbaru" to "new", "Rekomendasi" to "recommend"),
        "shortwave" to listOf("Populer" to "popular", "Terbaru" to "new", "Rekomendasi" to "recommend"),
        "shotshort" to listOf("Populer" to "popular", "Terbaru" to "new", "Rekomendasi" to "recommend"),
        "snackshort" to listOf("Populer" to "popular", "Terbaru" to "new", "Rekomendasi" to "recommend"),
        "sodareels" to listOf("Populer" to "popular", "Terbaru" to "new", "Rekomendasi" to "recommend"),
        "starshort" to listOf("Populer" to "popular", "Terbaru" to "new", "Rekomendasi" to "recommend"),
        "storyreel" to listOf("Populer" to "popular", "Terbaru" to "new", "Rekomendasi" to "recommend"),
        "vibeshort" to listOf("Populer" to "popular", "Terbaru" to "new", "Rekomendasi" to "recommend"),
        "wetv" to listOf("Populer" to "popular", "Terbaru" to "new", "Rekomendasi" to "recommend"),
    )

    fun isExtraPlatform(id: String): Boolean = platforms.any { it.id == id }

    fun homeUrls(platform: String, page: Int, base: String): List<String>? {
        val p = page.coerceAtLeast(1)
        return when (platform) {
            "bonustv" -> listOf("$base/api/modules?lang=id", "$base/api/recommend?lang=id&page=$p&page_size=20", "$base/api/videos/recently?lang=id")
            "cashdrama" -> listOf("$base/api/v1/home?lang=id&page=$p&pageSize=20&blockId=5", "$base/api/v1/home?lang=id&page=${p+1}&pageSize=20&blockId=5", "$base/api/v1/home?lang=id&page=${p+2}&pageSize=20&blockId=5")
            "dotdrama" -> listOf("$base/api/v1/dramas?page=$p&limit=50&lang=id", "$base/api/v1/collections?lang=id", "$base/api/v1/dramas?page=${p+1}&limit=50&lang=id")
            "dramabite" -> listOf("$base/api/v1/foryou?lang=id&page=$p", "$base/api/v1/dramas?lang=id&page=$p", "$base/api/v1/recommend?lang=id&page=$p")
            "dramapops" -> listOf("$base/api/drama/popular?limit=20&language=en", "$base/api/homepage?language=en", "$base/api/drama/trending?limit=20&language=en")
            "dramarush" -> listOf("$base/api/v1/tabs/0?lang=id", "$base/api/v1/ranking?lang=id", "$base/api/v1/tabs/319?lang=id")
            "flareflow" -> listOf("$base/api/popular/list?language=en", "$base/api/home?tab=1&language=en", "$base/api/foryou/list?page=$p&pageSize=20&language=en")
            "flextv" -> listOf("$base/api/v1/tabs/1?lang=id&page=$p", "$base/api/v1/tabs/2?lang=id&page=$p", "$base/api/v1/tabs/3?lang=id&page=$p")
            "flickreels" -> listOf("$base/api/v1/for-you?page=$p&page_size=20&lang=id", "$base/api/v1/hot-rank?lang=id", "$base/api/v1/search-rank?lang=id")
            "goodshort" -> listOf("$base/api/v1/home?channelId=656&page=$p&pageSize=20&index=1", "$base/api/v1/trending?page=$p&pageSize=20&index=1&tags=550,575", "$base/api/v1/ranking?type=0&page=$p&pageSize=20")
            "happyshort" -> listOf("$base/home?lang=en", "$base/ranking?lang=en", "$base/home?lang=en")
            "idrama" -> listOf("$base/api/v1/popular?page=$p&limit=20&lang=id", "$base/api/v1/latest?page=$p&limit=20&lang=id", "$base/api/v1/popular?page=${p+1}&limit=20&lang=id")
            "luminareels" -> listOf("$base/api/drama/list?page=$p&page_size=20&lang=id", "$base/api/drama/list?page=${p+1}&page_size=20&lang=id", "$base/api/drama/list?page=${p+2}&page_size=20&lang=id")
            "meloshort" -> listOf("$base/api/v1/dramas/top?lang=id", "$base/api/v1/drama/all?page=$p&limit=20&lang=id", "$base/api/v1/drama/all?page=${p+1}&limit=20&lang=id")
            "microdrama" -> listOf("$base/api/v1/dramas?lang=id&limit=50", "$base/api/v1/dramas?lang=en&limit=50", "$base/api/v1/dramas?lang=id&limit=50")
            "minishort" -> listOf("$base/api/home_recommend?language=en", "$base/api/catalog?module_type=8&page_index=$p&page_size=20&language=en", "$base/api/catalog?module_type=9&page_index=$p&page_size=20&language=en")
            "moboreels" -> listOf("$base/api/hotList?listId=10&skipSeries=1&langId=3", "$base/api/hotList?listId=11&skipSeries=1&langId=3", "$base/api/hotList?listId=10&skipSeries=20&langId=3")
            "playlet" -> listOf("$base/shorts/recommend?language=en&page=$p&pageSize=20", "$base/new-arrivals?language=en", "$base/column?columnTypeId=91&language=en")
            "radreels" -> listOf("$base/api/v1/ranking?lang=id", "$base/api/v1/foryou?page=$p&lang=id", "$base/api/v1/tab/1107?page=$p&size=20&lang=id")
            "rapidtv" -> listOf("$base/api/v1/dramas?page=$p&size=20&lang=id", "$base/api/v1/dramas?page=${p+1}&size=20&lang=id", "$base/api/v1/dramas?page=${p+2}&size=20&lang=id")
            "raptdrama" -> listOf("$base/open/classify_video?type=20&page=$p&limit=10&lang=en-US", "$base/open/classify_video?type=21&page=$p&limit=10&lang=en-US", "$base/open/classify_video?type=18&page=$p&limit=10&lang=en-US")
            "reelbuzz" -> listOf("$base/api/drama/module/recommend?pageNum=$p&pageSize=20&language=id", "$base/api/drama/list?pageNum=$p&pageSize=20&language=id", "$base/api/drama/display?pageNum=$p&pageSize=20&language=id")
            "sereal" -> listOf("$base/api/index/home/$p?lang=id", "$base/api/index/for-you/$p?lang=id", "$base/api/index/trending?lang=id")
            "shortbox" -> listOf("$base/api/list?page=$p&page_size=20&languages=id&streamable=true", "$base/api/for-you?page=$p&page_size=20&languages=id&streamable=true", "$base/api/recommend?languages=id&streamable=true")
            "shorten" -> listOf("$base/api/v1/editors?page=$p&perPage=20", "$base/api/v1/exclusive?page=$p&perPage=20", "$base/api/v1/releases?page=$p&perPage=20")
            "shortmax" -> listOf("$base/api/v1/feed/recommend?lang=id", "$base/api/v1/home?tab=1&lang=id", "$base/api/v1/feed/new?lang=id")
            "shortsky" -> listOf("$base/api/home?lang=id_id", "$base/api/recommend?lang=id_id", "$base/api/home?lang=id_id")
            "shortwave" -> listOf("$base/api/top?lang=id", "$base/api/all?lang=id", "$base/api/more?page=$p&page_size=20&lang=id")
            "shotshort" -> listOf("$base/api/popular?page=$p&lang=id", "$base/api/category?category=Romance&page=$p&lang=id", "$base/api/category?category=Urban&page=$p&lang=id")
            "snackshort" -> listOf("$base/api/v1/home?lang=Indonesian", "$base/api/v1/browsing?page=$p&pageSize=20&lang=Indonesian", "$base/api/v1/browsing?page=${p+1}&pageSize=20&lang=Indonesian")
            "sodareels" -> listOf("$base/api/v1/dramas?page=$p&limit=20&lang=id", "$base/api/v1/collections?lang=id", "$base/api/v1/dramas?page=${p+1}&limit=20&lang=id")
            "starshort" -> listOf("$base/api/v1/dramas?lang=4", "$base/api/v1/dramas/new?lang=3", "$base/api/v1/dramas?lang=3")
            "storyreel" -> listOf("$base/api/popular?page=$p&page_size=20&lang=in", "$base/api/search/ranking?page=$p&page_size=20&lang=in", "$base/api/popular?page=${p+1}&page_size=20&lang=in")
            "vibeshort" -> listOf("$base/api/home/ranking?lang=en", "$base/api/foryou/page?page=$p&pageSize=10&lang=en", "$base/api/foryou/page?page=${p+1}&pageSize=10&lang=en")
            "wetv" -> listOf("$base/api/search?keyword=cinta&lang=id&country=ID", "$base/api/search?keyword=ceo&lang=id&country=ID", "$base/api/search?keyword=nikah&lang=id&country=ID")
            else -> null
        }
    }

    fun detailUrl(platform: String, dramaId: String, base: String): String? {
        val encId = URLEncoder.encode(dramaId, "UTF-8")
        return when (platform) {
            "bonustv" -> "$base/api/video/$encId?lang=id"
            "cashdrama" -> "$base/api/v1/drama/$encId?ep=1&lang=id"
            "dotdrama" -> "$base/api/v1/dramas/$encId"
            "dramabite" -> "$base/api/v1/drama/$encId?lang=id"
            "dramapops" -> "$base/api/drama/$encId?language=en"
            "dramarush" -> "$base/api/v1/drama/$encId?lang=id"
            "flareflow" -> "$base/api/drama/$encId?language=en"
            "flextv" -> "$base/api/v1/series/$encId?lang=id"
            "flickreels" -> "$base/api/v1/chapters/$encId?lang=id"
            "goodshort" -> "$base/api/v1/chapters/$encId"
            "happyshort" -> "$base/drama/$encId"
            "idrama" -> "$base/api/v1/drama/$encId?lang=id"
            "luminareels" -> "$base/api/drama/detail?id=$encId&lang=id"
            "meloshort" -> "$base/api/v1/dramas/$encId/episodes?page=1&limit=100&lang=id"
            "microdrama" -> "$base/api/v1/dramas/$encId"
            "minishort" -> "$base/api/video_info?video_id=$encId&language=en"
            "moboreels" -> "$base/api/seriesDetail?seriesId=$encId&langId=3"
            "playlet" -> "$base/theater/$encId?language=en"
            "radreels" -> "$base/api/v1/episodes/$encId?lang=id"
            "rapidtv" -> "$base/api/v1/dramas/$encId?lang=id"
            "raptdrama" -> "$base/video/playlist?vid=$encId&lang=en-US"
            "reelbuzz" -> "$base/api/drama/detail?dramaId=$encId&language=id"
            "sereal" -> "$base/api/content/detail?contentId=$encId&lang=id"
            "shortbox" -> "$base/api/info/$encId?sort=1&languages=id"
            "shorten" -> "$base/api/v1/series/$encId"
            "shortmax" -> "$base/api/v1/detail/$encId?lang=id"
            "shortsky" -> "$base/api/drama/$encId?lang=id_id"
            "shortwave" -> "$base/api/drama/$encId?lang=id"
            "shotshort" -> "$base/api/book/$encId?lang=id"
            "snackshort" -> "$base/api/v1/book/$encId?lang=Indonesian"
            "sodareels" -> "$base/api/v1/info/$encId"
            "starshort" -> "$base/api/v1/dramas/$encId?lang=4"
            "storyreel" -> "$base/api/drama/$encId?lang=in"
            "vibeshort" -> "$base/api/shortplay/$encId"
            "wetv" -> "$base/api/detail?cid=$encId&lang=id&country=ID"
            else -> null
        }
    }

    fun searchUrl(platform: String, query: String, base: String): String? {
        val encQ = URLEncoder.encode(query, "UTF-8")
        return when (platform) {
            "bonustv" -> "$base/api/search?q=$encQ&keyword=$encQ&lang=id&page=1&page_size=20"
            "cashdrama" -> "$base/api/v1/search?q=$encQ&page=1&lang=id"
            "dramabite" -> "$base/api/v1/search?q=$encQ&lang=id&limit=20"
            "dramapops" -> "$base/api/drama/search?q=$encQ&limit=20&language=en"
            "dramarush" -> "$base/api/v1/search/$encQ?lang=id"
            "flareflow" -> "$base/api/search?keyword=$encQ&page=1&language=en"
            "flextv" -> "$base/api/v1/search?q=$encQ&page=1&lang=id"
            "flickreels" -> "$base/api/v1/search?keyword=$encQ&lang=id"
            "goodshort" -> "$base/api/v1/search?q=$encQ"
            "happyshort" -> "$base/search?keyword=$encQ"
            "idrama" -> "$base/api/v1/search?q=$encQ&page_size=20&lang=id"
            "luminareels" -> "$base/api/drama/search?q=$encQ&page=1&page_size=20&lang=id"
            "meloshort" -> "$base/api/v1/dramas/search?q=$encQ&lang=id"
            "microdrama" -> "$base/api/v1/dramas/search?q=$encQ&lang=id"
            "moboreels" -> "$base/api/search?searchKey=$encQ&pageNo=1&pageSize=20&langId=3"
            "playlet" -> "$base/search?keyword=$encQ&language=en&page=1&pageSize=20"
            "radreels" -> "$base/api/v1/drama/$encQ?page=1&lang=id"
            "rapidtv" -> "$base/api/v1/search?q=$encQ&page=1&size=20&lang=id"
            "raptdrama" -> "$base/open/search?keyword=$encQ&page=1&lang=en-US"
            "reelbuzz" -> "$base/api/search?keyword=$encQ&language=id"
            "sereal" -> "$base/api/index/search/$encQ?lang=id"
            "shortbox" -> "$base/api/search?q=$encQ&page=1&page_size=20&languages=id"
            "shortmax" -> "$base/api/v1/search?q=$encQ&lang=id&page=1"
            "shortsky" -> "$base/api/search?q=$encQ&page=1&lang=id_id"
            "shortwave" -> "$base/api/search/$encQ?lang=id"
            "shotshort" -> "$base/api/search?q=$encQ&page=1&lang=id"
            "snackshort" -> "$base/api/v1/search?q=$encQ&page=1&limit=20&lang=Indonesian"
            "sodareels" -> "$base/api/v1/search?q=$encQ&lang=id"
            "starshort" -> "$base/api/v1/dramas/search?q=$encQ&lang=4"
            "storyreel" -> "$base/api/search?q=$encQ&page=1&page_size=20&lang=in"
            "vibeshort" -> "$base/api/search?keyword=$encQ&page=1&pageSize=20&lang=en"
            "wetv" -> "$base/api/search?keyword=$encQ&lang=id&country=ID"
            else -> null
        }
    }

    fun streamUrl(platform: String, dramaId: String, episode: Int, base: String): String? {
        val encId = URLEncoder.encode(dramaId, "UTF-8")
        val ep = episode.coerceAtLeast(1)
        return when (platform) {
            "bonustv" -> "$base/api/stream/$encId?video_id=$encId&episode_num=$ep&resolution=720p&lang=id"
            "cashdrama" -> "$base/api/v1/play/$encId/$ep?lang=id"
            "dramabite" -> "$base/api/v1/drama/$encId/episode/$ep?lang=id"
            "dramapops" -> "$base/api/drama/$encId/$ep?quality=720p&language=en"
            "dramarush" -> "$base/api/v1/play/$encId/$ep?lang=id"
            "flareflow" -> "$base/api/drama/$encId/play?ep=$ep&language=en"
            "flextv" -> "$base/api/v1/play/$encId/$ep?lang=id"
            "flickreels" -> "$base/api/v1/stream/$encId/$ep?lang=id"
            "goodshort" -> "$base/api/v1/unlock/$encId?q=720p"
            "happyshort" -> "$base/drama/$encId/play/$ep"
            "idrama" -> "$base/api/v1/drama/$encId/play?episode=$ep&lang=id"
            "luminareels" -> "$base/api/stream/$encId/$ep/master.m3u8?lang=id"
            "meloshort" -> "$base/api/v1/dramas/$encId/episodes/$ep?lang=id"
            "minishort" -> "$base/api/episodes_content?video_id=$encId&episodes_id=$ep&language=en"
            "moboreels" -> "$base/api/video?seriesId=$encId&episNum=$ep&langId=3"
            "playlet" -> "$base/theater/$encId/open/$ep/playlist.m3u8"
            "radreels" -> "$base/api/v1/video/$encId/$ep?lang=id"
            "rapidtv" -> "$base/api/v1/dramas/$encId/episodes?lang=id"
            "raptdrama" -> "$base/video/playlist?vid=$encId&lang=en-US"
            "reelbuzz" -> "$base/api/drama/video/$encId/$ep?language=id"
            "sereal" -> "$base/api/watch/$encId/$ep?lang=id"
            "shortbox" -> "$base/api/stream/$encId/$ep?quality=720"
            "shortmax" -> "$base/api/v1/play/$encId?ep=$ep&lang=id"
            "shortsky" -> "$base/api/drama/$encId/episode/$ep?lang=id_id"
            "shortwave" -> "$base/api/stream/$encId/$ep?lang=id"
            "shotshort" -> "$base/api/book/$encId/chapter/$ep?lang=id"
            "snackshort" -> "$base/api/v1/book/$encId/episode/$ep?lang=Indonesian"
            "starshort" -> "$base/api/v1/dramas/$encId/episodes/$ep?lang=4"
            "storyreel" -> "$base/api/play?short_play_code=$encId&episode_num=$ep&lang=in"
            "vibeshort" -> "$base/api/unlock/farm/$encId/$ep"
            "wetv" -> "$base/api/play?vid=$encId&cid=$encId&lang=id&country=ID"
            else -> null
        }
    }
}
