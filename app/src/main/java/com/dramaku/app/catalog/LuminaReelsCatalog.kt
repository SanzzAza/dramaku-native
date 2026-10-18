package com.dramaku.app.catalog

import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder

/**
 * LuminaReels catalog untuk endpoint dramahub.be/luminareels
 *
 * Auto-generated dari docs: 7 endpoints
 * Base URL: https://dramahub.be/luminareels
 * Token: Bearer 15693e658f723c5b4c45900a5d045ef0ab6a053ecda4dadb831c68fef773ba5e
 *
 * - GET /api/languages // Daftar bahasa yang didukung []
 * - GET /api/drama/list // Feed / daftar drama [page, page_size, lang]
 * - GET /api/drama/category // Daftar kategori drama [lang]
 * - GET /api/drama/category/:id // Daftar drama pada kategori tertentu [id!, page, page_size, lang]
 * - GET /api/drama/search // Cari drama berdasarkan kata kunci [q!, page, page_size, lang]
 * - GET /api/drama/detail // Detail drama + episode [id!, lang]
 * - GET /api/stream/:id/:index/master.m3u8 // Playlist HLS hasil decrypt yang bisa diputar player [id!, index!, lang]
 */
object LuminaReelsCatalog {

    const val BASE = "/luminareels"
    const val BASE_URL = "https://dramahub.be/luminareels"

    const val DAFTARBAHASAYANGDIDUKUNG_PATH = "api/languages"
    fun feedDaftarDramaPath(page: Int = 1, page_size: Int = 20, lang: String = "en"): String = "api/drama/list?page=${page}&page_size=${page_size}&lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/drama/list // Feed / daftar drama
    fun daftarKategoriDramaPath(lang: String = "en"): String = "api/drama/category?lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/drama/category // Daftar kategori drama
    fun daftarDramaPadaKategoriTertentuPath(id: String, page: Int = 1, page_size: Int = 20, lang: String = "en"): String = "api/drama/category/${URLEncoder.encode(id, "UTF-8")}?page=${page}&page_size=${page_size}&lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/drama/category/:id // Daftar drama pada kategori tertentu
    fun cariDramaBerdasarkanKataKunciPath(q: String, page: Int = 1, page_size: Int = 20, lang: String = "en"): String = "api/drama/search?q=${URLEncoder.encode(q, "UTF-8")}&page=${page}&page_size=${page_size}&lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/drama/search // Cari drama berdasarkan kata kunci
    fun detailDramaEpisodePath(id: String, lang: String = "en"): String = "api/drama/detail?id=${URLEncoder.encode(id, "UTF-8")}&lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/drama/detail // Detail drama + episode
    fun playlistHlsHasilDecryptYangBisaDiputarPlayerPath(id: String, index: Int, lang: String = "en"): String = "api/stream/${URLEncoder.encode(id, "UTF-8")}/${URLEncoder.encode(index, "UTF-8")}/master.m3u8?lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/stream/:id/:index/master.m3u8 // Playlist HLS hasil decrypt yang bisa diputar player

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