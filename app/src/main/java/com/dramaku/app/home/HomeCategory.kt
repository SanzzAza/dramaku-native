package com.dramaku.app.home

import com.dramaku.app.ExtraPlatforms

/**
 * Kategori pintu masuk di layar awal (desain ala SonzaixBox, brand Dramaku).
 *
 * Short Drama → platform short drama vertikal (Melolo, DramaNova, FreeReels,
 *               DramaBox, NetShort).
 * Drama Asia  → serial Korea & China (segera hadir).
 * Anime       → donghua & anime subtitle Indonesia dari Anichin.
 */
enum class HomeCategory(
    val id: String,
    val title: String,
    val subtitle: String,
    val platforms: List<String>,
    val comingSoon: Boolean = false
) {
    ShortDrama(
        id = "short_drama",
        title = "Short Drama",
        subtitle = "Drama pendek vertikal episode singkat",
        platforms = listOf("melolo", "dramanova", "freereels", "dramabox", "netshort", "reelshort") + ExtraPlatforms.platforms.map { it.id }
    ),
    MovieDrama(
        id = "movie_drama",
        title = "Drama Asia",
        subtitle = "Serial Korea & China sedang disiapkan",
        platforms = emptyList(),
        comingSoon = true
    ),
    Anime(
        id = "anime",
        title = "Anime",
        subtitle = "Donghua & anime subtitle Indonesia",
        platforms = listOf("anichin"),
        comingSoon = false
    );

    fun defaultPlatform(): String = platforms.firstOrNull() ?: "melolo"
    fun containsPlatform(platformId: String): Boolean = platforms.contains(platformId)
}

/** Sapaan waktu untuk header layar awal. */
data class Greeting(val text: String, val emoji: String)

object Greetings {
    fun forHour(hourOfDay: Int): Greeting = when (hourOfDay) {
        in 4..10 -> Greeting("Selamat Pagi", "🌤️")
        in 11..14 -> Greeting("Selamat Siang", "☀️")
        in 15..17 -> Greeting("Selamat Sore", "🌇")
        else -> Greeting("Selamat Malam", "🌙")
    }
}
