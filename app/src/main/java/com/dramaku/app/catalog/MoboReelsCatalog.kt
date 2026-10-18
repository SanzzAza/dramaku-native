package com.dramaku.app.catalog

import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder

/**
 * MoboReels catalog untuk endpoint dramahub.be/moboreels
 *
 * Auto-generated dari docs: 10 endpoints
 * Base URL: https://dramahub.be/moboreels
 * Token: Bearer 15693e658f723c5b4c45900a5d045ef0ab6a053ecda4dadb831c68fef773ba5e
 *
 * - GET /api/languages // List 19 supported languages (langId mapping) []
 * - GET /api/channelList // List of main channels/categories [langId]
 * - GET /api/channelDetail // Channel detail + series list per section [schemeId!, channelId!, skipSeries, langId]
 * - GET /api/hotList // List of hot/trending dramas (10=Trending, 11=Latest) [listId!, skipSeries, schemeId, langId]
 * - GET /api/seriesDetail // Drama detail (title, cover, genre, episodes, status) [seriesId!, langId]
 * - GET /api/seriesPage // All series episodes (auto-paginate from seriesDetail) [seriesId!, langId]
 * - GET /api/guessYouLike // Drama rekomendasi serupa [seriesId!, langId]
 * - GET /api/video // Episode video URL (auto-unlock). Referer: https://www.cdreader.com/ [seriesId!, episNum!, langId]
 * - GET /api/proxy/subtitle // Proxy subtitle [episId!, langId]
 * - GET /api/search // Search drama by keyword (pagination support) [searchKey!, pageNo, pageSize, langId]
 */
object MoboReelsCatalog {

    const val BASE = "/moboreels"
    const val BASE_URL = "https://dramahub.be/moboreels"

    const val LIST19SUPPORTEDLANGUAGESLANGIDMAPPING_PATH = "api/languages"
    fun listOfMainChannelsCategoriesPath(langId: Int = 3): String = "api/channelList?langId=${langId}"
    // GET /api/channelList // List of main channels/categories
    fun channelDetailSeriesListPerSectionPath(schemeId: String, channelId: String, skipSeries: Int = 1, langId: Int = 3): String = "api/channelDetail?schemeId=${URLEncoder.encode(schemeId, "UTF-8")}&channelId=${URLEncoder.encode(channelId, "UTF-8")}&skipSeries=${skipSeries}&langId=${langId}"
    // GET /api/channelDetail // Channel detail + series list per section
    fun listOfHotTrendingDramas10Trending11LatestPath(listId: String, skipSeries: Int = 1, schemeId: String = "", langId: Int = 3): String = "api/hotList?listId=${URLEncoder.encode(listId, "UTF-8")}&skipSeries=${skipSeries}&schemeId=${URLEncoder.encode(schemeId, "UTF-8")}&langId=${langId}"
    // GET /api/hotList // List of hot/trending dramas (10=Trending, 11=Latest)
    fun dramaDetailTitleCoverGenreEpisodesStatusPath(seriesId: String, langId: Int = 3): String = "api/seriesDetail?seriesId=${URLEncoder.encode(seriesId, "UTF-8")}&langId=${langId}"
    // GET /api/seriesDetail // Drama detail (title, cover, genre, episodes, status)
    fun allSeriesEpisodesAutoPaginateFromSeriesdetailPath(seriesId: String, langId: Int = 3): String = "api/seriesPage?seriesId=${URLEncoder.encode(seriesId, "UTF-8")}&langId=${langId}"
    // GET /api/seriesPage // All series episodes (auto-paginate from seriesDetail)
    fun dramaRekomendasiSerupaPath(seriesId: String, langId: Int = 3): String = "api/guessYouLike?seriesId=${URLEncoder.encode(seriesId, "UTF-8")}&langId=${langId}"
    // GET /api/guessYouLike // Drama rekomendasi serupa
    fun episodeVideoUrlAutoUnlockRefererHttpsWwwCdreaderComPath(seriesId: String, episNum: String, langId: Int = 3): String = "api/video?seriesId=${URLEncoder.encode(seriesId, "UTF-8")}&episNum=${URLEncoder.encode(episNum, "UTF-8")}&langId=${langId}"
    // GET /api/video // Episode video URL (auto-unlock). Referer: https://www.cdreader.com/
    fun proxySubtitlePath(episId: String, langId: Int = 3): String = "api/proxy/subtitle?episId=${URLEncoder.encode(episId, "UTF-8")}&langId=${langId}"
    // GET /api/proxy/subtitle // Proxy subtitle
    fun searchDramaByKeywordPaginationSupportPath(searchKey: String, pageNo: Int = 1, pageSize: Int = 20, langId: Int = 3): String = "api/search?searchKey=${URLEncoder.encode(searchKey, "UTF-8")}&pageNo=${pageNo}&pageSize=${pageSize}&langId=${langId}"
    // GET /api/search // Search drama by keyword (pagination support)

    // Helper generic untuk parsing list dr balasan dramahub.be
    private fun JSONArray.objects(): List<JSONObject> = (0 until length()).mapNotNull { optJSONObject(it) }

    private fun JSONObject.stringAny(vararg keys: String): String {
        keys.forEach { k ->
            val v = opt(k)
            if (v != null && v != JSONObject.NULL && v !is JSONObject && v !is JSONArray) {
                val s = v.toString().trim(); if (s.isNotBlank()) return s
            }
        }
        return ""
    }
}