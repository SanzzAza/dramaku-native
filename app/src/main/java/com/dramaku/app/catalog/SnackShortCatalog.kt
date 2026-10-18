package com.dramaku.app.catalog

import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder

/**
 * SnackShort catalog untuk endpoint dramahub.be/snackshort
 *
 * Auto-generated dari docs: 8 endpoints
 * Base URL: https://dramahub.be/snackshort
 * Token: Bearer 15693e658f723c5b4c45900a5d045ef0ab6a053ecda4dadb831c68fef773ba5e
 *
 * - GET /api/v1/languages // List supported languages []
 * - GET /api/v1/home // Homepage feed [lang]
 * - GET /api/v1/tabs // Drama tabs [lang]
 * - GET /api/v1/browsing // Browse dramas [page, pageSize, lang]
 * - GET /api/v1/search // Search drama by keyword (q), or get search terms if no q [q, page, limit, lang]
 * - GET /api/v1/book/:bookId // Book/drama detail [bookId!, lang]
 * - GET /api/v1/book/:bookId/chapters // Chapter list [bookId!, lang]
 * - GET /api/v1/book/:bookId/episode/:chapterId // Episode video URL [bookId!, chapterId!, lang]
 */
object SnackShortCatalog {

    const val BASE = "/snackshort"
    const val BASE_URL = "https://dramahub.be/snackshort"

    const val LISTSUPPORTEDLANGUAGES_PATH = "api/v1/languages"
    fun homepageFeedPath(lang: String = "Indonesian"): String = "api/v1/home?lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/v1/home // Homepage feed
    fun dramaTabsPath(lang: String = "Indonesian"): String = "api/v1/tabs?lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/v1/tabs // Drama tabs
    fun browseDramasPath(page: Int = 1, pageSize: Int = 20, lang: String = "Indonesian"): String = "api/v1/browsing?page=${page}&pageSize=${pageSize}&lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/v1/browsing // Browse dramas
    fun searchDramaByKeywordQOrGetSearchTermsIfNoQPath(q: String = "", page: Int = 1, limit: Int = 20, lang: String = "Indonesian"): String = "api/v1/search?q=${URLEncoder.encode(q, "UTF-8")}&page=${page}&limit=${limit}&lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/v1/search // Search drama by keyword (q), or get search terms if no q
    fun bookDramaDetailPath(bookId: String, lang: String = "Indonesian"): String = "api/v1/book/${URLEncoder.encode(bookId, "UTF-8")}?lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/v1/book/:bookId // Book/drama detail
    fun chapterListPath(bookId: String, lang: String = "Indonesian"): String = "api/v1/book/${URLEncoder.encode(bookId, "UTF-8")}/chapters?lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/v1/book/:bookId/chapters // Chapter list
    fun episodeVideoUrlPath(bookId: String, chapterId: String, lang: String = "Indonesian"): String = "api/v1/book/${URLEncoder.encode(bookId, "UTF-8")}/episode/${URLEncoder.encode(chapterId, "UTF-8")}?lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/v1/book/:bookId/episode/:chapterId // Episode video URL

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