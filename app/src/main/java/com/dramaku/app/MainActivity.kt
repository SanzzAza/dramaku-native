package com.dramaku.app

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.Intent
import android.graphics.Color as AndroidColor
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.input.pointer.pointerInput
import coil.compose.AsyncImage
import coil.Coil
import com.dramaku.app.reminder.ResumeReminder
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.dramaku.app.data.NativeRemoteConfig
import com.dramaku.app.data.RemoteConfigRepository
import com.dramaku.app.home.Greetings
import com.dramaku.app.home.HomeCategory
import com.dramaku.app.storage.ProgressKeys
import com.dramaku.app.player.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.withContext
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import androidx.media3.common.C
import androidx.media3.common.AudioAttributes
import androidx.media3.common.Tracks
import androidx.media3.common.VideoSize
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.common.PlaybackException
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.ResolvingDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.ui.PlayerView
import com.dramaku.app.catalog.DramaboxCatalog
import com.dramaku.app.catalog.DramaboxCategory
import com.dramaku.app.catalog.NetshortCatalog
import com.dramaku.app.catalog.NetshortTab
import com.dramaku.app.catalog.ReelshortCatalog
import com.dramaku.app.catalog.ReelshortTab
import com.dramaku.app.catalog.AnichinCatalog
import com.dramaku.app.catalog.AnichinGenre
import androidx.media3.ui.AspectRatioFrameLayout
import okhttp3.Dispatcher
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.URLEncoder
import java.util.Calendar
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import kotlin.math.max
import kotlin.math.min

// ─────────────────────────────────────────────────────────────────
// TAMPILAN — hitam pekat, kartu abu gelap, dan aksen kuning terang.
// Seluruh layar memakai Plus Jakarta Sans agar konsisten dan mudah dibaca.
// ─────────────────────────────────────────────────────────────────

private object DS {
    val Bg = Color(0xFF000000)
    val Raise = Color(0xFF090909)
    val Card = Color(0xFF161616)
    val Card2 = Color(0xFF252525)
    val Line = Color(0xFF242424)
    val LineStrong = Color(0xFF3A3A3A)

    // Nama token dipertahankan supaya komponen lama ikut tema baru.
    val Green = Color(0xFFFFE500)
    val GreenDeep = Color(0xFF8C7E00)
    val GreenWash = Green.copy(alpha = 0.14f)
    val Cream = Color(0xFFFFFFFF)
    val Ink = Color(0xFF050505)
    val Cyan = Color(0xFF45A8FF)
    val CyanWash = Cyan.copy(alpha = 0.16f)
    val Violet = Color(0xFFC66BFF)
    val VioletWash = Violet.copy(alpha = 0.16f)

    val Hi = Color(0xFFF5F5F5)
    val Body = Color(0xFFD6D6D6)
    val Muted = Color(0xFF929292)
    val Faint = Color(0xFF626262)

    val Red = Color(0xFFFF626B)
    val RedWash = Red.copy(alpha = 0.15f)
    val Gold = Green
    val Rating = Violet

    val PosterFade = listOf(Color(0x00000000), Color(0xC9000000))
    val HeroFade = listOf(Color(0x10000000), Color(0x70000000), Color(0xFF000000))
    val SheetBg = Color(0xF5090909)
    val CardGradient = listOf(Card, Card)
}

private object Type {
    val Sans = FontFamily(
        Font(R.font.jakarta_regular, FontWeight.Normal),
        Font(R.font.jakarta_medium, FontWeight.Medium),
        Font(R.font.jakarta_semibold, FontWeight.SemiBold),
        Font(R.font.jakarta_bold, FontWeight.Bold)
    )
    val Display = Sans
}

private val Days = listOf("Minggu", "Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu")
private val Months = listOf("Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember")

private fun todayLine(): String {
    val c = Calendar.getInstance()
    return "${Days[c.get(Calendar.DAY_OF_WEEK) - 1]}, ${c.get(Calendar.DAY_OF_MONTH)} ${Months[c.get(Calendar.MONTH)]}"
}

// ─────────────────────────────────────────────────────────────────
// APP ENTRY
// ─────────────────────────────────────────────────────────────────

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Coil.setImageLoader(
            ImageLoader.Builder(this)
                .memoryCache { MemoryCache.Builder(this).maxSizePercent(0.25).build() }
                .diskCache { DiskCache.Builder().directory(cacheDir.resolve("coil_img")).maxSizePercent(0.05).build() }
                .crossfade(true)
                .okHttpClient {
                    OkHttpClient.Builder()
                        .addInterceptor { chain ->
                            val orig = chain.request()
                            val host = orig.url.host.lowercase()
                            val req = orig.newBuilder()
                                .header("User-Agent", "Mozilla/5.0 (Linux; Android 14; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
                                .header("Accept", "image/webp,image/png,image/jpeg,image/*,*/*;q=0.8")
                                .apply {
                                    if (host.contains("anichin") || host.contains("wp.com")) {
                                        header("Referer", "https://anichin.moe/")
                                    }
                                }
                                .build()
                            chain.proceed(req)
                        }
                        .connectTimeout(15, TimeUnit.SECONDS)
                        .readTimeout(20, TimeUnit.SECONDS)
                        .build()
                }
                .build()
        )
        window.statusBarColor = AndroidColor.BLACK
        window.navigationBarColor = AndroidColor.BLACK
        setContent { DramakuApp() }
    }
}

@Composable
private fun DramakuApp() {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = DS.Green,
            background = DS.Bg,
            surface = DS.Raise,
            onPrimary = DS.Ink,
            onBackground = DS.Hi,
            onSurface = DS.Hi
        )
    ) {
        App()
    }
}

// ─────────────────────────────────────────────────────────────────
// DATA MODELS
// ─────────────────────────────────────────────────────────────────

private enum class Tab(val label: String, val icon: ImageVector, val showNav: Boolean = true) {
    Clips("Cuplikan", Icons.Rounded.SmartDisplay),
    Home("Temukan", Icons.Rounded.Explore),
    Library("Perpustakaan", Icons.Rounded.VideoLibrary),
    Profile("Profil", Icons.Rounded.Person),
    Search("Cari", Icons.Rounded.Search, false)
}

data class PlatformInfo(val id: String, val label: String, val base: String, val logoUrl: String = "", val logoRes: Int = 0)
private data class Drama(
    val id: String, val title: String, val description: String = "", val poster: String = "",
    val episodes: Int = 0, val views: String = "", val tags: List<String> = emptyList(),
    val platform: String = "melolo", val subjectType: Int = 1
)
private data class EpisodeInfo(val number: Int, val streaming: String = "", val label: String = "", val locked: Boolean = false)
private data class Detail(val drama: Drama, val episodes: List<EpisodeInfo> = emptyList())
private data class HomeBundle(val recommended: List<Drama>, val popular: List<Drama>, val newest: List<Drama>, val loadedPage: Int = 1, val hasMore: Boolean = true)
private data class StreamResult(val url: String, val subtitle: String = "", val subtitleMime: String = "")
private const val HUB_BEARER = "Bearer 15693e658f723c5b4c45900a5d045ef0ab6a053ecda4dadb831c68fef773ba5e"
private const val HUB_REFERER = "https://www.cdreader.com/"

/** Bearer dramahub hanya untuk host dramahub.be. CDN presigned (S3/GCS/signature)
 * bisa menolak request yang membawa header Authorization asing, dan token kita
 * tidak perlu bocor ke pihak ketiga. Referer cdreader hanya untuk host yang
 * memang memeriksanya (dramahub + CDN cdreader milik DramaBox). */
private fun scopedVideoHeaders(host: String?): Map<String, String> {
    val h = host.orEmpty().lowercase()
    val hub = h == "dramahub.be" || h.endsWith(".dramahub.be")
    val cdreader = h == "cdreader.com" || h.endsWith(".cdreader.com")
    return buildMap {
        if (hub) put("Authorization", HUB_BEARER)
        if (hub || cdreader) put("Referer", HUB_REFERER)
    }
}

private const val STREAM_CACHE_TTL_MS = 45_000L
private const val DETAIL_CACHE_TTL_MS = 5 * 60_000L
private const val SIGNED_URL_SAFETY_MS = 3 * 60_000L
private data class CachedStream(val result: StreamResult, val expiresAtMs: Long)
private data class CachedDetail(val detail: Detail, val expiresAtMs: Long)
private data class PlayerSession(val detail: Detail, val startEpisode: Int)
private data class HistoryItem(
    val id: String, val title: String, val poster: String, val platform: String,
    val episode: Int, val pos: Long = 0L, val dur: Long = 0L, val updated: Long = System.currentTimeMillis()
) {
    val pct: Int get() = if (dur > 0) min(99, max(0, ((pos * 100) / dur).toInt())) else 0
}

private sealed class Load<out T> {
    object Idle : Load<Nothing>()
    object Loading : Load<Nothing>()
    data class Ok<T>(val data: T) : Load<T>()
    data class Err(val message: String) : Load<Nothing>()
}

// ─────────────────────────────────────────────────────────────────
// PLATFORMS
// ─────────────────────────────────────────────────────────────────

private val Platforms = listOf(
    // Melolo & NetShort: endpoint penuh dengan Bearer; DramaBox memakai proxy baru.
    PlatformInfo("melolo", "Melolo", "https://dramahub.be/melolo/api/v1", logoUrl = "https://dramahub.be/logos/melolo.png", logoRes = R.drawable.logo_melolo),
    PlatformInfo("dramanova", "Dramanova", "https://dramahub.be/dramanova", logoUrl = "https://dramahub.be/logos/dramanova.png", logoRes = R.drawable.logo_dramanova),
    PlatformInfo("freereels", "FreeReels", "https://dramahub.be/freereels/api/v1", logoUrl = "https://dramahub.be/logos/freereels.png", logoRes = R.drawable.logo_freereels),
    PlatformInfo("dramabox", "DramaBox", "https://dramahub.be/dramaboxv2/api/v1", logoUrl = "https://dramahub.be/logos/dramabox.png", logoRes = R.drawable.logo_dramabox),
    PlatformInfo("netshort", "NetShort", "https://dramahub.be/netshort/api/v1", logoUrl = "https://dramahub.be/logos/netshort.png", logoRes = R.drawable.logo_netshort),
    PlatformInfo("reelshort", "ReelShort", "https://dramahub.be/reelshort/api/v1", logoUrl = "https://dramahub.be/logos/reelshort.png", logoRes = R.drawable.logo_reelshort),
    PlatformInfo("anichin", "Anichin", "https://dramahub.be/anichin", logoUrl = "https://dramahub.be/logos/anichin.png", logoRes = R.drawable.logo_anichin),
    PlatformInfo("stardusttv", "Stardust TV", "https://dramahub.be/stardusttv/api/v1", logoUrl = "https://dramahub.be/logos/stardusttv.png", logoRes = R.drawable.logo_stardusttv)
) + ExtraPlatforms.platforms

// Rak genre DramaBox cadangan: dipakai kalau /categories tidak terbaca, supaya
// beranda tetap punya rak. Type id-nya sama dengan yang dipakai versi sebelumnya.
private val DRAMABOX_FALLBACK_GENRES = listOf(
    "Kekuatan super" to 433,
    "Kawin kontrak" to 454,
    "Melawan balik" to 462,
    "Kelahiran kembali" to 450,
    "Balas dendam" to 458,
    "Cinta pahit" to 449,
    "Perjalanan waktu" to 451
)

// Rak genre NetShort cadangan: tag id diambil dari /categories (labelName-nya
// sudah Bahasa Indonesia). Dipakai kalau /categories tidak terbaca.
private val NETSHORT_FALLBACK_TAGS = listOf(
    "Kepuasan" to "1983832175759147019",
    "Romantis Urban" to "1983832092736479243",
    "Misteri dan Detektif" to "1983832036940267532",
    "Balas Dendam" to "1983832036302733324",
    "Konflik Keluarga Kaya" to "1983832091893424132",
    "Sang Juara Kembali" to "1983832036285956107"
)

// Rak genre ReelShort cadangan: dipakai kalau tabs tidak terbaca.
private val REELSHORT_FALLBACK_GENRES = listOf(
    "Romance" to "romance",
    "Drama" to "drama",
    "Completed" to "completed",
    "Terbaru" to "new",
    "Untukmu" to "foryou"
)

private val ANICHIN_FALLBACK_GENRES = listOf(
    "Action" to "action",
    "Adventure" to "adventure",
    "Fantasy" to "fantasy",
    "Romance" to "romance",
    "Comedy" to "comedy",
    "Drama" to "drama",
    "Isekai" to "isekai",
    "Martial Arts" to "martial-arts"
)

private fun platform(id: String) = Platforms.firstOrNull { it.id == id }
    ?: PlatformInfo(id, id, "")
private fun platformLabel(id: String) = platform(id).label
private fun apiBase(id: String) = platform(id).base.ifBlank { error("Platform tidak tersedia") }

// ─────────────────────────────────────────────────────────────────
// MAIN APP COMPOSABLE
// ─────────────────────────────────────────────────────────────────

