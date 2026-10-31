package com.dramaku.app.catalog

import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder

/**
 * FilmKu — platform film (bukan short drama) yang memakai API vidbox.wtf.
 *
 * Sumber data (hasil reverse-engineering + uji langsung, 31 Okt 2026):
 *
 * - Daftar film & search  : GET https://vidbox.wtf/api/search/discover
 *                           (20 item/halaman, paging s/d page=500)
 * - Autocomplete          : GET https://vidbox.wtf/api/search/suggest?q=  (maks 5 hasil)
 * - Detail                : GET https://api.themoviedb.org/3/{movie|tv}/{id}  (TMDB, key dari bundle vidbox)
 * - Stream                : iframe provider pihak ketiga, id = TMDB ID.
 *                           Beberapa provider juga menerima pola /tv/{id}/{season}/{episode}.
 *
 * Catatan penting:
 * - vidbox TIDAK punya endpoint stream yang hidup. `/api/stream/…` milik streamed.st
 *   yang sudah tutup, `/watch/movie` balas 404. Yang dipakai vidbox sendiri adalah
 *   iframe provider di bawah — itulah yang direplikasi di sini.
 * - Semua respons di-parse defensif: field boleh hilang, tipe data boleh berubah.
 */
object FilmKuCatalog {

    const val PLATFORM_ID = "filmku"
    const val LABEL = "FilmKu"
    const val BASE_URL = "https://vidbox.wtf"
    const val TMDB_BASE = "https://api.themoviedb.org/3"
    const val TMDB_IMG = "https://image.tmdb.org/t/p/w500"
    const val LOGO_URL = "https://vidbox.wtf/logo.png"

    /** API key yang di-hardcode di bundle JS vidbox. Publik & bisa kena rate-limit bareng situs lain. */
    const val TMDB_KEY = "ef311eb0b9b07b9c73e9fb0a732cc150"

    const val PAGE_SIZE = 20
    const val MAX_PAGE = 500

    // ────────────────────────────────────────────────────────────────
    // URL builder
    // ────────────────────────────────────────────────────────────────

    private fun enc(v: String): String = URLEncoder.encode(v, "UTF-8")

    /** Daftar film. `sort` diabaikan server (selalu popularity) jadi tidak dikirim. */
    fun discoverUrl(
        page: Int = 1,
        type: String = "movie",
        genre: String = "all",
        year: String = "all",
        country: String = "all",
        watchProvider: String = "all",
        rating: String = "all",
        query: String = "",
    ): String {
        val q = query.trim()
        val params = buildList {
            add("type=$type")
            add("page=${page.coerceIn(1, MAX_PAGE)}")
            if (q.isNotBlank()) add("q=${enc(q)}")
            add("genre=$genre")
            add("year=$year")
            add("watch_provider=$watchProvider")
            add("country=$country")
            add("rating=$rating")
            add("now_playing=false")
            add("trending=false")
        }
        return "$BASE_URL/api/search/discover?${params.joinToString("&")}"
    }

    /** Autocomplete: maksimal 5 hasil, ringan. */
    fun suggestUrl(query: String): String = "$BASE_URL/api/search/suggest?q=${enc(query.trim())}"

    /** Detail TMDB. `append_to_response` menambah credits/videos/external_ids/imdb_id. */
    fun detailUrl(mediaId: String, mediaType: String = "movie"): String {
        val t = if (mediaType == "tv") "tv" else "movie"
        return "$TMDB_BASE/$t/${enc(mediaId)}?language=en-US&api_key=$TMDB_KEY" +
            "&append_to_response=credits,videos,external_ids"
    }

    /** Hanya kalau butuh daftar episode per season (bukan bagian dari playlist episode). */
    fun seasonUrl(mediaId: String, season: Int): String =
        "$TMDB_BASE/tv/${enc(mediaId)}/season/$season?language=en-US&api_key=$TMDB_KEY"

    fun posterUrl(path: String): String =
        if (path.isBlank()) "" else if (path.startsWith("http")) path else TMDB_IMG + path

    // ────────────────────────────────────────────────────────────────
    // Provider stream (iframe). Hasil uji 31 Okt 2026.
    // ────────────────────────────────────────────────────────────────

    data class EmbedProvider(
        val id: String,
        val label: String,
        val movieUrl: String,
        val tvUrl: String,
        val idType: String = "tmdb",   // "tmdb" | "imdb"
        /**
         * Status HTTP terakhir. 200 = halaman terbuka, TAPI belum tentu bisa memutar
         * di dalam iframe (ada yang 200 lalu memasang X-Frame-Options, atau 200 dengan
         * body "not found").
         */
        val status: Int = 200,
        /**
         * `embeddable` = hasil uji di dalam iframe (origin induk https, WebView app),
         * bukan sekadar cek HTTP:
         *   "play"  → memutar video (poster/kontrol tampil, ada request media)
         *   "blank" → halaman terbuka tapi tidak memutar (XFO, 404, sumber kosong)
         *   "deny"  → menolak di-iframe (X-Frame-Options / frame-ancestors)
         */
        val embeddable: String = "play",
        val note: String = "",
    )

