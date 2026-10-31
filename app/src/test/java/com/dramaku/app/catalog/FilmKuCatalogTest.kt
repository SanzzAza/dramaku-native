package com.dramaku.app.catalog

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit test untuk parser FilmKuCatalog.
 * Balasan JSON di bawah diambil dari respons asli endpoint (31 Okt 2026).
 */
class FilmKuCatalogTest {

    // ── /api/search/discover ────────────────────────────────────────
    private val discoverResponse = JSONObject(
        """
        {
          "results": [
            {
              "adult": false,
              "backdrop_path": "/9FE5eD92WfVCiivM9Pq9GVSrlWk.jpg",
              "genre_ids": [28, 53, 80],
              "id": 155,
              "title": "The Dark Knight",
              "original_language": "en",
              "original_title": "The Dark Knight",
              "overview": "Batman raises the stakes in his war on crime.",
              "popularity": 48.3658,
              "poster_path": "/qJ2tW6WMUDux911r6m7haRef0WH.jpg",
              "release_date": "2008-07-16",
              "softcore": false,
              "video": false,
              "media_type": "movie",
              "vote_average": 8.539,
              "vote_count": 15845
            },
            {
              "id": 1399,
              "name": "Game of Thrones",
              "original_name": "Game of Thrones",
              "overview": "Seven noble families fight for control.",
              "poster_path": "/1XS1oqL89opfnbLl8WnZY1O1uJx.jpg",
              "first_air_date": "2011-04-17",
              "vote_average": 8.4,
              "vote_count": 22000
            }
          ],
          "total_pages": 500
        }
        """.trimIndent()
    )

    @Test
    fun `parseDiscover reads movie items`() {
        val page = FilmKuCatalog.parseDiscover(discoverResponse)
        assertEquals(2, page.items.size)
        assertEquals(500, page.totalPages)

        val film = page.items[0]
        assertEquals("155", film.id)
        assertEquals("movie", film.mediaType)
        assertEquals("The Dark Knight", film.title)
        assertEquals("2008", film.year)
        assertEquals(8.539, film.rating, 0.001)
        assertEquals("/qJ2tW6WMUDux911r6m7haRef0WH.jpg", film.posterPath)
    }

    @Test
    fun `parseDiscover detects tv when media_type missing`() {
        val tv = FilmKuCatalog.parseDiscover(discoverResponse).items[1]
        assertEquals("1399", tv.id)
        assertEquals("tv", tv.mediaType)   // ditebak dari ada `name`, tanpa `title`
        assertEquals("Game of Thrones", tv.title)
        assertEquals("2011", tv.year)
    }

    @Test
    fun `parseDiscover survives empty upstream (page beyond 500)`() {
        val empty = JSONObject("""{"results":[],"total_pages":1}""")
        val page = FilmKuCatalog.parseDiscover(empty, requestedPage = 501)
        assertTrue(page.items.isEmpty())
        assertEquals(1, page.totalPages)   // server menjawab total_pages 1 untuk page>500
    }

    // ── /api/search/suggest ─────────────────────────────────────────
    private val suggestResponse = JSONObject(
        """
        {
          "success": true,
          "data": [
            {
              "id": 414906,
              "title": "The Batman",
              "poster_path": "/74xTEgt7R36Fpooo50r9T25onhq.jpg",
              "release_date": "2022-03-01",
              "vote_average": 7.6,
              "media_type": "movie"
            },
            {
              "id": 2287,
              "name": "Batman",
              "poster_path": "/c4zV2ysi5Qlns4CPjRJj3hBGMSx.jpg",
              "first_air_date": "1966-01-12",
              "vote_average": 7.2,
              "media_type": "tv"
            }
          ]
        }
        """.trimIndent()
    )

    @Test
    fun `parseSuggest handles mixed movie and tv`() {
        val list = FilmKuCatalog.parseSuggest(suggestResponse)
        assertEquals(2, list.size)
        assertEquals("movie", list[0].mediaType)
        assertEquals("tv", list[1].mediaType)
        assertEquals("Batman", list[1].title)
    }

    @Test
    fun `parseSuggest ignores junk rows`() {
        val junk = JSONObject("""{"success":true,"data":[{"id":null,"title":null},{"id":1,"title":"Ada"}]}""")
        val list = FilmKuCatalog.parseSuggest(junk)
        assertEquals(1, list.size)
        assertEquals("Ada", list[0].title)
    }

