package com.dramaku.app.catalog

import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder

/**
 * Anichin catalog untuk endpoint dramahub.be/anichin
 *
 * Endpoints:
 * 1. GET /home -> popular + latest
 * 2. GET /genres -> list genre
 * 3. GET /genre/{slug}/ -> anime by genre
 * 4. GET /ongoing/ -> ongoing anime
 * 5. GET /completed/ -> completed anime
 * 6. GET /schedule -> jadwal harian
 * 7. GET /search/{keyword} -> search anime
 * 8. GET /anime/{slug} -> detail anime + episode list
 * 9. GET /episode/{slug} -> streaming servers
 *
 * Semua parsing murni tanpa jaringan.
 */
data class AnichinAnime(
    val title: String,
    val slug: String,
    val poster: String,
    val episode: String = "",
    val type: String = "",
    val status: String = ""
)

data class AnichinGenre(val name: String, val slug: String)

data class AnichinScheduleDay(val day: String, val anime: List<AnichinAnime>)

data class AnichinEpisode(
    val number: String,
    val title: String,
    val slug: String,
    val date: String = ""
)

data class AnichinServer(val name: String, val url: String)

object AnichinCatalog {

    const val HOME_PATH = "home"
    const val GENRES_PATH = "genres"
    const val ONGOING_PATH = "ongoing/"
    const val COMPLETED_PATH = "completed/"
    const val SCHEDULE_PATH = "schedule"

    fun genrePath(slug: String): String = "genre/${URLEncoder.encode(slug, "UTF-8")}/"

    fun searchPath(keyword: String): String = "search/${URLEncoder.encode(keyword.trim(), "UTF-8")}"

    fun detailPath(slug: String): String = "anime/${URLEncoder.encode(slug, "UTF-8")}"

    fun episodePath(slug: String): String = "episode/${URLEncoder.encode(slug, "UTF-8")}"

    // ── Home: data.popular + data.latest ──
    fun parseHome(json: JSONObject): Pair<List<AnichinAnime>, List<AnichinAnime>> {
        val data = json.optJSONObject("data") ?: json
        val popularArr = data.optJSONArray("popular") ?: JSONArray()
        val latestArr = data.optJSONArray("latest") ?: JSONArray()
        return parseAnimeArray(popularArr) to parseAnimeArray(latestArr)
    }

    fun parseAnimeArray(arr: JSONArray): List<AnichinAnime> {
        val out = mutableListOf<AnichinAnime>()
        for (i in 0 until arr.length()) {
            val o = arr.optJSONObject(i) ?: continue
            val title = o.optString("title").trim()
            val slug = o.optString("slug").trim()
            if (title.isBlank() || slug.isBlank()) continue
            out.add(
                AnichinAnime(
                    title = title,
                    slug = slug,
                    poster = fixPoster(o.optString("poster")),
                    episode = o.optString("episode"),
                    type = o.optString("type"),
                    status = o.optString("status")
                )
            )
        }
        return out
    }

    // Genres: data = [{name, slug}]
    fun parseGenres(json: JSONObject): List<AnichinGenre> {
        val data = json.optJSONArray("data") ?: json.optJSONObject("data")?.optJSONArray("data") ?: JSONArray()
        // kadang data langsung array di root? cek status ok
        val arr = if (data.length() == 0) {
            // fallback kalau data di root adalah array? tapi struktur kita udah status ok
            json.optJSONArray("data") ?: JSONArray()
        } else data
        val out = mutableListOf<AnichinGenre>()
        for (i in 0 until arr.length()) {
            val o = arr.optJSONObject(i) ?: continue
            val name = o.optString("name").trim()
            val slug = o.optString("slug").trim()
            if (name.isBlank() || slug.isBlank()) continue
            out.add(AnichinGenre(name, slug))
        }
        return out
    }

    // Genre / ongoing / completed / search: data = [{title, slug, poster, episode, type, status}]
    fun parseList(json: JSONObject): List<AnichinAnime> {
        val listArr = json.optJSONArray("data") ?: JSONArray()
        return parseAnimeArray(listArr)
    }

