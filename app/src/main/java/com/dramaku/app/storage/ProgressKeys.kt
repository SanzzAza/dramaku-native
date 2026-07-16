package com.dramaku.app.storage

import java.net.URLEncoder

/**
 * SharedPreferences keys for playback progress.
 *
 * Version 2 includes the platform because drama IDs are only unique inside a
 * platform. Segments are URL encoded so IDs containing separators cannot
 * collide with each other.
 */
internal object ProgressKeys {
    private const val V2_PREFIX = "progress_v2|"

    fun episodePrefix(platform: String, dramaId: String, episode: Int): String =
        "$V2_PREFIX${encode(platform)}|${encode(dramaId)}|${episode.coerceAtLeast(1)}|"

    fun dramaPrefix(platform: String, dramaId: String): String =
        "$V2_PREFIX${encode(platform)}|${encode(dramaId)}|"

    /** Prefix used by releases before v4.7.1, kept for read-only migration. */
    fun legacyEpisodePrefix(dramaId: String, episode: Int): String =
        "progress_${dramaId}_${episode.coerceAtLeast(1)}_"

    private fun encode(value: String): String = URLEncoder.encode(value, Charsets.UTF_8.name())
}
