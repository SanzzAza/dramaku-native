package com.dramaku.app.catalog

import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder

/**
 * Playlet catalog untuk endpoint dramahub.be/playlet
 *
 * Auto-generated dari docs: 9 endpoints
 * Base URL: https://dramahub.be/playlet
 * Token: Bearer 15693e658f723c5b4c45900a5d045ef0ab6a053ecda4dadb831c68fef773ba5e
 *
 * - GET /languages // Daftar bahasa yang didukung Playlet []
 * - GET /shorts/recommend // Feed shorts [language, page, pageSize]
 * - GET /new-arrivals // Theater baru tiba [language]
 * - GET /home/columns // Daftar tipe kolom home (topColumnTypeList) [language]
 * - GET /column // Feed kolom (zoneColumnListNew, non-paginated) [columnTypeId!, language]
 * - GET /search // Pencarian theater [keyword!, language, page, pageSize]
 * - GET /theater/:id // Detail theater + daftar chapter [id!, language]
 * - GET /theater/:id/open/:chapter/playlist.m3u8 // Stream HLS tunggal (master / variant / segment v2-decrypt v3-clear) [id!, chapter!]
 * - GET /theater/:id/subtitle/:chapter // Pilihan subtitle per chapter [id!, chapter!, language]
 */
object PlayletCatalog {

    const val BASE = "/playlet"
    const val BASE_URL = "https://dramahub.be/playlet"

    const val DAFTARBAHASAYANGDIDUKUNGPLAYLET_PATH = "languages"
    fun feedShortsPath(language: String = "en", page: Int = 1, pageSize: Int = 20): String = "shorts/recommend?language=${URLEncoder.encode(language, "UTF-8")}&page=${page}&pageSize=${pageSize}"
    // GET /shorts/recommend // Feed shorts
    fun theaterBaruTibaPath(language: String = "en"): String = "new-arrivals?language=${URLEncoder.encode(language, "UTF-8")}"
    // GET /new-arrivals // Theater baru tiba
    fun daftarTipeKolomHomeTopcolumntypelistPath(language: String = "en"): String = "home/columns?language=${URLEncoder.encode(language, "UTF-8")}"
    // GET /home/columns // Daftar tipe kolom home (topColumnTypeList)
    fun feedKolomZonecolumnlistnewNonPaginatedPath(columnTypeId: String, language: String = "en"): String = "column?columnTypeId=${URLEncoder.encode(columnTypeId, "UTF-8")}&language=${URLEncoder.encode(language, "UTF-8")}"
    // GET /column // Feed kolom (zoneColumnListNew, non-paginated)
    fun pencarianTheaterPath(keyword: String, language: String = "en", page: Int = 1, pageSize: Int = 20): String = "search?keyword=${URLEncoder.encode(keyword, "UTF-8")}&language=${URLEncoder.encode(language, "UTF-8")}&page=${page}&pageSize=${pageSize}"
    // GET /search // Pencarian theater
    fun detailTheaterDaftarChapterPath(id: String, language: String = "en"): String = "theater/${URLEncoder.encode(id, "UTF-8")}?language=${URLEncoder.encode(language, "UTF-8")}"
    // GET /theater/:id // Detail theater + daftar chapter
    fun streamHlsTunggalMasterVariantSegmentV2DecryptV3ClearPath(id: String, chapter: String): String = "theater/${URLEncoder.encode(id, "UTF-8")}/open/${URLEncoder.encode(chapter, "UTF-8")}/playlist.m3u8"
    // GET /theater/:id/open/:chapter/playlist.m3u8 // Stream HLS tunggal (master / variant / segment v2-decrypt v3-clear)
    fun pilihanSubtitlePerChapterPath(id: String, chapter: String, language: String = ""): String = "theater/${URLEncoder.encode(id, "UTF-8")}/subtitle/${URLEncoder.encode(chapter, "UTF-8")}?language=${URLEncoder.encode(language, "UTF-8")}"
    // GET /theater/:id/subtitle/:chapter // Pilihan subtitle per chapter

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