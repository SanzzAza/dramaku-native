package com.dramaku.app.catalog

import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder

/**
 * RadReels catalog untuk endpoint dramahub.be/radreels
 *
 * Auto-generated dari docs: 9 endpoints
 * Base URL: https://dramahub.be/radreels
 * Token: Bearer 15693e658f723c5b4c45900a5d045ef0ab6a053ecda4dadb831c68fef773ba5e
 *
 * - GET /api/v1/languages // List available languages []
 * - GET /api/v1/home // Homepage [lang]
 * - GET /api/v1/tab/:id // Tab content [id!, page, size, lang]
 * - GET /api/v1/search/:query // Search drama [query!, page, lang]
 * - GET /api/v1/drama/:keyword // Drama detail [keyword!, page, lang]
 * - GET /api/v1/episodes/:fakeId // All episodes [fakeId!, lang]
 * - GET /api/v1/video/:videoFakeId/:episodicDramaId // Video URL [videoFakeId!, episodicDramaId!, lang]
 * - GET /api/v1/ranking // Ranking [lang]
 * - GET /api/v1/foryou // For You feed [page, lang]
 */
object RadReelsCatalog {

    const val BASE = "/radreels"
    const val BASE_URL = "https://dramahub.be/radreels"

    const val LISTAVAILABLELANGUAGES_PATH = "api/v1/languages"
    fun homepagePath(lang: String = "id"): String = "api/v1/home?lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/v1/home // Homepage
    fun tabContentPath(id: String, page: Int = 1, size: Int = 20, lang: String = "id"): String = "api/v1/tab/${URLEncoder.encode(id, "UTF-8")}?page=${page}&size=${size}&lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/v1/tab/:id // Tab content
    fun searchDramaPath(query: String, page: Int = 1, lang: String = "id"): String = "api/v1/search/${URLEncoder.encode(query, "UTF-8")}?page=${page}&lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/v1/search/:query // Search drama
    fun dramaDetailPath(keyword: String, page: Int = 1, lang: String = "id"): String = "api/v1/drama/${URLEncoder.encode(keyword, "UTF-8")}?page=${page}&lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/v1/drama/:keyword // Drama detail
    fun allEpisodesPath(fakeId: String, lang: String = "id"): String = "api/v1/episodes/${URLEncoder.encode(fakeId, "UTF-8")}?lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/v1/episodes/:fakeId // All episodes
    fun videoUrlPath(videoFakeId: String, episodicDramaId: String, lang: String = "id"): String = "api/v1/video/${URLEncoder.encode(videoFakeId, "UTF-8")}/${URLEncoder.encode(episodicDramaId, "UTF-8")}?lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/v1/video/:videoFakeId/:episodicDramaId // Video URL
    fun rankingPath(lang: String = "id"): String = "api/v1/ranking?lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/v1/ranking // Ranking
    fun forYouFeedPath(page: Int = 1, lang: String = "id"): String = "api/v1/foryou?page=${page}&lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/v1/foryou // For You feed

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