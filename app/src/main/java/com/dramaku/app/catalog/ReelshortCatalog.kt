package com.dramaku.app.catalog

import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder

/**
 * ReelShort catalog untuk endpoint dramahub.be/reelshort/api/v1
 *
 * Endpoints yang diimplement:
 * 1. GET /foryou?lang=in
 * 2. GET /new?lang=in
 * 3. GET /completed?lang=in
 * 4. GET /romance?lang=in
 * 5. GET /drama?lang=in
 * 6. GET /feed/{tabId}?lang=in
 * 7. GET /search?q={query}&page={page}&lang=in
 * 8. GET /search/suggestions?lang=in
 * 9. GET /book/{bookId}?lang=in
 * 10. GET /book/{bookId}/chapters?lang=in
 * 11. GET /book/{bookId}/chapter/{chapterId}/video
 *
 * Semua parsing murni tanpa jaringan supaya bisa diuji unit.
 */
data class ReelshortTab(val id: String, val name: String, val sort: Int = 0)
data class ReelshortChapter(val id: String, val name: String, val number: Int, val duration: Int = 0, val isLocked: Boolean = false, val cover: String = "")

object ReelshortCatalog {

    // ── Path constants ──
    const val FORYOU_PATH = "foryou?lang=in"
    const val NEW_PATH = "new?lang=in"
    const val COMPLETED_PATH = "completed?lang=in"
    const val ROMANCE_PATH = "romance?lang=in"
    const val DRAMA_PATH = "drama?lang=in"
    const val SEARCH_SUGGESTIONS_PATH = "search/suggestions?lang=in"

    fun feedPath(tabId: String, lang: String = "in"): String =
        "feed/${URLEncoder.encode(tabId, "UTF-8")}?lang=$lang"

    fun searchPath(query: String, page: Int = 1, lang: String = "in"): String {
        val q = URLEncoder.encode(query.trim(), "UTF-8")
        return "search?q=$q&page=${page.coerceAtLeast(1)}&lang=$lang"
    }

    fun detailPath(bookId: String, lang: String = "in"): String =
        "book/${URLEncoder.encode(bookId, "UTF-8")}?lang=$lang"

    fun chaptersPath(bookId: String, lang: String = "in"): String =
        "book/${URLEncoder.encode(bookId, "UTF-8")}/chapters?lang=$lang"

    fun videoPath(bookId: String, chapterId: String): String =
        "book/${URLEncoder.encode(bookId, "UTF-8")}/chapter/${URLEncoder.encode(chapterId, "UTF-8")}/video"

    // ── Tabs dari /foryou atau /feed ──
    // Struktur: data.tab_list = [{tab_id, tab_name, sort}]
    fun tabs(json: JSONObject): List<ReelshortTab> {
        val data = json.optJSONObject("data") ?: json
        val arr = data.optJSONArray("tab_list") ?: JSONArray()
        val out = mutableListOf<ReelshortTab>()
        for (i in 0 until arr.length()) {
            val o = arr.optJSONObject(i) ?: continue
            val id = o.optString("tab_id").ifBlank { o.optInt("tab_id", -1).takeIf { it > 0 }?.toString().orEmpty() }
            val name = o.optString("tab_name").trim()
            if (id.isBlank() || name.isBlank()) continue
            out.add(ReelshortTab(id, name, o.optInt("sort", 0)))
        }
        return out.sortedBy { it.sort }
    }

    // ── Books dari home endpoints (/foryou, /new, /completed, /romance, /drama, /feed/*) ──
    // Struktur: data.lists = [{books:[{book_id, book_title, book_pic, special_desc, chapter_count}]}]
    // Atau untuk search: data.lists sama
    fun booksFromHome(json: JSONObject): List<JSONObject> {
        val data = json.optJSONObject("data") ?: json
        val lists = data.optJSONArray("lists") ?: JSONArray()
        val out = mutableListOf<JSONObject>()
        for (i in 0 until lists.length()) {
            val listObj = lists.optJSONObject(i) ?: continue
            val books = listObj.optJSONArray("books") ?: JSONArray()
            for (j in 0 until books.length()) {
                books.optJSONObject(j)?.let { out.add(it) }
            }
        }
        return out
    }

    // Search endpoint punya struktur yang sama tapi ada di data.lists juga
    // Untuk search/suggestions: data.book_rank_data = [{book_id, book_title, ...}]
    fun booksFromSearch(json: JSONObject): List<JSONObject> {
        val data = json.optJSONObject("data") ?: json
        // Coba lists dulu (search?q=)
        val lists = data.optJSONArray("lists")
        if (lists != null && lists.length() > 0) {
            return booksFromHome(json)
        }
        // Fallback untuk search/suggestions -> book_rank_data
        val rank = data.optJSONArray("book_rank_data") ?: JSONArray()
        val out = mutableListOf<JSONObject>()
        for (i in 0 until rank.length()) {
            rank.optJSONObject(i)?.let { out.add(it) }
        }
        // tag_data juga bisa berisi books
        val tagData = data.optJSONArray("tag_data") ?: JSONArray()
        for (i in 0 until tagData.length()) {
            val o = tagData.optJSONObject(i) ?: continue
            // tag_data kadang berisi books array juga
            val books = o.optJSONArray("books")
            if (books != null) {
                for (j in 0 until books.length()) {
                    books.optJSONObject(j)?.let { out.add(it) }
                }
            } else {
                // atau objek book langsung
                if (o.has("book_id")) out.add(o)
            }
        }
        return out
    }

