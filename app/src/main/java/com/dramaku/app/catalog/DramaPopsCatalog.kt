package com.dramaku.app.catalog

import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder

/**
 * DramaPops catalog untuk endpoint dramahub.be/dramapops
 *
 * Auto-generated dari docs: 8 endpoints
 * Base URL: https://dramahub.be/dramapops
 * Token: Bearer 15693e658f723c5b4c45900a5d045ef0ab6a053ecda4dadb831c68fef773ba5e
 *
 * - GET /api/languages // Daftar bahasa []
 * - GET /api/homepage // Homepage (semua section) [language]
 * - GET /api/drama // Semua film [limit, language]
 * - GET /api/drama/trending // Film trending [limit, language]
 * - GET /api/drama/popular // Film populer [limit, language]
 * - GET /api/drama/search // Pencarian film [q!, limit, language]
 * - GET /api/drama/:id // Detail film [id!, language]
 * - GET /api/drama/:id/:ep // Video player episode [id!, ep!, quality, language]
 */
object DramaPopsCatalog {

    const val BASE = "/dramapops"
    const val BASE_URL = "https://dramahub.be/dramapops"

    const val DAFTARBAHASA_PATH = "api/languages"
    fun homepageSemuaSectionPath(language: String = "en"): String = "api/homepage?language=${URLEncoder.encode(language, "UTF-8")}"
    // GET /api/homepage // Homepage (semua section)
    fun semuaFilmPath(limit: Int = 20, language: String = "en"): String = "api/drama?limit=${limit}&language=${URLEncoder.encode(language, "UTF-8")}"
    // GET /api/drama // Semua film
    fun filmTrendingPath(limit: Int = 20, language: String = "en"): String = "api/drama/trending?limit=${limit}&language=${URLEncoder.encode(language, "UTF-8")}"
    // GET /api/drama/trending // Film trending
    fun filmPopulerPath(limit: Int = 20, language: String = "en"): String = "api/drama/popular?limit=${limit}&language=${URLEncoder.encode(language, "UTF-8")}"
    // GET /api/drama/popular // Film populer
    fun pencarianFilmPath(q: String, limit: Int = 20, language: String = "en"): String = "api/drama/search?q=${URLEncoder.encode(q, "UTF-8")}&limit=${limit}&language=${URLEncoder.encode(language, "UTF-8")}"
    // GET /api/drama/search // Pencarian film
    fun detailFilmPath(id: String, language: String = "en"): String = "api/drama/${URLEncoder.encode(id, "UTF-8")}?language=${URLEncoder.encode(language, "UTF-8")}"
    // GET /api/drama/:id // Detail film
    fun videoPlayerEpisodePath(id: String, ep: Int, quality: String = "", language: String = "en"): String = "api/drama/${URLEncoder.encode(id, "UTF-8")}/${ep}?quality=${URLEncoder.encode(quality, "UTF-8")}&language=${URLEncoder.encode(language, "UTF-8")}"
    // GET /api/drama/:id/:ep // Video player episode

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