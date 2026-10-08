package com.dramaku.app.catalog

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NetshortCatalogTest {

    // Ringkasan asli /tabs dari dramahub.be/netshort/api/v1.
    private val tabsResponse = JSONObject(
        """
        {
          "code": 200,
          "data": [
            {"id": "1894702358019043329", "name": "Jelajahi", "type": 0, "isDefault": true},
            {"id": "1894773235170693121", "name": "Baru", "type": 3, "isDefault": false},
            {"id": "1925512700977860609", "name": "Kategori", "type": 2, "isDefault": false},
            {"id": "1965372594714603522", "name": "Dubbing", "type": 0, "isDefault": false},
            {"id": "2005494078870331394", "name": "VIP", "type": 4, "isDefault": false},
            {"id": "2028728342269657089", "name": "Anime", "type": 0, "isDefault": false},
            {"id": "1919694988940324866", "name": "Ranking", "type": 1, "isDefault": false}
          ]
        }
        """.trimIndent()
    )

    @Test
    fun `tabs skips entries without id or name`() {
        val tabs = NetshortCatalog.tabs(tabsResponse)
        assertEquals(7, tabs.size)
        assertEquals("Jelajahi", tabs.first().name)
        assertTrue(tabs.first().isDefault)
    }

    @Test
    fun `content tabs keep only plain content tabs so rows stay reachable`() {
        val rows = NetshortCatalog.contentTabs(tabsResponse)
        assertEquals(listOf("Dubbing", "Anime"), rows.map { it.name })
        // type 1 (Ranking), 2 (Kategori), 4 (VIP) punya halaman sendiri.
        assertTrue(rows.none { it.name == "Kategori" || it.name == "VIP" || it.name == "Ranking" })
        assertTrue(rows.none { it.isDefault })
    }

    // Ringkasan /categories: tag[0] = "Semua" (labelLanguageId -1).
    private val categoriesResponse = JSONObject(
        """
        {
          "code": 200,
          "data": {
            "region": [{"key": 0, "value": "Semua"}],
            "audio": [{"key": 0, "value": "Semua"}],
            "orderMode": [{"key": 1, "value": "Populer"}, {"key": 2, "value": "Terbaru"}],
            "tag": [
              {"labelLanguageId": -1, "labelName": "Semua", "newLabelIdList": ["1983832175759147019"]},
              {"labelLanguageId": 2099382095592521735, "labelName": "Romantis Urban", "newLabelIdList": ["1983832092736479243", "x"]},
              {"labelLanguageId": 2099383543214604294, "labelName": "Balas Dendam", "newLabelIdList": ["1983832036302733324"]},
              {"labelLanguageId": 0, "labelName": "Kosong", "newLabelIdList": []}
            ]
          }
        }
        """.trimIndent()
    )

    @Test
    fun `tags drop the all bucket and keep the first label id per tag`() {
        val tags = NetshortCatalog.tags(categoriesResponse)
        assertEquals(listOf("Romantis Urban", "Balas Dendam"), tags.map { it.name })
        assertEquals("1983832092736479243", tags.first().labelId)
    }

    @Test
    fun `search hints are stripped of emoji because the api rejects them`() {
        val hintJson = JSONObject(
            """
            {
              "code": 200,
              "data": [
                {"name": "Wanita Pemburu Pelaku KDRT🎯", "shortPlayId": "2087124383667376129"},
                {"name": "Kamu Membuangku, Aku Dikasihi Dia💕💕"},
                {"name": "Jalan Kebangkitan Gadis Gemuk 🔥"},
                {"name": "🎯🔥"},
                {"name": "Tinju Maut ⚡🆙"}
              ]
            }
            """.trimIndent()
        )
        val hints = NetshortCatalog.searchHints(hintJson)
        assertEquals(
            listOf("Wanita Pemburu Pelaku KDRT", "Kamu Membuangku, Aku Dikasihi Dia", "Jalan Kebangkitan Gadis Gemuk", "Tinju Maut"),
            hints
        )
    }

    @Test
    fun `cleanHint keeps normal punctuation but removes symbols`() {
        assertEquals("Ayah (Sulih Suara)", NetshortCatalog.cleanHint("Ayah (Sulih Suara)✨"))
        assertEquals("Cinta & Duit", NetshortCatalog.cleanHint(" Cinta & Duit  "))
        assertEquals("", NetshortCatalog.cleanHint("🎉🎉"))
    }

    @Test
    fun `search path encodes spaces as percent twenty not plus`() {
        assertEquals("search/wanita%20pemburu/1?lang=id_ID", NetshortCatalog.searchPath("wanita pemburu"))
        assertEquals("search/love/2?lang=id_ID", NetshortCatalog.searchPath("love", 2))
        assertTrue(!NetshortCatalog.searchPath("a b").contains("+"))
    }

    @Test
    fun `episode and category paths use the verified upstream shape`() {
        assertEquals("episode/1994614483446874114/15?lang=id_ID", NetshortCatalog.episodePath("1994614483446874114", 15))
        assertEquals("detail/1994614483446874114?lang=id_ID", NetshortCatalog.detailPath("1994614483446874114"))
        assertEquals("similar/1994614483446874114?lang=id_ID", NetshortCatalog.similarPath("1994614483446874114"))
        assertEquals("tab/1894702358019043329/1?lang=id_ID", NetshortCatalog.tabPath("1894702358019043329"))
        assertEquals(
            "category/1?region=0&audio=0&tagId=1983832175469740041&lang=id_ID",
            NetshortCatalog.categoryPath("1983832175469740041")
        )
    }

    @Test
    fun `video picker prefers 720p normally and 540p when saving data`() {
        val videos = listOf("540p" to "https://cdn/a", "720p" to "https://cdn/b", "1080p" to "https://cdn/c")
        assertEquals("https://cdn/b", NetshortCatalog.pickVideo(videos, dataSaver = false))
        assertEquals("https://cdn/a", NetshortCatalog.pickVideo(videos, dataSaver = true))
        assertEquals(540, NetshortCatalog.qualityRank("540p"))
        assertEquals(0, NetshortCatalog.qualityRank("auto"))
    }

    @Test
    fun `video picker falls back to the nearest quality when preset is missing`() {
        val onlyHigh = listOf("1080p" to "https://cdn/high")
        assertEquals("https://cdn/high", NetshortCatalog.pickVideo(onlyHigh, dataSaver = false))
        // Data saver minta yang paling ringan; kalau cuma ada 1080p ya itu yang dipakai.
        assertEquals("https://cdn/high", NetshortCatalog.pickVideo(onlyHigh, dataSaver = true))

        val onlyLow = listOf("360p" to "https://cdn/low")
        assertEquals("https://cdn/low", NetshortCatalog.pickVideo(onlyLow, dataSaver = true))
        assertEquals("https://cdn/low", NetshortCatalog.pickVideo(onlyLow, dataSaver = false))
    }

    @Test
    fun `video picker ignores unusable urls and empty payloads`() {
        assertEquals("", NetshortCatalog.pickVideo(emptyList(), dataSaver = false))
        assertEquals("", NetshortCatalog.pickVideo(listOf("720p" to ""), dataSaver = false))
        assertEquals("https://cdn/ok", NetshortCatalog.pickVideo(listOf("720p" to "", "540p" to "https://cdn/ok"), dataSaver = false))
    }

    @Test
    fun `pickSubtitle parses single subtitle with id_ID language and webvtt format`() {
        val episodeJson = JSONObject(
            """
            {
              "code": 200,
              "data": {
                "episodeNo": 15,
                "episodeId": "2104114063532695564",
                "videos": [],
                "subtitles": [
                  {
                    "language": "id_ID",
                    "format": "webvtt",
                    "url": "https://video.netshort.com/81fb208084c1400aae88c8c26c62916d?auth_key=1792230188&mime_type=text_plain"
                  }
                ]
              }
            }
            """.trimIndent()
        )
        val sub = NetshortCatalog.pickSubtitle(episodeJson)
        org.junit.Assert.assertNotNull(sub)
        assertEquals("id_ID", sub!!.language)
        assertEquals("webvtt", sub.format)
        assertTrue(sub.url.startsWith("https://video.netshort.com/"))
    }

    @Test
    fun `pickSubtitle prioritizes Indonesian when multiple subtitles exist`() {
        val multiJson = JSONObject(
            """
            {
              "code": 200,
              "data": {
                "subtitles": [
                  {"language": "en_US", "format": "webvtt", "url": "https://video.netshort.com/en"},
                  {"language": "id_ID", "format": "webvtt", "url": "https://video.netshort.com/id"},
                  {"language": "zh_CN", "format": "webvtt", "url": "https://video.netshort.com/zh"}
                ]
              }
            }
            """.trimIndent()
        )
        val sub = NetshortCatalog.pickSubtitle(multiJson)
        org.junit.Assert.assertNotNull(sub)
        assertEquals("id_ID", sub!!.language)
        assertEquals("https://video.netshort.com/id", sub.url)
    }

    @Test
    fun `pickSubtitle returns null on empty or invalid subtitles`() {
        val emptyJson = JSONObject("""{"code": 200, "data": {"subtitles": []}}""")
        org.junit.Assert.assertNull(NetshortCatalog.pickSubtitle(emptyJson))

        val invalidJson = JSONObject("""{"code": 200, "data": {"subtitles": [{"language": "id_ID", "url": "not-http"}]}}""")
        org.junit.Assert.assertNull(NetshortCatalog.pickSubtitle(invalidJson))
    }
}
