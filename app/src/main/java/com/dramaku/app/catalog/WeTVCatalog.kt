package com.dramaku.app.catalog

import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder

/**
 * WeTV catalog untuk endpoint dramahub.be/wetv
 *
 * Auto-generated dari docs: 7 endpoints
 * Base URL: https://dramahub.be/wetv
 * Token: Bearer 15693e658f723c5b4c45900a5d045ef0ab6a053ecda4dadb831c68fef773ba5e
 *
 * - GET /api/channels // Daftar channel/kategori [lang, country]
 * - GET /api/feed // Konten per channel [channel_id, lang, country]
 * - GET /api/detail // Detail drama (sinopsis, genre, status, poster) [cid!, lang, country]
 * - GET /api/episodes // Daftar episode dengan tipe dan durasi [cid!, lang, country]
 * - GET /api/play // Info play URL (CDN direct + M3U8 stream). defn: ld/sd/hd/shd/fhd [vid!, cid!, defn, lang, country]
 * - GET /api/search // Cari drama/film [keyword!, lang, country]
 * - GET /api/languages // Daftar bahasa yang didukung []
 */
object WeTVCatalog {

    const val BASE = "/wetv"
    const val BASE_URL = "https://dramahub.be/wetv"

    fun daftarChannelKategoriPath(lang: String = "id", country: String = "ID"): String = "api/channels?lang=${URLEncoder.encode(lang, "UTF-8")}&country=${URLEncoder.encode(country, "UTF-8")}"
    // GET /api/channels // Daftar channel/kategori
    fun kontenPerChannelPath(channel_id: Int = 1001, lang: String = "id", country: String = "ID"): String = "api/feed?channel_id=${channel_id}&lang=${URLEncoder.encode(lang, "UTF-8")}&country=${URLEncoder.encode(country, "UTF-8")}"
    // GET /api/feed // Konten per channel
    fun detailDramaSinopsisGenreStatusPosterPath(cid: String, lang: String = "id", country: String = "ID"): String = "api/detail?cid=${URLEncoder.encode(cid, "UTF-8")}&lang=${URLEncoder.encode(lang, "UTF-8")}&country=${URLEncoder.encode(country, "UTF-8")}"
    // GET /api/detail // Detail drama (sinopsis, genre, status, poster)
    fun daftarEpisodeDenganTipeDanDurasiPath(cid: String, lang: String = "id", country: String = "ID"): String = "api/episodes?cid=${URLEncoder.encode(cid, "UTF-8")}&lang=${URLEncoder.encode(lang, "UTF-8")}&country=${URLEncoder.encode(country, "UTF-8")}"
    // GET /api/episodes // Daftar episode dengan tipe dan durasi
    fun infoPlayUrlCdnDirectM3u8StreamDefnLdSdHdShdFhdPath(vid: String, cid: String, defn: String = "hd", lang: String = "id", country: String = "ID"): String = "api/play?vid=${URLEncoder.encode(vid, "UTF-8")}&cid=${URLEncoder.encode(cid, "UTF-8")}&defn=${URLEncoder.encode(defn, "UTF-8")}&lang=${URLEncoder.encode(lang, "UTF-8")}&country=${URLEncoder.encode(country, "UTF-8")}"
    // GET /api/play // Info play URL (CDN direct + M3U8 stream). defn: ld/sd/hd/shd/fhd
    fun cariDramaFilmPath(keyword: String, lang: String = "id", country: String = "ID"): String = "api/search?keyword=${URLEncoder.encode(keyword, "UTF-8")}&lang=${URLEncoder.encode(lang, "UTF-8")}&country=${URLEncoder.encode(country, "UTF-8")}"
    // GET /api/search // Cari drama/film
    const val DAFTARBAHASAYANGDIDUKUNG_PATH = "api/languages"

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