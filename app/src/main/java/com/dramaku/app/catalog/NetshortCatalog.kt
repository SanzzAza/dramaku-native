package com.dramaku.app.catalog

import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder

/** Satu tab NetShort dari `/tabs` (Jelajahi, Baru, Dubbing, VIP, Anime, …). */
data class NetshortTab(val id: String, val name: String, val type: Int, val isDefault: Boolean = false)

/** Satu tag NetShort dari `/categories` — dipakai `category/{page}?tagId=…`. */
data class NetshortTag(val labelId: String, val name: String)

/**
 * Helper NetShort untuk endpoint `dramahub.be/netshort/api/v1`.
 *
 * Semua fungsi di sini murni (tanpa jaringan) supaya bisa diuji unit, mengikuti
 * pola [MovieboxCatalog] dan [DramaboxCatalog].
 */
object NetshortCatalog {

    // Jalur endpoint tetap (semua butuh lang=id_ID).
    const val EXPLORE_PATH = "explore/1?lang=id_ID"
    const val FEED_PATH = "feed/1?lang=id_ID"
    const val NEW_PATH = "new/1?lang=id_ID"
    const val DUBBING_PATH = "dubbing/1?lang=id_ID"
    const val VIP_PATH = "vip/1?lang=id_ID"
    const val TABS_PATH = "tabs?lang=id_ID"
    const val CATEGORIES_PATH = "categories?lang=id_ID"

    /** Urutan kualitas saat data saver aktif (hemat kuota). */
    private val SAVER_PREFERENCE = listOf("540p", "480p", "360p")

    /** Urutan kualitas normal: 720p paling pas untuk layar HP, lalu 1080p. */
    private val NORMAL_PREFERENCE = listOf("720p", "1080p", "540p")

    /** "540p" → 540; nilai tak dikenal → 0 supaya selalu bisa dibandingkan. */
    fun qualityRank(quality: String): Int = quality.takeWhile { it.isDigit() }.toIntOrNull() ?: 0

    /**
     * Pilih URL video dari `data.videos` pada balasan `/episode/{id}/{ep}`.
     *
     * Satu episode bisa datang dalam 540p/720p/1080p. Aturan pilihnya:
     * - data saver aktif → 540p dulu (paling ringan)
     * - normal → 720p dulu (1080p ikut dipertimbangkan kalau 720p tidak ada)
     * - kalau preset di atas tidak tersedia, ambil kualitas ujung yang searah:
     *   terendah untuk data saver, tertinggi untuk mode normal.
     */
    fun pickVideo(videos: List<Pair<String, String>>, dataSaver: Boolean): String {
        val usable = videos.filter { it.second.startsWith("http") }
        if (usable.isEmpty()) return ""
        val preference = if (dataSaver) SAVER_PREFERENCE else NORMAL_PREFERENCE
        preference.forEach { wanted ->
            usable.firstOrNull { it.first.equals(wanted, true) }?.let { return it.second }
        }
        return (if (dataSaver) usable.minByOrNull { qualityRank(it.first) } else usable.maxByOrNull { qualityRank(it.first) })?.second.orEmpty()
    }

    /**
     * Daftar tab dari `/tabs`.
     *
     * `type` menandai isi tab: 0 = daftar konten (bisa dibuka lewat
     * `tab/{id}/{page}`), 1 = peringkat, 2 = halaman kategori, 4 = VIP. Tab
     * bertipe lain punya endpoint sendiri, jadi hanya type 0 yang dipakai
     * sebagai rak beranda.
     */
    fun tabs(json: JSONObject, limit: Int = 24): List<NetshortTab> {
        val arr = json.optJSONArray("data") ?: JSONArray()
        val out = LinkedHashMap<String, NetshortTab>()
        for (i in 0 until arr.length()) {
            val o = arr.optJSONObject(i) ?: continue
            val id = o.stringAny("id")
            val name = o.stringAny("name").trim()
            if (id.isBlank() || name.isBlank()) continue
            out.putIfAbsent(id, NetshortTab(id, name, o.optInt("type", 0), o.optBoolean("isDefault", false)))
        }
        return out.values.take(limit)
    }

    /** Tab yang isinya daftar konten dan bukan feed utama (dipakai rak beranda). */
    fun contentTabs(json: JSONObject, limit: Int = 8): List<NetshortTab> =
        tabs(json).filter { it.type == 0 && !it.isDefault }.take(limit)