    // ── Detail: data = {book_id, book_title, book_pic, special_desc, chapter_count, ...} ──
    fun parseDetail(json: JSONObject): JSONObject {
        return json.optJSONObject("data") ?: json
    }

    // ── Chapters: data.chapters = [{chapter_id, chapter_name, serial_number, duration, ...}] ──
    fun chapters(json: JSONObject): List<ReelshortChapter> {
        val data = json.optJSONObject("data") ?: json
        val arr = data.optJSONArray("chapters") ?: JSONArray()
        val out = mutableListOf<ReelshortChapter>()
        for (i in 0 until arr.length()) {
            val o = arr.optJSONObject(i) ?: continue
            val id = o.optString("chapter_id").ifBlank { o.optString("id") }
            if (id.isBlank()) continue
            val name = o.optString("chapter_name").ifBlank { o.optString("name").ifBlank { "Episode ${i+1}" } }
            val num = o.optInt("serial_number", i+1)
            val dur = o.optInt("duration", 0)
            val locked = o.optInt("is_lock", 0) == 1 || o.optBoolean("isLocked", false)
            val cover = o.optString("video_pic").ifBlank { o.optString("cover") }
            out.add(ReelshortChapter(id, name, num, dur, locked, cover))
        }
        return out.sortedBy { it.number }
    }

    // ── Video: data.videos = [{PlayURL, Dpi, Encode, Bitrate}] ──
    // Pilih kualitas terbaik: 720p preferred, fallback ke tertinggi
    private val QUALITY_PREF = listOf(720, 1080, 540, 480)

    fun pickVideoUrl(videos: List<Pair<Int, String>>): String {
        if (videos.isEmpty()) return ""
        val usable = videos.filter { it.second.startsWith("http") }
        if (usable.isEmpty()) return ""
        // Cari preferensi
        QUALITY_PREF.forEach { q ->
            usable.firstOrNull { it.first == q }?.let { return it.second }
        }
        // Fallback: resolusi tertinggi
        return usable.maxByOrNull { it.first }?.second.orEmpty()
    }

    fun parseVideos(json: JSONObject): List<Pair<Int, String>> {
        val data = json.optJSONObject("data") ?: json
        val arr = data.optJSONArray("videos") ?: JSONArray()
        val out = mutableListOf<Pair<Int, String>>()
        for (i in 0 until arr.length()) {
            val o = arr.optJSONObject(i) ?: continue
            val url = o.optString("PlayURL").ifBlank { o.optString("play_url").ifBlank { o.optString("url") } }
            if (url.isBlank()) continue
            val dpi = o.optInt("Dpi", 0)
            // MultiBit entry punya MultiDpi
            val multiDpi = o.optInt("MultiDpi", 0)
            val effectiveDpi = if (multiDpi > 0) multiDpi else dpi
            // Kalau Dpi 0 tapi ada MultiMap, ambil 720
            if (effectiveDpi == 0) {
                val multiMap = o.optJSONArray("MultiMap")
                if (multiMap != null && multiMap.length() > 0) {
                    // Pilih 720 dari MultiMap jika ada
                    var chosen = url
                    for (k in 0 until multiMap.length()) {
                        val m = multiMap.optJSONObject(k) ?: continue
                        if (m.optInt("Dpi", 0) == 720) {
                            // URL multi bit biasanya sama, tapi tetap pakai url utama yang multi-bitrate
                            chosen = url
                            break
                        }
                    }
                    out.add(720 to chosen)
                    continue
                }
            }
            out.add((if (effectiveDpi == 0) 720 else effectiveDpi) to url)
        }
        return out
    }

    fun bestVideoUrl(json: JSONObject): String {
        val vids = parseVideos(json)
        return pickVideoUrl(vids)
    }

    // ── Search hints dari foryou/search_keyword_list atau suggestions ──
    fun searchHintsFromHome(json: JSONObject): List<String> {
        val data = json.optJSONObject("data") ?: json
        val arr = data.optJSONArray("search_keyword_list") ?: JSONArray()
        val out = mutableListOf<String>()
        for (i in 0 until arr.length()) {
            val s = arr.optString(i).trim()
            if (s.length >= 2) out.add(s)
        }
        return out.distinct().take(12)
    }

    fun searchHintsFromSuggestions(json: JSONObject): List<String> {
        // book_rank_data titles sebagai hint
        val books = booksFromSearch(json)
        return books.mapNotNull { it.optString("book_title").takeIf { t -> t.isNotBlank() } }.distinct().take(12)
    }

    // ── Helper untuk Drama mapping ──
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
