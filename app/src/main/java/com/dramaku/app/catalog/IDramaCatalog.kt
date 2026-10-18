package com.dramaku.app.catalog

import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder

/**
 * IDrama catalog untuk endpoint dramahub.be/idrama
 *
 * Auto-generated dari docs: 7 endpoints
 * Base URL: https://dramahub.be/idrama
 * Token: Bearer 15693e658f723c5b4c45900a5d045ef0ab6a053ecda4dadb831c68fef773ba5e
 *
 * - GET /api/v1/search // Search dramas by keyword (lang: id, en, ja, ko, es, pt, th) [q!, page_size, lang]
 * - GET /api/v1/popular // Popular dramas [page, limit, lang]
 * - GET /api/v1/latest // Latest dramas [page, limit, lang]
 * - GET /api/v1/genres // Get all available genres [lang]
 * - GET /api/v1/genre/:id // Dramas by genre [id!, page, limit, lang]
 * - GET /api/v1/drama/:id // Drama detail with episodes [id!, lang]
 * - GET /api/v1/drama/:id/play // Video play URL (HLS 540p/720p/1080p) [id!, episode, lang]
 */
object IDramaCatalog {

    const val BASE = "/idrama"
    const val BASE_URL = "https://dramahub.be/idrama"

    fun searchDramasByKeywordLangIdEnJaKoEsPtThPath(q: String, page_size: Int = 20, lang: String = "id"): String = "api/v1/search?q=${URLEncoder.encode(q, "UTF-8")}&page_size=${page_size}&lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/v1/search // Search dramas by keyword (lang: id, en, ja, ko, es, pt, th)
    fun popularDramasPath(page: Int = 1, limit: Int = 20, lang: String = "id"): String = "api/v1/popular?page=${page}&limit=${limit}&lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/v1/popular // Popular dramas
    fun latestDramasPath(page: Int = 1, limit: Int = 20, lang: String = "id"): String = "api/v1/latest?page=${page}&limit=${limit}&lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/v1/latest // Latest dramas
    fun getAllAvailableGenresPath(lang: String = "id"): String = "api/v1/genres?lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/v1/genres // Get all available genres
    fun dramasByGenrePath(id: String, page: Int = 1, limit: Int = 20, lang: String = "id"): String = "api/v1/genre/${URLEncoder.encode(id, "UTF-8")}?page=${page}&limit=${limit}&lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/v1/genre/:id // Dramas by genre
    fun dramaDetailWithEpisodesPath(id: String, lang: String = "id"): String = "api/v1/drama/${URLEncoder.encode(id, "UTF-8")}?lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/v1/drama/:id // Drama detail with episodes
    fun videoPlayUrlHls540p720p1080pPath(id: String, episode: Int = 1, lang: String = "id"): String = "api/v1/drama/${URLEncoder.encode(id, "UTF-8")}/play?episode=${episode}&lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/v1/drama/:id/play // Video play URL (HLS 540p/720p/1080p)

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