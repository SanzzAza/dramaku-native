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
    // Graphite cinema — gelap, bersih, tidak ramai
    val Bg = Color(0xFF050506)
    val Bg2 = Color(0xFF0E1014)
    val Bg3 = Color(0xFF171A20)
    val Bg4 = Color(0xFF242833)
    val Line = Color(0x1FFFFFFF)

    // Brand
    val Green = Color(0xFFFF304F)
    val GreenDark = Color(0xFFE11D3F)
    val GreenDim = Color(0xFFFF304F).copy(alpha = 0.14f)
    val Warm = Color(0xFFF4C56A)
    val Sky = Color(0xFF9CB5D2)

    // Text
    val White = Color(0xFFF7F7F8)
    val Text = Color(0xFFD5D8DF)
    val Muted = Color(0xFF8E95A3)
    val Hint = Color(0xFF596171)

    // Semantic
    val Red = Color(0xFFEF4444)
    val RedDim = Color(0xFFEF4444).copy(alpha = 0.12f)
    val Amber = Color(0xFFF59E0B)

    // Gradients
    val GreenGrad = listOf(Color(0xFFFF304F), Color(0xFFFF6A3D))
    val CardGrad = listOf(Color(0xFF171A20), Color(0xFF0E1014))
    val OverlayBottom = listOf(Color.Transparent, Color(0xEE050506))
    val OverlayFull = listOf(Color.Transparent, Color.Transparent, Color(0xCC050506))
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
private data class EpisodeInfo(val number: Int, val streaming: String = "", val label: String = "", val locked: Boolean = false)
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
    PlatformInfo("melolo", "Melolo", "https://new-api.sonzaix.workers.dev/melolo", logoRes = R.drawable.logo_melolo),
    PlatformInfo("freereels", "FreeReels", "https://new-api.sonzaix.workers.dev/freereels", logoRes = R.drawable.logo_freereels),
    PlatformInfo("flickreels", "FlickReels", "https://new-api.sonzaix.workers.dev/flickreels", logoRes = R.drawable.logo_flickreels),
    PlatformInfo("dramanova", "DramaNova", "https://new-api.sonzaix.workers.dev/dramanova", logoRes = R.drawable.logo_dramanova),
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
                                onResume = { h -> pendingResume = h; selectedDrama = Drama(h.id, h.title, poster = h.poster, platform = h.platform) }
                            )
                            Tab.Search -> SearchScreen(repo, store, selPlatform, onDrama = { selectedDrama = it }, onBack = { tab = Tab.Home }, dataTick = dataTick, bump = { dataTick++ })
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
// BOTTOM NAV — floating, compact, tidak terlalu ramai
// ─────────────────────────────────────────────────────────────────

