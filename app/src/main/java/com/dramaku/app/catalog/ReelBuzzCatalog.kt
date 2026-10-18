package com.dramaku.app.catalog

import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder

/**
 * ReelBuzz catalog untuk endpoint dramahub.be/reelbuzz
 *
 * Auto-generated dari docs: 7 endpoints
 * Base URL: https://dramahub.be/reelbuzz
 * Token: Bearer 15693e658f723c5b4c45900a5d045ef0ab6a053ecda4dadb831c68fef773ba5e
 *
 * - GET /api/languages // Daftar 14 bahasa yang didukung []
 * - GET /api/drama/list // Daftar drama rekomendasi [pageNum, pageSize, language]
 * - GET /api/drama/display // Feed halaman utama dengan paginasi [pageNum, pageSize, language]
 * - GET /api/drama/module/recommend // Modul rekomendasi [pageNum, pageSize, language]
 * - GET /api/search // Cari drama [keyword!, language]
 * - GET /api/drama/detail // Detail drama + daftar semua episode [dramaId!, language]
 * - GET /api/drama/video/:dramaId/:ep // URL video per episode (normal: HD CDN2 + LD terdekripsi, txvod: HLS bersih termasuk episode premium) [dramaId!, ep!, language]
 */
object ReelBuzzCatalog {

    const val BASE = "/reelbuzz"
    const val BASE_URL = "https://dramahub.be/reelbuzz"

    const val DAFTAR14BAHASAYANGDIDUKUNG_PATH = "api/languages"
    fun daftarDramaRekomendasiPath(pageNum: Int = 1, pageSize: Int = 10, language: String = ""): String = "api/drama/list?pageNum=${pageNum}&pageSize=${pageSize}&language=${URLEncoder.encode(language, "UTF-8")}"
    // GET /api/drama/list // Daftar drama rekomendasi
    fun feedHalamanUtamaDenganPaginasiPath(pageNum: Int = 1, pageSize: Int = 10, language: String = ""): String = "api/drama/display?pageNum=${pageNum}&pageSize=${pageSize}&language=${URLEncoder.encode(language, "UTF-8")}"
    // GET /api/drama/display // Feed halaman utama dengan paginasi
    fun modulRekomendasiPath(pageNum: Int = 1, pageSize: Int = 10, language: String = ""): String = "api/drama/module/recommend?pageNum=${pageNum}&pageSize=${pageSize}&language=${URLEncoder.encode(language, "UTF-8")}"
    // GET /api/drama/module/recommend // Modul rekomendasi
    fun cariDramaPath(keyword: String, language: String = ""): String = "api/search?keyword=${URLEncoder.encode(keyword, "UTF-8")}&language=${URLEncoder.encode(language, "UTF-8")}"
    // GET /api/search // Cari drama
    fun detailDramaDaftarSemuaEpisodePath(dramaId: String, language: String = ""): String = "api/drama/detail?dramaId=${URLEncoder.encode(dramaId, "UTF-8")}&language=${URLEncoder.encode(language, "UTF-8")}"
    // GET /api/drama/detail // Detail drama + daftar semua episode
    fun urlVideoPerEpisodeNormalHdCdn2LdTerdekripsiTxvodHlsBersihTermasukEpisodePremiumPath(dramaId: String, ep: Int, language: String = ""): String = "api/drama/video/${URLEncoder.encode(dramaId, "UTF-8")}/${URLEncoder.encode(ep, "UTF-8")}?language=${URLEncoder.encode(language, "UTF-8")}"
    // GET /api/drama/video/:dramaId/:ep // URL video per episode (normal: HD CDN2 + LD terdekripsi, txvod: HLS bersih termasuk episode premium)

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