package com.dramaku.app.catalog

import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder

/**
 * CashDrama catalog untuk endpoint dramahub.be/cashdrama
 *
 * Auto-generated dari docs: 9 endpoints
 * Base URL: https://dramahub.be/cashdrama
 * Token: Bearer 15693e658f723c5b4c45900a5d045ef0ab6a053ecda4dadb831c68fef773ba5e
 *
 * - GET /api/v1/languages // Supported languages []
 * - GET /api/v1/home // Home feed [lang, page, pageSize, blockId]
 * - GET /api/v1/blocks // Categories/blocks [lang]
 * - GET /api/v1/tags // Tags list [lang]
 * - GET /api/v1/drama/:vid // Drama detail [vid!, ep, lang]
 * - GET /api/v1/drama/:vid/episodes // Episode list [vid!, lang]
 * - GET /api/v1/search // Search drama [q!, page, lang]
 * - GET /api/v1/tags/search // Search by tag [tagId!, page, lang]
 * - GET /api/v1/play/:vid/:ep // Video URL (Tencent VOD) [vid!, ep!, lang]
 */
object CashDramaCatalog {

    const val BASE = "/cashdrama"
    const val BASE_URL = "https://dramahub.be/cashdrama"

    const val SUPPORTEDLANGUAGES_PATH = "api/v1/languages"
    fun homeFeedPath(lang: String = "id", page: Int = 1, pageSize: Int = 20, blockId: Int = 5): String = "api/v1/home?lang=${URLEncoder.encode(lang, "UTF-8")}&page=${page}&pageSize=${pageSize}&blockId=${blockId}"
    // GET /api/v1/home // Home feed
    fun categoriesBlocksPath(lang: String = "id"): String = "api/v1/blocks?lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/v1/blocks // Categories/blocks
    fun tagsListPath(lang: String = "id"): String = "api/v1/tags?lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/v1/tags // Tags list
    fun dramaDetailPath(vid: String, ep: Int = 1, lang: String = "id"): String = "api/v1/drama/${URLEncoder.encode(vid, "UTF-8")}?ep=${ep}&lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/v1/drama/:vid // Drama detail
    fun episodeListPath(vid: String, lang: String = "id"): String = "api/v1/drama/${URLEncoder.encode(vid, "UTF-8")}/episodes?lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/v1/drama/:vid/episodes // Episode list
    fun searchDramaPath(q: String, page: Int = 1, lang: String = "id"): String = "api/v1/search?q=${URLEncoder.encode(q, "UTF-8")}&page=${page}&lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/v1/search // Search drama
    fun searchByTagPath(tagId: String, page: Int = 1, lang: String = "id"): String = "api/v1/tags/search?tagId=${URLEncoder.encode(tagId, "UTF-8")}&page=${page}&lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/v1/tags/search // Search by tag
    fun videoUrlTencentVodPath(vid: String, ep: Int, lang: String = "id"): String = "api/v1/play/${URLEncoder.encode(vid, "UTF-8")}/${URLEncoder.encode(ep, "UTF-8")}?lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/v1/play/:vid/:ep // Video URL (Tencent VOD)

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