    /**
     * Urutan uji 31 Okt 2026 (iframe di WebView-style, 390x780, TMDB 155 & 1399).
     * Yang memutar ditaruh paling atas karena `HEALTHY_PROVIDERS.first()` adalah
     * sumber default — dulu Vidlink di posisi pertama padahal di dalam iframe ia
     * hanya menampilkan poster dengan posisi 0:00.
     */
    val PROVIDERS: List<EmbedProvider> = listOf(
        EmbedProvider("vidrock", "Vidrock", "https://vidrock.net/embed/movie/{id}?autoplay=true", "https://vidrock.net/embed/tv/{id}/{season}/{episode}?autoplay=true", embeddable = "play", note = "film terverifikasi memutar (durasi & kontrol tampil); serial belum"),
        EmbedProvider("111movies", "111Movies", "https://111movies.net/movie/{id}", "https://111movies.net/tv/{id}/{season}/{episode}", embeddable = "play", note = "sumber HLS punkflix; lambat saat pemanasan, serial belum"),
        EmbedProvider("vidrift", "Vidrift", "https://embed.vidrift.net/embed/movie/{id}?autoplay=true", "https://embed.vidrift.net/embed/tv/{id}/{season}/{episode}?autoplay=true", embeddable = "play", note = "film memutar (HLS hscow); serial masih LOADING saat diuji"),
        EmbedProvider("vidlink", "Vidlink", "https://vidlink.pro/movie/{id}", "https://vidlink.pro/tv/{id}/{season}/{episode}", embeddable = "blank", note = "poster tampil tapi video tidak mulai di iframe"),
        EmbedProvider("nxsha", "Nxsha", "https://nxsha.space/embed/movie/{id}?lang={lang}&autoplay=true&sub={lang}", "https://nxsha.space/embed/tv/{id}/{season}/{episode}?lang={lang}&autoplay=true&sub={lang}", embeddable = "blank", note = "VIDEOJS MEDIA_ERR_SRC_NOT_SUPPORTED"),
        EmbedProvider("videasy", "Videasy", "https://www.vidy.st/movie/{id}", "https://www.vidy.st/tv/{id}/{season}/{episode}", embeddable = "blank", note = "galat JS saat memuat sumber"),
        EmbedProvider("vidnest", "Vidnest", "https://vidnest.fun/movie/{id}", "https://vidnest.fun/tv/{id}/{season}/{episode}", embeddable = "blank", note = "film tidak ditemukan + secureDecrypt butuh konteks browser"),
        EmbedProvider("vapar", "Vapar", "https://vaplayer.ru/embed/movie/{id}?autoplay=true", "https://vaplayer.ru/embed/tv/{id}/{season}/{episode}?autoplay=true", embeddable = "blank", note = "iframe induknya diblokir NotSameOrigin"),
        EmbedProvider("ythd", "Ythd", "https://ythd.org/embed/{id}", "https://ythd.org/embed/{id}/{season}/{episode}", embeddable = "blank", note = "halaman menolak: Verification failed"),
        EmbedProvider("xpass", "Xpass", "https://play.xpass.top/e/movie/{id}", "https://play.xpass.top/e/tv/{id}/{season}/{episode}", embeddable = "blank", note = "404 page not found"),
        EmbedProvider("moviesapi", "Moviesapi", "https://moviesapi.vip/movie/{id}", "https://moviesapi.vip/tv/{id}/{season}/{episode}", embeddable = "blank", note = "404 NOT FOUND"),
        EmbedProvider("cinemaos", "Cinemaos", "https://cinemaos.tech/player/{id}", "https://cinemaos.tech/player/{id}/{season}/{episode}", embeddable = "deny", note = "X-Frame-Options menolak iframe"),
        EmbedProvider("2embed", "2Embed", "https://2embed.stream/embed/movie/{id}", "https://2embed.stream/embed/tv/{id}/{season}/{episode}", embeddable = "deny", note = "frame-ancestors menolak iframe"),
        EmbedProvider("primesrc", "Primesrc", "https://primesrc.me/embed/movie?imdb={imdb}", "https://primesrc.me/embed/tv?imdb={imdb}&season={season}&episode={episode}", idType = "imdb", embeddable = "deny", note = "frame-ancestors menolak iframe"),
        EmbedProvider("vidfast", "Vidfast", "https://vidfast.vc/movie/{id}?autoplay=true", "https://vidfast.vc/tv/{id}/{season}/{episode}?autoplay=true", embeddable = "deny", note = "X-Frame-Options menolak iframe"),
        EmbedProvider("vixsrc", "Vixsrc", "https://vixsrc.to/movie/{id}", "https://vixsrc.to/tv/{id}/{season}/{episode}", status = 403, embeddable = "deny", note = "Cloudflare 403"),
        EmbedProvider("peachify", "Peachify", "https://peachify.top/embed/movie/{id}", "https://peachify.top/embed/tv/{id}/{season}/{episode}", status = 403, embeddable = "deny", note = "Cloudflare 403"),
    )