    // ── Detail TMDB ─────────────────────────────────────────────────
    private val detailResponse = JSONObject(
        """
        {
          "id": 155,
          "title": "The Dark Knight",
          "overview": "Batman raises the stakes in his war on crime.",
          "poster_path": "/qJ2tW6WMUDux911r6m7haRef0WH.jpg",
          "release_date": "2008-07-16",
          "runtime": 152,
          "status": "Released",
          "tagline": "Why So Serious?",
          "imdb_id": "tt0468569",
          "vote_average": 8.539,
          "genres": [
            {"id": 28, "name": "Action"},
            {"id": 53, "name": "Thriller"},
            {"id": 80, "name": "Crime"}
          ],
          "credits": {
            "cast": [
              {"name": "Christian Bale"},
              {"name": "Heath Ledger"},
              {"name": "Aaron Eckhart"}
            ]
          }
        }
        """.trimIndent()
    )

    @Test
    fun `parseDetail reads core metadata and imdb id`() {
        val d = FilmKuCatalog.parseDetail(detailResponse)
        assertEquals("The Dark Knight", d.title)
        assertEquals(152, d.runtimeMinutes)
        assertEquals(listOf("Action", "Thriller", "Crime"), d.genres)
        assertEquals("tt0468569", d.imdbId)          // dipakai provider IMDb
        assertEquals(listOf("Christian Bale", "Heath Ledger", "Aaron Eckhart"), d.cast)
        assertEquals("movie", d.mediaType)
    }

    @Test
    fun `parseDetail falls back to external_ids imdb`() {
        val nested = JSONObject("""{"id":1,"title":"X","external_ids":{"imdb_id":"tt1234567"}}""")
        assertEquals("tt1234567", FilmKuCatalog.parseDetail(nested).imdbId)
    }

    @Test
    fun `parseDetail reads tv counters`() {
        val tv = JSONObject("""{"id":1399,"name":"Game of Thrones","number_of_seasons":8,"number_of_episodes":73,"episode_run_time":[57]}""")
        val d = FilmKuCatalog.parseDetail(tv, mediaType = "tv")
        assertEquals("tv", d.mediaType)
        assertEquals(8, d.seasons)
        assertEquals(73, d.episodes)
        assertEquals(57, d.runtimeMinutes)
    }

    // ── URL builder ─────────────────────────────────────────────────
    @Test
    fun `discoverUrl builds expected query`() {
        val url = FilmKuCatalog.discoverUrl(page = 2, genre = "28", country = "ID")
        assertTrue(url.startsWith("https://vidbox.wtf/api/search/discover?"))
        assertTrue(url.contains("page=2"))
        assertTrue(url.contains("genre=28"))
        assertTrue(url.contains("country=ID"))
        assertTrue(url.contains("type=movie"))
    }

    @Test
    fun `discoverUrl clamps page above 500`() {
        assertTrue(FilmKuCatalog.discoverUrl(page = 9000).contains("page=500"))
    }

    @Test
    fun `discoverUrl encodes search query`() {
        val url = FilmKuCatalog.discoverUrl(query = "the dark knight")
        assertTrue(url.contains("q=the+dark+knight") || url.contains("q=the%20dark%20knight"))
    }

    @Test
    fun `suggestUrl encodes query`() {
        assertEquals(
            "https://vidbox.wtf/api/search/suggest?q=batman",
            FilmKuCatalog.suggestUrl("batman")
        )
    }

    @Test
    fun `detailUrl switches endpoint for tv`() {
        assertTrue(FilmKuCatalog.detailUrl("1399", "tv").contains("/tv/1399"))
        assertTrue(FilmKuCatalog.detailUrl("155", "movie").contains("/movie/155"))
        assertTrue(FilmKuCatalog.detailUrl("155").contains(FilmKuCatalog.TMDB_KEY))
    }

    // ── Embed provider ──────────────────────────────────────────────
    @Test
    fun `embedUrl fills tmdb id for movie`() {
        val vidlink = FilmKuCatalog.PROVIDERS.first { it.id == "vidlink" }
        assertEquals(
            "https://vidlink.pro/movie/155",
            FilmKuCatalog.embedUrl(vidlink, tmdbId = "155")
        )
    }

    @Test
    fun `embedUrl fills season and episode for tv`() {
        val vidlink = FilmKuCatalog.PROVIDERS.first { it.id == "vidlink" }
        assertEquals(
            "https://vidlink.pro/tv/1399/2/5",
            FilmKuCatalog.embedUrl(vidlink, tmdbId = "1399", mediaType = "tv", season = 2, episode = 5)
        )
    }