@Composable
private fun App() {
    val ctx = LocalContext.current
    val store = remember { LocalStore(ctx) }
    val repo = remember { DramakuRepository() }
    val remoteRepo = remember { RemoteConfigRepository() }
    val scope = rememberCoroutineScope()

    var isOnline by remember { mutableStateOf(ctx.isNetworkAvailable()) }
    DisposableEffect(ctx) {
        val obs = LifecycleEventObserver { _, _ -> isOnline = ctx.isNetworkAvailable() }
        val lc = (ctx as? ComponentActivity)?.lifecycle
        lc?.addObserver(obs)
        onDispose { lc?.removeObserver(obs) }
    }

    var tab by remember { mutableStateOf(Tab.Home) }
    var selPlatform by remember {
        mutableStateOf(store.platform().takeIf { saved -> Platforms.any { it.id == saved } } ?: Platforms.first().id)
    }
    var refreshKey by remember { mutableIntStateOf(0) }
    var homeScrollToTop by remember { mutableIntStateOf(0) }
    var homeState by remember { mutableStateOf<Load<HomeBundle>>(Load.Idle) }
    var homeLoadingMore by remember { mutableStateOf(false) }
    var homeAppendError by remember { mutableStateOf<String?>(null) }
    var selectedDrama by remember { mutableStateOf<Drama?>(null) }
    var detailState by remember { mutableStateOf<Load<Detail>>(Load.Idle) }
    var remoteConfig by remember { mutableStateOf<NativeRemoteConfig?>(null) }
    var remoteError by remember { mutableStateOf<String?>(null) }
    var dataTick by remember { mutableIntStateOf(0) }
    var resolvingEpisode by remember { mutableIntStateOf(0) }
    var playerSession by remember { mutableStateOf<PlayerSession?>(null) }
    var clipFeedItems by remember { mutableStateOf<List<Drama>>(emptyList()) }
    var pendingResume by remember { mutableStateOf<HistoryItem?>(null) }
    // Langsung masuk ke Temukan; tidak ada onboarding akun atau layar login.
    var category by remember { mutableStateOf<HomeCategory?>(HomeCategory.ShortDrama) }
    var showSettings by remember { mutableStateOf(false) }
    var genreRows by remember { mutableStateOf<List<Pair<String, List<Drama>>>>(emptyList()) }
    var showPlatformPicker by remember { mutableStateOf(false) }



    fun openPlayer(d: Detail, ep: Int) { playerSession = PlayerSession(d, ep) }

    LaunchedEffect(refreshKey) {
        runCatching { remoteRepo.load() }
            .onSuccess { remoteConfig = it; remoteError = null }
            .onFailure { remoteError = it.message ?: "Remote config gagal" }
    }

    LaunchedEffect(selPlatform, refreshKey) {
        homeLoadingMore = false; homeAppendError = null; homeState = Load.Loading
        try { homeState = Load.Ok(repo.loadHome(selPlatform)) }
        catch (e: CancellationException) { throw e }
        catch (t: Throwable) { homeState = Load.Err(t.message ?: "Gagal memuat") }
    }

    LaunchedEffect(selectedDrama) {
        val d = selectedDrama ?: return@LaunchedEffect
        detailState = Load.Loading
        try { detailState = Load.Ok(repo.loadDetailCached(d)) }
        catch (e: CancellationException) { throw e }
        catch (t: Throwable) { detailState = Load.Err(t.message ?: "Gagal memuat detail") }
    }

    LaunchedEffect(detailState, pendingResume) {
        val p = pendingResume ?: return@LaunchedEffect
        val det = (detailState as? Load.Ok)?.data ?: return@LaunchedEffect
        if (det.drama.id == p.id && det.drama.platform == p.platform) {
            playerSession = PlayerSession(det, p.episode.coerceAtLeast(1))
            selectedDrama = null; pendingResume = null
        }
    }

    // Rak genre: Melolo lewat katalog search (feed mentok 18 judul, katalognya
    // jauh lebih dalam), DramaBox lewat kategori asli endpoint barunya
    // (/categories → browse?type=..., plus rak hidden-gems).
    LaunchedEffect(homeState) {
        val ok = (homeState as? Load.Ok)?.data
        if (ok == null) { genreRows = emptyList(); return@LaunchedEffect }
        val known = (ok.popular + ok.newest + ok.recommended).map { it.platform + "|" + it.id }.toSet()
        val wanted: List<Pair<String, suspend () -> List<Drama>>> = when {
            // DramaBox (dramahub.be): genre diambil dari /categories, jadi rak
            // beranda ikut berubah kalau katalog upstream menambah genre baru.
            // Kalau endpoint itu kosong/gagal, daftar type id lama dipakai.
            selPlatform == "dramabox" -> {
                val cats = repo.dramaboxCategories()
                if (cats.isEmpty()) {
                    // Proxy v2 tidak punya /categories & /hidden-gems, dan /browse
                    // mengabaikan sebagian besar type id lama — satu rak jelajah
                    // cukup supaya tidak muncul banyak rak dengan isi kembar.
                    listOf<Pair<String, suspend () -> List<Drama>>>(
                        "Jelajahi katalog" to { repo.browsePath(selPlatform, DramaboxCatalog.browsePath(2)) }
                    )
                } else {
                    val gems: Pair<String, suspend () -> List<Drama>> =
                        "Permata tersembunyi" to { repo.browsePath(selPlatform, DramaboxCatalog.HIDDEN_GEMS_PATH) }
                    val rows: List<Pair<String, suspend () -> List<Drama>>> = cats.map { c ->
                        c.name to suspend { repo.browsePath(selPlatform, DramaboxCatalog.browsePath(c.id)) }
                    }
                    listOf(gems) + rows
                }
            }
            // NetShort (dramahub.be): rak dirakit dari dua endpoint katalog —
            // /tabs untuk tab konten (Anime, Pelukan Jakarta, …) dan /categories
            // untuk tag. Keduanya di-cache, dan kalau kosong pemanggil pakai
            // daftar cadangan / jalur tetap.
            //
            // Jelajahi & Baru tidak dijadikan rak: isinya persis rak utama
            // beranda (explore/feed/new), jadi bakal habis kena filter "known".
            // Dubbing & VIP punya endpoint khusus di daftar `fixed`, jadi tab
            // dengan nama sama dilewati supaya tidak dobel.
            selPlatform == "netshort" -> {
                val covered = setOf("dubbing", "vip", "kategori", "ranking")
                val tabs = repo.netshortContentTabs().filter { it.name.trim().lowercase() !in covered }
                val tagRows: List<Pair<String, String>> = repo.netshortTags().ifEmpty { NETSHORT_FALLBACK_TAGS }
                val fixed: List<Pair<String, String>> = listOf(
                    "Sulih suara" to NetshortCatalog.DUBBING_PATH,
                    "VIP" to NetshortCatalog.VIP_PATH
                )
                val tabRows: List<Pair<String, suspend () -> List<Drama>>> = tabs.map { tab: NetshortTab ->
                    tab.name to { repo.browsePath(selPlatform, NetshortCatalog.tabPath(tab.id)) }
                }
                val fixedRows: List<Pair<String, suspend () -> List<Drama>>> = fixed.map { (label, path) ->
                    label to { repo.browsePath(selPlatform, path) }
                }
                val tagGenreRows: List<Pair<String, suspend () -> List<Drama>>> = tagRows.map { (label, tagId) ->
                    label to { repo.browsePath(selPlatform, NetshortCatalog.categoryPath(tagId)) }
                }
                tabRows + fixedRows + tagGenreRows
            }
            // dramanova_more & dramanova_baxi upstream kosong / shortTitle-nya
            // semua null, jadi tidak dipakai.
            selPlatform == "dramanova" -> listOf(
                "Katalog" to { repo.browsePath(selPlatform, "drama/list?pageNum=1&pageSize=20&languages=in") },
                "Terpopuler" to { repo.browseDramanova("dramanova_hot") },
                "Terbaru" to { repo.browseDramanova("dramanova_new") },
                "Anime" to { repo.browseDramanova("dramanova_anime") }
            )
            selPlatform == "freereels" -> listOf(
                "Untuk Wanita" to { repo.browseFreereels("female") },
                "Untuk Pria" to { repo.browseFreereels("male") },
                "Anime" to { repo.browseFreereels("anime") },
                "Dubbing" to { repo.browseFreereels("dubbing") },
                "Segera Tayang" to { repo.browseFreereels("coming-soon") }
            )
            // ReelShort (dramahub.be): tabs dari /foryou + endpoint khusus romance/drama/completed
            selPlatform == "reelshort" -> {
                val tabs = repo.reelshortTabs().ifEmpty {
                    REELSHORT_FALLBACK_GENRES.map { (label, id) -> ReelshortTab(id, label) }
                }
                // Hindari duplikasi: foryou/new/completed sudah jadi home bundle, jadi jadikan romance/drama sebagai rak utama
                val main: List<Pair<String, suspend () -> List<Drama>>> = listOf(
                    "Romance" to suspend { repo.browsePath(selPlatform, ReelshortCatalog.ROMANCE_PATH) },
                    "Drama" to suspend { repo.browsePath(selPlatform, ReelshortCatalog.DRAMA_PATH) },
                    "Completed" to suspend { repo.browsePath(selPlatform, ReelshortCatalog.COMPLETED_PATH) },
                    "Terbaru" to suspend { repo.browsePath(selPlatform, ReelshortCatalog.NEW_PATH) }
                )
                val tabRows: List<Pair<String, suspend () -> List<Drama>>> = tabs.filter { it.id !in setOf("foryou", "new", "completed", "romance", "drama") }.map { tab ->
                    tab.name to suspend { repo.browsePath(selPlatform, ReelshortCatalog.feedPath(tab.id)) }
                }
                main + tabRows
            }
            // Anichin (dramahub.be): genres + ongoing/completed/schedule
            selPlatform == "anichin" -> {
                val genres = repo.anichinGenres().ifEmpty {
                    ANICHIN_FALLBACK_GENRES.map { (name, slug) -> AnichinGenre(name, slug) }
                }
                val genreRows: List<Pair<String, suspend () -> List<Drama>>> = genres.map { g ->
                    g.name to suspend { repo.browsePath(selPlatform, AnichinCatalog.genrePath(g.slug)) }
                }
                val fixed: List<Pair<String, suspend () -> List<Drama>>> = listOf(
                    "Ongoing" to suspend { repo.browsePath(selPlatform, AnichinCatalog.ONGOING_PATH) },
                    "Completed" to suspend { repo.browsePath(selPlatform, AnichinCatalog.COMPLETED_PATH) },
                    "Jadwal" to suspend { repo.browsePath(selPlatform, AnichinCatalog.SCHEDULE_PATH) }
                )
                fixed + genreRows
            }
            // Melolo: rak asli dari API dramahub.be (peringkat + anime),
            // sisanya lewat katalog search (feed mentok 18 judul, katalognya
            // jauh lebih dalam).
            selPlatform == "melolo" -> listOf(
                "Peringkat" to { repo.browsePath(selPlatform, "rank?page=1&lang=id") },
                "Anime" to { repo.browsePath(selPlatform, "anime?page=1&lang=id") },
                "Populer" to { repo.searchPlatform("populer", selPlatform) },
                "Romansa" to { repo.searchPlatform("cinta", selPlatform) },
                "Sistem" to { repo.searchPlatform("sistem", selPlatform) },
                "Harem" to { repo.searchPlatform("harem", selPlatform) },
                "CEO & harta" to { repo.searchPlatform("ceo", selPlatform) },
                "Balas dendam" to { repo.searchPlatform("balas dendam", selPlatform) },
                "Lintas waktu" to { repo.searchPlatform("time travel", selPlatform) },
                "Kekuatan super" to { repo.searchPlatform("kekuatan super", selPlatform) },
                "Wanita kuat" to { repo.searchPlatform("wanita kuat", selPlatform) },
                "Kelahiran kembali" to { repo.searchPlatform("kelahiran kembali", selPlatform) }
            )
            ExtraPlatforms.isExtraPlatform(selPlatform) -> listOf(
                "Romansa" to { repo.searchPlatform("cinta", selPlatform) },
                "CEO & harta" to { repo.searchPlatform("ceo", selPlatform) }
            )
            else -> listOf(
            "Populer" to { repo.searchPlatform("populer", selPlatform) },
            "Romansa" to { repo.searchPlatform("cinta", selPlatform) },
            "Sistem" to { repo.searchPlatform("sistem", selPlatform) },
            "Harem" to { repo.searchPlatform("harem", selPlatform) },
            "CEO & harta" to { repo.searchPlatform("ceo", selPlatform) },
            "Balas dendam" to { repo.searchPlatform("balas dendam", selPlatform) },
            "Lintas waktu" to { repo.searchPlatform("time travel", selPlatform) },
            "Kekuatan super" to { repo.searchPlatform("kekuatan super", selPlatform) },
            "Wanita kuat" to { repo.searchPlatform("wanita kuat", selPlatform) },
            "Kelahiran kembali" to { repo.searchPlatform("kelahiran kembali", selPlatform) }
        )
        }
        genreRows = coroutineScope {
            wanted.map { (label, fetch) ->
                async {
                    label to runCatching { fetch() }
                        .getOrDefault(emptyList())
                        .distinctBy { it.platform + "|" + it.id }
                        .filter { (it.platform + "|" + it.id) !in known }
                        .take(16)
                }
            }.awaitAll()
        }.filter { it.second.size >= 4 }
    }

    fun loadMore() {
        val cur = (homeState as? Load.Ok)?.data ?: return
        if (homeLoadingMore || !cur.hasMore) return
        val pSnap = selPlatform; val np = cur.loadedPage + 1
        homeLoadingMore = true; homeAppendError = null
        scope.launch {
            try {
                val next = repo.loadHomePage(pSnap, np)
                if (selPlatform == pSnap) {
                    val latest = (homeState as? Load.Ok)?.data
                    if (latest != null && next.loadedPage > latest.loadedPage)
                        homeState = Load.Ok(mergeHomeBundles(latest, next))
                }
            } catch (e: CancellationException) { throw e }
            catch (t: Throwable) { if (selPlatform == pSnap) homeAppendError = t.message }
            finally { if (selPlatform == pSnap) homeLoadingMore = false }
        }
    }

    BackHandler(enabled = selectedDrama != null) { selectedDrama = null; pendingResume = null }
    BackHandler(enabled = showSettings) { showSettings = false }

    Box(Modifier.fillMaxSize().background(DS.Bg)) {
        Column {
            if (!isOnline) OfflineBanner { refreshKey++ }
            Box(Modifier.weight(1f)) {
                val activeCat = category
                if (activeCat == null) {
                    CategoryHomeScreen(
                        onSelect = { picked ->
                            if (picked.comingSoon) {
                                Toast.makeText(ctx, "${picked.title} segera hadir", Toast.LENGTH_SHORT).show()
                            } else {
                                category = picked
                                tab = Tab.Home
                                val pref = store.categoryPlatform(picked.id, picked.defaultPlatform())
                                selPlatform = if (picked.containsPlatform(pref)) pref else picked.defaultPlatform()
                                store.setPlatform(selPlatform)
                                refreshKey++
                            }
                        },
                        onSettings = { showSettings = true }
                    )
                } else {
                Scaffold(
                    containerColor = DS.Bg,
                    bottomBar = {
                        BottomNavBar(if (tab == Tab.Search) Tab.Home else tab) { target ->
                            if (target == Tab.Home && tab == Tab.Home) homeScrollToTop++ else tab = target
                        }
                    }
                ) { pad ->
                    Box(Modifier.padding(pad).fillMaxSize()) {
                        when (tab) {
                            Tab.Home -> HomeScreen(
                                platformId = selPlatform, scrollToTopSignal = homeScrollToTop, state = homeState,
                                category = activeCat, onExitCategory = {},
                                history = store.history(dataTick), remoteConfig = remoteConfig,
                                remoteError = remoteError, loadingMore = homeLoadingMore,
                                loadMoreError = homeAppendError, onLoadMore = ::loadMore,
                                onPlatform = {
                                    val allowed = remoteConfig?.isPlatformEnabled(it) ?: true
                                    if (!allowed) Toast.makeText(ctx, "${platformLabel(it)}: Maintenance", Toast.LENGTH_SHORT).show()
                                    else { selPlatform = it; store.setPlatform(it); store.setCategoryPlatform(activeCat.id, it); refreshKey++ }
                                },
                                onRefresh = { refreshKey++ }, onDrama = { selectedDrama = it },
                                onSearch = { tab = Tab.Search },
                                onRandom = {
                                    val b = (homeState as? Load.Ok)?.data
                                    val pool = (b?.popular.orEmpty() + b?.newest.orEmpty() + b?.recommended.orEmpty()).filter { it.id.isNotBlank() }
                                    if (pool.isNotEmpty()) selectedDrama = pool.random()
                                },
                                onClips = {
                                    val b = (homeState as? Load.Ok)?.data
                                    val pool = (b?.popular.orEmpty() + b?.newest.orEmpty() + b?.recommended.orEmpty())
                                        .filter { it.id.isNotBlank() && it.poster.isNotBlank() }.distinctBy { it.platform + it.id }
                                    if (pool.isNotEmpty()) clipFeedItems = pool.shuffled().take(80)
                                    else Toast.makeText(ctx, "Cuplikan belum tersedia", Toast.LENGTH_SHORT).show()
                                },
                                onResume = { h -> pendingResume = h; selectedDrama = Drama(h.id, h.title, poster = h.poster, platform = h.platform) },
                                genreRows = genreRows,
                                onPickPlatform = { showPlatformPicker = true }
                            )
                            Tab.Search -> SearchScreen(repo, store, selPlatform, onDrama = { selectedDrama = it }, onBack = { tab = Tab.Home }, dataTick = dataTick, bump = { dataTick++ })
                            Tab.Clips -> ClipsScreen(homeState, repo, store, onBack = { tab = Tab.Home }, onWatchFull = { playerSession = PlayerSession(it, 1) }, onOpenDetail = { selectedDrama = it })
                            Tab.Library -> LibraryScreen(store, dataTick, onDrama = { selectedDrama = it })
                            Tab.Profile -> ProfileScreen(store, dataTick, bump = { dataTick++ })
                        }
                    }
                }
                }
            }
        }

        AnimatedVisibility(selectedDrama != null) {
            selectedDrama?.let { d ->
                DetailScreen(detailState, d, store, resolvingEpisode,
                    onClose = { selectedDrama = null },
                    onPlay = { det, ep -> openPlayer(det, ep) },
                    onFavChanged = { dataTick++ },
                    onShare = { shareDrama(ctx, it) }
                )
            }
        }

        if (showPlatformPicker) {
            val pickerPlatforms = Platforms

            PlatformPickerModal(
                platforms = pickerPlatforms,
                currentPlatform = selPlatform,
                remoteConfig = remoteConfig,
                onSelect = { id ->
                    val allowed = remoteConfig?.isPlatformEnabled(id) ?: true
                    if (!allowed) Toast.makeText(ctx, "${platformLabel(id)}: Maintenance", Toast.LENGTH_SHORT).show()
                    else {
                        selPlatform = id
                        store.setPlatform(id)
                        val cat = category
                        if (cat != null) store.setCategoryPlatform(cat.id, id)
                        showPlatformPicker = false
                        refreshKey++
                    }
                },
                onDismiss = { showPlatformPicker = false }
            )
        }

        playerSession?.let { s ->
            VerticalEpisodePlayer(s.detail, s.startEpisode, repo, store) {
                playerSession = null; dataTick++
            }
        }

        if (clipFeedItems.isNotEmpty()) {
            ClipFeedPlayer(clipFeedItems, repo, store,
                onClose = { clipFeedItems = emptyList() },
                onWatchFull = { clipFeedItems = emptyList(); playerSession = PlayerSession(it, 1) },
                onOpenDetail = { clipFeedItems = emptyList(); selectedDrama = it }
            )
        }

        if (showSettings) {
            SettingsOverlay(store, dataTick, bump = { dataTick++ }, onClose = { showSettings = false })
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// BOTTOM NAV
// ─────────────────────────────────────────────────────────────────

@Composable
private fun BottomNavBar(selected: Tab, onSelect: (Tab) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(DS.Bg)
            .border(width = 0.5.dp, color = DS.Line, shape = RoundedCornerShape(0.dp))
            .padding(horizontal = 4.dp, vertical = 7.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Tab.values().filter { it.showNav }.forEach { tab ->
            NavItem(tab, tab == selected, onSelect, Modifier.weight(1f))
        }
    }
}

@Composable
private fun NavItem(tab: Tab, active: Boolean, onSelect: (Tab) -> Unit, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clickable { onSelect(tab) }
            .padding(vertical = 3.dp)
    ) {
        Icon(
            tab.icon,
            tab.label,
            tint = if (active) DS.Green else DS.Body,
            modifier = Modifier.size(27.dp)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            tab.label,
            color = if (active) DS.Green else DS.Muted,
            fontSize = 10.5.sp,
            fontFamily = Type.Sans,
            fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1
        )
    }
}

// ─────────────────────────────────────────────────────────────────
// PLATFORM PICKER MODAL
// ─────────────────────────────────────────────────────────────────

@Composable
private fun PlatformPickerModal(
    platforms: List<PlatformInfo>,
    currentPlatform: String,
    remoteConfig: NativeRemoteConfig?,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val platformColors = emptyMap<String, Color>()

    BackHandler { onDismiss() }

    Box(
        Modifier
            .fillMaxSize()
            .background(DS.Bg.copy(alpha = 0.92f))
            .clickable { onDismiss() }
    ) {
        Surface(
            color = DS.Raise,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .wrapContentHeight()
                .heightIn(max = with(LocalConfiguration.current) { screenHeightDp.dp * 0.82f })
        ) {
            Column(
                Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                // Handle bar
                Box(
                    Modifier
                        .size(width = 32.dp, height = 3.dp)
                        .clip(RoundedCornerShape(50))
                        .background(DS.Faint)
                        .align(Alignment.CenterHorizontally)
                )
                Spacer(Modifier.height(14.dp))

                // Title
                Text(
                    "Pilih Platform",
                    color = DS.Hi,
                    fontSize = 15.sp,
                    fontFamily = Type.Sans,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(Modifier.height(14.dp))

                // Grid 2 kolom
                platforms.chunked(2).forEach { row ->
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row.forEach { platform ->
                            val selected = platform.id == currentPlatform
                            val enabled = remoteConfig?.platform(platform.id)?.enabled ?: true
                            PlatformCard(
                                platform = platform,
                                selected = selected,
                                enabled = enabled,
                                accentColor = platformColors[platform.id] ?: DS.Green,
                                onClick = { if (enabled) onSelect(platform.id) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (row.size == 1) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun PlatformCard(
    platform: PlatformInfo,
    selected: Boolean,
    enabled: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (selected) accentColor.copy(alpha = 0.12f) else DS.Card
            )
            .border(
                1.dp,
                if (selected) accentColor else DS.Line,
                RoundedCornerShape(12.dp)
            )
            .clickable(enabled = enabled) { onClick() }
            .padding(vertical = 10.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logo box
        Box(
            Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (selected) accentColor else DS.Card2),
            contentAlignment = Alignment.Center
        ) {
            when {
                platform.logoRes != 0 -> {
                    AsyncImage(
                        platform.logoRes,
                        platform.label,
                        Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
                platform.logoUrl.isNotBlank() -> {
                    AsyncImage(
                        platform.logoUrl,
                        platform.label,
                        Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
                else -> {
                    Text(
                        platform.label.take(1).uppercase(),
                        color = if (selected) DS.Ink else accentColor,
                        fontFamily = Type.Sans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(6.dp))

        // Platform name
        Text(
            platform.label,
            color = if (selected) accentColor else if (enabled) DS.Hi else DS.Faint,
            fontSize = 10.5.sp,
            fontFamily = Type.Sans,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        if (!enabled) {
            Spacer(Modifier.height(2.dp))
            Text(
                "Off",
                color = DS.Faint,
                fontSize = 8.sp,
                fontFamily = Type.Sans
            )
        }

        if (selected) {
            Spacer(Modifier.height(4.dp))
            Box(
                Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(accentColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.Check,
                    null,
                    tint = DS.Ink,
                    modifier = Modifier.size(10.dp)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// HOME SCREEN
// ─────────────────────────────────────────────────────────────────

@Composable
private fun HomeScreen(
    platformId: String, scrollToTopSignal: Int, state: Load<HomeBundle>, history: List<HistoryItem>,
    remoteConfig: NativeRemoteConfig?, remoteError: String?,
    loadingMore: Boolean, loadMoreError: String?,
    onLoadMore: () -> Unit, onPlatform: (String) -> Unit, onRefresh: () -> Unit,
    onDrama: (Drama) -> Unit, onSearch: () -> Unit, onRandom: () -> Unit,
    onClips: () -> Unit, onResume: (HistoryItem) -> Unit,
    category: HomeCategory? = null, onExitCategory: () -> Unit = {},
    genreRows: List<Pair<String, List<Drama>>> = emptyList(),
    onPickPlatform: () -> Unit = {}
) {
    val listState = rememberLazyListState()
    LaunchedEffect(scrollToTopSignal) {
        if (scrollToTopSignal > 0) listState.animateScrollToItem(0)
    }
    var requestedPage by remember(platformId) { mutableIntStateOf(0) }
    val loadedPage = (state as? Load.Ok)?.data?.loadedPage ?: 0
    val chips = category?.let { cat -> Platforms.filter { cat.platforms.contains(it.id) } } ?: Platforms
    LaunchedEffect(platformId, loadedPage) { if (loadedPage <= 1) requestedPage = loadedPage }

    LaunchedEffect(listState, state, platformId, loadingMore) {
        snapshotFlow {
            val info = listState.layoutInfo
            val last = info.visibleItemsInfo.lastOrNull()
            last != null && last.index >= info.totalItemsCount - 4 && (last.offset + last.size) <= info.viewportEndOffset + 900
        }.collect { near ->
            val data = (state as? Load.Ok)?.data ?: return@collect
            val np = data.loadedPage + 1
            if (near && data.hasMore && !loadingMore && requestedPage != np) {
                requestedPage = np; onLoadMore()
            }
        }
    }

    val latestHistory = remember(history) { history.maxByOrNull { it.updated } }
    var resumeDismissed by remember(latestHistory?.let { "${it.platform}|${it.id}|${it.episode}" }) { mutableStateOf(false) }

    Box(Modifier.fillMaxSize().background(DS.Bg)) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = if (latestHistory != null && !resumeDismissed) 96.dp else 24.dp)
    ) {
        item {
            HomeHeader(
                platformId = platformId,
                category = category,
                remoteConfig = remoteConfig,
                remoteError = remoteError,
                chips = chips,
                onExitCategory = onExitCategory,
                onSearch = onSearch,
                onRefresh = onRefresh,
                onPlatform = onPlatform,
                onPickPlatform = onPickPlatform
            )
        }

        when (state) {
            Load.Loading, Load.Idle -> item { ShimmerLoader() }
            is Load.Err -> item { ErrorCard(state.message, onRefresh) }
            is Load.Ok -> {
                val data = state.data
                val newKeys = data.newest.map { it.platform + "|" + it.id }.toSet()
                val all = (data.newest + data.popular + data.recommended)
                    .filter { it.id.isNotBlank() && it.title.isNotBlank() }
                    .distinctBy { it.platform + "|" + it.id }
                    .distinctBy { it.platform + "|" + normalizeKey(it.title) }

                val trending = data.popular
                    .filter { it.id.isNotBlank() && it.title.isNotBlank() }
                    .distinctBy { it.platform + "|" + it.id }
                    .take(10)
                if (trending.size >= 3) {
                    item(key = "trending_$platformId") {
                        Column {
                            Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.LocalFireDepartment, null, tint = DS.Green, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Lagi Trending", color = DS.Hi, fontSize = 16.sp, fontFamily = Type.Sans, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.height(12.dp))
                            LazyRow(contentPadding = PaddingValues(horizontal = 14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                itemsIndexed(trending, key = { _, d -> d.platform + "|" + d.id }) { index, d -> TrendingRankCard(index + 1, d, onDrama) }
                            }
                            Spacer(Modifier.height(20.dp))
                        }
                    }
                }

                if (all.isEmpty()) {
                    item { EmptyState("Rak masih kosong", "Coba muat ulang, atau pindah sumber dulu.", Icons.Rounded.Movie) }
                } else {
                    all.chunked(2).forEachIndexed { index, row ->
                        item(key = "grid_${platformId}_${data.loadedPage}_$index") {
                            Row(
                                Modifier.fillMaxWidth().padding(horizontal = 14.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                row.forEach { d ->
                                    DiscoverDramaCard(
                                        drama = d,
                                        isNew = newKeys.contains(d.platform + "|" + d.id),
                                        onClick = onDrama,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                repeat(2 - row.size) { Spacer(Modifier.weight(1f)) }
                            }
                            Spacer(Modifier.height(10.dp))
                        }
                    }
                }

                item {
                    Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        if (loadingMore) {
                            CircularProgressIndicator(color = DS.Green, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.height(10.dp))
                            Text("Memuat judul lain…", color = DS.Muted, fontSize = 12.sp, fontFamily = Type.Sans)
                        } else if (!data.hasMore) {
                            Text("Itu semua untuk sekarang.", color = DS.Faint, fontSize = 12.sp, fontFamily = Type.Sans)
                        }
                        loadMoreError?.let { Text(it, color = DS.Red, fontSize = 12.sp, fontFamily = Type.Sans, modifier = Modifier.padding(top = 6.dp)) }
                    }
                }
            }
        }
    }

    if (latestHistory != null) {
        AnimatedVisibility(
            visible = !resumeDismissed,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut()
        ) {
            ResumeFloatingCard(latestHistory, onResume = onResume, onDismiss = { resumeDismissed = true })
        }
    }
    }
}

/** Melolo-style trending rail: big rank numeral over the poster, real view counts only. */
@Composable
private fun TrendingRankCard(rank: Int, drama: Drama, onClick: (Drama) -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    Column(Modifier.width(120.dp).clip(RoundedCornerShape(12.dp)).clickable { onClick(drama) }) {
        Box(Modifier.fillMaxWidth().aspectRatio(0.70f).clip(shape).background(DS.Card2)) {
            AsyncImage(drama.poster, drama.title, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent, Color.Black.copy(alpha = 0.80f)))))
            Text(
                rank.toString(),
                Modifier.align(Alignment.BottomStart).padding(start = 8.dp),
                color = Color.White,
                fontSize = 42.sp,
                fontFamily = Type.Sans,
                fontWeight = FontWeight.Bold
            )
            if (drama.views.isNotBlank()) {
                Row(Modifier.align(Alignment.BottomEnd).padding(end = 7.dp, bottom = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.PlayArrow, null, tint = Color.White.copy(alpha = 0.92f), modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(2.dp))
                    Text(drama.views, color = Color.White.copy(alpha = 0.92f), fontSize = 10.sp, fontFamily = Type.Sans, fontWeight = FontWeight.Bold, maxLines = 1)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            drama.title, color = DS.Hi, fontSize = 12.sp, lineHeight = 15.5.sp, fontFamily = Type.Sans,
            fontWeight = FontWeight.SemiBold, minLines = 2, maxLines = 2, overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(4.dp))
    }
}

/** Floating resume chip above the nav bar: one tap back to the exact episode and second. */
@Composable
private fun ResumeFloatingCard(h: HistoryItem, onResume: (HistoryItem) -> Unit, onDismiss: () -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    Row(
        Modifier
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .fillMaxWidth()
            .clip(shape)
            .background(DS.Card)
            .border(1.dp, DS.Line, shape)
            .clickable { onResume(h) }
            .padding(start = 10.dp, end = 4.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.width(38.dp).height(52.dp).clip(RoundedCornerShape(8.dp)).background(DS.Card2)) {
            if (h.poster.isNotBlank()) AsyncImage(h.poster, h.title, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text("LANJUT NONTON", color = DS.Green, fontSize = 9.sp, fontFamily = Type.Sans, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(3.dp))
            Text(h.title, color = DS.Hi, fontSize = 13.sp, fontFamily = Type.Sans, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(2.dp))
            Text("Ep. ${h.episode}" + if (h.pct > 0) " · ${h.pct}% selesai" else "", color = DS.Muted, fontSize = 11.sp, fontFamily = Type.Sans, maxLines = 1)
        }
        Spacer(Modifier.width(8.dp))
        Box(
            Modifier.size(36.dp).clip(CircleShape).background(DS.Green).clickable { onResume(h) },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.PlayArrow, "Lanjut nonton", tint = DS.Ink, modifier = Modifier.size(20.dp))
        }
        IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Rounded.Close, "Tutup", tint = DS.Muted, modifier = Modifier.size(16.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// LAYAR AWAL — pintu masuk, gaya indeks majalah
// ─────────────────────────────────────────────────────────────────

@Composable
private fun CategoryHomeScreen(onSelect: (HomeCategory) -> Unit, onSettings: () -> Unit) {
    val ctx = LocalContext.current
    val greeting = remember { Greetings.forHour(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) }
    val date = remember { todayLine() }

    Box(Modifier.fillMaxSize().background(DS.Bg)) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(48.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                BrandMark(Modifier.size(44.dp))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("DRAMAKU", color = DS.Hi, fontSize = 13.sp, fontFamily = Type.Sans, fontWeight = FontWeight.Bold, letterSpacing = 3.4.sp)
                    Spacer(Modifier.height(2.dp))
                    Text(date, color = DS.Muted, fontSize = 11.5.sp, fontFamily = Type.Sans)
                }
                GhostIconButton(Icons.Rounded.Settings, "Pengaturan", onSettings)
            }

            Spacer(Modifier.height(42.dp))
            Text("TEMUKAN TONTONANMU", color = DS.Muted, fontSize = 10.sp, fontFamily = Type.Sans, fontWeight = FontWeight.Bold, letterSpacing = 1.7.sp)
            Spacer(Modifier.height(12.dp))
            Text(
                "${greeting.text},\nsiap cari cerita?",
                color = DS.Hi,
                fontSize = 35.sp,
                lineHeight = 40.sp,
                fontFamily = Type.Display,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.8).sp
            )
            Spacer(Modifier.height(10.dp))
            Text(
                "Pilih ruang tontonan yang paling cocok dengan mood kamu.",
                color = DS.Body,
                fontSize = 14.sp,
                lineHeight = 21.sp,
                fontFamily = Type.Sans
            )

            Spacer(Modifier.height(34.dp))
            Text("JELAJAHI BERDASARKAN FORMAT", color = DS.Muted, fontSize = 10.sp, fontFamily = Type.Sans, fontWeight = FontWeight.Bold, letterSpacing = 1.8.sp)
            Spacer(Modifier.height(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                CategoryCard(
                    icon = Icons.Rounded.Smartphone,
                    title = "Short Drama",
                    subtitle = "Episode vertikal singkat untuk ditonton kapan saja.",
                    accentColor = DS.Green,
                    onClick = { onSelect(HomeCategory.ShortDrama) }
                )
                CategoryCard(
                    icon = Icons.Rounded.Movie,
                    title = HomeCategory.MovieDrama.title,
                    subtitle = HomeCategory.MovieDrama.subtitle,
                    accentColor = DS.Cyan,
                    onClick = { onSelect(HomeCategory.MovieDrama) }
                )
            }

            Spacer(Modifier.height(30.dp))
            Text("JELAJAHI LEBIH JAUH", color = DS.Muted, fontSize = 10.sp, fontFamily = Type.Sans, fontWeight = FontWeight.Bold, letterSpacing = 1.8.sp)
            Spacer(Modifier.height(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                CategoryCard(
                    icon = Icons.Rounded.AutoAwesome,
                    title = HomeCategory.Anime.title,
                    subtitle = HomeCategory.Anime.subtitle,
                    accentColor = DS.Violet,
                    onClick = { onSelect(HomeCategory.Anime) }
                )
            }

            Spacer(Modifier.height(28.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(DS.Card)
                    .border(1.dp, DS.Line, RoundedCornerShape(22.dp))
                    .clickable { Toast.makeText(ctx, "Link dukungan segera ditambahkan", Toast.LENGTH_SHORT).show() }
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(DS.CyanWash),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.LocalCafe, null, tint = DS.Cyan, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(13.dp))
                Column(Modifier.weight(1f)) {
                    Text("Dukung Dramaku", color = DS.Hi, fontSize = 13.5.sp, fontFamily = Type.Sans, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(2.dp))
                    Text("Bantu server tetap cepat dan nyaman dipakai.", color = DS.Muted, fontSize = 11.5.sp, fontFamily = Type.Sans)
                }
                Icon(Icons.Rounded.NorthEast, null, tint = DS.Cyan, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun CategoryCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(22.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(DS.Card)
            .border(1.dp, DS.Line, shape)
            .clickable(onClick = onClick)
            .padding(15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(accentColor.copy(alpha = 0.18f))
                .border(1.dp, accentColor.copy(alpha = 0.24f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, title, tint = accentColor, modifier = Modifier.size(23.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = DS.Hi, fontSize = 16.sp, fontFamily = Type.Sans, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(subtitle, color = DS.Body, fontSize = 11.5.sp, lineHeight = 16.sp, fontFamily = Type.Sans, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(2.dp))
        }
        Spacer(Modifier.width(8.dp))
        Box(
            Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(accentColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.ArrowForward, null, tint = accentColor, modifier = Modifier.size(17.dp))
        }
    }
}

@Composable
private fun GateHairline() {
    Box(Modifier.fillMaxWidth().height(1.dp).background(DS.Line))
}

@Composable
private fun GateRow(index: String, title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(index, color = DS.Faint, fontSize = 14.sp, fontFamily = Type.Sans, fontWeight = FontWeight.Medium, modifier = Modifier.width(34.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = DS.Hi, fontSize = 21.sp, fontFamily = Type.Sans, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text(subtitle, color = DS.Muted, fontSize = 12.5.sp, lineHeight = 17.sp, fontFamily = Type.Sans, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        Spacer(Modifier.width(14.dp))
        Icon(Icons.Rounded.ArrowForward, null, tint = DS.Muted, modifier = Modifier.size(19.dp))
    }
}

@Composable
private fun GateSoonCard(category: HomeCategory, icon: ImageVector, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(DS.Card)
            .border(1.dp, DS.Line, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(DS.Faint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, category.title, tint = DS.Muted, modifier = Modifier.size(14.dp))
            }
            Spacer(Modifier.weight(1f))
            Text("Segera", color = DS.Faint, fontSize = 10.sp, fontFamily = Type.Sans, fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp)
        }
        Spacer(Modifier.height(14.dp))
        Text(category.title, color = DS.Hi, fontSize = 15.sp, fontFamily = Type.Sans, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(3.dp))
        Text(category.subtitle, color = DS.Muted, fontSize = 11.sp, lineHeight = 15.sp, fontFamily = Type.Sans, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun BrandMark(modifier: Modifier = Modifier) {
    Box(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(DS.Green),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Rounded.PlayArrow, "Dramaku", tint = DS.Ink, modifier = Modifier.size(23.dp))
    }
}

@Composable
private fun GhostIconButton(icon: ImageVector, label: String, onClick: () -> Unit) {
    Box(
        Modifier
            .size(41.dp)
            .clip(CircleShape)
            .background(DS.Card.copy(alpha = 0.78f))
            .border(1.dp, DS.LineStrong, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, label, tint = DS.Body, modifier = Modifier.size(19.dp))
    }
}

@Composable
private fun SettingsOverlay(store: LocalStore, dataTick: Int, bump: () -> Unit, onClose: () -> Unit) {
    Box(Modifier.fillMaxSize().background(DS.Bg)) {
        Box(Modifier.fillMaxSize().padding(top = 54.dp)) {
            ProfileScreen(store, dataTick, bump)
        }
        Box(Modifier.padding(start = 16.dp, top = 12.dp)) {
            GhostIconButton(Icons.Rounded.ArrowBack, "Kembali", onClose)
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// HEADER BERANDA + KOMPONEN BERSAMA
// ─────────────────────────────────────────────────────────────────

@Composable
private fun HomeHeader(
    platformId: String,
    category: HomeCategory?,
    remoteConfig: NativeRemoteConfig?,
    remoteError: String?,
    chips: List<PlatformInfo>,
    onExitCategory: () -> Unit,
    onSearch: () -> Unit,
    onRefresh: () -> Unit,
    onPlatform: (String) -> Unit,
    onPickPlatform: () -> Unit = {}
) {
    val online = remoteConfig?.platform(platformId)?.enabled ?: true
    val alert = remoteConfig?.message?.takeIf { it.enabled }
        ?.let { listOf(it.title, it.text).filter { value -> value.isNotBlank() }.joinToString(" · ") }
        ?: remoteError?.let { "Status server: $it" }
        ?: if (!online) "${platformLabel(platformId)} sedang gangguan" else ""

    Column(
        Modifier
            .fillMaxWidth()
            .background(DS.Bg)
            .padding(horizontal = 14.dp, vertical = 14.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(54.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(DS.Card2)
                .clickable(onClick = onSearch)
                .padding(start = 15.dp, end = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Rounded.Search, "Cari", tint = DS.Body, modifier = Modifier.size(25.dp))
            Spacer(Modifier.width(12.dp))
            Text(
                "Cari judul drama",
                color = DS.Muted,
                fontSize = 15.sp,
                fontFamily = Type.Sans,
                modifier = Modifier.weight(1f)
            )
            Box(Modifier.width(1.dp).height(30.dp).background(DS.Faint))
            Text(
                "Cari",
                color = DS.Hi,
                fontSize = 14.sp,
                fontFamily = Type.Sans,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 15.dp)
            )
        }

        Spacer(Modifier.height(15.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DiscoverShortcut(Icons.Rounded.GridView, "Kategori", DS.Cyan, onPickPlatform, Modifier.weight(1f))
            Box(Modifier.width(1.dp).height(22.dp).background(DS.LineStrong))
            DiscoverShortcut(Icons.Rounded.EmojiEvents, "Peringkat", Color(0xFFFF68B2), onRefresh, Modifier.weight(1f))
            Box(Modifier.width(1.dp).height(22.dp).background(DS.LineStrong))
            DiscoverShortcut(Icons.Rounded.AutoAwesome, "Baru Tayang", DS.Violet, onRefresh, Modifier.weight(1f))
        }

        if (alert.isNotBlank()) {
            Spacer(Modifier.height(12.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (online) DS.GreenWash else DS.RedWash)
                    .padding(horizontal = 12.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.size(7.dp).clip(CircleShape).background(if (online) DS.Green else DS.Red))
                Spacer(Modifier.width(9.dp))
                Text(alert, color = DS.Body, fontSize = 11.5.sp, fontFamily = Type.Sans, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun DiscoverShortcut(
    icon: ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier.clickable(onClick = onClick).padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(28.dp).clip(RoundedCornerShape(9.dp)).background(tint),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = DS.Hi, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(8.dp))
        Text(label, color = DS.Hi, fontSize = 13.sp, fontFamily = Type.Sans, fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
}

@Composable
private fun PlatformLogo(platformId: String, modifier: Modifier = Modifier) {
    val info = platform(platformId)
    when {
        info.logoRes != 0 -> {
            AsyncImage(info.logoRes, info.label, modifier.clip(RoundedCornerShape(7.dp)), contentScale = ContentScale.Crop)
        }
        info.logoUrl.isNotBlank() -> {
            AsyncImage(info.logoUrl, info.label, modifier.clip(RoundedCornerShape(7.dp)), contentScale = ContentScale.Fit)
        }
        else -> {
            Box(modifier.clip(RoundedCornerShape(7.dp)).background(DS.Card2), contentAlignment = Alignment.Center) {
                Text(info.label.take(1), color = DS.Hi, fontFamily = Type.Sans, fontWeight = FontWeight.Bold, fontSize = 10.sp)
            }
        }
    }
}

@Composable
private fun PlatformBadge(platformId: String, compact: Boolean = false) {
    Row(
        Modifier
            .clip(RoundedCornerShape(50))
            .background(DS.Bg.copy(alpha = 0.56f))
            .border(1.dp, DS.LineStrong, RoundedCornerShape(50))
            .padding(horizontal = if (compact) 9.dp else 11.dp, vertical = if (compact) 6.dp else 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PlatformLogo(platformId, Modifier.size(if (compact) 15.dp else 17.dp))
        Spacer(Modifier.width(6.dp))
        Text(platformLabel(platformId), color = DS.Hi, fontSize = if (compact) 10.5.sp else 11.5.sp, fontFamily = Type.Sans, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun SearchDramaCard(drama: Drama, onClick: (Drama) -> Unit, rank: Int? = null) {
    val shape = RoundedCornerShape(12.dp)
    Column(
        Modifier
            .clip(shape)
            .background(DS.Card)
            .clickable { onClick(drama) }
    ) {
        Box(Modifier.fillMaxWidth().aspectRatio(0.70f).background(DS.Card2)) {
            AsyncImage(drama.poster, drama.title, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent, Color.Black.copy(alpha = 0.72f)))))
            rank?.let {
                Text(
                    "#$it",
                    Modifier.align(Alignment.TopStart).padding(8.dp).clip(RoundedCornerShape(6.dp)).background(DS.Green).padding(horizontal = 7.dp, vertical = 4.dp),
                    color = DS.Ink,
                    fontSize = 10.sp,
                    fontFamily = Type.Sans,
                    fontWeight = FontWeight.Bold
                )
            }
            Row(Modifier.align(Alignment.BottomStart).padding(9.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.LocalFireDepartment, null, tint = DS.Hi, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text(drama.views.ifBlank { "${drama.episodes.coerceAtLeast(1)} Ep" }, color = DS.Hi, fontSize = 10.5.sp, fontFamily = Type.Sans, fontWeight = FontWeight.Bold)
            }
        }
        Column(Modifier.padding(11.dp)) {
            Text(drama.title.ifBlank { "Tanpa judul" }, color = DS.Hi, fontSize = 13.5.sp, lineHeight = 18.sp, fontFamily = Type.Sans, fontWeight = FontWeight.SemiBold, minLines = 2, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(5.dp))
            Text(
                listOfNotNull(drama.tags.firstOrNull(), drama.episodes.takeIf { it > 0 }?.let { "$it episode" }).ifEmpty { listOf(platformLabel(drama.platform)) }.joinToString(" | "),
                color = DS.Muted,
                fontSize = 10.5.sp,
                fontFamily = Type.Sans,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SearchSectionTitle(title: String, subtitle: String = "") {
    Column(Modifier.fillMaxWidth()) {
        Text(title, color = DS.Hi, fontSize = 20.sp, fontFamily = Type.Display, fontWeight = FontWeight.SemiBold)
        if (subtitle.isNotBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(subtitle, color = DS.Muted, fontSize = 12.sp, lineHeight = 16.sp, fontFamily = Type.Sans)
        }
    }
}

@Composable
private fun Section(title: String, subtitle: String = "", onSeeAll: (() -> Unit)? = null, content: @Composable () -> Unit) {
    Column(Modifier.padding(top = 28.dp)) {
        // Section header row
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    color = DS.Hi,
                    fontSize = 20.sp,
                    fontFamily = Type.Display,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.2).sp
                )
                if (subtitle.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(subtitle, color = DS.Muted, fontSize = 11.5.sp, lineHeight = 15.sp, fontFamily = Type.Sans)
                }
            }
            if (onSeeAll != null) {
                Box(
                    Modifier
                        .clip(RoundedCornerShape(50))
                        .border(1.dp, DS.LineStrong, RoundedCornerShape(50))
                        .clickable(onClick = onSeeAll)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Semua", color = DS.Body, fontSize = 11.sp, fontFamily = Type.Sans, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.width(3.dp))
                        Icon(Icons.Rounded.ChevronRight, null, tint = DS.Body, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        content()
    }
}

@Composable
private fun HeroCard(drama: Drama, onClick: (Drama) -> Unit) {
    val shape = RoundedCornerShape(28.dp)
    Box(
        Modifier
            .padding(start = 20.dp, end = 20.dp, top = 8.dp)
            .fillMaxWidth()
            .height(430.dp)
            .clip(shape)
            .background(DS.Card)
            .border(1.dp, DS.LineStrong, shape)
            .clickable { onClick(drama) }
    ) {
        AsyncImage(drama.poster, drama.title, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)

        // Gradient berlapis menjaga title dan CTA tetap terbaca tanpa menyembunyikan artwork.
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    listOf(
                        Color.Transparent,
                        DS.Bg.copy(alpha = 0.18f),
                        DS.Bg.copy(alpha = 0.76f),
                        DS.Bg.copy(alpha = 0.98f)
                    )
                )
            )
        )
        Box(
            Modifier.fillMaxSize().background(
                Brush.horizontalGradient(listOf(DS.Bg.copy(alpha = 0.36f), Color.Transparent, DS.Bg.copy(alpha = 0.20f)))
            )
        )

        Row(
            Modifier
                .align(Alignment.TopStart)
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("REKOMENDASI", color = DS.Body, fontSize = 10.sp, fontFamily = Type.Sans, fontWeight = FontWeight.Bold, letterSpacing = 1.1.sp)
            Spacer(Modifier.weight(1f))
            PlatformBadge(drama.platform, compact = true)
        }

        Column(
            Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = 18.dp, vertical = 18.dp)
        ) {
            Text("TONTON SEKARANG", color = DS.Body, fontSize = 10.sp, fontFamily = Type.Sans, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
            Spacer(Modifier.height(7.dp))
            Text(
                drama.title,
                color = DS.Hi,
                fontSize = 29.sp,
                lineHeight = 33.sp,
                fontFamily = Type.Display,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (drama.episodes > 0) {
                    Text("${drama.episodes} episode", color = DS.Body, fontSize = 11.5.sp, fontFamily = Type.Sans, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.width(7.dp))
                    Box(Modifier.size(3.dp).clip(CircleShape).background(DS.Faint))
                    Spacer(Modifier.width(7.dp))
                }
                Text(platformLabel(drama.platform), color = DS.Body, fontSize = 11.5.sp, fontFamily = Type.Sans, fontWeight = FontWeight.Bold)
                if (drama.views.isNotBlank()) {
                    Spacer(Modifier.width(7.dp))
                    Box(Modifier.size(3.dp).clip(CircleShape).background(DS.Faint))
                    Spacer(Modifier.width(7.dp))
                    Text(drama.views, color = DS.Muted, fontSize = 11.5.sp, fontFamily = Type.Sans)
                }
            }
            Spacer(Modifier.height(17.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(9.dp), verticalAlignment = Alignment.CenterVertically) {
                Row(
                    Modifier
                        .clip(RoundedCornerShape(15.dp))
                        .background(DS.Green)
                        .clickable { onClick(drama) }
                        .padding(horizontal = 17.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.PlayArrow, null, tint = DS.Ink, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(5.dp))
                    Text("Mulai nonton", color = DS.Ink, fontSize = 12.5.sp, fontFamily = Type.Sans, fontWeight = FontWeight.Bold)
                }
                Row(
                    Modifier
                        .clip(RoundedCornerShape(15.dp))
                        .background(DS.Card.copy(alpha = 0.80f))
                        .border(1.dp, DS.LineStrong, RoundedCornerShape(15.dp))
                        .clickable { onClick(drama) }
                        .padding(horizontal = 14.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.Info, null, tint = DS.Hi, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(5.dp))
                    Text("Info", color = DS.Hi, fontSize = 12.5.sp, fontFamily = Type.Sans, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun ContinueCard(h: HistoryItem, onClick: (HistoryItem) -> Unit) {
    val shape = RoundedCornerShape(17.dp)
    Column(Modifier.width(142.dp).clickable { onClick(h) }) {
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(0.70f)
                .clip(shape)
                .background(DS.Card)
                .border(1.dp, DS.Line, shape)
        ) {
            if (h.poster.isNotBlank()) {
                AsyncImage(h.poster, h.title, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.Movie, h.title, tint = DS.Faint, modifier = Modifier.size(26.dp))
                }
            }
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent, DS.Bg.copy(alpha = 0.80f)))))

            Row(
                Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(DS.Green)
                    .padding(horizontal = 7.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Rounded.PlayArrow, null, tint = DS.Ink, modifier = Modifier.size(11.dp))
                Spacer(Modifier.width(3.dp))
                Text("EP ${h.episode}", color = DS.Ink, fontSize = 9.sp, fontFamily = Type.Sans, fontWeight = FontWeight.Bold)
            }

            Box(
                Modifier
                    .align(Alignment.Center)
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(DS.Bg.copy(alpha = 0.68f))
                    .border(1.dp, DS.Hi.copy(alpha = 0.36f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.PlayArrow, null, tint = DS.Hi, modifier = Modifier.size(19.dp))
            }

            LinearProgressIndicator(
                progress = (h.pct / 100f).coerceIn(0f, 1f),
                color = DS.Cyan,
                trackColor = DS.Bg.copy(alpha = 0.50f),
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(4.dp)
            )
        }
        Spacer(Modifier.height(9.dp))
        Text(h.title, color = DS.Hi, fontSize = 12.5.sp, lineHeight = 16.sp, fontFamily = Type.Sans, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(3.dp))
        Text(if (h.pct > 0) "${h.pct}% selesai" else platformLabel(h.platform), color = DS.Muted, fontSize = 10.5.sp, fontFamily = Type.Sans, maxLines = 1)
    }
}

@Composable
private fun DiscoverDramaCard(
    drama: Drama, isNew: Boolean, onClick: (Drama) -> Unit, modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(12.dp)
    Column(
        modifier
            .clip(shape)
            .background(DS.Card)
            .clickable { onClick(drama) }
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(0.70f)
                .background(DS.Card2)
        ) {
            AsyncImage(drama.poster, drama.title, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent, Color.Black.copy(alpha = 0.70f)))))
            if (isNew) {
                Text(
                    "BARU",
                    Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(DS.Green)
                        .padding(horizontal = 7.dp, vertical = 4.dp),
                    color = DS.Ink,
                    fontSize = 9.sp,
                    fontFamily = Type.Sans,
                    fontWeight = FontWeight.Bold
                )
            }
            Row(
                Modifier.align(Alignment.BottomStart).padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Rounded.LocalFireDepartment, null, tint = DS.Hi, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(4.dp))
                Text(
                    drama.views.ifBlank { "${drama.episodes.coerceAtLeast(1)} episode" },
                    color = DS.Hi,
                    fontSize = 11.5.sp,
                    fontFamily = Type.Sans,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }
        Column(Modifier.padding(horizontal = 12.dp, vertical = 12.dp)) {
            Text(
                drama.title.ifBlank { "Tanpa judul" },
                color = DS.Hi,
                fontSize = 15.sp,
                fontFamily = Type.Sans,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 20.sp,
                minLines = 2,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(6.dp))
            val meta = listOfNotNull(
                drama.tags.firstOrNull()?.takeIf { it.isNotBlank() },
                drama.episodes.takeIf { it > 0 }?.let { "$it episode" }
            ).ifEmpty { listOf(platformLabel(drama.platform)) }.joinToString(" | ")
            Text(meta, color = DS.Muted, fontSize = 11.5.sp, fontFamily = Type.Sans, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun PosterImage(url: String, title: String, modifier: Modifier) {
    val shape = RoundedCornerShape(14.dp)
    Box(modifier.clip(shape).background(DS.Card).border(1.dp, DS.Line, shape)) {
        if (url.isNotBlank()) {
            AsyncImage(url, title, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, DS.Bg.copy(alpha = 0.44f)))))
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.Movie, title, tint = DS.Faint, modifier = Modifier.size(26.dp))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// SHIMMER LOADER
// ─────────────────────────────────────────────────────────────────

@Composable
private fun ShimmerLoader() {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val offset by transition.animateFloat(0f, 1400f, infiniteRepeatable(tween(1200, easing = LinearEasing), RepeatMode.Restart), label = "s")
    val brush = Brush.linearGradient(
        listOf(DS.Card, DS.Card2, DS.Raise, DS.Card2, DS.Card),
        start = Offset(offset - 400f, offset - 400f),
        end = Offset(offset, offset)
    )

    Column(Modifier.padding(horizontal = 14.dp)) {
        repeat(2) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                repeat(2) {
                    Column(Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(DS.Card)) {
                        Box(Modifier.fillMaxWidth().aspectRatio(0.70f).background(brush))
                        Column(Modifier.padding(12.dp)) {
                            Box(Modifier.fillMaxWidth(0.88f).height(15.dp).clip(RoundedCornerShape(3.dp)).background(brush))
                            Spacer(Modifier.height(7.dp))
                            Box(Modifier.fillMaxWidth(0.58f).height(11.dp).clip(RoundedCornerShape(3.dp)).background(brush))
                        }
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// STATUS SCREEN — error, offline, placeholder, kosong
// ─────────────────────────────────────────────────────────────────

@Composable
private fun ErrorCard(message: String, onRetry: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(DS.RedWash),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.CloudOff, null, tint = DS.Red, modifier = Modifier.size(28.dp))
        }
        Spacer(Modifier.height(20.dp))
        Text("Gagal memuat", color = DS.Hi, fontSize = 18.sp, fontFamily = Type.Sans, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Text(
            message,
            color = DS.Muted,
            fontSize = 13.sp,
            lineHeight = 19.sp,
            fontFamily = Type.Sans,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        Box(
            Modifier
                .clip(RoundedCornerShape(50))
                .background(DS.Green)
                .clickable(onClick = onRetry)
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Refresh, null, tint = DS.Ink, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("Coba lagi", color = DS.Ink, fontFamily = Type.Sans, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun OfflineBanner(onRefresh: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(DS.RedWash)
            .padding(horizontal = 18.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(DS.Red.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.WifiOff, null, tint = DS.Red, modifier = Modifier.size(13.dp))
        }
        Spacer(Modifier.width(10.dp))
        Text("Koneksi terputus", color = DS.Hi, fontSize = 12.5.sp, fontFamily = Type.Sans, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        Text(
            "Muat ulang",
            color = DS.Green,
            fontSize = 12.5.sp,
            fontFamily = Type.Sans,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(DS.GreenWash)
                .clickable(onClick = onRefresh)
                .padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

@Composable
private fun PlaceholderScreen(title: String, subtitle: String, icon: ImageVector) {
    Box(Modifier.fillMaxSize().background(DS.Bg), contentAlignment = Alignment.Center) {
        Column(
            Modifier.padding(horizontal = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(DS.GreenWash),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = DS.Green, modifier = Modifier.size(30.dp))
            }
            Spacer(Modifier.height(20.dp))
            Text(title, color = DS.Hi, fontSize = 20.sp, fontFamily = Type.Sans, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Text(subtitle, color = DS.Muted, fontSize = 13.sp, lineHeight = 20.sp, fontFamily = Type.Sans, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun EmptyState(title: String, subtitle: String, icon: ImageVector) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 36.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(DS.Card)
                .border(1.dp, DS.Line, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = DS.Faint, modifier = Modifier.size(26.dp))
        }
        Spacer(Modifier.height(18.dp))
        Text(title, color = DS.Hi, fontSize = 17.sp, fontFamily = Type.Sans, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        Text(subtitle, color = DS.Muted, fontSize = 12.5.sp, lineHeight = 18.sp, fontFamily = Type.Sans, textAlign = TextAlign.Center)
    }
}

// ─────────────────────────────────────────────────────────────────
// CUPLIKAN
// ─────────────────────────────────────────────────────────────────

@Composable
private fun ClipsScreen(state: Load<HomeBundle>, repo: DramakuRepository, store: LocalStore, onBack: () -> Unit, onWatchFull: (Detail) -> Unit, onOpenDetail: (Drama) -> Unit) {
    when (state) {
        Load.Loading, Load.Idle -> Box(Modifier.fillMaxSize().background(DS.Bg), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = DS.Green, strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
                Spacer(Modifier.height(14.dp))
                Text("Menyiapkan cuplikan…", color = DS.Body, fontFamily = Type.Sans, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }
        }
        is Load.Err -> ErrorCard(state.message, onBack)
        is Load.Ok -> {
            val pool = remember(state.data) {
                (state.data.popular + state.data.newest + state.data.recommended).filter { it.id.isNotBlank() && it.poster.isNotBlank() }.distinctBy { it.platform + it.id }.take(100)
            }
            if (pool.isEmpty()) PlaceholderScreen("Cuplikan", "Belum tersedia untuk platform ini.", Icons.Rounded.PlayCircle)
            else ClipFeedPlayer(pool, repo, store, onClose = onBack, onWatchFull = onWatchFull, onOpenDetail = onOpenDetail)
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// CARI
// ─────────────────────────────────────────────────────────────────

@Composable
private fun SearchScreen(repo: DramakuRepository, store: LocalStore, currentPlatform: String, onDrama: (Drama) -> Unit, onBack: () -> Unit, dataTick: Int, bump: () -> Unit) {
    var q by remember { mutableStateOf("") }
    var state by remember { mutableStateOf<Load<List<Drama>>>(Load.Idle) }
    var showcase by remember { mutableStateOf<Load<HomeBundle>>(Load.Loading) }
    var searchPlatformId by remember { mutableStateOf(currentPlatform) }
    var searchTick by remember { mutableIntStateOf(0) }
    val recent = remember(dataTick) { store.recentSearches() }
    // Saran dari /search-hint — saat ini cuma NetShort yang punya endpoint ini.
    var hints by remember { mutableStateOf(emptyList<String>()) }

    BackHandler { onBack() }
    LaunchedEffect(currentPlatform) { searchPlatformId = currentPlatform }

    LaunchedEffect(searchPlatformId) {
        hints = when (searchPlatformId) {
            "netshort" -> runCatching { repo.netshortSearchHints() }.getOrDefault(emptyList())
            "reelshort" -> runCatching { repo.reelshortSearchHints() }.getOrDefault(emptyList())
            "anichin" -> runCatching { repo.anichinSearchHints() }.getOrDefault(emptyList())
            else -> emptyList()
        }
    }

    LaunchedEffect(searchPlatformId) {
        if (q.trim().length < 2) {
            showcase = Load.Loading
            showcase = runCatching { repo.loadHome(searchPlatformId) }
                .fold({ Load.Ok(it) }, { Load.Err(it.message ?: "Gagal memuat") })
        }
    }

    LaunchedEffect(q, searchPlatformId, searchTick) {
        val query = q.trim()
        if (query.length < 2) { state = Load.Idle; return@LaunchedEffect }
        delay(260)
        state = Load.Loading
        // Riwayat pencarian hanya dicatat saat pengguna membuka hasil,
        // bukan setiap potongan ketikan yang lolos debounce.
        state = runCatching { repo.searchPlatform(query, searchPlatformId) }
            .fold({ Load.Ok(it) }, { Load.Err(it.message ?: "Gagal") })
        bump()
    }

    Column(Modifier.fillMaxSize().background(DS.Bg).padding(horizontal = 20.dp, vertical = 14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            GhostIconButton(Icons.Rounded.ArrowBack, "Kembali", onBack)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text("Cari drama", color = DS.Hi, fontSize = 23.sp, fontFamily = Type.Sans, fontWeight = FontWeight.SemiBold)
                Text("Dari rak ${platformLabel(searchPlatformId)}", color = DS.Muted, fontSize = 12.sp, fontFamily = Type.Sans, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            PlatformBadge(searchPlatformId, compact = true)
        }

        Spacer(Modifier.height(18.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .height(54.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(DS.Card2)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Rounded.Search, null, tint = DS.Muted, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(11.dp))
            Box(Modifier.weight(1f)) {
                if (q.isBlank()) {
                    Text("Ketik judul drama…", color = DS.Faint, fontSize = 14.5.sp, fontFamily = Type.Sans, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                BasicTextField(
                    value = q,
                    onValueChange = { q = it },
                    singleLine = true,
                    textStyle = TextStyle(color = DS.Hi, fontSize = 14.5.sp, fontFamily = Type.Sans, fontWeight = FontWeight.Medium),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (q.isNotBlank()) {
                IconButton(onClick = { q = "" }, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Rounded.Close, "Hapus", tint = DS.Muted, modifier = Modifier.size(17.dp))
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(Platforms, key = { it.id }) { p ->
                val selected = p.id == searchPlatformId
                Row(
                    Modifier
                        .clip(RoundedCornerShape(50))
                        .background(if (selected) DS.GreenWash else Color.Transparent)
                        .border(1.dp, if (selected) DS.Green.copy(alpha = 0.4f) else DS.Line, RoundedCornerShape(50))
                        .clickable { searchPlatformId = p.id }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PlatformLogo(p.id, Modifier.size(15.dp))
                    Spacer(Modifier.width(7.dp))
                    Text(p.label, color = if (selected) DS.Hi else DS.Body, fontSize = 11.5.sp, fontFamily = Type.Sans, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium)
                }
            }
        }

        Spacer(Modifier.height(18.dp))
        Box(Modifier.weight(1f).fillMaxWidth()) {
            when {
                q.trim().length >= 2 -> when (val searchState = state) {
                    Load.Idle, Load.Loading -> {
                        Column(Modifier.fillMaxWidth()) {
                            LinearProgressIndicator(color = DS.Green, trackColor = DS.Card2, modifier = Modifier.fillMaxWidth().height(2.dp).clip(RoundedCornerShape(50)))
                            Spacer(Modifier.height(14.dp))
                            Text("Mencari di ${platformLabel(searchPlatformId)}…", color = DS.Muted, fontSize = 12.5.sp, fontFamily = Type.Sans)
                        }
                    }
                    is Load.Err -> ErrorCard(searchState.message) { searchTick++ }
                    is Load.Ok -> {
                        val all = searchState.data
                        if (all.isEmpty()) {
                            EmptyState("Tidak ada hasil", "Coba kata kunci lain, atau ganti platform.", Icons.Rounded.Search)
                        } else {
                            Column(Modifier.fillMaxSize()) {
                                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Text("${all.size} judul ditemukan", color = DS.Hi, fontSize = 13.sp, fontFamily = Type.Sans, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                                    Text(platformLabel(searchPlatformId), color = DS.Muted, fontSize = 11.5.sp, fontFamily = Type.Sans)
                                }
                                Spacer(Modifier.height(12.dp))
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(2),
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(all, key = { it.platform + it.id }) { d ->
                                        SearchDramaCard(d, { picked -> store.saveRecent(q.trim()); bump(); onDrama(picked) })
                                    }
                                }
                            }
                        }
                    }
                }
                else -> {
                    val popular = (showcase as? Load.Ok)?.data?.popular.orEmpty().take(6)
                    val trend = ((showcase as? Load.Ok)?.data?.newest.orEmpty() + (showcase as? Load.Ok)?.data?.recommended.orEmpty())
                        .distinctBy { it.platform + it.id }
                        .take(6)
                    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                        if (recent.isNotEmpty()) {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.weight(1f)) { SearchSectionTitle("Pencarian terakhir", "Ketuk untuk mencari lagi.") }
                                Text(
                                    "Hapus semua",
                                    Modifier.clip(RoundedCornerShape(8.dp)).clickable { store.clearRecentSearches(); bump() }.padding(horizontal = 8.dp, vertical = 6.dp),
                                    color = DS.Muted, fontSize = 11.5.sp, fontFamily = Type.Sans, fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            recent.take(8).forEach { keyword ->
                                Row(
                                    Modifier.fillMaxWidth().clickable { q = keyword }.padding(vertical = 11.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Rounded.History, null, tint = DS.Faint, modifier = Modifier.size(17.dp))
                                    Spacer(Modifier.width(12.dp))
                                    Text(keyword, color = DS.Hi, fontSize = 13.5.sp, fontFamily = Type.Sans, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    IconButton(onClick = { store.removeRecentSearch(keyword); bump() }, modifier = Modifier.size(26.dp)) {
                                        Icon(Icons.Rounded.Close, "Hapus", tint = DS.Faint, modifier = Modifier.size(16.dp))
                                    }
                                }
                                GateHairline()
                            }
                            Spacer(Modifier.height(22.dp))
                        }

                        if (hints.isNotEmpty()) {
                            SearchSectionTitle("Saran pencarian", "Kata kunci yang sedang dicari di platform ini.")
                            Spacer(Modifier.height(10.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(hints, key = { it }) { hint ->
                                    Row(
                                        Modifier
                                            .clip(RoundedCornerShape(50))
                                            .background(DS.Card)
                                            .border(1.dp, DS.Line, RoundedCornerShape(50))
                                            .clickable { q = hint }
                                            .padding(horizontal = 13.dp, vertical = 9.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Rounded.Search, null, tint = DS.Faint, modifier = Modifier.size(13.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text(hint, color = DS.Hi, fontSize = 12.sp, fontFamily = Type.Sans, fontWeight = FontWeight.Medium, maxLines = 1)
                                    }
                                }
                            }
                            Spacer(Modifier.height(22.dp))
                        }

                        SearchSectionTitle("Lagi ramai", "Judul yang sering dibuka di platform ini.")
                        Spacer(Modifier.height(14.dp))
                        if (popular.isNotEmpty()) {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                userScrollEnabled = false,
                                modifier = Modifier.height((((popular.size + 1) / 2) * 320).dp)
                            ) {
                                items(popular.size) { index -> SearchDramaCard(popular[index], onDrama, index + 1) }
                            }
                        } else if (showcase is Load.Loading) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(color = DS.Green, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(12.dp))
                                Text("Menyiapkan rekomendasi…", color = DS.Muted, fontSize = 12.5.sp, fontFamily = Type.Sans)
                            }
                        }

                        Spacer(Modifier.height(26.dp))
                        SearchSectionTitle("Coba juga", "Campuran rilisan baru dan rekomendasi.")
                        Spacer(Modifier.height(14.dp))
                        if (trend.isNotEmpty()) {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                userScrollEnabled = false,
                                modifier = Modifier.height((((trend.size + 1) / 2) * 320).dp)
                            ) {
                                items(trend.size) { index -> SearchDramaCard(trend[index], onDrama, index + 1) }
                            }
                        }
                        Spacer(Modifier.height(20.dp))
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// KOLEKSI
// ─────────────────────────────────────────────────────────────────

@Composable
private fun LibraryScreen(store: LocalStore, dataTick: Int, onDrama: (Drama) -> Unit) {
    val ctx = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var localTick by remember { mutableIntStateOf(0) }
    val history = remember(dataTick, localTick) { store.history(dataTick + localTick) }
    val favs = remember(dataTick, localTick) { store.favs() }
    val likes = remember(dataTick, localTick) { store.likes() }
    val saved = if (selectedTab == 1) favs else likes
    val tabs = listOf("Histori", "Koleksi", "Suka")

    Column(Modifier.fillMaxSize().background(DS.Bg).padding(horizontal = 22.dp)) {
        Spacer(Modifier.height(18.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            tabs.forEachIndexed { index, label ->
                Text(
                    label,
                    color = if (selectedTab == index) DS.Green else DS.Muted,
                    fontSize = 21.sp,
                    fontFamily = Type.Sans,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { selectedTab = index }.padding(end = 28.dp, top = 8.dp, bottom = 8.dp)
                )
            }
            Spacer(Modifier.weight(1f))
            Icon(Icons.Rounded.MoreHoriz, "Menu", tint = DS.Body, modifier = Modifier.size(27.dp))
        }

        Spacer(Modifier.height(22.dp))
        Text(
            when (selectedTab) { 0 -> "Hari Ini"; 1 -> "Tersimpan"; else -> "Disukai" },
            color = DS.Muted,
            fontSize = 15.sp,
            fontFamily = Type.Sans,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(16.dp))

        when (selectedTab) {
            0 -> if (history.isEmpty()) {
                EmptyState("Belum ada histori", "Mulai nonton, nanti progres kamu muncul di sini.", Icons.Rounded.History)
            } else {
                LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(18.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
                    items(history, key = { it.id + it.platform }) { h ->
                        LibraryRow(
                            h.title,
                            "${platformLabel(h.platform)} · Ep.${h.episode}${if (h.pct > 0) " · ${h.pct}% ditonton" else ""}",
                            h.poster,
                            onDelete = {
                                store.removeHistory(h.id, h.platform); localTick++; Toast.makeText(ctx, "Histori dihapus", Toast.LENGTH_SHORT).show()
                            }
                        ) { onDrama(Drama(h.id, h.title, poster = h.poster, platform = h.platform)) }
                    }
                }
            }
            else -> if (saved.isEmpty()) {
                EmptyState(
                    if (selectedTab == 1) "Koleksi masih kosong" else "Belum ada yang disukai",
                    if (selectedTab == 1) "Simpan drama ke koleksi agar mudah ditemukan lagi." else "Ketuk Suka di pemutar untuk menambahkan drama ke sini.",
                    Icons.Rounded.FavoriteBorder
                )
            } else {
                LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(18.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
                    items(saved, key = { it.platform + it.id }) { d ->
                        LibraryRow(
                            d.title,
                            listOfNotNull(d.tags.take(2).joinToString(", ").takeIf { it.isNotBlank() }, d.episodes.takeIf { it > 0 }?.let { "$it episode" }).joinToString(" · ").ifBlank { platformLabel(d.platform) },
                            d.poster,
                            onDelete = {
                                if (selectedTab == 1) store.removeFav(d.id, d.platform) else store.removeLike(d.id, d.platform)
                                localTick++; Toast.makeText(ctx, if (selectedTab == 1) "Koleksi diperbarui" else "Daftar suka diperbarui", Toast.LENGTH_SHORT).show()
                            }
                        ) { onDrama(d) }
                    }
                }
            }
        }
    }
}

@Composable
private fun LibraryRow(title: String, subtitle: String, poster: String, onDelete: (() -> Unit)? = null, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PosterImage(poster, title, Modifier.width(98.dp).height(138.dp))
        Spacer(Modifier.width(18.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = DS.Hi, fontSize = 17.sp, lineHeight = 22.sp, fontFamily = Type.Sans, fontWeight = FontWeight.SemiBold, maxLines = 3, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(9.dp))
            Text(subtitle, color = DS.Muted, fontSize = 13.sp, lineHeight = 18.sp, fontFamily = Type.Sans, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        if (onDelete != null) {
            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Rounded.MoreVert, "Opsi", tint = DS.Muted, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun StatTile(value: String, label: String, modifier: Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(DS.Card)
            .border(1.dp, DS.Line, RoundedCornerShape(16.dp))
            .padding(vertical = 13.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, color = DS.Hi, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, fontFamily = Type.Display, maxLines = 1)
        Spacer(Modifier.height(4.dp))
        Text(label, color = DS.Muted, fontSize = 10.5.sp, fontFamily = Type.Sans, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun SegOption(text: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) DS.Green else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            fontSize = 12.5.sp,
            fontFamily = Type.Sans,
            fontWeight = FontWeight.SemiBold,
            color = if (selected) DS.Ink else DS.Muted
        )
    }
}

// ─────────────────────────────────────────────────────────────────
// PROFIL
// ─────────────────────────────────────────────────────────────────

@Composable
private fun ProfileScreen(store: LocalStore, dataTick: Int, bump: () -> Unit) {
    val ctx = LocalContext.current
    var dataSaver by remember(dataTick) { mutableStateOf(store.dataSaver()) }
    var autoNext by remember(dataTick) { mutableStateOf(store.autoNext()) }
    var fitContain by remember(dataTick) { mutableStateOf(store.fitContain()) }
    var subtitleOn by remember(dataTick) { mutableStateOf(store.subtitleOn()) }
    var reminderOn by remember(dataTick) { mutableStateOf(store.resumeReminder()) }
    val notifPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (!granted) {
            reminderOn = false; store.setResumeReminder(false)
            Toast.makeText(ctx, "Izin notifikasi ditolak. Pengingat dimatikan.", Toast.LENGTH_SHORT).show()
        }
    }
    var dialog by remember { mutableStateOf<String?>(null) }
    val hCount = remember(dataTick) { store.history(dataTick).size }
    val fCount = remember(dataTick) { store.favs().size }
    val rCount = remember(dataTick) { store.recentSearches().size }
    val version = remember { try { ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionName } catch (_: Exception) { "4.9" } }

    Column(Modifier.fillMaxSize().background(DS.Bg).verticalScroll(rememberScrollState()).padding(horizontal = 22.dp)) {
        Spacer(Modifier.height(20.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            BrandMark(Modifier.size(66.dp).clip(CircleShape))
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text("Dramaku", color = DS.Hi, fontSize = 27.sp, fontFamily = Type.Sans, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text("Tanpa akun · semua data tersimpan lokal", color = DS.Muted, fontSize = 12.sp, fontFamily = Type.Sans)
            }
            Text("v$version", color = DS.Muted, fontSize = 11.sp, fontFamily = Type.Sans, fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.height(24.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            ProfileQuickAction(Icons.Rounded.History, "Histori") { Toast.makeText(ctx, "$hCount tontonan tersimpan", Toast.LENGTH_SHORT).show() }
            ProfileQuickAction(Icons.Rounded.Translate, "Bahasa") { dialog = "language" }
            ProfileQuickAction(Icons.Rounded.Tune, "Preferensi") { Toast.makeText(ctx, "Preferensi ada di bawah", Toast.LENGTH_SHORT).show() }
            ProfileQuickAction(Icons.Rounded.HeadsetMic, "Umpan Balik") { dialog = "feedback" }
        }

        Spacer(Modifier.height(24.dp))
        GateHairline()
        Spacer(Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatTile("$hCount", "Histori", Modifier.weight(1f))
            StatTile("$fCount", "Koleksi", Modifier.weight(1f))
            StatTile("$rCount", "Pencarian", Modifier.weight(1f))
        }

        Spacer(Modifier.height(26.dp))
        GroupTitle("PREFERENSI PEMUTARAN")
        SettingsGroup {
            SettingsSwitch("Hemat data", "Batasi video ke 480p supaya kuota lebih awet.", dataSaver) { dataSaver = it; store.setDataSaver(it); bump() }
            GroupDivider()
            SettingsSwitch("Putar lanjut otomatis", "Episode berikutnya diputar sendiri.", autoNext) { autoNext = it; store.setAutoNext(it); bump() }
            GroupDivider()
            SettingsSwitch("Rasio asli", "Tampilkan video tanpa crop.", fitContain) { fitContain = it; store.setFitContain(it); bump() }
            GroupDivider()
            SettingsSwitch("Subtitle", "Tampilkan teks kalau sumbernya menyediakan.", subtitleOn) { subtitleOn = it; store.setSubtitleOn(it); bump() }
            GroupDivider()
            SettingsSwitch("Pengingat lanjut nonton", "Diingatkan sehari kemudian kalau ada episode yang belum selesai.", reminderOn) { on ->
                reminderOn = on; store.setResumeReminder(on); bump()
                if (on) {
                    if (Build.VERSION.SDK_INT >= 33) notifPermission.launch("android.permission.POST_NOTIFICATIONS")
                    ResumeReminder.schedule(ctx)
                } else ResumeReminder.cancel(ctx)
            }
        }

        Spacer(Modifier.height(22.dp))
        GroupTitle("TENTANG")
        SettingsGroup {
            SettingsRow("Tentang Dramaku") { dialog = "about" }
            GroupDivider()
            SettingsRow("Privasi") { dialog = "privacy" }
            GroupDivider()
            SettingsRow("Disclaimer") { dialog = "disclaimer" }
        }

        Spacer(Modifier.height(22.dp))
        GroupTitle("BERSIHKAN DATA", danger = true)
        SettingsGroup {
            DangerRow("Hapus riwayat") { store.clearHistory(); bump(); Toast.makeText(ctx, "Riwayat dihapus", Toast.LENGTH_SHORT).show() }
            GroupDivider()
            DangerRow("Hapus favorit") { store.clearFavs(); bump(); Toast.makeText(ctx, "Favorit dihapus", Toast.LENGTH_SHORT).show() }
            GroupDivider()
            DangerRow("Hapus suka") { store.clearLikes(); bump(); Toast.makeText(ctx, "Daftar suka dihapus", Toast.LENGTH_SHORT).show() }
            GroupDivider()
            DangerRow("Hapus pencarian") { store.clearRecentSearches(); bump(); Toast.makeText(ctx, "Pencarian dihapus", Toast.LENGTH_SHORT).show() }
        }
        Spacer(Modifier.height(32.dp))
        Text("Dibuat pelan-pelan, ditonton lama-lama.", color = DS.Faint, fontSize = 11.5.sp, fontFamily = Type.Sans, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(28.dp))
    }

    dialog?.let { type ->
        val (title, body) = when (type) {
            "privacy" -> "Privasi" to "Riwayat, favorit, pencarian, dan progress nonton disimpan lokal di perangkat kamu. Dramaku tidak meng-host video dan tidak membuat akun pengguna."
            "disclaimer" -> "Disclaimer" to "Konten tetap milik platform masing-masing. Dramaku hanya menampilkan data dari sumber pihak ketiga sebagai client/aggregator. Gunakan dengan bijak."
            "language" -> "Bahasa" to "Antarmuka Dramaku saat ini menggunakan Bahasa Indonesia. Subtitle mengikuti ketersediaan dari tiap sumber."
            "feedback" -> "Umpan Balik" to "Fitur umpan balik tidak meminta akun atau data login. Gunakan kanal dukungan proyek untuk mengirim saran dan laporan."
            else -> {
                "Tentang Dramaku" to "Versi: $version\n\nDramaku Native dibuat untuk nonton drama pendek, serial Asia, dan film dalam satu aplikasi.\n\n• Player native berbasis ExoPlayer\n• Swipe episode vertikal\n• Favorit, riwayat, dan progress lokal\n• Mode hemat data\n\nDibangun dengan Kotlin + Jetpack Compose dan Plus Jakarta Sans."
            }
        }
        AlertDialog(
            onDismissRequest = { dialog = null },
            confirmButton = { TextButton(onClick = { dialog = null }) { Text("Tutup", color = DS.Green, fontFamily = Type.Sans, fontWeight = FontWeight.Bold) } },
            title = { Text(title, color = DS.Hi, fontFamily = Type.Sans, fontWeight = FontWeight.SemiBold, fontSize = 20.sp) },
            text = { Text(body, color = DS.Body, fontSize = 13.5.sp, lineHeight = 20.sp, fontFamily = Type.Sans) },
            containerColor = DS.Raise,
            shape = RoundedCornerShape(22.dp)
        )
    }
}

@Composable
private fun ProfileQuickAction(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(
        Modifier.width(76.dp).clickable(onClick = onClick).padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, label, tint = DS.Body, modifier = Modifier.size(27.dp))
        Spacer(Modifier.height(8.dp))
        Text(label, color = DS.Muted, fontSize = 11.5.sp, fontFamily = Type.Sans, textAlign = TextAlign.Center, maxLines = 1)
    }
}

@Composable
private fun GroupTitle(title: String, danger: Boolean = false) {
    Text(
        title,
        color = if (danger) DS.Red else DS.Green,
        fontSize = 10.5.sp,
        fontFamily = Type.Sans,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.8.sp,
        modifier = Modifier.padding(bottom = 10.dp)
    )
}

@Composable
private fun SettingsGroup(content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxWidth(), content = content)
}

@Composable
private fun GroupDivider() {
    Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(1.dp).background(DS.Line))
}

@Composable
private fun SettingsSwitch(title: String, subtitle: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, color = DS.Hi, fontSize = 13.5.sp, fontFamily = Type.Sans, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(2.dp))
            Text(subtitle, color = DS.Muted, fontSize = 11.5.sp, lineHeight = 15.sp, fontFamily = Type.Sans)
        }
        Switch(
            checked,
            onChecked,
            colors = SwitchDefaults.colors(
                checkedThumbColor = DS.Ink,
                checkedTrackColor = DS.Green,
                uncheckedThumbColor = DS.Muted,
                uncheckedTrackColor = DS.Card2,
                uncheckedBorderColor = DS.Card2
            )
        )
    }
}

@Composable
private fun SettingsRow(title: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = DS.Hi, fontSize = 13.5.sp, fontFamily = Type.Sans, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        Icon(Icons.Rounded.ChevronRight, null, tint = DS.Faint, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun DangerRow(title: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = DS.Red, fontSize = 13.5.sp, fontFamily = Type.Sans, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        Icon(Icons.Rounded.ChevronRight, null, tint = DS.Red.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun Chip(text: String, selected: Boolean = false, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Box(
        modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) DS.Green else DS.Card)
            .border(1.dp, if (selected) DS.Green.copy(alpha = 0.55f) else DS.Line, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, fontSize = 12.sp, fontFamily = Type.Sans, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, color = if (selected) DS.Ink else DS.Body)
    }
}

// ─────────────────────────────────────────────────────────────────
// DETAIL DRAMA
// ─────────────────────────────────────────────────────────────────

@Composable
private fun DetailScreen(state: Load<Detail>, fallback: Drama, store: LocalStore, resolvingEpisode: Int, onClose: () -> Unit, onPlay: (Detail, Int) -> Unit, onFavChanged: () -> Unit, onShare: (Drama) -> Unit) {
    val detail = (state as? Load.Ok)?.data ?: Detail(fallback)
    val drama = detail.drama
    val isFav = store.isFav(drama.id, drama.platform)
    val hist = store.history().firstOrNull { it.id == drama.id && it.platform == drama.platform }
    val resumeEp = hist?.episode?.coerceAtLeast(1) ?: 1
    val total = episodeCount(detail).coerceAtLeast(1)
    val isSingleVideo = total <= 1
    var detailRange by remember(drama.id, total) { mutableIntStateOf(((resumeEp - 1) / 30).coerceAtLeast(0)) }

    Box(Modifier.fillMaxSize().background(DS.Bg)) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 32.dp)) {
            item {
                // Backdrop sebagai panel artwork — lebih rapi dari poster full-bleed biasa.
                val backdropShape = RoundedCornerShape(28.dp)
                Box(
                    Modifier
                        .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 0.dp)
                        .fillMaxWidth()
                        .height(438.dp)
                        .clip(backdropShape)
                        .background(DS.Card)
                        .border(1.dp, DS.LineStrong, backdropShape)
                ) {
                    AsyncImage(drama.poster, drama.title, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)

                    Box(Modifier.fillMaxSize().background(
                        Brush.verticalGradient(
                            listOf(
                                DS.Bg.copy(alpha = 0.10f),
                                Color.Transparent,
                                DS.Bg.copy(alpha = 0.32f),
                                DS.Bg.copy(alpha = 0.84f),
                                DS.Bg
                            ),
                            startY = 0f
                        )
                    ))

                    // Top bar: back + platform badge
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(DS.Bg.copy(alpha = 0.58f))
                                .border(1.dp, DS.LineStrong, CircleShape)
                                .clickable(onClick = onClose),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.ArrowBack, "Kembali", tint = DS.Hi, modifier = Modifier.size(18.dp))
                        }
                        Spacer(Modifier.weight(1f))
                        PlatformBadge(drama.platform, compact = true)
                    }

                    // Bottom info overlay
                    Column(Modifier.align(Alignment.BottomStart).padding(horizontal = 20.dp, vertical = 18.dp)) {
                        // Tags row
                        if (drama.tags.isNotEmpty()) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(bottom = 10.dp)
                            ) {
                                drama.tags.take(3).forEach { tag ->
                                    Text(
                                        tag,
                                        color = DS.Body,
                                        fontSize = 10.5.sp,
                                        fontFamily = Type.Sans,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(9.dp))
                                            .background(DS.Card.copy(alpha = 0.74f))
                                            .border(1.dp, DS.LineStrong, RoundedCornerShape(9.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                        // Title
                        Text(
                            drama.title,
                            color = DS.Hi,
                            fontSize = 29.sp,
                            fontFamily = Type.Display,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 33.sp,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(8.dp))
                        // Meta row
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (drama.episodes > 0) {
                                Text(
                                    "${drama.episodes} Episode",
                                    color = DS.Green,
                                    fontSize = 12.sp,
                                    fontFamily = Type.Sans,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.width(8.dp))
                            }
                            Text(
                                platformLabel(drama.platform),
                                color = DS.Body,
                                fontSize = 12.sp,
                                fontFamily = Type.Sans
                            )
                            if (drama.views.isNotBlank()) {
                                Spacer(Modifier.width(8.dp))
                                Text("•", color = DS.Faint, fontSize = 12.sp)
                                Spacer(Modifier.width(8.dp))
                                Text(drama.views, color = DS.Muted, fontSize = 12.sp, fontFamily = Type.Sans)
                            }
                        }
                    }
                }
            }

            item {
                Column(Modifier.padding(horizontal = 20.dp)) {
                    // Loading indicator
                    if (state is Load.Loading) {
                        LinearProgressIndicator(
                            color = DS.Green,
                            trackColor = DS.Card2,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .height(2.dp)
                                .clip(RoundedCornerShape(50))
                        )
                    }
                    if (state is Load.Err) {
                        Text(state.message, color = DS.Red, fontSize = 12.5.sp, fontFamily = Type.Sans, modifier = Modifier.padding(vertical = 6.dp))
                    }

                    // Action buttons row
                    Spacer(Modifier.height(16.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Play button — primary
                        Row(
                            Modifier
                                .weight(2f)
                                .height(50.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (state is Load.Ok && resolvingEpisode == 0) DS.Green else DS.Card2)
                                .border(1.dp, if (state is Load.Ok) DS.Green.copy(alpha = 0.42f) else DS.Line, RoundedCornerShape(16.dp))
                                .clickable(enabled = state is Load.Ok && resolvingEpisode == 0) { onPlay(detail, resumeEp) }
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Rounded.PlayArrow,
                                null,
                                tint = if (state is Load.Ok) DS.Ink else DS.Faint,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                when {
                                    isSingleVideo && hist != null -> "Lanjutkan Video"
                                    isSingleVideo -> "Putar Video"
                                    hist != null -> "Lanjut Ep $resumeEp"
                                    else -> "Mulai Nonton"
                                },
                                color = if (state is Load.Ok) DS.Ink else DS.Faint,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.5.sp,
                                fontFamily = Type.Sans
                            )
                        }
                        // Favorite button
                        Box(
                            Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isFav) DS.GreenWash else DS.Card)
                                .border(1.dp, if (isFav) DS.Green.copy(alpha = 0.3f) else DS.Line, RoundedCornerShape(12.dp))
                                .clickable { store.toggleFav(drama); onFavChanged() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if (isFav) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                "Favorit",
                                tint = if (isFav) DS.Green else DS.Body,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        // Share button
                        Box(
                            Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(DS.Card)
                                .border(1.dp, DS.Line, RoundedCornerShape(12.dp))
                                .clickable { onShare(drama) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.Share, "Bagikan", tint = DS.Body, modifier = Modifier.size(19.dp))
                        }
                    }

                    // Resume card
                    if (hist != null) {
                        Spacer(Modifier.height(16.dp))
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(DS.Card.copy(alpha = 0.55f))
                                .clickable { onPlay(detail, resumeEp) }
                                .padding(14.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.History, null, tint = DS.Green, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Lanjutkan menonton", color = DS.Hi, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, fontFamily = Type.Sans, modifier = Modifier.weight(1f))
                                if (!isSingleVideo) Text("Ep $resumeEp", color = DS.Green, fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = Type.Sans)
                            }
                            if (hist.pct > 0) {
                                Spacer(Modifier.height(10.dp))
                                LinearProgressIndicator(
                                    progress = (hist.pct / 100f).coerceIn(0f, 1f),
                                    color = DS.Green,
                                    trackColor = DS.Card2,
                                    modifier = Modifier.fillMaxWidth().height(3.dp).clip(RoundedCornerShape(50))
                                )
                                Spacer(Modifier.height(4.dp))
                                Text("${hist.pct}% sudah ditonton", color = DS.Muted, fontSize = 11.sp, fontFamily = Type.Sans)
                            }
                        }
                    }

                    // Synopsis
                    if (drama.description.isNotBlank()) {
                        Spacer(Modifier.height(24.dp))
                        Text("Sinopsis", color = DS.Hi, fontSize = 17.sp, fontFamily = Type.Sans, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            drama.description,
                            color = DS.Body,
                            fontSize = 13.5.sp,
                            lineHeight = 21.sp,
                            fontFamily = Type.Sans
                        )
                    }

                    // Konten satu video tidak memerlukan daftar episode.
                    if (!isSingleVideo) {
                    Spacer(Modifier.height(24.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Column(Modifier.weight(1f)) {
                            Text("Episode", color = DS.Hi, fontSize = 17.sp, fontFamily = Type.Sans, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(2.dp))
                            Text("$total episode tersedia", color = DS.Muted, fontSize = 12.sp, fontFamily = Type.Sans)
                        }
                    }

                    // Episode range chips
                    Spacer(Modifier.height(14.dp))
                    val rangeSize = 30
                    val rangeCount = ((total + rangeSize - 1) / rangeSize).coerceAtLeast(1)
                    if (rangeCount > 1) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 12.dp)
                        ) {
                            items((0 until rangeCount).toList()) { r ->
                                val st = r * rangeSize + 1
                                val en = min(total, (r + 1) * rangeSize)
                                val selected = detailRange == r
                                Box(
                                    Modifier
                                        .clip(RoundedCornerShape(11.dp))
                                        .background(if (selected) DS.Green else DS.Card)
                                        .border(1.dp, if (selected) DS.Green.copy(alpha = 0.55f) else DS.Line, RoundedCornerShape(11.dp))
                                        .clickable { detailRange = r }
                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        "$st–$en",
                                        color = if (selected) DS.Ink else DS.Body,
                                        fontSize = 12.sp,
                                        fontFamily = Type.Sans,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    // Episode grid
                    val startEp = detailRange * rangeSize + 1
                    val endEp = min(total, startEp + rangeSize - 1)
                    (startEp..endEp).chunked(5).forEach { row ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { ep ->
                                val epInfo = detail.episodes.firstOrNull { it.number == ep } ?: detail.episodes.getOrNull(ep - 1)
                                val locked = epInfo?.locked == true
                                val isResume = hist != null && resumeEp == ep
                                Box(
                                    Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            when {
                                                resolvingEpisode == ep -> DS.Green
                                                locked -> DS.Raise
                                                isResume -> DS.GreenWash
                                                else -> DS.Card
                                            }
                                        )
                                        .border(
                                            1.dp,
                                            if (isResume) DS.Green.copy(alpha = 0.4f) else DS.Line,
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable(enabled = !locked && resolvingEpisode == 0 && state is Load.Ok) { onPlay(detail, ep) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    when {
                                        resolvingEpisode == ep -> Text("…", color = DS.Ink, fontWeight = FontWeight.Bold, fontSize = 13.sp, fontFamily = Type.Sans)
                                        locked -> Icon(Icons.Rounded.Lock, "Terkunci", tint = DS.Faint, modifier = Modifier.size(14.dp))
                                        else -> Text(
                                            ep.toString(),
                                            color = if (isResume) DS.Green else DS.Hi,
                                            fontWeight = if (isResume) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 13.sp,
                                            fontFamily = Type.Sans
                                        )
                                    }
                                }
                            }
                            repeat(5 - row.size) { Spacer(Modifier.weight(1f)) }
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                    }
                }
            }
        }
    }
}

private fun episodeCount(d: Detail): Int = max(d.drama.episodes, d.episodes.size)
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
private object VideoCache {
    private const val MAX_MB = 256L
    @Volatile private var inst: SimpleCache? = null
    fun get(ctx: Context): SimpleCache = inst ?: synchronized(this) {
        inst ?: SimpleCache(ctx.cacheDir.resolve("exo_video_cache"), LeastRecentlyUsedCacheEvictor(MAX_MB * 1024 * 1024)).also { inst = it }
    }
}

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
private fun buildPlayer(ctx: Context, requestHeaders: Map<String, String> = emptyMap()): ExoPlayer {
    val http = DefaultHttpDataSource.Factory()
        .setUserAgent("Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 Chrome/121 Mobile Safari/537.36")
        // Relay seperti gateway DramaBox butuh waktu lebih lama untuk byte pertama
        // (decrypt on-the-fly); timeout dilonggarkan agar tidak keburu divonis error.
        .setAllowCrossProtocolRedirects(true).setConnectTimeoutMs(20_000).setReadTimeoutMs(45_000)
    // Header sensitif dipasang per-request sesuai host tujuan, bukan default
    // untuk semua CDN: Bearer dramahub bocor ke CDN pihak ketiga bisa memicu
    // 400/403 pada URL presigned (S3/GCS menolak dua mekanisme auth sekaligus).
    if (requestHeaders.isNotEmpty()) http.setDefaultRequestProperties(requestHeaders)
    val upstream = ResolvingDataSource.Factory(http) { spec ->
        val scoped = scopedVideoHeaders(spec.uri.host)
        if (scoped.isEmpty()) spec else spec.withAdditionalHeaders(scoped)
    }
    val cache = CacheDataSource.Factory().setCache(VideoCache.get(ctx)).setUpstreamDataSourceFactory(upstream).setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
    return ExoPlayer.Builder(ctx)
        .setAudioAttributes(AudioAttributes.DEFAULT, true)
        .setHandleAudioBecomingNoisy(true)
        .setSeekBackIncrementMs(10_000L)
        .setSeekForwardIncrementMs(10_000L)
        .setLoadControl(DefaultLoadControl.Builder().setBufferDurationsMs(10_000, 30_000, 800, 1_500).build())
        .setRenderersFactory(DefaultRenderersFactory(ctx).setEnableDecoderFallback(true))
        .setTrackSelector(
            DefaultTrackSelector(ctx).apply {
                setParameters(
                    buildUponParameters()
                        .setPreferredVideoMimeTypes(MimeTypes.VIDEO_H264, MimeTypes.VIDEO_H265)
                        .setPreferredTextLanguage("id")
                        .setPreferredTextLanguages("id", "in", "en")
                        .setSelectUndeterminedTextLanguage(true)
                )
            }
        )
        .setMediaSourceFactory(DefaultMediaSourceFactory(cache)).build()
}

private fun playerError(e: PlaybackException): String {
    val r = e.message.orEmpty()
    return when {
        r.contains("403", true) || r.contains("401", true) -> "Akses CDN ditolak atau token video habis. URL akan diminta ulang saat Coba Lagi."
        r.contains("429", true) -> "CDN lagi batas permintaan. Tunggu sebentar lalu Coba Lagi."
        r.contains("video/hevc", true) || r.contains("hvc1", true) -> "Video HEVC tidak didukung di perangkat ini. Coba judul/episode lain."
        r.contains("MediaCodecVideoRenderer", true) -> "Decoder tidak bisa memutar video ini."
        r.contains("Source error", true) -> "Sumber video gagal dibuka. Aplikasi sudah mencoba meminta URL baru; tekan Coba Lagi."
        r.contains("timeout", true) -> "Koneksi timeout. Cek internet atau coba lagi."
        else -> r.ifBlank { "Video belum tersedia" }
    }
}

// ─────────────────────────────────────────────────────────────────
// PLAYER — feed cuplikan & swipe episode
// ─────────────────────────────────────────────────────────────────

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ClipFeedPlayer(items: List<Drama>, repo: DramakuRepository, store: LocalStore, onClose: () -> Unit, onWatchFull: (Detail) -> Unit, onOpenDetail: (Drama) -> Unit) {
    if (items.isEmpty()) return
    val ctx = LocalContext.current
    val act = ctx as? Activity
    val compAct = ctx as? ComponentActivity
    val pager = rememberPagerState(pageCount = { items.size })
    val player = remember { buildPlayer(ctx) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var curDetail by remember { mutableStateOf<Detail?>(null) }
    var uiVis by remember { mutableStateOf(true) }
    var retryKey by remember { mutableIntStateOf(0) }
    var autoRetriedPage by remember { mutableIntStateOf(-1) }
    var playing by remember { mutableStateOf(false) }

    fun stop() { runCatching { player.playWhenReady = false; player.pause(); player.stop(); player.clearMediaItems() } }
    fun close() { stop(); onClose() }
    BackHandler { close() }

    DisposableEffect(Unit) {
        act?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val l = object : Player.Listener {
            override fun onIsPlayingChanged(p: Boolean) {
                playing = p
                if (p) autoRetriedPage = -1
            }
            override fun onPlayerError(e: PlaybackException) {
                val page = pager.currentPage
                val drama = items.getOrNull(page)
                if (drama != null && autoRetriedPage != page) {
                    autoRetriedPage = page
                    repo.invalidateStream(drama, 1)
                    error = null
                    loading = true
                    retryKey++
                } else {
                    loading = false
                    error = playerError(e)
                }
            }
        }
        player.addListener(l)
        onDispose { act?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); player.removeListener(l); stop(); player.release() }
    }

    DisposableEffect(compAct) {
        val lc = compAct?.lifecycle ?: return@DisposableEffect onDispose { }
        val obs = LifecycleEventObserver { _, ev -> if (ev == Lifecycle.Event.ON_PAUSE || ev == Lifecycle.Event.ON_STOP) runCatching { player.pause() } }
        lc.addObserver(obs); onDispose { lc.removeObserver(obs) }
    }

    LaunchedEffect(uiVis, loading, error, pager.currentPage) {
        if (uiVis && !loading && error == null) { delay(2600); uiVis = false }
    }

    LaunchedEffect(pager.currentPage, retryKey) {
        val drama = items.getOrNull(pager.currentPage) ?: return@LaunchedEffect
        uiVis = true; loading = true; error = null; curDetail = null
        val qd = repo.previewDetail(drama); curDetail = qd
        val stream = try { repo.resolveStreamCached(qd, 1, store.dataSaver()) }
        catch (e: CancellationException) { throw e }
        catch (_: Throwable) {
            try { val fd = repo.loadDetailCached(drama); curDetail = fd; repo.resolveStreamCached(fd, 1, store.dataSaver()) }
            catch (e: CancellationException) { throw e }
            catch (t: Throwable) { loading = false; error = t.message ?: "Cuplikan gagal"; player.stop(); return@LaunchedEffect }
        }
        if (stream.url.isBlank()) { loading = false; error = "Cuplikan belum tersedia"; player.stop(); return@LaunchedEffect }
        runCatching { player.stop(); player.clearMediaItems() }
        player.setMediaItem(buildMediaItem(stream)); player.prepare(); player.seekTo(0); player.playWhenReady = true; loading = false
    }

    Box(Modifier.fillMaxSize().background(Color.Black).pointerInput(player, pager.currentPage) {
        detectTapGestures(onTap = { uiVis = !uiVis }, onDoubleTap = { uiVis = true; if (player.isPlaying) player.pause() else player.play() })
    }) {
        VerticalPager(pager, Modifier.fillMaxSize()) { page ->
            val drama = items[page]
            val display = if (page == pager.currentPage) curDetail?.drama ?: drama else drama
            Box(Modifier.fillMaxSize().background(Color.Black)) {
                if (page == pager.currentPage) {
                    AndroidView(factory = { PlayerView(it).apply { useController = false; resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM; this.player = player } }, update = { view -> view.player = player; view.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM }, modifier = Modifier.fillMaxSize())
                } else { AsyncImage(drama.poster, drama.title, Modifier.fillMaxSize(), contentScale = ContentScale.Crop) }
                Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent, DS.Bg.copy(alpha = 0.70f)), startY = 400f)))
                Column(Modifier.align(Alignment.BottomStart).padding(16.dp, 16.dp, 72.dp, 22.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(5.dp).clip(CircleShape).background(DS.Green))
                        Spacer(Modifier.width(7.dp))
                        Text(
                            "${platformLabel(display.platform)} · ${display.episodes.coerceAtLeast(1)} Ep",
                            color = DS.Body, fontSize = 11.5.sp, fontFamily = Type.Sans, fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(display.title, color = DS.Hi, fontSize = 20.sp, lineHeight = 24.sp, fontFamily = Type.Sans, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            Modifier.clip(RoundedCornerShape(50)).background(if (curDetail != null) DS.Cream else DS.Card2)
                                .clickable(enabled = curDetail != null) { curDetail?.let { stop(); onWatchFull(it) } }
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Text("Tonton penuh", color = if (curDetail != null) DS.Ink else DS.Faint, fontWeight = FontWeight.Bold, fontSize = 12.5.sp, fontFamily = Type.Sans)
                        }
                        Box(
                            Modifier.clip(RoundedCornerShape(50)).border(1.dp, DS.LineStrong, RoundedCornerShape(50))
                                .clickable { stop(); onOpenDetail(display) }
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Text("Detail", color = DS.Hi, fontWeight = FontWeight.SemiBold, fontSize = 12.5.sp, fontFamily = Type.Sans)
                        }
                    }
                }
            }
        }

        AnimatedVisibility(uiVis || loading || error != null, Modifier.align(Alignment.TopStart)) {
            Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.clip(CircleShape).background(DS.Bg.copy(alpha = 0.45f))) {
                    GhostIconButton(Icons.Rounded.ArrowBack, "Kembali", ::close)
                }
                Spacer(Modifier.width(10.dp))
                Text("${pager.currentPage + 1} / ${items.size}", color = DS.Hi, fontSize = 12.5.sp, fontFamily = Type.Sans, fontWeight = FontWeight.SemiBold)
            }
        }

        if (loading) {
            Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = DS.Green, strokeWidth = 2.5.dp)
                Spacer(Modifier.height(12.dp))
                Text("Memuat…", color = DS.Hi, fontSize = 13.sp, fontFamily = Type.Sans)
            }
        }

        error?.let { e ->
            PlayerErrorCard(e, Modifier.align(Alignment.Center)) {
                items.getOrNull(pager.currentPage)?.let { repo.invalidateStream(it, 1) }
                retryKey++
            }
        }
    }
}

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun VerticalEpisodePlayer(detail: Detail, startEp: Int, repo: DramakuRepository, store: LocalStore, onClose: () -> Unit) {
    val ctx = LocalContext.current
    val act = ctx as? ComponentActivity
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val total = episodeCount(detail).coerceAtLeast(1)
    val locked = remember(detail) { detail.episodes.filter { it.locked }.map { it.number }.toSet() }
    val pager = rememberPagerState(initialPage = (startEp - 1).coerceIn(0, total - 1), pageCount = { total })
    val player = remember { buildPlayer(ctx) }
    val retryBudget = remember { EpisodeRetryBudget() }
    var loadedEpisode by remember { mutableIntStateOf(0) }
    var requestedEpisode by remember { mutableIntStateOf(0) }
    var retryKey by remember { mutableIntStateOf(0) }
    var resumeOverride by remember { mutableStateOf<Pair<Int, Long>?>(null) }
    var loading by remember { mutableStateOf(true) }
    var buffering by remember { mutableStateOf(false) }
    var firstFrame by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var playing by remember { mutableStateOf(false) }
    var wantsToPlay by remember { mutableStateOf(true) }
    var foreground by remember { mutableStateOf(act?.lifecycle?.currentState?.isAtLeast(Lifecycle.State.RESUMED) ?: true) }
    var panel by remember { mutableStateOf<PlayerPanel?>(null) }
    var immersive by remember { mutableStateOf(false) }
    var chromeVisible by remember { mutableStateOf(true) }
    var chromeTick by remember { mutableIntStateOf(0) }
    var fitContain by remember { mutableStateOf(store.fitContain()) }
    var playbackSpeed by remember { mutableFloatStateOf(store.playbackSpeed()) }
    var speedHold by remember { mutableStateOf(false) }
    var collected by remember { mutableStateOf(store.isFav(detail.drama.id, detail.drama.platform)) }
    var liked by remember { mutableStateOf(store.isLiked(detail.drama.id, detail.drama.platform)) }
    var showLike by remember { mutableStateOf(false) }
    var notice by remember { mutableStateOf<String?>(null) }
    var showSwipeHint by remember { mutableStateOf(total > 1 && !store.playerHintShown()) }
    var curMs by remember { mutableLongStateOf(0L) }
    var durMs by remember { mutableLongStateOf(0L) }
    var bufferedMs by remember { mutableLongStateOf(0L) }
    var isSeeking by remember { mutableStateOf(false) }
    var streamHasSub by remember { mutableStateOf(false) }
    var hasSub by remember { mutableStateOf(false) }
    var embedUrl by remember { mutableStateOf("") }
    var portraitVideo by remember { mutableStateOf(detail.drama.platform != "anichin") }
    var subOn by remember { mutableStateOf(store.subtitleOn()) }
    var dataSaver by remember { mutableStateOf(store.dataSaver()) }
    var autoNext by remember { mutableStateOf(store.autoNext()) }

    fun mediaId(ep: Int) = ProgressKeys.episodePrefix(detail.drama.platform, detail.drama.id, ep)
    fun saveProgress() {
        // The media item, not the pager's transient currentPage, owns progress.
        // Never replace saved progress with 0 while a resolver is still loading.
        val ep = loadedEpisode
        if (ep <= 0 || player.currentMediaItem?.mediaId != mediaId(ep) || player.duration <= 0L) return
        store.updateProgress(detail.drama.id, detail.drama.platform, ep, player.currentPosition.coerceAtLeast(0L), player.duration)
    }
    fun closePlayer() { saveProgress(); player.pause(); onClose() }
    fun reload(manual: Boolean) {
        val ep = pager.settledPage + 1
        saveProgress()
        if (loadedEpisode == ep) resumeOverride = ep to player.currentPosition.coerceAtLeast(0L)
        if (manual) retryBudget.reset(ep)
        repo.invalidateStream(detail.drama, ep)
        error = null; loading = true; retryKey++
    }
    fun togglePlayback() {
        if (loadedEpisode <= 0 || error != null) return
        if (player.playbackState == Player.STATE_ENDED) { player.seekTo(0L); wantsToPlay = true }
        else wantsToPlay = !wantsToPlay
    }
    fun likeVideo(toggle: Boolean) {
        if (toggle || !liked) { store.toggleLike(detail.drama); liked = !liked }
        showLike = liked
    }
    fun revealChrome() { chromeVisible = true; chromeTick++ }
    fun seekBy(delta: Long) {
        if (loadedEpisode <= 0 || durMs <= 0L) return
        curMs = PlaybackPolicy.seekPosition(player.currentPosition, delta, durMs)
        player.seekTo(curMs); saveProgress()
        notice = if (delta > 0L) "+10 detik" else "−10 detik"
    }

    BackHandler {
        when {
            panel != null -> panel = null
            immersive -> immersive = false
            else -> closePlayer()
        }
    }

    DisposableEffect(act) {
        val window = act?.window
        val previousOrientation = act?.requestedOrientation
        val keepScreenOn = window?.attributes?.flags?.and(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) != 0
        val insets = window?.decorView?.let { ViewCompat.getRootWindowInsets(it) }
        val visibleBars = (if (insets?.isVisible(WindowInsetsCompat.Type.statusBars()) != false) WindowInsetsCompat.Type.statusBars() else 0) or
            (if (insets?.isVisible(WindowInsetsCompat.Type.navigationBars()) != false) WindowInsetsCompat.Type.navigationBars() else 0)
        act?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        window?.let {
            it.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            WindowCompat.setDecorFitsSystemWindows(it, false)
            WindowCompat.getInsetsController(it, it.decorView).apply {
                systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                hide(WindowInsetsCompat.Type.systemBars())
            }
        }
        onDispose {
            window?.let {
                if (!keepScreenOn) it.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                WindowCompat.setDecorFitsSystemWindows(it, true)
                WindowCompat.getInsetsController(it, it.decorView).show(visibleBars)
            }
            previousOrientation?.let { act?.requestedOrientation = it }
        }
    }

    DisposableEffect(player, act) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) { playing = isPlaying }
            override fun onRenderedFirstFrame() { if (loadedEpisode > 0) firstFrame = true }
            override fun onVideoSizeChanged(size: VideoSize) { if (size.width > 0 && size.height > 0) portraitVideo = size.height >= size.width }
            override fun onTracksChanged(tracks: Tracks) { hasSub = streamHasSub || tracks.groups.any { it.type == C.TRACK_TYPE_TEXT } }
            override fun onPlayWhenReadyChanged(ready: Boolean, reason: Int) {
                if (!ready && (reason == Player.PLAY_WHEN_READY_CHANGE_REASON_AUDIO_BECOMING_NOISY || reason == Player.PLAY_WHEN_READY_CHANGE_REASON_AUDIO_FOCUS_LOSS)) wantsToPlay = false
            }
            override fun onPlayerError(exception: PlaybackException) {
                buffering = false
                if (loadedEpisode > 0 && loadedEpisode == pager.settledPage + 1 && retryBudget.tryConsume(loadedEpisode)) reload(manual = false)
                else { loading = false; error = playerError(exception) }
            }
            override fun onPlaybackStateChanged(state: Int) {
                buffering = state == Player.STATE_BUFFERING
                if (state == Player.STATE_ENDED && loadedEpisode == pager.settledPage + 1) {
                    saveProgress()
                    val next = if (autoNext) PlaybackPolicy.nextEpisode(loadedEpisode, total, locked) else null
                    if (next != null) scope.launch { pager.animateScrollToPage(next - 1) }
                    else wantsToPlay = false
                }
            }
        }
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> foreground = true
                Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP -> { saveProgress(); foreground = false; speedHold = false; player.pause() }
                else -> Unit
            }
        }
        player.addListener(listener)
        act?.lifecycle?.addObserver(observer)
        onDispose {
            saveProgress()
            // Sesi nonton selesai: jadwalkan ulang pengingat bila fiturnya aktif.
            if (store.resumeReminder()) runCatching { ResumeReminder.schedule(ctx) }
            act?.lifecycle?.removeObserver(observer)
            player.removeListener(listener)
            player.stop(); player.clearMediaItems(); player.release()
        }
    }

    LaunchedEffect(wantsToPlay, foreground, panel, loadedEpisode, isSeeking) {
        player.playWhenReady = wantsToPlay && foreground && panel == null && loadedEpisode > 0 && !isSeeking
    }
    LaunchedEffect(speedHold, playbackSpeed, foreground, panel) {
        player.setPlaybackSpeed(if (speedHold && foreground && panel == null) 2f else playbackSpeed)
    }
    LaunchedEffect(subOn, dataSaver, portraitVideo) {
        player.trackSelectionParameters = player.trackSelectionParameters.buildUpon()
            .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, !subOn)
            .setPreferredTextLanguages("id", "in", "en")
            .setSelectUndeterminedTextLanguage(true)
            .apply {
                if (dataSaver) {
                    // Short dramas are portrait: 854x480 would unnecessarily reject 480x854.
                    if (portraitVideo) setMaxVideoSize(480, 854) else setMaxVideoSize(854, 480)
                    setMaxVideoBitrate(1_200_000)
                } else { clearVideoSizeConstraints(); setMaxVideoBitrate(Int.MAX_VALUE) }
            }.build()
    }
    // Chrome auto-hides a few seconds into playback; pausing, seeking, errors, or any panel brings it back.
    LaunchedEffect(chromeVisible, chromeTick, playing, panel, isSeeking, error) {
        if (!playing || panel != null || isSeeking || error != null) { chromeVisible = true; return@LaunchedEffect }
        if (!chromeVisible) return@LaunchedEffect
        delay(3_500L)
        chromeVisible = false
    }
    LaunchedEffect(notice) { if (notice != null) { delay(1_400); notice = null } }
    LaunchedEffect(showLike) { if (showLike) { delay(900); showLike = false } }
    LaunchedEffect(showSwipeHint) { if (showSwipeHint) { delay(5_000); store.setPlayerHintShown(); showSwipeHint = false } }
    LaunchedEffect(pager.settledPage) {
        if (showSwipeHint && pager.settledPage != (startEp - 1).coerceIn(0, total - 1)) { store.setPlayerHintShown(); showSwipeHint = false }
    }

    LaunchedEffect(pager.settledPage, retryKey) {
        val ep = pager.settledPage + 1
        saveProgress()
        // Stop BEFORE resolving the next URL. Fast swipes cancel this effect;
        // neither an old request nor old audio can take over the new episode.
        loadedEpisode = 0
        player.stop(); player.clearMediaItems()
        if (requestedEpisode != ep) wantsToPlay = true
        requestedEpisode = ep; retryBudget.beginEpisode(ep)
        firstFrame = false; embedUrl = ""; streamHasSub = false; hasSub = false
        speedHold = false; isSeeking = false; chromeVisible = true; curMs = 0L; durMs = 0L; bufferedMs = 0L
        loading = true; buffering = false; error = null
        if (ep in locked) { loading = false; error = "Episode ini masih terkunci di sumber. Pilih episode lain."; return@LaunchedEffect }
        val start = resumeOverride?.takeIf { it.first == ep }?.second ?: PlaybackPolicy.resumePosition(
            store.progressMs(detail.drama.id, detail.drama.platform, ep), store.progressDurationMs(detail.drama.id, detail.drama.platform, ep)
        )
        resumeOverride = null
        try {
            val stream = repo.resolveStreamCached(detail, ep, dataSaver)
            currentCoroutineContext().ensureActive()
            if (stream.url.isBlank()) throw IllegalStateException("Video belum tersedia")
            if (PlaybackPolicy.isEmbeddedStream(detail.drama.platform, stream.url)) {
                embedUrl = stream.url
                store.saveHistory(detail.drama, ep)
            } else {
                streamHasSub = stream.subtitle.isNotBlank(); hasSub = streamHasSub
                player.setMediaItem(buildMediaItem(stream).buildUpon().setMediaId(mediaId(ep)).build(), start)
                loadedEpisode = ep; curMs = start
                store.saveHistory(detail.drama, ep)
                player.setPlaybackSpeed(playbackSpeed)
                player.prepare()
                player.playWhenReady = wantsToPlay && foreground && panel == null
                if (start > 3_000L) notice = "Dilanjutkan dari ${clockLabel(start)}"
            }
            loading = false
        } catch (cancelled: CancellationException) { throw cancelled }
        catch (failure: Exception) { loading = false; buffering = false; error = failure.message ?: "Video belum tersedia" }
    }

    LaunchedEffect(loadedEpisode, dataSaver) {
        val next = PlaybackPolicy.nextEpisode(loadedEpisode, total, locked) ?: return@LaunchedEffect
        try { repo.resolveStreamCached(detail, next, dataSaver) }
        catch (cancelled: CancellationException) { throw cancelled }
        catch (_: Exception) { /* URL-only prefetch is optional; never fail current playback. */ }
    }
    LaunchedEffect(player) {
        var lastSave = 0L
        while (true) {
            if (loadedEpisode > 0) {
                durMs = player.duration.takeIf { it > 0L } ?: 0L
                bufferedMs = player.bufferedPosition.coerceAtLeast(0L)
                if (!isSeeking) curMs = player.currentPosition.coerceAtLeast(0L)
                val now = android.os.SystemClock.elapsedRealtime()
                if (now - lastSave >= 5_000L) { saveProgress(); lastSave = now }
            }
            delay(250L)
        }
    }

    val ui = EpisodePlayerUiState(
        title = detail.drama.title, poster = detail.drama.poster, platform = platformLabel(detail.drama.platform),
        views = detail.drama.views, episode = pager.settledPage + 1, total = total, description = detail.drama.description,
        playing = playing, loading = loading, buffering = buffering, failed = error != null, embedded = embedUrl.isNotBlank(),
        liked = liked, collected = collected, positionMs = curMs, durationMs = durMs, bufferedMs = bufferedMs,
        seeking = isSeeking, speed = playbackSpeed, speedHeld = speedHold, immersive = immersive, chromeVisible = chromeVisible,
        dataSaver = dataSaver, subtitleEnabled = subOn, hasSubtitles = hasSub, fitContain = fitContain,
        autoNext = autoNext, notice = notice, showLike = showLike
    )
    val action: (PlayerAction) -> Unit = { selected ->
        revealChrome() // Interacting with the chrome restarts the auto-hide countdown.
        when (selected) {
            PlayerAction.Back -> closePlayer()
            PlayerAction.PlayPause -> togglePlayback()
            PlayerAction.Like -> likeVideo(toggle = true)
            PlayerAction.Collect -> { store.toggleFav(detail.drama); collected = !collected; notice = if (collected) "Ditambahkan ke koleksi" else "Dihapus dari koleksi" }
            PlayerAction.Share -> shareDrama(ctx, detail.drama)
            PlayerAction.Episodes -> panel = PlayerPanel.Episodes
            PlayerAction.More -> panel = PlayerPanel.More
            PlayerAction.Immersive -> immersive = !immersive
            PlayerAction.Speed -> panel = PlayerPanel.Speed
            PlayerAction.Quality -> panel = PlayerPanel.Quality
            PlayerAction.Subtitles -> panel = PlayerPanel.Subtitles
            PlayerAction.Details -> panel = PlayerPanel.Details
            PlayerAction.Fit -> { fitContain = !fitContain; store.setFitContain(fitContain) }
            PlayerAction.AutoNext -> { autoNext = !autoNext; store.setAutoNext(autoNext) }
            PlayerAction.Reload -> { panel = null; reload(manual = true) }
        }
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        VerticalPager(pager, Modifier.fillMaxSize(), userScrollEnabled = !isSeeking && !speedHold && panel == null) { page ->
            Box(Modifier.fillMaxSize().background(Color.Black)) {
                if (page == pager.settledPage) {
                    if (embedUrl.isNotBlank()) {
                        EmbeddedEpisodeVideo(embedUrl, foreground, Modifier.fillMaxSize()) { error = it }
                    } else {
                        if (loadedEpisode == page + 1) EpisodeVideoSurface(player, fitContain, immersive, Modifier.fillMaxSize())
                        if (!firstFrame || loadedEpisode != page + 1) AsyncImage(detail.drama.poster, null, Modifier.fillMaxSize().alpha(0.55f), contentScale = ContentScale.Crop)
                        Box(Modifier.fillMaxSize().episodePlayerGestures(
                            episode = page,
                            onTap = { if (!immersive && !chromeVisible) revealChrome() else togglePlayback() },
                            onDoubleTap = { offset, width ->
                                when {
                                    offset.x < width * 0.35f -> seekBy(-10_000L)
                                    offset.x > width * 0.65f -> seekBy(10_000L)
                                    else -> { haptics.performHapticFeedback(HapticFeedbackType.LongPress); likeVideo(toggle = false) }
                                }
                            },
                            onHold = { held ->
                                speedHold = held && loadedEpisode > 0 && error == null
                                if (speedHold) haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                        ))
                    }
                } else AsyncImage(detail.drama.poster, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            }
        }
        // One-time onboarding: a bouncing chevron teaching the vertical swipe, dismissed on first swipe or after 5s.
        AnimatedVisibility(
            showSwipeHint && !immersive && panel == null && error == null,
            Modifier.align(Alignment.Center),
            enter = fadeIn(), exit = fadeOut()
        ) {
            val hint = rememberInfiniteTransition(label = "swipeHint")
            val dy by hint.animateFloat(0f, -14f, infiniteRepeatable(tween(650, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "swipeHintDy")
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Rounded.KeyboardArrowUp, null, tint = Color.White, modifier = Modifier.size(36.dp).offset(y = dy.dp))
                Spacer(Modifier.height(2.dp))
                Surface(color = Color.Black.copy(alpha = 0.60f), shape = RoundedCornerShape(30.dp), contentColor = Color.White) {
                    Text(
                        "Geser ke atas untuk episode berikutnya",
                        Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        fontSize = 12.sp, fontFamily = Type.Sans, fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
        EpisodePlayerChrome(
            ui, action,
            onSeekStart = { isSeeking = true },
            onSeekChange = { curMs = it },
            onSeekEnd = { player.seekTo(curMs); isSeeking = false; saveProgress() },
            onSeekCancel = { curMs = player.currentPosition.coerceAtLeast(0L); isSeeking = false }
        )
        error?.let { message ->
            PlayerErrorCard(message, Modifier.align(Alignment.Center), actionLabel = if (ui.episode in locked) "Pilih episode" else "Coba lagi") {
                if (ui.episode in locked) panel = PlayerPanel.Episodes else reload(manual = true)
            }
        }
        if (panel == PlayerPanel.Episodes) {
            val choices = remember(panel, ui.episode) {
                (1..total).map { ep ->
                    val duration = store.progressDurationMs(detail.drama.id, detail.drama.platform, ep)
                    val position = store.progressMs(detail.drama.id, detail.drama.platform, ep)
                    EpisodeChoice(ep, ep in locked, if (duration > 0L) (position.toFloat() / duration).coerceIn(0f, 1f) else 0f)
                }
            }
            EpisodeSelectionSheet(ui, choices, onDismiss = { panel = null }) { ep ->
                panel = null; wantsToPlay = true
                if (ep != pager.settledPage + 1) scope.launch { pager.scrollToPage(ep - 1) }
            }
        } else panel?.let { openPanel ->
            PlayerSettingsSheet(
                openPanel, ui, onDismiss = { panel = null }, onAction = action,
                onSpeed = { playbackSpeed = it; store.setPlaybackSpeed(it); panel = null },
                onDataSaver = { selected ->
                    panel = null
                    if (selected != dataSaver) { dataSaver = selected; store.setDataSaver(selected); reload(manual = true) }
                },
                onSubtitles = { subOn = it; store.setSubtitleOn(it); panel = null }
            )
        }
    }
}

@Composable
private fun PlayerErrorCard(message: String, modifier: Modifier = Modifier, actionLabel: String = "Coba lagi", onRetry: () -> Unit) {
    Column(
        modifier
            .padding(28.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(DS.SheetBg)
            .padding(22.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(DS.RedWash),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.Warning, null, tint = DS.Red, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.height(12.dp))
        Text(
            message,
            color = DS.Hi,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            fontFamily = Type.Sans,
            textAlign = TextAlign.Center,
            lineHeight = 19.sp
        )
        Spacer(Modifier.height(14.dp))
        Box(
            Modifier
                .clip(RoundedCornerShape(50))
                .background(DS.Green)
                .clickable(onClick = onRetry)
                .padding(horizontal = 22.dp, vertical = 10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Refresh, null, tint = DS.Ink, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(6.dp))
                Text(actionLabel, color = DS.Ink, fontWeight = FontWeight.Bold, fontSize = 12.5.sp, fontFamily = Type.Sans)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// HELPERS
// ─────────────────────────────────────────────────────────────────

private fun buildMediaItem(s: StreamResult): MediaItem {
    val url = cleanUrl(s.url)
    val b = MediaItem.Builder().setUri(Uri.parse(url))
    val lower = url.lowercase()
    when {
        // Playlist HLS (harus dicek sebelum "dash" karena URL seperti Shorten punya path "/dash/hls-.../main.m3u8")
        lower.contains(".m3u8") || lower.contains("m3u8") -> b.setMimeType(MimeTypes.APPLICATION_M3U8)
        // DramaBox (proxy dramahub.be/dramaboxbaru) membalas mp4 ber-token:
        // ".../api/stream?...&seg=video.mp4&k=..." — jangan dikira playlist.
        // NetShort (video.netshort.com) jalur URL-nya tanpa ekstensi, tapi query
        // auth_key-nya membawa mime_type=video_mp4.
        lower.contains(".mp4") || lower.contains("mime_type=video_mp4") -> b.setMimeType(MimeTypes.APPLICATION_MP4)
        lower.contains(".mpd") || lower.contains("/stream/mpd") -> b.setMimeType(MimeTypes.APPLICATION_MPD)
    }
    val subtitle = cleanUrl(s.subtitle)
    if (subtitle.isNotBlank()) {
        val path = subtitle.substringBefore('?').substringBefore('#').lowercase()
        val lowerSub = subtitle.lowercase()
        val mime = when {
            s.subtitleMime.isNotBlank() -> s.subtitleMime
            path.endsWith(".vtt") || path.endsWith(".webvtt") ||
                lowerSub.contains(".vtt") || lowerSub.contains(".webvtt") ||
                lowerSub.contains("format=webvtt") || lowerSub.contains("netshort.com") -> MimeTypes.TEXT_VTT
            path.endsWith(".ass") || path.endsWith(".ssa") || lowerSub.contains(".ass") || lowerSub.contains(".ssa") -> MimeTypes.TEXT_SSA  // ASS/SSA
            path.endsWith(".srt") || lowerSub.contains(".srt") -> MimeTypes.APPLICATION_SUBRIP
            else -> MimeTypes.TEXT_VTT
        }
        b.setSubtitleConfigurations(
            listOf(
                MediaItem.SubtitleConfiguration.Builder(Uri.parse(subtitle))
                    .setMimeType(mime)
                    .setLanguage("id")
                    .setSelectionFlags(C.SELECTION_FLAG_DEFAULT or C.SELECTION_FLAG_FORCED)
                    .build()
            )
        )
    }
    return b.build()
}

private fun formatMs(ms: Long): String {
    val s = (ms / 1000).coerceAtLeast(0)
    val h = s / 3600; val m = (s % 3600) / 60; val sec = s % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, sec) else "%d:%02d".format(m, sec)
}

private fun shareDrama(ctx: Context, d: Drama) {
    ctx.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, "${d.title}\n${platformLabel(d.platform)} · Dramaku") }, "Bagikan"))
}

private fun clockLabel(ms: Long): String {
    val seconds = (ms / 1_000).coerceAtLeast(0)
    return if (seconds >= 3_600) "%d:%02d:%02d".format(seconds / 3_600, seconds / 60 % 60, seconds % 60) else "%d:%02d".format(seconds / 60, seconds % 60)
}

private fun Context.isNetworkAvailable(): Boolean {
    val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return true
    return cm.getNetworkCapabilities(cm.activeNetwork)?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
}

// ─────────────────────────────────────────────────────────────────
// REPOSITORY — same API logic, with streamv2 fix
// ─────────────────────────────────────────────────────────────────

private class DramakuRepository {
    private val dispatcher = Dispatcher().apply { maxRequests = 32; maxRequestsPerHost = 16 }
    private val client = OkHttpClient.Builder().dispatcher(dispatcher).connectTimeout(15, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).callTimeout(45, TimeUnit.SECONDS).retryOnConnectionFailure(true).build()
    private val detailCache = ConcurrentHashMap<String, CachedDetail>()
    private val streamCache = ConcurrentHashMap<String, CachedStream>()
    private val forcedDetailRefresh = ConcurrentHashMap.newKeySet<String>()

    fun previewDetail(input: Drama): Detail {
        val now = System.currentTimeMillis()
        detailCache[detailKey(input)]?.takeIf { it.expiresAtMs > now }?.let { return it.detail }
        val t = input.episodes.coerceAtLeast(1)
        return Detail(input.copy(episodes = t), (1..t).map { EpisodeInfo(it) })
    }

    suspend fun loadDetailCached(input: Drama): Detail {
        val k = detailKey(input)
        val now = System.currentTimeMillis()
        detailCache[k]?.takeIf { it.expiresAtMs > now }?.let { return it.detail }
        return refreshDetail(input)
    }

    private suspend fun refreshDetail(input: Drama): Detail {
        val detail = loadDetail(input)
        detailCache[detailKey(input)] = CachedDetail(detail, System.currentTimeMillis() + DETAIL_CACHE_TTL_MS)
        return detail
    }

    // URL bertanda tangan dapat kedaluwarsa dalam 30–70 menit. Cache stream
    // tetap pendek dan Detail yang membawa URL langsung ikut punya TTL. Saat
    // URL mendekati expiry (atau pengguna menekan Coba Lagi), detail diambil
    // ulang sebelum resolver memilih link video.
    suspend fun resolveStreamCached(d: Detail, ep: Int, ds: Boolean): StreamResult {
        val k = streamKey(d.drama, ep, ds)
        val forceKey = forceRefreshKey(d.drama, ep)
        val now = System.currentTimeMillis()
        val forceRefresh = forcedDetailRefresh.remove(forceKey)
        if (!forceRefresh) {
            streamCache[k]?.takeIf { it.expiresAtMs > now }?.let { return it.result }
        }

        val stored = d.episodes.firstOrNull { it.number == ep }?.streaming
            ?: d.episodes.getOrNull(ep - 1)?.streaming.orEmpty()
        val effectiveDetail = if (forceRefresh || signedUrlNeedsRefresh(stored, now)) {
            runCatching { refreshDetail(d.drama) }.getOrElse { d }
        } else d

        var result = resolveStream(effectiveDetail, ep, ds)
        if (isSignedVideoUrl(result.url) && !streamIsReachable(result.url)) {
            // URL baru dari resolver pun kadang berasal dari cache upstream yang
            // sudah basi. Paksa satu refresh detail + resolver, lalu validasi lagi.
            detailCache.remove(detailKey(d.drama))
            val freshDetail = runCatching { refreshDetail(d.drama) }.getOrElse { effectiveDetail }
            result = resolveStream(freshDetail, ep, ds)
            if (isSignedVideoUrl(result.url) && !streamIsReachable(result.url)) {
                error("Link video dari sumber sudah kedaluwarsa. Coba lagi sebentar.")
            }
        }
        if (result.url.isNotBlank()) {
            streamCache[k] = CachedStream(result, streamExpiryMs(result.url, now))
        }
        return result
    }

    fun invalidateStream(d: Drama, ep: Int) {
        streamCache.remove(streamKey(d, ep, true))
        streamCache.remove(streamKey(d, ep, false))
        detailCache.remove(detailKey(d))
        forcedDetailRefresh.add(forceRefreshKey(d, ep))
    }

    private fun detailKey(d: Drama) = "${d.platform}|${d.id}"
    private fun streamKey(d: Drama, ep: Int, ds: Boolean) = "${d.platform}|${d.id}|$ep|${if (ds) "480" else "720"}"
    private fun forceRefreshKey(d: Drama, ep: Int) = "${d.platform}|${d.id}|$ep"

    private fun streamExpiryMs(url: String, now: Long): Long {
        val signedExpiry = signedUrlExpiryMs(url)
        val safeExpiry = signedExpiry?.minus(60_000L)?.coerceAtLeast(now + 5_000L)
        return min(now + STREAM_CACHE_TTL_MS, safeExpiry ?: Long.MAX_VALUE)
    }

    private fun isSignedVideoUrl(url: String): Boolean {
        val lower = url.lowercase()
        return listOf(
            "auth_key=", "expires=", "x-amz-", "signature=", "policy=",
            "token=", "hdnts=", "wssecret=", "x-oss-signature", "x-goog-signature", "sign="
        ).any { lower.contains(it) }
    }

    private suspend fun streamIsReachable(url: String): Boolean = withContext(Dispatchers.IO) {
        val clean = cleanUrl(url)
        if (!clean.startsWith("http")) return@withContext false
        runCatching {
            // Probe memakai header yang sama dengan pemutar: salah header di sini
            // membuat link sehat divonis kedaluwarsa (false negative).
            val builder = Request.Builder()
                .url(clean)
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 Chrome/121 Mobile Safari/537.36")
                .header("Accept", "*/*")
                .header("Range", "bytes=0-1")
            scopedVideoHeaders(runCatching { java.net.URI(clean).host }.getOrNull()).forEach { (k, v) -> builder.header(k, v) }
            client.newCall(builder.build()).execute().use { response -> response.isSuccessful }
        }.getOrDefault(false)
    }

    private fun signedUrlNeedsRefresh(stored: String, now: Long): Boolean {
        if (stored.isBlank()) return false
        val videoUrl = stored.substringBefore("##").substringAfterLast("|||").trim()
        val expiry = signedUrlExpiryMs(videoUrl)
        if (expiry != null) return expiry <= now + SIGNED_URL_SAFETY_MS
        return isSignedVideoUrl(videoUrl)
    }

    private fun signedUrlExpiryMs(url: String): Long? {
        if (url.isBlank()) return null
        val epoch = Regex("(?:auth_key=|[?&/]expires?=|[?&]exp=)([0-9]{10,13})", RegexOption.IGNORE_CASE)
            .find(url)?.groupValues?.getOrNull(1)?.toLongOrNull()
        if (epoch != null) return if (epoch > 9_999_999_999L) epoch else epoch * 1000L

        val dateRaw = Regex("[?&]X-Amz-Date=([0-9]{8}T[0-9]{6}Z)", RegexOption.IGNORE_CASE)
            .find(url)?.groupValues?.getOrNull(1)
        val ttlSeconds = Regex("[?&]X-Amz-Expires=([0-9]+)", RegexOption.IGNORE_CASE)
            .find(url)?.groupValues?.getOrNull(1)?.toLongOrNull()
        if (dateRaw != null && ttlSeconds != null) {
            val format = java.text.SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", java.util.Locale.US).apply {
                timeZone = java.util.TimeZone.getTimeZone("UTC")
            }
            val issuedAt = runCatching { format.parse(dateRaw)?.time }.getOrNull()
            if (issuedAt != null) return issuedAt + ttlSeconds * 1000L
        }
        return null
    }

    suspend fun loadHome(p: String) = loadHomePage(p, 1)

    private suspend fun fetchHome(url: String): JSONObject = getJson(url)

    suspend fun loadHomePage(p: String, page: Int): HomeBundle = coroutineScope {
        if (p == "stardusttv") return@coroutineScope loadStardustHome(page)
        if (p == "netshort") return@coroutineScope loadNetshortHome(page)
        if (p == "reelshort") return@coroutineScope loadReelshortHome(page)
        if (p == "anichin") return@coroutineScope loadAnichinHome(page)
        if (ExtraPlatforms.isExtraPlatform(p)) return@coroutineScope loadExtraPlatformHome(p, page)
        val req = homePageRequest(p, page)
        val json = try { fetchHome(req.url) } catch (e: CancellationException) { throw e } catch (_: Throwable) { null }
        val items = dedupe(json?.let { flat(it.dataOrSelf(), p) }.orEmpty())
        var rec = emptyList<Drama>(); var pop = emptyList<Drama>(); var nw = emptyList<Drama>()
        when (req.section) { HomeSection.Popular -> pop = items; HomeSection.Newest -> nw = items; HomeSection.Recommended -> rec = items }
        val more = req.hasMore
        if (rec.isEmpty() && pop.isEmpty() && nw.isEmpty() && req.virtualPage == 1) error("Sumber ini sedang tidak tersedia. Coba rak lain dulu ya.")
        HomeBundle(rec, pop, nw, req.virtualPage, more)
    }

    private suspend fun loadExtraPlatformHome(p: String, page: Int): HomeBundle = coroutineScope {
        val vp = page.coerceAtLeast(1)
        val urls = homeUrls(p, vp)
        val distinctUrls = urls.distinct()
        val fetchedMap = distinctUrls.map { u ->
            u to async {
                try { fetchHome(u).let { flat(it.dataOrSelf(), p) } }
                catch (e: CancellationException) { throw e }
                catch (_: Throwable) { emptyList() }
            }
        }.associate { (u, d) -> u to d.await() }
        var rec = dedupe(fetchedMap[urls.getOrElse(0) { "" }].orEmpty())
        var pop = dedupe(fetchedMap[urls.getOrElse(1) { "" }].orEmpty())
        var nw = dedupe(fetchedMap[urls.getOrElse(2) { "" }].orEmpty())
        val all = dedupe(rec + pop + nw)
        if (all.isEmpty() && vp == 1) error("Sumber ini sedang tidak tersedia. Coba rak lain dulu ya.")
        if (pop.isEmpty()) pop = all
        if (rec.isEmpty()) rec = all
        if (nw.isEmpty()) nw = all.reversed()
        val more = vp < 3 && all.size >= 8
        HomeBundle(rec, pop, nw, vp, more)
    }

    /** StardustTV: homepage menyatukan rak utama, sehingga tidak perlu
     * menebak-nebak endpoint upstream. */
    private suspend fun loadStardustHome(page: Int): HomeBundle = coroutineScope {
        val json = getJson("${apiBase("stardusttv")}/homepage?lang=id")
        val items = dedupe(flat(json.dataOrSelf(), "stardusttv"))
        val pageItems = if (page <= 1) items else emptyList()
        if (page == 1 && pageItems.isEmpty()) error("Stardust TV sedang tidak tersedia")
        HomeBundle(pageItems, pageItems, pageItems, page, false)
    }

    /**
     * Beranda NetShort: tiga slot HomeBundle diisi endpoint berbeda supaya
     * layar pertama variatif — explore (Jelajahi), feed (rekomendasi), new
     * (rilisan terbaru). Halaman berikutnya mengambil /feed dan /explore
     * dengan nomor halaman naik (dua-duanya benar-benar berganti isi), jadi
     * infinite scroll tetap dapat judul baru.
     */
    private suspend fun loadNetshortHome(page: Int): HomeBundle = coroutineScope {
        val vp = page.coerceAtLeast(1)
        val slots = listOf(
            "explore/$vp?lang=id_ID",
            "feed/$vp?lang=id_ID",
            "new/$vp?lang=id_ID"
        )
        val fetched = slots.map { path ->
            async { netshortJson(path)?.let { flat(it.dataOrSelf(), "netshort") }.orEmpty() }
        }.awaitAll()
        val pop = dedupe(fetched.getOrElse(0) { emptyList() })
        val rec = dedupe(fetched.getOrElse(1) { emptyList() })
        val nw = dedupe(fetched.getOrElse(2) { emptyList() })
        // Tiap halaman menambah ~10 judul feed + belasan judul explore baru
        // (dua-duanya sudah dites berganti isi di halaman 2 dan 3), jadi
        // dibatasi 3 halaman saja supaya tidak menarik katalog tanpa ujung.
        val more = nw.size >= 20 && vp < 3
        if (pop.isEmpty() && rec.isEmpty() && nw.isEmpty() && vp == 1) error("Sumber ini sedang tidak tersedia. Coba rak lain dulu ya.")
        return@coroutineScope HomeBundle(rec, pop, nw, vp, more)
    }

    /**
     * Beranda ReelShort: foryou = recommended, new = newest, completed = popular.
     * Plus romance & drama sebagai extra untuk genre rows.
     * Endpoint: /foryou, /new, /completed, /romance, /drama
     */
    private suspend fun loadReelshortHome(page: Int): HomeBundle = coroutineScope {
        val vp = page.coerceAtLeast(1)
        // Untuk page 1: ambil 3 endpoint utama
        // Untuk page >1: coba pakai feed dengan tab_id? Tapi API reelshort tidak support page param,
        // jadi untuk infinite scroll kita fallback ke feed tab populer/new
        val slots = if (vp == 1) {
            listOf(
                ReelshortCatalog.FORYOU_PATH,
                ReelshortCatalog.NEW_PATH,
                ReelshortCatalog.COMPLETED_PATH
            )
        } else {
            // Page >1: ambil dari tab_list kalau ada, atau ulang foryou dengan page param (dicoba)
            listOf(
                "foryou?lang=in&page=$vp",
                "new?lang=in&page=$vp",
                "completed?lang=in&page=$vp"
            )
        }
        val fetched = slots.map { path ->
            async { reelshortJson(path)?.let { flat(it.dataOrSelf(), "reelshort") }.orEmpty() }
        }.awaitAll()
        val rec = dedupe(fetched.getOrElse(0) { emptyList() }) // foryou
        val nw = dedupe(fetched.getOrElse(1) { emptyList() })  // new
        val pop = dedupe(fetched.getOrElse(2) { emptyList() }) // completed
        val more = vp < 3 && (rec.size + pop.size + nw.size) >= 20
        if (pop.isEmpty() && rec.isEmpty() && nw.isEmpty() && vp == 1) error("Sumber ini sedang tidak tersedia. Coba rak lain dulu ya.")
        return@coroutineScope HomeBundle(rec, pop, nw, vp, more)
    }

    /**
     * Beranda Anichin: popular = ongoing, recommended = latest, newest = completed
     * Endpoint: /home (popular+latest), /ongoing/, /completed/
     */
    private suspend fun loadAnichinHome(page: Int): HomeBundle = coroutineScope {
        val vp = page.coerceAtLeast(1)
        val slots = if (vp == 1) {
            listOf(
                AnichinCatalog.HOME_PATH,
                AnichinCatalog.ONGOING_PATH,
                AnichinCatalog.COMPLETED_PATH
            )
        } else {
            listOf(
                "${AnichinCatalog.ONGOING_PATH}?page=$vp",
                "${AnichinCatalog.COMPLETED_PATH}?page=$vp",
                "${AnichinCatalog.HOME_PATH}?page=$vp"
            )
        }
        val fetched = slots.map { path ->
            async { anichinJson(path)?.let { flat(it.dataOrSelf(), "anichin") }.orEmpty() }
        }.awaitAll()
        val rec = dedupe(fetched.getOrElse(0) { emptyList() })
        val pop = dedupe(fetched.getOrElse(1) { emptyList() })
        val nw = dedupe(fetched.getOrElse(2) { emptyList() })
        val more = vp < 3 && (rec.size + pop.size + nw.size) >= 10
        if (pop.isEmpty() && rec.isEmpty() && nw.isEmpty() && vp == 1) error("Sumber ini sedang tidak tersedia. Coba rak lain dulu ya.")
        return@coroutineScope HomeBundle(rec, pop, nw, vp, more)
    }

    suspend fun searchPlatform(q: String, p: String): List<Drama> = coroutineScope {
        val items = runCatching {
            when (p) {
                "stardusttv" -> flat(getJson("${apiBase(p)}/search?q=${enc(q)}&lang=id&page=1&page_size=20").dataOrSelf(), p)
                "dramabox" -> flat(getJson("${apiBase(p)}/search?keyword=${enc(q)}&page=1&lang=in").dataOrSelf(), p)
                // NetShort: keyword masuk ke path (bukan query), spasi harus %20.
                "netshort" -> flat(getJson("${apiBase(p)}/${NetshortCatalog.searchPath(q)}").dataOrSelf(), p)
                // ReelShort: /search?q=&page=&lang=in
                "reelshort" -> flat(getJson("${apiBase(p)}/${ReelshortCatalog.searchPath(q, 1)}").dataOrSelf(), p)
                "dramanova" -> flat(getJson("${apiBase(p)}/drama/search?q=${enc(q)}&pageNum=1&pageSize=50&languages=in").dataOrSelf(), p)
                "freereels" -> freereelsSearch(q, p)
                "anichin" -> flat(getJson("${apiBase(p)}/${AnichinCatalog.searchPath(q)}").dataOrSelf(), p)
                "raptdrama" -> {
                    val body = JSONObject().put("keyword", q).put("page", 1).put("limit", 20).put("lang", "en-US").toString()
                    flat(getJson("${apiBase(p)}/open/search", jsonBody = body).dataOrSelf(), p)
                }
                "starshort" -> {
                    val base = apiBase(p)
                    val sr = getJson("$base/api/v1/dramas/search?q=${enc(q)}&lang=4")
                    val homePool = runCatching { flat(getJson("$base/api/v1/dramas?lang=4").dataOrSelf(), p) }.getOrDefault(emptyList())
                        .associateBy { it.id }
                    val arr = sr.optJSONArray("data") ?: JSONArray()
                    arr.objects().take(12).map { o ->
                        async {
                            val id = o.stringAny("id")
                            val title = cleanText(o.stringAny("title"))
                            homePool[id] ?: runCatching {
                                val det = getJson("$base/api/v1/dramas/${enc(id)}?lang=4")
                                normalize(det, p).copy(id = id, title = title.ifBlank { det.stringAny("title") })
                            }.getOrNull() ?: Drama(id, title, platform = p)
                        }
                    }.awaitAll().filter { it.id.isNotBlank() && it.title.isNotBlank() }
                }
                else -> {
                    val extraUrl = ExtraPlatforms.searchUrl(p, q, apiBase(p))
                    val direct = if (extraUrl != null) {
                        runCatching { flat(getJson(extraUrl).dataOrSelf(), p) }.getOrDefault(emptyList())
                    } else if (!ExtraPlatforms.isExtraPlatform(p)) {
                        runCatching { flat(getJson("${apiBase(p)}/search?q=${enc(q)}&lang=id&limit=50&offset=0").dataOrSelf(), p) }.getOrDefault(emptyList())
                    } else emptyList()
                    if (direct.isNotEmpty() || !ExtraPlatforms.isExtraPlatform(p)) direct
                    else {
                        val urls = homeUrls(p, 1).distinct()
                        val pool = urls.map { u -> async { runCatching { flat(fetchHome(u).dataOrSelf(), p) }.getOrDefault(emptyList()) } }.awaitAll().flatten()
                        val tokens = normalizeKey(q).split(" ").filter { it.length >= 2 }
                        pool.filter { d ->
                            val hay = normalizeKey(d.title + " " + d.description + " " + d.tags.joinToString(" "))
                            tokens.isEmpty() || tokens.any { hay.contains(it) }
                        }
                    }
                }
            }
        }.getOrDefault(emptyList())
        dedupeAndRank(items, q).take(80)
    }

    // Endpoint /search di dramahub.be kadang balas 500 upstream (routing &
    // validasinya jalan, backend-nya yang error). Kalau gagal/kosong,
    // fallback: saring katalog home (foryou + popular + new) secara lokal
    // supaya fitur Cari tetap kepakai.
    private suspend fun freereelsSearch(q: String, p: String): List<Drama> = coroutineScope {
        val direct = runCatching {
            flat(getJson("${apiBase(p)}/search?q=${enc(q)}&lang=id-ID&limit=50").dataOrSelf(), p)
        }.getOrDefault(emptyList())
        if (direct.isNotEmpty()) return@coroutineScope direct
        val base = apiBase(p)
        val catalog = listOf(
            "$base/foryou?page=1&lang=id-ID",
            "$base/popular?page=0&lang=id-ID",
            "$base/new?page=0&lang=id-ID"
        ).map { url -> async { runCatching { flat(getJson(url).dataOrSelf(), p) }.getOrDefault(emptyList()) } }
            .awaitAll().flatten().distinctBy { it.platform + "|" + it.id }
        val words = normalizeKey(q).split(" ").filter { it.length >= 2 }
        catalog.filter { d ->
            val t = normalizeKey(d.title) + " " + normalizeKey(d.description)
            words.any { t.contains(it) }
        }
    }

    // Ambil satu rak konten dari path bebas (mis. "hidden-gems?lang=in" atau
    // "browse?type=433&page=1&lang=in") — dipakai rak genre di beranda.
    suspend fun browsePath(p: String, path: String): List<Drama> =
        runCatching { flat(getJson("${apiBase(p)}/$path").dataOrSelf(), p) }.getOrDefault(emptyList())

    // DramaBox (dramahub.be): daftar genre resmi dari /categories, dipakai rak
    // beranda supaya labelnya ikut katalog upstream. Gagal/kosong = pemanggil
    // jatuh ke daftar type id cadangan. Genre jarang berubah, jadi disimpan
    // sekali per sesi biar pindah platform bolak-balik tidak menembak ulang.
    private var dramaboxCategoriesCache: List<DramaboxCategory>? = null

    // NetShort (dramahub.be): /tabs untuk tab konten dan /categories untuk tag.
    // Dua-duanya jarang berubah, jadi disimpan sekali per sesi seperti DramaBox.
    private var netshortTabsCache: List<NetshortTab>? = null
    private var netshortTagsCache: List<Pair<String, String>>? = null

    // ReelShort (dramahub.be): /foryou tabs
    private var reelshortTabsCache: List<ReelshortTab>? = null
    private var reelshortSearchHintsCache: List<String>? = null

    // Anichin (dramahub.be): genres + schedule
    private var anichinGenresCache: List<AnichinGenre>? = null
    private var anichinSearchHintsCache: List<String>? = null

    // Pembungkus anti-gagal: endpoint katalog yang kosong/error tidak boleh
    // menjatuhkan beranda, cukup bikin rak-nya dilewati.
    private suspend fun netshortJson(path: String): JSONObject? = try {
        getJson("${apiBase("netshort")}/$path")
    } catch (e: CancellationException) {
        throw e
    } catch (_: Throwable) {
        null
    }

    private suspend fun reelshortJson(path: String): JSONObject? = try {
        getJson("${apiBase("reelshort")}/$path")
    } catch (e: CancellationException) {
        throw e
    } catch (_: Throwable) {
        null
    }

    private suspend fun anichinJson(path: String): JSONObject? = try {
        getJson("${apiBase("anichin")}/$path")
    } catch (e: CancellationException) {
        throw e
    } catch (_: Throwable) {
        null
    }

    suspend fun netshortContentTabs(): List<NetshortTab> {
        netshortTabsCache?.takeIf { it.isNotEmpty() }?.let { return it }
        val tabs = netshortJson(NetshortCatalog.TABS_PATH)?.let { NetshortCatalog.contentTabs(it) }.orEmpty()
        if (tabs.isNotEmpty()) netshortTabsCache = tabs
        return tabs
    }

    suspend fun netshortTags(): List<Pair<String, String>> {
        netshortTagsCache?.takeIf { it.isNotEmpty() }?.let { return it }
        val tags = netshortJson(NetshortCatalog.CATEGORIES_PATH)
            ?.let { json -> NetshortCatalog.tags(json).map { it.name to it.labelId } }.orEmpty()
        if (tags.isNotEmpty()) netshortTagsCache = tags
        return tags
    }

    // Saran pencarian NetShort (/search-hint), dipakai chip di layar Cari.
    suspend fun netshortSearchHints(): List<String> =
        netshortJson("search-hint?lang=id_ID")?.let { NetshortCatalog.searchHints(it) }.orEmpty()

    // ReelShort tabs dari /foryou
    suspend fun reelshortTabs(): List<ReelshortTab> {
        reelshortTabsCache?.takeIf { it.isNotEmpty() }?.let { return it }
        val tabs = reelshortJson(ReelshortCatalog.FORYOU_PATH)?.let { ReelshortCatalog.tabs(it) }.orEmpty()
        if (tabs.isNotEmpty()) reelshortTabsCache = tabs
        return tabs
    }

    // Saran pencarian ReelShort: dari search_keyword_list di foryou + suggestions
    suspend fun reelshortSearchHints(): List<String> {
        reelshortSearchHintsCache?.takeIf { it.isNotEmpty() }?.let { return it }
        val hints = mutableListOf<String>()
        reelshortJson(ReelshortCatalog.FORYOU_PATH)?.let { hints += ReelshortCatalog.searchHintsFromHome(it) }
        if (hints.size < 8) {
            reelshortJson(ReelshortCatalog.SEARCH_SUGGESTIONS_PATH)?.let { hints += ReelshortCatalog.searchHintsFromSuggestions(it) }
        }
        val distinct = hints.distinct().take(12)
        if (distinct.isNotEmpty()) reelshortSearchHintsCache = distinct
        return distinct
    }

    // Anichin genres dari /genres
    suspend fun anichinGenres(): List<AnichinGenre> {
        anichinGenresCache?.takeIf { it.isNotEmpty() }?.let { return it }
        val genres = anichinJson(AnichinCatalog.GENRES_PATH)?.let { AnichinCatalog.parseGenres(it) }.orEmpty()
        if (genres.isNotEmpty()) anichinGenresCache = genres
        return genres
    }

    // Saran pencarian Anichin: dari genres + popular titles
    suspend fun anichinSearchHints(): List<String> {
        anichinSearchHintsCache?.takeIf { it.isNotEmpty() }?.let { return it }
        val hints = mutableListOf<String>()
        anichinGenres().take(8).forEach { hints.add(it.name) }
        anichinJson(AnichinCatalog.HOME_PATH)?.let { json ->
            val (popular, _) = AnichinCatalog.parseHome(json)
            popular.take(6).forEach { hints.add(it.title) }
        }
        val distinct = hints.distinct().take(12)
        if (distinct.isNotEmpty()) anichinSearchHintsCache = distinct
        return distinct
    }

    // Anichin schedule
    suspend fun anichinSchedule() = anichinJson(AnichinCatalog.SCHEDULE_PATH)?.let { AnichinCatalog.parseSchedule(it) }.orEmpty()


    suspend fun dramaboxCategories(): List<DramaboxCategory> {
        dramaboxCategoriesCache?.takeIf { it.isNotEmpty() }?.let { return it }
        val fetched = try {
            DramaboxCatalog.categories(getJson("${apiBase("dramabox")}/categories?lang=in"))
        } catch (e: CancellationException) {
            throw e
        } catch (_: Throwable) {
            emptyList()
        }
        if (fetched.isNotEmpty()) dramaboxCategoriesCache = fetched
        return fetched
    }

    // Dramanova (dramahub.be): ambil items dari modul home
    // (dramanova_hot, dramanova_new, dramanova_more, dramanova_baxi, dramanova_anime).
    suspend fun browseDramanova(categoryKey: String): List<Drama> =
        runCatching { flat(getJson("${apiBase("dramanova")}/home/modules/$categoryKey/items?pageNum=1&pageSize=20&languages=in").dataOrSelf(), "dramanova") }.getOrDefault(emptyList())

    // Freereels: ambil by category path (female, male, anime, dubbing, coming-soon)
    suspend fun browseFreereels(categoryPath: String): List<Drama> =
        runCatching { flat(getJson("${apiBase("freereels")}/$categoryPath?page=0&lang=id-ID").dataOrSelf(), "freereels") }.getOrDefault(emptyList())

    suspend fun loadDetail(input: Drama): Detail {
        val p = input.platform
        val url = detailUrl(input); val json = getJson(url)
        if (p == "melolo") {
            val bookJson = json
            val multiJson = runCatching { getJson("${apiBase(p)}/multi-video?id=${enc(input.id)}&lang=id") }.getOrNull()
            val data = bookJson.optJSONObject("data") ?: bookJson
            val seriesObj = multiJson?.optJSONObject("series") ?: multiJson?.optJSONObject("data") ?: multiJson ?: JSONObject()
            val epsArr = multiJson?.optJSONArray("episodes") ?: multiJson?.optJSONArray("video_list") ?: seriesObj.optJSONArray("episodes") ?: seriesObj.optJSONArray("video_list") ?: bookJson.optJSONArray("episodes") ?: JSONArray()

            val title = data.stringAny("title", "book_name", "bookName", "name").ifBlank { seriesObj.stringAny("title") }.ifBlank { input.title }
            val desc = data.stringAny("introduction", "description", "synopsis", "intro").ifBlank { seriesObj.stringAny("intro", "intro") }.ifBlank { input.description }
            val poster = fixImg(data.stringAny("cover", "thumb_url", "image", "poster").ifBlank { seriesObj.stringAny("cover") }.ifBlank { input.poster })

            val eps = epsArr.objects().mapIndexed { i, o ->
                EpisodeInfo(
                    o.intAny("index", "episode", "episode_no", "episode_number", "chapterIndex", i + 1),
                    o.stringAny("stream_url", "streaming", "url", "play_url", "video_url"),
                    o.stringAny("episode_label", "title", "label")
                )
            }
            val total = max(data.intAny("episode_count", "chapterCount", "chapter_count", input.episodes), eps.size)
            val drama = Drama(input.id, title, desc, poster, total, input.views, tagsOf(data), p, input.subjectType)
            return Detail(drama, if (eps.isNotEmpty()) eps else (1..total.coerceAtLeast(1)).map { EpisodeInfo(it) })
        }
        if (p == "stardusttv") {
            val data = json.optJSONObject("data") ?: json
            val parsed = normalize(data, p).let { it.copy(id = it.id.ifBlank { input.id }, title = it.title.ifBlank { input.title }, poster = fixImg(it.poster.ifBlank { input.poster }), platform = p) }
            val count = max(parsed.episodes, input.episodes).coerceAtLeast(1)
            val eps = (1..count).map { EpisodeInfo(it) }
            return Detail(parsed.copy(episodes = count), eps)
        }
        if (p == "dramabox") {
            // Proxy v2: data { bookName, coverWap, introduction, tags, chapterCount,
            // episodeList [{ chapterIndex (0-based), chapterId, isPay }] }.
            // Bentuk lama (bookInfo + chapterList) tetap didukung sebagai cadangan.
            val data = json.optJSONObject("data") ?: error("Detail tidak ditemukan")
            val info = data.optJSONObject("bookInfo") ?: data
            val epsArr = data.optJSONArray("chapterList") ?: data.optJSONArray("episodeList") ?: JSONArray()
            val title = info.stringAny("bookName", "name", "title").ifBlank { input.title }
            val desc = cleanText(info.stringAny("introduction", "description", "synopsis")).ifBlank { input.description }
            val poster = fixImg(info.stringAny("cover", "coverWap", "image", "poster").ifBlank { input.poster })
            val eps = epsArr.objects().mapIndexed { i, o ->
                // chapterIndex v2 mulai dari 0 dan sah bernilai 0 — jangan lewat
                // intAny (nilai 0 dianggap kosong di sana).
                val number = if (o.has("chapterIndex")) o.optInt("chapterIndex", i) + 1
                else o.intAny("indexStr", "index", "episode", i + 1)
                // isPay tidak dijadikan kunci: /play tetap melayani episode berbayar.
                EpisodeInfo(
                    number,
                    o.stringAny("stream_url", "streaming", "url"),
                    o.stringAny("episode_label", "title", "label", "chapterName")
                )
            }
            val total = max(info.intAny("chapterCount", "chapter_count", input.episodes), eps.size)
            val drama = Drama(input.id, title, desc, poster, total, input.views, tagsOf(info), p, input.subjectType)
            return Detail(drama, if (eps.isNotEmpty()) eps else (1..total.coerceAtLeast(1)).map { EpisodeInfo(it) })
        }
        if (p == "netshort") {
            // NetShort: data { id, title, cover, description, labels, totalEpisodes,
            // isFinished, episodes: [{ episodeNo, episodeId, cover, isLocked }] }.
            // Endpoint /episode memang tetap melayani episode isLocked=true, jadi
            // penandanya tidak dipakai untuk menyembunyikan episode.
            val data = json.optJSONObject("data") ?: error("Detail tidak ditemukan")
            val title = data.stringAny("title", "name").ifBlank { input.title }
            val desc = cleanText(data.stringAny("description", "introduction", "synopsis")).ifBlank { input.description }
            val poster = fixImg(data.stringAny("cover").ifBlank { input.poster })
            val epsArr = data.optJSONArray("episodes") ?: JSONArray()
            val eps = epsArr.objects().mapIndexed { i, o ->
                EpisodeInfo(
                    number = o.intAny("episodeNo", "episode", i + 1),
                    label = o.stringAny("title", "label")
                )
            }
            val total = max(data.intAny("totalEpisodes", "episodeCount", input.episodes), eps.size)
            val drama = Drama(input.id, title, desc, poster, total, input.views, tagsOf(data), p, input.subjectType)
            return Detail(drama, if (eps.isNotEmpty()) eps else (1..total.coerceAtLeast(1)).map { EpisodeInfo(it) })
        }
        if (p == "dramanova") {
            // Dramanova (dramahub.be): { drama: { dramaId, title, synopsis, poster (relatif!),
            // totalEpisodes, tags, score }, episodes: [{ episodeNo, episodeId, title, wasLocked }] }.
            // Subtitle tidak dibalas di detail — diambil dari endpoint unlock saat stream.
            val data = json.optJSONObject("drama") ?: json
            val title = data.stringAny("title").ifBlank { input.title }
            val desc = cleanText(data.stringAny("synopsis", "description")).ifBlank { input.description }
            // Poster balasan relatif (mis. "poster_i/xxx.jpg") — sambungkan ke CDN-nya.
            var poster = data.stringAny("poster", "cover").ifBlank { input.poster }
            if (poster.isNotBlank() && !poster.startsWith("http")) poster = "https://aasleeimg.yfeitrade.com/$poster"
            poster = fixImg(poster)
            val epsArr = json.optJSONArray("episodes") ?: data.optJSONArray("episodes") ?: JSONArray()
            val eps = epsArr.objects().mapIndexed { i, o ->
                EpisodeInfo(
                    number = o.intAny("episodeNo", "number", "episode", i + 1),
                    label = o.stringAny("title", "label"),
                    locked = o.optBoolean("wasLocked", false)
                )
            }
            val total = max(data.intAny("totalEpisodes", "episodes", input.episodes), eps.size)
            val drama = Drama(input.id, title, desc, poster, total, input.views, tagsOf(data), p, input.subjectType)
            return Detail(drama, if (eps.isNotEmpty()) eps else (1..total.coerceAtLeast(1)).map { EpisodeInfo(it) })
        }
        if (p == "freereels") {
            // Freereels: { id, name, desc, cover, episode_count, follow_count, episode_list: [{ id, name, episode_number, video_url, m3u8_url }] }
            val title = json.stringAny("name", "title").ifBlank { input.title }
            val desc = cleanText(json.stringAny("desc", "description")).ifBlank { input.description }
            val poster = fixImg(json.stringAny("cover").ifBlank { input.poster })
            // Episode list bisa ada inline di detail, atau dari endpoint terpisah
            val epsArr = json.optJSONArray("episode_list") ?: JSONArray()
            val eps = epsArr.objects().mapIndexed { i, o ->
                EpisodeInfo(
                    number = o.intAny("episode_number", "index", i + 1),
                    streaming = o.stringAny("id"), // episode ID dipakai untuk resolve stream
                    label = o.stringAny("name", "title"),
                    locked = !o.optBoolean("free", true)
                )
            }
            val total = max(json.intAny("episode_count", input.episodes), eps.size)
            val viewCount = json.optLong("follow_count", 0).takeIf { it > 0 }?.let { "${it/1000}K" } ?: json.stringAny("view_count")
            val drama = Drama(input.id, title, desc, poster, total, viewCount, tagsOf(json), p, input.subjectType)
            return Detail(drama, if (eps.isNotEmpty()) eps else (1..total.coerceAtLeast(1)).map { EpisodeInfo(it) })
        }
        if (p == "reelshort") {
            // ReelShort: detail dari /book/{id}?lang=in = data {book_id, book_title, book_pic, special_desc, chapter_count, tag, theme, etc}
            // chapters dari /book/{id}/chapters?lang=in = data.chapters [{chapter_id, chapter_name, serial_number, duration}]
            val data = json.optJSONObject("data") ?: json
            val title = data.stringAny("book_title", "title", "name").ifBlank { input.title }
            val desc = cleanText(data.stringAny("special_desc", "description", "introduction", "synopsis")).ifBlank { input.description }
            val poster = fixImg(data.stringAny("book_pic", "cover", "poster").ifBlank { input.poster })
            val totalFromDetail = data.intAny("chapter_count", "totalEpisodes", "episode_count", input.episodes)
            // Ambil chapters
            val chaptersJson = runCatching { getJson("${apiBase(p)}/${ReelshortCatalog.chaptersPath(input.id)}") }.getOrNull()
            val chapters = chaptersJson?.let { ReelshortCatalog.chapters(it) }.orEmpty()
            val eps = if (chapters.isNotEmpty()) {
                chapters.map { ch ->
                    EpisodeInfo(
                        number = ch.number,
                        streaming = ch.id, // chapter_id dipakai untuk resolve stream
                        label = ch.name,
                        locked = ch.isLocked
                    )
                }
            } else {
                // Fallback: buat episode list dari total
                (1..totalFromDetail.coerceAtLeast(1)).map { EpisodeInfo(it) }
            }
            val total = max(totalFromDetail, eps.size).coerceAtLeast(1)
            val tags = mutableListOf<String>()
            // theme dan tag
            data.optJSONArray("theme")?.let { arr -> for (i in 0 until arr.length()) { val t = arr.optString(i); if (t.isNotBlank()) tags.add(t) } }
            data.optJSONArray("tag")?.let { arr -> for (i in 0 until arr.length()) { val t = arr.optString(i); if (t.isNotBlank()) tags.add(t) } }
            data.optJSONArray("tag_list")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val o = arr.optJSONObject(i) ?: continue
                    val n = o.optString("tag_name").trim()
                    if (n.isNotBlank()) tags.add(n)
                }
            }
            if (tags.isEmpty()) tags.addAll(tagsOf(data))
            val drama = Drama(input.id, title, desc, poster, total, input.views, tags.distinct().take(8), p, input.subjectType)
            return Detail(drama, eps)
        }
        if (p == "anichin") {
            // Anichin: detail dari /anime/{slug} = data {title, poster, synopsis, genres[], info, episodes[]}
            // json di sini sudah dari detailUrl (anime/{id})
            val detail = AnichinCatalog.parseDetail(json) ?: error("Detail tidak ditemukan")
            val title = detail.title.ifBlank { input.title }
            val desc = cleanText(detail.synopsis).ifBlank { input.description }
            val poster = fixImg(detail.poster.ifBlank { input.poster })
            val eps = detail.episodes.mapIndexed { idx, ep ->
                EpisodeInfo(
                    number = ep.number.filter { it.isDigit() }.toIntOrNull() ?: (idx + 1),
                    streaming = ep.slug, // episode slug dipakai untuk resolve stream
                    label = ep.title.ifBlank { ep.number },
                    locked = false
                )
            }
            // Kalau episode number berupa "Movie", tetap jadikan 1
            val normalizedEps = if (eps.isEmpty()) {
                listOf(EpisodeInfo(1, input.id, title))
            } else {
                // Pastikan number urut 1..N kalau parsing gagal
                eps.mapIndexed { i, e -> if (e.number <= 0) e.copy(number = i + 1) else e }
            }
            val total = normalizedEps.size.coerceAtLeast(1)
            val tags = (detail.genres + detail.info.values).distinct().take(8)
            val infoStatus = detail.info["status"] ?: detail.info["tipe"] ?: ""
            val views = if (infoStatus.isNotBlank()) infoStatus else input.views
            val drama = Drama(input.id, title, desc, poster, total, views, tags, p, input.subjectType)
            return Detail(drama, normalizedEps)
        }
        if (ExtraPlatforms.isExtraPlatform(p)) {
            return loadExtraPlatformDetail(input, json)
        }
        val data = json.optJSONObject("data") ?: error("Detail tidak ditemukan")
        val d = normalize(data, p).let { it.copy(id = it.id.ifBlank { input.id }, title = it.title.ifBlank { input.title }, poster = fixImg(it.poster.ifBlank { input.poster }), description = it.description.ifBlank { input.description }, episodes = max(it.episodes, input.episodes), platform = p) }
        val epsArr = data.optJSONArray("video_list") ?: data.optJSONArray("episode_list") ?: data.optJSONArray("episodes") ?: data.optJSONArray("chapterList")
        val eps = epsArr?.objects()?.mapIndexed { i, o -> EpisodeInfo(o.intAny("episode", "episode_no", "chapterIndex", i + 1), o.stringAny("streaming"), o.stringAny("episode_label", "title", "label")) }.orEmpty()
        val total = max(d.episodes, eps.size)
        return Detail(d.copy(episodes = total), if (eps.isNotEmpty()) eps else (1..total.coerceAtLeast(1)).map { EpisodeInfo(it) })
    }

    private suspend fun loadExtraPlatformDetail(input: Drama, json: JSONObject): Detail {
        val p = input.platform
        val base = apiBase(p)
        val id = input.id

        if (p == "wetv") {
            val data = json.optJSONObject("data") ?: json
            val title = cleanText(data.stringAny("title", "name").ifBlank { input.title })
            val desc = cleanText(data.stringAny("description", "desc", "introduction").ifBlank { input.description })
            val poster = fixImg(data.stringAny("posterVt", "posterHz", "cover", "poster").ifBlank { input.poster })
            val epsJson = runCatching { getJson("$base/api/episodes?cid=${enc(id)}&lang=id&country=ID") }.getOrNull()
            val epsArr = epsJson?.optJSONArray("data") ?: data.optJSONArray("episodes") ?: JSONArray()
            val eps = epsArr.objects().mapIndexed { i, o ->
                EpisodeInfo(
                    number = i + 1,
                    streaming = o.stringAny("vid", "id"),
                    label = o.stringAny("title", "episode")
                )
            }
            val total = max(max(data.intAny("episodeCount", "totalEpisodes", input.episodes), eps.size), 1)
            val drama = Drama(id, title, desc, poster, total, input.views, tagsOf(data).ifEmpty { input.tags }, p, input.subjectType)
            return Detail(drama, if (eps.isNotEmpty()) eps else (1..total).map { EpisodeInfo(it) })
        }

        if (p == "sodareels") {
            val dinsur = json.optJSONObject("data")?.optJSONObject("dinsur") ?: json.optJSONObject("dinsur") ?: json.optJSONObject("data") ?: json
            val bmini = dinsur.optJSONObject("bmini") ?: dinsur
            val title = cleanText(bmini.stringAny("nmeasu", *DRAMA_TITLE_KEYS).ifBlank { input.title })
            val desc = cleanText(bmini.stringAny("ddet", *DRAMA_DESC_KEYS).ifBlank { input.description })
            val poster = fixImg(bmini.stringAny("pbat", *DRAMA_COVER_KEYS).ifBlank { input.poster })
            val erefu = dinsur.optJSONArray("erefu") ?: JSONArray()
            val eps = erefu.objects().mapIndexed { i, o ->
                val epNo = o.intAny("estate", i + 1)
                val ppoem = o.optJSONArray("ppoem")?.objects().orEmpty()
                val best = ppoem.firstOrNull { it.stringAny("Dcoura").contains("720", true) }
                    ?: ppoem.firstOrNull { it.stringAny("Mcurr", "Bdesi").startsWith("http") }
                val url = best?.stringAny("Mcurr", "Bdesi").orEmpty()
                EpisodeInfo(number = epNo, streaming = url, label = "EP $epNo")
            }
            val total = max(max(bmini.intAny("ewin", *DRAMA_EP_KEYS, input.episodes), eps.size), 1)
            val drama = Drama(id, title, desc, poster, total, input.views, input.tags, p, input.subjectType)
            return Detail(drama, if (eps.isNotEmpty()) eps else (1..total).map { EpisodeInfo(it) })
        }

        if (p == "dotdrama") {
            val dgiv = json.optJSONObject("dgiv") ?: json.optJSONObject("data")?.optJSONObject("dgiv") ?: json.optJSONObject("data") ?: json
            val bswitc = dgiv.optJSONObject("bswitc") ?: dgiv
            val title = cleanText(bswitc.stringAny("nseri", "rwill", *DRAMA_TITLE_KEYS).ifBlank { input.title })
            val desc = cleanText(bswitc.stringAny("dwill", *DRAMA_DESC_KEYS).ifBlank { input.description })
            val poster = fixImg(bswitc.stringAny("pday", *DRAMA_COVER_KEYS).ifBlank { input.poster })
            val ebeer = dgiv.optJSONArray("ebeer") ?: dgiv.optJSONArray("eallo") ?: JSONArray()
            val eps = ebeer.objects().mapIndexed { i, o ->
                val epNo = o.intAny("ewheel", i + 1)
                val pphys = o.optJSONArray("pphys")?.objects().orEmpty()
                val best = pphys.firstOrNull { it.stringAny("Dbag").contains("720", true) }
                    ?: pphys.firstOrNull { it.stringAny("Mopp", "Bcold").startsWith("http") }
                val url = best?.stringAny("Mopp", "Bcold").orEmpty()
                EpisodeInfo(number = epNo, streaming = url, label = "EP $epNo")
            }
            val total = max(max(bswitc.intAny("ewood", *DRAMA_EP_KEYS, input.episodes), eps.size), 1)
            val drama = Drama(id, title, desc, poster, total, input.views, input.tags, p, input.subjectType)
            return Detail(drama, if (eps.isNotEmpty()) eps else (1..total).map { EpisodeInfo(it) })
        }

        if (p == "snackshort") {
            val inner = json.optJSONObject("data")?.optJSONObject("data") ?: json.optJSONObject("data") ?: json
            val book = inner.optJSONObject("book") ?: inner
            val ep1PlayUrl = inner.stringAny("play_url", "sd_264_url", "hd_264_url")
            val title = cleanText(book.stringAny("book_name", "title", *DRAMA_TITLE_KEYS).ifBlank { input.title })
            val desc = cleanText(book.stringAny("introduce", *DRAMA_DESC_KEYS).ifBlank { input.description })
            val poster = fixImg(book.stringAny("image", "horizontalImage", *DRAMA_COVER_KEYS).ifBlank { input.poster })
            val chJson = runCatching { getJson("$base/api/v1/book/${enc(id)}/chapters?lang=Indonesian") }.getOrNull()
            val chArr = chJson?.optJSONObject("data")?.optJSONArray("data") ?: chJson?.optJSONArray("data") ?: JSONArray()
            val eps = chArr.objects().mapIndexed { i, o ->
                val cid = o.stringAny("chapter_id", "id")
                val streamRef = if (i == 0 && ep1PlayUrl.startsWith("http")) "$cid|||$ep1PlayUrl" else cid
                EpisodeInfo(
                    number = o.intAny("chapter_order", i + 1),
                    streaming = streamRef,
                    label = o.stringAny("chapter_name", "title")
                )
            }
            val total = max(max(book.intAny("chapters", "onlineChapter", *DRAMA_EP_KEYS, input.episodes), eps.size), 1)
            val drama = Drama(id, title, desc, poster, total, input.views, tagsOf(book).ifEmpty { input.tags }, p, input.subjectType)
            return Detail(drama, if (eps.isNotEmpty()) eps else (1..total).map { EpisodeInfo(it, if (it == 1 && ep1PlayUrl.startsWith("http")) "1|||$ep1PlayUrl" else "") })
        }

        if (p == "shotshort") {
            val data = json.optJSONObject("data") ?: json
            val title = cleanText(data.stringAny("bookName", "title", *DRAMA_TITLE_KEYS).ifBlank { input.title })
            val desc = cleanText(data.stringAny("introduction", *DRAMA_DESC_KEYS).ifBlank { input.description })
            val poster = fixImg(data.stringAny("coverUrl", *DRAMA_COVER_KEYS).ifBlank { input.poster })
            val epsJson = runCatching { getJson("$base/api/book/${enc(id)}/episodes?lang=id") }.getOrNull()
            val epsArr = epsJson?.optJSONObject("data")?.optJSONArray("episodes") ?: data.optJSONArray("episodes") ?: JSONArray()
            val eps = epsArr.objects().mapIndexed { i, o ->
                EpisodeInfo(
                    number = i + 1,
                    streaming = o.stringAny("chapterId", "chapter_id", "id"),
                    label = o.stringAny("chapterName", "title")
                )
            }
            val total = max(max(data.intAny("totalChapterNum", *DRAMA_EP_KEYS, input.episodes), eps.size), 1)
            val drama = Drama(id, title, desc, poster, total, input.views, tagsOf(data).ifEmpty { input.tags }, p, input.subjectType)
            return Detail(drama, if (eps.isNotEmpty()) eps else (1..total).map { EpisodeInfo(it) })
        }

        if (p == "shortwave") {
            val data = json.optJSONObject("data") ?: json
            val title = cleanText(data.stringAny("title", "drama_title", *DRAMA_TITLE_KEYS).ifBlank { input.title })
            val desc = cleanText(data.stringAny("drama_description", *DRAMA_DESC_KEYS).ifBlank { input.description })
            val poster = fixImg(data.stringAny("drama_cover", *DRAMA_COVER_KEYS).ifBlank { input.poster })
            val epsArr = data.optJSONArray("episodes") ?: JSONArray()
            val eps = epsArr.objects().mapIndexed { i, o ->
                EpisodeInfo(
                    number = o.intAny("index", "chapter_index", i + 1),
                    streaming = o.stringAny("chapter_id", "chapterId", "id"),
                    label = o.stringAny("chapter_name", "title")
                )
            }
            val total = max(max(data.intAny("episode_count", *DRAMA_EP_KEYS, input.episodes), eps.size), 1)
            val drama = Drama(id, title, desc, poster, total, input.views, tagsOf(data).ifEmpty { input.tags }, p, input.subjectType)
            return Detail(drama, if (eps.isNotEmpty()) eps else (1..total).map { EpisodeInfo(it) })
        }

        if (p == "shortsky") {
            val data = json.optJSONObject("data")?.optJSONObject("jieguo") ?: json.optJSONObject("data") ?: json
            val title = cleanText(data.stringAny("title", *DRAMA_TITLE_KEYS).ifBlank { input.title })
            val desc = cleanText(data.stringAny("desc", *DRAMA_DESC_KEYS).ifBlank { input.description })
            val poster = fixImg(data.stringAny("icon", "cover", *DRAMA_COVER_KEYS).ifBlank { input.poster })
            val total = max(data.intAny("episode", *DRAMA_EP_KEYS, input.episodes), 1)
            val drama = Drama(id, title, desc, poster, total, input.views, tagsOf(data).ifEmpty { input.tags }, p, input.subjectType)
            return Detail(drama, (1..total).map { EpisodeInfo(it) })
        }

        if (p == "shorten") {
            val data = json.optJSONObject("data")?.optJSONObject("data") ?: json.optJSONObject("data") ?: json
            val title = cleanText(data.stringAny("title", *DRAMA_TITLE_KEYS).ifBlank { input.title })
            val desc = cleanText(data.stringAny("description", *DRAMA_DESC_KEYS).ifBlank { input.description })
            val poster = fixImg(data.coverUrl().ifBlank { data.stringAny(*DRAMA_COVER_KEYS) }.ifBlank { input.poster })
            val rawEps = data.optJSONArray("seasons")?.optJSONObject(0)?.optJSONArray("episodes")?.objects().orEmpty()
            val nonTeaser = if (rawEps.size > 1 && rawEps.any { it.optInt("number", 0) > 0 }) {
                rawEps.filter { it.optInt("number", 1) > 0 }
            } else rawEps
            val eps = nonTeaser.mapIndexed { i, o ->
                val vUrl = o.stringAny("video_url", "url").replace("http://", "https://")
                val sUrl = pickSubtitleUrl(o)
                EpisodeInfo(
                    number = i + 1,
                    streaming = if (sUrl.isNotBlank()) "$vUrl##$sUrl" else vUrl,
                    label = o.stringAny("title").ifBlank { "EP ${i + 1}" }
                )
            }
            val total = max(eps.size, input.episodes).coerceAtLeast(1)
            val drama = Drama(id, title, desc, poster, total, input.views, tagsOf(data).ifEmpty { input.tags }, p, input.subjectType)
            return Detail(drama, if (eps.isNotEmpty()) eps else (1..total).map { EpisodeInfo(it) })
        }

        if (p == "reelbuzz") {
            val data = json.optJSONObject("rows") ?: json.optJSONObject("data") ?: json
            val title = cleanText(data.stringAny("title", *DRAMA_TITLE_KEYS).ifBlank { input.title })
            val desc = cleanText(data.stringAny("intro", *DRAMA_DESC_KEYS).ifBlank { input.description })
            val poster = fixImg(data.stringAny("cover", *DRAMA_COVER_KEYS).ifBlank { input.poster })
            val epsArr = data.optJSONArray("episodes") ?: JSONArray()
            val eps = epsArr.objects().mapIndexed { i, o ->
                EpisodeInfo(
                    number = o.intAny("chapterNum", "episode", i + 1),
                    streaming = o.stringAny("chapterId", "id"),
                    label = o.stringAny("title")
                )
            }
            val total = max(max(data.intAny("totalEpisodes", *DRAMA_EP_KEYS, input.episodes), eps.size), 1)
            val drama = Drama(id, title, desc, poster, total, input.views, tagsOf(data).ifEmpty { input.tags }, p, input.subjectType)
            return Detail(drama, if (eps.isNotEmpty()) eps else (1..total).map { EpisodeInfo(it) })
        }

        if (p == "raptdrama") {
            val data = json.optJSONObject("data") ?: json
            val listArr = data.optJSONArray("list") ?: JSONArray()
            val eps = listArr.objects().mapIndexed { i, o ->
                EpisodeInfo(
                    number = o.intAny("idx", i + 1),
                    streaming = o.stringAny("src", "url"),
                    label = o.stringAny("msg", "title").ifBlank { "EP ${i + 1}" }
                )
            }
            val total = max(max(data.intAny("total", "count", input.episodes), eps.size), 1)
            val drama = Drama(id, input.title, input.description, input.poster, total, input.views, input.tags, p, input.subjectType)
            return Detail(drama, if (eps.isNotEmpty()) eps else (1..total).map { EpisodeInfo(it) })
        }

        if (p == "rapidtv" || p == "microdrama" || p == "dramabite") {
            val dramaObj = json.optJSONObject("drama") ?: json.optJSONObject("data") ?: json
            val title = cleanText(dramaObj.stringAny("title", *DRAMA_TITLE_KEYS).ifBlank { input.title })
            val desc = cleanText(dramaObj.stringAny("description", "synopsis", *DRAMA_DESC_KEYS).ifBlank { input.description })
            val poster = fixImg(dramaObj.stringAny("poster", "cover", *DRAMA_COVER_KEYS).ifBlank { input.poster })
            val epsArr = json.optJSONArray("episodes") ?: dramaObj.optJSONArray("episodes") ?: json.optJSONArray("data") ?: JSONArray()
            val eps = epsArr.objects().mapIndexed { i, o ->
                val vids = o.optJSONArray("videos")?.objects().orEmpty()
                val bestVid = vids.firstOrNull { it.stringAny("quality", "definition").contains("720", true) }
                    ?: vids.firstOrNull { it.stringAny("url", "video_url").startsWith("http") }
                val streamUrl = bestVid?.stringAny("url", "video_url").orEmpty().ifBlank { o.stringAny("video", "url", "video_url") }
                EpisodeInfo(
                    number = o.intAny("episode", "index", "number", i + 1),
                    streaming = streamUrl,
                    label = o.stringAny("title", "name")
                )
            }
            val total = max(max(dramaObj.intAny("totalEpisodes", "episodes", *DRAMA_EP_KEYS, input.episodes), eps.size), 1)
            val drama = Drama(id, title, desc, poster, total, input.views, tagsOf(dramaObj).ifEmpty { input.tags }, p, input.subjectType)
            return Detail(drama, if (eps.isNotEmpty()) eps else (1..total).map { EpisodeInfo(it) })
        }

        if (p == "radreels") {
            val epsArr = json.optJSONArray("data") ?: JSONArray()
            val firstEp = epsArr.optJSONObject(0)
            val title = cleanText(firstEp?.stringAny("title").orEmpty().ifBlank { input.title })
            val poster = fixImg(firstEp?.stringAny("coverImgUrl").orEmpty().ifBlank { input.poster })
            val eps = epsArr.objects().mapIndexed { i, o ->
                val vFake = o.stringAny("videoFakeId")
                val epId = o.stringAny("id", "episodicDramaId")
                EpisodeInfo(
                    number = o.intAny("seq", i + 1),
                    streaming = if (vFake.isNotBlank() && epId.isNotBlank()) "$vFake/$epId" else "",
                    label = "EP ${o.intAny("seq", i + 1)}"
                )
            }
            val total = max(eps.size, input.episodes).coerceAtLeast(1)
            val drama = Drama(id, title, input.description, poster, total, input.views, input.tags, p, input.subjectType)
            return Detail(drama, if (eps.isNotEmpty()) eps else (1..total).map { EpisodeInfo(it) })
        }

        if (p == "playlet") {
            val data = json.optJSONObject("data") ?: json
            val title = cleanText(data.stringAny("name", "title", *DRAMA_TITLE_KEYS).ifBlank { input.title })
            val desc = cleanText(data.stringAny("intro", *DRAMA_DESC_KEYS).ifBlank { input.description })
            val poster = fixImg(data.stringAny("cover", *DRAMA_COVER_KEYS).ifBlank { input.poster })
            val chArr = data.optJSONArray("chapters") ?: data.optJSONArray("episodes") ?: JSONArray()
            val eps = chArr.objects().mapIndexed { i, o ->
                val cId = o.stringAny("chapterId", "id").ifBlank { (i + 1).toString() }
                EpisodeInfo(
                    number = o.intAny("chapterId", i + 1),
                    streaming = cId,
                    label = o.stringAny("title").ifBlank { "EP ${i + 1}" }
                )
            }
            val total = max(eps.size, input.episodes).coerceAtLeast(1)
            val drama = Drama(id, title, desc, poster, total, input.views, tagsOf(data).ifEmpty { input.tags }, p, input.subjectType)
            return Detail(drama, if (eps.isNotEmpty()) eps else (1..total).map { EpisodeInfo(it, it.toString()) })
        }

        if (p == "minishort") {
            val data = json.optJSONObject("data") ?: json
            val title = cleanText(data.stringAny("video_name", *DRAMA_TITLE_KEYS).ifBlank { input.title })
            val desc = cleanText(data.stringAny("video_desc", *DRAMA_DESC_KEYS).ifBlank { input.description })
            val poster = fixImg(data.stringAny("video_img", *DRAMA_COVER_KEYS).ifBlank { input.poster })
            val groups = data.optJSONArray("episodes")?.objects().orEmpty()
            val flatList = groups.flatMap { grp ->
                grp.optJSONArray("list")?.objects() ?: listOf(grp)
            }
            val filtered = if (flatList.size > 1) {
                flatList.filterNot { it.stringAny("episodes_title").equals("Trailer", true) }
            } else flatList
            val eps = filtered.mapIndexed { i, o ->
                EpisodeInfo(
                    number = i + 1,
                    streaming = o.stringAny("episodes_id", "id"),
                    label = o.stringAny("episodes_title", "title").ifBlank { "EP ${i + 1}" }
                )
            }
            val total = max(max(data.intAny("video_num", *DRAMA_EP_KEYS, input.episodes), eps.size), 1)
            val drama = Drama(id, title, desc, poster, total, input.views, tagsOf(data).ifEmpty { input.tags }, p, input.subjectType)
            return Detail(drama, if (eps.isNotEmpty()) eps else (1..total).map { EpisodeInfo(it) })
        }

        if (p == "meloshort") {
            val epsArr = json.optJSONArray("data") ?: json.optJSONObject("data")?.optJSONArray("episodes") ?: JSONArray()
            val eps = epsArr.objects().mapIndexed { i, o ->
                EpisodeInfo(
                    number = o.intAny("chapter_index", "index", i + 1),
                    streaming = o.stringAny("chapter_id", "id"),
                    label = o.stringAny("chapter_name", "title").ifBlank { "EP ${i + 1}" }
                )
            }
            val total = max(eps.size, input.episodes).coerceAtLeast(1)
            val drama = Drama(id, input.title, input.description, input.poster, total, input.views, input.tags, p, input.subjectType)
            return Detail(drama, if (eps.isNotEmpty()) eps else (1..total).map { EpisodeInfo(it) })
        }

        if (p == "flickreels") {
            val data = json.optJSONObject("data") ?: json
            val title = cleanText(data.stringAny("title", *DRAMA_TITLE_KEYS).ifBlank { input.title })
            val desc = cleanText(
                data.optJSONObject("playlet_detail")?.stringAny("introduce", "description").orEmpty()
                    .ifBlank { data.stringAny(*DRAMA_DESC_KEYS) }
                    .ifBlank { input.description }
            )
            val poster = fixImg(data.stringAny("cover", "process_cover", *DRAMA_COVER_KEYS).ifBlank { input.poster })
            val rawList = data.optJSONArray("list")?.objects().orEmpty()
            val filtered = if (rawList.size > 1 && rawList.any { it.optInt("chapter_num", 1) > 0 }) {
                rawList.filter { it.optInt("chapter_num", 1) > 0 }
            } else rawList
            val eps = filtered.mapIndexed { i, o ->
                EpisodeInfo(
                    number = i + 1,
                    streaming = o.stringAny("chapter_id", "id"),
                    label = o.stringAny("chapter_title", "title").ifBlank { "EP ${i + 1}" }
                )
            }
            val total = max(max(data.intAny("upload_num", *DRAMA_EP_KEYS, input.episodes), eps.size), 1)
            val drama = Drama(id, title, desc, poster, total, input.views, tagsOf(data).ifEmpty { input.tags }, p, input.subjectType)
            return Detail(drama, if (eps.isNotEmpty()) eps else (1..total).map { EpisodeInfo(it) })
        }

        if (p == "flextv") {
            val data = json.optJSONObject("data") ?: json
            val title = cleanText(data.stringAny("series_name", *DRAMA_TITLE_KEYS).ifBlank { input.title })
            val desc = cleanText(data.stringAny("description", *DRAMA_DESC_KEYS).ifBlank { input.description })
            val poster = fixImg(data.stringAny("cover", *DRAMA_COVER_KEYS).ifBlank { input.poster })
            val epsJson = runCatching { getJson("$base/api/v1/series/${enc(id)}/episodes?lang=id") }.getOrNull()
            val epsArr = epsJson?.optJSONObject("data")?.optJSONArray("list") ?: data.optJSONArray("list") ?: JSONArray()
            val eps = epsArr.objects().mapIndexed { i, o ->
                val epNo = o.intAny("series_no", i + 1)
                EpisodeInfo(
                    number = epNo,
                    streaming = o.stringAny("id"),
                    label = "EP $epNo"
                )
            }
            val total = max(max(data.intAny("last_series_no", *DRAMA_EP_KEYS, input.episodes), eps.size), 1)
            val drama = Drama(id, title, desc, poster, total, input.views, tagsOf(data).ifEmpty { input.tags }, p, input.subjectType)
            return Detail(drama, if (eps.isNotEmpty()) eps else (1..total).map { EpisodeInfo(it) })
        }

        if (p == "goodshort") {
            val data = json.optJSONObject("data") ?: json
            val listArr = data.optJSONArray("list") ?: JSONArray()
            val eps = listArr.objects().mapIndexed { i, o ->
                val vPath = o.optJSONArray("cdnList")?.optJSONObject(0)?.stringAny("videoPath").orEmpty()
                EpisodeInfo(
                    number = i + 1,
                    streaming = vPath,
                    label = o.stringAny("chapterName", "title").ifBlank { "EP ${i + 1}" }
                )
            }
            val total = max(eps.size, input.episodes).coerceAtLeast(1)
            val drama = Drama(id, input.title, input.description, input.poster, total, input.views, input.tags, p, input.subjectType)
            return Detail(drama, if (eps.isNotEmpty()) eps else (1..total).map { EpisodeInfo(it) })
        }

        if (p == "happyshort") {
            val data = json.optJSONObject("data") ?: json
            val infoObj = data.optJSONArray("list")?.optJSONObject(0) ?: data
            val title = cleanText(infoObj.stringAny("video_name", *DRAMA_TITLE_KEYS).ifBlank { input.title })
            val desc = cleanText(infoObj.stringAny("introduce", *DRAMA_DESC_KEYS).ifBlank { input.description })
            val poster = fixImg(infoObj.stringAny("cover", *DRAMA_COVER_KEYS).ifBlank { input.poster })
            val epsArr = data.optJSONArray("episodes") ?: JSONArray()
            val eps = epsArr.objects().mapIndexed { i, o ->
                EpisodeInfo(
                    number = o.intAny("episodeNo", i + 1),
                    streaming = o.stringAny("episodeId", "id"),
                    label = o.stringAny("name").ifBlank { "EP ${i + 1}" }
                )
            }
            val total = max(eps.size, input.episodes).coerceAtLeast(1)
            val drama = Drama(id, title, desc, poster, total, input.views, input.tags, p, input.subjectType)
            return Detail(drama, if (eps.isNotEmpty()) eps else (1..total).map { EpisodeInfo(it) })
        }

        if (p == "cashdrama") {
            val data = json.optJSONObject("data") ?: json
            val infoObj = data.optJSONObject("info") ?: data
            val title = cleanText(infoObj.stringAny("name", *DRAMA_TITLE_KEYS).ifBlank { input.title })
            val desc = cleanText(infoObj.stringAny(*DRAMA_DESC_KEYS).ifBlank { input.description })
            val poster = fixImg(infoObj.stringAny("cover", *DRAMA_COVER_KEYS).ifBlank { input.poster })
            val total = max(infoObj.intAny("dramaCount", *DRAMA_EP_KEYS, input.episodes), 1)
            val drama = Drama(id, title, desc, poster, total, input.views, input.tags, p, input.subjectType)
            return Detail(drama, (1..total).map { EpisodeInfo(it) })
        }

        val data = json.optJSONObject("data") ?: json
        val d = normalize(data, p).let {
            it.copy(
                id = id.ifBlank { it.id },
                title = it.title.ifBlank { input.title },
                poster = fixImg(it.poster.ifBlank { input.poster }),
                description = it.description.ifBlank { input.description },
                episodes = max(it.episodes, input.episodes),
                platform = p
            )
        }
        val epsArr = data.optJSONArray("video_list")
            ?: data.optJSONArray("episode_list")
            ?: data.optJSONArray("episodes")
            ?: data.optJSONArray("chapterList")
            ?: data.optJSONArray("dramaListResponseList")
            ?: data.optJSONArray("chapters")
            ?: data.optJSONArray("list")
            ?: json.optJSONArray("episode_list")
            ?: json.optJSONArray("episodes")
        val eps = epsArr?.objects()?.mapIndexed { i, o ->
            EpisodeInfo(
                number = o.intAny("episode", "episode_no", "episode_num", "episodeNum", "episode_index", "chapterIndex", "chapter_index", "index", "number", i + 1),
                streaming = o.stringAny("streaming", "video_url", "play_url", "url", "chapter_id", "chapterId", "episode_id", "id"),
                label = o.stringAny("episode_label", "title", "label", "name", "chapter_name")
            )
        }.orEmpty()
        val total = max(d.episodes, eps.size).coerceAtLeast(1)
        return Detail(d.copy(episodes = total), if (eps.isNotEmpty()) eps else (1..total).map { EpisodeInfo(it) })
    }

    suspend fun resolveStream(d: Detail, ep: Int, ds: Boolean): StreamResult {
        val base = apiBase(d.drama.platform); val id = d.drama.id
        if (d.drama.platform == "stardusttv") {
            val json = getJson("$base/video/${enc(id)}/episode/${ep.coerceAtLeast(1)}?lang=id")
            val url = json.stringAny("video", "url", "streaming", "play_url", "video_url")
                .ifBlank { json.optJSONObject("data")?.stringAny("video", "url", "streaming", "play_url", "video_url").orEmpty() }
            if (url.isBlank()) error("Video belum tersedia")
            return StreamResult(url, pickSubtitleUrl(json))
        }
        if (d.drama.platform == "dramabox") {
            // Proxy v2: /play?bookId&episode membalas data.videoPathList
            // [{ quality: 1080|720|540, urlvideo }] lewat gerbang stream iniapi
            // (Content-Type video/mp4, URL tanpa ekstensi — ExoPlayer sniff).
            val json = getJson("$base/play?bookId=${enc(id)}&episode=${ep.coerceAtLeast(1)}&lang=in")
            val data = json.optJSONObject("data") ?: json
            val paths = (data.optJSONArray("videoPathList") ?: JSONArray()).objects()
                .filter { it.stringAny("urlvideo", "url", "videoPath").startsWith("http") }
                .sortedBy { it.optInt("quality", 0) }
            // PENTING — jangan ulangi "optimasi" memutar URL CDN hasil decode u=:
            // file dramaboxdb berakhiran .encrypt.mp4 memakai enkripsi Aliyun
            // private (`etavirp_nuyila`). Header moov-nya polos (durasi terbaca),
            // tapi bitstream h264-nya tersandi — ExoPlayer selalu "Source error".
            // Gateway iniapi men-decrypt on-the-fly (area mdat-nya terbukti beda
            // dan ter-decode bersih), jadi playback WAJIB lewat gateway meski
            // lebih lambat. Kualitas dibatasi 720p agar relay-nya tidak keteteran.
            val pick = if (ds) paths.firstOrNull()
            else paths.lastOrNull { it.optInt("quality", 0) in 1..720 } ?: paths.lastOrNull()
            val video = pick?.stringAny("urlvideo", "url", "videoPath").orEmpty()
            if (video.isNotBlank()) return StreamResult(video, pickSubtitleUrl(data, json))
            // Cadangan bentuk lama { video: "..." } bila proxy berganti lagi.
            val legacy = DramaboxCatalog.streamUrl(json)
            if (legacy.isNotBlank()) return StreamResult(legacy, pickSubtitleUrl(json))
            error("Video belum tersedia")
        }
        if (d.drama.platform == "netshort") {
            // /episode/{id}/{ep} membalas { episodeNo, episodeId, videos:[{quality,url}], subtitles }.
            // URL-nya mp4 progresif di video.netshort.com (ber-auth_key, terdeteksi
            // dari Content-Type), pilih kualitas sesuai mode hemat data.
            val json = getJson("$base/${NetshortCatalog.episodePath(id, ep)}")
            val data = json.optJSONObject("data") ?: json
            val videos = (data.optJSONArray("videos") ?: JSONArray()).objects().map { o ->
                o.stringAny("quality", "definition") to o.stringAny("url", "link")
            }
            val link = NetshortCatalog.pickVideo(videos, ds)
            if (link.isBlank()) error("Video belum tersedia")
            val netshortSub = NetshortCatalog.pickSubtitle(json) ?: NetshortCatalog.pickSubtitle(data)
            val subUrl = netshortSub?.url.orEmpty().ifBlank { pickSubtitleUrl(data, json) }
            val subMime = if (netshortSub?.format.equals("webvtt", true) || subUrl.contains("netshort.com")) {
                MimeTypes.TEXT_VTT
            } else ""
            return StreamResult(link, subUrl, subMime)
        }
        if (d.drama.platform == "dramanova") {
            // Dramanova (dramahub.be): unlock/{dramaId}/{ep}?languages=in&subtitles=true
            // membalas videos (per definisi) + subtitles (srt) dalam satu balasan.
            val json = getJson("$base/unlock/${enc(id)}/${ep.coerceAtLeast(1)}?languages=in&subtitles=true")
            val videosArr = json.optJSONArray("videos") ?: JSONArray()
            val bestVideo = videosArr.objects().maxByOrNull { o ->
                when (o.stringAny("definition").lowercase()) {
                    "1080p" -> 3; "720p" -> 2; "480p" -> 1; else -> 0
                }
            }
            val link = bestVideo?.stringAny("main_url", "backup_url").orEmpty()
            if (link.isBlank()) error("Video belum tersedia")
            val subsArr = json.optJSONArray("subtitles") ?: JSONArray()
            val subtitle = (0 until subsArr.length()).mapNotNull { i ->
                val s = subsArr.optJSONObject(i)
                val lang = s?.stringAny("language", "label").orEmpty().lowercase()
                if (lang.startsWith("id") || lang == "in") s.stringAny("url") else null
            }.firstOrNull().orEmpty()
            return StreamResult(link, subtitle)
        }
        if (d.drama.platform == "freereels") {
            // Freereels: /dramas/{key}/play/{ep}?lang=id-ID → langsung dapat stream URL
            val json = getJson("$base/dramas/${enc(id)}/play/${ep.coerceAtLeast(1)}?lang=id-ID")
            // Pilih stream: external_audio_h264_m3u8 > video_url > m3u8_url
            val link = listOf(
                json.stringAny("external_audio_h264_m3u8"),
                json.stringAny("video_url"),
                json.stringAny("m3u8_url"),
                json.stringAny("external_audio_h265_m3u8")
            ).firstOrNull { it.isNotBlank() && it.startsWith("http") } ?: ""
            if (link.isBlank()) error("Video belum tersedia")
            // Subtitle Indonesia
            val subsArr = json.optJSONArray("subtitle_list") ?: JSONArray()
            val subtitle = (0 until subsArr.length()).mapNotNull { i ->
                val s = subsArr.optJSONObject(i)
                val lang = s?.stringAny("language").orEmpty().lowercase()
                if (lang.startsWith("id") || lang == "in") s.stringAny("subtitle") else null
            }.firstOrNull().orEmpty()
            return StreamResult(link, subtitle)
        }
        if (d.drama.platform == "reelshort") {
            // ReelShort: /book/{id}/chapter/{chapterId}/video -> {data: {videos: [{PlayURL, Dpi, Encode}]}}
            val chapterId = d.episodes.firstOrNull { it.number == ep }?.streaming ?: d.episodes.getOrNull(ep - 1)?.streaming ?: error("Chapter ID tidak ditemukan")
            val json = getJson("$base/${ReelshortCatalog.videoPath(id, chapterId)}")
            val bestUrl = ReelshortCatalog.bestVideoUrl(json)
            if (bestUrl.isBlank()) error("Video belum tersedia")
            // Subtitle: cek vtt di response
            val data = json.optJSONObject("data") ?: json
            val vtt = data.optString("vtt").ifBlank { data.optJSONObject("vtt")?.optString("url").orEmpty() }
            return StreamResult(bestUrl, vtt)
        }
        if (d.drama.platform == "anichin") {
            // Anichin: /episode/{slug} -> {data: {streaming, servers: [{name, url}]}}
            // streaming bisa ok.ru embed, atau server lain. Kita coba ambil yang paling playable di ExoPlayer.
            // Prioritas: cari mp4/m3u8 langsung, kalau tidak ada, pakai streaming primary (ok.ru) - ExoPlayer bisa handle via webview? Tapi kita coba return URL embed.
            val epSlug = d.episodes.firstOrNull { it.number == ep }?.streaming ?: d.episodes.getOrNull(ep - 1)?.streaming ?: error("Episode slug tidak ditemukan")
            val json = getJson("$base/${AnichinCatalog.episodePath(epSlug)}")
            val epStream = AnichinCatalog.parseEpisode(json) ?: error("Video belum tersedia")
            // Pilih server: prioritaskan mp4/m3u8, lalu player lokal anichin yang paling stabil di ID
            // OK.ru kadang diblokir di ID / butuh VPN, jadi prioritas rendah
            val bestServer = epStream.servers.firstOrNull { it.url.contains(".m3u8") || it.url.contains(".mp4") }
                ?: epStream.servers.firstOrNull { it.url.contains("anichin-player") }
                ?: epStream.servers.firstOrNull { it.url.contains("abyssplayer") || it.url.contains("abyss") }
                ?: epStream.servers.firstOrNull { it.url.contains("turbovid") }
                ?: epStream.servers.firstOrNull { it.url.contains("dailymotion") }
                ?: epStream.servers.firstOrNull { it.url.contains("rumble") }
                ?: epStream.servers.firstOrNull { it.url.contains("ok.ru") }
                ?: epStream.servers.firstOrNull { it.name.contains("Primary", true) }
                ?: epStream.servers.firstOrNull()
            val url = bestServer?.url?.takeIf { it.isNotBlank() } ?: epStream.primaryUrl
            if (url.isBlank()) error("Video belum tersedia")
            // Anichin tidak punya subtitle terpisah di API, biasanya hardsub Indonesia
            return StreamResult(url)
        }
        if (ExtraPlatforms.isExtraPlatform(d.drama.platform)) {
            return resolveExtraPlatformStream(d, ep, ds)
        }
        val multiVideoJson = runCatching { getJson("$base/multi-video?id=${enc(id)}&lang=id") }.getOrNull()
        val list = multiVideoJson?.optJSONArray("episodes")
            ?: multiVideoJson?.optJSONArray("video_list")
            ?: multiVideoJson?.optJSONObject("data")?.optJSONArray("episodes")
            ?: JSONArray()
        val epObj = list.objects().firstOrNull { it.intAny("index", "episode", "episode_no", 0) == ep } ?: list.optJSONObject(ep - 1)
        val source = epObj?.stringAny("stream_url", "streaming", "url", "play_url", "video_url").orEmpty()
        if (source.isBlank()) error("Video belum tersedia")
        return StreamResult(source)
    }

    private suspend fun resolveExtraPlatformStream(d: Detail, ep: Int, ds: Boolean): StreamResult {
        val p = d.drama.platform
        val base = apiBase(p)
        val id = d.drama.id
        val epInfo = d.episodes.firstOrNull { it.number == ep } ?: d.episodes.getOrNull(ep - 1)
        val stored = epInfo?.streaming.orEmpty().trim()

        fun storedResult(value: String = stored): StreamResult? {
            if (!value.startsWith("http://") && !value.startsWith("https://")) return null
            if (value.contains("##")) {
                val parts = value.split("##", limit = 2)
                return StreamResult(cleanUrl(parts[0]), cleanUrl(parts.getOrElse(1) { "" }))
            }
            return StreamResult(cleanUrl(value))
        }

        // Enam sumber ini mengirim URL episode langsung di endpoint detail.
        // Ambil dari cache detail ber-TTL 5 menit, bukan Detail lama yang masih
        // tertahan di layar; token provider hanya hidup sekitar 30–70 menit.
        if (p in setOf("sodareels", "dotdrama", "shorten", "raptdrama", "rapidtv", "microdrama")) {
            val refreshed = loadDetailCached(d.drama)
            val freshEp = refreshed.episodes.firstOrNull { it.number == ep } ?: refreshed.episodes.getOrNull(ep - 1)
            storedResult(freshEp?.streaming.orEmpty())?.let { return it }
            error("Video belum tersedia")
        }

        if (p == "playlet") {
            val chapId = stored.ifBlank { ep.coerceAtLeast(1).toString() }
            return StreamResult("$base/theater/${enc(id)}/open/$chapId/playlist.m3u8")
        }

        if (p == "wetv") {
            val vid = stored.ifBlank {
                val epsJson = runCatching { getJson("$base/api/episodes?cid=${enc(id)}&lang=id&country=ID") }.getOrNull()
                epsJson?.optJSONArray("data")?.optJSONObject(ep - 1)?.stringAny("vid", "id").orEmpty()
            }
            if (vid.isBlank()) error("Episode WeTV belum tersedia")
            val json = getJson("$base/api/play?vid=${enc(vid)}&cid=${enc(id)}&lang=id&country=ID")
            val data = json.optJSONObject("data") ?: json
            val stream = extractStreamUrl(json, ds)
            if (stream.isBlank()) {
                val msg = data.stringAny("msg", "message")
                error(msg.ifBlank { "Video WeTV belum tersedia" })
            }
            return StreamResult(stream, pickSubtitleUrl(data, json))
        }

        if (p == "snackshort") {
            val parts = stored.split("|||")
            val chapId = parts[0].ifBlank {
                val chJson = runCatching { getJson("$base/api/v1/book/${enc(id)}/chapters?lang=Indonesian") }.getOrNull()
                val arr = chJson?.optJSONObject("data")?.optJSONArray("data") ?: chJson?.optJSONArray("data")
                arr?.optJSONObject(ep - 1)?.stringAny("chapter_id", "id").orEmpty()
            }
            val fallbackUrl = parts.getOrNull(1).orEmpty()
            val json = if (chapId.isNotBlank()) {
                runCatching { getJson("$base/api/v1/book/${enc(id)}/episode/${enc(chapId)}?lang=Indonesian") }.getOrNull()
            } else null
            val stream = json?.let { extractStreamUrl(it, ds) }.orEmpty()
            if (stream.isNotBlank()) return StreamResult(stream, pickSubtitleUrl(json))
            if (fallbackUrl.startsWith("http")) return StreamResult(fallbackUrl)
            if (ep == 1) {
                val bookJson = runCatching { getJson("$base/api/v1/book/${enc(id)}?lang=Indonesian") }.getOrNull()
                val bookStream = bookJson?.let { extractStreamUrl(it, ds) }.orEmpty()
                if (bookStream.isNotBlank()) return StreamResult(bookStream, pickSubtitleUrl(bookJson))
            }
            error("Video belum tersedia")
        }

        if (p == "shotshort") {
            val chapId = stored.ifBlank {
                val epsJson = runCatching { getJson("$base/api/book/${enc(id)}/episodes?lang=id") }.getOrNull()
                epsJson?.optJSONObject("data")?.optJSONArray("episodes")?.optJSONObject(ep - 1)?.stringAny("chapterId", "id").orEmpty()
            }
            val json = getJson("$base/api/book/${enc(id)}/chapter/${enc(chapId.ifBlank { ep.toString() })}?lang=id")
            val stream = extractStreamUrl(json, ds)
            if (stream.isBlank()) error("Video belum tersedia")
            return StreamResult(stream, pickSubtitleUrl(json, json.optJSONObject("data")))
        }

        if (p == "shortwave") {
            val chapId = stored.ifBlank {
                val detJson = runCatching { getJson("$base/api/drama/${enc(id)}?lang=id") }.getOrNull()
                detJson?.optJSONObject("data")?.optJSONArray("episodes")?.optJSONObject(ep - 1)?.stringAny("chapter_id", "id").orEmpty()
            }
            val json = getJson("$base/api/stream/${enc(id)}/${enc(chapId.ifBlank { ep.toString() })}?lang=id")
            val stream = extractStreamUrl(json, ds)
            if (stream.isBlank()) error("Video belum tersedia")
            return StreamResult(stream, pickSubtitleUrl(json, json.optJSONObject("data")))
        }

        if (p == "meloshort") {
            val chapId = stored.ifBlank {
                val epsJson = runCatching { getJson("$base/api/v1/dramas/${enc(id)}/episodes?page=1&limit=100&lang=id") }.getOrNull()
                epsJson?.optJSONArray("data")?.optJSONObject(ep - 1)?.stringAny("chapter_id", "id").orEmpty()
            }
            val json = getJson("$base/api/v1/dramas/${enc(id)}/episodes/${enc(chapId.ifBlank { ep.toString() })}?lang=id")
            val stream = extractStreamUrl(json, ds)
            if (stream.isBlank()) error("Video belum tersedia")
            return StreamResult(stream, pickSubtitleUrl(json, json.optJSONObject("data")))
        }

        if (p == "minishort") {
            val epId = stored.ifBlank {
                val infoJson = runCatching { getJson("$base/api/video_info?video_id=${enc(id)}&language=en") }.getOrNull()
                val groups = infoJson?.optJSONObject("data")?.optJSONArray("episodes")?.objects().orEmpty()
                val flatList = groups.flatMap { it.optJSONArray("list")?.objects() ?: listOf(it) }
                    .filterNot { it.stringAny("episodes_title").equals("Trailer", true) }
                flatList.getOrNull(ep - 1)?.stringAny("episodes_id", "id").orEmpty()
            }
            val json = getJson("$base/api/episodes_content?video_id=${enc(id)}&episodes_id=${enc(epId.ifBlank { ep.toString() })}&language=en")
            val stream = extractStreamUrl(json, ds)
            if (stream.isBlank()) error("Video belum tersedia")
            return StreamResult(stream, pickSubtitleUrl(json, json.optJSONObject("data")))
        }

        if (p == "radreels") {
            val pathPart = stored.takeIf { it.contains("/") } ?: run {
                val epsJson = runCatching { getJson("$base/api/v1/episodes/${enc(id)}?lang=id") }.getOrNull()
                val o = epsJson?.optJSONArray("data")?.optJSONObject(ep - 1)
                if (o != null) "${o.stringAny("videoFakeId")}/${o.stringAny("id")}" else ""
            }
            if (pathPart.isBlank()) error("Episode RadReels tidak ditemukan")
            val json = getJson("$base/api/v1/video/$pathPart?lang=id")
            val stream = extractStreamUrl(json, ds)
            if (stream.isBlank()) error("Video belum tersedia")
            return StreamResult(stream, pickSubtitleUrl(json))
        }

        if (p == "flickreels") {
            val chapId = stored.ifBlank {
                val chJson = runCatching { getJson("$base/api/v1/chapters/${enc(id)}?lang=id") }.getOrNull()
                val list = chJson?.optJSONObject("data")?.optJSONArray("list")?.objects().orEmpty()
                    .filter { it.optInt("chapter_num", 1) > 0 }
                list.getOrNull(ep - 1)?.stringAny("chapter_id", "id").orEmpty()
            }
            val json = getJson("$base/api/v1/stream/${enc(id)}/${enc(chapId.ifBlank { ep.toString() })}?lang=id")
            val stream = extractStreamUrl(json, ds)
            if (stream.isBlank()) error("Video belum tersedia")
            return StreamResult(stream, pickSubtitleUrl(json, json.optJSONObject("data")))
        }

        if (p == "flextv") {
            val secId = stored.ifBlank {
                val epsJson = runCatching { getJson("$base/api/v1/series/${enc(id)}/episodes?lang=id") }.getOrNull()
                epsJson?.optJSONObject("data")?.optJSONArray("list")?.optJSONObject(ep - 1)?.stringAny("id").orEmpty()
            }
            val json = getJson("$base/api/v1/play/${enc(id)}/${enc(secId.ifBlank { ep.toString() })}?lang=id")
            val stream = extractStreamUrl(json, ds)
            if (stream.isBlank()) error("Video belum tersedia")
            return StreamResult(stream, pickSubtitleUrl(json, json.optJSONObject("data")))
        }

        if (p == "goodshort") {
            val json = getJson("$base/api/v1/unlock/${enc(id)}?q=${if (ds) "540p" else "720p"}")
            val epObj = json.optJSONArray("episodes")?.optJSONObject(ep - 1)
            val stream = epObj?.optJSONArray("videos")?.optJSONObject(0)?.stringAny("m3u8", "url").orEmpty()
                .ifBlank { epObj?.stringAny("url", "m3u8").orEmpty() }
                .ifBlank { extractStreamUrl(json, ds) }
            if (stream.isBlank()) error("Video belum tersedia")
            return StreamResult(stream)
        }

        val url = ExtraPlatforms.streamUrl(p, id, ep, base)
        if (url != null) {
            // Beberapa proxy (mis. LuminaReels/Playlet) memang mengembalikan
            // playlist langsung, bukan JSON.
            if (url.substringBefore('?').endsWith(".m3u8", ignoreCase = true)) {
                return StreamResult(url)
            }
            val json = runCatching { getJson(url) }.getOrNull()
            if (json != null) {
                val stream = extractStreamUrl(json, ds)
                if (stream.isNotBlank()) return StreamResult(stream, pickSubtitleUrl(json, json.optJSONObject("data")))
            }
        }

        // Cadangan terakhir untuk provider yang menyisipkan stream di detail.
        // Jalur endpoint di atas selalu dicoba lebih dulu agar URL bertanda
        // tangan tidak diambil dari Detail lama.
        storedResult()?.let { return it }
        error("Video belum tersedia")
    }

    private suspend fun getJson(url: String, jsonBody: String? = null): JSONObject = withContext(Dispatchers.IO) {
        // Origin di balik proxy kadang balas 5xx sementara.
        // Retry singkat supaya home tidak langsung error.
        var last: Throwable? = null
        repeat(3) { attempt ->
            if (attempt > 0) delay(450L * attempt)
            try {
                val reqBuilder = Request.Builder().url(url)
                    .header("User-Agent", "DramakuNative/4.9.9 Android")
                    .header("Accept", "application/json, text/plain, */*")
                    .header("Cache-Control", "no-cache, no-store")
                    .header("Pragma", "no-cache")
                if (jsonBody != null) {
                    reqBuilder.post(jsonBody.toRequestBody("application/json; charset=utf-8".toMediaType()))
                }
                if (url.contains("dramahub.be")) {
                    reqBuilder.header("Authorization", HUB_BEARER)
                }
                return@withContext client.newCall(reqBuilder.build()).execute().use { r ->
                    val body = r.body?.string().orEmpty().trim()
                    val json = when {
                        body.startsWith("[") -> runCatching { JSONObject().put("data", JSONArray(body)) }.getOrNull()
                        body.startsWith("{") -> runCatching { JSONObject(body) }.getOrNull()
                        else -> null
                    }
                    if (!r.isSuccessful) {
                        val msg = json?.stringAny("error_msg", "error", "message", "msg").orEmpty()
                        val fallback = when {
                            r.code in 500..599 -> "Server sumber sedang gangguan (HTTP ${r.code}). Coba lagi nanti atau pindah sumber."
                            r.code == 404 -> "Endpoint sumber tidak ditemukan (404). Sumber ini mungkin sedang dirombak penyedianya."
                            else -> "HTTP ${r.code}"
                        }
                        error(msg.ifBlank { fallback })
                    }
                    val parsed = json ?: JSONObject(body)
                    val code = parsed.optInt("code", 200)
                    if (code >= 400) error(parsed.stringAny("error_msg", "error", "message", "msg").ifBlank { "HTTP $code" })
                    val innerData = parsed.optJSONObject("data")
                    val innerCode = innerData?.optInt("code", 200) ?: 200
                    if (innerCode >= 400 && innerData != null && !innerData.has("list") && !innerData.has("episodes")) {
                        error(innerData.stringAny("error_msg", "error", "message", "msg").ifBlank { "HTTP $innerCode" })
                    }
                    val statusZero = parsed.has("status") && parsed.optInt("status", 1) == 0
                    val msg = parsed.stringAny("message", "msg")
                    val explicitSuccess = parsed.optBoolean("success", false)
                        || msg.equals("success", true)
                        || msg.equals("ok", true)
                        || msg.contains("成功")
                        || parsed.has("data")
                        || parsed.has("rows")
                    if (statusZero && !explicitSuccess) error(parsed.stringAny("error_msg", "error", "message", "msg").ifBlank { "Gagal memuat data" })
                    parsed
                }
            } catch (e: CancellationException) {
                throw e
            } catch (t: Throwable) {
                last = t
            }
        }
        throw (last ?: IllegalStateException("Gagal memuat data"))
    }
}

private enum class HomeSection { Recommended, Popular, Newest }
private data class HomePageRequest(val section: HomeSection, val url: String, val virtualPage: Int, val hasMore: Boolean)

private fun homePageRequest(p: String, page: Int): HomePageRequest {
    val pr = pagesFor(p); val sections = listOf(HomeSection.Popular, HomeSection.Newest, HomeSection.Recommended)
    val total = pr.count() * sections.size; val vp = page.coerceIn(1, total.coerceAtLeast(1))
    val section = sections[(vp - 1) % sections.size]; val rp = pr.first + ((vp - 1) / sections.size)
    val urls = homeUrls(p, rp)
    val url = when (section) { HomeSection.Recommended -> urls[0]; HomeSection.Popular -> urls[1]; HomeSection.Newest -> urls[2] }
    return HomePageRequest(section, url, vp, vp < total)
}

// API Melolo (dramahub.be) mengabaikan offset/page/session: tiap halaman
// mengembalikan feed 18 judul yang sama, jadi jangan fetch ulang (hemat
// kuota + waktu). Dramanova dan Freereels juga dibatasi 1 halaman.
private fun pagesFor(p: String): IntRange = 1..1

private fun homeUrls(p: String, page: Int): List<String> {
    val base = apiBase(p)
    ExtraPlatforms.homeUrls(p, page, base)?.let { return it }
    // Proxy DramaBox v2 (dramahub.be/dramaboxv2/api/v1): /home ber-page, /rank
    // untuk peringkat. Kode Bahasa Indonesia tetap "in".
    if (p == "dramabox") {
        return listOf("$base/home?lang=in&page=$page", "$base/rank?lang=in", "$base/home?lang=in&page=${page + 1}")
    }
    // NetShort: daftar NetShort punya jalur sendiri (loadNetshortHome), jadi
    // tiga URL ini jarang dipakai — hanya saat pemanggil generik terlanjur minta.
    if (p == "netshort") {
        return listOf(
            "$base/${NetshortCatalog.EXPLORE_PATH}",
            "$base/${NetshortCatalog.FEED_PATH}",
            "$base/${NetshortCatalog.NEW_PATH}"
        )
    }
    if (p == "dramanova") {
        // Dramanova (dramahub.be): discover feed + modul home (hot & new).
        // Katalog lengkap (drama/list) dipakai rak genre "Katalog".
        return listOf(
            "$base/discover/feed?pageNum=1&pageSize=20&languages=in",
            "$base/home/modules/dramanova_hot/items?pageNum=1&pageSize=20&languages=in",
            "$base/home/modules/dramanova_new/items?pageNum=1&pageSize=20&languages=in"
        )
    }
    if (p == "freereels") {
        // Freereels: foryou (recommended), popular, new
        return listOf(
            "$base/foryou?page=1&lang=id-ID",
            "$base/popular?page=0&lang=id-ID",
            "$base/new?page=0&lang=id-ID"
        )
    }
    if (p == "reelshort") {
        // ReelShort: foryou (recommended), new, completed, romance, drama - 3 utama dipakai home
        return listOf(
            "$base/${ReelshortCatalog.FORYOU_PATH}",
            "$base/${ReelshortCatalog.NEW_PATH}",
            "$base/${ReelshortCatalog.COMPLETED_PATH}"
        )
    }
    if (p == "anichin") {
        // Anichin: home (popular+latest), ongoing, completed
        return listOf(
            "$base/${AnichinCatalog.HOME_PATH}",
            "$base/${AnichinCatalog.ONGOING_PATH}",
            "$base/${AnichinCatalog.COMPLETED_PATH}"
        )
    }
    // Melolo (dramahub.be): bookmall = feed trending, rank = daftar peringkat,
    // bookmall/tabs = kumpulan judul lain di tab genre.
    return listOf("$base/bookmall?lang=id", "$base/rank?page=1&lang=id", "$base/bookmall/tabs?gender=0&lang=id")
}

private fun detailUrl(d: Drama): String {
    ExtraPlatforms.detailUrl(d.platform, d.id, apiBase(d.platform))?.let { return it }
    return when (d.platform) {
    "stardusttv" -> "${apiBase(d.platform)}/video/${enc(d.id)}?lang=id"
    "dramabox" -> "${apiBase(d.platform)}/book?id=${enc(d.id)}&lang=in"
    "netshort" -> "${apiBase(d.platform)}/${NetshortCatalog.detailPath(d.id)}"
    "reelshort" -> "${apiBase(d.platform)}/${ReelshortCatalog.detailPath(d.id)}"
    "anichin" -> "${apiBase(d.platform)}/${AnichinCatalog.detailPath(d.id)}"
    "dramanova" -> "${apiBase(d.platform)}/detail/${enc(d.id)}?languages=in"
    "freereels" -> "${apiBase(d.platform)}/dramas/${enc(d.id)}?lang=id-ID"
    else -> "${apiBase(d.platform)}/book?id=${enc(d.id)}&lang=id"
    }
}

private fun dedupe(items: List<Drama>) = items.filter { it.id.isNotBlank() && it.title.isNotBlank() }.distinctBy { it.platform + "|" + it.id }.distinctBy { it.platform + "|" + normalizeKey(it.title) }

private fun extractStreamUrl(json: JSONObject, ds: Boolean): String {
    val data = json.optJSONObject("data")?.optJSONObject("data") ?: json.optJSONObject("data") ?: json

    // 1. Cek obj video: { video_720, video_1080, video_480 } (ShortMax, ShortWave)
    val vidObj = data.optJSONObject("video") ?: data.optJSONObject("videoUrl") ?: json.optJSONObject("video")
    if (vidObj != null) {
        val u = if (ds) {
            vidObj.stringAny("video_480", "video_540", "video_720", "video_1080", "url", "play_url")
        } else {
            vidObj.stringAny("video_720", "video_1080", "video_480", "url", "play_url")
        }
        if (u.startsWith("http")) return cleanUrl(u)
    }

    // 2. Cek array videoUrl / videoUrls: [{url, quality}] (StoryReel, FlareFlow, DramaRush, VibeShort)
    val vidArr = data.optJSONArray("videoUrl") ?: data.optJSONArray("videoUrls") ?: data.optJSONArray("videos") ?: json.optJSONArray("videos")
    if (vidArr != null) {
        val objs = vidArr.objects()
        val chosen = if (ds) {
            objs.firstOrNull { it.stringAny("quality", "definition", "dpi").contains("480", true) || it.stringAny("quality", "definition").contains("540", true) }
                ?: objs.firstOrNull { it.stringAny("url", "play_url", "video_url", "m3u8").startsWith("http") }
        } else {
            objs.firstOrNull { it.stringAny("quality", "definition", "dpi").contains("720", true) }
                ?: objs.firstOrNull { it.stringAny("quality", "definition", "dpi").contains("1080", true) }
                ?: objs.firstOrNull { it.stringAny("url", "play_url", "video_url", "m3u8").startsWith("http") }
        }
        val u = chosen?.stringAny("url", "play_url", "video_url", "m3u8", "PlayURL").orEmpty()
        if (u.startsWith("http")) return cleanUrl(u)
    }

    // 3. Cek m3u8s array (ShortBox)
    val m3u8s = data.optJSONArray("m3u8s") ?: json.optJSONArray("m3u8s")
    if (m3u8s != null && m3u8s.length() > 0) {
        val u = m3u8s.optString(0)
        if (u.startsWith("http")) return cleanUrl(u)
    }

    // 4. Cek streams array (MeloShort: pilih encrypted == false)
    val streams = data.optJSONArray("streams") ?: json.optJSONArray("streams")
    if (streams != null) {
        val objs = streams.objects()
        val unenc = objs.filter { !it.optBoolean("encrypted", false) }
        val pool = unenc.ifEmpty { objs }
        val chosen = pool.firstOrNull { it.stringAny("quality", "definition").contains("720", true) }
            ?: pool.firstOrNull { it.stringAny("url", "play_url").startsWith("http") }
        val u = chosen?.stringAny("url", "play_url").orEmpty()
        if (u.startsWith("http")) return cleanUrl(u)
    }

    // 5. Cek adaptive array (BonusTV)
    val adaptive = data.optJSONArray("adaptive") ?: json.optJSONArray("adaptive")
    if (adaptive != null) {
        val u = adaptive.objects().firstOrNull()?.stringAny("url", "play_url").orEmpty()
        if (u.startsWith("http")) return cleanUrl(u)
    }

    // 6. Cek episode.play_info_list (IDrama)
    val playInfoList = data.optJSONObject("episode")?.optJSONArray("play_info_list") ?: data.optJSONArray("play_info_list")
    if (playInfoList != null) {
        val objs = playInfoList.objects()
        val chosen = objs.firstOrNull { it.stringAny("definition", "quality").contains("720", true) }
            ?: objs.firstOrNull { it.stringAny("play_url", "main_url", "url").startsWith("http") }
        val u = chosen?.stringAny("play_url", "main_url", "backup_url", "url").orEmpty()
        if (u.startsWith("http")) return cleanUrl(u)
    }

    // 7. Cek videoFiles array (DramaPops)
    val vFiles = data.optJSONArray("videoFiles") ?: json.optJSONArray("videoFiles")
    if (vFiles != null) {
        val u = vFiles.objects().firstOrNull()?.stringAny("url", "play_url").orEmpty()
        if (u.startsWith("http")) return cleanUrl(u)
    }

    // 8. Cek direct string keys pada data dan json
    val directKeys = arrayOf(
        "hdUrl", "sdUrl", "videoUrl", "video_url", "playUrl", "play_url", "hls_url", "hlsUrl",
        "stream_url", "streamUrl", "m3u8_url", "m3u8", "mediaUrl", "resourceLink", "source",
        "main_url", "backup_url", "video", "url", "sd_264_url", "hd_264_url", "sd_265_url", "hd_265_url"
    )
    val direct = data.stringAny(*directKeys).takeIf { it.startsWith("http") }
        ?: json.stringAny(*directKeys).takeIf { it.startsWith("http") }
    if (!direct.isNullOrBlank()) return cleanUrl(direct)

    return ""
}

private fun mergeHomeBundles(c: HomeBundle, n: HomeBundle) = HomeBundle(dedupe(c.recommended + n.recommended), dedupe(c.popular + n.popular), dedupe(c.newest + n.newest), max(c.loadedPage, n.loadedPage), n.hasMore)

// Feed Melolo memuat rak ("Trending"), tab genre ("Peringkat", "Time Travel"),
// dan section layer yang sama-sama punya pasangan id+name. Mereka bukan drama.
// Syaratnya pakai sinyal konten nyata: cover, sinopsis, atau jumlah episode.
// Kunci field generik untuk semua platform (lama + 35 baru dari dramahub.be).
// Tiap upstream menamai field beda-beda (playlet_id, video_id, seriesId, cid,
// bahkan key ter-obfuscate seperti dcup/dlit), jadi semua alias dipungut di sini.
private val DRAMA_ID_KEYS = arrayOf(
    "book_id", "bookId", "drama_id", "subjectId", "id", "key", "season_id", "aid",
    "dramaId", "t_book_id", "slug", "playlet_id", "playletId", "video_id", "videoId", "seriesId",
    "series_id", "contentId", "content_id", "shortPlayCode", "shortplay_id",
    "shortPlayId", "theater_id", "compilationsFakeId", "compilationsId", "cid", "vid", "dcup", "dlit", "code"
)
private val DRAMA_TITLE_KEYS = arrayOf(
    "book_name", "bookName", "drama_name", "title", "book_title", "bookTitle", "name",
    "shortTitle", "anime_title", "playlet_title", "video_name", "videoName",
    "seriesName", "series_name", "contentName", "content_name", "shortPlayName",
    "short_play_name", "theater_name", "playletName", "nseri", "nmeasu", "vod_name",
    "drama_title", "subjectName", "subject_name", "rwill"
)
private val DRAMA_COVER_KEYS = arrayOf(
    "cover", "thumb_url", "image", "poster", "coverWap", "bookCover", "posterImg",
    "cover_url", "posterUri", "book_pic", "bookPic", "coverUrl", "picUrl",
    "cover_image", "cover_img", "video_img", "coverImgUrl", "contentCoverUrl",
    "coverId", "pday", "pbat", "poster_url", "vertical_cover", "verticalCover",
    "coverImage", "img", "img_url", "imgUrl", "imageUrl", "pic", "pic_url",
    "thumbnail", "thumbnail_url", "headImg", "head_img", "vertical_image",
    "coverImg", "hcover", "vcover", "drama_cover", "icon", "posterVt", "posterHz",
    "horizontalImage", "process_cover"
)
private val DRAMA_DESC_KEYS = arrayOf(
    "introduction", "description", "meta_description", "meta_sinopsis", "shoot",
    "content", "synopsis", "abstract", "desc", "evaluate", "special_desc", "dwill",
    "ddet", "recommendIntro", "summary", "intro", "brief", "plot", "introduce",
    "drama_description", "video_desc", "des"
)
private val DRAMA_EP_KEYS = arrayOf(
    "serial_count", "chapter_count", "chapterCount", "episode_count", "meta_episode",
    "total_episodes", "totalEpisodes", "episode_num", "episodes", "freeEpisodes",
    "chapter_num", "totalEpisode", "uploadOfEpisodes", "current_count", "total_num",
    "eps", "ewood", "ewin", "episodeCount", "dramaCount", "upload_num", "last_series_no",
    "video_num", "chapters", "onlineChapter", "episode", "total"
)
private val DRAMA_SIGNAL_KEYS = listOf(
    "subjectId", "key", "season_id", "ep_id", "aid", "shortPlayCode", "compilationsFakeId", "compilationsId",
    "theater_id", "shortplay_id", "cid", "playlet_id", "playletId", "video_id", "seriesId", "series_id",
    "contentId", "dcup", "dlit", "drama_id", "vid"
)

private fun JSONObject.hasDramaSignal(): Boolean =
    stringAny(*DRAMA_COVER_KEYS).isNotBlank() ||
        optJSONObject("cover") != null ||
        optJSONObject("images") != null ||
        stringAny(*DRAMA_DESC_KEYS).isNotBlank() ||
        intAny(*DRAMA_EP_KEYS, 0) > 0 ||
        DRAMA_SIGNAL_KEYS.any { has(it) }

private fun extractDramaId(o: JSONObject, fp: String): String {
    return when (fp) {
        "shortmax" -> o.stringAny("code", "id")
        "storyreel", "flareflow" -> o.stringAny("shortPlayCode", "shortPlayId", "id")
        "dramarush", "vibeshort" -> o.stringAny("shortPlayId", "id", "shortPlayCode")
        "flextv" -> o.stringAny("series_id", "id")
        "radreels" -> o.stringAny("compilationsFakeId", "compilationsId", "id")
        "shortsky" -> o.optJSONObject("redirect_param")?.stringAny("playletId", "playlet_id").orEmpty()
            .ifBlank { o.stringAny("playletId", "playlet_id", "id") }
        "shortbox" -> o.stringAny("shortplay_id", "id")
        "meloshort", "shortwave" -> o.stringAny("drama_id", "id")
        "shorten" -> o.stringAny("slug", "id")
        "playlet" -> o.stringAny("theater_id", "id")
        "moboreels" -> o.stringAny("seriesId", "id")
        "wetv" -> o.stringAny("cid", "id")
        "cashdrama", "raptdrama" -> o.stringAny("vid", "id")
        "sereal" -> o.stringAny("contentId", "id")
        "dotdrama" -> o.stringAny("dcup", "id")
        "sodareels" -> o.stringAny("dlit", "id")
        "minishort", "happyshort" -> o.stringAny("video_id", "id")
        "flickreels" -> o.stringAny("playlet_id", "id")
        "goodshort", "shotshort", "snackshort", "reelbuzz" -> o.stringAny("bookId", "book_id", "id")
        else -> o.stringAny(*DRAMA_ID_KEYS)
    }.ifBlank { o.stringAny(*DRAMA_ID_KEYS) }
}

private fun flat(any: Any?, fp: String): List<Drama> {
    val out = mutableListOf<Drama>()
    val skipKeys = setOf("categories", "category_info", "episodes", "episode_list", "video_list", "chapterList", "chapters", "seasons", "right_float")
    fun extractBooks(node: Any?) {
        when (node) {
            is JSONArray -> {
                // Array dapat berisi drama langsung (NetShort) atau potongan
                // JSON mentah; yang bukan objek diurai ulang supaya array
                // bertingkat tetap tembus.
                node.objects().forEach { extractBooks(it) }
                (0 until node.length()).forEach { i ->
                    val v = node.opt(i)
                    if (v is String) runCatching { extractBooks(JSONObject(v)) }
                }
            }
            is JSONObject -> {
                val bookId = extractDramaId(node, fp)
                val bookName = node.stringAny(*DRAMA_TITLE_KEYS)
                if (bookId.isNotBlank() && bookName.isNotBlank() && node.hasDramaSignal()) {
                    val d = normalize(node, fp)
                    if (d.id.isNotBlank() && d.title.isNotBlank() && !d.title.equals("Populer", true) && !d.title.equals("Romansa", true) && !d.title.equals("Ceo", true)) {
                        out += d
                    }
                }
                val keys = node.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    if (k !in skipKeys) {
                        val v = node.opt(k)
                        if (v is JSONObject || v is JSONArray) {
                            extractBooks(v)
                        }
                    }
                }
            }
        }
    }
    extractBooks(any)
    return out
        // Proxy DramaBox v2 mencampur buku katalog cppartner (prefix 42…, bisa
        // diputar) dengan buku 41… yang selalu dijawab "书籍找不到了" oleh upstream.
        // Buang yang mati supaya pengguna tidak membuka judul tanpa video.
        .filterNot { it.platform == "dramabox" && it.id.startsWith("41") }
        .distinctBy { it.platform + "|" + it.id }
}

private fun normalize(o: JSONObject, fp: String): Drama {
    val p = fp
    val id = extractDramaId(o, p)
    val epCount = o.intAny(*DRAMA_EP_KEYS, "episode_number", "chapterCnt", "totalChapterNum", 0)
        .let {
            if (it > 0) it
            else o.optJSONObject("data")?.optJSONObject("id_id")?.intAny("episode", 0)
                ?: o.optJSONObject("id_id")?.intAny("episode", 0)
                ?: 0
        }
    return Drama(
        id,
        cleanText(o.stringAny(*DRAMA_TITLE_KEYS)),
        cleanText(o.stringAny(*DRAMA_DESC_KEYS)),
        fixImg(o.stringAny(*DRAMA_COVER_KEYS).ifBlank { o.coverUrl() }),
        // totalChapterNum = nama field jumlah episode di balasan /search DramaBox.
        epCount,
        o.stringAny("watch_value", "hotCode", "viewCountDisplay", "hits", "viewers", "views").ifBlank {
            val fc = o.optLong("follow_count", 0)
            if (fc > 0) "${fc/1000}K" else o.optJSONObject("rankVo")?.stringAny("hotCode").orEmpty()
        },
        tagsOf(o),
        p,
        o.intAny("subjectType", 1)
    )
}

private fun tagsOf(o: JSONObject): List<String> {
    val out = mutableListOf<String>()
    fun add(a: JSONArray?) { a?.let { for (i in 0 until it.length()) when (val v = it.opt(i)) { is JSONObject -> out += v.stringAny("tagName", "name", "title"); else -> out += v?.toString().orEmpty() } } }
    add(o.optJSONArray("tags")); add(o.optJSONArray("tagV3s")); add(o.optJSONArray("categories")); add(o.optJSONArray("theme"))
    // ReelShort tag_list = [{tag_name}]
    o.optJSONArray("tag_list")?.let { arr ->
        for (i in 0 until arr.length()) {
            val jo = arr.optJSONObject(i) ?: continue
            val n = jo.optString("tag_name").trim()
            if (n.isNotBlank()) out.add(n)
        }
    }
    o.stringAny("category", "genre").split(",").map { it.trim() }.filter { it.isNotBlank() }.forEach { out += it }
    return out.map { it.trim() }.filter { it.isNotBlank() }.distinct().take(8)
}

private fun dedupeAndRank(items: List<Drama>, query: String): List<Drama> {
    val q = normalizeKey(query); val seen = HashSet<String>()
    return items.filter { seen.add(it.platform + "|" + it.id) }.distinctBy { normalizeKey(it.title) }.sortedByDescending { d ->
        val t = normalizeKey(d.title); var s = 0
        if (t == q) s += 100 else if (t.startsWith(q)) s += 70 else if (t.contains(q)) s += 45
        q.split(" ").filter { it.isNotBlank() }.forEach { if (t.contains(it)) s += 8 }
        if (d.poster.isNotBlank()) s += 3; if (d.episodes > 0) s += 2; s
    }
}

// ─────────────────────────────────────────────────────────────────
// LOCAL STORE
// ─────────────────────────────────────────────────────────────────

private class LocalStore(ctx: Context) {
    private val p = ctx.getSharedPreferences("dramaku_native", Context.MODE_PRIVATE)
    fun platform() = p.getString("platform", "melolo") ?: "melolo"
    fun setPlatform(id: String) = p.edit().putString("platform", id).apply()
    fun categoryPlatform(cat: String, fallback: String) = p.getString("cat_platform_$cat", fallback) ?: fallback
    fun setCategoryPlatform(cat: String, platformId: String) = p.edit().putString("cat_platform_$cat", platformId).apply()
    fun dataSaver() = p.getBoolean("dataSaver", false)
    fun setDataSaver(v: Boolean) = p.edit().putBoolean("dataSaver", v).apply()
    fun autoNext() = p.getBoolean("autoNext", true)
    fun setAutoNext(v: Boolean) = p.edit().putBoolean("autoNext", v).apply()
    fun fitContain() = p.getBoolean("fitContain", true)
    fun setFitContain(v: Boolean) = p.edit().putBoolean("fitContain", v).apply()
    fun resumeReminder() = p.getBoolean(ResumeReminder.PREF_ENABLED, false)
    fun setResumeReminder(v: Boolean) = p.edit().putBoolean(ResumeReminder.PREF_ENABLED, v).apply()
    fun playerHintShown() = p.getBoolean("playerHintShown", false)
    fun setPlayerHintShown() = p.edit().putBoolean("playerHintShown", true).apply()
    fun subtitleOn() = p.getBoolean("subtitleOn", true)
    fun setSubtitleOn(v: Boolean) = p.edit().putBoolean("subtitleOn", v).apply()
    fun playbackSpeed() = p.getFloat("playbackSpeed", 1f).takeIf { it in listOf(0.75f, 1f, 1.25f, 1.5f, 2f) } ?: 1f
    fun setPlaybackSpeed(v: Float) = p.edit().putFloat("playbackSpeed", v).apply()

    fun history(tick: Int = 0): List<HistoryItem> = runCatching {
        val a = JSONArray(p.getString("history", "[]") ?: "[]")
        (0 until a.length()).mapNotNull { i -> a.optJSONObject(i)?.let { o -> HistoryItem(o.stringAny("id"), o.stringAny("title"), o.stringAny("poster"), o.stringAny("platform"), o.intAny("episode", 1), o.optLong("pos", 0), o.optLong("dur", 0), o.optLong("updated", 0)) } }.sortedByDescending { it.updated }
    }.getOrDefault(emptyList())

    fun saveHistory(drama: Drama, ep: Int) {
        val a = JSONArray(); val cur = history(); val prev = cur.firstOrNull { it.id == drama.id && it.platform == drama.platform }
        val old = cur.filterNot { it.id == drama.id && it.platform == drama.platform }.toMutableList()
        val keep = prev != null && prev.episode == ep
        old.add(0, HistoryItem(drama.id, drama.title.ifBlank { prev?.title.orEmpty() }, drama.poster.ifBlank { prev?.poster.orEmpty() }, drama.platform, ep, pos = if (keep) prev?.pos ?: 0 else 0, dur = if (keep) prev?.dur ?: 0 else 0))
        old.take(80).forEach { a.put(JSONObject().apply { put("id", it.id); put("title", it.title); put("poster", it.poster); put("platform", it.platform); put("episode", it.episode); put("pos", it.pos); put("dur", it.dur); put("updated", it.updated) }) }
        p.edit().putString("history", a.toString()).apply()
    }

    fun updateProgress(id: String, platform: String, ep: Int, pos: Long, dur: Long) {
        val prefix = ProgressKeys.episodePrefix(platform, id, ep)
        val sp = pos.coerceAtLeast(0); val sd = dur.coerceAtLeast(0)
        val ed = p.edit().putLong(prefix + "pos", sp).putLong(prefix + "dur", sd)
        val list = history().toMutableList(); val idx = list.indexOfFirst { it.id == id && it.platform == platform }
        if (idx >= 0) { list[idx] = list[idx].copy(episode = ep, pos = sp, dur = sd, updated = System.currentTimeMillis()); val a = JSONArray(); list.sortedByDescending { it.updated }.forEach { a.put(JSONObject().apply { put("id", it.id); put("title", it.title); put("poster", it.poster); put("platform", it.platform); put("episode", it.episode); put("pos", it.pos); put("dur", it.dur); put("updated", it.updated) }) }; ed.putString("history", a.toString()) }
        ed.apply()
    }

    fun progressMs(id: String, platform: String, ep: Int): Long {
        val prefix = ProgressKeys.episodePrefix(platform, id, ep); val saved = p.getLong(prefix + "pos", -1)
        if (saved >= 0) return saved
        val h = history().firstOrNull { it.id == id && it.platform == platform && it.episode == ep }
        val lp = ProgressKeys.legacyEpisodePrefix(id, ep); val legacy = p.getLong(lp + "pos", -1)
        if (legacy >= 0 && h != null) { p.edit().putLong(prefix + "pos", legacy).putLong(prefix + "dur", p.getLong(lp + "dur", 0).coerceAtLeast(0)).apply(); return legacy }
        return h?.pos ?: 0
    }

    fun progressDurationMs(id: String, platform: String, ep: Int): Long {
        val prefix = ProgressKeys.episodePrefix(platform, id, ep); val saved = p.getLong(prefix + "dur", -1)
        if (saved >= 0) return saved.coerceAtLeast(0)
        val h = history().firstOrNull { it.id == id && it.platform == platform && it.episode == ep }
        val lp = ProgressKeys.legacyEpisodePrefix(id, ep); val ld = p.getLong(lp + "dur", -1)
        return when { ld >= 0 && h != null -> ld.coerceAtLeast(0); h != null -> h.dur.coerceAtLeast(0); else -> 0 }
    }

    fun clearHistory() { val ed = p.edit().remove("history"); p.all.keys.filter { it.startsWith("progress_") }.forEach { ed.remove(it) }; ed.apply() }

    fun removeHistory(id: String, platform: String) {
        val a = JSONArray()
        history().filterNot { it.id == id && it.platform == platform }.forEach {
            a.put(JSONObject().apply { put("id", it.id); put("title", it.title); put("poster", it.poster); put("platform", it.platform); put("episode", it.episode); put("pos", it.pos); put("dur", it.dur); put("updated", it.updated) })
        }
        val prefix = ProgressKeys.dramaPrefix(platform, id)
        val ed = p.edit().putString("history", a.toString())
        p.all.keys.filter { it.startsWith(prefix) }.forEach { ed.remove(it) }
        ed.apply()
    }

    fun removeFav(id: String, platform: String) {
        val a = JSONArray()
        favs().filterNot { it.id == id && it.platform == platform }.forEach {
            a.put(JSONObject().apply { put("id", it.id); put("title", it.title); put("description", it.description); put("poster", it.poster); put("episodes", it.episodes); put("views", it.views); put("platform", it.platform); put("subjectType", it.subjectType); put("tags", JSONArray(it.tags)) })
        }
        p.edit().putString("favs", a.toString()).apply()
    }

    private fun savedDramas(key: String): List<Drama> = runCatching { val a = JSONArray(p.getString(key, "[]")); (0 until a.length()).mapNotNull { i -> a.optJSONObject(i)?.let { o -> Drama(o.stringAny("id"), o.stringAny("title"), o.stringAny("description"), o.stringAny("poster"), o.intAny("episodes", 0), o.stringAny("views"), o.optJSONArray("tags")?.let { arr -> (0 until arr.length()).mapNotNull { arr.optString(it).takeIf { s -> s.isNotBlank() } } }.orEmpty(), o.stringAny("platform").ifBlank { "melolo" }, o.intAny("subjectType", 1)) } } }.getOrDefault(emptyList())
    fun favs(): List<Drama> = savedDramas("favs")
    fun likes(): List<Drama> = savedDramas("likes")
    fun isLiked(id: String, platform: String) = likes().any { it.id == id && it.platform == platform }
    fun toggleLike(drama: Drama) {
        val list = likes().toMutableList()
        val index = list.indexOfFirst { it.id == drama.id && it.platform == drama.platform }
        if (index >= 0) list.removeAt(index) else list.add(0, drama)
        saveLikes(list)
    }
    fun removeLike(id: String, platform: String) = saveLikes(likes().filterNot { it.id == id && it.platform == platform })
    fun clearLikes() = p.edit().remove("likes").apply()
    private fun saveLikes(values: List<Drama>) {
        val array = JSONArray()
        values.take(120).forEach { drama ->
            array.put(JSONObject().apply {
                put("id", drama.id); put("title", drama.title); put("description", drama.description)
                put("poster", drama.poster); put("episodes", drama.episodes); put("views", drama.views)
                put("platform", drama.platform); put("subjectType", drama.subjectType); put("tags", JSONArray(drama.tags))
            })
        }
        p.edit().putString("likes", array.toString()).apply()
    }
    fun isFav(id: String, platform: String) = favs().any { it.id == id && it.platform == platform }
    fun toggleFav(d: Drama) { val list = favs().toMutableList(); val idx = list.indexOfFirst { it.id == d.id && it.platform == d.platform }; if (idx >= 0) list.removeAt(idx) else list.add(0, d); val a = JSONArray(); list.take(120).forEach { a.put(JSONObject().apply { put("id", it.id); put("title", it.title); put("description", it.description); put("poster", it.poster); put("episodes", it.episodes); put("views", it.views); put("platform", it.platform); put("subjectType", it.subjectType); put("tags", JSONArray(it.tags)) }) }; p.edit().putString("favs", a.toString()).apply() }
    fun clearFavs() = p.edit().remove("favs").apply()
    fun recentSearches(): List<String> = JSONArray(p.getString("recent", "[]") ?: "[]").let { a -> (0 until a.length()).mapNotNull { a.optString(it).takeIf { s -> s.isNotBlank() } } }
    fun saveRecent(q: String) { val list = recentSearches().filterNot { it.equals(q, true) }.toMutableList(); list.add(0, q); val a = JSONArray(); list.take(10).forEach { a.put(it) }; p.edit().putString("recent", a.toString()).apply() }
    fun removeRecentSearch(q: String) { val a = JSONArray(); recentSearches().filterNot { it.equals(q, true) }.forEach { a.put(it) }; p.edit().putString("recent", a.toString()).apply() }
    fun clearRecentSearches() = p.edit().remove("recent").apply()
}

// ─────────────────────────────────────────────────────────────────
// JSON HELPERS
// ─────────────────────────────────────────────────────────────────

private fun JSONObject.dataOrSelf(): Any = opt("data")?.takeUnless { it == JSONObject.NULL } ?: this
private fun JSONArray.objects(): List<JSONObject> = (0 until length()).mapNotNull { optJSONObject(it) }
private fun JSONObject.stringAny(vararg keys: String): String { keys.forEach { k -> val v = opt(k); if (v != null && v != JSONObject.NULL) { if (v is String && v.isNotBlank()) return v.trim(); if (v !is JSONObject && v !is JSONArray && v.toString().isNotBlank()) return v.toString().trim() } }; return "" }
private fun JSONObject.intAny(vararg keys: Any): Int { var fb = 0; keys.forEach { k -> if (k is Int) fb = k; else if (k is String && has(k)) { val v = opt(k); val n = when (v) { is Number -> v.toInt(); is String -> v.filter { it.isDigit() }.toIntOrNull() ?: 0; else -> 0 }; if (n != 0) return n } }; return fb }
// Subtitle: tiap sumber menamai listnya beda-beda (captions/subtitles/subs/...),
// isinya pun bisa {lan,url} atau {language,link}. Dipungut semua lalu diurut:
// Indonesia dulu, lalu Inggris, terakhir entri apa pun yang ada.
private val SUB_LIST_KEYS = arrayOf("extCaptions", "captions", "caption_list", "captionList", "subtitles", "subtitle_list", "subtitleList", "subs")
private val SUB_URL_KEYS = arrayOf("url", "link", "subtitle", "src", "file", "resourceLink")
private val SUB_LANG_KEYS = arrayOf("lan", "lang", "language", "lanName", "languageName", "langName", "name", "label")

private fun pickSubtitleUrl(vararg roots: JSONObject?): String {
    val found = mutableListOf<Pair<String, String>>() // bahasa (lowercase) ke url
    // Cek root-nya sendiri plus satu level anak objek (mis. data.playInfo.captions).
    val scan = roots.filterNotNull().flatMap { r ->
        listOf(r) + r.keys().asSequence().mapNotNull { k -> r.optJSONObject(k) }.toList()
    }
    scan.forEach { root ->
        SUB_LIST_KEYS.forEach { key ->
            val arr = root.optJSONArray(key) ?: return@forEach
            arr.objects().forEach { o ->
                val url = cleanUrl(o.stringAny(*SUB_URL_KEYS))
                if (url.startsWith("http") && !url.contains(".m3u8", true)) {
                    found += o.stringAny(*SUB_LANG_KEYS).lowercase().trim() to url
                }
            }
        }
    }
    if (found.isEmpty()) return ""
    fun isIndo(l: String): Boolean {
        val norm = l.replace('_', '-')
        return norm == "id" || norm == "in" || norm == "ind" || norm.startsWith("id-") || norm.startsWith("indonesia") || norm.contains("bahasa")
    }
    fun isEng(l: String): Boolean {
        val norm = l.replace('_', '-')
        return norm == "en" || norm == "eng" || norm.startsWith("en-") || norm.startsWith("english")
    }
    return found.firstOrNull { isIndo(it.first) }?.second
        ?: found.firstOrNull { isEng(it.first) }?.second
        ?: found.first().second
}

private fun JSONObject.coverUrl(): String {
    val c = opt("cover")
    if (c is JSONObject) {
        val u = c.stringAny("url", "image", "cover")
        if (u.isNotBlank()) return u
    }
    val imgs = optJSONObject("images")
    if (imgs != null) {
        val u = imgs.stringAny("image", "cover", "poster", "url")
        if (u.isNotBlank()) return u
    }
    return ""
}
private fun fixImg(u: String): String {
        if (u.contains("fizzopic.org") && u.contains(".heic")) {
            val m = Regex("novel-images-apsoutheast/([a-f0-9]+)~").find(u)
            if (m != null) return "https://p19-novel-sg.ibyteimg.com/img/novel-images-sg/${m.groupValues[1]}~tplv-resize:570:810.jpg"
        }
        // Anichin: ganti domain lama ke anichin.moe yang terbukti 200 OK
        if (u.contains("anichin.be") || u.contains("anichin.care") || u.contains("anichin.cafe") || u.contains("anichin.academy")) {
            return u.replace("https://anichin.be", "https://anichin.moe")
                .replace("http://anichin.be", "https://anichin.moe")
                .replace("https://anichin.care", "https://anichin.moe")
                .replace("https://anichin.cafe", "https://anichin.moe")
                .replace("https://anichin.academy", "https://anichin.moe")
        }
        if (u.startsWith("/wp-content")) {
            return "https://anichin.moe$u"
        }
        if (u.startsWith("/")) {
            // Coba prefix anichin domain untuk poster relatif
            return "https://anichin.moe$u"
        }
        return u
    }
private fun cleanText(s: String) = s.replace(Regex("<[^>]+>"), " ").replace("&nbsp;", " ").replace(Regex("\\s+"), " ").trim()
private fun cleanUrl(u: String): String {
    val t = u.trim()
    val raw = if (t.startsWith("[") && t.contains("](http")) {
        Regex("\\]\\((https?://[^)]+)\\)").find(t)?.groupValues?.getOrNull(1) ?: t
    } else t
    return raw
        .replace("&amp;", "&")
        .replace("\\u0026", "&")
        .replace("http://sulao.montagehub.xyz", "https://sulao.montagehub.xyz")
        .replace("http://media.shorten.watch", "https://media.shorten.watch")
        .replace(" ", "%20")
}
private fun enc(s: String) = URLEncoder.encode(s, "UTF-8")
private fun normalizeKey(s: String) = s.lowercase().replace(Regex("[^a-z0-9\\p{L}\\s]"), " ").replace(Regex("\\s+"), " ").trim()
