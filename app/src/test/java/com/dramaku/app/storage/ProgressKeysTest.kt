package com.dramaku.app.storage

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgressKeysTest {
    @Test
    fun `same drama ID on different platforms has different key`() {
        val melolo = ProgressKeys.episodePrefix("melolo", "123", 4)
        val movieBox = ProgressKeys.episodePrefix("moviebox", "123", 4)

        assertNotEquals(melolo, movieBox)
    }

    @Test
    fun `episode and drama prefixes remain scoped to one drama`() {
        val dramaPrefix = ProgressKeys.dramaPrefix("moviebox", "id|with spaces")
        val episodePrefix = ProgressKeys.episodePrefix("moviebox", "id|with spaces", 7)
        val otherDrama = ProgressKeys.episodePrefix("moviebox", "id", 7)

        assertTrue(episodePrefix.startsWith(dramaPrefix))
        assertFalse(otherDrama.startsWith(dramaPrefix))
    }

    @Test
    fun `invalid episode number is normalized`() {
        assertEquals(
            ProgressKeys.episodePrefix("drakor", "abc", 1),
            ProgressKeys.episodePrefix("drakor", "abc", 0)
        )
    }

    @Test
    fun `legacy key format is retained for migration`() {
        assertEquals("progress_drama-1_2_", ProgressKeys.legacyEpisodePrefix("drama-1", 2))
    }
}
