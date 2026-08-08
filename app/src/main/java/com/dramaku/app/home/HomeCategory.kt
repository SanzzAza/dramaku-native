package com.dramaku.app.home

/**
 * Kategori pintu masuk di layar awal (desain ala SonzaixBox, brand Dramaku).
 *
 * Short Drama → platform short drama vertikal (Melolo & DramaBox).
 * Movie Drama → endpoint Drakor (Serial Korea & China).
 * Movie Box   → endpoint MovieBox (film/serial global + Shorts vertikal).
 * Anime/Manga → disiapkan, belum terintegrasi ("segera hadir").
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
        subtitle = "Drama pendek dari Melolo, Dramanova, FreeReels & DramaBox",
        platforms = listOf("melolo", "dramanova", "freereels", "dramabox")
    ),
    MovieDrama(
        id = "movie_drama",
        title = "Drama Asia",
        subtitle = "Serial Korea & China buat maraton",
        platforms = listOf("drakor")
    ),
    MovieBox(
        id = "movie_box",
        title = "Movie Box",
        subtitle = "Film layar lebar & shorts buat santai",
        platforms = listOf("moviebox", "mbshorts")
    ),
    Anime(
        id = "anime",
        title = "Anime",
        subtitle = "Slot anime sedang disiapkan",
        platforms = emptyList(),
        comingSoon = true
    ),
    Manga(
        id = "manga",
        title = "Manga",
        subtitle = "Baca manga, manhwa & manhua",
        platforms = emptyList(),
        comingSoon = true
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