    /** Provider yang status HTTP-nya 200 (belum tentu bisa memutar di iframe). */
    val HEALTHY_PROVIDERS: List<EmbedProvider> get() = PROVIDERS.filter { it.status == 200 }

    /** Provider yang benar-benar memutar di dalam iframe — dipakai sebagai default. */
    val PLAYABLE_PROVIDERS: List<EmbedProvider> get() = PROVIDERS.filter { it.embeddable == "play" }

    /**
     * Daftar untuk pemilih "sumber video" di pemutar: yang memutar dulu, lalu sisanya
     * (ditandai di UI) supaya pengguna masih bisa mencoba kalau yang atas sedang mati.
     */
    val PROVIDER_CHOICES: List<EmbedProvider> get() = PROVIDERS.filter { it.status == 200 }

    fun providerById(id: String?): EmbedProvider? = PROVIDERS.firstOrNull { it.id == id }

    /**
     * URL embed untuk satu episode.
     *
     * @param tmdbId TMDB id (dipakai sebagian besar provider)
     * @param imdbId IMDb id `tt…` (wajib untuk provider ber-idType "imdb", mis. Primesrc)
     */
    fun embedUrl(
        provider: EmbedProvider,
        tmdbId: String,
        imdbId: String = "",
        mediaType: String = "movie",
        season: Int = 1,
        episode: Int = 1,
        lang: String = "en",
    ): String {
        val isTv = mediaType == "tv"
        val tpl = if (isTv) provider.tvUrl else provider.movieUrl
        val idValue = if (provider.idType == "imdb") imdbId.ifBlank { tmdbId } else tmdbId
        return tpl
            .replace("{id}", enc(idValue))
            .replace("{imdb}", enc(imdbId.ifBlank { tmdbId }))
            .replace("{season}", season.coerceAtLeast(1).toString())
            .replace("{episode}", episode.coerceAtLeast(1).toString())
            .replace("{lang}", lang)
    }

    /** Rantai fallback: provider sehat sesuai urutan, dipakai kalau yang sebelumnya gagal di WebView. */
    fun embedFallbacks(tmdbId: String, imdbId: String = "", mediaType: String = "movie", season: Int = 1, episode: Int = 1): List<String> =
        PROVIDER_CHOICES.map { embedUrl(it, tmdbId, imdbId, mediaType, season, episode) }

    // ────────────────────────────────────────────────────────────────
    // Parser
    // ────────────────────────────────────────────────────────────────

    /** Satu item katalog (daftar film / hasil search). */
    data class FilmItem(
        val id: String,          // TMDB id sebagai string
        val mediaType: String,   // "movie" | "tv"
        val title: String,
        val posterPath: String,
        val overview: String,
        val year: String,
        val rating: Double,
        val votes: Int,
    )

    /** Hasil /api/search/discover — sekaligus dipakai untuk search (?q=). */
    data class DiscoverPage(val items: List<FilmItem>, val page: Int, val found: Int, val totalPages: Int)

    private fun JSONObject.str(vararg keys: String): String {
        keys.forEach { k ->
            val v = opt(k)
            if (v != null && v != JSONObject.NULL && v !is JSONObject && v !is JSONArray) {
                val s = v.toString().trim()
                if (s.isNotBlank()) return s
            }
        }
        return ""
    }

    private fun JSONObject.num(vararg keys: String): Double {
        keys.forEach { k ->
            val v = opt(k)
            if (v is Number) return v.toDouble()
            if (v is String) v.toDoubleOrNull()?.let { return it }
        }
        return 0.0
    }

    private fun JSONArray.objects(): List<JSONObject> = (0 until length()).mapNotNull { optJSONObject(it) }

