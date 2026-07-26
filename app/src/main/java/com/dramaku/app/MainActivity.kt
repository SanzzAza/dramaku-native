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
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.ui.PlayerView
import androidx.media3.ui.AspectRatioFrameLayout
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.util.Calendar
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import kotlin.math.max
import kotlin.math.min

// ─────────────────────────────────────────────────────────────────
// DESIGN SYSTEM — Clean, minimal, premium
// ─────────────────────────────────────────────────────────────────

private object DS {
    // Backgrounds
    val Bg = Color(0xFF0A0E14)
    val Bg2 = Color(0xFF0F1520)
    val Bg3 = Color(0xFF161D2A)
    val Bg4 = Color(0xFF1C2535)

    // Brand
    val Green = Color(0xFF00E5A0)
    val GreenDark = Color(0xFF00C98B)
    val GreenDim = Color(0xFF00E5A0).copy(alpha = 0.12f)

    // Text
    val White = Color(0xFFF1F5F9)
    val Text = Color(0xFFCBD5E1)
    val Muted = Color(0xFF64748B)
    val Hint = Color(0xFF475569)

    // Semantic
    val Red = Color(0xFFEF4444)
    val RedDim = Color(0xFFEF4444).copy(alpha = 0.12f)
    val Amber = Color(0xFFF59E0B)

    // Gradients
    val GreenGrad = listOf(Color(0xFF00E5A0), Color(0xFF00C4FF))
    val CardGrad = listOf(Color(0xFF161D2A), Color(0xFF0F1520))
    val OverlayBottom = listOf(Color.Transparent, Color(0xCC0A0E14))
    val OverlayFull = listOf(Color.Transparent, Color.Transparent, Color(0xAA0A0E14))
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
            surface = DS.Bg2,
            onPrimary = Color.Black,
            onBackground = DS.White,
            onSurface = DS.White
        )
    ) {
        App()
    }
}

// ─────────────────────────────────────────────────────────────────
// DATA MODELS
// ─────────────────────────────────────────────────────────────────

private enum class Tab(val label: String, val icon: ImageVector, val showNav: Boolean = true) {
    Clips("Cuplikan", Icons.Rounded.PlayCircle),
    Home("Beranda", Icons.Rounded.Home),
    Rewards("Hadiah", Icons.Rounded.CardGiftcard),
    Library("Koleksi", Icons.Rounded.Bookmark),
    Profile("Profil", Icons.Rounded.Person),
    Search("Cari", Icons.Rounded.Search, false)
}

