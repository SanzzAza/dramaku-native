package com.dramaku.app.catalog

import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder

/**
 * ShortMax catalog untuk endpoint dramahub.be/shortmax
 *
 * Auto-generated dari docs: 10 endpoints
 * Base URL: https://dramahub.be/shortmax
 * Token: Bearer 15693e658f723c5b4c45900a5d045ef0ab6a053ecda4dadb831c68fef773ba5e
 *
 * - GET /api/v1/search // Search drama [q!, lang, page]
 * - GET /api/v1/home // Homepage by tab [tab, lang]
 * - GET /api/v1/feed/recommend // Tab Rekomendasi [lang]
 * - GET /api/v1/feed/new // Tab Baru [lang]
 * - GET /api/v1/feed/ranked // Tab Peringkat (3 section) [lang]
 * - GET /api/v1/feed/epic // Tab Dunia Epic [lang]
 * - GET /api/v1/foryou // For You feed [page, lang]
 * - GET /api/v1/detail/:code // Drama detail [code!, lang]
 * - GET /api/v1/play/:code // Episode video URL (VIP) [code!, ep!, lang]
 * - GET /api/v1/languages // Supported languages []
 */
object ShortMaxCatalog {

    const val BASE = "/shortmax"
    const val BASE_URL = "https://dramahub.be/shortmax"

    fun searchDramaPath(q: String, lang: String = "id", page: Int = 1): String = "api/v1/search?q=${URLEncoder.encode(q, "UTF-8")}&lang=${URLEncoder.encode(lang, "UTF-8")}&page=${page}"
    // GET /api/v1/search // Search drama
    fun homepageByTabPath(tab: Int = 1, lang: String = "id"): String = "api/v1/home?tab=${tab}&lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/v1/home // Homepage by tab
    fun tabRekomendasiPath(lang: String = "id"): String = "api/v1/feed/recommend?lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/v1/feed/recommend // Tab Rekomendasi
    fun tabBaruPath(lang: String = "id"): String = "api/v1/feed/new?lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/v1/feed/new // Tab Baru
    fun tabPeringkat3SectionPath(lang: String = "id"): String = "api/v1/feed/ranked?lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/v1/feed/ranked // Tab Peringkat (3 section)
    fun tabDuniaEpicPath(lang: String = "id"): String = "api/v1/feed/epic?lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/v1/feed/epic // Tab Dunia Epic
    fun forYouFeedPath(page: Int = 1, lang: String = "id"): String = "api/v1/foryou?page=${page}&lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/v1/foryou // For You feed
    fun dramaDetailPath(code: String, lang: String = "id"): String = "api/v1/detail/${URLEncoder.encode(code, "UTF-8")}?lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/v1/detail/:code // Drama detail
    fun episodeVideoUrlVipPath(code: String, ep: Int, lang: String = "id"): String = "api/v1/play/${URLEncoder.encode(code, "UTF-8")}?ep=${ep}&lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/v1/play/:code // Episode video URL (VIP)
    const val SUPPORTEDLANGUAGES_PATH = "api/v1/languages"

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