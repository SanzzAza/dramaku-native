package com.dramaku.app.catalog

import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder

/**
 * ShotShort catalog untuk endpoint dramahub.be/shotshort
 *
 * Auto-generated dari docs: 8 endpoints
 * Base URL: https://dramahub.be/shotshort
 * Token: Bearer 15693e658f723c5b4c45900a5d045ef0ab6a053ecda4dadb831c68fef773ba5e
 *
 * - GET /api/languages // List of 13 supported languages []
 * - GET /api/popular // Popular dramas [page, lang]
 * - GET /api/search // Search dramas [q!, page, lang]
 * - GET /api/book/:id // Drama detail with episodes [id!, lang]
 * - GET /api/book/:id/episodes // List all episodes with chapterId [id!, lang]
 * - GET /api/book/:bookId/chapter/:chapterId // Chapter video URL with auto-fetch subtitles [bookId!, chapterId!, lang]
 * - GET /api/category/list // Category list [lang]
 * - GET /api/category // Category content (case-sensitive: Romance, Urban, Mafia) [category, page, lang]
 */
object ShotShortCatalog {

    const val BASE = "/shotshort"
    const val BASE_URL = "https://dramahub.be/shotshort"

    const val LISTOF13SUPPORTEDLANGUAGES_PATH = "api/languages"
    fun popularDramasPath(page: Int = 1, lang: String = "id"): String = "api/popular?page=${page}&lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/popular // Popular dramas
    fun searchDramasPath(q: String, page: Int = 1, lang: String = "id"): String = "api/search?q=${URLEncoder.encode(q, "UTF-8")}&page=${page}&lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/search // Search dramas
    fun dramaDetailWithEpisodesPath(id: String, lang: String = "id"): String = "api/book/${URLEncoder.encode(id, "UTF-8")}?lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/book/:id // Drama detail with episodes
    fun listAllEpisodesWithChapteridPath(id: String, lang: String = "id"): String = "api/book/${URLEncoder.encode(id, "UTF-8")}/episodes?lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/book/:id/episodes // List all episodes with chapterId
    fun chapterVideoUrlWithAutoFetchSubtitlesPath(bookId: String, chapterId: String, lang: String = "id"): String = "api/book/${URLEncoder.encode(bookId, "UTF-8")}/chapter/${chapterId}?lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/book/:bookId/chapter/:chapterId // Chapter video URL with auto-fetch subtitles
    fun categoryListPath(lang: String = "id"): String = "api/category/list?lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/category/list // Category list
    fun categoryContentCaseSensitiveRomanceUrbanMafiaPath(category: String = "", page: Int = 1, lang: String = "id"): String = "api/category?category=${URLEncoder.encode(category, "UTF-8")}&page=${page}&lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/category // Category content (case-sensitive: Romance, Urban, Mafia)

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