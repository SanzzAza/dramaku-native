package com.dramaku.app.catalog

import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder

/**
 * HappyShort catalog untuk endpoint dramahub.be/happyshort
 *
 * Auto-generated dari docs: 6 endpoints
 * Base URL: https://dramahub.be/happyshort
 * Token: Bearer 15693e658f723c5b4c45900a5d045ef0ab6a053ecda4dadb831c68fef773ba5e
 *
 * - GET /languages // Daftar bahasa tersedia []
 * - GET /home // Rekomendasi home [lang]
 * - GET /ranking // Peringkat/populer [lang]
 * - GET /search // Cari drama (alias: kw) [keyword]
 * - GET /drama/:videoId // Detail drama + daftar episode [videoId]
 * - GET /drama/:videoId/play/:ep // URL streaming episode [videoId, ep]
 */
object HappyShortCatalog {

    const val BASE = "/happyshort"
    const val BASE_URL = "https://dramahub.be/happyshort"

    const val DAFTARBAHASATERSEDIA_PATH = "languages"
    fun rekomendasiHomePath(lang: String = "en"): String = "home?lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /home // Rekomendasi home
    fun peringkatPopulerPath(lang: String = "en"): String = "ranking?lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /ranking // Peringkat/populer
    fun cariDramaAliasKwPath(keyword: String = "sea"): String = "search?keyword=${URLEncoder.encode(keyword, "UTF-8")}"
    // GET /search // Cari drama (alias: kw)
    fun detailDramaDaftarEpisodePath(videoId: Int = 350964): String = "drama/${videoId}"
    // GET /drama/:videoId // Detail drama + daftar episode
    fun urlStreamingEpisodePath(videoId: Int = 350964, ep: Int = 1): String = "drama/${videoId}/play/${ep}"
    // GET /drama/:videoId/play/:ep // URL streaming episode

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