package com.dramaku.app.catalog

import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder

/**
 * ShortBox catalog untuk endpoint dramahub.be/shortbox
 *
 * Auto-generated dari docs: 10 endpoints
 * Base URL: https://dramahub.be/shortbox
 * Token: Bearer 15693e658f723c5b4c45900a5d045ef0ab6a053ecda4dadb831c68fef773ba5e
 *
 * - GET /api/languages // Daftar bahasa tersedia []
 * - GET /api/categories // Daftar kategori drama [lang]
 * - GET /api/list // Browse drama (ShortBox) [page, page_size, languages, streamable]
 * - GET /api/new-list // Drama terbaru (PSSDK) [page, page_size, languages]
 * - GET /api/recommend // Rekomendasi home [languages, streamable]
 * - GET /api/for-you // Feed For You (paginasi lokal) [page, page_size, languages, streamable]
 * - GET /api/hot-search // Drama populer/trending [languages]
 * - GET /api/search // Cari drama (alias: query, keyword) [q!, page, page_size, languages]
 * - GET /api/info/:id // Detail drama + daftar episode [id!, sort, languages]
 * - GET /api/stream/:id/:ep // Stream URL (m3u8/DRM) [id!, ep!, quality]
 */
object ShortBoxCatalog {

    const val BASE = "/shortbox"
    const val BASE_URL = "https://dramahub.be/shortbox"

    const val DAFTARBAHASATERSEDIA_PATH = "api/languages"
    fun daftarKategoriDramaPath(lang: String = "id"): String = "api/categories?lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/categories // Daftar kategori drama
    fun browseDramaShortboxPath(page: Int = 1, page_size: Int = 10, languages: String = "id", streamable: String = "true"): String = "api/list?page=${page}&page_size=${page_size}&languages=${URLEncoder.encode(languages, "UTF-8")}&streamable=${URLEncoder.encode(streamable, "UTF-8")}"
    // GET /api/list // Browse drama (ShortBox)
    fun dramaTerbaruPssdkPath(page: Int = 1, page_size: Int = 10, languages: String = "id"): String = "api/new-list?page=${page}&page_size=${page_size}&languages=${URLEncoder.encode(languages, "UTF-8")}"
    // GET /api/new-list // Drama terbaru (PSSDK)
    fun rekomendasiHomePath(languages: String = "id", streamable: String = "true"): String = "api/recommend?languages=${URLEncoder.encode(languages, "UTF-8")}&streamable=${URLEncoder.encode(streamable, "UTF-8")}"
    // GET /api/recommend // Rekomendasi home
    fun feedForYouPaginasiLokalPath(page: Int = 1, page_size: Int = 10, languages: String = "id", streamable: String = "true"): String = "api/for-you?page=${page}&page_size=${page_size}&languages=${URLEncoder.encode(languages, "UTF-8")}&streamable=${URLEncoder.encode(streamable, "UTF-8")}"
    // GET /api/for-you // Feed For You (paginasi lokal)
    fun dramaPopulerTrendingPath(languages: String = "id"): String = "api/hot-search?languages=${URLEncoder.encode(languages, "UTF-8")}"
    // GET /api/hot-search // Drama populer/trending
    fun cariDramaAliasQueryKeywordPath(q: String, page: Int = 1, page_size: Int = 10, languages: String = "id"): String = "api/search?q=${URLEncoder.encode(q, "UTF-8")}&page=${page}&page_size=${page_size}&languages=${URLEncoder.encode(languages, "UTF-8")}"
    // GET /api/search // Cari drama (alias: query, keyword)
    fun detailDramaDaftarEpisodePath(id: String, sort: Int = 1, languages: String = "id"): String = "api/info/${URLEncoder.encode(id, "UTF-8")}?sort=${sort}&languages=${URLEncoder.encode(languages, "UTF-8")}"
    // GET /api/info/:id // Detail drama + daftar episode
    fun streamUrlM3u8DrmPath(id: String, ep: Int, quality: String = "720"): String = "api/stream/${URLEncoder.encode(id, "UTF-8")}/${ep}?quality=${URLEncoder.encode(quality, "UTF-8")}"
    // GET /api/stream/:id/:ep // Stream URL (m3u8/DRM)

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