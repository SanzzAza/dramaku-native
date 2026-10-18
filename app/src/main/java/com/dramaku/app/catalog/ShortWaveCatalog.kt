package com.dramaku.app.catalog

import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder

/**
 * ShortWave catalog untuk endpoint dramahub.be/shortwave
 *
 * Auto-generated dari docs: 8 endpoints
 * Base URL: https://dramahub.be/shortwave
 * Token: Bearer 15693e658f723c5b4c45900a5d045ef0ab6a053ecda4dadb831c68fef773ba5e
 *
 * - GET /api/set-lang/:lang // Set language. Title, tags, content change accordingly [lang!]
 * - GET /api/top // Drama trending [lang]
 * - GET /api/all // All dramas [lang]
 * - GET /api/more // Dramas with pagination [page, page_size, lang]
 * - GET /api/rankings // Ranking drama [lang]
 * - GET /api/search/:query // Cari drama [query!, lang]
 * - GET /api/drama/:dramaId // Drama detail + all episodes + cover per episode [dramaId!, lang]
 * - GET /api/stream/:dramaId/:chapterId // Stream URL (M3U8) + subtitle (WebVTT). Auto unlock jika locked [dramaId!, chapterId!, lang]
 */
object ShortWaveCatalog {

    const val BASE = "/shortwave"
    const val BASE_URL = "https://dramahub.be/shortwave"

    fun setLanguageTitleTagsContentChangeAccordinglyPath(lang: String): String = "api/set-lang/${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/set-lang/:lang // Set language. Title, tags, content change accordingly
    fun dramaTrendingPath(lang: String = ""): String = "api/top?lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/top // Drama trending
    fun allDramasPath(lang: String = ""): String = "api/all?lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/all // All dramas
    fun dramasWithPaginationPath(page: Int = 1, page_size: Int = 20, lang: String = ""): String = "api/more?page=${page}&page_size=${page_size}&lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/more // Dramas with pagination
    fun rankingDramaPath(lang: String = ""): String = "api/rankings?lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/rankings // Ranking drama
    fun cariDramaPath(query: String, lang: String = ""): String = "api/search/${URLEncoder.encode(query, "UTF-8")}?lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/search/:query // Cari drama
    fun dramaDetailAllEpisodesCoverPerEpisodePath(dramaId: String, lang: String = ""): String = "api/drama/${URLEncoder.encode(dramaId, "UTF-8")}?lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/drama/:dramaId // Drama detail + all episodes + cover per episode
    fun streamUrlM3u8SubtitleWebvttAutoUnlockJikaLockedPath(dramaId: String, chapterId: String, lang: String = ""): String = "api/stream/${URLEncoder.encode(dramaId, "UTF-8")}/${chapterId}?lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/stream/:dramaId/:chapterId // Stream URL (M3U8) + subtitle (WebVTT). Auto unlock jika locked

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