private data class PlatformInfo(val id: String, val label: String, val base: String, val logoUrl: String = "")
private data class Drama(
    val id: String, val title: String, val description: String = "", val poster: String = "",
    val episodes: Int = 0, val views: String = "", val tags: List<String> = emptyList(),
    val platform: String = "melolo", val subjectType: Int = 1
)
private data class EpisodeInfo(val number: Int, val streaming: String = "")
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
    PlatformInfo("melolo", "Melolo", "https://new-api.sonzaix.workers.dev/melolo", "https://www.google.com/s2/favicons?sz=256&domain=melolo.id"),
    PlatformInfo("freereels", "FreeReels", "https://new-api.sonzaix.workers.dev/freereels", "https://static-v1.mydramawave.com/frontend_static/Logo.png"),
    PlatformInfo("flickreels", "FlickReels", "https://new-api.sonzaix.workers.dev/flickreels", "https://www.google.com/s2/favicons?sz=256&domain=flickreels.com"),
    PlatformInfo("dramanova", "DramaNova", "https://new-api.sonzaix.workers.dev/dramanova", "https://www.google.com/s2/favicons?sz=256&domain=dramanova.app"),
    PlatformInfo("reelshort", "ReelShort", "https://new-api.sonzaix.workers.dev/reelshort", "https://v-mps.crazymaplestudios.com/images/211d3420-d721-11f0-84ad-6b5693b490dc.png"),
    PlatformInfo("netshort", "NetShort", "https://new-api.sonzaix.workers.dev/netshort", "https://netshort.com/assets/logo/logo.png"),
    PlatformInfo("dramabox", "DramaBox", "https://new-api.sonzaix.workers.dev/dramabox", "https://www.google.com/s2/favicons?sz=256&domain=dramaboxapp.com"),
    PlatformInfo("goodshort", "GoodShort", "https://new-api.sonzaix.workers.dev/goodshort", "https://acfs3.goodshort.com/dist/src/assets/images/pc/common/1b3b5f4e-logo.png"),
    PlatformInfo("moviebox", "MovieBox", "https://new-api.sonzaix.workers.dev/moviebox", "https://www.google.com/s2/favicons?sz=256&domain=moviebox.ng"),
    PlatformInfo("drakor", "Drakor", "https://new-api.sonzaix.workers.dev/drama", "https://www.google.com/s2/favicons?sz=256&domain=drakor.id")
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
                        BottomNavBar(tab) { tab = it }
                    }
                ) { pad ->
                    Box(Modifier.padding(pad).fillMaxSize()) {
                        when (tab) {
                            Tab.Home -> HomeScreen(
                                platformId = selPlatform, state = homeState,
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
                                onResume = { h -> pendingResume = h; selectedDrama = Drama(h.id, h.title, poster = h.poster, platform = h.platform) }
                            )
                            Tab.Search -> SearchScreen(repo, store, selPlatform, onDrama = { selectedDrama = it }, dataTick = dataTick, bump = { dataTick++ })
                            Tab.Clips -> ClipsScreen(homeState, repo, store, onBack = { tab = Tab.Home }, onWatchFull = { playerSession = PlayerSession(it, 1) }, onOpenDetail = { selectedDrama = it })
                            Tab.Rewards -> PlaceholderScreen("Hadiah", "Fitur reward sedang disiapkan", Icons.Rounded.CardGiftcard)
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
// BOTTOM NAV — clean pill indicator
// ─────────────────────────────────────────────────────────────────

@Composable
private fun BottomNavBar(selected: Tab, onSelect: (Tab) -> Unit) {
    Surface(color = DS.Bg2.copy(alpha = 0.95f), tonalElevation = 0.dp) {
        Column {
            Box(Modifier.fillMaxWidth().height(0.5.dp).background(DS.Bg4))
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Tab.values().filter { it.showNav }.forEach { t ->
                    val active = t == selected
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSelect(t) }
                            .padding(vertical = 6.dp)
                    ) {
                        Icon(
                            t.icon, contentDescription = t.label,
                            tint = if (active) DS.Green else DS.Hint,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            t.label,
                            color = if (active) DS.Green else DS.Hint,
                            fontSize = 10.sp,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// HOME SCREEN — clean streaming layout
// ─────────────────────────────────────────────────────────────────

@Composable
private fun HomeScreen(
    platformId: String, state: Load<HomeBundle>, history: List<HistoryItem>,
    remoteConfig: NativeRemoteConfig?, remoteError: String?,
    loadingMore: Boolean, loadMoreError: String?,
    onLoadMore: () -> Unit, onPlatform: (String) -> Unit, onRefresh: () -> Unit,
    onDrama: (Drama) -> Unit, onSearch: () -> Unit, onRandom: () -> Unit,
    onClips: () -> Unit, onResume: (HistoryItem) -> Unit,
    category: HomeCategory? = null, onExitCategory: () -> Unit = {}
) {
    val listState = rememberLazyListState()
    var requestedPage by remember(platformId) { mutableIntStateOf(0) }
    val loadedPage = (state as? Load.Ok)?.data?.loadedPage ?: 0
    val chips = category?.let { cat -> Platforms.filter { cat.platforms.contains(it.id) } } ?: Platforms
    LaunchedEffect(platformId, loadedPage) { if (loadedPage <= 1) requestedPage = loadedPage }

    LaunchedEffect(listState, state, platformId, loadingMore) {
        snapshotFlow {
            val info = listState.layoutInfo
            val last = info.visibleItemsInfo.lastOrNull()
            last != null && last.index >= info.totalItemsCount - 3 && (last.offset + last.size) <= info.viewportEndOffset + 900
        }.collect { near ->
            val data = (state as? Load.Ok)?.data ?: return@collect
            val np = data.loadedPage + 1
            if (near && data.hasMore && !loadingMore && requestedPage != np) {
                requestedPage = np; onLoadMore()
            }
        }
    }

    LazyColumn(state = listState, modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 18.dp)) {
        item {
            HomePremiumHeader(
                platformId = platformId,
                category = category,
                remoteConfig = remoteConfig,
                remoteError = remoteError,
                historyCount = history.size,
                chips = chips,
                onExitCategory = onExitCategory,
                onSearch = onSearch,
                onRefresh = onRefresh,
                onRandom = onRandom,
                onClips = onClips,
                onPlatform = onPlatform
            )
        }

        when (state) {
            Load.Loading, Load.Idle -> item { ShimmerLoader() }
            is Load.Err -> item { ErrorCard(state.message, onRefresh) }
            is Load.Ok -> {
                val data = state.data
                val all = data.popular + data.newest + data.recommended
                val spotlight = all.firstOrNull { it.poster.isNotBlank() }

                if (spotlight != null) item { HeroCard(spotlight, onDrama) }

                if (history.isNotEmpty()) item {
                    Section("Lanjutkan", "Lanjut dari progress terakhir tanpa ribet") {
                        LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            items(history.take(8), key = { it.platform + it.id }) { h -> ContinueCard(h, onResume) }
                        }
                    }
                }

                if (data.popular.isNotEmpty()) item {
                    Section("Paling Hot", "Yang lagi gacor dan paling sering ditonton") {
                        LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            items(data.popular.take(20), key = { it.platform + it.id }) { d -> DramaCard(d, onDrama, Modifier.width(150.dp)) }
                        }
                    }
                }

                if (data.newest.isNotEmpty()) item {
                    Section("Rilis Terbaru", "Update baru masuk biar kamu gak ketinggalan") {
                        Column(Modifier.padding(horizontal = 20.dp)) {
                            data.newest.chunked(2).forEach { row ->
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    row.forEach { d -> DramaCard(d, onDrama, Modifier.weight(1f)) }
                                    repeat(2 - row.size) { Spacer(Modifier.weight(1f)) }
                                }
                                Spacer(Modifier.height(12.dp))
                            }
                        }
                    }
                }

                if (data.recommended.isNotEmpty()) item {
                    Section("Pilihan Buat Kamu", "Kurasi yang lebih premium buat sesi nonton malam ini") {
                        Column(Modifier.padding(horizontal = 20.dp)) {
                            data.recommended.chunked(2).forEach { row ->
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    row.forEach { d -> DramaCard(d, onDrama, Modifier.weight(1f)) }
                                    repeat(2 - row.size) { Spacer(Modifier.weight(1f)) }
                                }
                                Spacer(Modifier.height(12.dp))
                            }
                        }
                    }
                }

                item {
                    Column(Modifier.fillMaxWidth().padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        if (loadingMore) {
                            CircularProgressIndicator(color = DS.Green, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("Memuat batch berikutnya...", color = DS.Hint, fontSize = 11.sp)
                        } else if (!data.hasMore) {
                            Text("Koleksi home udah mentok, tinggal gas nonton", color = DS.Hint, fontSize = 11.sp)
                        }
                        loadMoreError?.let { Text(it, color = DS.Red, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp)) }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// CATEGORY HOME — layar awal pintu masuk (desain ala SonzaixBox)
// ─────────────────────────────────────────────────────────────────

@Composable
private fun CategoryHomeScreen(onSelect: (HomeCategory) -> Unit, onSettings: () -> Unit) {
    val ctx = LocalContext.current
    val greeting = remember { Greetings.forHour(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) }
    Box(Modifier.fillMaxSize().background(Color(0xFF05070C))) {
        // Glow merah lembut di atas seperti referensi
        Box(
            Modifier.fillMaxWidth().height(280.dp)
                .background(Brush.verticalGradient(listOf(Color(0x2EEF3A5F), Color.Transparent)))
        )
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(56.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text("${greeting.text} ${greeting.emoji}", color = Color(0xFFB9C0CE), fontSize = 17.sp, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(6.dp))
                    Text("Dramaku", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Black)
                }
                Spacer(Modifier.width(8.dp))
                Box(
                    Modifier.size(44.dp).clip(CircleShape).background(Color(0xFF171B25)).clickable(onClick = onSettings),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Settings, "Pengaturan", tint = Color(0xFFAAB3C2), modifier = Modifier.size(22.dp))
                }
            }
            Spacer(Modifier.height(26.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                CategoryMenuCard(
                    HomeCategory.ShortDrama, Icons.Rounded.PlayArrow,
                    tileColor = Color(0xFFF04469),
                    gradient = listOf(Color(0xFF3B1120), Color(0xFF1B0911)),
                    modifier = Modifier.weight(1f)
                ) { onSelect(HomeCategory.ShortDrama) }
                CategoryMenuCard(
                    HomeCategory.MovieDrama, Icons.Rounded.Movie,
                    tileColor = Color(0xFF3B9BF0),
                    gradient = listOf(Color(0xFF12293E), Color(0xFF0A1622)),
                    modifier = Modifier.weight(1f)
                ) { onSelect(HomeCategory.MovieDrama) }
            }
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                CategoryMenuCard(
                    HomeCategory.MovieBox, Icons.Rounded.Tv,
                    tileColor = Color(0xFFF5832B),
                    gradient = listOf(Color(0xFF3A240D), Color(0xFF1D1307)),
                    modifier = Modifier.weight(1f)
                ) { onSelect(HomeCategory.MovieBox) }
                CategoryMenuCard(
                    HomeCategory.Anime, Icons.Rounded.Toll,
                    tileColor = Color(0xFF9D5CF0),
                    gradient = listOf(Color(0xFF33124A), Color(0xFF1B0E2B)),
                    modifier = Modifier.weight(1f)
                ) { onSelect(HomeCategory.Anime) }
            }
            Spacer(Modifier.height(14.dp))

            WideMenuCard(
                HomeCategory.Manga, Icons.Rounded.Image,
                tileColor = Color(0xFF5E6EE8),
                gradient = listOf(Color(0xFF1A1E2E), Color(0xFF12151F))
            ) { onSelect(HomeCategory.Manga) }

            Spacer(Modifier.height(28.dp))
            Row(
                Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(50))
                    .border(1.dp, Color(0xFF2A3040), RoundedCornerShape(50))
                    .clickable { Toast.makeText(ctx, "Link traktir kopi segera hadir ☕", Toast.LENGTH_SHORT).show() }
                    .padding(vertical = 14.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("☕  Traktir Kopi untuk Developer", color = Color(0xFF98A1B3), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(16.dp))
            Text(
                "Developed by SanzzXD", color = Color(0xFF4A5163), fontSize = 12.sp,
                textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun CategoryMenuCard(
    category: HomeCategory,
    icon: ImageVector,
    tileColor: Color,
    gradient: List<Color>,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier.height(184.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(Brush.linearGradient(gradient))
            .clickable(onClick = onClick)
            .padding(18.dp)
    ) {
        Box(
            Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(tileColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, category.title, tint = Color.White, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.height(14.dp))
        Text(category.title, color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold, lineHeight = 23.sp)
        Spacer(Modifier.height(6.dp))
        Text(category.subtitle, color = Color(0xFF94A0B5), fontSize = 12.sp, lineHeight = 15.sp)
        if (category.comingSoon) {
            Spacer(Modifier.height(8.dp))
            ComingSoonBadge()
        }
    }
}

@Composable
private fun ComingSoonBadge() {
    Text(
        "Segera hadir",
        color = Color(0xFF8C93A5),
        fontSize = 10.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Color(0x14FFFFFF))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}

@Composable
private fun WideMenuCard(
    category: HomeCategory,
    icon: ImageVector,
    tileColor: Color,
    gradient: List<Color>,
    onClick: () -> Unit
) {
    Row(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(Brush.linearGradient(gradient))
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(tileColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, category.title, tint = Color.White, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(category.title, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                if (category.comingSoon) {
                    Spacer(Modifier.width(8.dp))
                    ComingSoonBadge()
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(category.subtitle, color = Color(0xFF94A0B5), fontSize = 12.sp)
        }
        Icon(Icons.Rounded.ChevronRight, null, tint = Color(0xFF6E7890), modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun SettingsOverlay(store: LocalStore, dataTick: Int, bump: () -> Unit, onClose: () -> Unit) {
    Box(Modifier.fillMaxSize().background(DS.Bg)) {
        Box(Modifier.fillMaxSize().padding(top = 54.dp)) {
            ProfileScreen(store, dataTick, bump)
        }
        IconButton(
            onClick = onClose,
            modifier = Modifier.padding(start = 12.dp, top = 10.dp).clip(CircleShape).background(DS.Bg3)
        ) {
            Icon(Icons.Rounded.ArrowBack, "Kembali", tint = DS.White, modifier = Modifier.size(20.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// REUSABLE COMPONENTS
// ─────────────────────────────────────────────────────────────────

@Composable
private fun HomePremiumHeader(
    platformId: String,
    category: HomeCategory?,
    remoteConfig: NativeRemoteConfig?,
    remoteError: String?,
    historyCount: Int,
    chips: List<PlatformInfo>,
    onExitCategory: () -> Unit,
    onSearch: () -> Unit,
    onRefresh: () -> Unit,
    onRandom: () -> Unit,
    onClips: () -> Unit,
    onPlatform: (String) -> Unit
) {
    val message = remoteConfig?.message?.takeIf { it.enabled }
    val selectedState = remoteConfig?.platform(platformId)
    val categoryTitle = category?.title.orEmpty()
    val title = when {
        category == null -> "Beranda Premium"
        category?.platforms?.size == 1 -> categoryTitle.ifBlank { "Beranda Premium" }
        else -> "$categoryTitle Premium"
    }
    val subtitle = when {
        category?.platforms?.size == 1 -> category?.subtitle.orEmpty()
        category != null -> "Kurasi mewah buat ${platformLabel(platformId)}"
        else -> "Nonton lebih rapih, lebih mahal look-nya, lebih gacor feel-nya"
    }

    Box(
        Modifier
            .padding(horizontal = 20.dp, vertical = 14.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF1A2232), Color(0xFF101722), Color(0xFF0B0F16))
                )
            )
            .border(1.dp, Color(0x1FFFFFFF), RoundedCornerShape(28.dp))
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(190.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0x2200E5A0), Color(0x1600C4FF), Color.Transparent)
                    )
                )
        )
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (category != null) {
                    Box(
                        Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0x14FFFFFF))
                            .clickable(onClick = onExitCategory),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.Apps, "Semua kategori", tint = DS.White, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(10.dp))
                }
                Column(Modifier.weight(1f)) {
                    Text("Dramaku", color = DS.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
                    Text(title, color = Color(0xFF92A2BD), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
                HeaderCircleButton(Icons.Rounded.Search, "Cari", onSearch)
                Spacer(Modifier.width(8.dp))
                HeaderCircleButton(Icons.Rounded.Refresh, "Refresh", onRefresh)
            }

            Spacer(Modifier.height(18.dp))
            Text(subtitle, color = DS.White, fontSize = 24.sp, fontWeight = FontWeight.Black, lineHeight = 28.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                selectedState?.reason?.takeIf { it.isNotBlank() && it != "Aktif" }
                    ?: "Pilih platform yang lagi paling cocok, terus gas nonton tanpa ribet.",
                color = Color(0xFFA7B5CB),
                fontSize = 12.sp,
                lineHeight = 17.sp
            )

            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HeaderStatChip(if ((selectedState?.enabled ?: true)) "Aktif" else "Maintenance", if ((selectedState?.enabled ?: true)) DS.Green else DS.Amber)
                HeaderStatChip("$historyCount riwayat", Color(0xFF7DD3FC))
                HeaderStatChip(platformLabel(platformId), Color(0xFFFDA4AF))
            }

            val alertText = when {
                message != null -> listOf(message.title, message.text).filter { it.isNotBlank() }.joinToString(" • ")
                remoteError != null -> "Status server belum kebaca: $remoteError"
                else -> ""
            }
            if (alertText.isNotBlank()) {
                Spacer(Modifier.height(14.dp))
                Surface(color = Color(0x12FFFFFF), shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Campaign, null, tint = DS.Green, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(alertText, color = DS.Text, fontSize = 11.sp, lineHeight = 15.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                }
            }

            if (chips.size > 1) {
                Spacer(Modifier.height(16.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(chips, key = { it.id }) { p ->
                        val sel = p.id == platformId
                        val st = remoteConfig?.platform(p.id)
                        val enabled = st?.enabled ?: true
                        Surface(
                            color = if (sel) Color(0x1E00E5A0) else Color(0x14FFFFFF),
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier.clickable(enabled = enabled) { onPlatform(p.id) }
                        ) {
                            Row(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                AsyncImage(p.logoUrl, p.label, Modifier.size(18.dp).clip(RoundedCornerShape(6.dp)))
                                Spacer(Modifier.width(8.dp))
                                Text(p.label, color = if (enabled) DS.White else DS.Hint, fontSize = 12.sp, fontWeight = if (sel) FontWeight.Bold else FontWeight.Medium)
                                if (!enabled) {
                                    Spacer(Modifier.width(6.dp))
                                    Box(Modifier.size(7.dp).clip(CircleShape).background(DS.Amber))
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                QuickAction("Acak", Icons.Rounded.Shuffle, Modifier.weight(1f), onRandom)
                QuickAction("Cuplikan", Icons.Rounded.PlayCircle, Modifier.weight(1f), onClips)
            }
        }
    }
}

@Composable
private fun HeaderCircleButton(icon: ImageVector, label: String, onClick: () -> Unit) {
    Box(
        Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(Color(0x14FFFFFF))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, label, tint = DS.White, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun HeaderStatChip(text: String, color: Color) {
    Surface(color = Color(0x14FFFFFF), shape = RoundedCornerShape(50)) {
        Row(Modifier.padding(horizontal = 10.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(color))
            Spacer(Modifier.width(6.dp))
            Text(text, color = DS.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun PlatformLogo(platformId: String, modifier: Modifier = Modifier) {
    val info = platform(platformId)
    if (info.logoUrl.isNotBlank()) {
        AsyncImage(info.logoUrl, info.label, modifier.clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Fit)
    } else {
        Box(modifier.clip(RoundedCornerShape(8.dp)).background(DS.Bg4), contentAlignment = Alignment.Center) {
            Text(info.label.take(1), color = DS.White, fontWeight = FontWeight.Black, fontSize = 10.sp)
        }
    }
}

@Composable
private fun PlatformBadge(platformId: String, compact: Boolean = false) {
    Surface(color = Color(0x14FFFFFF), shape = RoundedCornerShape(50)) {
        Row(
            Modifier.padding(horizontal = if (compact) 8.dp else 10.dp, vertical = if (compact) 6.dp else 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlatformLogo(platformId, Modifier.size(if (compact) 16.dp else 18.dp))
            Spacer(Modifier.width(6.dp))
            Text(platformLabel(platformId), color = DS.White, fontSize = if (compact) 10.sp else 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SearchDramaCard(drama: Drama, onClick: (Drama) -> Unit) {
    Surface(
        color = DS.Bg3,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick(drama) }
    ) {
        Column(Modifier.padding(8.dp)) {
            Box {
                PosterImage(drama.poster, drama.title, Modifier.fillMaxWidth().aspectRatio(0.8f))
                Box(Modifier.align(Alignment.TopStart).padding(8.dp)) {
                    PlatformBadge(drama.platform, compact = true)
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(drama.title.ifBlank { "Tanpa Judul" }, color = DS.White, fontSize = 13.sp, fontWeight = FontWeight.Black, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(4.dp))
            Text(drama.description.ifBlank { "Hasil pencarian dari ${platformLabel(drama.platform)}" }, color = DS.Muted, fontSize = 10.sp, lineHeight = 14.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun Section(title: String, subtitle: String = "", content: @Composable () -> Unit) {
    Column(Modifier.padding(top = 22.dp)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), verticalAlignment = Alignment.Bottom) {
            Column(Modifier.weight(1f)) {
                Text(title, color = DS.White, fontSize = 19.sp, fontWeight = FontWeight.Black)
                if (subtitle.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(subtitle, color = DS.Muted, fontSize = 11.sp, lineHeight = 15.sp)
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        content()
    }
}

@Composable
private fun QuickAction(label: String, icon: ImageVector, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        color = Color(0x14FFFFFF), shape = RoundedCornerShape(18.dp),
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Row(Modifier.padding(horizontal = 14.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(34.dp).clip(RoundedCornerShape(12.dp)).background(Brush.linearGradient(listOf(DS.Green, Color(0xFF00C4FF)))),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = Color.Black, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(label, color = DS.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text("Buka cepat", color = DS.Muted, fontSize = 10.sp)
            }
            Icon(Icons.Rounded.ChevronRight, null, tint = DS.Hint, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun HeroCard(drama: Drama, onClick: (Drama) -> Unit) {
    Box(
        Modifier
            .padding(horizontal = 20.dp)
            .height(260.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick(drama) }
            .background(DS.Bg3)
    ) {
        AsyncImage(drama.poster, drama.title, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    listOf(Color(0x33000000), Color.Transparent, Color(0xE60A0E14))
                )
            )
        )
        Row(
            Modifier.align(Alignment.TopStart).padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(color = Color(0xCC111827), shape = RoundedCornerShape(50)) {
                Text("Featured", color = DS.Green, fontSize = 10.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp))
            }
            PlatformBadge(drama.platform, compact = false)
        }
        Column(Modifier.align(Alignment.BottomStart).padding(18.dp)) {
            Text("Pilihan utama malam ini", color = Color(0xFF9AE6B4), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.7.sp)
            Spacer(Modifier.height(6.dp))
            Text(drama.title, color = DS.White, fontSize = 24.sp, fontWeight = FontWeight.Black, lineHeight = 28.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(6.dp))
            Text(
                drama.description.take(120).ifBlank { "Koleksi premium dari ${platformLabel(drama.platform)}" },
                color = Color(0xFFD4DEEA),
                fontSize = 12.sp,
                lineHeight = 17.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(color = DS.Green, shape = RoundedCornerShape(12.dp)) {
                    Row(Modifier.padding(horizontal = 14.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.PlayArrow, null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Tonton Sekarang", color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.Black)
                    }
                }
                Surface(color = Color(0x1AFFFFFF), shape = RoundedCornerShape(12.dp)) {
                    Text("${drama.episodes.coerceAtLeast(1)} Episode", color = DS.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp))
                }
            }
        }
    }
}

@Composable
private fun ContinueCard(h: HistoryItem, onClick: (HistoryItem) -> Unit) {
    Column(
        Modifier
            .width(142.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(DS.Bg3)
            .clickable { onClick(h) }
            .padding(8.dp)
    ) {
        Box {
            PosterImage(h.poster, h.title, Modifier.fillMaxWidth().height(198.dp))
            Surface(color = Color(0xCC0B1220), shape = RoundedCornerShape(8.dp), modifier = Modifier.align(Alignment.TopStart).padding(8.dp)) {
                Text("Ep ${h.episode}", color = DS.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
            }
            Surface(color = Color(0xA600E5A0), shape = RoundedCornerShape(8.dp), modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)) {
                Text("${h.pct.coerceAtLeast(0)}%", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
            }
            LinearProgressIndicator(
                progress = (h.pct / 100f).coerceIn(0f, 1f),
                color = DS.Green,
                trackColor = Color(0x66000000),
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(4.dp)
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(h.title, color = DS.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(8.dp))
        PlatformBadge(h.platform, compact = true)
        Spacer(Modifier.height(4.dp))
        Text(if (h.pct > 0) "Lanjut dari ${h.pct}%" else "Siap dilanjut", color = DS.Muted, fontSize = 11.sp)
    }
}

@Composable
private fun DramaCard(drama: Drama, onClick: (Drama) -> Unit, modifier: Modifier = Modifier.width(120.dp)) {
    Column(
        modifier
            .clip(RoundedCornerShape(20.dp))
            .background(DS.Bg3)
            .clickable { onClick(drama) }
            .padding(8.dp)
    ) {
        Box {
            PosterImage(drama.poster, drama.title, Modifier.fillMaxWidth().aspectRatio(0.72f))
            Box(Modifier.align(Alignment.TopStart).padding(8.dp)) {
                PlatformBadge(drama.platform, compact = true)
            }
            if (drama.episodes > 0) {
                Surface(color = Color(0xA600E5A0), shape = RoundedCornerShape(8.dp), modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)) {
                    Text("${drama.episodes} Ep", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp))
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(drama.title.ifBlank { "Tanpa Judul" }, color = DS.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(4.dp))
        Text(
            drama.description.ifBlank { "Koleksi premium ${platformLabel(drama.platform)}" },
            color = DS.Muted,
            fontSize = 10.sp,
            lineHeight = 14.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun PosterImage(url: String, title: String, modifier: Modifier) {
    Box(modifier.clip(RoundedCornerShape(14.dp)).background(DS.Bg3)) {
        if (url.isNotBlank()) {
            AsyncImage(url, title, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color(0x33000000)))))
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.Movie, title, tint = DS.Hint, modifier = Modifier.size(28.dp))
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
    val offset by transition.animateFloat(0f, 1200f, infiniteRepeatable(tween(1400, easing = LinearEasing), RepeatMode.Restart), label = "s")
    val brush = Brush.linearGradient(listOf(DS.Bg3, DS.Bg4, DS.Bg3), start = Offset(offset - 300f, offset - 300f), end = Offset(offset, offset))

    Column(Modifier.padding(20.dp)) {
        Box(Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(20.dp)).background(brush))
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { repeat(4) { Box(Modifier.width(70.dp).height(30.dp).clip(RoundedCornerShape(20.dp)).background(brush)) } }
        Spacer(Modifier.height(16.dp))
        repeat(3) { Box(Modifier.fillMaxWidth().height(100.dp).clip(RoundedCornerShape(14.dp)).background(brush)); Spacer(Modifier.height(10.dp)) }
    }
}

// ─────────────────────────────────────────────────────────────────
// ERROR CARD
// ─────────────────────────────────────────────────────────────────

@Composable
private fun ErrorCard(message: String, onRetry: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(color = DS.RedDim, shape = RoundedCornerShape(16.dp), modifier = Modifier.size(56.dp)) {
            Box(contentAlignment = Alignment.Center) { Icon(Icons.Rounded.ErrorOutline, null, tint = DS.Red, modifier = Modifier.size(28.dp)) }
        }
        Spacer(Modifier.height(14.dp))
        Text("Gagal memuat", color = DS.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text(message, color = DS.Muted, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 4.dp, bottom = 16.dp))
        Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = DS.Green, contentColor = Color.Black), shape = RoundedCornerShape(12.dp)) {
            Icon(Icons.Rounded.Refresh, null, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(6.dp))
            Text("Coba Lagi", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
    }
}

@Composable
private fun OfflineBanner(onRefresh: () -> Unit) {
    Surface(color = DS.Red, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.WifiOff, null, tint = DS.White, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text("Tidak ada internet", color = DS.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text("Refresh", color = DS.White, fontSize = 12.sp, fontWeight = FontWeight.Black, modifier = Modifier.clickable(onClick = onRefresh))
        }
    }
}

@Composable
private fun PlaceholderScreen(title: String, subtitle: String, icon: ImageVector) {
    Box(Modifier.fillMaxSize().background(DS.Bg), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(color = DS.GreenDim, shape = RoundedCornerShape(20.dp), modifier = Modifier.size(64.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = DS.Green, modifier = Modifier.size(30.dp)) }
            }
            Spacer(Modifier.height(14.dp))
            Text(title, color = DS.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = DS.Muted, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// CLIPS / SEARCH / LIBRARY / PROFILE — compact implementations
// ─────────────────────────────────────────────────────────────────

@Composable
private fun ClipsScreen(state: Load<HomeBundle>, repo: DramakuRepository, store: LocalStore, onBack: () -> Unit, onWatchFull: (Detail) -> Unit, onOpenDetail: (Drama) -> Unit) {
    when (state) {
        Load.Loading, Load.Idle -> Box(Modifier.fillMaxSize().background(DS.Bg), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = DS.Green); Spacer(Modifier.height(12.dp))
                Text("Menyiapkan cuplikan...", color = DS.Text, fontWeight = FontWeight.Bold)
            }
        }
        is Load.Err -> ErrorCard(state.message, onBack)
        is Load.Ok -> {
            val pool = remember(state.data) {
                (state.data.popular + state.data.newest + state.data.recommended).filter { it.id.isNotBlank() && it.poster.isNotBlank() }.distinctBy { it.platform + it.id }.take(100)
            }
            if (pool.isEmpty()) PlaceholderScreen("Cuplikan", "Belum tersedia untuk platform ini", Icons.Rounded.PlayCircle)
            else ClipFeedPlayer(pool, repo, store, onClose = onBack, onWatchFull = onWatchFull, onOpenDetail = onOpenDetail)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SearchScreen(repo: DramakuRepository, store: LocalStore, currentPlatform: String, onDrama: (Drama) -> Unit, dataTick: Int, bump: () -> Unit) {
    var q by remember { mutableStateOf("") }
    var state by remember { mutableStateOf<Load<List<Drama>>>(Load.Idle) }
    var searchPlatformId by remember { mutableStateOf(currentPlatform) }
    val recent = remember(dataTick) { store.recentSearches() }

    LaunchedEffect(currentPlatform) { searchPlatformId = currentPlatform }

    LaunchedEffect(q, searchPlatformId) {
        val query = q.trim()
        if (query.length < 2) { state = Load.Idle; return@LaunchedEffect }
        delay(400)
        state = Load.Loading
        state = runCatching {
            store.saveRecent(query)
            repo.searchPlatform(query, searchPlatformId)
        }.fold({ Load.Ok(it) }, { Load.Err(it.message ?: "Gagal") })
        bump()
    }

    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Surface(
            color = DS.Bg3,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Cari yang Lagi Gacor", color = DS.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
                        Spacer(Modifier.height(4.dp))
                        Text("Pilih platform favorit lalu cari pakai logo brand aslinya", color = DS.Muted, fontSize = 12.sp, lineHeight = 17.sp)
                    }
                    PlatformBadge(searchPlatformId, compact = false)
                }
                Spacer(Modifier.height(14.dp))
                OutlinedTextField(
                    value = q,
                    onValueChange = { q = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Cari judul drama, movie, atau episode...", color = DS.Hint) },
                    singleLine = true,
                    leadingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            PlatformLogo(searchPlatformId, Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Icon(Icons.Rounded.Search, null, tint = DS.Hint, modifier = Modifier.size(18.dp))
                        }
                    },
                    trailingIcon = {
                        if (q.isNotBlank()) {
                            IconButton(onClick = { q = "" }) {
                                Icon(Icons.Rounded.Close, "Hapus", tint = DS.Hint, modifier = Modifier.size(18.dp))
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DS.Green,
                        unfocusedBorderColor = DS.Bg4,
                        focusedTextColor = DS.White,
                        unfocusedTextColor = DS.White,
                        cursorColor = DS.Green,
                        focusedLeadingIconColor = DS.Green,
                        unfocusedLeadingIconColor = DS.Hint
                    ),
                    shape = RoundedCornerShape(18.dp)
                )
                Spacer(Modifier.height(14.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(Platforms, key = { it.id }) { p ->
                        val selected = p.id == searchPlatformId
                        Surface(
                            color = if (selected) Color(0x1E00E5A0) else Color(0x14FFFFFF),
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier.clickable { searchPlatformId = p.id }
                        ) {
                            Row(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                PlatformLogo(p.id, Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(p.label, color = DS.White, fontSize = 12.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium)
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Box(Modifier.weight(1f).fillMaxWidth()) {
            when (state) {
                Load.Idle -> {
                    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                        Text("Trending", color = DS.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
                        Spacer(Modifier.height(10.dp))
                        val trending = listOf("CEO", "Romantis", "Balas Dendam", "Korea", "China", "Action", "Cinta Kontrak", "Ongoing")
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            trending.forEach { keyword -> Chip(keyword) { q = keyword } }
                        }

                        if (recent.isNotEmpty()) {
                            Spacer(Modifier.height(22.dp))
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Text("Terakhir Dicari", color = DS.White, fontSize = 16.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
                                Text("Hapus", color = DS.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { store.clearRecentSearches(); bump() })
                            }
                            Spacer(Modifier.height(10.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                recent.forEach { keyword ->
                                    Surface(color = DS.Bg3, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().clickable { q = keyword }) {
                                        Row(Modifier.padding(horizontal = 14.dp, vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Rounded.History, null, tint = DS.Green, modifier = Modifier.size(17.dp))
                                            Spacer(Modifier.width(10.dp))
                                            Text(keyword, color = DS.Text, fontSize = 13.sp, modifier = Modifier.weight(1f))
                                            PlatformBadge(searchPlatformId, compact = true)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                Load.Loading -> {
                    Column {
                        LinearProgressIndicator(color = DS.Green, trackColor = DS.Bg4, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(14.dp))
                        Text("Mencari di ${platformLabel(searchPlatformId)}...", color = DS.Muted, fontSize = 12.sp)
                    }
                }
                is Load.Err -> ErrorCard((state as Load.Err).message) { q = "$q " }
                is Load.Ok -> {
                    val all = (state as Load.Ok<List<Drama>>).data
                    if (all.isEmpty()) {
                        Column(Modifier.fillMaxWidth().padding(top = 40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Surface(color = DS.Bg3, shape = RoundedCornerShape(18.dp), modifier = Modifier.size(64.dp)) {
                                Box(contentAlignment = Alignment.Center) { PlatformLogo(searchPlatformId, Modifier.size(28.dp)) }
                            }
                            Spacer(Modifier.height(14.dp))
                            Text("Tidak ada hasil di ${platformLabel(searchPlatformId)}", color = DS.White, fontSize = 15.sp, fontWeight = FontWeight.Black)
                            Text("Coba kata kunci lain atau pindah platform", color = DS.Hint, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                        }
                    } else {
                        Column(Modifier.fillMaxSize()) {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Text("${all.size} ditemukan", color = DS.Muted, fontSize = 12.sp, modifier = Modifier.weight(1f))
                                PlatformBadge(searchPlatformId, compact = true)
                            }
                            Spacer(Modifier.height(10.dp))
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(all, key = { it.platform + it.id }) { d -> SearchDramaCard(d, onDrama) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LibraryScreen(store: LocalStore, dataTick: Int, onDrama: (Drama) -> Unit) {
    val ctx = LocalContext.current
    val history = remember(dataTick) { store.history(dataTick) }
    val favs = remember(dataTick) { store.favs() }
    var showFav by remember { mutableStateOf(false) }
    var localTick by remember { mutableIntStateOf(0) }

    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Text("Koleksi", color = DS.White, fontSize = 26.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Chip("Riwayat (${history.size})", !showFav) { showFav = false }
            Chip("Favorit (${favs.size})", showFav) { showFav = true }
        }
        Spacer(Modifier.height(14.dp))
        if (showFav) {
            if (favs.isEmpty()) {
                EmptyState("Belum ada favorit", "Tambahkan drama ke favorit dari halaman detail", Icons.Rounded.FavoriteBorder)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(favs, key = { it.platform + it.id }) { d ->
                        ListItem(d.title, platformLabel(d.platform), d.poster, onDelete = {
                            store.removeFav(d.id, d.platform); localTick++; Toast.makeText(ctx, "Dihapus dari favorit", Toast.LENGTH_SHORT).show()
                        }) { onDrama(d) }
                    }
                }
            }
        } else {
            if (history.isEmpty()) {
                EmptyState("Belum ada riwayat", "Mulai nonton drama untuk melihat riwayat", Icons.Rounded.History)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(history, key = { it.id + it.platform }) { h ->
                        ListItem(
                            h.title, "Ep ${h.episode}${if (h.pct > 0) " · ${h.pct}%" else ""}", h.poster,
                            onDelete = {
                                store.removeHistory(h.id, h.platform); localTick++; Toast.makeText(ctx, "Dihapus dari riwayat", Toast.LENGTH_SHORT).show()
                            }
                        ) { onDrama(Drama(h.id, h.title, poster = h.poster, platform = h.platform)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(title: String, subtitle: String, icon: ImageVector) {
    Column(Modifier.fillMaxWidth().padding(top = 60.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(color = DS.Bg3, shape = RoundedCornerShape(20.dp), modifier = Modifier.size(72.dp)) {
            Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = DS.Hint, modifier = Modifier.size(32.dp)) }
        }
        Spacer(Modifier.height(16.dp))
        Text(title, color = DS.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text(subtitle, color = DS.Hint, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 4.dp, start = 32.dp, end = 32.dp))
    }
}

@Composable
private fun ListItem(title: String, subtitle: String, poster: String, onDelete: (() -> Unit)? = null, onClick: () -> Unit) {
    Surface(color = DS.Bg3, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            PosterImage(poster, title, Modifier.width(56.dp).height(80.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = DS.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(subtitle, color = DS.Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))
            }
            if (onDelete != null) {
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Rounded.DeleteOutline, "Hapus", tint = DS.Hint, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

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

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Profil", color = DS.White, fontSize = 26.sp, fontWeight = FontWeight.Black)
                Text("Pengaturan aplikasi", color = DS.Muted, fontSize = 12.sp)
            }
            Box(Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(Brush.linearGradient(DS.GreenGrad)), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.PlayArrow, "Dramaku", tint = Color.Black, modifier = Modifier.size(22.dp))
            }
        }
        Spacer(Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Stat("$hCount", "Riwayat", Modifier.weight(1f))
            Stat("$fCount", "Favorit", Modifier.weight(1f))
            Stat("$rCount", "Recent", Modifier.weight(1f))
        }
        Spacer(Modifier.height(20.dp))
        Text("Pemutaran", color = DS.Text, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
        Spacer(Modifier.height(8.dp))
        SettingItem("Hemat data", "Prioritaskan 480p", dataSaver) { dataSaver = it; store.setDataSaver(it); bump() }
        SettingItem("Auto next", "Lanjut otomatis", autoNext) { autoNext = it; store.setAutoNext(it); bump() }
        SettingItem("Rasio asli", "Video tanpa crop", fitContain) { fitContain = it; store.setFitContain(it); bump() }
        Spacer(Modifier.height(16.dp))
        Text("Info", color = DS.Text, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
        Spacer(Modifier.height(8.dp))
        SettingRow("Tentang Dramaku") { dialog = "about" }
        SettingRow("Privasi") { dialog = "privacy" }
        SettingRow("Disclaimer") { dialog = "disclaimer" }
        Spacer(Modifier.height(16.dp))
        Text("Hapus Data", color = DS.Red, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
        Spacer(Modifier.height(8.dp))
        DangerRow("Hapus riwayat") { store.clearHistory(); bump(); Toast.makeText(ctx, "Riwayat dihapus", Toast.LENGTH_SHORT).show() }
        DangerRow("Hapus favorit") { store.clearFavs(); bump(); Toast.makeText(ctx, "Favorit dihapus", Toast.LENGTH_SHORT).show() }
        DangerRow("Hapus pencarian") { store.clearRecentSearches(); bump(); Toast.makeText(ctx, "Pencarian dihapus", Toast.LENGTH_SHORT).show() }
    }

    dialog?.let { type ->
        val (title, body) = when (type) {
            "privacy" -> "Privasi" to "Data history, favorit, dan progress disimpan lokal di perangkat kamu. Dramaku tidak meng-host video dan tidak mengumpulkan data pribadi."
            "disclaimer" -> "Disclaimer" to "Semua konten milik platform masing-masing. Dramaku adalah aggregator UI/client. Gunakan dengan bijak dan hormati hak cipta pemilik konten."
            else -> {
                val version = try { ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionName } catch (_: Exception) { "5.0" }
                "Tentang Dramaku" to "Versi: $version\n\nDramaku Native adalah aplikasi aggregator drama pendek & film dengan:\n\n• ExoPlayer native untuk pemutaran video\n• Search lintas 10+ platform\n• Vertical swipe player\n• History & favorit lokal\n• Mode hemat data\n\nDikembangkan dengan Kotlin + Jetpack Compose"
            }
        }
        AlertDialog(
            onDismissRequest = { dialog = null },
            confirmButton = { TextButton(onClick = { dialog = null }) { Text("Tutup", color = DS.Green) } },
            title = { Text(title, color = DS.White, fontWeight = FontWeight.Bold) },
            text = { Text(body, color = DS.Muted, fontSize = 13.sp, lineHeight = 19.sp) },
            containerColor = DS.Bg2
        )
    }
}

@Composable
private fun Stat(value: String, label: String, modifier: Modifier) {
    Surface(color = DS.Bg3, shape = RoundedCornerShape(14.dp), modifier = modifier) {
        Column(Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, color = DS.Green, fontWeight = FontWeight.Black, fontSize = 20.sp)
            Text(label, color = DS.Muted, fontSize = 11.sp)
        }
    }
}

@Composable
private fun SettingItem(title: String, subtitle: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Surface(color = DS.Bg3, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, color = DS.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text(subtitle, color = DS.Muted, fontSize = 11.sp)
            }
            Switch(checked, onChecked, colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = DS.Green, uncheckedThumbColor = DS.Muted, uncheckedTrackColor = DS.Bg4, uncheckedBorderColor = DS.Bg4))
        }
    }
}

@Composable
private fun SettingRow(title: String, onClick: () -> Unit) {
    Surface(color = DS.Bg3, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable(onClick = onClick)) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(title, color = DS.White, fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
            Icon(Icons.Rounded.ChevronRight, null, tint = DS.Hint, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun DangerRow(title: String, onClick: () -> Unit) {
    Surface(color = DS.RedDim, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable(onClick = onClick)) {
        Text(title, color = DS.Red, fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(14.dp))
    }
}

@Composable
private fun Chip(text: String, selected: Boolean = false, onClick: () -> Unit = {}) {
    Surface(color = if (selected) DS.Green else DS.Bg3, shape = RoundedCornerShape(20.dp), modifier = Modifier.clickable(onClick = onClick)) {
        Text(text, Modifier.padding(horizontal = 14.dp, vertical = 8.dp), fontSize = 12.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, color = if (selected) Color.Black else DS.Text)
    }
}

// ─────────────────────────────────────────────────────────────────
// DETAIL SCREEN
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
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 28.dp)) {
            item {
                Box(Modifier.fillMaxWidth().height(356.dp)) {
                    AsyncImage(drama.poster, drama.title, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    Box(
                        Modifier.fillMaxSize().background(
                            Brush.verticalGradient(
                                listOf(Color(0x26000000), Color.Transparent, Color(0xF00A0E14)),
                                startY = 20f
                            )
                        )
                    )
                    Box(
                        Modifier.fillMaxSize().background(
                            Brush.horizontalGradient(
                                listOf(Color(0x1A00E5A0), Color.Transparent, Color.Transparent)
                            )
                        )
                    )
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onClose, modifier = Modifier.clip(CircleShape).background(Color(0x660B1220))) {
                            Icon(Icons.Rounded.ArrowBack, "Kembali", tint = DS.White, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.weight(1f))
                        Surface(color = Color(0x1AFFFFFF), shape = RoundedCornerShape(50)) {
                            Text(platformLabel(drama.platform), color = DS.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp))
                        }
                    }
                    Column(Modifier.align(Alignment.BottomStart).padding(start = 20.dp, end = 20.dp, bottom = 86.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            DetailMetaPill("Featured", DS.Green, filled = true)
                            DetailMetaPill("${total.coerceAtLeast(1)} Episode", Color(0xFF7DD3FC))
                            if (preferLandscape) DetailMetaPill("Landscape", Color(0xFFF59E0B))
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(drama.title, color = DS.White, fontSize = 28.sp, fontWeight = FontWeight.Black, lineHeight = 32.sp, maxLines = 3, overflow = TextOverflow.Ellipsis)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            drama.description.take(140).ifBlank { "Kurasi premium ${platformLabel(drama.platform)} buat sesi nonton yang lebih gacor." },
                            color = Color(0xFFD9E2EC),
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(
                    Modifier.padding(horizontal = 20.dp).offset(y = (-56).dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    PosterImage(drama.poster, drama.title, Modifier.width(118.dp).height(170.dp))
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f).padding(bottom = 8.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            DetailMetaPill(platformLabel(drama.platform), Color(0xFFFDA4AF))
                            DetailMetaPill(if (hist != null) "Resume siap" else "Fresh start", if (hist != null) DS.Green else Color(0xFFA78BFA))
                        }
                        Spacer(Modifier.height(10.dp))
                        Text("Premium detail", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Text("${total.coerceAtLeast(1)} episode • ${if (preferLandscape) "player landscape" else "player portrait"}", color = DS.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, lineHeight = 18.sp)
                    }
                }
            }

            item {
                Column(Modifier.padding(horizontal = 20.dp).offset(y = (-32).dp)) {
                    when (state) {
                        Load.Loading -> LinearProgressIndicator(color = DS.Green, trackColor = DS.Bg4, modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp))
                        is Load.Err -> Text((state as Load.Err).message, color = DS.Red, modifier = Modifier.padding(bottom = 14.dp))
                        else -> {}
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { onPlay(detail, resumeEp) },
                            enabled = state is Load.Ok && resolvingEpisode == 0,
                            colors = ButtonDefaults.buttonColors(containerColor = DS.Green, contentColor = Color.Black),
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier.weight(1f).height(54.dp)
                        ) {
                            Icon(Icons.Rounded.PlayArrow, null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(if (hist != null) "Lanjut Ep $resumeEp" else "Mulai Tonton", fontWeight = FontWeight.Black, fontSize = 14.sp)
                        }
                        IconButton(
                            onClick = { store.toggleFav(drama); onFavChanged() },
                            modifier = Modifier.size(54.dp).clip(RoundedCornerShape(18.dp)).background(if (isFav) DS.Green else DS.Bg3)
                        ) {
                            Icon(if (isFav) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder, "Fav", tint = if (isFav) Color.Black else DS.Text, modifier = Modifier.size(22.dp))
                        }
                        IconButton(
                            onClick = { onShare(drama) },
                            modifier = Modifier.size(54.dp).clip(RoundedCornerShape(18.dp)).background(DS.Bg3)
                        ) {
                            Icon(Icons.Rounded.Share, "Share", tint = DS.Text, modifier = Modifier.size(22.dp))
                        }
                    }

                    Spacer(Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        DetailInfoCard("Platform", platformLabel(drama.platform), Icons.Rounded.Public, Modifier.weight(1f))
                        DetailInfoCard("Episode", total.toString(), Icons.Rounded.GridView, Modifier.weight(1f))
                        DetailInfoCard("Mode", if (preferLandscape) "Wide" else "Vertical", Icons.Rounded.AspectRatio, Modifier.weight(1f))
                    }

                    if (hist != null) {
                        Spacer(Modifier.height(14.dp))
                        Surface(
                            color = DS.Bg3,
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier.fillMaxWidth().clickable { onPlay(detail, resumeEp) }
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Lanjutkan nonton", color = DS.White, fontWeight = FontWeight.Black, fontSize = 14.sp, modifier = Modifier.weight(1f))
                                    Text("Ep $resumeEp", color = DS.Green, fontWeight = FontWeight.Black, fontSize = 13.sp)
                                }
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    if (hist.pct > 0) "Progress kamu udah ${hist.pct}% • tinggal lanjut gas" else "Progress belum banyak, tinggal lanjut dari awal episode ini.",
                                    color = DS.Muted,
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp
                                )
                                if (hist.pct > 0) {
                                    Spacer(Modifier.height(10.dp))
                                    LinearProgressIndicator(
                                        progress = (hist.pct / 100f).coerceIn(0f, 1f),
                                        color = DS.Green,
                                        trackColor = DS.Bg4,
                                        modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(50))
                                    )
                                }
                            }
                        }
                    }

                    if (drama.tags.isNotEmpty()) {
                        Spacer(Modifier.height(14.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(drama.tags.take(8)) { tag -> DetailMetaPill(tag, Color(0xFF60A5FA)) }
                        }
                    }

                    Spacer(Modifier.height(18.dp))
                    Surface(color = DS.Bg3, shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(18.dp)) {
                            Text("Sinopsis", color = DS.White, fontSize = 17.sp, fontWeight = FontWeight.Black)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                drama.description.ifBlank { "Belum ada sinopsis untuk judul ini." },
                                color = DS.Text,
                                fontSize = 13.sp,
                                lineHeight = 20.sp
                            )
                        }
                    }

                    Spacer(Modifier.height(18.dp))
                    Surface(color = DS.Bg3, shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text("Daftar Episode", color = DS.White, fontSize = 17.sp, fontWeight = FontWeight.Black)
                                    Spacer(Modifier.height(4.dp))
                                    Text("Pilih episode buat langsung gas ke player", color = DS.Muted, fontSize = 11.sp)
                                }
                                DetailMetaPill("$total total", DS.Green)
                            }
                            Spacer(Modifier.height(12.dp))
                            val rangeSize = 30
                            val rangeCount = ((total + rangeSize - 1) / rangeSize).coerceAtLeast(1)
                            if (rangeCount > 1) {
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), contentPadding = PaddingValues(bottom = 10.dp)) {
                                    items((0 until rangeCount).toList()) { r ->
                                        val st = r * rangeSize + 1
                                        val en = min(total, (r + 1) * rangeSize)
                                        Chip("$st-$en", detailRange == r) { detailRange = r }
                                    }
                                }
                            }
                            val startEp = detailRange * rangeSize + 1
                            val endEp = min(total, startEp + rangeSize - 1)
                            (startEp..endEp).chunked(4).forEach { row ->
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    row.forEach { ep ->
                                        val isResume = hist != null && resumeEp == ep
                                        Surface(
                                            color = when {
                                                resolvingEpisode == ep -> DS.Green
                                                isResume -> Color(0x1F00E5A0)
                                                else -> DS.Bg4
                                            },
                                            contentColor = if (resolvingEpisode == ep) Color.Black else DS.White,
                                            shape = RoundedCornerShape(14.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(52.dp)
                                                .clickable(enabled = resolvingEpisode == 0 && state is Load.Ok) { onPlay(detail, ep) }
                                        ) {
                                            Column(
                                                Modifier.fillMaxSize().padding(horizontal = 6.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Text(if (resolvingEpisode == ep) "..." else "Ep $ep", fontWeight = FontWeight.Black, fontSize = 13.sp)
                                                if (isResume && resolvingEpisode != ep) {
                                                    Spacer(Modifier.height(2.dp))
                                                    Text("Lanjut", color = DS.Green, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                    repeat(4 - row.size) { Spacer(Modifier.weight(1f)) }
                                }
                                Spacer(Modifier.height(10.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailMetaPill(text: String, accent: Color, filled: Boolean = false) {
    Surface(color = if (filled) accent else Color(0x14FFFFFF), shape = RoundedCornerShape(50)) {
        Row(Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            if (!filled) {
                Box(Modifier.size(8.dp).clip(CircleShape).background(accent))
                Spacer(Modifier.width(6.dp))
            }
            Text(text, color = if (filled) Color.Black else DS.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun DetailInfoCard(title: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Surface(color = DS.Bg3, shape = RoundedCornerShape(18.dp), modifier = modifier) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 14.dp), horizontalAlignment = Alignment.Start) {
            Icon(icon, null, tint = DS.Green, modifier = Modifier.size(16.dp))
            Spacer(Modifier.height(10.dp))
            Text(title, color = DS.Muted, fontSize = 10.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(2.dp))
            Text(value, color = DS.White, fontSize = 13.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

private fun episodeCount(d: Detail): Int = max(d.drama.episodes, d.episodes.size)

// ─────────────────────────────────────────────────────────────────
// PLAYER — VerticalEpisodePlayer & ClipFeedPlayer
// (keeping existing player logic, only error messages updated)
// ─────────────────────────────────────────────────────────────────

private object VideoCache {
    private const val MAX_MB = 256L
    @Volatile private var inst: SimpleCache? = null
    fun get(ctx: Context): SimpleCache = inst ?: synchronized(this) {
        inst ?: SimpleCache(ctx.cacheDir.resolve("exo_video_cache"), LeastRecentlyUsedCacheEvictor(MAX_MB * 1024 * 1024)).also { inst = it }
    }
}

private fun buildPlayer(ctx: Context): ExoPlayer {
    val http = DefaultHttpDataSource.Factory()
        .setUserAgent("Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 Chrome/121 Mobile Safari/537.36")
        .setAllowCrossProtocolRedirects(true).setConnectTimeoutMs(15_000).setReadTimeoutMs(30_000)
    val cache = CacheDataSource.Factory().setCache(VideoCache.get(ctx)).setUpstreamDataSourceFactory(http).setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
    return ExoPlayer.Builder(ctx)
        .setRenderersFactory(DefaultRenderersFactory(ctx).setEnableDecoderFallback(true))
        .setTrackSelector(DefaultTrackSelector(ctx).apply { setParameters(buildUponParameters().setPreferredVideoMimeTypes(MimeTypes.VIDEO_H264, MimeTypes.VIDEO_H265)) })
        .setMediaSourceFactory(DefaultMediaSourceFactory(cache)).build()
}

private fun playerError(e: PlaybackException): String {
    val r = e.message.orEmpty()
    return when {
        r.contains("video/hevc", true) || r.contains("hvc1", true) -> "Video HEVC tidak didukung. Coba episode lain."
        r.contains("MediaCodecVideoRenderer", true) -> "Decoder gagal. Coba Retry atau hemat data."
        r.contains("Source error", true) -> "Link expired. Tekan Coba Lagi."
        r.contains("timeout", true) -> "Koneksi timeout. Cek internet."
        else -> r.ifBlank { "Video belum tersedia" }
    }
}

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
                    AndroidView({ PlayerView(it).apply { useController = false; resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM; this.player = player } }, modifier = Modifier.fillMaxSize())
                } else { AsyncImage(drama.poster, drama.title, Modifier.fillMaxSize(), contentScale = ContentScale.Crop) }
                Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent, Color(0xAA000000)), startY = 400f)))
                Column(Modifier.align(Alignment.BottomStart).padding(14.dp, 14.dp, 76.dp, 20.dp)) {
                    Text(display.title, color = DS.White, fontSize = 15.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("${platformLabel(display.platform)} · ${display.episodes.coerceAtLeast(1)} Ep", color = DS.Text, fontSize = 11.sp)
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { curDetail?.let { stop(); onWatchFull(it) } }, enabled = curDetail != null, colors = ButtonDefaults.buttonColors(containerColor = DS.Green, contentColor = Color.Black), shape = RoundedCornerShape(10.dp), modifier = Modifier.height(34.dp)) {
                            Text("Tonton", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        OutlinedButton(onClick = { stop(); onOpenDetail(display) }, shape = RoundedCornerShape(10.dp), modifier = Modifier.height(34.dp)) {
                            Text("Detail", color = DS.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Top bar
        AnimatedVisibility(uiVis || loading || error != null, Modifier.align(Alignment.TopStart)) {
            Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { close() }, Modifier.clip(CircleShape).background(DS.Bg4.copy(alpha = 0.6f))) { Icon(Icons.Rounded.ArrowBack, "Kembali", tint = DS.White, modifier = Modifier.size(20.dp)) }
                Spacer(Modifier.width(8.dp))
                Text("${pager.currentPage + 1}/${items.size}", color = DS.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Loading
        if (loading) {
            Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = DS.Green, strokeWidth = 2.5.dp); Spacer(Modifier.height(10.dp))
                Text("Memuat...", color = DS.White, fontSize = 13.sp)
            }
        }

        // Error
        error?.let { e ->
            Surface(color = DS.Bg2.copy(alpha = 0.9f), shape = RoundedCornerShape(16.dp), modifier = Modifier.align(Alignment.Center).padding(24.dp)) {
                Column(Modifier.padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(e, color = DS.White, fontWeight = FontWeight.Bold, fontSize = 14.sp, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { retryKey++ }, colors = ButtonDefaults.buttonColors(containerColor = DS.Green, contentColor = Color.Black), shape = RoundedCornerShape(10.dp)) {
                        Text("Coba Lagi", fontWeight = FontWeight.Bold)
                    }
                }
            }
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
        player.setMediaItem(buildMediaItem(stream)); player.prepare()
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
                    AndroidView({ PlayerView(it).apply { useController = false; this.player = player; resizeMode = if (fitContain) AspectRatioFrameLayout.RESIZE_MODE_FIT else AspectRatioFrameLayout.RESIZE_MODE_ZOOM } }, modifier = Modifier.fillMaxSize())
                }
                if (uiVis || loading || error != null) {
                    Box(
                        Modifier.fillMaxSize().background(
                            Brush.verticalGradient(
                                listOf(Color(0x22000000), Color.Transparent, Color(0xC4000000)),
                                startY = 100f
                            )
                        )
                    )
                    Surface(
                        color = Color(0x5A0B1220),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.align(Alignment.BottomStart).padding(start = 16.dp, end = 92.dp, bottom = 28.dp)
                    ) {
                        Column(Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                DetailMetaPill(platformLabel(detail.drama.platform), DS.Green)
                                DetailMetaPill("Ep ${page + 1}/$total", Color(0xFF7DD3FC))
                                if (preferLandscape) DetailMetaPill("Wide", Color(0xFFF59E0B))
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(detail.drama.title, color = DS.White, fontSize = 16.sp, fontWeight = FontWeight.Black, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                if (playing) "Sedang diputar • swipe vertikal untuk pindah episode" else "Siap diputar • tap buat munculin kontrol",
                                color = DS.Text,
                                fontSize = 11.sp,
                                lineHeight = 15.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // Top bar
        AnimatedVisibility(uiVis || loading || error != null, Modifier.align(Alignment.TopStart)) {
            Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { closePlayer() }, Modifier.clip(CircleShape).background(DS.Bg4.copy(alpha = 0.6f))) { Icon(Icons.Rounded.ArrowBack, null, tint = DS.White, modifier = Modifier.size(20.dp)) }
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text(detail.drama.title, color = DS.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("Ep ${pager.currentPage + 1}/$total", color = DS.Muted, fontSize = 11.sp)
                }
                DetailMetaPill(if (preferLandscape) "Landscape" else "Portrait", if (preferLandscape) Color(0xFFF59E0B) else DS.Green)
            }
        }

        // Side buttons
        AnimatedVisibility(uiVis || loading || error != null, Modifier.align(Alignment.CenterEnd)) {
            Column(Modifier.padding(end = 12.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SideBtn(if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, if (playing) "Pause" else "Play") { uiVis = true; if (player.isPlaying) player.pause() else player.play() }
                SideBtn(Icons.Rounded.List, "Episode") { uiVis = true; epSheet = true }
                SideBtn(if (fitContain) Icons.Rounded.AspectRatio else Icons.Rounded.Fullscreen, if (fitContain) "Asli" else "Penuh") { uiVis = true; fitContain = !fitContain; if (!preferLandscape) store.setFitContain(fitContain) }
                SideBtn(Icons.Rounded.Refresh, "Retry") { uiVis = true; retryKey++ }
            }
        }

        // Seekbar
        AnimatedVisibility(uiVis || loading || error != null, Modifier.align(Alignment.BottomCenter)) {
            Surface(
                color = Color(0x720B1220),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
            ) {
                Column(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(formatMs(curMs), color = DS.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.weight(1f))
                        Text(if (durMs > 0) "Sisa ${formatMs((durMs - curMs).coerceAtLeast(0L))}" else "", color = DS.Muted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.width(8.dp))
                        Text(formatMs(durMs), color = DS.Muted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = if (durMs > 0) (curMs.toFloat() / durMs.toFloat()).coerceIn(0f, 1f) else 0f,
                        onValueChange = { isSeeking = true; curMs = (it * durMs).toLong().coerceAtLeast(0) },
                        onValueChangeFinished = { player.seekTo(curMs); saveProgress(pager.currentPage + 1); isSeeking = false },
                        enabled = durMs > 0,
                        colors = SliderDefaults.colors(thumbColor = DS.Green, activeTrackColor = DS.Green, inactiveTrackColor = DS.Bg4)
                    )
                }
            }
        }

        // Flash
        AnimatedVisibility(flash != null || speedHold || liked, Modifier.align(Alignment.Center)) {
            Surface(color = DS.Bg4.copy(alpha = 0.8f), shape = RoundedCornerShape(12.dp)) {
                Text(flash ?: if (speedHold) "2x" else "♥", color = if (liked) DS.Green else DS.White, fontWeight = FontWeight.Black, fontSize = if (liked) 32.sp else 18.sp, modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp))
            }
        }

        // Loading
        if (loading) { Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) { CircularProgressIndicator(color = DS.Green, strokeWidth = 2.5.dp); Spacer(Modifier.height(10.dp)); Text("Memuat Ep ${pager.currentPage + 1}...", color = DS.White, fontSize = 13.sp) } }

        // Error
        error?.let { e ->
            Surface(color = DS.Bg2.copy(alpha = 0.9f), shape = RoundedCornerShape(16.dp), modifier = Modifier.align(Alignment.Center).padding(24.dp)) {
                Column(Modifier.padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(e, color = DS.White, fontWeight = FontWeight.Bold, fontSize = 14.sp, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { retryKey++ }, colors = ButtonDefaults.buttonColors(containerColor = DS.Green, contentColor = Color.Black), shape = RoundedCornerShape(10.dp)) { Text("Coba Lagi", fontWeight = FontWeight.Bold) }
                }
            }
        }

        // Episode sheet
        if (epSheet) {
            Box(Modifier.fillMaxSize().background(DS.Bg.copy(alpha = 0.8f)).clickable { epSheet = false })
            Surface(color = DS.Bg2, shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp), modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().heightIn(max = 400.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text("Episode", color = DS.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(detail.drama.title, color = DS.Muted, fontSize = 12.sp, maxLines = 1)
                    Spacer(Modifier.height(12.dp))
                    LazyVerticalGrid(columns = GridCells.Fixed(5), verticalArrangement = Arrangement.spacedBy(6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items((1..total).toList()) { ep ->
                            val active = ep == pager.currentPage + 1
                            Surface(color = if (active) DS.Green else DS.Bg3, contentColor = if (active) Color.Black else DS.White, shape = RoundedCornerShape(10.dp), modifier = Modifier.height(40.dp).clickable { epSheet = false; uiVis = true; scope.launch { pager.animateScrollToPage(ep - 1) } }) {
                                Box(contentAlignment = Alignment.Center) { Text(ep.toString(), fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SideBtn(icon: ImageVector, label: String, onClick: () -> Unit) {
    Surface(color = DS.Bg4.copy(alpha = 0.78f), shape = RoundedCornerShape(16.dp), modifier = Modifier.width(64.dp).clickable(onClick = onClick)) {
        Column(Modifier.padding(vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, label, tint = DS.White, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(4.dp))
            Text(label, color = DS.Text, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// HELPERS
// ─────────────────────────────────────────────────────────────────

private fun prefersLandscapePlayback(drama: Drama): Boolean = drama.platform == "moviebox" || drama.platform == "drakor"

private fun buildMediaItem(s: StreamResult): MediaItem {
    val b = MediaItem.Builder().setUri(Uri.parse(s.url))
    if (s.url.lowercase().contains("m3u8")) b.setMimeType(MimeTypes.APPLICATION_M3U8)
    if (s.subtitle.isNotBlank()) {
        val mime = if (s.subtitle.lowercase().endsWith(".vtt")) MimeTypes.TEXT_VTT else MimeTypes.APPLICATION_SUBRIP
        b.setSubtitleConfigurations(listOf(MediaItem.SubtitleConfiguration.Builder(Uri.parse(s.subtitle)).setMimeType(mime).setLanguage("id").setSelectionFlags(C.SELECTION_FLAG_DEFAULT).build()))
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

private fun Context.isNetworkAvailable(): Boolean {
    val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return true
    return cm.getNetworkCapabilities(cm.activeNetwork)?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
}

// ─────────────────────────────────────────────────────────────────
// REPOSITORY — same API logic, with streamv2 fix
// ─────────────────────────────────────────────────────────────────

private fun extractStreamV2Url(json: JSONObject): String {
    val episodes = json.optJSONArray("episodes")?.objects().orEmpty()
    for (ep in episodes) {
        val cdnList = ep.optJSONArray("cdnList")?.objects().orEmpty()
        for (cdn in cdnList) {
            val paths = cdn.optJSONArray("videoPathList")?.objects().orEmpty()
            val hd = paths.firstOrNull { it.stringAny("sharpnessName").contains("HD", true) }
            val picked = hd ?: paths.firstOrNull()
            val vp = picked?.stringAny("videoPath").orEmpty()
            if (vp.isNotBlank()) return vp
        }
        val direct = ep.stringAny("playUrl", "url", "videoPath")
        if (direct.isNotBlank()) return direct
    }
    return json.stringAny("url")
}

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
        val k = streamKey(d.drama, ep, ds); val now = System.currentTimeMillis()
        streamCache[k]?.takeIf { it.expiresAtMs > now }?.let { return it.result }
        return resolveStream(d, ep, ds).also { r -> if (r.url.isNotBlank()) streamCache[k] = CachedStream(r, now + 300_000) }
    }

    private fun detailKey(d: Drama) = "${d.platform}|${d.id}"
    private fun streamKey(d: Drama, ep: Int, ds: Boolean) = "${d.platform}|${d.id}|$ep|${if (ds) "480" else "720"}"

    suspend fun loadHome(p: String) = loadHomePage(p, 1)

    suspend fun loadHomePage(p: String, page: Int): HomeBundle = coroutineScope {
        val req = homePageRequest(p, page)
        val json = try { getJson(req.url) } catch (e: CancellationException) { throw e } catch (_: Throwable) { null }
        val items = dedupe(json?.let { flat(it.dataOrSelf(), p) }.orEmpty())
        var rec = emptyList<Drama>(); var pop = emptyList<Drama>(); var nw = emptyList<Drama>()
        when (req.section) { HomeSection.Popular -> pop = items; HomeSection.Newest -> nw = items; HomeSection.Recommended -> rec = items }
        var more = req.hasMore
        if (p == "dramanova" && req.virtualPage == 1 && rec.isEmpty() && pop.isEmpty() && nw.isEmpty()) {
            val fb = loadFallback("dramanova"); rec = fb.recommended; pop = fb.popular; nw = fb.newest; more = false
        }
        if (rec.isEmpty() && pop.isEmpty() && nw.isEmpty() && req.virtualPage == 1) error("Data kosong")
        HomeBundle(rec, pop, nw, req.virtualPage, more)
    }

    private suspend fun loadFallback(broken: String): HomeBundle = coroutineScope {
        val jobs = listOf("melolo", "freereels", "goodshort", "dramabox", "drakor").map { p -> async { runCatching { loadHome(p) }.getOrNull() } }
        val b = jobs.awaitAll().filterNotNull()
        HomeBundle(dedupe(b.flatMap { it.recommended }).take(80), dedupe(b.flatMap { it.popular }).take(60), dedupe(b.flatMap { it.newest }).take(60), 1, false)
    }

    suspend fun searchPlatform(q: String, p: String): List<Drama> = coroutineScope {
        val enc = enc(q); val pl = Platforms.firstOrNull { it.id == p } ?: Platforms.first()
        val url = when (pl.id) {
            "melolo" -> "${pl.base}/search?q=$enc&page=1&lang=id"
            "freereels" -> "${pl.base}/search?q=$enc&page=1&lang=id"
            "flickreels" -> "${pl.base}/search?q=$enc"
            "dramanova" -> "${pl.base}/search?q=$enc&page=1&size=10"
            "reelshort" -> "${pl.base}/search?q=$enc&page=1&limit=10"
            "netshort" -> "${pl.base}/search?query=$enc&page=1"
            "dramabox" -> "${pl.base}/search?q=$enc&page=1&lang=in"
            "goodshort" -> "${pl.base}/search?q=$enc&page=1"
            "moviebox" -> "${pl.base}/search?q=$enc&page=1&perPage=10"
            "drakor" -> "${pl.base}/search?q=$enc&page=1&limit=30&type=1&order=1"
            else -> "${pl.base}/search?q=$enc"
        }
        dedupeAndRank(runCatching { flat(getJson(url).dataOrSelf(), pl.id) }.getOrDefault(emptyList()), q).take(80)
    }

    suspend fun loadDetail(input: Drama): Detail {
        val p = input.platform; val url = detailUrl(input); val json = getJson(url)
        if (p == "drakor") {
            val info = json.optJSONObject("info") ?: error("Detail tidak ditemukan")
            val epsArr = json.optJSONObject("episodes")?.optJSONArray("data") ?: JSONArray()
            val eps = epsArr.objects().mapIndexed { i, o -> EpisodeInfo(o.intAny("episode_number", i + 1), o.stringAny("streaming")) }
            val d = normalize(info, p).copy(id = info.stringAny("id").ifBlank { input.id }, title = info.stringAny("title").ifBlank { input.title }, poster = fixImg(info.stringAny("image").ifBlank { input.poster }), description = cleanText(info.stringAny("meta_sinopsis", "shoot", "content", "meta_description")).ifBlank { input.description }, episodes = eps.size.takeIf { it > 0 } ?: info.intAny("meta_episode", input.episodes), platform = p, subjectType = 2)
            return Detail(d, eps)
        }
        val data = json.optJSONObject("data") ?: error("Detail tidak ditemukan")
        if (p == "goodshort" && data.has("book")) {
            val book = data.optJSONObject("book") ?: data; val list = data.optJSONArray("list") ?: JSONArray()
            val d = normalize(book, p).copy(id = book.stringAny("bookId").ifBlank { input.id }, title = book.stringAny("bookName").ifBlank { input.title }, description = book.stringAny("introduction").ifBlank { input.description }, episodes = book.intAny("chapterCount", list.length()), poster = fixImg(book.stringAny("cover").ifBlank { input.poster }), platform = p)
            return Detail(d, (0 until list.length()).map { EpisodeInfo(it + 1) })
        }
        if (p == "moviebox") {
            val total = data.optJSONArray("resourceDetectors")?.optJSONObject(0)?.intAny("totalEpisode", 0) ?: data.optJSONObject("resourceDetectors")?.intAny("totalEpisode", 0) ?: input.episodes
            val d = normalize(data, p).copy(id = data.stringAny("subjectId").ifBlank { input.id }, title = data.stringAny("title").ifBlank { input.title }, description = data.stringAny("description").ifBlank { input.description }, episodes = if (data.intAny("subjectType", 1) == 2) max(total, 1) else 1, poster = fixImg(data.coverUrl().ifBlank { input.poster }), tags = data.stringAny("genre").split(",").map { it.trim() }.filter { it.isNotBlank() }, subjectType = data.intAny("subjectType", input.subjectType), platform = p)
            return Detail(d, (1..d.episodes.coerceAtLeast(1)).map { EpisodeInfo(it) })
        }
        val d = normalize(data, p).let { it.copy(id = it.id.ifBlank { input.id }, title = it.title.ifBlank { input.title }, poster = fixImg(it.poster.ifBlank { input.poster }), description = it.description.ifBlank { input.description }, episodes = max(it.episodes, input.episodes), platform = p) }
        val epsArr = data.optJSONArray("video_list") ?: data.optJSONArray("episode_list") ?: data.optJSONArray("episodes") ?: data.optJSONArray("chapterList")
        val eps = epsArr?.objects()?.mapIndexed { i, o -> EpisodeInfo(o.intAny("episode", "episode_no", "chapterIndex", i + 1), o.stringAny("streaming")) }.orEmpty()
        val total = max(d.episodes, eps.size)
        return Detail(d.copy(episodes = total), if (eps.isNotEmpty()) eps else (1..total.coerceAtLeast(1)).map { EpisodeInfo(it) })
    }

    suspend fun resolveStream(d: Detail, ep: Int, ds: Boolean): StreamResult {
        val drama = d.drama; val p = drama.platform; val base = apiBase(p); val id = drama.id; val res = if (ds) 480 else 720
        return when (p) {
            "melolo" -> {
                val v2 = getJson("$base/streamv2?id=${enc(id)}&ep=$ep")
                val url = extractStreamV2Url(v2)
                if (url.isNotBlank()) StreamResult(url) else error("Stream Melolo tidak tersedia")
            }
            "freereels" -> {
                val j = getJson("$base/stream?dramaId=${enc(id)}&episode=$ep&lang=id").optJSONObject("data") ?: error("Video belum tersedia")
                StreamResult(j.stringAny("h264_m3u8", "m3u8_url", "video_url"), subtitleFrom(j.optJSONArray("subtitles")))
            }
            "flickreels" -> {
                val url = runCatching { getJson("$base/stream?id=${enc(id)}&ep=$ep").optJSONObject("data")?.stringAny("hls_url").orEmpty() }.getOrDefault("")
                if (url.isNotBlank()) return StreamResult(url)
                val dj = getJson("$base/detail?id=${enc(id)}")
                val eps = dj.optJSONObject("data")?.optJSONArray("episodes") ?: dj.optJSONObject("data")?.optJSONArray("episode_list") ?: JSONArray()
                val e = eps.objects().firstOrNull { it.intAny("episode", "episode_no", 0) == ep } ?: eps.optJSONObject(ep - 1)
                StreamResult(e?.stringAny("hls_url", "url", "video_url").orEmpty())
            }
            "reelshort" -> {
                val data = getJson("$base/stream?id=${enc(id)}&episode_no=$ep").optJSONObject("data") ?: error("Video belum tersedia")
                val vl = data.optJSONArray("videoList")?.objects().orEmpty()
                val pick = vl.firstOrNull { it.stringAny("encode") == "H264" && it.intAny("dpi", 0) == res } ?: vl.firstOrNull { it.stringAny("encode") == "H264" } ?: vl.firstOrNull()
                StreamResult(pick?.stringAny("playUrl").orEmpty().ifBlank { data.stringAny("play_url") })
            }
            "drakor" -> {
                val streaming = d.episodes.firstOrNull { it.number == ep }?.streaming ?: d.episodes.getOrNull(ep - 1)?.streaming.orEmpty()
                if (streaming.isBlank()) error("Episode belum punya stream")
                val j = getJson("$base/stream?streaming=${enc(streaming)}")
                StreamResult(if (ds) j.stringAny("480p", "360p", "720p") else j.stringAny("720p", "480p", "360p"))
            }
            "moviebox" -> {
                val resolutions = listOf(res, 720, 1080, 480, 360).distinct()
                if (drama.subjectType == 2) {
                    // Series: request tanpa resolution filter, filter episode & codec di client
                    var url = ""; var sub = ""
                    for (r in resolutions) {
                        val j = runCatching { getJson("$base/download-series?subjectId=${enc(id)}&se=1&resolution=$r").optJSONObject("data") }.getOrNull() ?: continue
                        val eps = j.optJSONArray("episodes")?.objects().orEmpty()
                        // Cari episode yang benar
                        val target = eps.firstOrNull { it.intAny("ep", 1) == ep } ?: eps.firstOrNull()
                        if (target == null) continue
                        val link = target.stringAny("resourceLink").orEmpty()
                        if (link.isBlank()) continue
                        // Prioritas: H264 > HEVC
                        val codec = target.stringAny("codecName").lowercase()
                        url = link
                        sub = target.optJSONObject("subtitle")?.stringAny("url").orEmpty()
                        if (codec.contains("h264")) break // H264 ditemukan, stop
                        // Kalau HEVC, coba resolution lain untuk cari H264
                    }
                    // Fallback: kalau semua HEVC, pakai yang pertama
                    if (url.isBlank()) {
                        val j = runCatching { getJson("$base/download-series?subjectId=${enc(id)}&se=1&resolution=720").optJSONObject("data") }.getOrNull()
                        val eps = j?.optJSONArray("episodes")?.objects().orEmpty()
                        val target = eps.firstOrNull { it.intAny("ep", 1) == ep } ?: eps.firstOrNull()
                        url = target?.stringAny("resourceLink").orEmpty()
                        sub = target?.optJSONObject("subtitle")?.stringAny("url").orEmpty()
                    }
                    StreamResult(url, sub)
                } else {
                    // Movie: cari H264 dulu
                    var url = ""; var sub = ""
                    for (r in resolutions) {
                        val j = runCatching { getJson("$base/download-movie?subjectId=${enc(id)}&resolution=$r").optJSONObject("data") }.getOrNull() ?: continue
                        val files = j.optJSONArray("files")?.objects().orEmpty()
                        // Prioritas: H264 > non-HEVC > apapun
                        val f = files.firstOrNull { it.stringAny("codecName").contains("h264", true) }
                            ?: files.firstOrNull { !it.stringAny("codecName").contains("hevc", true) }
                            ?: files.firstOrNull()
                        url = f?.stringAny("resourceLink").orEmpty()
                        sub = j.optJSONObject("subtitle")?.stringAny("url").orEmpty()
                        if (url.isNotBlank()) break
                    }
                    StreamResult(url, sub)
                }
            }
            "goodshort" -> {
                val dj = runCatching { getJson("$base/detail?bookId=${enc(id)}") }.getOrNull()
                val ld = dj?.optJSONObject("data")?.optJSONArray("list")
                val epData = ld?.optJSONObject(ep - 1)
                val videos = epData?.optJSONArray("multiVideos")?.objects().orEmpty()
                val pick = videos.firstOrNull { it.stringAny("type") == "${res}p" } ?: videos.firstOrNull { it.stringAny("type") == "720p" } ?: videos.firstOrNull()
                val from = pick?.stringAny("filePath").orEmpty()
                if (from.isNotBlank()) return StreamResult(from)
                val cdn = epData?.optJSONArray("cdnList")?.objects().orEmpty().firstOrNull { it.stringAny("videoPath").isNotBlank() }?.stringAny("videoPath").orEmpty()
                if (cdn.isNotBlank()) return StreamResult(cdn)
                val sd = getJson("$base/stream?bookId=${enc(id)}").optJSONObject("data")
                val dl = sd?.optJSONArray("downloadList") ?: error("Video belum tersedia")
                val le = dl.optJSONObject(ep - 1) ?: error("Episode belum tersedia")
                val lv = le.optJSONArray("multiVideos")?.objects().orEmpty()
                val lp = lv.firstOrNull { it.stringAny("type") == "${res}p" } ?: lv.firstOrNull { it.stringAny("type") == "720p" } ?: lv.firstOrNull()
                StreamResult(lp?.stringAny("filePath").orEmpty())
            }
            "dramabox" -> {
                val data = getJson("$base/stream?bookId=${enc(id)}&chapterIndex=${ep - 1}&lang=in").optJSONObject("data") ?: error("Video belum tersedia")
                val q = data.optJSONArray("qualities")?.objects()?.firstOrNull { it.intAny("quality", 0) == res } ?: data.optJSONArray("qualities")?.optJSONObject(0)
                StreamResult(data.stringAny("videoUrl").ifBlank { q?.stringAny("videoPath").orEmpty() })
            }
            "netshort" -> {
                val v2 = getJson("$base/streamv2?id=${enc(id)}&ep=$ep")
                val nested = extractStreamV2Url(v2)
                if (nested.isNotBlank()) return StreamResult(nested)
                val data = v2.optJSONObject("data") ?: error("Video belum tersedia")
                val s = data.optJSONArray("streams")?.objects()?.firstOrNull { it.stringAny("encode") == "H264" } ?: data.optJSONArray("streams")?.optJSONObject(0)
                StreamResult(data.stringAny("play_url").ifBlank { s?.stringAny("url").orEmpty() })
            }
            "dramanova" -> {
                val data = getJson("$base/stream?id=${enc(id)}&ep=$ep").optJSONObject("data") ?: error("Video belum tersedia")
                val play = data.optJSONObject("play") ?: data
                val q = play.optJSONArray("qualities")?.objects()?.firstOrNull { it.stringAny("codec") == "h264" } ?: play.optJSONArray("qualities")?.optJSONObject(0)
                StreamResult(play.stringAny("video_url", "backup_url").ifBlank { q?.stringAny("main_url", "backup_url").orEmpty() }, subtitleFrom(data.optJSONObject("info")?.optJSONArray("subtitle_tracks")))
            }
            else -> {
                val v2 = runCatching { getJson("$base/streamv2?id=${enc(id)}&ep=$ep") }.getOrNull()
                if (v2 != null) { val n = extractStreamV2Url(v2); if (n.isNotBlank()) return StreamResult(n); val l = v2.stringAny("url"); if (l.isNotBlank() && v2.optBoolean("playable", true)) return StreamResult(l) }
                val j = getJson("$base/stream?id=${enc(id)}&ep=$ep")
                val q = j.optJSONArray("qualities")?.objects()?.firstOrNull { it.stringAny("codec") == "h264" } ?: j.optJSONArray("qualities")?.optJSONObject(0)
                StreamResult(q?.stringAny("url").orEmpty())
            }
        }.also { if (it.url.isBlank()) error("Video belum tersedia") }
    }

    private suspend fun getJson(url: String): JSONObject = withContext(Dispatchers.IO) {
        // Endpoint MovieBox/Drakor kadang balas 5xx sementara (Cloudflare origin).
        // Retry singkat supaya kategori Movie Drama & Movie Box tidak langsung error.
        var last: Throwable? = null
        repeat(3) { attempt ->
            if (attempt > 0) delay(450L * attempt)
            try {
                return@withContext client.newCall(
                    Request.Builder().url(url)
                        .header("User-Agent", "DramakuNative/5.0 Android")
                        .header("Accept", "application/json, text/plain, */*")
                        .build()
                ).execute().use { r ->
                    val body = r.body?.string().orEmpty()
                    if (!r.isSuccessful) error("HTTP ${r.code}")
                    val json = JSONObject(body)
                    val code = json.optInt("code", 200)
                    if (code >= 400) error(json.stringAny("message", "error").ifBlank { "HTTP $code" })
                    json
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

private fun pagesFor(p: String): IntRange = when (p) { "flickreels", "netshort" -> 1..1; "drakor" -> 1..5; else -> 1..5 }

private fun homeUrls(p: String, page: Int = 1): List<String> {
    val base = apiBase(p); val sp = page.coerceAtLeast(1)
    val nl = p in setOf("flickreels", "dramanova", "reelshort", "netshort")
    val lang = if (p == "dramabox") "&lang=in" else if (!nl) "&lang=id" else ""
    var h = "$base/home?page=$sp$lang"; var pop = "$base/populer?page=$sp$lang"; var nw = "$base/new?page=$sp$lang"
    when (p) {
        "dramanova" -> { h = "$base/recommend?page=$sp&size=20"; pop = "$base/discovery?size=20&page=$sp"; nw = "$base/recommend?page=$sp&size=20" }
        "flickreels" -> { pop = "$base/populer"; nw = "$base/new?page=$sp" }
        "reelshort" -> { h = "$base/home?tab_id=0&sub_tab_id=0&page=$sp&limit=20"; pop = "$base/populer?page=$sp&limit=20&period=0&rule=0"; nw = "$base/new?page=$sp&limit=20" }
        "netshort" -> { h = "$base/home?page=1"; pop = "$base/populer"; nw = "$base/new" }
        "dramabox" -> { h = "$base/home?page=$sp&lang=in"; pop = "$base/populer?page=$sp&lang=in"; nw = "$base/new?page=$sp&lang=in" }
        "goodshort" -> { h = "$base/home?page=$sp"; pop = "$base/populer?page=$sp"; nw = "$base/new?page=$sp&channelId=563" }
        "moviebox" -> { h = "$base/indonesia?page=$sp&perPage=20"; pop = "$base/global?page=$sp&perPage=20"; nw = "$base/horror?page=$sp&perPage=20" }
        "drakor" -> { h = "$base/home/korea?page=$sp&limit=30&sort=LATEST"; pop = "$base/trending?page=$sp&limit=30&days=30"; nw = "$base/terbaru?page=$sp&limit=30" }
    }
    return listOf(h, pop, nw)
}

private fun detailUrl(d: Drama): String = when (d.platform) {
    "dramabox" -> "${apiBase(d.platform)}/detail?bookId=${enc(d.id)}&lang=in"
    "goodshort" -> "${apiBase(d.platform)}/detail?bookId=${enc(d.id)}"
    "moviebox" -> "${apiBase(d.platform)}/detail?subjectId=${enc(d.id)}"
    "drakor" -> "${apiBase(d.platform)}/detail?id=${enc(d.id)}"
    "flickreels", "dramanova", "reelshort", "netshort" -> "${apiBase(d.platform)}/detail?id=${enc(d.id)}"
    else -> "${apiBase(d.platform)}/detail?id=${enc(d.id)}&lang=id"
}

private fun dedupe(items: List<Drama>) = items.filter { it.id.isNotBlank() && it.title.isNotBlank() }.distinctBy { it.platform + "|" + it.id }.distinctBy { it.platform + "|" + normalizeKey(it.title) }
private fun mergeHomeBundles(c: HomeBundle, n: HomeBundle) = HomeBundle(dedupe(c.recommended + n.recommended), dedupe(c.popular + n.popular), dedupe(c.newest + n.newest), max(c.loadedPage, n.loadedPage), n.hasMore)

private fun flat(any: Any?, fp: String): List<Drama> {
    val out = mutableListOf<Drama>()
    when (any) {
        is JSONArray -> any.objects().forEach { o -> val b = o.optJSONArray("books"); if (b != null) out += flat(b, fp) else out += normalize(o, fp) }
        is JSONObject -> when {
            any.has("trending") || any.has("popular") || any.has("newest") -> listOf("trending", "popular", "newest").forEach { k -> out += flat(any.optJSONArray(k), "dramabox") }
            any.optJSONObject("classifyBookList")?.optJSONArray("records") != null -> out += flat(any.optJSONObject("classifyBookList")?.optJSONArray("records"), "dramabox")
            any.optJSONArray("items") != null -> out += flat(any.optJSONArray("items"), fp)
            any.optJSONArray("subjects") != null -> out += flat(any.optJSONArray("subjects"), "moviebox")
            any.optJSONArray("results") != null -> any.optJSONArray("results")!!.objects().forEach { r -> out += flat(r.optJSONArray("subjects"), "moviebox") }
            else -> out += normalize(any, fp)
        }
    }
    return out.filter { it.id.isNotBlank() && it.title.isNotBlank() }.distinctBy { it.platform + "|" + it.id }
}

private fun normalize(o: JSONObject, fp: String): Drama {
    val isDrakor = o.has("meta_episode") || (o.has("id") && o.has("title") && o.has("image"))
    val p = when { fp == "dramabox" || o.has("bookId") -> "dramabox"; fp == "moviebox" || o.has("subjectId") -> "moviebox"; fp == "drakor" || isDrakor -> "drakor"; o.optBoolean("free", false) -> "freereels"; else -> fp }
    return Drama(o.stringAny("drama_id", "bookId", "id", "subjectId"), o.stringAny("drama_name", "bookName", "title", "bookTitle"), cleanText(o.stringAny("introduction", "description", "meta_description", "meta_sinopsis", "shoot", "content", "synopsis")), fixImg(o.stringAny("thumb_url", "coverWap", "cover", "bookCover", "image", "poster", "posterImg").ifBlank { o.coverUrl() }), o.intAny("chapterCount", "episode_count", "meta_episode", "episode_number", "total_episodes", "chapterCnt", 0), o.stringAny("watch_value", "hotCode", "viewCountDisplay", "hits", "viewers").ifBlank { o.optJSONObject("rankVo")?.stringAny("hotCode").orEmpty() }, tagsOf(o), p, o.intAny("subjectType", 1))
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
private fun subtitleFrom(arr: JSONArray?): String { val list = arr?.objects().orEmpty(); return (list.firstOrNull { it.stringAny("language", "lang").startsWith("id", true) } ?: list.firstOrNull())?.stringAny("url", "label").orEmpty() }
private fun fixImg(u: String): String { if (u.contains("fizzopic.org") && u.contains(".heic")) { val m = Regex("novel-images-apsoutheast/([a-f0-9]+)~").find(u); if (m != null) return "https://p19-novel-sg.ibyteimg.com/img/novel-images-sg/${m.groupValues[1]}~tplv-resize:570:810.jpg" }; return u }
private fun cleanText(s: String) = s.replace(Regex("<[^>]+>"), " ").replace("&nbsp;", " ").replace(Regex("\\s+"), " ").trim()
private fun enc(s: String) = URLEncoder.encode(s, "UTF-8")
private fun normalizeKey(s: String) = s.lowercase().replace(Regex("[^a-z0-9\\p{L}\\s]"), " ").replace(Regex("\\s+"), " ").trim()