    @Test
    fun `embedUrl uses imdb id for primesrc`() {
        val primesrc = FilmKuCatalog.PROVIDERS.first { it.id == "primesrc" }
        assertEquals(
            "https://primesrc.me/embed/movie?imdb=tt0468569",
            FilmKuCatalog.embedUrl(primesrc, tmdbId = "155", imdbId = "tt0468569")
        )
    }

    @Test
    fun `embedUrl substitutes language placeholder`() {
        val nxsha = FilmKuCatalog.PROVIDERS.first { it.id == "nxsha" }
        val url = FilmKuCatalog.embedUrl(nxsha, tmdbId = "155", lang = "id")
        assertTrue(url.contains("lang=id"))
        assertTrue(url.contains("sub=id"))
    }

    @Test
    fun `embedFallbacks only uses healthy providers`() {
        val chain = FilmKuCatalog.embedFallbacks("155", "tt0468569")
        assertTrue(chain.isNotEmpty())
        // provider 403 tidak boleh masuk rantai otomatis
        assertTrue(chain.none { it.contains("vixsrc.to") })
        assertTrue(chain.none { it.contains("peachify") })
    }

    @Test
    fun `default provider is one that actually plays in an iframe`() {
        // Regresi: dulu daftar dimulai Vidlink yang cuma menampilkan poster di iframe,
        // sehingga pemutar menampilkan layar hitam tanpa pesan apa pun.
        val first = FilmKuCatalog.PLAYABLE_PROVIDERS.first()
        assertEquals("vidrock", first.id)
        assertEquals("play", first.embeddable)
        assertTrue(FilmKuCatalog.PLAYABLE_PROVIDERS.all { it.status == 200 })
    }

    @Test
    fun `providers that refuse iframes are not playable`() {
        val playable = FilmKuCatalog.PLAYABLE_PROVIDERS.map { it.id }
        listOf("vidfast", "cinemaos", "2embed", "primesrc", "vixsrc", "peachify").forEach {
            assertTrue("$it seharusnya tidak dianggap playable", it !in playable)
        }
    }

    @Test
    fun `provider choices keep blocked ones out and playable ones on top`() {
        val choices = FilmKuCatalog.PROVIDER_CHOICES
        assertEquals(15, choices.size)
        assertEquals(3, FilmKuCatalog.PLAYABLE_PROVIDERS.size)
        assertEquals(choices.take(3).map { it.id }, FilmKuCatalog.PLAYABLE_PROVIDERS.map { it.id })
        assertTrue(choices.none { it.id == "vixsrc" })
    }

    @Test
    fun `providerById resolves known and missing ids`() {
        assertEquals("vidrock", FilmKuCatalog.providerById("vidrock")?.id)
        assertEquals(null, FilmKuCatalog.providerById("tidak-ada"))
        assertEquals(null, FilmKuCatalog.providerById(null))
    }

    @Test
    fun `posterUrl handles empty and absolute`() {
        assertEquals("", FilmKuCatalog.posterUrl(""))
        assertEquals("https://x/y.jpg", FilmKuCatalog.posterUrl("https://x/y.jpg"))
        assertEquals(
            "https://image.tmdb.org/t/p/w500/abc.jpg",
            FilmKuCatalog.posterUrl("/abc.jpg")
        )
    }

    // ── Episode mapping ─────────────────────────────────────────────
    @Test
    fun `episodes for movie is a single synthetic episode`() {
        val d = FilmKuCatalog.parseDetail(detailResponse)
        val eps = FilmKuCatalog.episodes(d)
        assertEquals(1, eps.size)
        assertEquals(1 to "Film penuh", eps[0])
    }

    @Test
    fun `episodes for tv expands to total episode count`() {
        val tv = FilmKuCatalog.parseDetail(
            JSONObject("""{"id":1399,"name":"GoT","number_of_episodes":73}"""), "tv"
        )
        val eps = FilmKuCatalog.episodes(tv)
        assertEquals(73, eps.size)
        assertEquals(1 to "Episode 1", eps.first())
        assertEquals(73 to "Episode 73", eps.last())
    }

    @Test
    fun `parseItem returns null for junk without crashing`() {
        assertNull(FilmKuCatalog.parseItem(JSONObject("""{"foo":"bar"}""")))
    }
}
