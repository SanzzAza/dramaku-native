package com.dramaku.app.catalog

import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder

/**
 * BonusTV catalog untuk endpoint dramahub.be/bonustv
 *
 * Auto-generated dari docs: 11 endpoints
 * Base URL: https://dramahub.be/bonustv
 * Token: Bearer 15693e658f723c5b4c45900a5d045ef0ab6a053ecda4dadb831c68fef773ba5e
 *
 * - GET /api/languages // Supported languages (lokal) []
 * - GET /api/banners // Banners [lang]
 * - GET /api/tabs // Tabs list [lang]
 * - GET /api/tabs/:id // Tab content (id: 1001=All, 1002=New Dramas, 1003=...) [id!, tab_id, lang]
 * - GET /api/modules // Modules (all videos flattened, dedupe by id) [lang]
 * - GET /api/shorts // Shorts (pagination) [lang, page, page_size]
 * - GET /api/recommend // Recommend (random each call) [lang, page, page_size]
 * - GET /api/videos/recently // Recently added (full list, 20 items) [lang]
 * - GET /api/search // Search drama (q or keyword) [q, keyword, lang, page, page_size]
 * - GET /api/video/:id // Video detail with episodes [id!, video_id, lang]
 * - GET /api/stream/:id // Episode HLS stream + subtitle [id!, video_id, episode_num, resolution, lang]
 */
object BonusTVCatalog {

    const val BASE = "/bonustv"
    const val BASE_URL = "https://dramahub.be/bonustv"

    const val SUPPORTEDLANGUAGESLOKAL_PATH = "api/languages"
    fun bannersPath(lang: String = "id"): String = "api/banners?lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/banners // Banners
    fun tabsListPath(lang: String = "id"): String = "api/tabs?lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/tabs // Tabs list
    fun tabContentId1001All1002NewDramas1003Path(id: String, tab_id: String = "", lang: String = "id"): String = "api/tabs/${URLEncoder.encode(id, "UTF-8")}?tab_id=${URLEncoder.encode(tab_id, "UTF-8")}&lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/tabs/:id // Tab content (id: 1001=All, 1002=New Dramas, 1003=...)
    fun modulesAllVideosFlattenedDedupeByIdPath(lang: String = "id"): String = "api/modules?lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/modules // Modules (all videos flattened, dedupe by id)
    fun shortsPaginationPath(lang: String = "id", page: Int = 1, page_size: Int = 10): String = "api/shorts?lang=${URLEncoder.encode(lang, "UTF-8")}&page=${page}&page_size=${page_size}"
    // GET /api/shorts // Shorts (pagination)
    fun recommendRandomEachCallPath(lang: String = "id", page: Int = 1, page_size: Int = 10): String = "api/recommend?lang=${URLEncoder.encode(lang, "UTF-8")}&page=${page}&page_size=${page_size}"
    // GET /api/recommend // Recommend (random each call)
    fun recentlyAddedFullList20ItemsPath(lang: String = "id"): String = "api/videos/recently?lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/videos/recently // Recently added (full list, 20 items)
    fun searchDramaQOrKeywordPath(q: String = "", keyword: String = "", lang: String = "id", page: Int = 1, page_size: Int = 10): String = "api/search?q=${URLEncoder.encode(q, "UTF-8")}&keyword=${URLEncoder.encode(keyword, "UTF-8")}&lang=${URLEncoder.encode(lang, "UTF-8")}&page=${page}&page_size=${page_size}"
    // GET /api/search // Search drama (q or keyword)
    fun videoDetailWithEpisodesPath(id: String, video_id: String = "", lang: String = "id"): String = "api/video/${URLEncoder.encode(id, "UTF-8")}?video_id=${URLEncoder.encode(video_id, "UTF-8")}&lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/video/:id // Video detail with episodes
    fun episodeHlsStreamSubtitlePath(id: String, video_id: String = "", episode_num: Int = 1, resolution: String = "720p", lang: String = "id"): String = "api/stream/${URLEncoder.encode(id, "UTF-8")}?video_id=${URLEncoder.encode(video_id, "UTF-8")}&episode_num=${episode_num}&resolution=${URLEncoder.encode(resolution, "UTF-8")}&lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/stream/:id // Episode HLS stream + subtitle

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