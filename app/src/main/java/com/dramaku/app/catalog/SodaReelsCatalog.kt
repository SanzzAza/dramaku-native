package com.dramaku.app.catalog

import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder

/**
 * SodaReels catalog untuk endpoint dramahub.be/sodareels
 *
 * Auto-generated dari docs: 6 endpoints
 * Base URL: https://dramahub.be/sodareels
 * Token: Bearer 15693e658f723c5b4c45900a5d045ef0ab6a053ecda4dadb831c68fef773ba5e
 *
 * - GET /api/v1/languages // Supported languages []
 * - GET /api/v1/home // Homepage feed [page, count, lang]
 * - GET /api/v1/search // Search drama [q!, lang]
 * - GET /api/v1/drama/:id // Episode video URLs (use ewash from /info) [id!]
 * - GET /api/v1/info/:id // Drama info + episode list [id!]
 * - GET /api/v1/episodes // Episode video URLs (use ewash from /info) [ids!]
 */
object SodaReelsCatalog {

    const val BASE = "/sodareels"
    const val BASE_URL = "https://dramahub.be/sodareels"

    const val SUPPORTEDLANGUAGES_PATH = "api/v1/languages"
    fun homepageFeedPath(page: Int = 1, count: Int = 20, lang: String = "id"): String = "api/v1/home?page=${page}&count=${count}&lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/v1/home // Homepage feed
    fun searchDramaPath(q: String, lang: String = "id"): String = "api/v1/search?q=${URLEncoder.encode(q, "UTF-8")}&lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/v1/search // Search drama
    fun episodeVideoUrlsUseEwashFromInfoPath(id: String): String = "api/v1/drama/${URLEncoder.encode(id, "UTF-8")}"
    // GET /api/v1/drama/:id // Episode video URLs (use ewash from /info)
    fun dramaInfoEpisodeListPath(id: String): String = "api/v1/info/${URLEncoder.encode(id, "UTF-8")}"
    // GET /api/v1/info/:id // Drama info + episode list
    fun episodeVideoUrlsUseEwashFromInfo2Path(ids: String): String = "api/v1/episodes?ids=${URLEncoder.encode(ids, "UTF-8")}"
    // GET /api/v1/episodes // Episode video URLs (use ewash from /info)

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