package com.dramaku.app.catalog

import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder

/**
 * VibeShort catalog untuk endpoint dramahub.be/vibeshort
 *
 * Auto-generated dari docs: 8 endpoints
 * Base URL: https://dramahub.be/vibeshort
 * Token: Bearer 15693e658f723c5b4c45900a5d045ef0ab6a053ecda4dadb831c68fef773ba5e
 *
 * - GET /api/languages // Daftar bahasa tersedia []
 * - GET /api/home/classes // Kategori home [lang]
 * - GET /api/home/ranking // Peringkat home [lang]
 * - GET /api/home/class/plays // Drama per kelas [classId!, page, pageSize, lang]
 * - GET /api/search // Cari drama [keyword!, page, pageSize, lang]
 * - GET /api/foryou/page // For You feed [page, pageSize, lang]
 * - GET /api/shortplay/:id // Detail shortplay + episode [id!]
 * - GET /api/unlock/farm/:shortPlayId/:episode // URL streaming (VIP pool, cache 2 jam) [shortPlayId!, episode!]
 */
object VibeShortCatalog {

    const val BASE = "/vibeshort"
    const val BASE_URL = "https://dramahub.be/vibeshort"

    const val DAFTARBAHASATERSEDIA_PATH = "api/languages"
    fun kategoriHomePath(lang: String = "en"): String = "api/home/classes?lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/home/classes // Kategori home
    fun peringkatHomePath(lang: String = "en"): String = "api/home/ranking?lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/home/ranking // Peringkat home
    fun dramaPerKelasPath(classId: String, page: Int = 1, pageSize: Int = 20, lang: String = "en"): String = "api/home/class/plays?classId=${URLEncoder.encode(classId, "UTF-8")}&page=${page}&pageSize=${pageSize}&lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/home/class/plays // Drama per kelas
    fun cariDramaPath(keyword: String, page: Int = 1, pageSize: Int = 20, lang: String = "en"): String = "api/search?keyword=${URLEncoder.encode(keyword, "UTF-8")}&page=${page}&pageSize=${pageSize}&lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/search // Cari drama
    fun forYouFeedPath(page: Int = 1, pageSize: Int = 10, lang: String = "en"): String = "api/foryou/page?page=${page}&pageSize=${pageSize}&lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/foryou/page // For You feed
    fun detailShortplayEpisodePath(id: String): String = "api/shortplay/${URLEncoder.encode(id, "UTF-8")}"
    // GET /api/shortplay/:id // Detail shortplay + episode
    fun urlStreamingVipPoolCache2JamPath(shortPlayId: String, episode: Int): String = "api/unlock/farm/${URLEncoder.encode(shortPlayId, "UTF-8")}/${episode}"
    // GET /api/unlock/farm/:shortPlayId/:episode // URL streaming (VIP pool, cache 2 jam)

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