@Composable
private fun BottomNavBar(selected: Tab, onSelect: (Tab) -> Unit) {
    Surface(color = DS.Bg2.copy(alpha = 0.98f), tonalElevation = 0.dp) {
        Column {
            Box(Modifier.fillMaxWidth().height(1.dp).background(DS.Line))
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 9.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Tab.values().filter { it.showNav }.forEach { tab ->
                    val active = tab == selected
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onSelect(tab) }
                            .padding(vertical = 3.dp)
                    ) {
                        Box(
                            Modifier
                                .size(width = 42.dp, height = 30.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (active) DS.GreenDim else Color.Transparent),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(tab.icon, tab.label, tint = if (active) DS.Green else DS.Hint, modifier = Modifier.size(21.dp))
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            tab.label,
                            color = if (active) DS.White else DS.Hint,
                            fontSize = 10.sp,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                            maxLines = 1
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
    platformId: String, scrollToTopSignal: Int, state: Load<HomeBundle>, history: List<HistoryItem>,
    remoteConfig: NativeRemoteConfig?, remoteError: String?,
    loadingMore: Boolean, loadMoreError: String?,
    onLoadMore: () -> Unit, onPlatform: (String) -> Unit, onRefresh: () -> Unit,
    onDrama: (Drama) -> Unit, onSearch: () -> Unit, onRandom: () -> Unit,
    onClips: () -> Unit, onResume: (HistoryItem) -> Unit,
    category: HomeCategory? = null, onExitCategory: () -> Unit = {}
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
        contentPadding = PaddingValues(bottom = 20.dp)
    ) {
        item {
            HomeHeader(
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
                val newKeys = data.newest.map { it.platform + "|" + it.id }.toSet()
                val all = (data.newest + data.popular + data.recommended)
                    .filter { it.id.isNotBlank() && it.title.isNotBlank() }
                    .distinctBy { it.platform + "|" + it.id }
                    .distinctBy { it.platform + "|" + normalizeKey(it.title) }

                if (all.isEmpty()) {
                    item { EmptyState("Belum ada judul", "Coba refresh atau pindah sumber dulu", Icons.Rounded.Movie) }
                } else {
                    item { HeroCard(all.first(), onDrama) }
                    if (history.isNotEmpty()) item {
                        Section("Lanjutkan menonton", "Lanjut dari tempat terakhir") {
                            LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(history.take(8), key = { it.platform + it.id }) { watched -> ContinueCard(watched, onResume) }
                            }
                        }
                    }

                    item {
                        Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 26.dp, bottom = 14.dp), verticalAlignment = Alignment.Bottom) {
                            Column(Modifier.weight(1f)) {
                                Text("Pilihan untukmu", color = DS.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp)
                                Text("${platformLabel(platformId)} · ${all.size} judul", color = DS.Muted, fontSize = 11.sp)
                            }
                            Text("Acak", color = DS.Green, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable(onClick = onRandom))
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
                            Spacer(Modifier.height(16.dp))
                        }
                    }
                }

                item {
                    Column(Modifier.fillMaxWidth().padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        if (loadingMore) {
                            CircularProgressIndicator(color = DS.Green, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("Memuat lagi...", color = DS.Hint, fontSize = 11.sp)
                        } else if (!data.hasMore) {
                            Text("Semua daftar sudah ditampilkan", color = DS.Hint, fontSize = 11.sp)
                        }
                        loadMoreError?.let { Text(it, color = DS.Red, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp)) }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// CATEGORY HOME — clean entry screen
// ─────────────────────────────────────────────────────────────────

@Composable
private fun CategoryHomeScreen(onSelect: (HomeCategory) -> Unit, onSettings: () -> Unit) {
    val ctx = LocalContext.current
    val greeting = remember { Greetings.forHour(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) }
    Box(Modifier.fillMaxSize().background(DS.Bg)) {
        Box(
            Modifier.align(Alignment.TopEnd).size(220.dp).offset(x = 80.dp, y = (-80).dp)
                .clip(CircleShape).background(DS.Green.copy(alpha = 0.10f))
        )
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(40.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                BrandLogoMini(Modifier.size(44.dp))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Dramaku", color = DS.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
                    Text("${greeting.text}, mau nonton apa?", color = DS.Muted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                HeaderCircleButton(Icons.Rounded.Settings, "Pengaturan", onSettings)
            }

            Spacer(Modifier.height(30.dp))
            Text("Pilih mode", color = DS.White, fontSize = 34.sp, fontWeight = FontWeight.Black, letterSpacing = (-0.8).sp)
            Spacer(Modifier.height(6.dp))
            Text("Langsung masuk ke koleksi yang kamu mau.", color = DS.Muted, fontSize = 13.sp, lineHeight = 19.sp)
            Spacer(Modifier.height(22.dp))

            CategoryWideCard(
                category = HomeCategory.ShortDrama,
                icon = Icons.Rounded.PlayArrow,
                title = "Short Drama",
                subtitle = "Episode pendek, enak buat nonton cepat.",
                accent = DS.Green,
                onClick = { onSelect(HomeCategory.ShortDrama) }
            )
            Spacer(Modifier.height(12.dp))
            CategoryWideCard(
                category = HomeCategory.MovieDrama,
                icon = Icons.Rounded.Movie,
                title = HomeCategory.MovieDrama.title,
                subtitle = HomeCategory.MovieDrama.subtitle,
                accent = Color(0xFF60A5FA),
                onClick = { onSelect(HomeCategory.MovieDrama) }
            )
            Spacer(Modifier.height(12.dp))
            CategoryWideCard(
                category = HomeCategory.MovieBox,
                icon = Icons.Rounded.Tv,
                title = HomeCategory.MovieBox.title,
                subtitle = HomeCategory.MovieBox.subtitle,
                accent = DS.Warm,
                onClick = { onSelect(HomeCategory.MovieBox) }
            )

            Spacer(Modifier.height(20.dp))
            Text("Segera hadir", color = DS.Muted, fontSize = 12.sp, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp)
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CategorySmallCard(HomeCategory.Anime, Icons.Rounded.AutoAwesome, Modifier.weight(1f)) { onSelect(HomeCategory.Anime) }
                CategorySmallCard(HomeCategory.Manga, Icons.Rounded.MenuBook, Modifier.weight(1f)) { onSelect(HomeCategory.Manga) }
            }

            Spacer(Modifier.height(22.dp))
            Surface(
                color = DS.Bg2,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, DS.Line, RoundedCornerShape(18.dp)).clickable {
                    Toast.makeText(ctx, "Link dukungan segera ditambahkan", Toast.LENGTH_SHORT).show()
                }
            ) {
                Row(Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.FavoriteBorder, null, tint = DS.Green, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("Dukung pengembangan Dramaku", color = DS.Text, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Icon(Icons.Rounded.ChevronRight, null, tint = DS.Hint, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun BrandLogoMini(modifier: Modifier = Modifier) {
    Box(modifier.clip(RoundedCornerShape(13.dp)).background(DS.Green), contentAlignment = Alignment.Center) {
        Icon(Icons.Rounded.PlayArrow, "Dramaku", tint = Color.White, modifier = Modifier.size(23.dp))
    }
}

@Composable
private fun CategoryWideCard(
    category: HomeCategory,
    icon: ImageVector,
    title: String,
    subtitle: String,
    accent: Color,
    onClick: () -> Unit
) {
    Surface(
        color = DS.Bg2,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth().border(1.dp, DS.Line, RoundedCornerShape(20.dp)).clickable(onClick = onClick)
    ) {
        Box {
            Row(Modifier.padding(horizontal = 17.dp, vertical = 17.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(accent.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                    Icon(icon, title, tint = accent, modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(title, color = DS.White, fontSize = 17.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        if (category.comingSoon) { Spacer(Modifier.width(8.dp)); ComingSoonBadge() }
                    }
                    Spacer(Modifier.height(5.dp))
                    Text(subtitle, color = DS.Muted, fontSize = 12.sp, lineHeight = 17.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
                Icon(Icons.Rounded.ArrowOutward, null, tint = accent.copy(alpha = 0.8f), modifier = Modifier.size(18.dp))
            }
            Box(Modifier.align(Alignment.CenterStart).width(3.dp).height(42.dp).clip(RoundedCornerShape(8.dp)).background(accent))
        }
    }
}

@Composable
private fun CategorySmallCard(category: HomeCategory, icon: ImageVector, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        color = DS.Bg2,
        shape = RoundedCornerShape(18.dp),
        modifier = modifier.border(1.dp, DS.Line, RoundedCornerShape(18.dp)).clickable(onClick = onClick)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, category.title, tint = DS.Muted, modifier = Modifier.size(20.dp))
                Spacer(Modifier.weight(1f))
                ComingSoonBadge()
            }
            Spacer(Modifier.height(14.dp))
            Text(category.title, color = DS.White, fontSize = 15.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(4.dp))
            Text(category.subtitle, color = DS.Muted, fontSize = 11.sp, lineHeight = 15.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun ComingSoonBadge() {
    Text(
        "Segera",
        color = DS.Hint,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.clip(RoundedCornerShape(50)).background(Color(0x12FFFFFF)).padding(horizontal = 8.dp, vertical = 4.dp)
    )
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
private fun HomeHeader(
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
    val selectedState = remoteConfig?.platform(platformId)
    val online = selectedState?.enabled ?: true
    val hour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val greeting = when (hour) { in 5..10 -> "Selamat pagi"; in 11..14 -> "Selamat siang"; in 15..17 -> "Selamat sore"; else -> "Selamat malam" }
    val alert = remoteConfig?.message?.takeIf { it.enabled }?.let { listOf(it.title, it.text).filter { it.isNotBlank() }.joinToString(" · ") }
        ?: remoteError?.let { "Status server: $it" }
        ?: if (!online) "${platformLabel(platformId)} sedang gangguan" else ""

    Column(Modifier.fillMaxWidth().padding(top = 14.dp, bottom = 4.dp)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(greeting, color = DS.Muted, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(2.dp))
                Text("Dramaku", color = DS.White, fontSize = 25.sp, fontWeight = FontWeight.Black, letterSpacing = (-0.8).sp)
            }
            HeaderCircleButton(Icons.Rounded.Search, "Cari", onSearch)
            Spacer(Modifier.width(9.dp))
            HeaderCircleButton(Icons.Rounded.Refresh, "Muat ulang", onRefresh)
        }
        Spacer(Modifier.height(18.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(chips, key = { it.id }) { source ->
                val selected = source.id == platformId
                val enabled = remoteConfig?.platform(source.id)?.enabled ?: true
                Surface(
                    color = if (selected) DS.Green else DS.Bg3,
                    contentColor = if (selected) Color.White else DS.Text,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.border(if (selected) 0.dp else 1.dp, DS.Line, RoundedCornerShape(10.dp))
                        .clickable(enabled = enabled) { onPlatform(source.id) }
                ) {
                    Text(source.label, fontSize = 12.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 13.dp, vertical = 9.dp))
                }
            }
        }
        if (alert.isNotBlank()) {
            Spacer(Modifier.height(12.dp))
            Row(Modifier.padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(6.dp).clip(CircleShape).background(if (online) DS.Amber else DS.Red))
                Spacer(Modifier.width(7.dp))
                Text(alert, color = DS.Muted, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun HeaderCircleButton(icon: ImageVector, label: String, onClick: () -> Unit) {
    Box(
        Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(DS.Bg2)
            .border(1.dp, DS.Line, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, label, tint = DS.Text, modifier = Modifier.size(19.dp))
    }
}

@Composable
private fun HeaderStatChip(text: String, color: Color) {
    Surface(color = Color(0x10FFFFFF), shape = RoundedCornerShape(50)) {
        Row(Modifier.padding(horizontal = 10.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(color))
            Spacer(Modifier.width(6.dp))
            Text(text, color = DS.Text, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun HomeTinyAction(label: String, icon: ImageVector, modifier: Modifier, onClick: () -> Unit) {
    Surface(color = DS.Bg2, shape = RoundedCornerShape(50), modifier = modifier.border(1.dp, DS.Line, RoundedCornerShape(50)).clickable(onClick = onClick)) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Icon(icon, null, tint = DS.Green, modifier = Modifier.size(17.dp))
            Spacer(Modifier.width(7.dp))
            Text(label, color = DS.Text, fontSize = 12.sp, fontWeight = FontWeight.Black, maxLines = 1)
        }
    }
}

@Composable
private fun CompactQuickAction(label: String, icon: ImageVector, modifier: Modifier, onClick: () -> Unit) {
    HomeTinyAction(label, icon, modifier, onClick)
}

@Composable
private fun PlatformLogo(platformId: String, modifier: Modifier = Modifier) {
    val info = platform(platformId)
    when {
        info.logoRes != 0 -> {
            AsyncImage(info.logoRes, info.label, modifier.clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
        }
        info.logoUrl.isNotBlank() -> {
            AsyncImage(info.logoUrl, info.label, modifier.clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Fit)
        }
        else -> {
            Box(modifier.clip(RoundedCornerShape(8.dp)).background(DS.Bg4), contentAlignment = Alignment.Center) {
                Text(info.label.take(1), color = DS.White, fontWeight = FontWeight.Black, fontSize = 10.sp)
            }
        }
    }
}

@Composable
private fun PlatformBadge(platformId: String, compact: Boolean = false) {
    Surface(
        color = Color(0x12FFFFFF),
        shape = RoundedCornerShape(50),
        modifier = Modifier.border(1.dp, DS.Line, RoundedCornerShape(50))
    ) {
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
private fun SearchDramaCard(drama: Drama, onClick: (Drama) -> Unit, rank: Int? = null) {
    Column(
        Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(DS.Bg2)
            .border(1.dp, DS.Line, RoundedCornerShape(18.dp))
            .clickable { onClick(drama) }
            .padding(7.dp)
    ) {
        Box {
            PosterImage(drama.poster, drama.title, Modifier.fillMaxWidth().aspectRatio(0.7f))
            rank?.let {
                Surface(color = DS.Green, shape = RoundedCornerShape(topStart = 14.dp, bottomEnd = 12.dp), modifier = Modifier.align(Alignment.TopStart)) {
                    Text(it.toString(), color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp))
                }
            }
            Box(Modifier.align(Alignment.TopEnd).padding(6.dp).clip(RoundedCornerShape(9.dp)).background(Color(0xAA0B1020)).padding(4.dp)) {
                PlatformLogo(drama.platform, Modifier.size(16.dp))
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(drama.title.ifBlank { "Tanpa Judul" }, color = DS.White, fontSize = 12.sp, fontWeight = FontWeight.Black, maxLines = 2, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(3.dp))
        Text(
            drama.description.ifBlank { platformLabel(drama.platform) },
            color = DS.Muted,
            fontSize = 10.sp,
            lineHeight = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SearchSectionTitle(title: String, subtitle: String = "") {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
        Column(Modifier.weight(1f)) {
            Text(title, color = DS.White, fontSize = 17.sp, fontWeight = FontWeight.Black)
            if (subtitle.isNotBlank()) {
                Spacer(Modifier.height(3.dp))
                Text(subtitle, color = DS.Muted, fontSize = 11.sp, lineHeight = 15.sp)
            }
        }
        Box(Modifier.width(28.dp).height(4.dp).clip(RoundedCornerShape(50)).background(DS.Green))
    }
}

@Composable
private fun Section(title: String, subtitle: String = "", content: @Composable () -> Unit) {
    Column(Modifier.padding(top = 24.dp)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), verticalAlignment = Alignment.Bottom) {
            Column(Modifier.weight(1f)) {
                Text(title, color = DS.White, fontSize = 20.sp, fontWeight = FontWeight.Black, letterSpacing = (-0.3).sp)
                if (subtitle.isNotBlank()) {
                    Spacer(Modifier.height(3.dp))
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
                Modifier.size(34.dp).clip(RoundedCornerShape(12.dp)).background(Brush.linearGradient(DS.GreenGrad)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = Color.Black, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(label, color = DS.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text("Akses cepat", color = DS.Muted, fontSize = 10.sp)
            }
            Icon(Icons.Rounded.ChevronRight, null, tint = DS.Hint, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun HeroCard(drama: Drama, onClick: (Drama) -> Unit) {
    Box(
        Modifier.fillMaxWidth().height(360.dp).clickable { onClick(drama) }
    ) {
        AsyncImage(drama.poster, drama.title, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0x10000000), Color(0x30000000), DS.Bg), startY = 0f, endY = 850f)))
        Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(DS.Bg.copy(alpha = 0.42f), Color.Transparent))))
        Column(Modifier.align(Alignment.BottomStart).padding(horizontal = 20.dp, vertical = 20.dp)) {
            Text(platformLabel(drama.platform).uppercase(), color = DS.Warm, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Spacer(Modifier.height(6.dp))
            Text(drama.title, color = DS.White, fontSize = 28.sp, lineHeight = 31.sp, fontWeight = FontWeight.Black, letterSpacing = (-1).sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(7.dp))
            Text(listOfNotNull(drama.views.takeIf { it.isNotBlank() }, drama.episodes.takeIf { it > 0 }?.let { "$it episode" }).joinToString("  ·  ").ifBlank { "Pilihan untuk malam ini" }, color = DS.Text, fontSize = 12.sp)
            Spacer(Modifier.height(15.dp))
            Surface(color = DS.White, contentColor = DS.Bg, shape = RoundedCornerShape(10.dp)) {
                Row(Modifier.padding(horizontal = 15.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.PlayArrow, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(5.dp))
                    Text("Mulai nonton", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ContinueCard(h: HistoryItem, onClick: (HistoryItem) -> Unit) {
    Column(Modifier.width(132.dp).clickable { onClick(h) }) {
        Box {
            PosterImage(h.poster, h.title, Modifier.fillMaxWidth().height(188.dp))
            Surface(color = Color(0xCC050506), shape = RoundedCornerShape(8.dp), modifier = Modifier.align(Alignment.TopStart).padding(7.dp)) {
                Text("Ep ${h.episode}", color = DS.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp))
            }
            LinearProgressIndicator(
                progress = (h.pct / 100f).coerceIn(0f, 1f),
                color = DS.Green,
                trackColor = Color(0x66000000),
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(4.dp)
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(h.title, color = DS.White, fontSize = 12.sp, fontWeight = FontWeight.Black, maxLines = 2, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(3.dp))
        Text(if (h.pct > 0) "${h.pct}% ditonton" else platformLabel(h.platform), color = DS.Muted, fontSize = 10.sp, maxLines = 1)
    }
}

@Composable
private fun DramaCard(drama: Drama, onClick: (Drama) -> Unit, modifier: Modifier = Modifier.width(120.dp)) {
    DiscoverDramaCard(drama = drama, isNew = false, onClick = onClick, modifier = modifier)
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
                .aspectRatio(0.71f)
                .clip(RoundedCornerShape(16.dp))
                .background(DS.Bg3)
        ) {
            AsyncImage(drama.poster, drama.title, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color(0x8F000000)))))
            if (isNew) {
                Text(
                    "BARU", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black,
                    modifier = Modifier.align(Alignment.TopStart).padding(8.dp)
                        .clip(RoundedCornerShape(7.dp)).background(DS.Green).padding(horizontal = 7.dp, vertical = 4.dp)
                )
            }
            if (drama.views.isNotBlank()) {
                Text(
                    drama.views, color = DS.White, fontSize = 10.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp)
                        .clip(RoundedCornerShape(7.dp)).background(Color(0x99000000)).padding(horizontal = 7.dp, vertical = 4.dp)
                )
            }
        }
        Spacer(Modifier.height(9.dp))
        Text(
            drama.title.ifBlank { "Tanpa Judul" }, color = DS.White, fontSize = 13.sp,
            fontWeight = FontWeight.Bold, lineHeight = 18.sp, maxLines = 2, overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(4.dp))
        Text(
            buildString {
                append(platformLabel(drama.platform))
                if (drama.episodes > 0) append("  ·  ${drama.episodes} ep")
            },
            color = DS.Muted, fontSize = 10.sp, fontWeight = FontWeight.Medium,
            maxLines = 1, overflow = TextOverflow.Ellipsis
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
    val brush = Brush.linearGradient(listOf(DS.Bg2, DS.Bg4.copy(alpha = 0.75f), DS.Bg2), start = Offset(offset - 300f, offset - 300f), end = Offset(offset, offset))

    Column(Modifier.padding(20.dp)) {
        Box(Modifier.fillMaxWidth().height(230.dp).clip(RoundedCornerShape(28.dp)).background(brush))
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(4) { Box(Modifier.width(78.dp).height(34.dp).clip(RoundedCornerShape(50)).background(brush)) }
        }
        Spacer(Modifier.height(18.dp))
        repeat(3) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.weight(1f).height(180.dp).clip(RoundedCornerShape(22.dp)).background(brush))
                Box(Modifier.weight(1f).height(180.dp).clip(RoundedCornerShape(22.dp)).background(brush))
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// ERROR CARD
// ─────────────────────────────────────────────────────────────────

@Composable
private fun ErrorCard(message: String, onRetry: () -> Unit) {
    Surface(
        color = DS.Bg2,
        shape = RoundedCornerShape(26.dp),
        modifier = Modifier.fillMaxWidth().padding(22.dp).border(1.dp, DS.Line, RoundedCornerShape(26.dp))
    ) {
        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(color = DS.RedDim, shape = RoundedCornerShape(20.dp), modifier = Modifier.size(60.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(Icons.Rounded.ErrorOutline, null, tint = DS.Red, modifier = Modifier.size(30.dp)) }
            }
            Spacer(Modifier.height(14.dp))
            Text("Belum bisa dimuat", color = DS.White, fontSize = 17.sp, fontWeight = FontWeight.Black)
            Text(message, color = DS.Muted, fontSize = 12.sp, lineHeight = 17.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 5.dp, bottom = 17.dp))
            Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = DS.Green, contentColor = Color.Black), shape = RoundedCornerShape(16.dp)) {
                Icon(Icons.Rounded.Refresh, null, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(6.dp))
                Text("Coba lagi", fontWeight = FontWeight.Black, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun OfflineBanner(onRefresh: () -> Unit) {
    Surface(color = Color(0xFF35151B), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.WifiOff, null, tint = DS.Green, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text("Internet lagi putus", color = DS.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text("Refresh", color = DS.Green, fontSize = 12.sp, fontWeight = FontWeight.Black, modifier = Modifier.clickable(onClick = onRefresh))
        }
    }
}

@Composable
private fun PlaceholderScreen(title: String, subtitle: String, icon: ImageVector) {
    Box(Modifier.fillMaxSize().background(DS.Bg), contentAlignment = Alignment.Center) {
        Surface(color = DS.Bg2, shape = RoundedCornerShape(28.dp), modifier = Modifier.padding(24.dp).border(1.dp, DS.Line, RoundedCornerShape(28.dp))) {
            Column(Modifier.padding(horizontal = 24.dp, vertical = 28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(color = DS.GreenDim, shape = RoundedCornerShape(22.dp), modifier = Modifier.size(66.dp)) {
                    Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = DS.Green, modifier = Modifier.size(31.dp)) }
                }
                Spacer(Modifier.height(15.dp))
                Text(title, color = DS.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
                Text(subtitle, color = DS.Muted, fontSize = 13.sp, lineHeight = 18.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 5.dp))
            }
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

    Box(Modifier.fillMaxSize().background(DS.Bg)) {
        Box(Modifier.align(Alignment.TopEnd).size(190.dp).offset(x = 58.dp, y = (-70).dp).clip(CircleShape).background(DS.Green.copy(alpha = 0.12f)))
        Box(Modifier.align(Alignment.TopStart).size(160.dp).offset(x = (-80).dp, y = 150.dp).clip(CircleShape).background(DS.Sky.copy(alpha = 0.08f)))

        Column(Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                HeaderCircleButton(Icons.Rounded.ArrowBack, "Kembali", onBack)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Cari drama", color = DS.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
                    Text("Cari judul di ${platformLabel(searchPlatformId)}", color = DS.Muted, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                PlatformBadge(searchPlatformId, compact = true)
            }

            Spacer(Modifier.height(16.dp))
            Surface(
                color = DS.Bg2,
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, DS.Line, RoundedCornerShape(22.dp))
            ) {
                Row(Modifier.padding(horizontal = 14.dp, vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Search, null, tint = DS.Green, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(10.dp))
                    Box(Modifier.weight(1f)) {
                        if (q.isBlank()) {
                            Text("Judul, genre, atau kata kunci...", color = DS.Hint, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        BasicTextField(
                            value = q,
                            onValueChange = { q = it },
                            singleLine = true,
                            textStyle = TextStyle(color = DS.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    if (q.isNotBlank()) {
                        IconButton(onClick = { q = "" }, modifier = Modifier.size(30.dp)) {
                            Icon(Icons.Rounded.Close, "Hapus", tint = DS.Hint, modifier = Modifier.size(18.dp))
                        }
                    }
                    Surface(color = DS.Green, shape = RoundedCornerShape(14.dp), modifier = Modifier.clickable { searchTick++ }) {
                        Text("Cari", color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp))
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(Platforms, key = { it.id }) { p ->
                    val selected = p.id == searchPlatformId
                    Surface(
                        color = if (selected) DS.GreenDim else Color(0x10FFFFFF),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .border(1.dp, if (selected) DS.Green.copy(alpha = 0.45f) else Color.Transparent, RoundedCornerShape(18.dp))
                            .clickable { searchPlatformId = p.id }
                    ) {
                        Row(Modifier.padding(horizontal = 11.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            PlatformLogo(p.id, Modifier.size(16.dp))
                            Spacer(Modifier.width(7.dp))
                            Text(p.label, color = if (selected) DS.White else DS.Text, fontSize = 11.sp, fontWeight = if (selected) FontWeight.Black else FontWeight.SemiBold)
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Box(Modifier.weight(1f).fillMaxWidth()) {
                when {
                    q.trim().length >= 2 -> when (val searchState = state) {
                        Load.Idle, Load.Loading -> {
                            Surface(color = DS.Bg2, shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth().border(1.dp, DS.Line, RoundedCornerShape(22.dp))) {
                                Column(Modifier.padding(18.dp)) {
                                    LinearProgressIndicator(color = DS.Green, trackColor = DS.Bg4, modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(50)))
                                    Spacer(Modifier.height(12.dp))
                                    Text("Mencari di ${platformLabel(searchPlatformId)}...", color = DS.Muted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                        is Load.Err -> ErrorCard(searchState.message) { searchTick++ }
                        is Load.Ok -> {
                            val all = searchState.data
                            if (all.isEmpty()) {
                                EmptyState("Tidak ada hasil", "Coba kata kunci lain atau ganti platform", Icons.Rounded.Search)
                            } else {
                                Column(Modifier.fillMaxSize()) {
                                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                        Text("${all.size} judul ditemukan", color = DS.Text, fontSize = 13.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
                                        Text("${platformLabel(searchPlatformId)}", color = DS.Muted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                    Spacer(Modifier.height(10.dp))
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
                                SearchSectionTitle("Terakhir dicari", "Tap kata kunci buat cari ulang")
                                Spacer(Modifier.height(10.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    recent.take(6).forEach { keyword ->
                                        Surface(color = DS.Bg2, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().border(1.dp, DS.Line, RoundedCornerShape(16.dp))) {
                                            Row(Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Rounded.History, null, tint = DS.Green, modifier = Modifier.size(18.dp))
                                                Spacer(Modifier.width(10.dp))
                                                Text(keyword, color = DS.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f).clickable { q = keyword })
                                                IconButton(onClick = { store.removeRecentSearch(keyword); bump() }, modifier = Modifier.size(28.dp)) {
                                                    Icon(Icons.Rounded.Close, "Hapus", tint = DS.Hint, modifier = Modifier.size(17.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                                Spacer(Modifier.height(20.dp))
                            }

                            SearchSectionTitle("Ramai ditonton", "Judul yang sering dibuka di platform ini")
                            Spacer(Modifier.height(12.dp))
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
                                Surface(color = DS.Bg2, shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth().border(1.dp, DS.Line, RoundedCornerShape(20.dp))) {
                                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                        CircularProgressIndicator(color = DS.Green, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
                                        Spacer(Modifier.width(12.dp))
                                        Text("Menyiapkan rekomendasi...", color = DS.Muted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }

                            Spacer(Modifier.height(22.dp))
                            SearchSectionTitle("Coba juga", "Campuran update baru dan rekomendasi")
                            Spacer(Modifier.height(12.dp))
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
                            Spacer(Modifier.height(18.dp))
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
    var showFav by remember { mutableStateOf(false) }
    var localTick by remember { mutableIntStateOf(0) }
    val history = remember(dataTick, localTick) { store.history(dataTick + localTick) }
    val favs = remember(dataTick, localTick) { store.favs() }

    Box(Modifier.fillMaxSize().background(DS.Bg)) {
        Box(Modifier.align(Alignment.TopEnd).size(180.dp).offset(x = 70.dp, y = (-60).dp).clip(CircleShape).background(DS.Green.copy(alpha = 0.10f)))
        Column(Modifier.fillMaxSize().padding(20.dp)) {
            Text("Koleksi", color = DS.White, fontSize = 28.sp, fontWeight = FontWeight.Black, letterSpacing = (-0.5).sp)
            Text("Riwayat dan judul yang kamu simpan", color = DS.Muted, fontSize = 12.sp)
            Spacer(Modifier.height(18.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Stat("${history.size}", "Riwayat", Modifier.weight(1f))
                Stat("${favs.size}", "Favorit", Modifier.weight(1f))
            }
            Spacer(Modifier.height(16.dp))
            Surface(color = DS.Bg2, shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth().border(1.dp, DS.Line, RoundedCornerShape(24.dp))) {
                Row(Modifier.padding(6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Chip("Lanjutkan", !showFav, Modifier.weight(1f)) { showFav = false }
                    Chip("Favorit", showFav, Modifier.weight(1f)) { showFav = true }
                }
            }
            Spacer(Modifier.height(16.dp))

            val listModifier = Modifier.fillMaxSize()
            if (showFav) {
                if (favs.isEmpty()) {
                    EmptyState("Belum ada favorit", "Simpan drama dari halaman detail biar gampang dibuka lagi", Icons.Rounded.FavoriteBorder)
                } else {
                    LazyColumn(listModifier, verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 22.dp)) {
                        items(favs, key = { it.platform + it.id }) { d ->
                            ListItem(d.title, platformLabel(d.platform), d.poster, onDelete = {
                                store.removeFav(d.id, d.platform); localTick++; Toast.makeText(ctx, "Favorit dihapus", Toast.LENGTH_SHORT).show()
                            }) { onDrama(d) }
                        }
                    }
                }
            } else {
                if (history.isEmpty()) {
                    EmptyState("Belum ada riwayat", "Mulai nonton, nanti progress kamu muncul di sini", Icons.Rounded.History)
                } else {
                    LazyColumn(listModifier, verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 22.dp)) {
                        items(history, key = { it.id + it.platform }) { h ->
                            ListItem(
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
}

@Composable
private fun EmptyState(title: String, subtitle: String, icon: ImageVector) {
    Surface(
        color = DS.Bg2,
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier.fillMaxWidth().padding(top = 24.dp).border(1.dp, DS.Line, RoundedCornerShape(28.dp))
    ) {
        Column(Modifier.padding(horizontal = 24.dp, vertical = 34.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(color = Color(0x10FFFFFF), shape = RoundedCornerShape(22.dp), modifier = Modifier.size(72.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = DS.Green, modifier = Modifier.size(32.dp)) }
            }
            Spacer(Modifier.height(16.dp))
            Text(title, color = DS.White, fontSize = 17.sp, fontWeight = FontWeight.Black)
            Text(subtitle, color = DS.Muted, fontSize = 12.sp, lineHeight = 18.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 5.dp, start = 10.dp, end = 10.dp))
        }
    }
}

@Composable
private fun ListItem(title: String, subtitle: String, poster: String, onDelete: (() -> Unit)? = null, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).clickable(onClick = onClick).padding(vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PosterImage(poster, title, Modifier.width(58.dp).height(80.dp))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = DS.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(5.dp))
            Text(subtitle, color = DS.Muted, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        if (onDelete != null) {
            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Rounded.MoreVert, "Pilihan", tint = DS.Hint, modifier = Modifier.size(20.dp))
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

    Box(Modifier.fillMaxSize().background(DS.Bg)) {
        Box(Modifier.align(Alignment.TopEnd).size(220.dp).offset(x = 80.dp, y = (-80).dp).clip(CircleShape).background(DS.Green.copy(alpha = 0.12f)))
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
            Surface(
                color = Color.Transparent,
                shape = RoundedCornerShape(30.dp),
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(30.dp)).background(Brush.linearGradient(listOf(Color(0xFF151D2E), Color(0xFF0B101C)))).border(1.dp, DS.Line, RoundedCornerShape(30.dp))
            ) {
                Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    BrandLogoMini(Modifier.size(56.dp))
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Profil", color = DS.White, fontSize = 25.sp, fontWeight = FontWeight.Black)
                        Text("Atur pengalaman nontonmu", color = DS.Muted, fontSize = 12.sp, lineHeight = 17.sp)
                    }
                    Surface(color = DS.GreenDim, shape = RoundedCornerShape(50)) {
                        Text("Lokal", color = DS.Green, fontSize = 11.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp))
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Stat("$hCount", "Riwayat", Modifier.weight(1f))
                Stat("$fCount", "Favorit", Modifier.weight(1f))
                Stat("$rCount", "Cari", Modifier.weight(1f))
            }

            Spacer(Modifier.height(22.dp))
            ProfileGroupTitle("Pemutaran")
            SettingItem("Hemat data", "Pakai kualitas lebih ringan saat streaming", dataSaver) { dataSaver = it; store.setDataSaver(it); bump() }
            SettingItem("Auto next", "Lanjut episode berikutnya otomatis", autoNext) { autoNext = it; store.setAutoNext(it); bump() }
            SettingItem("Rasio asli", "Tampilkan video tanpa crop", fitContain) { fitContain = it; store.setFitContain(it); bump() }

            Spacer(Modifier.height(18.dp))
            ProfileGroupTitle("Info aplikasi")
            SettingRow("Tentang Dramaku") { dialog = "about" }
            SettingRow("Privasi") { dialog = "privacy" }
            SettingRow("Disclaimer") { dialog = "disclaimer" }

            Spacer(Modifier.height(18.dp))
            ProfileGroupTitle("Bersihkan data", danger = true)
            DangerRow("Hapus riwayat") { store.clearHistory(); bump(); Toast.makeText(ctx, "Riwayat dihapus", Toast.LENGTH_SHORT).show() }
            DangerRow("Hapus favorit") { store.clearFavs(); bump(); Toast.makeText(ctx, "Favorit dihapus", Toast.LENGTH_SHORT).show() }
            DangerRow("Hapus pencarian") { store.clearRecentSearches(); bump(); Toast.makeText(ctx, "Pencarian dihapus", Toast.LENGTH_SHORT).show() }
            Spacer(Modifier.height(28.dp))
        }
    }

    dialog?.let { type ->
        val (title, body) = when (type) {
            "privacy" -> "Privasi" to "Riwayat, favorit, pencarian, dan progress nonton disimpan lokal di perangkat kamu. Dramaku tidak meng-host video dan tidak membuat akun pengguna."
            "disclaimer" -> "Disclaimer" to "Konten tetap milik platform masing-masing. Dramaku hanya menampilkan data dari sumber pihak ketiga sebagai client/aggregator. Gunakan dengan bijak."
            else -> {
                val version = try { ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionName } catch (_: Exception) { "5.0" }
                "Tentang Dramaku" to "Versi: $version\n\nDramaku Native dibuat untuk nonton drama pendek, serial Asia, dan film dalam satu aplikasi.\n\n• Player native berbasis ExoPlayer\n• Swipe episode vertikal\n• Favorit, riwayat, dan progress lokal\n• Mode hemat data\n\nDibangun dengan Kotlin + Jetpack Compose."
            }
        }
        AlertDialog(
            onDismissRequest = { dialog = null },
            confirmButton = { TextButton(onClick = { dialog = null }) { Text("Tutup", color = DS.Green, fontWeight = FontWeight.Black) } },
            title = { Text(title, color = DS.White, fontWeight = FontWeight.Black) },
            text = { Text(body, color = DS.Text, fontSize = 13.sp, lineHeight = 19.sp) },
            containerColor = DS.Bg2,
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
private fun ProfileGroupTitle(title: String, danger: Boolean = false) {
    Text(
        title,
        color = if (danger) DS.Red else DS.Text,
        fontSize = 12.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = 0.6.sp,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun Stat(value: String, label: String, modifier: Modifier) {
    Surface(color = DS.Bg2, shape = RoundedCornerShape(20.dp), modifier = modifier.border(1.dp, DS.Line, RoundedCornerShape(20.dp))) {
        Column(Modifier.padding(vertical = 15.dp, horizontal = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, color = DS.Green, fontWeight = FontWeight.Black, fontSize = 21.sp, maxLines = 1)
            Spacer(Modifier.height(2.dp))
            Text(label, color = DS.Muted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun SettingItem(title: String, subtitle: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Surface(color = DS.Bg2, shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp).border(1.dp, DS.Line, RoundedCornerShape(20.dp))) {
        Row(Modifier.padding(horizontal = 15.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, color = DS.White, fontSize = 14.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(2.dp))
                Text(subtitle, color = DS.Muted, fontSize = 11.sp, lineHeight = 15.sp)
            }
            Switch(
                checked,
                onChecked,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.Black,
                    checkedTrackColor = DS.Green,
                    uncheckedThumbColor = DS.Muted,
                    uncheckedTrackColor = DS.Bg4,
                    uncheckedBorderColor = DS.Bg4
                )
            )
        }
    }
}

@Composable
private fun SettingRow(title: String, onClick: () -> Unit) {
    Surface(color = DS.Bg2, shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp).border(1.dp, DS.Line, RoundedCornerShape(20.dp)).clickable(onClick = onClick)) {
        Row(Modifier.padding(horizontal = 15.dp, vertical = 15.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(title, color = DS.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Icon(Icons.Rounded.ChevronRight, null, tint = DS.Hint, modifier = Modifier.size(19.dp))
        }
    }
}

@Composable
private fun DangerRow(title: String, onClick: () -> Unit) {
    Surface(color = DS.RedDim, shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp).border(1.dp, DS.Red.copy(alpha = 0.18f), RoundedCornerShape(20.dp)).clickable(onClick = onClick)) {
        Row(Modifier.padding(horizontal = 15.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(title, color = DS.Red, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Icon(Icons.Rounded.ChevronRight, null, tint = DS.Red, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun Chip(text: String, selected: Boolean = false, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Surface(color = if (selected) DS.Green else Color.Transparent, shape = RoundedCornerShape(18.dp), modifier = modifier.clickable(onClick = onClick)) {
        Box(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), contentAlignment = Alignment.Center) {
            Text(text, fontSize = 12.sp, fontWeight = FontWeight.Black, color = if (selected) Color.Black else DS.Text)
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// DETAIL SCREEN
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
                Box(Modifier.fillMaxWidth().height(420.dp)) {
                    AsyncImage(drama.poster, drama.title, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    Box(
                        Modifier.fillMaxSize().background(
                            Brush.verticalGradient(
                                listOf(Color(0x18000000), Color.Transparent, DS.Bg),
                                startY = 0f
                            )
                        )
                    )
                    Box(
                        Modifier.fillMaxSize().background(
                            Brush.horizontalGradient(
                                listOf(DS.Green.copy(alpha = 0.12f), Color.Transparent, Color.Transparent)
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
                    Column(Modifier.align(Alignment.BottomStart).padding(start = 20.dp, end = 20.dp, bottom = 28.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            DetailMetaPill("Pilihan", DS.Green, filled = true)
                            DetailMetaPill("${total.coerceAtLeast(1)} Episode", Color(0xFF7DD3FC))
                            if (preferLandscape) DetailMetaPill("Landscape", Color(0xFFF59E0B))
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(drama.title, color = DS.White, fontSize = 28.sp, fontWeight = FontWeight.Black, lineHeight = 32.sp, maxLines = 3, overflow = TextOverflow.Ellipsis)
                    }
                }

            }

            item {
                Column(Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
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

                    Spacer(Modifier.height(13.dp))
                    Text(
                        "${platformLabel(drama.platform)}  ·  $total episode${if (preferLandscape) "  ·  layar lebar" else ""}",
                        color = DS.Muted, fontSize = 12.sp, fontWeight = FontWeight.Medium
                    )

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
                                    if (hist.pct > 0) "Progress kamu ${hist.pct}% • siap dilanjutkan" else "Progress belum banyak, lanjut dari awal episode ini.",
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

                    Spacer(Modifier.height(26.dp))
                    Text("Sinopsis", color = DS.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        drama.description.ifBlank { "Belum ada sinopsis untuk judul ini." },
                        color = DS.Text, fontSize = 13.sp, lineHeight = 21.sp
                    )

                    Spacer(Modifier.height(18.dp))
                    Surface(color = DS.Bg3, shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text("Daftar Episode", color = DS.White, fontSize = 17.sp, fontWeight = FontWeight.Black)
                                    Spacer(Modifier.height(4.dp))
                                    Text("Pilih episode yang mau diputar", color = DS.Muted, fontSize = 11.sp)
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
                                        val epInfo = detail.episodes.firstOrNull { it.number == ep } ?: detail.episodes.getOrNull(ep - 1)
                                        val locked = epInfo?.locked == true
                                        val isResume = hist != null && resumeEp == ep
                                        Surface(
                                            color = when {
                                                resolvingEpisode == ep -> DS.Green
                                                locked -> DS.Bg3.copy(alpha = 0.58f)
                                                isResume -> DS.GreenDim
                                                else -> DS.Bg4
                                            },
                                            contentColor = if (resolvingEpisode == ep) Color.Black else DS.White,
                                            shape = RoundedCornerShape(14.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(52.dp)
                                                .clickable(enabled = !locked && resolvingEpisode == 0 && state is Load.Ok) { onPlay(detail, ep) }
                                        ) {
                                            Column(
                                                Modifier.fillMaxSize().padding(horizontal = 6.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Text(if (resolvingEpisode == ep) "..." else "Ep $ep", fontWeight = FontWeight.Black, fontSize = 13.sp, color = if (locked) DS.Hint else Color.Unspecified)
                                                when {
                                                    locked -> {
                                                        Spacer(Modifier.height(2.dp))
                                                        Text("Premium", color = DS.Hint, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                    isResume && resolvingEpisode != ep -> {
                                                        Spacer(Modifier.height(2.dp))
                                                        Text("Lanjut", color = DS.Green, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                    }
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

private fun buildPlayer(ctx: Context, requestHeaders: Map<String, String> = emptyMap()): ExoPlayer {
    val http = DefaultHttpDataSource.Factory()
        .setUserAgent("Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 Chrome/121 Mobile Safari/537.36")
        .setAllowCrossProtocolRedirects(true).setConnectTimeoutMs(15_000).setReadTimeoutMs(30_000)
    if (requestHeaders.isNotEmpty()) http.setDefaultRequestProperties(requestHeaders)
    val cache = CacheDataSource.Factory().setCache(VideoCache.get(ctx)).setUpstreamDataSourceFactory(http).setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
    return ExoPlayer.Builder(ctx)
        .setRenderersFactory(DefaultRenderersFactory(ctx).setEnableDecoderFallback(true))
        .setTrackSelector(DefaultTrackSelector(ctx).apply { setParameters(buildUponParameters().setPreferredVideoMimeTypes(MimeTypes.VIDEO_H264, MimeTypes.VIDEO_H265)) })
        .setMediaSourceFactory(DefaultMediaSourceFactory(cache)).build()
}

private fun streamHeaders(platformId: String): Map<String, String> = when (platformId) {
    "drakor" -> mapOf(
        "Accept" to "*/*",
        "Referer" to "https://drakor.id/",
        "Origin" to "https://drakor.id",
        "Cookie" to "DRIVE_STREAM=drakor.id"
    )
    "moviebox" -> mapOf(
        "Accept" to "video/mp4,video/*;q=0.9,*/*;q=0.8",
        "Referer" to "https://www.moviebox.com/",
        "Origin" to "https://www.moviebox.com"
    )
    else -> emptyMap()
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ClipFeedPlayer(items: List<Drama>, repo: DramakuRepository, store: LocalStore, onClose: () -> Unit, onWatchFull: (Detail) -> Unit, onOpenDetail: (Drama) -> Unit) {
    if (items.isEmpty()) return
    val ctx = LocalContext.current
    val act = ctx as? Activity
    val compAct = ctx as? ComponentActivity
    val pager = rememberPagerState(pageCount = { items.size })
    val clipHeaders = remember(items) {
        when {
            items.any { it.platform == "drakor" } -> streamHeaders("drakor")
            items.any { it.platform == "moviebox" } -> streamHeaders("moviebox")
            else -> emptyMap()
        }
    }
    val player = remember(clipHeaders) { buildPlayer(ctx, clipHeaders) }
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
                    AndroidView(factory = { PlayerView(it).apply { useController = false; resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM; this.player = player } }, update = { view -> view.player = player; view.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM }, modifier = Modifier.fillMaxSize())
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
    val player = remember(detail.drama.platform) { buildPlayer(ctx, streamHeaders(detail.drama.platform)) }
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
                    AndroidView(factory = { PlayerView(it).apply { useController = false; this.player = player; resizeMode = if (fitContain) AspectRatioFrameLayout.RESIZE_MODE_FIT else AspectRatioFrameLayout.RESIZE_MODE_ZOOM } }, update = { view -> view.player = player; view.resizeMode = if (fitContain) AspectRatioFrameLayout.RESIZE_MODE_FIT else AspectRatioFrameLayout.RESIZE_MODE_ZOOM }, modifier = Modifier.fillMaxSize())
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
                    Column(
                        Modifier.align(Alignment.BottomStart).padding(start = 18.dp, end = 18.dp, bottom = 132.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            PlayerChip(platformLabel(detail.drama.platform), DS.Green)
                            PlayerChip("Ep ${page + 1}/$total", DS.Sky)
                            if (preferLandscape) PlayerChip("Wide", DS.Amber)
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(detail.drama.title, color = DS.White, fontSize = 20.sp, fontWeight = FontWeight.Black, lineHeight = 24.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Spacer(Modifier.height(5.dp))
                        Text(
                            if (playing) "Swipe naik/turun untuk episode lain" else "Tap layar untuk kontrol",
                            color = DS.Text,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
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

        // Bottom controls — satu area kontrol, tanpa tombol yang menumpuk di sisi video.
        AnimatedVisibility(uiVis || loading || error != null, Modifier.align(Alignment.BottomCenter)) {
            Surface(color = Color(0xD80A0B0F), shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(horizontal = 18.dp, vertical = 12.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(formatMs(curMs), color = DS.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.weight(1f))
                        Text(if (durMs > 0) formatMs(durMs) else "", color = DS.Text, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                    }
                    Slider(
                        value = if (durMs > 0) (curMs.toFloat() / durMs.toFloat()).coerceIn(0f, 1f) else 0f,
                        onValueChange = { isSeeking = true; curMs = (it * durMs).toLong().coerceAtLeast(0) },
                        onValueChangeFinished = { player.seekTo(curMs); saveProgress(pager.currentPage + 1); isSeeking = false },
                        enabled = durMs > 0,
                        colors = SliderDefaults.colors(thumbColor = DS.White, activeTrackColor = DS.Green, inactiveTrackColor = Color(0x55FFFFFF)),
                        modifier = Modifier.fillMaxWidth().height(24.dp)
                    )
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { uiVis = true; epSheet = true }) { Icon(Icons.Rounded.List, "Episode", tint = DS.Text) }
                        IconButton(onClick = { uiVis = true; retryKey++ }) { Icon(Icons.Rounded.Refresh, "Muat ulang", tint = DS.Text) }
                        Spacer(Modifier.weight(1f))
                        IconButton(onClick = { uiVis = true; fitContain = !fitContain; if (!preferLandscape) store.setFitContain(fitContain) }) { Icon(if (fitContain) Icons.Rounded.AspectRatio else Icons.Rounded.Fullscreen, "Ukuran layar", tint = DS.Text) }
                        Spacer(Modifier.width(8.dp))
                        Surface(color = DS.White, contentColor = DS.Bg, shape = CircleShape, modifier = Modifier.size(44.dp).clickable { uiVis = true; if (player.isPlaying) player.pause() else player.play() }) {
                            Box(contentAlignment = Alignment.Center) { Icon(if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, if (playing) "Jeda" else "Putar", modifier = Modifier.size(23.dp)) }
                        }
                    }
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
private fun PlayerChip(text: String, dot: Color) {
    Surface(color = Color(0x66000000), shape = RoundedCornerShape(50)) {
        Row(Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(7.dp).clip(CircleShape).background(dot))
            Spacer(Modifier.width(6.dp))
            Text(text, color = DS.White, fontSize = 11.sp, fontWeight = FontWeight.Black, maxLines = 1)
        }
    }
}

@Composable
private fun SideBtn(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(58.dp).clickable(onClick = onClick)) {
        Box(
            Modifier.size(52.dp).clip(CircleShape).background(Color(0x66000000)).border(1.dp, Color(0x22FFFFFF), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, label, tint = DS.White, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.height(5.dp))
        Text(label, color = DS.White, fontSize = 9.sp, fontWeight = FontWeight.Black, maxLines = 1, textAlign = TextAlign.Center)
    }
}

// ─────────────────────────────────────────────────────────────────
// HELPERS
// ─────────────────────────────────────────────────────────────────

private fun prefersLandscapePlayback(drama: Drama): Boolean = drama.platform == "moviebox" || drama.platform == "drakor"

private fun buildMediaItem(s: StreamResult): MediaItem {
    val url = cleanUrl(s.url)
    val b = MediaItem.Builder().setUri(Uri.parse(url))
    val lower = url.lowercase()
    when {
        lower.contains("m3u8") -> b.setMimeType(MimeTypes.APPLICATION_M3U8)
        lower.contains(".mp4") -> b.setMimeType(MimeTypes.APPLICATION_MP4)
    }
    val subtitle = cleanUrl(s.subtitle)
    if (subtitle.isNotBlank()) {
        val mime = if (subtitle.lowercase().endsWith(".vtt")) MimeTypes.TEXT_VTT else MimeTypes.APPLICATION_SUBRIP
        b.setSubtitleConfigurations(listOf(MediaItem.SubtitleConfiguration.Builder(Uri.parse(subtitle)).setMimeType(mime).setLanguage("id").setSelectionFlags(C.SELECTION_FLAG_DEFAULT).build()))
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

    private sealed class MBRes {
        data class Ok(val url: String, val subtitle: String) : MBRes()
        data class Err(val msg: String) : MBRes()
    }

    private suspend fun resolveMovieboxFrom(
        streamBase: String, id: String, ep: Int, subjectType: Int, resolutions: List<Int>
    ): MBRes {
        var sawExpiredLink = false
        fun linkOf(o: JSONObject?): String {
            val u = cleanUrl(o?.stringAny("resourceLink").orEmpty())
            if (u.isNotBlank() && isMovieboxExpired(u)) {
                sawExpiredLink = true
                return ""
            }
            return u
        }
        fun subOf(o: JSONObject?): String = cleanUrl(o?.optJSONObject("subtitle")?.stringAny("url").orEmpty())
        fun codecOf(o: JSONObject?): String = o?.stringAny("codecName", "codec").orEmpty().lowercase()
        suspend fun mbJson(url: String): JSONObject? {
            var last: Throwable? = null
            repeat(2) { i ->
                if (i > 0) delay(500L)
                try {
                    val j = getJson(url)
                    val code = j.optInt("code", 200)
                    val msg = j.stringAny("message", "error_msg", "error")
                    if (code != 0 && code != 200) {
                        last = RuntimeException(msg.ifBlank { "HTTP $code" })
                        return@repeat
                    }
                    return j
                } catch (ce: CancellationException) { throw ce }
                catch (t: Throwable) { last = t }
            }
            return null
        }

        return if (subjectType == 2) {
            val episodeByKey = HashMap<Int, Triple<String, String, String>>()
            var bestH264: Pair<String, String>? = null
            for (r in resolutions) {
                val stamp = System.currentTimeMillis()
                val data = mbJson("$streamBase/download-series?subjectId=${enc(id)}&se=1&resolution=$r&_=$stamp")?.optJSONObject("data") ?: continue
                val eps = data.optJSONArray("episodes")?.objects().orEmpty()
                for (e in eps) {
                    val epNum = e.intAny("ep", 1)
                    if (epNum != ep) continue
                    val url = linkOf(e)
                    if (url.isBlank()) continue
                    val sub = subOf(e); val codec = codecOf(e)
                    if (codec.contains("h264") && bestH264 == null) bestH264 = url to sub
                    episodeByKey.putIfAbsent(epNum, Triple(url, sub, codec))
                }
                if (bestH264 != null) return MBRes.Ok(bestH264.first, bestH264.second)
            }
            val fallback = episodeByKey[ep]
            if (fallback != null) return MBRes.Ok(fallback.first, fallback.second)
            MBRes.Err(if (sawExpiredLink) "Link MovieBox expired. Tekan Coba Lagi." else "Episode $ep belum tersedia di MovieBox.")
        } else {
            var fallbackUrl = ""; var fallbackSub = ""
            for (r in resolutions) {
                val stamp = System.currentTimeMillis()
                val data = mbJson("$streamBase/download-movie?subjectId=${enc(id)}&resolution=$r&_=$stamp")?.optJSONObject("data") ?: continue
                val files = data.optJSONArray("files")?.objects().orEmpty().filter { linkOf(it).isNotBlank() }
                val picked = files.firstOrNull { codecOf(it).contains("h264") }
                    ?: files.firstOrNull { !codecOf(it).contains("hevc") }
                    ?: files.firstOrNull()
                if (picked != null) {
                    val pickedUrl = linkOf(picked)
                    val pickedSub = cleanUrl(data.optJSONObject("subtitle")?.stringAny("url").orEmpty())
                    val pickedCodec = codecOf(picked)
                    if (pickedCodec.contains("h264")) return MBRes.Ok(pickedUrl, pickedSub)
                    if (fallbackUrl.isBlank()) { fallbackUrl = pickedUrl; fallbackSub = pickedSub }
                }
            }
            if (fallbackUrl.isNotBlank()) return MBRes.Ok(fallbackUrl, fallbackSub)
            MBRes.Err(if (sawExpiredLink) "Link MovieBox expired. Tekan Coba Lagi." else "Video MovieBox belum tersedia.")
        }
    }

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
        if (d.drama.platform in setOf("melolo", "moviebox", "drakor", "dramanova", "goodshort")) {
            return resolveStream(d, ep, ds)
        }
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
        val p = input.platform
        val url = detailUrl(input)
        // Untuk MovieBox movie, coba hit endpoint detail seperti biasa agar
        // sinopsis/poster/tags bisa terisi. Kalau endpoint gagal (mis. subjectType
        // tidak dikenali atau struktur beda), fallback ke data listing.
        val json: JSONObject = if (p == "moviebox") {
            runCatching { getJson(url) }.getOrNull() ?: JSONObject()
        } else {
            getJson(url)
        }
        if (p == "drakor") {
            val info = json.optJSONObject("info") ?: error("Detail tidak ditemukan")
            val epsArr = json.optJSONObject("episodes")?.optJSONArray("data") ?: JSONArray()
            val eps = epsArr.objects().mapIndexed { i, o ->
                val label = o.stringAny("episode_label", "title", "label")
                val streamId = o.stringAny("streaming")
                val locked = label.contains("premium", true) || streamId.isBlank() || !o.optBoolean("cdn_ready", true)
                EpisodeInfo(o.intAny("episode_number", i + 1), streamId, label, locked)
            }
            val d = normalize(info, p).copy(id = info.stringAny("id").ifBlank { input.id }, title = info.stringAny("title").ifBlank { input.title }, poster = fixImg(info.stringAny("image").ifBlank { input.poster }), description = cleanText(info.stringAny("meta_sinopsis", "shoot", "content", "meta_description")).ifBlank { input.description }, episodes = eps.size.takeIf { it > 0 } ?: info.intAny("meta_episode", input.episodes), platform = p, subjectType = 2)
            return Detail(d, eps)
        }
        val data = json.optJSONObject("data") ?: error("Detail tidak ditemukan")
        if (p == "goodshort" && data.has("book")) {
            val book = data.optJSONObject("book") ?: data; val list = data.optJSONArray("list") ?: JSONArray()
            val intro = book.stringAny("introduction").takeUnless { it == "1" || it.equals("success", true) }.orEmpty()
            val d = normalize(book, p).copy(id = book.stringAny("bookId").ifBlank { input.id }, title = book.stringAny("bookName").ifBlank { input.title }, description = intro.ifBlank { input.description }, episodes = book.intAny("chapterCount", list.length()), poster = fixImg(book.stringAny("cover").ifBlank { input.poster }), platform = p)
            return Detail(d, (0 until list.length()).map { EpisodeInfo(it + 1) })
        }
        if (p == "moviebox") {
            // Kalau endpoint detail berhasil, pakai data. Kalau fallback JSON kosong,
            // pakai data dari listing (input) dengan 1 episode (movie).
            val mbData = json.optJSONObject("data")
            if (mbData != null) {
                val rd0 = mbData.optJSONArray("resourceDetectors")?.optJSONObject(0)
                val total = rd0?.intAny("totalEpisode", 0)
                    ?: mbData.optJSONObject("resourceDetectors")?.intAny("totalEpisode", 0)
                    ?: input.episodes
                val d = normalize(mbData, p).copy(
                    id = mbData.stringAny("subjectId").ifBlank { input.id },
                    title = mbData.stringAny("title").ifBlank { input.title },
                    description = mbData.stringAny("description").ifBlank { input.description },
                    episodes = if (mbData.intAny("subjectType", 1) == 2) max(total, 1) else 1,
                    poster = fixImg(mbData.coverUrl().ifBlank { input.poster }),
                    tags = mbData.stringAny("genre").split(",").map { it.trim() }.filter { it.isNotBlank() },
                    subjectType = mbData.intAny("subjectType", input.subjectType),
                    platform = p
                )
                return Detail(d, (1..d.episodes.coerceAtLeast(1)).map { EpisodeInfo(it) })
            }
            // Fallback: pakai data dari listing, treat sebagai movie 1 episode.
            val d = input.copy(episodes = 1, subjectType = input.subjectType.takeIf { it != 0 } ?: 1, platform = p)
            return Detail(d, listOf(EpisodeInfo(1)))
        }
        val d = normalize(data, p).let { it.copy(id = it.id.ifBlank { input.id }, title = it.title.ifBlank { input.title }, poster = fixImg(it.poster.ifBlank { input.poster }), description = it.description.ifBlank { input.description }, episodes = max(it.episodes, input.episodes), platform = p) }
        val epsArr = data.optJSONArray("video_list") ?: data.optJSONArray("episode_list") ?: data.optJSONArray("episodes") ?: data.optJSONArray("chapterList")
        val eps = epsArr?.objects()?.mapIndexed { i, o -> EpisodeInfo(o.intAny("episode", "episode_no", "chapterIndex", i + 1), o.stringAny("streaming"), o.stringAny("episode_label", "title", "label")) }.orEmpty()
        val total = max(d.episodes, eps.size)
        return Detail(d.copy(episodes = total), if (eps.isNotEmpty()) eps else (1..total.coerceAtLeast(1)).map { EpisodeInfo(it) })
    }

    suspend fun resolveStream(d: Detail, ep: Int, ds: Boolean): StreamResult {
        val drama = d.drama; val p = drama.platform; val base = apiBase(p); val id = drama.id; val res = if (ds) 480 else 720
        return when (p) {
            "melolo" -> {
                // streamv2 kadang menyimpan source ByteDance lama. Buat ulang URL proxy dari
                // /stream yang fresh + kid, supaya proxy Melolo dapat mendekripsi stream terbaru.
                val stamp = System.currentTimeMillis()
                val raw = runCatching { getJson("$base/stream?id=${enc(id)}&ep=$ep&_=$stamp") }.getOrNull()
                val qualities = raw?.optJSONArray("qualities")?.objects().orEmpty()
                val selected = qualities.firstOrNull { it.stringAny("label").contains("720") }
                    ?: qualities.firstOrNull { it.stringAny("label").contains("540") }
                    ?: qualities.firstOrNull()
                val source = selected?.stringAny("url", "backup_url").orEmpty()
                val kid = selected?.stringAny("kid").orEmpty()
                if (source.isNotBlank() && kid.isNotBlank()) {
                    StreamResult("$base/melolo?url=${enc(source)}&kid=${enc(kid)}")
                } else {
                    // Tetap pertahankan resolver v2 bila format /stream berubah di provider.
                    val v2 = getJson("$base/streamv2?id=${enc(id)}&ep=$ep&_=$stamp")
                    val url = extractStreamV2Url(v2)
                    if (url.isNotBlank()) StreamResult(url) else error("Stream Melolo tidak tersedia")
                }
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
                val info = d.episodes.firstOrNull { it.number == ep } ?: d.episodes.getOrNull(ep - 1)
                if (info?.locked == true) {
                    error("Episode Drakor ini masih premium / belum dibuka gratis. Coba episode lain atau tunggu jadwal gratisnya.")
                }
                val streaming = info?.streaming.orEmpty()
                if (streaming.isBlank()) error("Episode Drakor belum punya link stream")
                val j = getJson("$base/stream?streaming=${enc(streaming)}")
                val url = if (ds) {
                    j.stringAny("480p", "360p", "720p", "1080p", "url")
                } else {
                    j.stringAny("720p", "1080p", "480p", "360p", "url")
                }
                StreamResult(url)
            }
            "moviebox" -> {
                // Worker baru (new-api.sonzaix.workers.dev) sekarang mengembalikan
                // link CDN bcdn.hakunaymatata.com. Fallback ke host lama kalau worker baru
                // error (geo-block / 5xx) supaya user masih bisa nonton.
                val streamBaseCandidates = listOfNotNull(
                    base,
                    "https://api.sonzaix.indevs.in/moviebox".takeIf { base.contains("new-api") }
                ).distinct()
                // Movie: endpoint hanya balas 360/480 HEVC + 1080 H264. Series: 720 bisa HEVC/H264 campur.
                val resolutions = if (drama.subjectType == 2) {
                    listOf(res, 1080, 720, 480, 360).distinct()
                } else {
                    listOf(1080, res, 720, 480, 360).distinct()
                }

                var lastErr: String? = null
                for (streamBase in streamBaseCandidates) {
                    val result = resolveMovieboxFrom(streamBase, id, ep, drama.subjectType, resolutions)
                    when (result) {
                        is MBRes.Ok -> return StreamResult(result.url, result.subtitle)
                        is MBRes.Err -> lastErr = result.msg
                    }
                }
                error(lastErr ?: "Video MovieBox belum tersedia.")
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
                val url = play.stringAny("videoUrl", "video_url", "main_url", "backup_url")
                    .ifBlank { q?.stringAny("main_url", "backup_url").orEmpty() }
                StreamResult(cleanUrl(url), subtitleFrom(data.optJSONObject("info")?.optJSONArray("subtitle_tracks")))
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
        "moviebox" -> {
            // /global sering timeout/stale di API MovieBox. Pakai endpoint yang stabil supaya home tidak nyangkut shimmer.
            h = "$base/homepage?tabId=0"
            pop = "$base/indonesia?page=$sp&perPage=20"
            nw = "$base/horror?page=$sp&perPage=20"
        }
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
private fun subtitleFrom(arr: JSONArray?): String { val list = arr?.objects().orEmpty(); return (list.firstOrNull { it.stringAny("language", "lang").startsWith("id", true) } ?: list.firstOrNull())?.stringAny("url", "label").orEmpty() }
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
private fun isExpiredSignedUrl(url: String, bufferSeconds: Long = 45L): Boolean {
    val now = System.currentTimeMillis() / 1000L
    val match = Regex("[?&](?:expires|expiredTime)=([0-9]{10,13})").find(url) ?: return false
    val raw = match.groupValues[1].toLongOrNull() ?: return false
    val expiresAt = if (raw > 9_999_999_999L) raw / 1000L else raw
    return expiresAt <= now + bufferSeconds
}
/**
 * CDN MovieBox (bcdn.hakunaymatata.com) menandatangani link dengan `sign=...&t=<issued-at-epoch>`.
 * Parameter `t` adalah waktu penandatanganan, BUKAN waktu kedaluwarsa. TTL sesungguhnya
 * ditentukan di sisi Policy/Signature (tidak terekspos di parameter). Jangan salahkan
 * `t=` sebagai expired; cuma deteksi jika Policy-nya sudah lewat via Policy blob.
 */
private fun isMovieboxExpired(url: String): Boolean {
    if (url.contains("hakunaymatata.com")) {
        // CDN CloudFront pakai Policy=base64(SATS) untuk expiry.
        // - Kalau ada Policy, parse dan cek DateLessThan/AWS:EpochTime.
        // - Kalau tidak ada Policy (hanya sign+t), TTL tidak diketahui — anggap valid
        //   biar player yang coba. Kalau 403/429, user bisa Retry.
        val policyMatch = Regex("[?&]Policy=([^&]+)").find(url) ?: return false
        return runCatching {
            val b64 = policyMatch.groupValues[1].replace('-', '+').replace('_', '/')
            val decoded = android.util.Base64.decode(b64, android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP)
            val pol = String(decoded)
            // Key "DateLessThan":{"AWS:EpochTime":N}
            val exp = Regex("DateLessThan[^}]*?\"(?:AWS:)?EpochTime\"\\s*:\\s*(\\d+)").find(pol)
                ?.groupValues?.get(1)?.toLongOrNull()
                ?: Regex("\"(?:AWS:)?EpochTime\"\\s*:\\s*(\\d+)").find(pol)?.groupValues?.get(1)?.toLongOrNull()
            // Buffer kecil: kalau expiry < now+5 detik, anggap expired
            exp != null && exp < System.currentTimeMillis() / 1000L - 5L
        }.getOrDefault(false)
    }
    return isExpiredSignedUrl(url)
}
private fun enc(s: String) = URLEncoder.encode(s, "UTF-8")
private fun normalizeKey(s: String) = s.lowercase().replace(Regex("[^a-z0-9\\p{L}\\s]"), " ").replace(Regex("\\s+"), " ").trim()
