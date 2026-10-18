package com.dramaku.app.catalog

import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder

/**
 * DotDrama catalog untuk endpoint dramahub.be/dotdrama
 *
 * Auto-generated dari docs: 4 endpoints
 * Base URL: https://dramahub.be/dotdrama
 * Token: Bearer 15693e658f723c5b4c45900a5d045ef0ab6a053ecda4dadb831c68fef773ba5e
 *
 * - GET /api/v1/languages // List supported languages []
 * - GET /api/v1/dramas // Drama list [page, limit, lang]
 * - GET /api/v1/collections // Collections (hot, for you, etc) [lang]
 * - GET /api/v1/dramas/:id // Drama detail + episodes with video URL [id!]
 */
object DotDramaCatalog {

    const val BASE = "/dotdrama"
    const val BASE_URL = "https://dramahub.be/dotdrama"

    const val LISTSUPPORTEDLANGUAGES_PATH = "api/v1/languages"
    fun dramaListPath(page: Int = 1, limit: Int = 50, lang: String = "id"): String = "api/v1/dramas?page=${page}&limit=${limit}&lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/v1/dramas // Drama list
    fun collectionsHotForYouEtcPath(lang: String = "id"): String = "api/v1/collections?lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/v1/collections // Collections (hot, for you, etc)
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