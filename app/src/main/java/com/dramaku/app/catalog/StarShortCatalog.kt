package com.dramaku.app.catalog

import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder

/**
 * StarShort catalog untuk endpoint dramahub.be/starshort
 *
 * Auto-generated dari docs: 7 endpoints
 * Base URL: https://dramahub.be/starshort
 * Token: Bearer 15693e658f723c5b4c45900a5d045ef0ab6a053ecda4dadb831c68fef773ba5e
 *
 * - GET /api/v1/languages // List supported languages (returns id:name mapping) []
 * - GET /api/v1/dramas // Popular dramas (lang: 3=English, 4=Indonesian) [lang]
 * - GET /api/v1/dramas/new // New releases [lang]
 * - GET /api/v1/dramas/search // Search drama [q!, lang]
 * - GET /api/v1/dramas/:dramaId // Drama detail [dramaId!, lang]
 * - GET /api/v1/dramas/:dramaId/episodes // Episode list [dramaId!, lang]
 * - GET /api/v1/dramas/:dramaId/episodes/:epNum // Episode video URL [dramaId!, epNum!, lang]
 */
object StarShortCatalog {

    const val BASE = "/starshort"
    const val BASE_URL = "https://dramahub.be/starshort"

    const val LISTSUPPORTEDLANGUAGESRETURNSIDNAMEMAPPING_PATH = "api/v1/languages"
    fun popularDramasLang3English4IndonesianPath(lang: String = "4"): String = "api/v1/dramas?lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/v1/dramas // Popular dramas (lang: 3=English, 4=Indonesian)
    fun newReleasesPath(lang: String = "4"): String = "api/v1/dramas/new?lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/v1/dramas/new // New releases
    fun searchDramaPath(q: String, lang: String = "4"): String = "api/v1/dramas/search?q=${URLEncoder.encode(q, "UTF-8")}&lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/v1/dramas/search // Search drama
    fun dramaDetailPath(dramaId: String, lang: String = "4"): String = "api/v1/dramas/${URLEncoder.encode(dramaId, "UTF-8")}?lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/v1/dramas/:dramaId // Drama detail
    fun episodeListPath(dramaId: String, lang: String = "4"): String = "api/v1/dramas/${URLEncoder.encode(dramaId, "UTF-8")}/episodes?lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/v1/dramas/:dramaId/episodes // Episode list
    fun episodeVideoUrlPath(dramaId: String, epNum: Int, lang: String = "4"): String = "api/v1/dramas/${URLEncoder.encode(dramaId, "UTF-8")}/episodes/${epNum}?lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/v1/dramas/:dramaId/episodes/:epNum // Episode video URL

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