package com.dramaku.app.catalog

import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder

/**
 * FlickReels catalog untuk endpoint dramahub.be/flickreels
 *
 * Auto-generated dari docs: 11 endpoints
 * Base URL: https://dramahub.be/flickreels
 * Token: Bearer 15693e658f723c5b4c45900a5d045ef0ab6a053ecda4dadb831c68fef773ba5e
 *
 * - GET /api/v1/language // Supported languages []
 * - GET /api/v1/for-you // For You feed [page, page_size, lang]
 * - GET /api/v1/hot-rank // Hot ranking [lang]
 * - GET /api/v1/navigation // Categories list [lang]
 * - GET /api/v1/category // Dramas by category [navigation_id!, page, page_size, lang]
 * - GET /api/v1/hot-keywords // Hot keywords [lang]
 * - GET /api/v1/search-rank // Search ranking [lang]
 * - GET /api/v1/search // Search drama [keyword!, lang]
 * - GET /api/v1/banners // Banners/sliders [lang]
 * - GET /api/v1/chapters/:id // Chapter list [id!, lang]
 * - GET /api/v1/stream/:playletId/:chapterId // Stream URL [playletId!, chapterId!, lang]
 */
object FlickReelsCatalog {

    const val BASE = "/flickreels"
    const val BASE_URL = "https://dramahub.be/flickreels"

    const val SUPPORTEDLANGUAGES_PATH = "api/v1/language"
    fun forYouFeedPath(page: Int = 1, page_size: Int = 6, lang: String = "id"): String = "api/v1/for-you?page=${page}&page_size=${page_size}&lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/v1/for-you // For You feed
    fun hotRankingPath(lang: String = "id"): String = "api/v1/hot-rank?lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/v1/hot-rank // Hot ranking
    fun categoriesListPath(lang: String = "id"): String = "api/v1/navigation?lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/v1/navigation // Categories list
    fun dramasByCategoryPath(navigation_id: String, page: Int = 1, page_size: Int = 20, lang: String = "id"): String = "api/v1/category?navigation_id=${URLEncoder.encode(navigation_id, "UTF-8")}&page=${page}&page_size=${page_size}&lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/v1/category // Dramas by category
    fun hotKeywordsPath(lang: String = "id"): String = "api/v1/hot-keywords?lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/v1/hot-keywords // Hot keywords
    fun searchRankingPath(lang: String = "id"): String = "api/v1/search-rank?lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/v1/search-rank // Search ranking
    fun searchDramaPath(keyword: String, lang: String = "id"): String = "api/v1/search?keyword=${URLEncoder.encode(keyword, "UTF-8")}&lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/v1/search // Search drama
    fun bannersSlidersPath(lang: String = "id"): String = "api/v1/banners?lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/v1/banners // Banners/sliders
    fun chapterListPath(id: String, lang: String = "id"): String = "api/v1/chapters/${URLEncoder.encode(id, "UTF-8")}?lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/v1/chapters/:id // Chapter list
    fun streamUrlPath(playletId: String, chapterId: String, lang: String = "id"): String = "api/v1/stream/${URLEncoder.encode(playletId, "UTF-8")}/${chapterId}?lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/v1/stream/:playletId/:chapterId // Stream URL

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