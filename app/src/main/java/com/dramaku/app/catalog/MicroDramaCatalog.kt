package com.dramaku.app.catalog

import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder

/**
 * MicroDrama catalog untuk endpoint dramahub.be/microdrama
 *
 * Auto-generated dari docs: 4 endpoints
 * Base URL: https://dramahub.be/microdrama
 * Token: Bearer 15693e658f723c5b4c45900a5d045ef0ab6a053ecda4dadb831c68fef773ba5e
 *
 * - GET /api/v1/languages // List supported languages []
 * - GET /api/v1/dramas // Drama list [lang, limit]
 * - GET /api/v1/dramas/search // Search drama [q!, lang]
 * - GET /api/v1/dramas/:id // Drama detail + episodes with video URL [id!]
 */
object MicroDramaCatalog {

    const val BASE = "/microdrama"
    const val BASE_URL = "https://dramahub.be/microdrama"

    const val LISTSUPPORTEDLANGUAGES_PATH = "api/v1/languages"
    fun dramaListPath(lang: String = "id", limit: Int = 50): String = "api/v1/dramas?lang=${URLEncoder.encode(lang, "UTF-8")}&limit=${limit}"
    // GET /api/v1/dramas // Drama list
    fun searchDramaPath(q: String, lang: String = "id"): String = "api/v1/dramas/search?q=${URLEncoder.encode(q, "UTF-8")}&lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/v1/dramas/search // Search drama
    fun dramaDetailEpisodesWithVideoUrlPath(id: String): String = "api/v1/dramas/${URLEncoder.encode(id, "UTF-8")}"
    // GET /api/v1/dramas/:id // Drama detail + episodes with video URL

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