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
        PlatformInfo("bonustv", "BonusTV", "https://dramahub.be/bonustv"),
        PlatformInfo("cashdrama", "CashDrama", "https://dramahub.be/cashdrama"),
        PlatformInfo("dotdrama", "DotDrama", "https://dramahub.be/dotdrama"),
        PlatformInfo("dramabite", "DramaBite", "https://dramahub.be/dramabite"),
        PlatformInfo("dramapops", "DramaPops", "https://dramahub.be/dramapops"),
        PlatformInfo("dramarush", "DramaRush", "https://dramahub.be/dramarush"),
        PlatformInfo("flareflow", "FlareFlow", "https://dramahub.be/flareflow"),
        PlatformInfo("flextv", "FlexTV", "https://dramahub.be/flextv"),
        PlatformInfo("flickreels", "FlickReels", "https://dramahub.be/flickreels"),
        PlatformInfo("goodshort", "GoodShort", "https://dramahub.be/goodshort"),
        PlatformInfo("happyshort", "HappyShort", "https://dramahub.be/happyshort"),
        PlatformInfo("idrama", "IDrama", "https://dramahub.be/idrama"),
        PlatformInfo("luminareels", "LuminaReels", "https://dramahub.be/luminareels"),
        PlatformInfo("meloshort", "MeloShort", "https://dramahub.be/meloshort"),
        PlatformInfo("microdrama", "MicroDrama", "https://dramahub.be/microdrama"),
        PlatformInfo("minishort", "MiniShort", "https://dramahub.be/minishort"),
        PlatformInfo("moboreels", "MoboReels", "https://dramahub.be/moboreels"),
        PlatformInfo("playlet", "Playlet", "https://dramahub.be/playlet"),
        PlatformInfo("radreels", "RadReels", "https://dramahub.be/radreels"),
        PlatformInfo("rapidtv", "RapidTV", "https://dramahub.be/rapidtv"),
        PlatformInfo("raptdrama", "RaptDrama", "https://dramahub.be/raptdrama"),
        PlatformInfo("reelbuzz", "ReelBuzz", "https://dramahub.be/reelbuzz"),
        PlatformInfo("sereal", "Sereal", "https://dramahub.be/sereal"),
        PlatformInfo("shortbox", "ShortBox", "https://dramahub.be/shortbox"),
        PlatformInfo("shorten", "Shorten", "https://dramahub.be/shorten"),
        PlatformInfo("shortmax", "ShortMax", "https://dramahub.be/shortmax"),
        PlatformInfo("shortsky", "ShortSky", "https://dramahub.be/shortsky"),
        PlatformInfo("shortwave", "ShortWave", "https://dramahub.be/shortwave"),
        PlatformInfo("shotshort", "ShotShort", "https://dramahub.be/shotshort"),
        PlatformInfo("snackshort", "SnackShort", "https://dramahub.be/snackshort"),
        PlatformInfo("sodareels", "SodaReels", "https://dramahub.be/sodareels"),
        PlatformInfo("starshort", "StarShort", "https://dramahub.be/starshort"),
        PlatformInfo("storyreel", "StoryReel", "https://dramahub.be/storyreel"),
        PlatformInfo("vibeshort", "VibeShort", "https://dramahub.be/vibeshort"),
        PlatformInfo("wetv", "WeTV", "https://dramahub.be/wetv"),
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
            "bonustv" -> listOf("$base/api/modules?lang=id", "$base/api/recommend?lang=id&page=$p&page_size=$p", "$base/api/videos/recently?lang=id")
            "cashdrama" -> listOf("$base/api/v1/home?lang=id&page=$p&pageSize=20&blockId=5", "$base/api/v1/home?lang=id&page=$p&pageSize=20&blockId=6", "$base/api/v1/home?lang=id&page=${p+1}&pageSize=20&blockId=5")
            "dotdrama" -> listOf("$base/api/v1/dramas?page=$p&limit=50&lang=id", "$base/api/v1/collections?lang=id", "$base/api/v1/dramas?page=${p+1}&limit=50&lang=id")
            "dramabite" -> listOf("$base/api/v1/foryou?lang=id&page=$p", "$base/api/v1/dramas?lang=id&page=$p", "$base/api/v1/foryou?lang=id&page=${p+1}")
            "dramapops" -> listOf("$base/api/drama/popular?limit=20&language=en", "$base/api/homepage?language=en", "$base/api/drama/trending?limit=20&language=en")
            "dramarush" -> listOf("$base/api/v1/tabs/0?lang=id", "$base/api/v1/tabs/1?lang=id", "$base/api/v1/tabs/2?lang=id")
            "flareflow" -> listOf("$base/api/popular/list?language=en", "$base/api/home?tab=1&language=en", "$base/api/foryou/list?page=$p&pageSize=20&language=en")
            "flextv" -> listOf("$base/api/v1/tabs/1?lang=id&page=$p", "$base/api/v1/tabs/2?lang=id&page=$p", "$base/api/v1/tabs/3?lang=id&page=$p")
            "flickreels" -> listOf("$base/api/v1/for-you?page=$p&page_size=6&lang=id", "$base/api/v1/hot-rank?lang=id", "$base/api/v1/for-you?page=${p+1}&page_size=6&lang=id")
            "goodshort" -> listOf("$base/api/v1/home?channelId=656&page=$p&pageSize=20&index=", "$base/api/v1/trending?page=$p&pageSize=20&index=&tags=550,575", "$base/api/v1/ranking?type=0&page=$p&pageSize=20")
            "happyshort" -> listOf("$base/home?lang=id", "$base/ranking?lang=id", "$base/home?lang=id&page=$p")
            "idrama" -> listOf("$base/api/v1/popular?page=$p&limit=20&lang=id", "$base/api/v1/latest?page=$p&limit=20&lang=id", "$base/api/v1/latest?page=${p+1}&limit=20&lang=id")
            "luminareels" -> listOf("$base/api/drama/list?page=$p&page_size=20&lang=id", "$base/api/drama/list?page=${p+1}&page_size=20&lang=id", "$base/api/drama/list?page=${p+2}&page_size=20&lang=id")
            "meloshort" -> listOf("$base/api/v1/dramas/top?lang=id", "$base/api/v1/dramas/discover?page=$p&limit=20&lang=id", "$base/api/v1/drama/all?page=$p&limit=20&lang=id")
            "microdrama" -> listOf("$base/api/v1/dramas?page=$p&limit=50&lang=id", "$base/api/v1/dramas?page=${p+1}&limit=50&lang=id", "$base/api/v1/dramas?page=${p+2}&limit=50&lang=id")
            "minishort" -> listOf("$base/api/home_recommend?language=en", "$base/api/catalog?module_type=8&page_index=$p&page_size=20&language=en", "$base/api/catalog?module_type=8&page_index=${p+1}&page_size=20&language=en")
            "moboreels" -> listOf("$base/api/hotList?langId=3", "$base/api/hotList?langId=3&type=11", "$base/api/guessYouLike?langId=3")
            "playlet" -> listOf("$base/shorts/recommend?language=en", "$base/home/columns?language=en", "$base/column?columnTypeId=1&language=en")
            "radreels" -> listOf("$base/api/v1/home?lang=id", "$base/api/v1/ranking?lang=id", "$base/api/v1/foryou?page=$p&lang=id")
            "rapidtv" -> listOf("$base/api/v1/dramas?page=$p&size=20&lang=id", "$base/api/v1/dramas?page=${p+1}&size=20&lang=id", "$base/api/v1/dramas?page=${p+2}&size=20&lang=id")
            "raptdrama" -> listOf("$base/open/recommend?page=$p&lang=en-US", "$base/open/trending?lang=en-US", "$base/open/new?lang=en-US")
            "reelbuzz" -> listOf("$base/api/drama/module/recommend?pageNum=$p&pageSize=20&language=id", "$base/api/drama/list?pageNum=$p&pageSize=20&language=id", "$base/api/drama/display?pageNum=$p&pageSize=20&language=id")
            "sereal" -> listOf("$base/api/index/home/1?page=$p&lang=id", "$base/api/index/for-you/1?page=$p&lang=id", "$base/api/index/trending?lang=id")
            "shortbox" -> listOf("$base/api/list?page=$p&page_size=20&languages=id&streamable=true", "$base/api/new-list?page=$p&page_size=20&languages=id", "$base/api/recommend?languages=id&streamable=true")
            "shorten" -> listOf("$base/api/v1/exclusive?page=$p&perPage=20", "$base/api/v1/dubbed?page=$p&perPage=20", "$base/api/v1/releases?page=$p&perPage=20")
            "shortmax" -> listOf("$base/api/v1/feed/recommend?lang=id", "$base/api/v1/home?tab=1&lang=id", "$base/api/v1/feed/new?lang=id")
            "shortsky" -> listOf("$base/api/home?lang=id_id", "$base/api/recommend?lang=id_id", "$base/api/home?lang=id_id&page=$p")
            "shortwave" -> listOf("$base/api/rankings?lang=id", "$base/api/top?lang=id", "$base/api/all?lang=id")
            "shotshort" -> listOf("$base/api/popular?page=$p&lang=id", "$base/api/popular?page=${p+1}&lang=id", "$base/api/popular?page=${p+2}&lang=id")
            "snackshort" -> listOf("$base/api/v1/browsing?page=$p&pageSize=20&lang=Indonesian", "$base/api/v1/home?lang=Indonesian", "$base/api/v1/tabs?lang=Indonesian")
            "sodareels" -> listOf("$base/api/v1/home?page=$p&count=20&lang=id", "$base/api/v1/home?page=${p+1}&count=20&lang=id", "$base/api/v1/home?page=${p+2}&count=20&lang=id")
            "starshort" -> listOf("$base/api/v1/dramas?lang=4", "$base/api/v1/dramas/new?lang=3", "$base/api/v1/dramas?lang=4&page=${p+1}")
            "storyreel" -> listOf("$base/api/for-you?page_size=20&lang=in", "$base/api/popular?page=$p&page_size=20&lang=in", "$base/api/search/ranking?page=$p&page_size=20&lang=in")
            "vibeshort" -> listOf("$base/api/home/ranking?lang=en", "$base/api/foryou/page?page=$p&pageSize=20&lang=en", "$base/api/foryou/page?page=${p+1}&pageSize=20&lang=en")
            "wetv" -> listOf("$base/api/feed?channel_id=1001&lang=id&country=ID", "$base/api/feed?channel_id=1002&lang=id&country=ID", "$base/api/feed?channel_id=1003&lang=id&country=ID")
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
            "happyshort" -> "$base/drama/$encId"
            "idrama" -> "$base/api/v1/drama/$encId?lang=id"
            "luminareels" -> "$base/api/drama/detail?id=$encId&lang=en"
            "meloshort" -> "$base/api/v1/dramas/$encId/episodes?lang=id"
            "microdrama" -> "$base/api/v1/dramas/$encId"
            "minishort" -> "$base/api/video_info?video_id=$encId&language=en"
            "moboreels" -> "$base/api/seriesDetail?seriesId=$encId&langId=3"
            "playlet" -> "$base/theater/$encId?language=en"
            "radreels" -> "$base/api/v1/drama/$encId?lang=id"
            "rapidtv" -> "$base/api/v1/dramas/$encId?lang=id"
            "raptdrama" -> "$base/video/videoinfo?vid=$encId&lang=en-US"
            "reelbuzz" -> "$base/api/drama/detail?dramaId=$encId&language=id"
            "sereal" -> "$base/api/content/detail?contentId=$encId&lang=id"
            "shortbox" -> "$base/api/info/$encId?sort=1&languages=id"
            "shorten" -> "$base/api/v1/series/$encId"
            "shortmax" -> "$base/api/v1/detail/$encId?lang=id"
            "shortsky" -> "$base/api/drama/$encId?lang=id_id"
            "shortwave" -> "$base/api/drama/$encId?lang=id"
            "shotshort" -> "$base/api/book/$encId?lang=id"
            "snackshort" -> "$base/api/v1/book/$encId?lang=Indonesian"
            "sodareels" -> "$base/api/v1/drama/$encId"
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
            "dramarush" -> "$base/api/v1/ranking?lang=id"
            "flareflow" -> "$base/api/search?keyword=$encQ&page=1&language=en"
            "flextv" -> "$base/api/v1/search?q=$encQ&page=1&lang=id"
            "flickreels" -> "$base/api/v1/search-rank?lang=id"
            "goodshort" -> "$base/api/v1/search?q=$encQ"
            "happyshort" -> "$base/search?keyword=$encQ"
            "idrama" -> "$base/api/v1/search?q=$encQ&page_size=20&lang=id"
            "luminareels" -> "$base/api/drama/search?q=$encQ&page=1&page_size=20&lang=en"
            "meloshort" -> "$base/api/v1/dramas/search?q=$encQ&lang=id"
            "microdrama" -> "$base/api/v1/dramas/search?q=$encQ&lang=id"
            "moboreels" -> "$base/api/search?searchKey=$encQ&pageNo=1&pageSize=20&langId=3"
            "playlet" -> "$base/search?keyword=$encQ&language=en&page=1&pageSize=20"
            "radreels" -> "$base/api/v1/search/$encQ?page=1&lang=id"
            "rapidtv" -> "$base/api/v1/search?q=$encQ&page=1&size=20&lang=id"
            "raptdrama" -> "$base/open/search?keyword=$encQ&page=1&lang=en-US"
            "reelbuzz" -> "$base/api/search?keyword=$encQ&language=id"
            "sereal" -> "$base/api/index/search/$encQ?lang=id"
            "shortbox" -> "$base/api/hot-search?languages=id"
            "shortmax" -> "$base/api/v1/search?q=$encQ&lang=id&page=1"
            "shortsky" -> "$base/api/search?q=$encQ&page=1&lang=id_id"
            "shortwave" -> "$base/api/search/$encQ?lang=id"
            "shotshort" -> "$base/api/search?q=$encQ&page=1&lang=id"
            "snackshort" -> "$base/api/v1/search?q=$encQ&page=1&limit=20&lang=Indonesian"
            "sodareels" -> "$base/api/v1/search?q=$encQ&lang=id"
            "starshort" -> "$base/api/v1/dramas/search?q=$encQ&lang=4"
            "storyreel" -> "$base/api/search/ranking?page=1&page_size=20&lang=in"
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
            "dramapops" -> "$base/api/drama/$encId/$ep?quality=id&language=en"
            "dramarush" -> "$base/api/v1/play/$encId/$ep?lang=id"
            "flareflow" -> "$base/api/drama/$encId/play?ep=$ep&language=en"
            "flextv" -> "$base/api/v1/play/$encId/$ep?lang=id"
            "flickreels" -> "$base/api/v1/stream/$encId/$ep?lang=id"
            "happyshort" -> "$base/drama/$encId/play/$ep"
            "idrama" -> "$base/api/v1/drama/$encId/play?episode=$ep&lang=id"
            "luminareels" -> "$base/api/stream/$encId/$ep/master.m3u8?lang=en"
            "meloshort" -> "$base/api/v1/dramas/$encId/episodes/$ep?lang=id"
            "minishort" -> "$base/api/episodes_content?video_id=$encId&episodes_id=431426&language=en"
            "moboreels" -> "$base/api/video?langId=3"
            "playlet" -> "$base/theater/$encId/open/$ep/playlist.m3u8"
            "radreels" -> "$base/api/v1/video/$encId/$ep?lang=id"
            "raptdrama" -> "$base/open/classify_video?lang=en-US"
            "reelbuzz" -> "$base/api/drama/display?pageNum=1&pageSize=10&language=id"
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
            "wetv" -> "$base/api/play?vid=$encId&cid=$encId&defn=hd&lang=id&country=ID"
            else -> null
        }
    }
}