    fun parseItem(o: JSONObject): FilmItem? {
        val id = o.str("id")
        // media_type bisa hilang di beberapa respons → tebak dari field judul.
        val type = o.str("media_type").ifBlank { if (o.has("name") && !o.has("title")) "tv" else "movie" }
        val title = o.str("title", "name", "original_title", "original_name")
        if (id.isBlank() || title.isBlank()) return null
        val date = o.str("release_date", "first_air_date")
        return FilmItem(
            id = id,
            mediaType = if (type == "tv") "tv" else "movie",
            title = title,
            posterPath = o.str("poster_path", "backdrop_path"),
            overview = o.str("overview"),
            year = date.take(4),
            rating = o.num("vote_average"),
            votes = o.num("vote_count").toInt(),
        )
    }

    fun parseDiscover(json: JSONObject, requestedPage: Int = 1): DiscoverPage {
        val arr = json.optJSONArray("results") ?: JSONArray()
        val items = arr.objects().mapNotNull { parseItem(it) }
        return DiscoverPage(
            items = items,
            page = requestedPage,
            found = json.optInt("total_results", items.size),
            totalPages = json.optInt("total_pages", if (items.isEmpty()) 1 else MAX_PAGE),
        )
    }

    /** /api/search/suggest → `{ success, data: [ ... ] }` */
    fun parseSuggest(json: JSONObject): List<FilmItem> =
        (json.optJSONArray("data") ?: JSONArray()).objects().mapNotNull { parseItem(it) }

    /** Item bisa datang sebagai array root (suggest) atau objek ber-`results`. */
    fun parseAnyList(json: JSONObject): List<FilmItem> =
        (json.optJSONArray("results") ?: json.optJSONArray("data") ?: JSONArray())
            .objects().mapNotNull { parseItem(it) }

    /** Detail TMDB. */
    data class FilmDetail(
        val id: String,
        val mediaType: String,
        val title: String,
        val overview: String,
        val posterPath: String,
        val year: String,
        val runtimeMinutes: Int,
        val rating: Double,
        val genres: List<String>,
        val imdbId: String,               // ← kunci untuk provider ber-idType "imdb"
        val cast: List<String>,
        val seasons: Int,
        val episodes: Int,
        val status: String,
        val tagline: String,
    )

    fun parseDetail(json: JSONObject, mediaType: String = "movie"): FilmDetail {
        val isTv = mediaType == "tv"
        val genres = mutableListOf<String>()
        json.optJSONArray("genres")?.objects()?.forEach { g ->
            val n = g.str("name")
            if (n.isNotBlank()) genres += n
        }
        val cast = mutableListOf<String>()
        json.optJSONObject("credits")?.optJSONArray("cast")?.objects()?.take(10)?.forEach { c ->
            val n = c.str("name")
            if (n.isNotBlank()) cast += n
        }
        val date = json.str("release_date", "first_air_date")
        val runtime = if (isTv) json.optJSONArray("episode_run_time")?.optInt(0, 0) ?: 0
                      else json.optInt("runtime", 0)
        return FilmDetail(
            id = json.str("id"),
            mediaType = if (isTv) "tv" else "movie",
            title = json.str("title", "name", "original_title", "original_name"),
            overview = json.str("overview"),
            posterPath = json.str("poster_path"),
            year = date.take(4),
            runtimeMinutes = runtime.coerceAtLeast(0),
            rating = json.num("vote_average"),
            genres = genres,
            imdbId = json.str("imdb_id", "external_ids.imdb_id")
                .ifBlank { json.optJSONObject("external_ids")?.str("imdb_id").orEmpty() },
            cast = cast,
            seasons = json.optInt("number_of_seasons", 0),
            episodes = json.optInt("number_of_episodes", 0),
            status = json.str("status"),
            tagline = json.str("tagline"),
        )
    }

    /**
     * Episode untuk pemutar milik Dramaku.
     *
     * - movie → 1 episode sintetis ("Film penuh", nomor 1)
     * - tv    → satu episode per nomor 1..number_of_episodes (season dioper terpisah
     *           ke resolver, karena pola embed `{season}` diisi dari situ)
     *
     * `streaming` sengaja dikosongkan: URL embed dibangun di resolver, bukan disimpan
     * di EpisodeInfo (supaya ganti provider tidak perlu reload detail).
     */
    fun episodes(detail: FilmDetail): List<Pair<Int, String>> {
        if (detail.mediaType != "tv") return listOf(1 to "Film penuh")
        val n = detail.episodes.coerceAtLeast(1)
        return (1..n).map { it to "Episode $it" }
    }

    /** Label ringkas untuk kartu: "Film • 2008 • ⭐ 8.5" */
    fun subtitleOf(item: FilmItem): String = buildString {
        append(if (item.mediaType == "tv") "Serial" else "Film")
        if (item.year.isNotBlank()) append(" • ${item.year}")
        if (item.rating > 0) append(" • ⭐ ${"%.1f".format(item.rating)}")
    }
}
