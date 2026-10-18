package com.dramaku.app.catalog

import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder

/**
 * FlexTV catalog untuk endpoint dramahub.be/flextv
 *
 * Auto-generated dari docs: 7 endpoints
 * Base URL: https://dramahub.be/flextv
 * Token: Bearer 15693e658f723c5b4c45900a5d045ef0ab6a053ecda4dadb831c68fef773ba5e
 *
 * - GET /api/v1/languages // List supported languages []
 * - GET /api/v1/tabs // Get tabs/categories [lang]
 * - GET /api/v1/tabs/:id // Get dramas by tab (id: 1=Popular/Fokus, 2=New/Baru, 3=Chart/Peringkat, 6=Original+, 7=For Her/Wanita, 8=For Him/Pria, 11=Anime) [id!, page, lang]
 * - GET /api/v1/search // Search dramas [q!, page, lang]
 * - GET /api/v1/series/:id // Series detail [id!, lang]
 * - GET /api/v1/series/:id/episodes // Episode list [id!, lang]
 * - GET /api/v1/play/:series_id/:section_id // Video URL [series_id!, section_id!, lang]
 */
object FlexTVCatalog {

    const val BASE = "/flextv"
    const val BASE_URL = "https://dramahub.be/flextv"

    const val LISTSUPPORTEDLANGUAGES_PATH = "api/v1/languages"
    fun getTabsCategoriesPath(lang: String = "id"): String = "api/v1/tabs?lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/v1/tabs // Get tabs/categories
    fun getDramasByTabId1PopularFokus2NewBaru3ChartPeringkat6Original7ForHerWanita8ForHimPria11AnimePath(id: String, page: Int = 1, lang: String = "id"): String = "api/v1/tabs/${URLEncoder.encode(id, "UTF-8")}?page=${page}&lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/v1/tabs/:id // Get dramas by tab (id: 1=Popular/Fokus, 2=New/Baru, 3=Chart/Peringkat, 6=Original+, 7=For Her/Wanita, 8=For Him/Pria, 11=Anime)
    fun searchDramasPath(q: String, page: Int = 1, lang: String = "id"): String = "api/v1/search?q=${URLEncoder.encode(q, "UTF-8")}&page=${page}&lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/v1/search // Search dramas
    fun seriesDetailPath(id: String, lang: String = "id"): String = "api/v1/series/${URLEncoder.encode(id, "UTF-8")}?lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/v1/series/:id // Series detail
    fun episodeListPath(id: String, lang: String = "id"): String = "api/v1/series/${URLEncoder.encode(id, "UTF-8")}/episodes?lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/v1/series/:id/episodes // Episode list
    fun videoUrlPath(series_id: String, section_id: String, lang: String = "id"): String = "api/v1/play/${URLEncoder.encode(series_id, "UTF-8")}/${URLEncoder.encode(section_id, "UTF-8")}?lang=${URLEncoder.encode(lang, "UTF-8")}"
    // GET /api/v1/play/:series_id/:section_id // Video URL

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