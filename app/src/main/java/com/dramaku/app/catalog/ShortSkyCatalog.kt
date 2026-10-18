package com.dramaku.app.catalog

import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder

/**
 * ShortSky catalog untuk endpoint dramahub.be/shortsky
 *
 * Auto-generated dari docs: 6 endpoints
 * Base URL: https://dramahub.be/shortsky
 * Token: Bearer 15693e658f723c5b4c45900a5d045ef0ab6a053ecda4dadb831c68fef773ba5e
 *
 * - GET /api/languages // Supported languages []
 * - GET /api/home // Homepage feed [lang]
 * - GET /api/recommend // Recommended dramas [lang]
 * - GET /api/search // Search drama [q!, page, lang]
 * - GET /api/drama/:id // Drama detail [id!, lang]
 * - GET /api/drama/:id/episode/:ep // Episode video URLs (480/720/1080) [id!, ep!, lang]
 */
object ShortSkyCatalog {

    const val BASE = "/shortsky"
    const val BASE_URL = "https://dramahub.be/shortsky"

    const val SUPPORTEDLANGUAGES_PATH = "api/languages"
    fun homepageFeedPath(lang: String = "id_id"): String = "api/home?lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/home // Homepage feed
    fun recommendedDramasPath(lang: String = "id_id"): String = "api/recommend?lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/recommend // Recommended dramas
    fun searchDramaPath(q: String, page: Int = 1, lang: String = "id_id"): String = "api/search?q=${URLEncoder.encode(q, "UTF-8")}&page=${page}&lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/search // Search drama
    fun dramaDetailPath(id: String, lang: String = "id_id"): String = "api/drama/${URLEncoder.encode(id, "UTF-8")}?lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/drama/:id // Drama detail
    fun episodeVideoUrls4807201080Path(id: String, ep: Int, lang: String = "id_id"): String = "api/drama/${URLEncoder.encode(id, "UTF-8")}/episode/${ep}?lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/drama/:id/episode/:ep // Episode video URLs (480/720/1080)

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