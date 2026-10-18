package com.dramaku.app.catalog

import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder

/**
 * RapidTV catalog untuk endpoint dramahub.be/rapidtv
 *
 * Auto-generated dari docs: 5 endpoints
 * Base URL: https://dramahub.be/rapidtv
 * Token: Bearer 15693e658f723c5b4c45900a5d045ef0ab6a053ecda4dadb831c68fef773ba5e
 *
 * - GET /api/v1/languages // Available languages []
 * - GET /api/v1/dramas // List dramas [page, size, lang]
 * - GET /api/v1/dramas/:id // Drama detail + episodes [id!, lang]
 * - GET /api/v1/dramas/:id/episodes // All episodes with video URLs [id!, lang]
 * - GET /api/v1/search // Search drama [q!, page, size, lang]
 */
object RapidTVCatalog {

    const val BASE = "/rapidtv"
    const val BASE_URL = "https://dramahub.be/rapidtv"

    const val AVAILABLELANGUAGES_PATH = "api/v1/languages"
    fun listDramasPath(page: Int = 1, size: Int = 20, lang: String = "id"): String = "api/v1/dramas?page=${page}&size=${size}&lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/v1/dramas // List dramas
    fun dramaDetailEpisodesPath(id: String, lang: String = "id"): String = "api/v1/dramas/${URLEncoder.encode(id, "UTF-8")}?lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/v1/dramas/:id // Drama detail + episodes
    fun allEpisodesWithVideoUrlsPath(id: String, lang: String = "id"): String = "api/v1/dramas/${URLEncoder.encode(id, "UTF-8")}/episodes?lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/v1/dramas/:id/episodes // All episodes with video URLs
    fun searchDramaPath(q: String, page: Int = 1, size: Int = 20, lang: String = "id"): String = "api/v1/search?q=${URLEncoder.encode(q, "UTF-8")}&page=${page}&size=${size}&lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/v1/search // Search drama

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