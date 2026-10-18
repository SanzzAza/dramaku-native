package com.dramaku.app.player

import org.junit.Assert.*
import org.junit.Test

class PlaybackPolicyTest {
    @Test fun `resume keeps progress and restarts a completed episode`() {
        assertEquals(25_000L, PlaybackPolicy.resumePosition(25_000L, 60_000L))
        assertEquals(0L, PlaybackPolicy.resumePosition(58_000L, 60_000L))
        assertEquals(0L, PlaybackPolicy.resumePosition(90_000L, 60_000L))
    }

    @Test fun `short clips and unknown duration retain useful progress`() {
        assertEquals(1_000L, PlaybackPolicy.resumePosition(1_000L, 3_000L))
        assertEquals(15_000L, PlaybackPolicy.resumePosition(15_000L, -1L))
        assertEquals(0L, PlaybackPolicy.resumePosition(-10L, 60_000L))
    }

    @Test fun `seek is bounded without overflowing`() {
        assertEquals(0L, PlaybackPolicy.seekPosition(2_000L, -10_000L, 60_000L))
        assertEquals(60_000L, PlaybackPolicy.seekPosition(59_000L, 10_000L, 60_000L))
        assertEquals(13_000L, PlaybackPolicy.seekPosition(3_000L, 10_000L, -1L))
        assertEquals(Long.MAX_VALUE, PlaybackPolicy.seekPosition(Long.MAX_VALUE - 5L, 10_000L, 0L))
    }

    @Test fun `scrubbing clamps invalid input`() {
        assertEquals(30_000L, PlaybackPolicy.positionAt(0.5f, 60_000L))
        assertEquals(0L, PlaybackPolicy.positionAt(-1f, 60_000L))
        assertEquals(60_000L, PlaybackPolicy.positionAt(2f, 60_000L))
        assertEquals(0L, PlaybackPolicy.positionAt(Float.NaN, 60_000L))
        assertEquals(0L, PlaybackPolicy.positionAt(0.5f, -1L))
    }

    @Test fun `episode ranges cover the final partial group`() {
        assertEquals(listOf(1..30, 31..60, 61..65), PlaybackPolicy.episodeRanges(65))
        assertEquals(listOf(1..1), PlaybackPolicy.episodeRanges(1))
        assertTrue(PlaybackPolicy.episodeRanges(0).isEmpty())
    }

    @Test fun `auto next cannot cross the end or a locked episode`() {
        assertEquals(2, PlaybackPolicy.nextEpisode(1, 3, emptySet()))
        assertNull(PlaybackPolicy.nextEpisode(3, 3, emptySet()))
        assertNull(PlaybackPolicy.nextEpisode(1, 3, setOf(2)))
        assertNull(PlaybackPolicy.nextEpisode(0, 3, emptySet()))
    }

    @Test fun `retry budget permits exactly one automatic refresh per episode`() {
        val budget = EpisodeRetryBudget()
        assertFalse(budget.tryConsume(0))
        assertTrue(budget.tryConsume(1))
        budget.beginEpisode(1) // A successful frame or reload must not reset the budget.
        assertFalse(budget.tryConsume(1))
        assertTrue(budget.tryConsume(2))
        assertFalse(budget.tryConsume(2))
        budget.reset(2) // Explicit user retry starts a new attempt.
        assertTrue(budget.tryConsume(2))
    }

    @Test fun `embed detection is limited to supported HTTPS anichin players`() {
        assertTrue(PlaybackPolicy.isEmbeddedStream("anichin", "https://ok.ru/videoembed/123"))
        assertTrue(PlaybackPolicy.isEmbeddedStream("anichin", "https://anichin-player.example/embed/123"))
        assertFalse(PlaybackPolicy.isEmbeddedStream("melolo", "https://example.com/embed/123"))
        assertFalse(PlaybackPolicy.isEmbeddedStream("anichin", "https://cdn.example/video.mp4?ref=embed"))
        assertFalse(PlaybackPolicy.isEmbeddedStream("anichin", "http://ok.ru/videoembed/123"))
        assertFalse(PlaybackPolicy.isEmbeddedStream("anichin", "file:///embed/video"))
    }
}
