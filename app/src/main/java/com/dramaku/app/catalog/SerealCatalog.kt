package com.dramaku.app.catalog

import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder

/**
 * Sereal catalog untuk endpoint dramahub.be/sereal
 *
 * Auto-generated dari docs: 9 endpoints
 * Base URL: https://dramahub.be/sereal
 * Token: Bearer 15693e658f723c5b4c45900a5d045ef0ab6a053ecda4dadb831c68fef773ba5e
 *
 * - GET /api/languages // Supported languages []
 * - GET /api/index/home/:page // Homepage feed [page!, lang]
 * - GET /api/index/for-you/:page // For You feed [page!, lang]
 * - GET /api/index/search/:q // Search drama [q!, lang]
 * - GET /api/index/trending // Trending dramas [lang]
 * - GET /api/index/content-search/:contentId/:page // Content search by ID [contentId!, page!, lang]
 * - GET /api/content/detail // Drama detail [contentId!, lang]
 * - GET /api/content/recommend-smart/:page // Smart recommendations [page!, lang]
 * - GET /api/watch/:contentId/:ep // Episode video URL [contentId!, ep!, lang]
 */
object SerealCatalog {

    const val BASE = "/sereal"
    const val BASE_URL = "https://dramahub.be/sereal"

    const val SUPPORTEDLANGUAGES_PATH = "api/languages"
    fun homepageFeedPath(page: Int, lang: String = "id"): String = "api/index/home/${page}?lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/index/home/:page // Homepage feed
    fun forYouFeedPath(page: Int, lang: String = "id"): String = "api/index/for-you/${page}?lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/index/for-you/:page // For You feed
    fun searchDramaPath(q: String, lang: String = "id"): String = "api/index/search/${URLEncoder.encode(q, "UTF-8")}?lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/index/search/:q // Search drama
    fun trendingDramasPath(lang: String = "id"): String = "api/index/trending?lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/index/trending // Trending dramas
    fun contentSearchByIdPath(contentId: String, page: Int, lang: String = "id"): String = "api/index/content-search/${URLEncoder.encode(contentId, "UTF-8")}/${page}?lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/index/content-search/:contentId/:page // Content search by ID
    fun dramaDetailPath(contentId: String, lang: String = "id"): String = "api/content/detail?contentId=${URLEncoder.encode(contentId, "UTF-8")}&lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/content/detail // Drama detail
    fun smartRecommendationsPath(page: Int, lang: String = "id"): String = "api/content/recommend-smart/${page}?lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/content/recommend-smart/:page // Smart recommendations
    fun episodeVideoUrlPath(contentId: String, ep: Int, lang: String = "id"): String = "api/watch/${URLEncoder.encode(contentId, "UTF-8")}/${ep}?lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/watch/:contentId/:ep // Episode video URL

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