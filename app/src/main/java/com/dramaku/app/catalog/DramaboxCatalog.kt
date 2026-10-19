package com.dramaku.app.catalog

import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder

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
 * Semua logika di sini murni (tanpa jaringan) supaya bisa diuji unit.
 */
object DramaboxCatalog {

    /** Field yang boleh membawa URL video di balasan `/stream`. */
    private val STREAM_URL_KEYS = arrayOf("video", "playUrl", "play_url", "url", "resourceLink", "source")

    /**
     * Jalur pemutar DramaBox v2. Endpoint `/play` memilih chapter lewat `index`
     * yang berbasis nol; parameter `episode` lama diabaikan oleh upstream dan
     * selalu membalas episode pertama.
     */
    fun playPath(bookId: String, episode: Int): String {
        val encodedId = URLEncoder.encode(bookId, "UTF-8")
        val chapterIndex = (episode - 1).coerceAtLeast(0)
        return "play?bookId=$encodedId&index=$chapterIndex&lang=in"
    }

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

    private const val B64_URL_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_"

    /** Decode base64 URL-safe tanpa padding — pure Kotlin supaya tetap bisa diuji unit. */
    private fun decodeBase64Url(s: String): ByteArray? {
        val clean = s.trim().trimEnd('=')
        if (clean.isEmpty()) return null
        val out = java.io.ByteArrayOutputStream(clean.length * 3 / 4)
        var buffer = 0
        var bits = 0
        for (raw in clean) {
            val c = when (raw) { '+' -> '-'; '/' -> '_'; else -> raw }
            val idx = B64_URL_ALPHABET.indexOf(c)
            if (idx < 0) return null
            buffer = (buffer shl 6) or idx
            bits += 6
            if (bits >= 8) {
                bits -= 8
                out.write((buffer shr bits) and 0xFF)
            }
        }
        return out.toByteArray()
    }

    /**
     * ⚠️ JANGAN pakai hasil fungsi ini untuk playback.
     *
     * URL video proxy v2 dibungkus gateway (`…/api/web/stream?u=<base64>`).
     * Param `u` memang bisa di-decode menjadi URL CDN dramaboxdb bertanda
     * tangan CloudFront — tapi file `.encrypt.mp4` di CDN memakai enkripsi
     * Aliyun private (`etavirp_nuyila`): moov terbaca (durasi muncul), bitstream
     * h264 tersandi, decoder selalu gagal ("Sumber video gagal dibuka").
     * Gateway-lah yang men-decrypt on-the-fly (area mdat-nya berbeda dan
     * terbukti ter-decode). Fungsi ini dipertahankan hanya untuk diagnostik
     * dan sebagai dokumentasi temuan; playback harus tetap lewat gateway.
     */
    fun directVideoUrl(wrapped: String): String {
        val marker = "stream?u="
        val idx = wrapped.indexOf(marker)
        if (idx < 0) return ""
        val payload = wrapped.substring(idx + marker.length).substringBefore('&').substringBefore('#')
        val decoded = decodeBase64Url(payload)?.toString(Charsets.UTF_8).orEmpty()
        return if (decoded.startsWith("http://") || decoded.startsWith("https://")) decoded.trim() else ""
    }
}
