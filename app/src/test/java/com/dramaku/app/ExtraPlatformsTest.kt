package com.dramaku.app

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExtraPlatformsTest {
    @Test
    fun allExtraPlatformsHaveThreeHomeUrlsAndDetailUrl() {
        for (p in ExtraPlatforms.platforms) {
            val base = "https://dramahub.be/api/providers/${p.id}"
            val homes = ExtraPlatforms.homeUrls(p.id, 1, base)
            assertNotNull("homeUrls missing for ${p.id}", homes)
            assertTrue("Expected 3 home URLs for ${p.id}", homes!!.size == 3)
            homes.forEach { url ->
                assertTrue("Blank home URL for ${p.id}", url.startsWith("http"))
            }

            val detail = ExtraPlatforms.detailUrl(p.id, "test_id_123", base)
            assertNotNull("detailUrl missing for ${p.id}", detail)
            assertTrue("detailUrl should start with http for ${p.id}", detail!!.startsWith("http"))
            assertTrue("logoUrl missing for ${p.id}", p.logoUrl.startsWith("https://dramahub.be/logos/"))
            assertTrue("logoRes missing for ${p.id}", p.logoRes != 0)
        }
    }

    @Test
    fun fixedEndpointsMatchVerifiedUpstreamPaths() {
        val base = "https://dramahub.be/api/providers"
        // WeTV uses Indonesian mini-drama search feeds instead of 404 /api/feed
        val wetvHomes = ExtraPlatforms.homeUrls("wetv", 1, "$base/wetv")!!
        assertFalse(wetvHomes.any { it.contains("/api/feed") })
        assertTrue(wetvHomes.all { it.contains("/api/search?") })

        // SodaReels uses /api/v1/dramas and /api/v1/collections instead of 404 /api/v1/home
        val sodaHomes = ExtraPlatforms.homeUrls("sodareels", 1, "$base/sodareels")!!
        assertFalse(sodaHomes.any { it.contains("/api/v1/home") })

        // RaptDrama uses /open/classify_video and /video/playlist
        val raptHomes = ExtraPlatforms.homeUrls("raptdrama", 1, "$base/raptdrama")!!
        assertTrue(raptHomes.all { it.contains("/open/classify_video") })
        val raptDetail = ExtraPlatforms.detailUrl("raptdrama", "999", "$base/raptdrama")!!
        assertTrue(raptDetail.contains("/video/playlist?vid=999"))

        // MoboReels uses /api/video?seriesId=...&episNum=...&langId=3
        val moboStream = ExtraPlatforms.streamUrl("moboreels", "146350", 2, "$base/moboreels")!!
        assertTrue(moboStream.contains("/api/video?seriesId=146350&episNum=2&langId=3"))

        // ShortSky search uses /api/search?q=...&page=1&lang=id_id
        val shortskySearch = ExtraPlatforms.searchUrl("shortsky", "cinta", "$base/shortsky")!!
        assertTrue(shortskySearch.contains("/api/search?q=cinta&page=1&lang=id_id"))
    }
}
