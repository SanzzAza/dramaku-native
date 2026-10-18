package com.dramaku.app.catalog

import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder

/**
 * MeloShort catalog untuk endpoint dramahub.be/meloshort
 *
 * Auto-generated dari docs: 7 endpoints
 * Base URL: https://dramahub.be/meloshort
 * Token: Bearer 15693e658f723c5b4c45900a5d045ef0ab6a053ecda4dadb831c68fef773ba5e
 *
 * - GET /api/v1/languages // Available languages []
 * - GET /api/v1/drama/all // Drama list [page, limit, lang]
 * - GET /api/v1/dramas/discover // Discover dramas [page, limit, lang]
 * - GET /api/v1/dramas/top // Top ranked dramas [lang]
 * - GET /api/v1/dramas/search // Search drama [q!, lang]
 * - GET /api/v1/dramas/:id/episodes // Episode list [id!, page, limit, lang]
 * - GET /api/v1/dramas/:id/episodes/:chapter // Episode video URL [id!, chapter!, lang]
 */
object MeloShortCatalog {

    const val BASE = "/meloshort"
    const val BASE_URL = "https://dramahub.be/meloshort"

    const val AVAILABLELANGUAGES_PATH = "api/v1/languages"
    fun dramaListPath(page: Int = 1, limit: Int = 20, lang: String = "id"): String = "api/v1/drama/all?page=${page}&limit=${limit}&lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/v1/drama/all // Drama list
    fun discoverDramasPath(page: Int = 1, limit: Int = 20, lang: String = "id"): String = "api/v1/dramas/discover?page=${page}&limit=${limit}&lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/v1/dramas/discover // Discover dramas
    fun topRankedDramasPath(lang: String = "id"): String = "api/v1/dramas/top?lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/v1/dramas/top // Top ranked dramas
    fun searchDramaPath(q: String, lang: String = "id"): String = "api/v1/dramas/search?q=${URLEncoder.encode(q, "UTF-8")}&lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/v1/dramas/search // Search drama
    fun episodeListPath(id: String, page: Int = 1, limit: Int = 100, lang: String = "id"): String = "api/v1/dramas/${URLEncoder.encode(id, "UTF-8")}/episodes?page=${page}&limit=${limit}&lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/v1/dramas/:id/episodes // Episode list
    fun episodeVideoUrlPath(id: String, chapter: String, lang: String = "id"): String = "api/v1/dramas/${URLEncoder.encode(id, "UTF-8")}/episodes/${URLEncoder.encode(chapter, "UTF-8")}?lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/v1/dramas/:id/episodes/:chapter // Episode video URL

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