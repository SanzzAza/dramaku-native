package com.dramaku.app

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.Intent
import android.graphics.Color as AndroidColor
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.input.pointer.pointerInput
import coil.compose.AsyncImage
import coil.Coil
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.dramaku.app.data.NativeRemoteConfig
import com.dramaku.app.data.RemoteConfigRepository
import com.dramaku.app.home.Greetings
import com.dramaku.app.home.HomeCategory
import com.dramaku.app.storage.ProgressKeys
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
import kotlinx.coroutines.withTimeoutOrNull
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.common.PlaybackException
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MergingMediaSource
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.ui.PlayerView
import androidx.media3.ui.AspectRatioFrameLayout
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okhttp3.Request
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
// TAMPILAN — "sinema premium": gelap sinematik, kontras tinggi,
// aksen mint elektrik. Tipografi: Fraunces (display) + Jakarta Sans.
// Terinspirasi WeTV / iQIYI / ShortTV — bersih, fokus konten.
// ─────────────────────────────────────────────────────────────────

private object DS {
    // Sinematik gelap — depth & contrast
    val Bg = Color(0xFF0A0908)
    val Raise = Color(0xFF131110)
    val Card = Color(0xFF1A1714)
    val Card2 = Color(0xFF221E18)
    val Line = Color(0x0DF5E8D0)
    val LineStrong = Color(0x20F5E8D0)

    // Merek — mint elektrik
    val Green = Color(0xFF2EE8A0)
    val GreenDeep = Color(0xFF0FAD6E)
    val GreenWash = Color(0xFF2EE8A0).copy(alpha = 0.10f)
    val Cream = Color(0xFFF5E8D0)
    val Ink = Color(0xFF131110)

    // Teks — hierarchy jelas
    val Hi = Color(0xFFF5F0E8)
    val Body = Color(0xFFB8B0A0)
    val Muted = Color(0xFF787068)
    val Faint = Color(0xFF4A443D)

    // Semantik
    val Red = Color(0xFFEF5350)
    val RedWash = Color(0xFFEF5350).copy(alpha = 0.12f)
    val Gold = Color(0xFFD4A853)
    val Rating = Color(0xFFFFB300)

    // Gradasi poster & hero
    val PosterFade = listOf(Color(0x000A0908), Color(0x800A0908))
    val HeroFade = listOf(Color(0x100A0908), Color(0x500A0908), Color(0xFF0A0908))
    val SheetBg = Color(0xE8131110)
    val CardGradient = listOf(Color(0xFF1A1714), Color(0xFF221E18))
}

private object Type {
    val Sans = FontFamily(
        Font(R.font.jakarta_regular, FontWeight.Normal),
        Font(R.font.jakarta_medium, FontWeight.Medium),
        Font(R.font.jakarta_semibold, FontWeight.SemiBold),
        Font(R.font.jakarta_bold, FontWeight.Bold)
    )
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
                .build()
        )
        window.statusBarColor = AndroidColor.rgb(10, 9, 8)
        window.navigationBarColor = AndroidColor.rgb(10, 9, 8)
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
    Clips("Cuplikan", Icons.Rounded.PlayCircle, false),
    Home("Beranda", Icons.Rounded.Home),
    Rewards("Hadiah", Icons.Rounded.CardGiftcard, false),
    Library("Koleksi", Icons.Rounded.Bookmark),
    Profile("Profil", Icons.Rounded.Person),
    Search("Cari", Icons.Rounded.Search)
}

private data class PlatformInfo(val id: String, val label: String, val base: String, val logoUrl: String = "", val logoRes: Int = 0)
private data class Drama(
    val id: String, val title: String, val description: String = "", val poster: String = "",
    val episodes: Int = 0, val views: String = "", val tags: List<String> = emptyList(),
    val platform: String = "melolo", val subjectType: Int = 1
)
private data class EpisodeInfo(val number: Int, val streaming: String = "", val label: String = "", val locked: Boolean = false, val se: Int = 1, val subtitle: String = "")
private data class Detail(val drama: Drama, val episodes: List<EpisodeInfo> = emptyList())
private data class HomeBundle(val recommended: List<Drama>, val popular: List<Drama>, val newest: List<Drama>, val loadedPage: Int = 1, val hasMore: Boolean = true)
private data class StreamResult(val url: String, val subtitle: String = "")
private data class CachedStream(val result: StreamResult, val expiresAtMs: Long)
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
    PlatformInfo("melolo", "Melolo", "https://captain.sapimu.au/melolo/api/v1", logoRes = R.drawable.logo_melolo),
    PlatformInfo("dramanova", "Dramanova", "https://captain.sapimu.au/dramanova/api/v1"),
    PlatformInfo("freereels", "FreeReels", "https://captain.sapimu.au/freereels/api/v1"),
    PlatformInfo("dramabox", "DramaBox", "https://captain.sapimu.au/dramaboxbaru/api"),
    PlatformInfo("bstation", "Bstation", "https://captain.sapimu.au/bstation/api"),
    PlatformInfo("moviebox", "MovieBox", "https://captain.sapimu.au/moviebox/api"),
    PlatformInfo("mbshorts", "Shorts", "https://captain.sapimu.au/moviebox/api")
)

