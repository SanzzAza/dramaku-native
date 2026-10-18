package com.dramaku.app.home

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeCategoryTest {

    @Test
    fun greetingCoversEveryHour() {
        for (hour in 0..23) {
            val g = Greetings.forHour(hour)
            assertTrue("hour=$hour text kosong", g.text.isNotBlank())
            assertTrue("hour=$hour emoji kosong", g.emoji.isNotBlank())
        }
    }

    @Test
    fun greetingMatchesTimeOfDay() {
        assertEquals("Selamat Pagi", Greetings.forHour(6).text)
        assertEquals("Selamat Siang", Greetings.forHour(12).text)
        assertEquals("Selamat Sore", Greetings.forHour(16).text)
        assertEquals("Selamat Malam", Greetings.forHour(19).text)
        assertEquals("Selamat Malam", Greetings.forHour(23).text)
        assertEquals("Selamat Malam", Greetings.forHour(2).text)
    }

    @Test
    fun shortDramaContainsOnlyShortFormSources() {
        assertTrue(HomeCategory.ShortDrama.containsPlatform("melolo"))
        assertTrue(HomeCategory.ShortDrama.containsPlatform("dramabox"))
        assertTrue(HomeCategory.ShortDrama.containsPlatform("netshort"))
        assertTrue(HomeCategory.ShortDrama.containsPlatform("reelshort"))
        assertFalse(HomeCategory.ShortDrama.containsPlatform("drakor"))
        assertFalse(HomeCategory.ShortDrama.containsPlatform("anichin"))
    }

    @Test
    fun animeResolvesItsDefaultPlatform() {
        assertEquals("anichin", HomeCategory.Anime.defaultPlatform())
    }

    @Test
    fun comingSoonCategoriesHaveNoPlatforms() {
        val comingSoon = HomeCategory.values().filter { it.comingSoon }
        assertTrue("harus ada minimal satu kategori segera hadir", comingSoon.isNotEmpty())
        comingSoon.forEach { assertTrue("${it.id} masih punya platform", it.platforms.isEmpty()) }
        HomeCategory.values().filterNot { it.comingSoon }
            .forEach { assertTrue("${it.id} tidak punya platform", it.platforms.isNotEmpty()) }
        // Anime sekarang aktif dengan Anichin
        assertFalse(HomeCategory.Anime.comingSoon)
        assertTrue(HomeCategory.Anime.platforms.contains("anichin"))
        assertTrue(HomeCategory.MovieDrama.comingSoon)
        assertFalse(HomeCategory.ShortDrama.comingSoon)
    }

    @Test
    fun idsAreStableForStorageKeys() {
        assertEquals(
            listOf("short_drama", "movie_drama", "anime"),
            HomeCategory.values().map { it.id }
        )
    }
}
