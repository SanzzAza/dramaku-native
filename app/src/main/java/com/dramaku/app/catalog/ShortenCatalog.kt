package com.dramaku.app.catalog

import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder

/**
 * Shorten catalog untuk endpoint dramahub.be/shorten
 *
 * Auto-generated dari docs: 7 endpoints
 * Base URL: https://dramahub.be/shorten
 * Token: Bearer 15693e658f723c5b4c45900a5d045ef0ab6a053ecda4dadb831c68fef773ba5e
 *
 * - GET /api/v1/editors // Editor picks [page, perPage]
 * - GET /api/v1/exclusive // Exclusive dramas [page, perPage]
 * - GET /api/v1/dubbed // Dubbed dramas [page, perPage]
 * - GET /api/v1/releases // New releases [page, perPage]
 * - GET /api/v1/categories // Categories list []
 * - GET /api/v1/explore // Explore page []
 * - GET /api/v1/series/:slug // Series detail + episodes (video hash included) [slug!]
 */
object ShortenCatalog {

    const val BASE = "/shorten"
    const val BASE_URL = "https://dramahub.be/shorten"

    fun editorPicksPath(page: Int = 1, perPage: Int = 20): String = "api/v1/editors?page=${page}&perPage=${perPage}"
    // GET /api/v1/editors // Editor picks
    fun exclusiveDramasPath(page: Int = 1, perPage: Int = 20): String = "api/v1/exclusive?page=${page}&perPage=${perPage}"
    // GET /api/v1/exclusive // Exclusive dramas
    fun dubbedDramasPath(page: Int = 1, perPage: Int = 20): String = "api/v1/dubbed?page=${page}&perPage=${perPage}"
    // GET /api/v1/dubbed // Dubbed dramas
    fun newReleasesPath(page: Int = 1, perPage: Int = 20): String = "api/v1/releases?page=${page}&perPage=${perPage}"
    // GET /api/v1/releases // New releases
    const val CATEGORIESLIST_PATH = "api/v1/categories"
    const val EXPLOREPAGE_PATH = "api/v1/explore"
    fun seriesDetailEpisodesVideoHashIncludedPath(slug: String): String = "api/v1/series/${URLEncoder.encode(slug, "UTF-8")}"
    // GET /api/v1/series/:slug // Series detail + episodes (video hash included)

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