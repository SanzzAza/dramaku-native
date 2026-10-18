package com.dramaku.app.catalog

import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder

/**
 * DramaBite catalog untuk endpoint dramahub.be/dramabite
 *
 * Auto-generated dari docs: 9 endpoints
 * Base URL: https://dramahub.be/dramabite
 * Token: Bearer 15693e658f723c5b4c45900a5d045ef0ab6a053ecda4dadb831c68fef773ba5e
 *
 * - GET /api/v1/languages // List supported languages []
 * - GET /api/v1/dramas // Drama list [lang, page]
 * - GET /api/v1/foryou // For You recommendations [lang, page]
 * - GET /api/v1/hot // Hot/trending keywords []
 * - GET /api/v1/drama/:id // Drama detail with episodes [id!, lang]
 * - GET /api/v1/drama/:id/likes // Drama like count [id!, lang]
 * - GET /api/v1/drama/:id/episode/:ep // Episode video URL [id!, ep!, lang]
 * - GET /api/v1/search // Search drama (gRPC server-side) [q!, lang, limit]
 * - GET /api/v1/recommend // Homepage recommendations [lang, page]
 */
object DramaBiteCatalog {

    const val BASE = "/dramabite"
    const val BASE_URL = "https://dramahub.be/dramabite"

    const val LISTSUPPORTEDLANGUAGES_PATH = "api/v1/languages"
    fun dramaListPath(lang: String = "id", page: Int = 1): String = "api/v1/dramas?lang=${URLEncoder.encode(lang, "UTF-8")}&page=${page}"
    // GET /api/v1/dramas // Drama list
    fun forYouRecommendationsPath(lang: String = "id", page: Int = 1): String = "api/v1/foryou?lang=${URLEncoder.encode(lang, "UTF-8")}&page=${page}"
    // GET /api/v1/foryou // For You recommendations
    const val HOTTRENDINGKEYWORDS_PATH = "api/v1/hot"
    fun dramaDetailWithEpisodesPath(id: String, lang: String = "id"): String = "api/v1/drama/${URLEncoder.encode(id, "UTF-8")}?lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/v1/drama/:id // Drama detail with episodes
    fun dramaLikeCountPath(id: String, lang: String = "id"): String = "api/v1/drama/${URLEncoder.encode(id, "UTF-8")}/likes?lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/v1/drama/:id/likes // Drama like count
    fun episodeVideoUrlPath(id: String, ep: Int, lang: String = "id"): String = "api/v1/drama/${URLEncoder.encode(id, "UTF-8")}/episode/${ep}?lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/v1/drama/:id/episode/:ep // Episode video URL
    fun searchDramaGrpcServerSidePath(q: String, lang: String = "id", limit: Int = 20): String = "api/v1/search?q=${URLEncoder.encode(q, "UTF-8")}&lang=${URLEncoder.encode(lang, "UTF-8")}&limit=${limit}"
    // GET /api/v1/search // Search drama (gRPC server-side)
    fun homepageRecommendationsPath(lang: String = "id", page: Int = 1): String = "api/v1/recommend?lang=${URLEncoder.encode(lang, "UTF-8")}&page=${page}"
    // GET /api/v1/recommend // Homepage recommendations

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