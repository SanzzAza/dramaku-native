package com.dramaku.app.player

import java.net.URI

/** Playback decisions kept independent of Android so edge cases can be unit-tested. */
internal object PlaybackPolicy {
    fun resumePosition(positionMs: Long, durationMs: Long): Long {
        val position = positionMs.coerceAtLeast(0L)
        if (durationMs <= 0L) return position
        // A completed episode starts from the beginning, but short clips must not
        // lose their progress just because their entire duration is under 4 sec.
        val endWindow = minOf(4_000L, durationMs / 10L)
        return if (position >= durationMs - endWindow) 0L else position.coerceAtMost(durationMs)
    }

    fun seekPosition(positionMs: Long, deltaMs: Long, durationMs: Long): Long {
        val position = positionMs.coerceAtLeast(0L)
        val target = if (deltaMs > 0 && position > Long.MAX_VALUE - deltaMs) Long.MAX_VALUE else position + deltaMs
        return target.coerceIn(0L, durationMs.takeIf { it > 0L } ?: Long.MAX_VALUE)
    }

    fun positionAt(fraction: Float, durationMs: Long): Long {
        if (durationMs <= 0L || !fraction.isFinite()) return 0L
        return (fraction.coerceIn(0f, 1f).toDouble() * durationMs).toLong().coerceIn(0L, durationMs)
    }

    fun episodeRanges(total: Int, size: Int = 30): List<IntRange> {
        require(size > 0)
        if (total <= 0) return emptyList()
        return (1..total step size).map { start -> start..minOf(total.toLong(), start.toLong() + size - 1L).toInt() }
    }

    fun nextEpisode(current: Int, total: Int, locked: Set<Int>): Int? =
        (current + 1).takeIf { current in 1 until total && it !in locked }

    fun isEmbeddedStream(platform: String, url: String): Boolean {
        if (platform != "anichin") return false
        val uri = runCatching { URI(url) }.getOrNull() ?: return false
        if (uri.scheme != "https") return false
        val path = uri.path.orEmpty().lowercase()
        if (listOf(".mp4", ".m3u8", ".mpd").any { path.endsWith(it) }) return false
        val host = uri.host.orEmpty().lowercase()
        return listOf("ok.ru", "dailymotion.com", "rumble.com").any { host == it || host.endsWith(".$it") } ||
            listOf("anichin-player", "abyss", "vidhide", "turbovid").any { host.contains(it) } || path.contains("/embed")
    }
}

/** Never reset on onIsPlayingChanged: intermittent failures otherwise retry forever. */
internal class EpisodeRetryBudget(private val limit: Int = 1) {
    private var episode = 0
    private var attempts = 0

    fun beginEpisode(value: Int) {
        if (episode != value) reset(value)
    }

    fun reset(value: Int) {
        episode = value
        attempts = 0
    }

    fun tryConsume(value: Int): Boolean {
        if (value <= 0) return false
        beginEpisode(value)
        if (attempts >= limit) return false
        attempts++
        return true
    }
}