    /**
     * Tag dari `/categories` → `data.tag[]`. Tiap entri punya `labelName` dan
     * `newLabelIdList`; id pertamanya dipakai sebagai `tagId` untuk
     * `category/{page}`. Entri "Semua" (labelLanguageId -1) dilewati karena
     * bukan genre.
     */
    fun tags(json: JSONObject, limit: Int = 6): List<NetshortTag> {
        val arr = json.optJSONObject("data")?.optJSONArray("tag") ?: JSONArray()
        val out = LinkedHashMap<String, NetshortTag>()
        for (i in 0 until arr.length()) {
            val o = arr.optJSONObject(i) ?: continue
            val name = o.stringAny("labelName", "value").trim()
            if (name.isBlank() || name.equals("Semua", true) || name.equals("All", true)) continue
            val ids = o.optJSONArray("newLabelIdList") ?: JSONArray()
            val labelId = ids.optString(0).trim()
            if (labelId.isBlank()) continue
            out.putIfAbsent(name.lowercase(), NetshortTag(labelId, name))
        }
        return out.values.take(limit)
    }

    /**
     * Judul saran dari `/search-hint` (dipakai chip "Saran pencarian").
     *
     * Endpoint `search/{keyword}` menolak emoji (balasannya bukan JSON), jadi
     * nama dibersihkan dulu lewat [cleanHint] — emoji dan tanda baca dibuang.
     */
    fun searchHints(json: JSONObject, limit: Int = 8): List<String> {
        val arr = json.optJSONArray("data") ?: JSONArray()
        return arr.objects()
            .map { cleanHint(it.stringAny("name", "title")) }
            .filter { it.length >= 3 }
            .distinctBy { it.lowercase() }
            .take(limit)
    }

    /**
     * Bersihkan judul saran jadi kata kunci yang aman untuk path URL:
     * emoji/simbol dibuang, spasi dirapikan. Contoh:
     * "Tinju Maut ⚡🆙" → "Tinju Maut".
     */
    fun cleanHint(name: String): String = name
        .filter {
            it.isLetterOrDigit() || it == ' ' || it == '-' || it == '\'' || it == '.' ||
                it == ',' || it == '&' || it == '(' || it == ')'
        }
        .replace(Regex("\\s+"), " ")
        .trim()

    /** `search/{keyword}/{page}` — spasi jadi %20 (proxy menolak "+"). */
    fun searchPath(keyword: String, page: Int = 1): String {
        val encoded = URLEncoder.encode(keyword.trim(), "UTF-8").replace("+", "%20")
        return "search/$encoded/${page.coerceAtLeast(1)}?lang=id_ID"
    }

    fun detailPath(dramaId: String) = "detail/${URLEncoder.encode(dramaId, "UTF-8")}?lang=id_ID"

    fun similarPath(dramaId: String) = "similar/${URLEncoder.encode(dramaId, "UTF-8")}?lang=id_ID"

    fun episodePath(dramaId: String, episode: Int) =
        "episode/${URLEncoder.encode(dramaId, "UTF-8")}/${episode.coerceAtLeast(1)}?lang=id_ID"

    fun tabPath(tabId: String, page: Int = 1) = "tab/${URLEncoder.encode(tabId, "UTF-8")}/${page.coerceAtLeast(1)}?lang=id_ID"

    /** `category/{page}?region=0&audio=0&tagId=…` — region/audio 0 = "Semua". */
    fun categoryPath(tagId: String, page: Int = 1) =
        "category/${page.coerceAtLeast(1)}?region=0&audio=0&tagId=${URLEncoder.encode(tagId, "UTF-8")}&lang=id_ID"

    /** `JSONArray` → daftar `JSONObject` (kembar dengan helper di MainActivity). */
    private fun JSONArray.objects(): List<JSONObject> = (0 until length()).mapNotNull { optJSONObject(it) }

    private fun JSONObject.stringAny(vararg keys: String): String {
        keys.forEach { k ->
            val v = opt(k)
            if (v != null && v != JSONObject.NULL && v !is JSONObject && v !is JSONArray) {
                val s = v.toString().trim()
                if (s.isNotBlank()) return s
            }
        }
        return ""
    }
}
