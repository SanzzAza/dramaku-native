package com.dramaku.app.catalog

import org.json.JSONArray
import org.json.JSONObject

/**
 * Satu genre DramaBox dari endpoint `/categories`.
 *
 * [id] dipakai sebagai parameter `browse?type=<id>` dan [name] sudah berbahasa
 * Indonesia (dikirim apa adanya oleh API), jadi layak dipasang langsung sebagai
 * judul rak di beranda.
 */
data class DramaboxCategory(val id: Int, val name: String)

/**
 * Helper DramaBox untuk endpoint `dramahub.be/dramaboxbaru/api`.
 *
 * Semua logika di sini murni (tanpa jaringan) supaya bisa diuji unit, sama
 * seperti [MovieboxCatalog].
 */
object DramaboxCatalog {

    /** Field yang boleh membawa URL video di balasan `/stream`. */
    private val STREAM_URL_KEYS = arrayOf("video", "playUrl", "play_url", "url", "resourceLink", "source")

    /**
     * Ambil URL video dari balasan `/stream`.
     *
     * Endpoint baru membalas JSON, bukan playlist mentah:
     * `{"code":0,"video":"https://…&seg=video.mp4&k=…","duration":193.118,"subtitles":[]}`
     * Nilai `video` inilah yang diputar ExoPlayer (mp4 ber-token yang redirect 302
     * ke CDN dramaboxdb). Kalau field-nya tidak ada — misalnya proxy gaya lama
     * yang mengirim playlist langsung — hasilnya string kosong supaya pemanggil
     * bisa jatuh ke URL endpoint-nya sendiri.
     */
    fun streamUrl(json: JSONObject): String {
        val nodes = listOf(json, json.optJSONObject("data"))
        nodes.forEach { node ->
            node ?: return@forEach
            STREAM_URL_KEYS.forEach { key ->
                val v = node.opt(key)
                if (v is String && v.startsWith("http")) return v.trim()
            }
        }
        return ""
    }

    /**
     * Daftar genre dari `/categories`.
     *
     * Entri `all` (id 0) dibuang karena bukan genre, duplikat nama dirapikan
     * (upstream punya dua entri "Bayi"), dan hasilnya dipotong [limit] supaya
     * beranda tidak menembak terlalu banyak rak sekaligus.
     */
    fun categories(json: JSONObject, limit: Int = 10): List<DramaboxCategory> {
        val arr = json.optJSONArray("data") ?: json.optJSONObject("data")?.optJSONArray("list") ?: JSONArray()
        val unique = LinkedHashMap<String, DramaboxCategory>()
        for (i in 0 until arr.length()) {
            val o = arr.optJSONObject(i) ?: continue
            val id = o.optInt("id", -1)
            val name = o.optString("name").ifBlank { o.optString("replaceName") }.trim()
            if (id <= 0 || name.isBlank() || name.equals("all", true)) continue
            unique.putIfAbsent(name.lowercase(), DramaboxCategory(id, name))
        }
        return unique.values.take(limit)
    }

    /** Path rak genre: `browse?type=<id>&page=<page>&lang=in`. */
    fun browsePath(categoryId: Int, page: Int = 1): String = "browse?type=$categoryId&page=$page&lang=in"

    /** Path rak "Permata tersembunyi" — endpoint khusus DramaBox di proxy baru. */
    const val HIDDEN_GEMS_PATH = "hidden-gems?lang=in"
}
