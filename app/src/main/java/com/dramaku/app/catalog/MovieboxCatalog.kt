package com.dramaku.app.catalog

/**
 * Satu episode MovieBox setelah dirapikan.
 *
 * [display] nomor yang dilihat pengguna (selalu berurutan mulai 1),
 * [upstream] nomor asli yang harus dikirim ke API, dan [season] nomor season-nya.
 * Pemisahan ini penting: pada serial multi-season nomor upstream berulang tiap
 * season (S1E1..S1E9, lalu S2E1..), jadi nomor tampilan tidak bisa dipakai
 * langsung sebagai parameter permintaan.
 */
data class MovieboxEpisode(
    val display: Int,
    val upstream: Int,
    val season: Int,
    val label: String
)

/**
 * Hasil perencanaan daftar episode: daftar yang aman ditampilkan, penanda film,
 * dan daftar season yang terdeteksi di respons.
 */
data class MovieboxPlan(
    val episodes: List<MovieboxEpisode>,
    val isMovie: Boolean,
    val seasons: List<Int>
) {
    val hasHiddenSeasons: Boolean get() = seasons.size > 1
}

object MovieboxCatalog {

    /**
     * Ubah entri mentah `episodes` dari `subject/get` menjadi daftar yang aman.
     *
     * Aturannya:
     * - Film ditandai oleh entri tunggal dengan episode 0 dan season 0. Hasilnya
     *   satu entri saja, nomor upstream tetap 0 supaya permintaan streamnya tepat.
     * - Serial hanya mengambil season pertama. Menggabungkan semua season jadi
     *   satu daftar datar membuat nomor di atas jumlah episode season pertama
     *   diam-diam memutar episode pertama, karena API mengabaikan parameter `se`.
     * - Nomor tampilan selalu dirapikan jadi 1..N walau upstream bolong.
     *
     * @param raw pasangan (nomor episode, nomor season, judul) apa adanya dari API.
     */
    fun plan(raw: List<Triple<Int, Int, String>>): MovieboxPlan {
        if (raw.isEmpty()) {
            return MovieboxPlan(emptyList(), isMovie = true, seasons = emptyList())
        }
        if (raw.all { it.first == 0 && it.second == 0 }) {
            val (ep, se, label) = raw.first()
            return MovieboxPlan(
                listOf(MovieboxEpisode(display = 1, upstream = ep, season = se, label = label)),
                isMovie = true,
                seasons = listOf(se)
            )
        }
        val seasons = raw.map { it.second }.distinct().sorted()
        val firstSeason = seasons.first()
        val episodes = raw.asSequence()
            .filter { it.second == firstSeason }
            .sortedBy { it.first }
            .mapIndexed { index, (ep, se, label) ->
                MovieboxEpisode(display = index + 1, upstream = ep, season = se, label = label)
            }
            .toList()
        return MovieboxPlan(episodes, isMovie = false, seasons = seasons)
    }
}
