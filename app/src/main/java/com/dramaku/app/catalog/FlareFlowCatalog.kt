package com.dramaku.app.catalog

import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder

/**
 * FlareFlow catalog untuk endpoint dramahub.be/flareflow
 *
 * Auto-generated dari docs: 9 endpoints
 * Base URL: https://dramahub.be/flareflow
 * Token: Bearer 15693e658f723c5b4c45900a5d045ef0ab6a053ecda4dadb831c68fef773ba5e
 *
 * - GET /api/languages // Supported languages []
 * - GET /api/home // Home feed by tab [tab, language]
 * - GET /api/classes // Genre list [language]
 * - GET /api/classes/:classId // Dramas by genre [classId!, page, pageSize, language]
 * - GET /api/popular/list // Popular dramas [language]
 * - GET /api/foryou/list // For You list [page, pageSize, language]
 * - GET /api/search // Search drama [keyword!, page, language]
 * - GET /api/drama/:id // Drama detail with episodes [id!, language]
 * - GET /api/drama/:id/play // Episode video URL (480/720/1080) [id!, ep!, language]
 */
object FlareFlowCatalog {

    const val BASE = "/flareflow"
    const val BASE_URL = "https://dramahub.be/flareflow"

    const val SUPPORTEDLANGUAGES_PATH = "api/languages"
    fun homeFeedByTabPath(tab: Int = 1, language: String = "en"): String = "api/home?tab=${tab}&language=${URLEncoder.encode(language, "UTF-8")}"
    // GET /api/home // Home feed by tab
    fun genreListPath(language: String = "en"): String = "api/classes?language=${URLEncoder.encode(language, "UTF-8")}"
    // GET /api/classes // Genre list
    fun dramasByGenrePath(classId: String, page: Int = 1, pageSize: Int = 20, language: String = "en"): String = "api/classes/${URLEncoder.encode(classId, "UTF-8")}?page=${page}&pageSize=${pageSize}&language=${URLEncoder.encode(language, "UTF-8")}"
    // GET /api/classes/:classId // Dramas by genre
    fun popularDramasPath(language: String = "en"): String = "api/popular/list?language=${URLEncoder.encode(language, "UTF-8")}"
    // GET /api/popular/list // Popular dramas
    fun forYouListPath(page: Int = 1, pageSize: Int = 20, language: String = "en"): String = "api/foryou/list?page=${page}&pageSize=${pageSize}&language=${URLEncoder.encode(language, "UTF-8")}"
    // GET /api/foryou/list // For You list
    fun searchDramaPath(keyword: String, page: Int = 1, language: String = "en"): String = "api/search?keyword=${URLEncoder.encode(keyword, "UTF-8")}&page=${page}&language=${URLEncoder.encode(language, "UTF-8")}"
    // GET /api/search // Search drama
    fun dramaDetailWithEpisodesPath(id: String, language: String = "en"): String = "api/drama/${URLEncoder.encode(id, "UTF-8")}?language=${URLEncoder.encode(language, "UTF-8")}"
    // GET /api/drama/:id // Drama detail with episodes
    fun episodeVideoUrl4807201080Path(id: String, ep: Int, language: String = "en"): String = "api/drama/${URLEncoder.encode(id, "UTF-8")}/play?ep=${ep}&language=${URLEncoder.encode(language, "UTF-8")}"
    // GET /api/drama/:id/play // Episode video URL (480/720/1080)

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