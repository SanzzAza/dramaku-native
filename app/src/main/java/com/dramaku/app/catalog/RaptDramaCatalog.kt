package com.dramaku.app.catalog

import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder

/**
 * RaptDrama catalog untuk endpoint dramahub.be/raptdrama
 *
 * Auto-generated dari docs: 10 endpoints
 * Base URL: https://dramahub.be/raptdrama
 * Token: Bearer 15693e658f723c5b4c45900a5d045ef0ab6a053ecda4dadb831c68fef773ba5e
 *
 * - GET /v1/languages // Supported languages []
 * - GET /open/recommend // Rekomendasi drama (pagination) [page, lang]
 * - GET /open/trending // Drama trending [lang]
 * - GET /open/new // Drama rilis terbaru [lang]
 * - GET /open/classifyv2 // Daftar kategori [lang]
 * - POST /open/search // Pencarian drama (body JSON: {"keyword":"love","page":1}) [keyword!, page, lang]
 * - GET /open/classify_video // List drama per kategori [type!, page, lang]
 * - POST /video/videoinfo // Detail video (body JSON: {"vid": 2435}) [vid!, lang]
 * - GET /video/getchapters // Daftar episode [vid!, lang]
 * - GET /video/playlist // Load semua episode + URL m3u8 sekaligus (cukup vid) [vid!, lang]
 */
object RaptDramaCatalog {

    const val BASE = "/raptdrama"
    const val BASE_URL = "https://dramahub.be/raptdrama"

    const val SUPPORTEDLANGUAGES_PATH = "v1/languages"
    fun rekomendasiDramaPaginationPath(page: Int = 1, lang: String = "en-US"): String = "open/recommend?page=${page}&lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /open/recommend // Rekomendasi drama (pagination)
    fun dramaTrendingPath(lang: String = "en-US"): String = "open/trending?lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /open/trending // Drama trending
    fun dramaRilisTerbaruPath(lang: String = "en-US"): String = "open/new?lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /open/new // Drama rilis terbaru
    fun daftarKategoriPath(lang: String = "en-US"): String = "open/classifyv2?lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /open/classifyv2 // Daftar kategori
    fun pencarianDramaBodyJsonKeywordLovePage1Path(keyword: String, page: Int = 1, lang: String = "en-US"): String = "open/search?keyword=${URLEncoder.encode(keyword, "UTF-8")}&page=${page}&lang=${URLEncoder.encode(lang, "UTF-8")}"
    // POST /open/search // Pencarian drama (body JSON: {"keyword":"love","page":1})
    fun listDramaPerKategoriPath(type: Int, page: Int = 1, lang: String = "en-US"): String = "open/classify_video?type=${type}&page=${page}&lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /open/classify_video // List drama per kategori
    fun detailVideoBodyJsonVid2435Path(vid: String, lang: String = "en-US"): String = "video/videoinfo?vid=${URLEncoder.encode(vid, "UTF-8")}&lang=${URLEncoder.encode(lang, "UTF-8")}"
    // POST /video/videoinfo // Detail video (body JSON: {"vid": 2435})
    fun daftarEpisodePath(vid: String, lang: String = "en-US"): String = "video/getchapters?vid=${URLEncoder.encode(vid, "UTF-8")}&lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /video/getchapters // Daftar episode
    fun loadSemuaEpisodeUrlM3u8SekaligusCukupVidPath(vid: String, lang: String = "en-US"): String = "video/playlist?vid=${URLEncoder.encode(vid, "UTF-8")}&lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /video/playlist // Load semua episode + URL m3u8 sekaligus (cukup vid)

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