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
        val netshort = ProgressKeys.episodePrefix("netshort", "123", 4)

        assertNotEquals(melolo, netshort)
    }

    @Test
    fun `episode and drama prefixes remain scoped to one drama`() {
        val dramaPrefix = ProgressKeys.dramaPrefix("netshort", "id|with spaces")
        val episodePrefix = ProgressKeys.episodePrefix("netshort", "id|with spaces", 7)
        val otherDrama = ProgressKeys.episodePrefix("netshort", "id", 7)

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
