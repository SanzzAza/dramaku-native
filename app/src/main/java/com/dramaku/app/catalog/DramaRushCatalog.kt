package com.dramaku.app.catalog

import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder

/**
 * DramaRush catalog untuk endpoint dramahub.be/dramarush
 *
 * Auto-generated dari docs: 7 endpoints
 * Base URL: https://dramahub.be/dramarush
 * Token: Bearer 15693e658f723c5b4c45900a5d045ef0ab6a053ecda4dadb831c68fef773ba5e
 *
 * - GET /api/v1/languages // Supported languages []
 * - GET /api/v1/ranking // Search ranking [lang]
 * - GET /api/v1/tabs/:id // Tab home data [id!, lang]
 * - GET /api/v1/drama/:id // Drama detail [id!, lang]
 * - GET /api/v1/search/:q // Search drama [q!, lang]
 * - GET /api/v1/play/:id // Get episodes with video URLs (paginated) [id!, page, size, lang]
 * - GET /api/v1/play/:id/:ep // Get single episode video URL [id!, ep!, lang]
 */
object DramaRushCatalog {

    const val BASE = "/dramarush"
    const val BASE_URL = "https://dramahub.be/dramarush"

    const val SUPPORTEDLANGUAGES_PATH = "api/v1/languages"
    fun searchRankingPath(lang: String = "id"): String = "api/v1/ranking?lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/v1/ranking // Search ranking
    fun tabHomeDataPath(id: String, lang: String = "id"): String = "api/v1/tabs/${URLEncoder.encode(id, "UTF-8")}?lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/v1/tabs/:id // Tab home data
    fun dramaDetailPath(id: String, lang: String = "id"): String = "api/v1/drama/${URLEncoder.encode(id, "UTF-8")}?lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/v1/drama/:id // Drama detail
    fun searchDramaPath(q: String, lang: String = "id"): String = "api/v1/search/${URLEncoder.encode(q, "UTF-8")}?lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/v1/search/:q // Search drama
    fun getEpisodesWithVideoUrlsPaginatedPath(id: String, page: Int = 1, size: Int = 20, lang: String = "id"): String = "api/v1/play/${URLEncoder.encode(id, "UTF-8")}?page=${page}&size=${size}&lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/v1/play/:id // Get episodes with video URLs (paginated)
    fun getSingleEpisodeVideoUrlPath(id: String, ep: Int, lang: String = "id"): String = "api/v1/play/${URLEncoder.encode(id, "UTF-8")}/${ep}?lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/v1/play/:id/:ep // Get single episode video URL

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