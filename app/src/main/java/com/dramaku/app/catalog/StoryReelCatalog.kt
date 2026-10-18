package com.dramaku.app.catalog

import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder

/**
 * StoryReel catalog untuk endpoint dramahub.be/storyreel
 *
 * Auto-generated dari docs: 7 endpoints
 * Base URL: https://dramahub.be/storyreel
 * Token: Bearer 15693e658f723c5b4c45900a5d045ef0ab6a053ecda4dadb831c68fef773ba5e
 *
 * - GET /api/languages // Daftar bahasa tersedia []
 * - GET /api/for-you // For You feed [page_size, lang]
 * - GET /api/popular // Drama populer [page, page_size, lang]
 * - GET /api/search/ranking // Search ranking [page, page_size, lang]
 * - GET /api/search // Cari drama (alias: keyword, search_text) [q!, page, page_size, lang]
 * - GET /api/drama/:short_play_code // Detail drama + episode [short_play_code!, lang]
 * - GET /api/play // URL streaming episode [short_play_code, episode_num, lang]
 */
object StoryReelCatalog {

    const val BASE = "/storyreel"
    const val BASE_URL = "https://dramahub.be/storyreel"

    const val DAFTARBAHASATERSEDIA_PATH = "api/languages"
    fun forYouFeedPath(page_size: Int = 20, lang: String = "in"): String = "api/for-you?page_size=${page_size}&lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/for-you // For You feed
    fun dramaPopulerPath(page: Int = 1, page_size: Int = 20, lang: String = "in"): String = "api/popular?page=${page}&page_size=${page_size}&lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/popular // Drama populer
    fun searchRankingPath(page: Int = 1, page_size: Int = 20, lang: String = "in"): String = "api/search/ranking?page=${page}&page_size=${page_size}&lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/search/ranking // Search ranking
    fun cariDramaAliasKeywordSearchTextPath(q: String, page: Int = 1, page_size: Int = 20, lang: String = "in"): String = "api/search?q=${URLEncoder.encode(q, "UTF-8")}&page=${page}&page_size=${page_size}&lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/search // Cari drama (alias: keyword, search_text)
    fun detailDramaEpisodePath(short_play_code: String, lang: String = "in"): String = "api/drama/${URLEncoder.encode(short_play_code, "UTF-8")}?lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/drama/:short_play_code // Detail drama + episode
    fun urlStreamingEpisodePath(short_play_code: Int = 845338, episode_num: Int = 5, lang: String = "in"): String = "api/play?short_play_code=${short_play_code}&episode_num=${episode_num}&lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/play // URL streaming episode

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