    // Schedule: data = [{day, anime: [{title, slug, time, poster}]}]
    fun parseSchedule(json: JSONObject): List<AnichinScheduleDay> {
        val arr = json.optJSONArray("data") ?: JSONArray()
        val out = mutableListOf<AnichinScheduleDay>()
        for (i in 0 until arr.length()) {
            val dayObj = arr.optJSONObject(i) ?: continue
            val day = dayObj.optString("day").trim()
            if (day.isBlank()) continue
            val animeArr = dayObj.optJSONArray("anime") ?: JSONArray()
            val animeList = mutableListOf<AnichinAnime>()
            for (j in 0 until animeArr.length()) {
                val a = animeArr.optJSONObject(j) ?: continue
                val title = a.optString("title").trim()
                val slug = a.optString("slug").trim()
                if (title.isBlank() || slug.isBlank()) continue
                animeList.add(
                    AnichinAnime(
                        title = title,
                        slug = slug,
                        poster = fixPoster(a.optString("poster")),
                        episode = a.optString("time"), // time dipakai sebagai episode label di schedule
                        type = "",
                        status = ""
                    )
                )
            }
            out.add(AnichinScheduleDay(day, animeList))
        }
        return out
    }

    // Detail: data = {title, poster, synopsis, genres[], info:{...}, episodes:[{number, title, slug, date}]}
    data class Detail(
        val title: String,
        val poster: String,
        val synopsis: String,
        val genres: List<String>,
        val info: Map<String, String>,
        val episodes: List<AnichinEpisode>
    )

    fun parseDetail(json: JSONObject): Detail? {
        val data = json.optJSONObject("data") ?: return null
        val title = data.optString("title").trim()
        if (title.isBlank()) return null
        val poster = fixPoster(data.optString("poster"))
        val synopsis = data.optString("synopsis").trim()
        val genresArr = data.optJSONArray("genres") ?: JSONArray()
        val genres = mutableListOf<String>()
        for (i in 0 until genresArr.length()) {
            val g = genresArr.optString(i).trim()
            if (g.isNotBlank()) genres.add(g)
        }
        val infoObj = data.optJSONObject("info") ?: JSONObject()
        val info = mutableMapOf<String, String>()
        val keys = infoObj.keys()
        while (keys.hasNext()) {
            val k = keys.next()
            val v = infoObj.optString(k).trim()
            if (v.isNotBlank()) info[k] = v
        }
        val epsArr = data.optJSONArray("episodes") ?: JSONArray()
        val episodes = mutableListOf<AnichinEpisode>()
        for (i in 0 until epsArr.length()) {
            val e = epsArr.optJSONObject(i) ?: continue
            val slug = e.optString("slug").trim()
            if (slug.isBlank()) continue
            episodes.add(
                AnichinEpisode(
                    number = e.optString("number").trim(),
                    title = e.optString("title").trim(),
                    slug = slug,
                    date = e.optString("date").trim()
                )
            )
        }
        return Detail(title, poster, synopsis, genres, info, episodes)
    }

    // Episode: data = {title, streaming, servers:[{name, url}]}
    data class EpisodeStream(
        val title: String,
        val primaryUrl: String,
        val servers: List<AnichinServer>
    )

    fun parseEpisode(json: JSONObject): EpisodeStream? {
        val data = json.optJSONObject("data") ?: return null
        val title = data.optString("title").trim()
        val streaming = data.optString("streaming").trim()
        val serversArr = data.optJSONArray("servers") ?: JSONArray()
        val servers = mutableListOf<AnichinServer>()
        for (i in 0 until serversArr.length()) {
            val s = serversArr.optJSONObject(i) ?: continue
            val name = s.optString("name").trim()
            val url = s.optString("url").trim()
            if (url.isBlank()) continue
            servers.add(AnichinServer(name.ifBlank { "Server ${i+1}" }, url))
        }
        // primary streaming masukkan sebagai server pertama kalau belum ada
        if (streaming.isNotBlank() && servers.none { it.url == streaming }) {
            servers.add(0, AnichinServer("Primary", streaming))
        }
        return EpisodeStream(title, streaming, servers)
    }

    // Poster di anichin kadang relatif /wp-content/... -> perlu jadi absolute ke anichin
    fun fixPoster(poster: String): String {
        if (poster.isBlank()) return ""
        if (poster.startsWith("http")) {
            return poster.replace("https://anichin.be", "https://anichin.moe")
                .replace("http://anichin.be", "https://anichin.moe")
                .replace("https://anichin.care", "https://anichin.moe")
                .replace("https://anichin.cafe", "https://anichin.moe")
                .replace("https://anichin.academy", "https://anichin.moe")
        }
        return if (poster.startsWith("/")) "https://anichin.moe$poster" else poster
    }

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
