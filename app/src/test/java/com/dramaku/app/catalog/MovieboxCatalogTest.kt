package com.dramaku.app.catalog

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MovieboxCatalogTest {

    @Test
    fun `film ditandai dan nomor upstream nol dipertahankan`() {
        val plan = MovieboxCatalog.plan(listOf(Triple(0, 0, "Kjærlighet")))

        assertTrue(plan.isMovie)
        assertEquals(1, plan.episodes.size)
        assertEquals(1, plan.episodes.first().display)
        assertEquals(0, plan.episodes.first().upstream)
        assertEquals(0, plan.episodes.first().season)
    }

    @Test
    fun `respons kosong dianggap film satu tayangan`() {
        val plan = MovieboxCatalog.plan(emptyList())

        assertTrue(plan.isMovie)
        assertTrue(plan.episodes.isEmpty())
    }

    /**
     * Regresi Squid Game: API membalas 22 entri untuk 3 season dengan nomor
     * berulang. Sebelum diperbaiki, daftar datar 1..22 membuat episode 10+
     * memutar episode 1.
     */
    @Test
    fun `serial multi-season hanya menampilkan season pertama`() {
        val raw = buildList {
            (1..9).forEach { add(Triple(it, 1, "S1E$it")) }
            (1..7).forEach { add(Triple(it, 2, "S2E$it")) }
            (1..6).forEach { add(Triple(it, 3, "S3E$it")) }
        }

        val plan = MovieboxCatalog.plan(raw)

        assertFalse(plan.isMovie)
        assertEquals(listOf(1, 2, 3), plan.seasons)
        assertTrue(plan.hasHiddenSeasons)
        assertEquals(9, plan.episodes.size)
        assertTrue(plan.episodes.all { it.season == 1 })
        assertEquals((1..9).toList(), plan.episodes.map { it.display })
        assertEquals((1..9).toList(), plan.episodes.map { it.upstream })
    }

    @Test
    fun `nomor tampilan dirapikan walau upstream bolong`() {
        val raw = listOf(Triple(3, 1, "c"), Triple(1, 1, "a"), Triple(7, 1, "b"))

        val plan = MovieboxCatalog.plan(raw)

        assertEquals(listOf(1, 2, 3), plan.episodes.map { it.display })
        assertEquals(listOf(1, 3, 7), plan.episodes.map { it.upstream })
        assertEquals(listOf("a", "c", "b"), plan.episodes.map { it.label })
    }

    @Test
    fun `serial satu season tidak menyembunyikan apa pun`() {
        val raw = (1..12).map { Triple(it, 1, "Ep $it") }

        val plan = MovieboxCatalog.plan(raw)

        assertFalse(plan.isMovie)
        assertFalse(plan.hasHiddenSeasons)
        assertEquals(12, plan.episodes.size)
    }
}
