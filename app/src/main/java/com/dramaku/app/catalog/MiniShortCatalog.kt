package com.dramaku.app.catalog

import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder

/**
 * MiniShort catalog untuk endpoint dramahub.be/minishort
 *
 * Auto-generated dari docs: 6 endpoints
 * Base URL: https://dramahub.be/minishort
 * Token: Bearer 15693e658f723c5b4c45900a5d045ef0ab6a053ecda4dadb831c68fef773ba5e
 *
 * - GET /api/languages // Daftar bahasa tersedia []
 * - GET /api/home_recommend // Rekomendasi home [language]
 * - GET /api/modules // Daftar modul kategori [language]
 * - GET /api/catalog // Katalog drama per modul [module_type, page_index, page_size, language]
 * - GET /api/video_info // Detail video + daftar episode [video_id, language]
 * - GET /api/episodes_content // URL streaming episode [video_id, episodes_id, language]
 */
object MiniShortCatalog {

    const val BASE = "/minishort"
    const val BASE_URL = "https://dramahub.be/minishort"

    const val DAFTARBAHASATERSEDIA_PATH = "api/languages"
    fun rekomendasiHomePath(language: String = "en"): String = "api/home_recommend?language=${URLEncoder.encode(language, "UTF-8")}"
    // GET /api/home_recommend // Rekomendasi home
    fun daftarModulKategoriPath(language: String = "en"): String = "api/modules?language=${URLEncoder.encode(language, "UTF-8")}"
    // GET /api/modules // Daftar modul kategori
    fun katalogDramaPerModulPath(module_type: Int = 8, page_index: Int = 1, page_size: Int = 20, language: String = "en"): String = "api/catalog?module_type=${module_type}&page_index=${page_index}&page_size=${page_size}&language=${URLEncoder.encode(language, "UTF-8")}"
    // GET /api/catalog // Katalog drama per modul
    fun detailVideoDaftarEpisodePath(video_id: Int = 6101, language: String = "en"): String = "api/video_info?video_id=${video_id}&language=${URLEncoder.encode(language, "UTF-8")}"
    // GET /api/video_info // Detail video + daftar episode
    fun urlStreamingEpisodePath(video_id: Int = 6101, episodes_id: Int = 431426, language: String = "en"): String = "api/episodes_content?video_id=${video_id}&episodes_id=${episodes_id}&language=${URLEncoder.encode(language, "UTF-8")}"
    // GET /api/episodes_content // URL streaming episode

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