package com.dramaku.app.catalog

import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder

/**
 * GoodShort catalog untuk endpoint dramahub.be/goodshort
 *
 * Auto-generated dari docs: 9 endpoints
 * Base URL: https://dramahub.be/goodshort
 * Token: Bearer 15693e658f723c5b4c45900a5d045ef0ab6a053ecda4dadb831c68fef773ba5e
 *
 * - GET /api/v1/home // Katalog feed utama [channelId, page, pageSize, index]
 * - GET /api/v1/nav // Menu navigasi utama (tab app) []
 * - GET /api/v1/trending // Feed Tren (cursor layerId_bookId) [page, pageSize, index, tags]
 * - GET /api/v1/category // Filter kategori/genre [genre, character, setting, popular, subtype, gender, page, pageSize]
 * - GET /api/v1/ranking // Ranking (0=Tren, 1=Terbaru, 2=Pencarian Populer) [type, page, pageSize]
 * - GET /api/v1/search // Cari drama [q!]
 * - GET /api/v1/chapters/:bookId // Daftar episode satu drama [bookId!]
 * - GET /api/v1/unlock/:bookId // Semua episode + semua kualitas (720p/540p/1080p, m3u8 + fileSize) [bookId!, q]
 * - GET /api/v1/key // Kunci dekripsi AES-128 video offline []
 */
object GoodShortCatalog {

    const val BASE = "/goodshort"
    const val BASE_URL = "https://dramahub.be/goodshort"

    fun katalogFeedUtamaPath(channelId: Int = 656, page: Int = 1, pageSize: Int = 12, index: Int = 1): String = "api/v1/home?channelId=${channelId}&page=${page}&pageSize=${pageSize}&index=${index}"
    // GET /api/v1/home // Katalog feed utama
    const val MENUNAVIGASIUTAMATABAPP_PATH = "api/v1/nav"
    fun feedTrenCursorLayeridBookidPath(page: Int = 1, pageSize: Int = 12, index: Int = 1, tags: String = "550,575"): String = "api/v1/trending?page=${page}&pageSize=${pageSize}&index=${index}&tags=${URLEncoder.encode(tags, "UTF-8")}"
    // GET /api/v1/trending // Feed Tren (cursor layerId_bookId)
    fun filterKategoriGenrePath(genre: String = "", character: String = "", setting: String = "", popular: String = "", subtype: String = "", gender: String = "", page: Int = 1, pageSize: Int = 12): String = "api/v1/category?genre=${URLEncoder.encode(genre, "UTF-8")}&character=${URLEncoder.encode(character, "UTF-8")}&setting=${URLEncoder.encode(setting, "UTF-8")}&popular=${URLEncoder.encode(popular, "UTF-8")}&subtype=${URLEncoder.encode(subtype, "UTF-8")}&gender=${URLEncoder.encode(gender, "UTF-8")}&page=${page}&pageSize=${pageSize}"
    // GET /api/v1/category // Filter kategori/genre
    fun ranking0Tren1Terbaru2PencarianPopulerPath(type: Int = 1, page: Int = 1, pageSize: Int = 12): String = "api/v1/ranking?type=${type}&page=${page}&pageSize=${pageSize}"
    // GET /api/v1/ranking // Ranking (0=Tren, 1=Terbaru, 2=Pencarian Populer)
    fun cariDramaPath(q: String): String = "api/v1/search?q=${URLEncoder.encode(q, "UTF-8")}"
    // GET /api/v1/search // Cari drama
    fun daftarEpisodeSatuDramaPath(bookId: String): String = "api/v1/chapters/${URLEncoder.encode(bookId, "UTF-8")}"
    // GET /api/v1/chapters/:bookId // Daftar episode satu drama
    fun semuaEpisodeSemuaKualitas720p540p1080pM3u8FilesizePath(bookId: String, q: String = "720p"): String = "api/v1/unlock/${URLEncoder.encode(bookId, "UTF-8")}?q=${URLEncoder.encode(q, "UTF-8")}"
    // GET /api/v1/unlock/:bookId // Semua episode + semua kualitas (720p/540p/1080p, m3u8 + fileSize)
    const val KUNCIDEKRIPSIAES128VIDEOOFFLINE_PATH = "api/v1/key"

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