package com.dramaku.app.catalog

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DramaboxCatalogTest {

    // Balasan asli /stream dari dramahub.be/dramaboxbaru/api (mp4 ber-token).
    private val streamResponse = JSONObject(
        """
        {
          "code": 0,
          "video": "https://dramabox.backengine.web.id/api/stream?bookId=42000010883&episode=1&lang=in&seg=video.mp4&k=1797552000-0-0-b41b6f375ebc5693a2fc140f4903f1bc",
          "duration": 193.118,
          "subtitles": []
        }
        """.trimIndent()
    )

    @Test
    fun `streamUrl takes video field from proxy json`() {
        assertTrue(DramaboxCatalog.streamUrl(streamResponse).startsWith("https://dramabox.backengine.web.id/api/stream?"))
        assertTrue(DramaboxCatalog.streamUrl(streamResponse).contains("seg=video.mp4"))
    }

    @Test
    fun `streamUrl also reads nested data and alias keys`() {
        val nested = JSONObject("""{"code":0,"data":{"playUrl":"https://cdn.example.com/a.mp4"}}""")
        assertEquals("https://cdn.example.com/a.mp4", DramaboxCatalog.streamUrl(nested))
    }

    @Test
    fun `streamUrl stays empty for non json style payload so caller can fall back`() {
        assertEquals("", DramaboxCatalog.streamUrl(JSONObject("""{"code":0,"message":"Success"}""")))
    }

    // Ringkasan /categories dari API (name sudah Bahasa Indonesia).
    private val categoriesResponse = JSONObject(
        """
        {
          "code": 0,
          "message": "Success",
          "data": [
            {"id": 0, "name": "all", "replaceName": "all", "checked": false},
            {"id": 449, "name": "Cinta Pahit", "replaceName": "Cinta-Pahit", "checked": false},
            {"id": 454, "name": "Kawin Kontrak", "replaceName": "Kawin-Kontrak", "checked": false},
            {"id": 460, "name": "Bayi", "replaceName": "Bayi", "checked": false},
            {"id": 828, "name": "Bayi", "replaceName": "Bayi", "checked": false},
            {"id": 433, "name": "", "replaceName": "Kekuatan-Super", "checked": false}
          ]
        }
        """.trimIndent()
    )

    @Test
    fun `categories drops all entry and duplicate names`() {
        val cats = DramaboxCatalog.categories(categoriesResponse, limit = 10)
        assertEquals(listOf("Cinta Pahit", "Kawin Kontrak", "Bayi", "Kekuatan-Super"), cats.map { it.name })
        assertEquals(listOf(449, 454, 460, 433), cats.map { it.id })
    }

    @Test
    fun `categories honours limit and handles missing data`() {
        assertEquals(2, DramaboxCatalog.categories(categoriesResponse, limit = 2).size)
        assertEquals(emptyList<DramaboxCategory>(), DramaboxCatalog.categories(JSONObject("""{"code":0}""")))
    }

    @Test
    fun `browsePath builds genre row path`() {
        assertEquals("browse?type=449&page=1&lang=in", DramaboxCatalog.browsePath(449))
        assertEquals("browse?type=449&page=3&lang=in", DramaboxCatalog.browsePath(449, page = 3))
    }
}