private fun platform(id: String) = Platforms.firstOrNull { it.id == id } ?: Platforms.first()
private fun platformLabel(id: String) = platform(id).label
private fun apiBase(id: String) = platform(id).base

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
    var selPlatform by remember { mutableStateOf(store.platform()) }
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
    var category by remember { mutableStateOf<HomeCategory?>(null) }
    var showSettings by remember { mutableStateOf(false) }
    var genreRows by remember { mutableStateOf<List<Pair<String, List<Drama>>>>(emptyList()) }
    var showPlatformPicker by remember { mutableStateOf(false) }

    val playerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val data = result.data
        if (result.resultCode == Activity.RESULT_OK && data != null) {
            val id = data.getStringExtra(PlayerActivity.RESULT_DRAMA_ID).orEmpty()
            val pid = data.getStringExtra(PlayerActivity.RESULT_PLATFORM).orEmpty()
            val ep = data.getIntExtra(PlayerActivity.RESULT_EPISODE, 1)
            val pos = data.getLongExtra(PlayerActivity.RESULT_POSITION, 0L)
            val dur = data.getLongExtra(PlayerActivity.RESULT_DURATION, 0L)
            if (id.isNotBlank() && pid.isNotBlank()) {
                store.updateProgress(id, pid, ep, pos, dur)
                dataTick++
            }
        }
    }

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
    // jauh lebih dalam), DramaBox lewat kategori asli endpoint-nya (browse + gems).
    LaunchedEffect(homeState) {
        val ok = (homeState as? Load.Ok)?.data
        if (ok == null) { genreRows = emptyList(); return@LaunchedEffect }
        val known = (ok.popular + ok.newest + ok.recommended).map { it.platform + "|" + it.id }.toSet()
        val wanted: List<Pair<String, suspend () -> List<Drama>>> = when {
            selPlatform == "dramabox" -> listOf(
                "Permata tersembunyi" to { repo.browsePath(selPlatform, "hidden-gems?lang=in") },
                "Kekuatan super" to { repo.browsePath(selPlatform, "browse?type=433&page=1&lang=in") },
                "Kawin kontrak" to { repo.browsePath(selPlatform, "browse?type=454&page=1&lang=in") },
                "Melawan balik" to { repo.browsePath(selPlatform, "browse?type=462&page=1&lang=in") },
                "Kelahiran kembali" to { repo.browsePath(selPlatform, "browse?type=450&page=1&lang=in") },
                "Balas dendam" to { repo.browsePath(selPlatform, "browse?type=458&page=1&lang=in") },
                "Cinta pahit" to { repo.browsePath(selPlatform, "browse?type=449&page=1&lang=in") },
                "Perjalanan waktu" to { repo.browsePath(selPlatform, "browse?type=451&page=1&lang=in") }
            )
            selPlatform == "moviebox" -> listOf(
                "K-Drama" to { repo.browsePath(selPlatform, "tabs/category-content?type=4380734070238626200&lang=id") },
                "C-Drama" to { repo.browsePath(selPlatform, "tabs/category-content?type=173752404280836544&lang=id") },
                "Anime" to { repo.browsePath(selPlatform, "tabs/category-content?type=62133389738001440&lang=id") },
                "Action" to { repo.browsePath(selPlatform, "tabs/category-content?type=6978603205429526968&lang=id") },
                "Romance" to { repo.browsePath(selPlatform, "tabs/category-content?type=2389813900859556536&lang=id") },
                "Comedy" to { repo.browsePath(selPlatform, "tabs/category-content?type=8785384881686725944&lang=id") }
            )
            // Shorts: feed sudah dua rak dari reel/trending, tanpa kategori tambahan.
            selPlatform == "mbshorts" -> emptyList()
            selPlatform == "dramanova" -> listOf(
                "Terpopuler" to { repo.browseDramanova("dramanova_hot") },
                "Terbaru" to { repo.browseDramanova("dramanova_new") },
                "Lainnya" to { repo.browseDramanova("dramanova_more") },
                "Preview" to { repo.browseDramanova("dramanova_previews") },
                "Gratis" to { repo.browseDramanova("dramanova_free") },
                "Animasi" to { repo.browseDramanova("Dramanova_Animation") }
            )
            selPlatform == "freereels" -> listOf(
                "Untuk Wanita" to { repo.browseFreereels("female") },
                "Untuk Pria" to { repo.browseFreereels("male") },
                "Anime" to { repo.browseFreereels("anime") },
                "Dubbing" to { repo.browseFreereels("dubbing") },
                "Segera Tayang" to { repo.browseFreereels("coming-soon") }
            )
            selPlatform == "bstation" -> listOf(
                "Terpopuler" to { repo.browseBstation() },
                "Hot-Blooded" to { repo.browseBstationGenre("20006") },
                "Romance" to { repo.browseBstationGenre("20019") },
                "Isekai" to { repo.browseBstationGenre("20007") },
                "Action" to { repo.browseBstationGenre("20011") },
                "Fantasy" to { repo.browseBstationGenre("20010") }
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
    // Back dari halaman kategori kembali ke layar awal kategori (pintu masuk)
    BackHandler(enabled = category != null && !showSettings && selectedDrama == null && playerSession == null && clipFeedItems.isEmpty()) { category = null }

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
                        BottomNavBar(tab) { target ->
                            if (target == Tab.Home && tab == Tab.Home) homeScrollToTop++ else tab = target
                        }
                    }
                ) { pad ->
                    Box(Modifier.padding(pad).fillMaxSize()) {
                        when (tab) {
                            Tab.Home -> HomeScreen(
                                platformId = selPlatform, scrollToTopSignal = homeScrollToTop, state = homeState,
                                category = activeCat, onExitCategory = { category = null },
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
                            Tab.Rewards -> PlaceholderScreen("Hadiah", "Halaman ini masih kami siapkan, sabar sebentar ya.", Icons.Rounded.CardGiftcard)
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
            // Filter platform sesuai kategori aktif
            val pickerPlatforms = category?.let { cat ->
                Platforms.filter { cat.platforms.contains(it.id) }
            } ?: Platforms.filter { it.id in listOf("melolo", "dramanova", "freereels", "dramabox") }

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
    Column(
        Modifier
            .fillMaxWidth()
            .background(DS.Raise.copy(alpha = 0.96f))
    ) {
        // Top gradient line
        Box(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color.Transparent, DS.LineStrong, DS.Green.copy(alpha = 0.15f), DS.LineStrong, Color.Transparent)
                    )
                )
        )
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val navTabs = Tab.values().filter { it.showNav }
            // Split: left side (Home, Search), center (Clips), right side (Library, Profile)
            val leftTabs = navTabs.take(2)
            val rightTabs = navTabs.drop(2)

            leftTabs.forEach { tab ->
                NavItem(tab, tab == selected, onSelect, Modifier.weight(1f))
            }

            // Center Clips button — prominent, elevated
            val clipsActive = selected == Tab.Clips
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onSelect(Tab.Clips) }
                    .padding(vertical = 2.dp)
            ) {
                Box(
                    Modifier
                        .size(width = 52.dp, height = 34.dp)
                        .clip(RoundedCornerShape(17.dp))
                        .background(
                            if (clipsActive) Brush.verticalGradient(listOf(DS.Green, DS.Green))
                            else Brush.verticalGradient(listOf(DS.Green.copy(alpha = 0.9f), DS.GreenDeep))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Tab.Clips.icon, Tab.Clips.label,
                        tint = if (clipsActive) DS.Ink else DS.Ink,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    Tab.Clips.label,
                    color = if (clipsActive) DS.Green else DS.Muted,
                    fontSize = 10.sp,
                    fontFamily = Type.Sans,
                    fontWeight = if (clipsActive) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 1
                )
            }

            rightTabs.forEach { tab ->
                NavItem(tab, tab == selected, onSelect, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun NavItem(tab: Tab, active: Boolean, onSelect: (Tab) -> Unit, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable { onSelect(tab) }
            .padding(vertical = 2.dp)
    ) {
        Box(
            Modifier
                .size(width = 44.dp, height = 30.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(if (active) DS.GreenWash else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Icon(tab.icon, tab.label, tint = if (active) DS.Green else DS.Faint, modifier = Modifier.size(22.dp))
            if (active) {
                // Active dot indicator
                Box(
                    Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(DS.Green)
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 2.dp)
                )
            }
        }
        Spacer(Modifier.height(2.dp))
        Text(
            tab.label,
            color = if (active) DS.Hi else DS.Faint,
            fontSize = 10.sp,
            fontFamily = Type.Sans,
            fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium,
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
    // Color palette untuk platform tanpa logo
    val platformColors = mapOf(
        "melolo" to Color(0xFFFFCC00),
        "dramanova" to Color(0xFF2EE8A0),
        "freereels" to Color(0xFF7C4DFF),
        "dramabox" to Color(0xFFFF4081),
        "moviebox" to Color(0xFF00BCD4),
        "mbshorts" to Color(0xFFFF6E40)
    )

    BackHandler { onDismiss() }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0x990A0908))
            .clickable { onDismiss() }
    ) {
        Surface(
            color = DS.Raise,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            Column(
                Modifier
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
                            .size(24.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
                platform.logoUrl.isNotBlank() -> {
                    AsyncImage(
                        platform.logoUrl,
                        platform.label,
                        Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        contentScale = ContentScale.Fit
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

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize().background(DS.Bg),
        contentPadding = PaddingValues(bottom = 24.dp)
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

                if (all.isEmpty()) {
                    item { EmptyState("Rak masih kosong", "Coba muat ulang, atau pindah sumber dulu.", Icons.Rounded.Movie) }
                } else {
                    item { HeroCard(all.first(), onDrama) }
                    if (history.isNotEmpty()) item {
                        Section("Lanjutkan menonton", "Persis di tempat kamu berhenti.") {
                            LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(history.take(8), key = { it.platform + it.id }) { watched -> ContinueCard(watched, onResume) }
                            }
                        }
                    }

                    genreRows.forEach { (label, shelfItems) ->
                        item(key = "shelf_$label") {
                            Section(label) {
                                LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    items(shelfItems, key = { it.platform + it.id }) { d ->
                                        DiscoverDramaCard(drama = d, isNew = false, onClick = onDrama, modifier = Modifier.width(124.dp))
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 30.dp, bottom = 14.dp), verticalAlignment = Alignment.Bottom) {
                            Column(Modifier.weight(1f)) {
                                Text("Jelajahi ${platformLabel(platformId)}", color = DS.Hi, fontSize = 20.sp, fontFamily = Type.Sans, fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.height(3.dp))
                                Text("${all.size} judul siap diputar", color = DS.Muted, fontSize = 12.sp, fontFamily = Type.Sans)
                            }
                            Row(
                                Modifier.clip(RoundedCornerShape(50)).clickable(onClick = onRandom).padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Rounded.Shuffle, "Acak", tint = DS.Green, modifier = Modifier.size(15.dp))
                                Spacer(Modifier.width(5.dp))
                                Text("Acak", color = DS.Green, fontSize = 12.5.sp, fontFamily = Type.Sans, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    all.drop(1).chunked(2).forEachIndexed { index, row ->
                        item(key = "grid_${platformId}_${data.loadedPage}_$index") {
                            Row(
                                Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
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
                            Spacer(Modifier.height(18.dp))
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
}

// ─────────────────────────────────────────────────────────────────
// LAYAR AWAL — pintu masuk, gaya indeks majalah
// ─────────────────────────────────────────────────────────────────

@Composable
private fun CategoryHomeScreen(onSelect: (HomeCategory) -> Unit, onSettings: () -> Unit) {
    val ctx = LocalContext.current
    val greeting = remember { Greetings.forHour(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) }
    val date = remember { todayLine() }
    Column(
        Modifier
            .fillMaxSize()
            .background(DS.Bg)
            .verticalScroll(rememberScrollState())
    ) {
        // Header area
        Column(Modifier.padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(48.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                BrandMark(Modifier.size(40.dp))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("DRAMAKU", color = DS.Hi, fontSize = 14.sp, fontFamily = Type.Sans, fontWeight = FontWeight.Bold, letterSpacing = 4.sp)
                    Spacer(Modifier.height(2.dp))
                    Text(date, color = DS.Muted, fontSize = 11.5.sp, fontFamily = Type.Sans)
                }
                GhostIconButton(Icons.Rounded.Settings, "Pengaturan", onSettings)
            }

            Spacer(Modifier.height(52.dp))
            Text("${greeting.text}.", color = DS.Hi, fontSize = 34.sp, lineHeight = 38.sp, fontFamily = Type.Sans, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.5).sp)
            Spacer(Modifier.height(8.dp))
            Text(
                "Pilih kategori tontonanmu.",
                color = DS.Muted,
                fontSize = 14.sp,
                lineHeight = 21.sp,
                fontFamily = Type.Sans
            )
        }

        Spacer(Modifier.height(36.dp))

        // Main categories — card-based, full width
        Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            CategoryCard(
                icon = Icons.Rounded.Smartphone,
                title = "Short Drama",
                subtitle = "Drama vertikal episode pendek",
                accentColor = DS.Green,
                onClick = { onSelect(HomeCategory.ShortDrama) }
            )
            CategoryCard(
                icon = Icons.Rounded.Movie,
                title = HomeCategory.MovieDrama.title,
                subtitle = HomeCategory.MovieDrama.subtitle,
                accentColor = DS.Gold,
                onClick = { onSelect(HomeCategory.MovieDrama) }
            )
            CategoryCard(
                icon = Icons.Rounded.Theaters,
                title = HomeCategory.MovieBox.title,
                subtitle = HomeCategory.MovieBox.subtitle,
                accentColor = Color(0xFF8B5CF6),
                onClick = { onSelect(HomeCategory.MovieBox) }
            )
        }

        // Coming soon / next category
        Spacer(Modifier.height(32.dp))
        Column(Modifier.padding(horizontal = 20.dp)) {
            Text("KATEGORI LAIN", color = DS.Faint, fontSize = 10.sp, fontFamily = Type.Sans, fontWeight = FontWeight.SemiBold, letterSpacing = 2.5.sp)
            Spacer(Modifier.height(12.dp))
            CategoryCard(
                icon = Icons.Rounded.AutoAwesome,
                title = HomeCategory.Bstation.title,
                subtitle = HomeCategory.Bstation.subtitle,
                accentColor = Color(0xFF00A1D6),
                onClick = { onSelect(HomeCategory.Bstation) }
            )
        }

        // Support card
        Spacer(Modifier.height(28.dp))
        Column(Modifier.padding(horizontal = 20.dp)) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(DS.Card)
                    .border(1.dp, DS.Line, RoundedCornerShape(14.dp))
                    .clickable { Toast.makeText(ctx, "Link dukungan segera ditambahkan", Toast.LENGTH_SHORT).show() }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(DS.Gold.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.LocalCafe, null, tint = DS.Gold, modifier = Modifier.size(17.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Traktir kopi", color = DS.Hi, fontSize = 13.sp, fontFamily = Type.Sans, fontWeight = FontWeight.SemiBold)
                    Text("Biar servernya tetap menyala.", color = DS.Muted, fontSize = 11.sp, fontFamily = Type.Sans)
                }
                Icon(Icons.Rounded.NorthEast, null, tint = DS.Faint, modifier = Modifier.size(15.dp))
            }
        }
        Spacer(Modifier.height(40.dp))
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
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DS.Card)
            .border(1.dp, DS.Line, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon circle
        Box(
            Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, title, tint = accentColor, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = DS.Hi, fontSize = 16.sp, fontFamily = Type.Sans, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(3.dp))
            Text(subtitle, color = DS.Muted, fontSize = 12.sp, lineHeight = 16.sp, fontFamily = Type.Sans, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        Spacer(Modifier.width(8.dp))
        Box(
            Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.ArrowForward, null, tint = accentColor, modifier = Modifier.size(16.dp))
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
    Box(modifier.clip(RoundedCornerShape(13.dp)).background(DS.GreenDeep), contentAlignment = Alignment.Center) {
        Icon(Icons.Rounded.PlayArrow, "Dramaku", tint = DS.Ink, modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun GhostIconButton(icon: ImageVector, label: String, onClick: () -> Unit) {
    Box(
        Modifier
            .size(41.dp)
            .clip(CircleShape)
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
    val selectedState = remoteConfig?.platform(platformId)
    val online = selectedState?.enabled ?: true
    val alert = remoteConfig?.message?.takeIf { it.enabled }?.let { listOf(it.title, it.text).filter { it.isNotBlank() }.joinToString(" · ") }
        ?: remoteError?.let { "Status server: $it" }
        ?: if (!online) "${platformLabel(platformId)} sedang gangguan" else ""

    Column(Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 4.dp)) {
        // Top row: title + actions
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (category != null) {
                        Box(
                            Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(DS.Card)
                                .border(1.dp, DS.Line, CircleShape)
                                .clickable(onClick = onExitCategory),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.ArrowBack, "Kembali", tint = DS.Body, modifier = Modifier.size(16.dp))
                        }
                        Spacer(Modifier.width(10.dp))
                    }
                    Column {
                        Text(
                            category?.title ?: "Dramaku",
                            color = DS.Hi,
                            fontSize = 24.sp,
                            fontFamily = Type.Sans,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = (-0.3).sp
                        )
                        Text(
                            platformLabel(platformId),
                            color = DS.Muted,
                            fontSize = 12.sp,
                            fontFamily = Type.Sans
                        )
                    }
                }
            }
            // Search button
            Box(
                Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(DS.Card)
                    .border(1.dp, DS.Line, CircleShape)
                    .clickable(onClick = onSearch),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Search, "Cari", tint = DS.Body, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(8.dp))
            // Refresh button
            Box(
                Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(DS.Card)
                    .border(1.dp, DS.Line, CircleShape)
                    .clickable(onClick = onRefresh),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Refresh, "Muat ulang", tint = DS.Body, modifier = Modifier.size(18.dp))
            }
        }

        // Platform picker — compact pill
        Spacer(Modifier.height(12.dp))
        Box(
            Modifier
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(50))
                .background(DS.Card)
                .border(1.dp, DS.Line, RoundedCornerShape(50))
                .clickable { onPickPlatform() }
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(DS.GreenWash),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Apps, null, tint = DS.Green, modifier = Modifier.size(12.dp))
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    platformLabel(platformId),
                    color = DS.Hi,
                    fontSize = 12.sp,
                    fontFamily = Type.Sans,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.width(6.dp))
                Icon(Icons.Rounded.KeyboardArrowDown, null, tint = DS.Faint, modifier = Modifier.size(14.dp))
            }
        }

        // Alert banner
        if (alert.isNotBlank()) {
            Spacer(Modifier.height(12.dp))
            Row(
                Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(DS.RedWash)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.size(6.dp).clip(CircleShape).background(if (online) DS.Gold else DS.Red))
                Spacer(Modifier.width(8.dp))
                Text(
                    alert,
                    color = DS.Body,
                    fontSize = 11.5.sp,
                    fontFamily = Type.Sans,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
        }
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
            .border(1.dp, DS.Line, RoundedCornerShape(50))
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
    Column(Modifier.clickable { onClick(drama) }) {
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(0.68f)
                .clip(RoundedCornerShape(10.dp))
                .background(DS.Card)
        ) {
            // Poster
            AsyncImage(drama.poster, drama.title, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            Box(Modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(Color.Transparent, Color(0x600A0908)), startY = 200f)
            ))

            // Rank badge (top-left)
            rank?.let {
                Box(
                    Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(DS.Green)
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text("#$it", color = DS.Ink, fontSize = 10.sp, fontFamily = Type.Sans, fontWeight = FontWeight.Bold)
                }
            }

            // Platform logo (top-right)
            Box(Modifier.align(Alignment.TopEnd).padding(6.dp)) {
                PlatformLogo(drama.platform, Modifier.size(16.dp))
            }

            // Episode count (bottom-left)
            if (drama.episodes > 0) {
                Box(Modifier.align(Alignment.BottomStart).padding(6.dp)) {
                    Text(
                        "${drama.episodes} Ep",
                        color = DS.Hi,
                        fontSize = 10.sp,
                        fontFamily = Type.Sans,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0x990A0908))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
            }
        }
        Spacer(Modifier.height(7.dp))
        Text(
            drama.title.ifBlank { "Tanpa judul" },
            color = DS.Hi,
            fontSize = 12.sp,
            lineHeight = 15.sp,
            fontFamily = Type.Sans,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(2.dp))
        Text(
            drama.description.ifBlank { platformLabel(drama.platform) },
            color = DS.Muted,
            fontSize = 10.5.sp,
            lineHeight = 13.sp,
            fontFamily = Type.Sans,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SearchSectionTitle(title: String, subtitle: String = "") {
    Column(Modifier.fillMaxWidth()) {
        Text(title, color = DS.Hi, fontSize = 19.sp, fontFamily = Type.Sans, fontWeight = FontWeight.SemiBold)
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
                    fontSize = 19.sp,
                    fontFamily = Type.Sans,
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
    Box(
        Modifier
            .fillMaxWidth()
            .height(440.dp)
            .clickable { onClick(drama) }
    ) {
        // Background image
        AsyncImage(drama.poster, drama.title, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)

        // Multi-layer gradient overlay — cinematic feel
        Box(Modifier.fillMaxSize().background(
            Brush.verticalGradient(
                listOf(
                    Color(0x300A0908),     // Top: light
                    Color(0x000A0908),     // Middle: transparent
                    Color(0x700A0908),     // Lower-mid: start darkening
                    Color(0xCC0A0908),     // Bottom: heavy
                    DS.Bg                  // End: solid
                ),
                startY = 0f
            )
        ))
        // Side gradient for text readability
        Box(Modifier.fillMaxSize().background(
            Brush.horizontalGradient(
                listOf(Color(0x400A0908), Color.Transparent),
                endX = 600f
            )
        ))

        // Top badges
        Column(Modifier.align(Alignment.TopStart).padding(horizontal = 20.dp, vertical = 18.dp)) {
            Row(
                Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(DS.Green)
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Rounded.LocalFireDepartment, null, tint = DS.Ink, modifier = Modifier.size(13.dp))
                Spacer(Modifier.width(5.dp))
                Text("TRENDING", color = DS.Ink, fontSize = 10.sp, fontFamily = Type.Sans, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
        }

        // Platform badge top-right
        Box(Modifier.align(Alignment.TopEnd).padding(horizontal = 20.dp, vertical = 18.dp)) {
            PlatformBadge(drama.platform, compact = true)
        }

        // Content at bottom
        Column(Modifier.align(Alignment.BottomStart).padding(horizontal = 20.dp, vertical = 22.dp)) {
            // Title — large, bold
            Text(
                drama.title,
                color = DS.Hi,
                fontSize = 28.sp,
                lineHeight = 32.sp,
                fontFamily = Type.Sans,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(10.dp))

            // Meta info row
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (drama.episodes > 0) {
                    Text(
                        "${drama.episodes} Episode",
                        color = DS.Body,
                        fontSize = 12.5.sp,
                        fontFamily = Type.Sans,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.width(8.dp))
                    Box(Modifier.size(3.dp).clip(CircleShape).background(DS.Faint))
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    platformLabel(drama.platform),
                    color = DS.Green,
                    fontSize = 12.5.sp,
                    fontFamily = Type.Sans,
                    fontWeight = FontWeight.SemiBold
                )
                if (drama.views.isNotBlank()) {
                    Spacer(Modifier.width(8.dp))
                    Box(Modifier.size(3.dp).clip(CircleShape).background(DS.Faint))
                    Spacer(Modifier.width(8.dp))
                    Text(drama.views, color = DS.Muted, fontSize = 12.sp, fontFamily = Type.Sans)
                }
            }

            Spacer(Modifier.height(18.dp))

            // Action buttons
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                // Play button — prominent
                Box(
                    Modifier
                        .clip(RoundedCornerShape(50))
                        .background(DS.Green)
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                        .clickable { onClick(drama) }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.PlayArrow, null, tint = DS.Ink, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Tonton", color = DS.Ink, fontSize = 13.5.sp, fontFamily = Type.Sans, fontWeight = FontWeight.Bold)
                    }
                }
                // Detail button — outlined
                Box(
                    Modifier
                        .clip(RoundedCornerShape(50))
                        .border(1.5.dp, DS.Hi.copy(alpha = 0.5f), RoundedCornerShape(50))
                        .padding(horizontal = 18.dp, vertical = 12.dp)
                        .clickable { onClick(drama) }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Info, null, tint = DS.Hi, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Detail", color = DS.Hi, fontSize = 13.sp, fontFamily = Type.Sans, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ContinueCard(h: HistoryItem, onClick: (HistoryItem) -> Unit) {
    Column(Modifier.width(135.dp).clickable { onClick(h) }) {
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(0.7f)
                .clip(RoundedCornerShape(10.dp))
                .background(DS.Card)
        ) {
            // Poster
            if (h.poster.isNotBlank()) {
                AsyncImage(h.poster, h.title, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.Movie, h.title, tint = DS.Faint, modifier = Modifier.size(26.dp))
                }
            }

            // Dark overlay at bottom for progress bar area
            Box(Modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(Color.Transparent, Color(0x600A0908)), startY = 300f)
            ))

            // Episode badge
            Box(
                Modifier
                    .align(Alignment.TopStart)
                    .padding(7.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(DS.Green)
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
                Text("Ep ${h.episode}", color = DS.Ink, fontSize = 9.5.sp, fontFamily = Type.Sans, fontWeight = FontWeight.Bold)
            }

            // Play icon overlay (center)
            Box(
                Modifier
                    .align(Alignment.Center)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0x660A0908))
                    .border(1.5.dp, Color(0x80F5F0E8), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.PlayArrow, null, tint = DS.Hi, modifier = Modifier.size(18.dp))
            }

            // Progress bar at bottom
            LinearProgressIndicator(
                progress = (h.pct / 100f).coerceIn(0f, 1f),
                color = DS.Green,
                trackColor = Color(0x40F5F0E8),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(3.dp)
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            h.title,
            color = DS.Hi,
            fontSize = 12.5.sp,
            lineHeight = 16.sp,
            fontFamily = Type.Sans,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(2.dp))
        Text(
            if (h.pct > 0) "${h.pct}% ditonton" else platformLabel(h.platform),
            color = DS.Muted,
            fontSize = 10.5.sp,
            fontFamily = Type.Sans,
            maxLines = 1
        )
    }
}

@Composable
private fun DiscoverDramaCard(
    drama: Drama,
    isNew: Boolean,
    onClick: (Drama) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.clickable { onClick(drama) }) {
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(0.68f)
                .clip(RoundedCornerShape(12.dp))
                .background(DS.Card)
        ) {
            // Poster image
            AsyncImage(drama.poster, drama.title, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)

            // Bottom gradient for text readability
            Box(Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    listOf(Color.Transparent, Color(0x000A0908), Color(0xB30A0908)),
                    startY = 200f
                )
            ))

            // Top-left badges
            Column(
                Modifier.align(Alignment.TopStart).padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                if (isNew) {
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(DS.Green)
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text("BARU", color = DS.Ink, fontSize = 9.sp, fontFamily = Type.Sans, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
                    }
                }
            }

            // Top-right: platform logo
            Box(Modifier.align(Alignment.TopEnd).padding(8.dp)) {
                PlatformLogo(drama.platform, Modifier.size(18.dp))
            }

            // Bottom overlay: episode count
            if (drama.episodes > 0) {
                Box(Modifier.align(Alignment.BottomStart).padding(8.dp)) {
                    Row(
                        Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xB30A0908))
                            .padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.PlayCircle, null, tint = DS.Green, modifier = Modifier.size(11.dp))
                        Spacer(Modifier.width(3.dp))
                        Text("${drama.episodes} Ep", color = DS.Hi, fontSize = 10.sp, fontFamily = Type.Sans, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Bottom-right: views
            if (drama.views.isNotBlank()) {
                Box(Modifier.align(Alignment.BottomEnd).padding(8.dp)) {
                    Row(
                        Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xB30A0908))
                            .padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.Visibility, null, tint = DS.Body, modifier = Modifier.size(11.dp))
                        Spacer(Modifier.width(3.dp))
                        Text(drama.views, color = DS.Body, fontSize = 10.sp, fontFamily = Type.Sans, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }

        // Title and metadata below card
        Spacer(Modifier.height(8.dp))
        Text(
            drama.title.ifBlank { "Tanpa judul" },
            color = DS.Hi,
            fontSize = 13.sp,
            fontFamily = Type.Sans,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 17.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(3.dp))
        Text(
            platformLabel(drama.platform),
            color = DS.Muted,
            fontSize = 11.sp,
            fontFamily = Type.Sans,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun PosterImage(url: String, title: String, modifier: Modifier) {
    Box(modifier.clip(RoundedCornerShape(10.dp)).background(DS.Card)) {
        if (url.isNotBlank()) {
            AsyncImage(url, title, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            Box(Modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(Color.Transparent, Color(0x400A0908)), startY = 200f)
            ))
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

    Column(Modifier.padding(vertical = 0.dp)) {
        // Hero skeleton
        Box(Modifier.fillMaxWidth().height(380.dp).background(brush))
        Spacer(Modifier.height(24.dp))

        // Section title skeleton
        Box(Modifier.padding(horizontal = 20.dp).width(140.dp).height(20.dp).clip(RoundedCornerShape(4.dp)).background(brush))
        Spacer(Modifier.height(14.dp))

        // Row of cards
        Row(Modifier.padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            repeat(3) {
                Column {
                    Box(Modifier.width(115.dp).height(164.dp).clip(RoundedCornerShape(12.dp)).background(brush))
                    Spacer(Modifier.height(8.dp))
                    Box(Modifier.width(80.dp).height(12.dp).clip(RoundedCornerShape(3.dp)).background(brush))
                }
            }
        }
        Spacer(Modifier.height(28.dp))

        // Grid pairs
        repeat(2) {
            Row(Modifier.padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Column(Modifier.weight(1f)) {
                    Box(Modifier.fillMaxWidth().aspectRatio(0.68f).clip(RoundedCornerShape(12.dp)).background(brush))
                    Spacer(Modifier.height(8.dp))
                    Box(Modifier.width(100.dp).height(14.dp).clip(RoundedCornerShape(3.dp)).background(brush))
                    Spacer(Modifier.height(5.dp))
                    Box(Modifier.width(60.dp).height(10.dp).clip(RoundedCornerShape(3.dp)).background(brush))
                }
                Column(Modifier.weight(1f)) {
                    Box(Modifier.fillMaxWidth().aspectRatio(0.68f).clip(RoundedCornerShape(12.dp)).background(brush))
                    Spacer(Modifier.height(8.dp))
                    Box(Modifier.width(100.dp).height(14.dp).clip(RoundedCornerShape(3.dp)).background(brush))
                    Spacer(Modifier.height(5.dp))
                    Box(Modifier.width(60.dp).height(10.dp).clip(RoundedCornerShape(3.dp)).background(brush))
                }
            }
            Spacer(Modifier.height(18.dp))
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

    BackHandler { onBack() }
    LaunchedEffect(currentPlatform) { searchPlatformId = currentPlatform }

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
        state = runCatching {
            store.saveRecent(query)
            repo.searchPlatform(query, searchPlatformId)
        }.fold({ Load.Ok(it) }, { Load.Err(it.message ?: "Gagal") })
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
                .clip(RoundedCornerShape(50))
                .background(DS.Card)
                .border(1.dp, DS.Line, RoundedCornerShape(50))
                .padding(horizontal = 16.dp, vertical = 13.dp),
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
                                    columns = GridCells.Fixed(3),
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(all, key = { it.platform + it.id }) { d -> SearchDramaCard(d, onDrama) }
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
                            SearchSectionTitle("Pencarian terakhir", "Ketuk untuk mencari lagi.")
                            Spacer(Modifier.height(8.dp))
                            recent.take(6).forEach { keyword ->
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

                        SearchSectionTitle("Lagi ramai", "Judul yang sering dibuka di platform ini.")
                        Spacer(Modifier.height(14.dp))
                        if (popular.isNotEmpty()) {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(3),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                userScrollEnabled = false,
                                modifier = Modifier.height((((popular.size + 2) / 3) * 238).dp)
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
                                columns = GridCells.Fixed(3),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                userScrollEnabled = false,
                                modifier = Modifier.height((((trend.size + 2) / 3) * 238).dp)
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
    var showFav by remember { mutableStateOf(false) }
    var localTick by remember { mutableIntStateOf(0) }
    val history = remember(dataTick, localTick) { store.history(dataTick + localTick) }
    val favs = remember(dataTick, localTick) { store.favs() }

    Column(Modifier.fillMaxSize().background(DS.Bg).padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(16.dp))
        Text("Koleksi", color = DS.Hi, fontSize = 24.sp, fontFamily = Type.Sans, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(4.dp))
        Text("Riwayat dan favorit tersimpan di perangkatmu.", color = DS.Muted, fontSize = 12.5.sp, fontFamily = Type.Sans)
        Spacer(Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatTile("${history.size}", "Riwayat", Modifier.weight(1f))
            StatTile("${favs.size}", "Favorit", Modifier.weight(1f))
        }
        Spacer(Modifier.height(16.dp))
        // Tab switcher
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(DS.Card)
                .border(1.dp, DS.Line, RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            SegOption("Lanjutkan", !showFav, Modifier.weight(1f)) { showFav = false }
            SegOption("Favorit", showFav, Modifier.weight(1f)) { showFav = true }
        }
        Spacer(Modifier.height(16.dp))

        if (showFav) {
            if (favs.isEmpty()) {
                EmptyState("Belum ada favorit", "Simpan drama dari halaman detail biar gampang dibuka lagi.", Icons.Rounded.FavoriteBorder)
            } else {
                LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(bottom = 22.dp)) {
                    items(favs, key = { it.platform + it.id }) { d ->
                        LibraryRow(d.title, platformLabel(d.platform), d.poster, onDelete = {
                            store.removeFav(d.id, d.platform); localTick++; Toast.makeText(ctx, "Favorit dihapus", Toast.LENGTH_SHORT).show()
                        }) { onDrama(d) }
                    }
                }
            }
        } else {
            if (history.isEmpty()) {
                EmptyState("Belum ada riwayat", "Mulai nonton, nanti progress kamu muncul di sini.", Icons.Rounded.History)
            } else {
                LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(bottom = 22.dp)) {
                    items(history, key = { it.id + it.platform }) { h ->
                        LibraryRow(
                            h.title,
                            "${platformLabel(h.platform)} • Ep ${h.episode}${if (h.pct > 0) " • ${h.pct}%" else ""}",
                            h.poster,
                            onDelete = {
                                store.removeHistory(h.id, h.platform); localTick++; Toast.makeText(ctx, "Riwayat dihapus", Toast.LENGTH_SHORT).show()
                            }
                        ) { onDrama(Drama(h.id, h.title, poster = h.poster, platform = h.platform)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun LibraryRow(title: String, subtitle: String, poster: String, onDelete: (() -> Unit)? = null, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DS.Card)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PosterImage(poster, title, Modifier.width(52.dp).height(74.dp))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = DS.Hi, fontSize = 13.5.sp, fontFamily = Type.Sans, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(4.dp))
            Text(subtitle, color = DS.Muted, fontSize = 11.sp, fontFamily = Type.Sans, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        if (onDelete != null) {
            IconButton(onClick = onDelete, modifier = Modifier.size(34.dp)) {
                Icon(Icons.Rounded.DeleteOutline, "Hapus", tint = DS.Faint, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun StatTile(value: String, label: String, modifier: Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(12.dp))
            .background(DS.Card)
            .border(1.dp, DS.Line, RoundedCornerShape(12.dp))
            .padding(vertical = 14.dp, horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, color = DS.Green, fontWeight = FontWeight.Bold, fontSize = 22.sp, fontFamily = Type.Sans, maxLines = 1)
        Spacer(Modifier.height(4.dp))
        Text(label, color = DS.Muted, fontSize = 11.sp, fontFamily = Type.Sans, fontWeight = FontWeight.Medium)
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
    var dialog by remember { mutableStateOf<String?>(null) }
    val hCount = remember(dataTick) { store.history(dataTick).size }
    val fCount = remember(dataTick) { store.favs().size }
    val rCount = remember(dataTick) { store.recentSearches().size }
    val version = remember { try { ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionName } catch (_: Exception) { "4.9" } }

    Column(Modifier.fillMaxSize().background(DS.Bg).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Profil", color = DS.Hi, fontSize = 24.sp, fontFamily = Type.Sans, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(3.dp))
                Text("Semua data tersimpan di perangkatmu.", color = DS.Muted, fontSize = 12.5.sp, fontFamily = Type.Sans)
            }
            Box(
                Modifier
                    .clip(RoundedCornerShape(50))
                    .background(DS.Card)
                    .border(1.dp, DS.Line, RoundedCornerShape(50))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text("v$version", color = DS.Muted, fontSize = 11.sp, fontFamily = Type.Sans, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatTile("$hCount", "Riwayat", Modifier.weight(1f))
            StatTile("$fCount", "Favorit", Modifier.weight(1f))
            StatTile("$rCount", "Pencarian", Modifier.weight(1f))
        }

        Spacer(Modifier.height(26.dp))
        GroupTitle("PEMUTARAN")
        SettingsGroup {
            SettingsSwitch("Hemat data", "Kualitas lebih ringan saat streaming.", dataSaver) { dataSaver = it; store.setDataSaver(it); bump() }
            GroupDivider()
            SettingsSwitch("Putar lanjut otomatis", "Episode berikutnya diputar sendiri.", autoNext) { autoNext = it; store.setAutoNext(it); bump() }
            GroupDivider()
            SettingsSwitch("Rasio asli", "Tampilkan video tanpa crop.", fitContain) { fitContain = it; store.setFitContain(it); bump() }
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
            else -> {
                "Tentang Dramaku" to "Versi: $version\n\nDramaku Native dibuat untuk nonton drama pendek, serial Asia, dan film dalam satu aplikasi.\n\n• Player native berbasis ExoPlayer\n• Swipe episode vertikal\n• Favorit, riwayat, dan progress lokal\n• Mode hemat data\n\nDibangun dengan Kotlin + Jetpack Compose, ditata dengan Fraunces & Plus Jakarta Sans."
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
private fun GroupTitle(title: String, danger: Boolean = false) {
    Text(
        title,
        color = if (danger) DS.Red else DS.Faint,
        fontSize = 10.5.sp,
        fontFamily = Type.Sans,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.8.sp,
        modifier = Modifier.padding(bottom = 10.dp)
    )
}

@Composable
private fun SettingsGroup(content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(DS.Card).border(1.dp, DS.Line, RoundedCornerShape(18.dp)),
        content = content
    )
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
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) DS.Green else DS.Card)
            .border(1.dp, if (selected) DS.Green else DS.Line, RoundedCornerShape(8.dp))
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
    val preferLandscape = prefersLandscapePlayback(drama)
    var detailRange by remember(drama.id, total) { mutableIntStateOf(((resumeEp - 1) / 30).coerceAtLeast(0)) }

    Box(Modifier.fillMaxSize().background(DS.Bg)) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 32.dp)) {
            item {
                // Large backdrop section
                Box(Modifier.fillMaxWidth().height(420.dp)) {
                    AsyncImage(drama.poster, drama.title, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)

                    // Multi-layer gradient
                    Box(Modifier.fillMaxSize().background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0x200A0908),
                                Color(0x000A0908),
                                Color(0x400A0908),
                                Color(0xB00A0908),
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
                                .background(Color(0x600A0908))
                                .border(1.dp, DS.Line, CircleShape)
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
                                            .clip(RoundedCornerShape(4.dp))
                                            .border(1.dp, DS.Line, RoundedCornerShape(4.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                        // Title
                        Text(
                            drama.title,
                            color = DS.Hi,
                            fontSize = 26.sp,
                            fontFamily = Type.Sans,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 30.sp,
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
                                .height(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (state is Load.Ok && resolvingEpisode == 0) DS.Green else DS.Card2)
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
                                if (hist != null) "Lanjut Ep $resumeEp" else "Mulai Nonton",
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
                                .clip(RoundedCornerShape(14.dp))
                                .background(DS.Card)
                                .border(1.dp, DS.Line, RoundedCornerShape(14.dp))
                                .clickable { onPlay(detail, resumeEp) }
                                .padding(14.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.History, null, tint = DS.Green, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Lanjutkan menonton", color = DS.Hi, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, fontFamily = Type.Sans, modifier = Modifier.weight(1f))
                                Text("Ep $resumeEp", color = DS.Green, fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = Type.Sans)
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

                    // Episode list
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
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selected) DS.Green else DS.Card)
                                        .border(1.dp, if (selected) DS.Green else DS.Line, RoundedCornerShape(8.dp))
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

private fun episodeCount(d: Detail): Int = max(d.drama.episodes, d.episodes.size)
private object VideoCache {
    private const val MAX_MB = 256L
    @Volatile private var inst: SimpleCache? = null
    fun get(ctx: Context): SimpleCache = inst ?: synchronized(this) {
        inst ?: SimpleCache(ctx.cacheDir.resolve("exo_video_cache"), LeastRecentlyUsedCacheEvictor(MAX_MB * 1024 * 1024)).also { inst = it }
    }
}

private fun buildPlayer(ctx: Context, requestHeaders: Map<String, String> = emptyMap()): ExoPlayer {
    val http = DefaultHttpDataSource.Factory()
        .setUserAgent("Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 Chrome/121 Mobile Safari/537.36")
        .setAllowCrossProtocolRedirects(true).setConnectTimeoutMs(15_000).setReadTimeoutMs(30_000)
    // Playlist DramaBox di-proxy (captain.sapimu.au) dan wajib Bearer; segmen .ts
    // di CDN bebas token, jadi aman kalau header ikut terkirim ke sana juga.
    val headers = requestHeaders.toMutableMap()
    if (!headers.containsKey("Authorization")) headers["Authorization"] = "Bearer 15693e658f723c5b4c45900a5d045ef0ab6a053ecda4dadb831c68fef773ba5e"
    http.setDefaultRequestProperties(headers)
    val cache = CacheDataSource.Factory().setCache(VideoCache.get(ctx)).setUpstreamDataSourceFactory(http).setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
    return ExoPlayer.Builder(ctx)
        .setRenderersFactory(DefaultRenderersFactory(ctx).setEnableDecoderFallback(true))
        .setTrackSelector(DefaultTrackSelector(ctx).apply { setParameters(buildUponParameters().setPreferredVideoMimeTypes(MimeTypes.VIDEO_H264, MimeTypes.VIDEO_H265)) })
        .setMediaSourceFactory(DefaultMediaSourceFactory(cache)).build()
}

private fun playerError(e: PlaybackException): String {
    val r = e.message.orEmpty()
    return when {
        r.contains("403", true) || r.contains("401", true) -> "Akses video ditolak (CDN). Tekan Coba Lagi."
        r.contains("429", true) -> "CDN lagi batas permintaan. Tunggu sebentar lalu Coba Lagi."
        r.contains("video/hevc", true) || r.contains("hvc1", true) -> "Video HEVC tidak didukung di perangkat ini. Coba judul/episode lain."
        r.contains("MediaCodecVideoRenderer", true) -> "Decoder tidak bisa memutar video ini."
        r.contains("Source error", true) -> "Link expired/berubah. Tekan Coba Lagi."
        r.contains("timeout", true) -> "Koneksi timeout. Cek internet atau coba lagi."
        else -> r.ifBlank { "Video belum tersedia" }
    }
}

// ─────────────────────────────────────────────────────────────────
// PLAYER — feed cuplikan & swipe episode
// ─────────────────────────────────────────────────────────────────

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
    var playing by remember { mutableStateOf(false) }

    fun stop() { runCatching { player.playWhenReady = false; player.pause(); player.stop(); player.clearMediaItems() } }
    fun close() { stop(); onClose() }
    BackHandler { close() }

    DisposableEffect(Unit) {
        act?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val l = object : Player.Listener {
            override fun onIsPlayingChanged(p: Boolean) { playing = p }
            override fun onPlayerError(e: PlaybackException) { loading = false; error = playerError(e) }
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
        // Bstation: gunakan MergingMediaSource untuk gabung video+audio
        if (curDetail?.drama?.platform == "bstation" && stream.url.contains("|||")) {
            val mediaSource = buildBstationMediaSources(stream)
            player.setMediaSource(mediaSource); player.prepare(); player.seekTo(0); player.playWhenReady = true; loading = false
        } else {
            player.setMediaItem(buildMediaItem(stream)); player.prepare(); player.seekTo(0); player.playWhenReady = true; loading = false
        }
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
                Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent, Color(0xB30A0908)), startY = 400f)))
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
                Box(Modifier.clip(CircleShape).background(Color(0x730A0908))) {
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
            PlayerErrorCard(e, Modifier.align(Alignment.Center)) { retryKey++ }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun VerticalEpisodePlayer(detail: Detail, startEp: Int, repo: DramakuRepository, store: LocalStore, onClose: () -> Unit) {
    val ctx = LocalContext.current
    val act = ctx as? Activity
    val compAct = ctx as? ComponentActivity
    val scope = rememberCoroutineScope()
    val total = episodeCount(detail).coerceAtLeast(1)
    val pager = rememberPagerState(initialPage = (startEp - 1).coerceIn(0, total - 1), pageCount = { total })
    val player = remember { buildPlayer(ctx) }
    val preferLandscape = remember(detail.drama.platform, detail.drama.subjectType) { prefersLandscapePlayback(detail.drama) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var retryKey by remember { mutableIntStateOf(0) }
    var lastEp by remember { mutableIntStateOf(startEp.coerceIn(1, total)) }
    var uiVis by remember { mutableStateOf(true) }
    var epSheet by remember { mutableStateOf(false) }
    var fitContain by remember(detail.drama.id, detail.drama.platform) { mutableStateOf(if (preferLandscape) true else store.fitContain()) }
    var playing by remember { mutableStateOf(false) }
    var curMs by remember { mutableLongStateOf(0L) }
    var durMs by remember { mutableLongStateOf(0L) }
    var isSeeking by remember { mutableStateOf(false) }
    var flash by remember { mutableStateOf<String?>(null) }
    var speedHold by remember { mutableStateOf(false) }
    var liked by remember { mutableStateOf(false) }
    var lastSaveMs by remember { mutableLongStateOf(0L) }

    fun saveProgress(ep: Int) { runCatching { store.updateProgress(detail.drama.id, detail.drama.platform, ep, player.currentPosition.coerceAtLeast(0L), player.duration.takeIf { it > 0 } ?: 0L) } }
    fun closePlayer() { saveProgress(pager.currentPage + 1); runCatching { player.pause() }; onClose() }
    BackHandler { if (epSheet) epSheet = false else closePlayer() }

    DisposableEffect(act, preferLandscape) {
        act?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val prevOrientation = act?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        if (preferLandscape) {
            act?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        } else {
            act?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        val l = object : Player.Listener {
            override fun onIsPlayingChanged(p: Boolean) { playing = p }
            override fun onPlayerError(e: PlaybackException) { loading = false; error = playerError(e) }
            override fun onPlaybackStateChanged(s: Int) { if (s == Player.STATE_ENDED && store.autoNext() && pager.currentPage < total - 1) { saveProgress(pager.currentPage + 1); scope.launch { pager.animateScrollToPage(pager.currentPage + 1) } } }
        }
        player.addListener(l)
        onDispose {
            act?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            act?.requestedOrientation = prevOrientation
            player.removeListener(l)
            saveProgress(pager.currentPage + 1)
            runCatching { player.stop() }
            player.release()
        }
    }

    DisposableEffect(compAct) {
        val lc = compAct?.lifecycle ?: return@DisposableEffect onDispose { }
        val obs = LifecycleEventObserver { _, ev -> if (ev == Lifecycle.Event.ON_PAUSE || ev == Lifecycle.Event.ON_STOP) { saveProgress(pager.currentPage + 1); runCatching { player.pause() } } }
        lc.addObserver(obs); onDispose { lc.removeObserver(obs) }
    }

    LaunchedEffect(uiVis, loading, error, pager.currentPage) {
        if (uiVis && !loading && error == null && !epSheet) { delay(2800); uiVis = false }
    }

    LaunchedEffect(player, pager.currentPage, loading) {
        while (true) {
            val d = player.duration.takeIf { it > 0 } ?: 0L; durMs = d
            if (!isSeeking) curMs = player.currentPosition.coerceAtLeast(0L)
            if (d > 0 && System.currentTimeMillis() - lastSaveMs > 2500) { lastSaveMs = System.currentTimeMillis(); saveProgress(pager.currentPage + 1) }
            delay(500)
        }
    }

    LaunchedEffect(flash) { if (flash != null) { delay(700); flash = null } }

    LaunchedEffect(pager.currentPage, retryKey) {
        val ep = pager.currentPage + 1
        if (lastEp != ep) saveProgress(lastEp); lastEp = ep
        uiVis = true; epSheet = false; liked = false; curMs = 0; durMs = 0; lastSaveMs = 0; loading = true; error = null
        val savedPos = store.progressMs(detail.drama.id, detail.drama.platform, ep)
        val savedDur = store.progressDurationMs(detail.drama.id, detail.drama.platform, ep)
        val start = if (savedDur > 0 && savedPos >= savedDur - 4000) 0L else savedPos
        store.saveHistory(detail.drama, ep)
        val stream = try { repo.resolveStreamCached(detail, ep, store.dataSaver()) }
        catch (e: CancellationException) { throw e }
        catch (t: Throwable) { loading = false; error = t.message ?: "Video belum tersedia"; player.stop(); return@LaunchedEffect }
        if (stream.url.isBlank()) { loading = false; error = "Video belum tersedia"; player.stop(); return@LaunchedEffect }
        runCatching { player.stop(); player.clearMediaItems() }
        // Bstation: gunakan MergingMediaSource untuk gabung video+audio
        if (detail.drama.platform == "bstation" && stream.url.contains("|||")) {
            val mediaSource = buildBstationMediaSources(stream)
            player.setMediaSource(mediaSource); player.prepare()
        } else {
            player.setMediaItem(buildMediaItem(stream)); player.prepare()
        }
        if (start > 0) player.seekTo(start); player.playWhenReady = true; loading = false
        if (ep < total) launch { try { repo.resolveStreamCached(detail, ep + 1, store.dataSaver()) } catch (_: Throwable) {} }
    }

    Box(Modifier.fillMaxSize().background(Color.Black).pointerInput(player, pager.currentPage) {
        detectTapGestures(
            onTap = { uiVis = !uiVis; if (!uiVis) epSheet = false },
            onDoubleTap = { o -> uiVis = true; val w = size.width.coerceAtLeast(1); when {
                o.x < w * 0.42f -> { val n = (player.currentPosition - 10000).coerceAtLeast(0); player.seekTo(n); curMs = n; flash = "-10s" }
                o.x > w * 0.58f -> { val n = (player.currentPosition + 10000).coerceAtMost(player.duration.takeIf { it > 0 } ?: Long.MAX_VALUE); player.seekTo(n); curMs = n; flash = "+10s" }
                else -> { liked = true; flash = "♥" }
            }},
            onPress = { val q = withTimeoutOrNull(520) { tryAwaitRelease() }; if (q == null) { speedHold = true; flash = "2x"; runCatching { player.setPlaybackSpeed(2f) }; tryAwaitRelease(); runCatching { player.setPlaybackSpeed(1f) }; speedHold = false } }
        )
    }) {
        VerticalPager(pager, Modifier.fillMaxSize()) { page ->
            Box(Modifier.fillMaxSize().background(Color.Black)) {
                if (page == pager.currentPage) {
                    AndroidView(factory = { PlayerView(it).apply { useController = false; this.player = player; resizeMode = if (fitContain) AspectRatioFrameLayout.RESIZE_MODE_FIT else AspectRatioFrameLayout.RESIZE_MODE_ZOOM } }, update = { view -> view.player = player; view.resizeMode = if (fitContain) AspectRatioFrameLayout.RESIZE_MODE_FIT else AspectRatioFrameLayout.RESIZE_MODE_ZOOM }, modifier = Modifier.fillMaxSize())
                }
                if (uiVis || loading || error != null) {
                    Box(
                        Modifier.fillMaxSize().background(
                            Brush.verticalGradient(
                                listOf(Color(0x400A0908), Color.Transparent, Color(0xD90A0908)),
                                startY = 100f
                            )
                        )
                    )
                    Column(
                        Modifier.align(Alignment.BottomStart).padding(start = 18.dp, end = 18.dp, bottom = 138.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            PlayerOverlayChip(platformLabel(detail.drama.platform))
                            PlayerOverlayChip("Ep ${page + 1} / $total")
                            if (preferLandscape) PlayerOverlayChip("Wide")
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(detail.drama.title, color = DS.Hi, fontSize = 20.sp, fontFamily = Type.Sans, fontWeight = FontWeight.SemiBold, lineHeight = 24.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Spacer(Modifier.height(5.dp))
                        Text(
                            if (playing) "Geser naik atau turun untuk ganti episode" else "Ketuk layar untuk kontrol",
                            color = DS.Body,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            fontFamily = Type.Sans,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        AnimatedVisibility(uiVis || loading || error != null, Modifier.align(Alignment.TopStart)) {
            Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.clip(CircleShape).background(Color(0x730A0908))) {
                    GhostIconButton(Icons.Rounded.ArrowBack, "Tutup", ::closePlayer)
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(detail.drama.title, color = DS.Hi, fontSize = 13.5.sp, fontFamily = Type.Sans, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("Ep ${pager.currentPage + 1} / $total", color = DS.Muted, fontSize = 11.sp, fontFamily = Type.Sans)
                }
                Text(
                    if (preferLandscape) "Landscape" else "Portrait",
                    color = DS.Body, fontSize = 10.5.sp, fontFamily = Type.Sans, fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clip(RoundedCornerShape(50)).border(1.dp, DS.Line, RoundedCornerShape(50)).padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }

        // Kontrol bawah — satu panel, tidak ada tombol yang menutupi video.
        AnimatedVisibility(uiVis || loading || error != null, Modifier.align(Alignment.BottomCenter)) {
            Surface(color = DS.SheetBg, shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(horizontal = 18.dp, vertical = 12.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(formatMs(curMs), color = DS.Hi, fontSize = 11.sp, fontFamily = Type.Sans, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.weight(1f))
                        Text(if (durMs > 0) formatMs(durMs) else "", color = DS.Muted, fontSize = 11.sp, fontFamily = Type.Sans)
                    }
                    Slider(
                        value = if (durMs > 0) (curMs.toFloat() / durMs.toFloat()).coerceIn(0f, 1f) else 0f,
                        onValueChange = { isSeeking = true; curMs = (it * durMs).toLong().coerceAtLeast(0) },
                        onValueChangeFinished = { player.seekTo(curMs); saveProgress(pager.currentPage + 1); isSeeking = false },
                        enabled = durMs > 0,
                        colors = SliderDefaults.colors(thumbColor = DS.Cream, activeTrackColor = DS.Green, inactiveTrackColor = DS.Card2),
                        modifier = Modifier.fillMaxWidth().height(24.dp)
                    )
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { uiVis = true; epSheet = true }) { Icon(Icons.Rounded.List, "Episode", tint = DS.Body) }
                        IconButton(onClick = { uiVis = true; retryKey++ }) { Icon(Icons.Rounded.Refresh, "Muat ulang", tint = DS.Body) }
                        Spacer(Modifier.weight(1f))
                        IconButton(onClick = { uiVis = true; fitContain = !fitContain; if (!preferLandscape) store.setFitContain(fitContain) }) { Icon(if (fitContain) Icons.Rounded.AspectRatio else Icons.Rounded.Fullscreen, "Ukuran layar", tint = DS.Body) }
                        Spacer(Modifier.width(10.dp))
                        Box(
                            Modifier.size(46.dp).clip(CircleShape).background(DS.Cream).clickable { uiVis = true; if (player.isPlaying) player.pause() else player.play() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, if (playing) "Jeda" else "Putar", tint = DS.Ink, modifier = Modifier.size(23.dp))
                        }
                    }
                }
            }
        }

        AnimatedVisibility(flash != null || speedHold || liked, Modifier.align(Alignment.Center)) {
            Surface(color = DS.SheetBg, shape = RoundedCornerShape(50)) {
                Text(flash ?: if (speedHold) "2x" else "♥", color = if (liked) DS.Green else DS.Hi, fontFamily = Type.Sans, fontWeight = FontWeight.Bold, fontSize = if (liked) 26.sp else 16.sp, modifier = Modifier.padding(horizontal = 22.dp, vertical = 10.dp))
            }
        }

        if (loading) {
            Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = DS.Green, strokeWidth = 2.5.dp)
                Spacer(Modifier.height(12.dp))
                Text("Memuat Ep ${pager.currentPage + 1}…", color = DS.Hi, fontSize = 13.sp, fontFamily = Type.Sans)
            }
        }

        error?.let { e ->
            PlayerErrorCard(e, Modifier.align(Alignment.Center)) { retryKey++ }
        }

        // Sheet episode
        if (epSheet) {
            Box(Modifier.fillMaxSize().background(DS.Bg.copy(alpha = 0.84f)).clickable { epSheet = false })
            Surface(color = DS.Raise, shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp), modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().heightIn(max = 420.dp)) {
                Column(Modifier.padding(18.dp)) {
                    Text("Episode", color = DS.Hi, fontSize = 19.sp, fontFamily = Type.Sans, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(3.dp))
                    Text(detail.drama.title, color = DS.Muted, fontSize = 12.sp, fontFamily = Type.Sans, maxLines = 1)
                    Spacer(Modifier.height(14.dp))
                    LazyVerticalGrid(columns = GridCells.Fixed(6), verticalArrangement = Arrangement.spacedBy(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items((1..total).toList()) { ep ->
                            val active = ep == pager.currentPage + 1
                            Box(
                                Modifier.height(40.dp).clip(RoundedCornerShape(10.dp))
                                    .background(if (active) DS.Cream else DS.Card)
                                    .clickable { epSheet = false; uiVis = true; scope.launch { pager.animateScrollToPage(ep - 1) } },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(ep.toString(), color = if (active) DS.Ink else DS.Hi, fontWeight = if (active) FontWeight.Bold else FontWeight.Medium, fontSize = 12.5.sp, fontFamily = Type.Sans)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayerOverlayChip(text: String) {
    Text(
        text,
        color = DS.Hi,
        fontSize = 11.sp,
        fontFamily = Type.Sans,
        fontWeight = FontWeight.SemiBold,
        maxLines = 1,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0x800A0908))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    )
}

@Composable
private fun PlayerErrorCard(message: String, modifier: Modifier = Modifier, onRetry: () -> Unit) {
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
                Text("Coba lagi", color = DS.Ink, fontWeight = FontWeight.Bold, fontSize = 12.5.sp, fontFamily = Type.Sans)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// HELPERS
// ─────────────────────────────────────────────────────────────────

// Sumber layar lebar (MovieBox/Drakor) sudah mati — semua memutar portrait.
private fun prefersLandscapePlayback(drama: Drama): Boolean = false

private fun buildMediaItem(s: StreamResult): MediaItem {
    val url = cleanUrl(s.url)
    val b = MediaItem.Builder().setUri(Uri.parse(url))
    val lower = url.lowercase()
    when {
        // DASH MPD dari Bstation
        lower.contains("mpd") || lower.contains("dash") -> b.setMimeType(MimeTypes.APPLICATION_MPD)
        // Hanya playlist DramaBox yang butuh hint HLS (URL-nya tanpa .m3u8).
        // Stream Melolo juga mengandung "/stream?" tapi itu MP4 — jangan disamaratakan.
        lower.contains("m3u8") || lower.contains("dramaboxbaru/api/stream") -> b.setMimeType(MimeTypes.APPLICATION_M3U8)
        lower.contains(".mp4") -> b.setMimeType(MimeTypes.APPLICATION_MP4)
    }
    val subtitle = cleanUrl(s.subtitle)
    if (subtitle.isNotBlank()) {
        val mime = when {
            subtitle.lowercase().endsWith(".vtt") -> MimeTypes.TEXT_VTT
            subtitle.lowercase().endsWith(".ass") -> MimeTypes.TEXT_SSA  // ASS/SSA
            else -> MimeTypes.APPLICATION_SUBRIP
        }
        b.setSubtitleConfigurations(listOf(MediaItem.SubtitleConfiguration.Builder(Uri.parse(subtitle)).setMimeType(mime).setLanguage("id").setSelectionFlags(C.SELECTION_FLAG_DEFAULT).build()))
    }
    return b.build()
}

// Bstation: MPD punya video + audio terpisah. Extract dan merge via MergingMediaSource.
private fun buildBstationMediaSources(stream: StreamResult): androidx.media3.common.MediaSource {
    val parts = stream.url.split("|||")
    val videoUrl = cleanUrl(parts[0])
    val audioUrl = if (parts.size > 1) cleanUrl(parts[1]) else null
    
    val http = DefaultHttpDataSource.Factory()
        .setUserAgent("Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 Chrome/121 Mobile Safari/537.36")
        .setAllowCrossProtocolRedirects(true)
        .setConnectTimeoutMs(15_000)
        .setReadTimeoutMs(30_000)
        .setDefaultRequestProperties(mapOf("Authorization" to "Bearer 15693e658f723c5b4c45900a5d045ef0ab6a053ecda4dadb831c68fef773ba5e"))
    
    val videoSource = DefaultMediaSourceFactory(http)
        .createMediaSource(MediaItem.fromUri(Uri.parse(videoUrl)).buildUpon().setMimeType(MimeTypes.VIDEO_MP4).build())
    
    return if (audioUrl != null) {
        val audioSource = DefaultMediaSourceFactory(http)
            .createMediaSource(MediaItem.fromUri(Uri.parse(audioUrl)).buildUpon().setMimeType(MimeTypes.AUDIO_MP4).build())
        MergingMediaSource(videoSource, audioSource)
    } else {
        videoSource
    }
}

private fun formatMs(ms: Long): String {
    val s = (ms / 1000).coerceAtLeast(0)
    val h = s / 3600; val m = (s % 3600) / 60; val sec = s % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, sec) else "%d:%02d".format(m, sec)
}

private fun shareDrama(ctx: Context, d: Drama) {
    ctx.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, "${d.title}\n${platformLabel(d.platform)} · Dramaku") }, "Bagikan"))
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
    private val detailCache = ConcurrentHashMap<String, Detail>()
    private val streamCache = ConcurrentHashMap<String, CachedStream>()

    fun previewDetail(input: Drama): Detail {
        detailCache[detailKey(input)]?.let { return it }
        val t = input.episodes.coerceAtLeast(1)
        return Detail(input.copy(episodes = t), (1..t).map { EpisodeInfo(it) })
    }

    suspend fun loadDetailCached(input: Drama): Detail {
        val k = detailKey(input); detailCache[k]?.let { return it }
        return loadDetail(input).also { detailCache[k] = it }
    }

    suspend fun resolveStreamCached(d: Detail, ep: Int, ds: Boolean): StreamResult {
        // Signed URL dari provider cepat expired. Jangan cache supaya Retry selalu ambil link/token baru.
        if (d.drama.platform in setOf("melolo", "dramanova", "freereels", "bstation", "dramabox", "moviebox", "mbshorts")) {
            return resolveStream(d, ep, ds)
        }
        val k = streamKey(d.drama, ep, ds); val now = System.currentTimeMillis()
        streamCache[k]?.takeIf { it.expiresAtMs > now }?.let { return it.result }
        return resolveStream(d, ep, ds).also { r -> if (r.url.isNotBlank()) streamCache[k] = CachedStream(r, now + 300_000) }
    }

    private fun detailKey(d: Drama) = "${d.platform}|${d.id}"
    private fun streamKey(d: Drama, ep: Int, ds: Boolean) = "${d.platform}|${d.id}|$ep|${if (ds) "480" else "720"}"

    suspend fun loadHome(p: String) = loadHomePage(p, 1)

    // URL berawalan "POST " di-fetch pakai POST (endpoint shorts menolak GET).
    private suspend fun fetchHome(url: String): JSONObject =
        if (url.startsWith("POST ")) getJson(url.removePrefix("POST ").trim(), post = true) else getJson(url)

    suspend fun loadHomePage(p: String, page: Int): HomeBundle = coroutineScope {
        val req = homePageRequest(p, page)
        val json = try { fetchHome(req.url) } catch (e: CancellationException) { throw e } catch (_: Throwable) { null }
        val items = dedupe(json?.let { flat(it.dataOrSelf(), p) }.orEmpty())
        var rec = emptyList<Drama>(); var pop = emptyList<Drama>(); var nw = emptyList<Drama>()
        when (req.section) { HomeSection.Popular -> pop = items; HomeSection.Newest -> nw = items; HomeSection.Recommended -> rec = items }
        val more = req.hasMore
        if (rec.isEmpty() && pop.isEmpty() && nw.isEmpty() && req.virtualPage == 1) error("Sumber ini sedang tidak tersedia. Coba rak lain dulu ya.")
        HomeBundle(rec, pop, nw, req.virtualPage, more)
    }

    suspend fun searchPlatform(q: String, p: String): List<Drama> = coroutineScope {
        // Shorts belum punya endpoint pencarian — balikin kosong saja.
        if (p == "mbshorts") return@coroutineScope emptyList()
        val items = runCatching {
            when (p) {
                "dramabox" -> flat(getJson("${apiBase(p)}/search?keyword=${enc(q)}&page=1&lang=in").dataOrSelf(), p)
                // perPage di-radius upstream: 10–20 aman, 24 ke atas dibalas data kosong.
                "moviebox" -> flat(getJson("${apiBase(p)}/subject/search?keyword=${enc(q)}&page=1&perPage=20", post = true).dataOrSelf(), p)
                "dramanova" -> flat(getJson("${apiBase(p)}/search?q=${enc(q)}&lang=in").dataOrSelf(), p)
                "freereels" -> flat(getJson("${apiBase(p)}/search?q=${enc(q)}&lang=id-ID&limit=50").dataOrSelf(), p)
                "bstation" -> flat(getJson("${apiBase(p)}/search?keyword=${enc(q)}&pn=1&lang=id_ID").dataOrSelf(), p)
                else -> flat(getJson("${apiBase(p)}/search?q=${enc(q)}&lang=id&limit=50&offset=0").dataOrSelf(), p)
            }
        }.getOrDefault(emptyList())
        dedupeAndRank(items, q).take(80)
    }

    // Ambil satu rak konten dari path bebas (mis. "hidden-gems?lang=in" atau
    // "browse?type=433&page=1&lang=in") — dipakai rak genre di beranda.
    suspend fun browsePath(p: String, path: String): List<Drama> =
        runCatching { flat(getJson("${apiBase(p)}/$path").dataOrSelf(), p) }.getOrDefault(emptyList())

    // Dramanova: ambil recommend by categoryKey
    suspend fun browseDramanova(categoryKey: String): List<Drama> =
        runCatching { flat(getJson("${apiBase("dramanova")}/recommend?lang=in&categoryKey=${enc(categoryKey)}&page=1&limit=20").dataOrSelf(), "dramanova") }.getOrDefault(emptyList())

    // Freereels: ambil by category path (female, male, anime, dubbing, coming-soon)
    suspend fun browseFreereels(categoryPath: String): List<Drama> =
        runCatching { flat(getJson("${apiBase("freereels")}/$categoryPath?page=0&lang=id-ID").dataOrSelf(), "freereels") }.getOrDefault(emptyList())

    // Bstation: ambil from ogv home
    suspend fun browseBstation(): List<Drama> =
        runCatching { flat(getJson("${apiBase("bstation")}/ogv/home?lang=id_ID").dataOrSelf(), "bstation") }.getOrDefault(emptyList())

    // Bstation: ambil by genre style_id
    suspend fun browseBstationGenre(styleId: String): List<Drama> =
        runCatching { flat(getJson("${apiBase("bstation")}/ogv/season/result?style_id=${enc(styleId)}&page=1&lang=id_ID").dataOrSelf(), "bstation") }.getOrDefault(emptyList())

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
        if (p == "dramabox") {
            // Struktur DramaBox: data.bookInfo { bookName, cover, introduction, chapterCount }
            // + data.chapterList [{ id, indexStr, utime, duration }].
            val data = json.optJSONObject("data") ?: error("Detail tidak ditemukan")
            val info = data.optJSONObject("bookInfo") ?: data
            val epsArr = data.optJSONArray("chapterList") ?: JSONArray()
            val title = info.stringAny("bookName", "name", "title").ifBlank { input.title }
            val desc = cleanText(info.stringAny("introduction", "description", "synopsis")).ifBlank { input.description }
            val poster = fixImg(info.stringAny("cover", "coverWap", "image", "poster").ifBlank { input.poster })
            val eps = epsArr.objects().mapIndexed { i, o ->
                EpisodeInfo(
                    o.intAny("indexStr", "index", "episode", i + 1),
                    o.stringAny("stream_url", "streaming", "url"),
                    o.stringAny("episode_label", "title", "label")
                )
            }
            val total = max(info.intAny("chapterCount", "chapter_count", input.episodes), eps.size)
            val drama = Drama(input.id, title, desc, poster, total, input.views, tagsOf(info), p, input.subjectType)
            return Detail(drama, if (eps.isNotEmpty()) eps else (1..total.coerceAtLeast(1)).map { EpisodeInfo(it) })
        }
        if (p == "moviebox" || p == "mbshorts") {
            // subject/get: { ..., description, cover:{url}, genre, episodes:[{episode,se,title,duration}] }
            // shorts/info: { ..., totalEpisode, episodes:[...] } — cover juga objek {url}.
            val data = json.optJSONObject("data") ?: error("Detail tidak ditemukan")
            val title = data.stringAny("title", "bookName", "name").ifBlank { input.title }
            val desc = cleanText(data.stringAny("description", "introduction", "synopsis")).ifBlank { input.description }
            val poster = fixImg(data.coverUrl().ifBlank { data.stringAny("cover_url", "image", "poster") }.ifBlank { input.poster })
            val epsArr = data.optJSONArray("episodes") ?: JSONArray()
            val eps = epsArr.objects().mapIndexed { i, o ->
                EpisodeInfo(
                    o.intAny("episode", "index", i + 1),
                    o.stringAny("stream_url", "streaming", "url"),
                    o.stringAny("episode_label", "label").ifBlank { o.stringAny("title") },
                    se = o.intAny("se", "season", 1)
                )
            }
            val total = max(data.intAny("totalEpisode", "chapterCount", "episode_count", input.episodes), eps.size)
            val drama = Drama(input.id, title, desc, poster, total, input.views, tagsOf(data), p, input.subjectType)
            return Detail(drama, if (eps.isNotEmpty()) eps else (1..total.coerceAtLeast(1)).map { EpisodeInfo(it) })
        }
        if (p == "dramanova") {
            // Dramanova: { id, title, cover, description, totalEpisodes, episodes: [{ id, number, title, fileId, free, subtitles: [{ lang, url }] }] }
            val title = json.stringAny("title").ifBlank { input.title }
            val desc = cleanText(json.stringAny("description")).ifBlank { input.description }
            val poster = fixImg(json.stringAny("cover").ifBlank { input.poster })
            val epsArr = json.optJSONArray("episodes") ?: JSONArray()
            val eps = epsArr.objects().map { o ->
                val subsArr = o.optJSONArray("subtitles") ?: JSONArray()
                val subtitleUrl = (0 until subsArr.length()).mapNotNull { i ->
                    val s = subsArr.optJSONObject(i)
                    if (s?.stringAny("lang") == "in") s.stringAny("url") else null
                }.firstOrNull().orEmpty()
                EpisodeInfo(
                    number = o.intAny("number", "episode", 0),
                    streaming = o.stringAny("fileId", "id"),
                    label = o.stringAny("title", "label"),
                    locked = !o.optBoolean("free", true),
                    subtitle = subtitleUrl
                )
            }
            val total = max(json.intAny("totalEpisodes", "episodes", input.episodes), eps.size)
            val drama = Drama(input.id, title, desc, poster, total, input.views, tagsOf(json), p, input.subjectType)
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
        if (p == "bstation") {
            // Bstation: { code, message, data: { season_id, title, cover, evaluate, episodes: [{ id, title, long_title }] } }
            val data = json.optJSONObject("data") ?: json
            val title = data.stringAny("title").ifBlank { input.title }
            val desc = cleanText(data.stringAny("evaluate", "description")).ifBlank { input.description }
            val poster = fixImg(data.stringAny("cover").ifBlank { input.poster })
            val epsArr = data.optJSONArray("episodes") ?: JSONArray()
            val eps = epsArr.objects().map { o ->
                val epNum = o.stringAny("title").toIntOrNull() ?: o.intAny("index", "number", 0)
                EpisodeInfo(
                    number = epNum,
                    streaming = o.stringAny("id"), // ep_id untuk resolve stream
                    label = o.stringAny("long_title", "title")
                )
            }
            val total = max(eps.size, input.episodes)
            val typeName = data.stringAny("type_name")
            val tags = if (typeName.isNotBlank()) listOf(typeName) else emptyList()
            val drama = Drama(input.id, title, desc, poster, total, "", tags, p, input.subjectType)
            return Detail(drama, if (eps.isNotEmpty()) eps else (1..total.coerceAtLeast(1)).map { EpisodeInfo(it) })
        }
        val data = json.optJSONObject("data") ?: error("Detail tidak ditemukan")
        val d = normalize(data, p).let { it.copy(id = it.id.ifBlank { input.id }, title = it.title.ifBlank { input.title }, poster = fixImg(it.poster.ifBlank { input.poster }), description = it.description.ifBlank { input.description }, episodes = max(it.episodes, input.episodes), platform = p) }
        val epsArr = data.optJSONArray("video_list") ?: data.optJSONArray("episode_list") ?: data.optJSONArray("episodes") ?: data.optJSONArray("chapterList")
        val eps = epsArr?.objects()?.mapIndexed { i, o -> EpisodeInfo(o.intAny("episode", "episode_no", "chapterIndex", i + 1), o.stringAny("streaming"), o.stringAny("episode_label", "title", "label")) }.orEmpty()
        val total = max(d.episodes, eps.size)
        return Detail(d.copy(episodes = total), if (eps.isNotEmpty()) eps else (1..total.coerceAtLeast(1)).map { EpisodeInfo(it) })
    }

    suspend fun resolveStream(d: Detail, ep: Int, ds: Boolean): StreamResult {
        val base = apiBase(d.drama.platform); val id = d.drama.id
        if (d.drama.platform == "dramabox") {
            // Endpoint ini langsung membalas playlist m3u8 — URL-nya sendiri yang diputar.
            // Header Bearer dipasang di data source player (buildPlayer / PlayerActivity).
            return StreamResult("$base/stream?bookId=${enc(id)}&episode=${ep.coerceAtLeast(1)}&lang=in")
        }
        if (d.drama.platform == "moviebox") {
            // Series MovieBox punya nomor season (se); episode paramnya per-season.
            val se = d.episodes.firstOrNull { it.number == ep }?.se ?: 1
            val json = getJson("$base/stream/${enc(id)}?ep=${ep.coerceAtLeast(1)}&se=$se&subjectId=${enc(id)}&lang=id")
            val link = json.optJSONObject("data")?.stringAny("resourceLink", "url", "link").orEmpty()
            if (link.isBlank()) error("Video belum tersedia")
            return StreamResult(link)
        }
        if (d.drama.platform == "mbshorts") {
            val json = getJson("$base/shorts/mini-list?subjectId=${enc(id)}&ep=${ep.coerceAtLeast(1)}&lang=id")
            val link = json.optJSONObject("data")?.stringAny("url", "resourceLink").orEmpty()
            if (link.isBlank()) error("Video belum tersedia")
            return StreamResult(link)
        }
        if (d.drama.platform == "dramanova") {
            // Dramanova: pakai fileId dari episode untuk hit endpoint video
            val epInfo = d.episodes.firstOrNull { it.number == ep }
            val fileId = epInfo?.streaming?.takeIf { it.isNotBlank() } ?: error("FileId episode tidak ditemukan")
            val json = getJson("$base/video?id=${enc(fileId)}")
            val videosArr = json.optJSONArray("videos") ?: JSONArray()
            val bestVideo = videosArr.objects().maxByOrNull { o ->
                when (o.stringAny("definition").lowercase()) {
                    "1080p" -> 3; "720p" -> 2; "480p" -> 1; else -> 0
                }
            }
            val link = bestVideo?.stringAny("main_url", "backup_url").orEmpty()
            if (link.isBlank()) error("Video belum tersedia")
            val subtitle = epInfo?.subtitle?.takeIf { it.isNotBlank() }.orEmpty()
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
        if (d.drama.platform == "bstation") {
            // Bstation: /stream/mpd?id={ep_id}&qn=64 → DASH MPD XML (video+audio terpisah)
            val epId = d.episodes.firstOrNull { it.number == ep }?.streaming ?: error("Episode ID tidak ditemukan")
            val mpdXml = getString("$base/stream/mpd?id=${enc(epId)}&qn=64&lang=id_ID")
            // Parse MPD: extract video + audio BaseURL, merge di player
            val videoUrl = Regex("<AdaptationSet[^>]*contentType=\"video\"[^>]*>.*?<BaseURL>(.*?)</BaseURL>", RegexOption.DOT_MATCHES_ALL)
                .find(mpdXml)?.groupValues?.getOrNull(1)?.trim() ?: ""
            val audioUrl = Regex("<AdaptationSet[^>]*contentType=\"audio\"[^>]*>.*?<BaseURL>(.*?)</BaseURL>", RegexOption.DOT_MATCHES_ALL)
                .find(mpdXml)?.groupValues?.getOrNull(1)?.trim() ?: ""
            if (videoUrl.isBlank()) error("Video belum tersedia")
            // Format: videoURL|||audioURL (dipisah special delimiter, di-parse di buildMediaItem)
            val streamUrl = if (audioUrl.isNotBlank()) "$videoUrl|||$audioUrl" else videoUrl
            return StreamResult(streamUrl, "")
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

    private suspend fun getJson(url: String, post: Boolean = false): JSONObject = withContext(Dispatchers.IO) {
        // Origin di balik proxy kadang balas 5xx sementara.
        // Retry singkat supaya home tidak langsung error.
        var last: Throwable? = null
        repeat(3) { attempt ->
            if (attempt > 0) delay(450L * attempt)
            try {
                val reqBuilder = Request.Builder().url(url)
                    .header("User-Agent", "DramakuNative/5.0 Android")
                    .header("Accept", "application/json, text/plain, */*")
                if (post) reqBuilder.post(okhttp3.FormBody.Builder().build())
                if (url.contains("captain.sapimu.au")) {
                    reqBuilder.header("Authorization", "Bearer 15693e658f723c5b4c45900a5d045ef0ab6a053ecda4dadb831c68fef773ba5e")
                }
                return@withContext client.newCall(reqBuilder.build()).execute().use { r ->
                    val body = r.body?.string().orEmpty()
                    val json = runCatching { JSONObject(body) }.getOrNull()
                    if (!r.isSuccessful) {
                        val msg = json?.stringAny("error_msg", "error", "message").orEmpty()
                        error(msg.ifBlank { "HTTP ${r.code}" })
                    }
                    val parsed = json ?: JSONObject(body)
                    val code = parsed.optInt("code", 200)
                    if (code >= 400) error(parsed.stringAny("error_msg", "error", "message").ifBlank { "HTTP $code" })
                    val statusZero = parsed.optInt("status", 1) == 0
                    val explicitSuccess = parsed.optBoolean("success", false) || parsed.stringAny("message").equals("success", true)
                    if (statusZero && !explicitSuccess) error(parsed.stringAny("error_msg", "error", "message").ifBlank { "Gagal memuat data" })
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

    private suspend fun getString(url: String): String = withContext(Dispatchers.IO) {
        var last: Throwable? = null
        repeat(3) { attempt ->
            if (attempt > 0) delay(450L * attempt)
            try {
                val reqBuilder = Request.Builder().url(url)
                    .header("User-Agent", "DramakuNative/5.0 Android")
                    .header("Accept", "*/*")
                if (url.contains("captain.sapimu.au")) {
                    reqBuilder.header("Authorization", "Bearer 15693e658f723c5b4c45900a5d045ef0ab6a053ecda4dadb831c68fef773ba5e")
                }
                return@withContext client.newCall(reqBuilder.build()).execute().use { r ->
                    if (!r.isSuccessful) error("HTTP ${r.code}")
                    r.body?.string().orEmpty()
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

// Proxy Melolo mengabaikan offset/page/session: tiap halaman mengembalikan
// feed 18 judul yang sama, jadi jangan fetch ulang (hemat kuota + waktu).
// Dramanova dan Freereels juga dibatasi 1 halaman.
private fun pagesFor(p: String): IntRange = 1..1

private fun homeUrls(p: String, page: Int): List<String> {
    val base = apiBase(p)
    // Kode Bahasa Indonesia di proxy DramaBox adalah "in"; "lang=id" malah 500 di upstream-nya.
    if (p == "dramabox") {
        return listOf("$base/recommend/book?lang=in", "$base/rank?lang=in", "$base/home?lang=in")
    }
    // MovieBox: list-nya kaya (home-content 237 judul), taruh di slot Popular
    // supaya catatan pertama layar langsung penuh. Sisanya rak kategori asli.
    if (p == "moviebox") {
        return listOf(
            "$base/tabs/category-content?type=6159907949583500480&lang=id",
            "$base/tabs/home-content?lang=id",
            "$base/tabs/category-content?type=2529702013798074864&lang=id"
        )
    }
    if (p == "mbshorts") {
        // Endpoint shorts cuma terima POST (GET dibalas 500 "cf eror") —
        // penanda "POST " diproses saat fetch.
        return listOf(
            "POST $base/shorts/reel?page=1&perPage=24",
            "POST $base/shorts/most-trending?page=1&perPage=24",
            "POST $base/shorts/reel?page=2&perPage=24"
        )
    }
    if (p == "dramanova") {
        // Dramanova: pakai recommend endpoint untuk hot/new/more.
        return listOf(
            "$base/recommend?lang=in&categoryKey=dramanova_hot&page=1&limit=6",
            "$base/recommend?lang=in&categoryKey=dramanova_new&page=1&limit=12",
            "$base/recommend?lang=in&categoryKey=dramanova_more&page=1&limit=20"
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
    if (p == "bstation") {
        // Bstation: semua endpoint return items dengan season_id
        return listOf(
            "$base/ogv/home?lang=id_ID",
            "$base/ogv/season/result?style_id=-1&page=1&lang=id_ID",
            "$base/ogv/season/result?style_id=20006&page=1&lang=id_ID"
        )
    }
    return listOf("$base/bookmall?lang=id", "$base/bookmall/tabs?gender=0&lang=id", "$base/bookmall?lang=id")
}

private fun detailUrl(d: Drama): String = when (d.platform) {
    "dramabox" -> "${apiBase(d.platform)}/drama/${enc(d.id)}?lang=in"
    "moviebox" -> "${apiBase(d.platform)}/subject/get?subjectId=${enc(d.id)}&lang=id"
    "mbshorts" -> "${apiBase(d.platform)}/shorts/info?subjectId=${enc(d.id)}&lang=id"
    "dramanova" -> "${apiBase(d.platform)}/drama/${enc(d.id)}?lang=in"
    "freereels" -> "${apiBase(d.platform)}/dramas/${enc(d.id)}?lang=id-ID"
    "bstation" -> "${apiBase(d.platform)}/view/info?id=${enc(d.id)}&lang=id_ID"
    else -> "${apiBase(d.platform)}/book?id=${enc(d.id)}&lang=id"
}

private fun dedupe(items: List<Drama>) = items.filter { it.id.isNotBlank() && it.title.isNotBlank() }.distinctBy { it.platform + "|" + it.id }.distinctBy { it.platform + "|" + normalizeKey(it.title) }
private fun mergeHomeBundles(c: HomeBundle, n: HomeBundle) = HomeBundle(dedupe(c.recommended + n.recommended), dedupe(c.popular + n.popular), dedupe(c.newest + n.newest), max(c.loadedPage, n.loadedPage), n.hasMore)

// Feed Melolo memuat rak ("Trending"), tab genre ("Peringkat", "Time Travel"),
// dan section layer yang sama-sama punya pasangan id+name. Mereka bukan drama.
// Syaratnya pakai sinyal konten nyata: cover, sinopsis, atau jumlah episode.
private fun JSONObject.hasDramaSignal(): Boolean =
    stringAny("cover", "thumb_url", "image", "poster", "coverWap", "bookCover", "posterImg", "cover_url").isNotBlank() ||
        optJSONObject("cover") != null ||
        stringAny("abstract", "introduction", "description", "synopsis", "content", "meta_description", "desc", "evaluate").isNotBlank() ||
        intAny("serial_count", "chapter_count", "chapterCount", "episode_count", "meta_episode", "total_episodes") > 0 ||
        has("subjectId") || has("key") || has("season_id") || has("ep_id") || has("aid") ||
        (has("season_id") && stringAny("title").isNotBlank() && stringAny("cover").isNotBlank())

private fun flat(any: Any?, fp: String): List<Drama> {
    val out = mutableListOf<Drama>()
    fun extractBooks(node: Any?) {
        when (node) {
            is JSONArray -> node.objects().forEach { extractBooks(it) }
            is JSONObject -> {
                val bookId = node.stringAny("book_id", "bookId", "drama_id", "subjectId", "id", "key", "season_id", "aid")
                val bookName = node.stringAny("book_name", "bookName", "drama_name", "title", "name")
                if (bookId.isNotBlank() && bookName.isNotBlank() && node.hasDramaSignal()) {
                    val d = normalize(node, fp)
                    if (d.id.isNotBlank() && d.title.isNotBlank() && !d.title.equals("Populer", true) && !d.title.equals("Romansa", true) && !d.title.equals("Ceo", true)) {
                        out += d
                    }
                }
                val keys = node.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    if (k != "categories" && k != "category_info") {
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
    return out.distinctBy { it.platform + "|" + it.id }
}

private fun normalize(o: JSONObject, fp: String): Drama {
    val p = fp
    // Bstation: season_id atau aid sebagai ID
    val id = o.stringAny("drama_id", "book_id", "bookId", "id", "subjectId", "key", "season_id", "aid")
    return Drama(
        id,
        o.stringAny("drama_name", "book_name", "bookName", "title", "bookTitle", "name"),
        cleanText(o.stringAny("introduction", "description", "meta_description", "meta_sinopsis", "shoot", "content", "synopsis", "abstract", "desc", "evaluate")),
        fixImg(o.stringAny("thumb_url", "cover_url", "coverWap", "cover", "bookCover", "image", "poster", "posterImg").ifBlank { o.coverUrl() }),
        o.intAny("chapter_count", "chapterCount", "episode_count", "meta_episode", "episode_number", "total_episodes", "chapterCnt", "totalEpisode", 0),
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
    add(o.optJSONArray("tags")); add(o.optJSONArray("tagV3s")); add(o.optJSONArray("categories"))
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
    fun autoNext() = p.getBoolean("autoNext", false)
    fun setAutoNext(v: Boolean) = p.edit().putBoolean("autoNext", v).apply()
    fun fitContain() = p.getBoolean("fitContain", false)
    fun setFitContain(v: Boolean) = p.edit().putBoolean("fitContain", v).apply()

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

    fun favs(): List<Drama> = runCatching { val a = JSONArray(p.getString("favs", "[]")); (0 until a.length()).mapNotNull { i -> a.optJSONObject(i)?.let { o -> Drama(o.stringAny("id"), o.stringAny("title"), o.stringAny("description"), o.stringAny("poster"), o.intAny("episodes", 0), o.stringAny("views"), o.optJSONArray("tags")?.let { arr -> (0 until arr.length()).mapNotNull { arr.optString(it).takeIf { s -> s.isNotBlank() } } }.orEmpty(), o.stringAny("platform").ifBlank { "melolo" }, o.intAny("subjectType", 1)) } } }.getOrDefault(emptyList())
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
private fun JSONObject.coverUrl(): String { val c = opt("cover"); return if (c is JSONObject) c.stringAny("url") else "" }
private fun fixImg(u: String): String { if (u.contains("fizzopic.org") && u.contains(".heic")) { val m = Regex("novel-images-apsoutheast/([a-f0-9]+)~").find(u); if (m != null) return "https://p19-novel-sg.ibyteimg.com/img/novel-images-sg/${m.groupValues[1]}~tplv-resize:570:810.jpg" }; return u }
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
        .replace(" ", "%20")
}
private fun enc(s: String) = URLEncoder.encode(s, "UTF-8")
private fun normalizeKey(s: String) = s.lowercase().replace(Regex("[^a-z0-9\\p{L}\\s]"), " ").replace(Regex("\\s+"), " ").trim()
