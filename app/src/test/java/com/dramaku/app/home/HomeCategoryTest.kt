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
    fun shortDramaExcludesMovieEndpoints() {
        assertTrue(HomeCategory.ShortDrama.containsPlatform("melolo"))
        assertTrue(HomeCategory.ShortDrama.containsPlatform("dramabox"))
        assertFalse(HomeCategory.ShortDrama.containsPlatform("moviebox"))
        assertFalse(HomeCategory.ShortDrama.containsPlatform("drakor"))
    }

    @Test
    fun singlePlatformCategoriesResolveDefaults() {
        assertEquals("moviebox", HomeCategory.MovieBox.defaultPlatform())
        assertEquals("bstation", HomeCategory.Bstation.defaultPlatform())
    }

    @Test
    fun comingSoonCategoriesHaveNoPlatforms() {
        val comingSoon = HomeCategory.values().filter { it.comingSoon }
        assertTrue("harus ada minimal satu kategori segera hadir", comingSoon.isNotEmpty())
        comingSoon.forEach { assertTrue("${it.id} masih punya platform", it.platforms.isEmpty()) }
        HomeCategory.values().filterNot { it.comingSoon }
            .forEach { assertTrue("${it.id} tidak punya platform", it.platforms.isNotEmpty()) }
        assertTrue(HomeCategory.Anime.comingSoon)
        assertTrue(HomeCategory.MovieDrama.comingSoon)
        assertFalse(HomeCategory.ShortDrama.comingSoon)
        assertFalse(HomeCategory.MovieBox.comingSoon)
        assertFalse(HomeCategory.Bstation.comingSoon)
    }

    @Test
    fun idsAreStableForStorageKeys() {
        assertEquals("short_drama", HomeCategory.ShortDrama.id)
        assertEquals("movie_drama", HomeCategory.MovieDrama.id)
        assertEquals("movie_box", HomeCategory.MovieBox.id)
        assertEquals("anime", HomeCategory.Anime.id)
        assertEquals("bstation", HomeCategory.Bstation.id)
    }
}
