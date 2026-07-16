package com.dramaku.app

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color as AndroidColor
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
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.input.pointer.pointerInput
import coil.compose.AsyncImage
import com.dramaku.app.data.NativeRemoteConfig
import com.dramaku.app.data.RemoteConfigRepository
import com.dramaku.app.storage.ProgressKeys
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
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView
import androidx.media3.ui.AspectRatioFrameLayout
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit
import kotlin.math.max
import kotlin.math.min

private val Bg = Color(0xFF05080D)
private val Bg2 = Color(0xFF09121B)
private val Bg3 = Color(0xFF101B27)
private val Card = Color(0xFF0D1722)
private val Accent = Color(0xFF10F5A6)
private val Accent2 = Color(0xFF34D399)
private val Text = Color(0xFFEFFFF7)
private val Muted = Color(0xFF91A4BA)
private val Danger = Color(0xFFFB7185)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = AndroidColor.BLACK
        window.navigationBarColor = AndroidColor.BLACK
        setContent {
            DramakuTheme {
                DramakuNativeApp()
            }
        }
    }
}

@Composable
private fun DramakuTheme(content: @Composable () -> Unit) {
    val colors = darkColorScheme(
        primary = Accent,
        secondary = Accent2,
        background = Bg,
        surface = Bg2,
        onPrimary = Color.Black,
        onBackground = Text,
        onSurface = Text
    )
    MaterialTheme(colorScheme = colors, typography = Typography(), content = content)
}

private enum class RootTab(val title: String, val showInNav: Boolean = true) {
    Clips("Cuplikan"),
    Home("Temukan"),
    Rewards("Hadiah"),
    Library("Daftar Saya"),
    Settings("Profil"),
    Search("Cari", false)
}

private data class PlatformInfo(val id: String, val label: String, val base: String, val logoUrl: String = "")
private data class Drama(
    val id: String,
    val title: String,
    val description: String = "",
    val poster: String = "",
    val episodes: Int = 0,
    val views: String = "",
    val tags: List<String> = emptyList(),
    val platform: String = "melolo",
    val subjectType: Int = 1
)
private data class EpisodeInfo(val number: Int, val streaming: String = "")
private data class Detail(val drama: Drama, val episodes: List<EpisodeInfo> = emptyList())
private data class HomeBundle(val recommended: List<Drama>, val popular: List<Drama>, val newest: List<Drama>)
private data class StreamResult(val url: String, val subtitle: String = "")
private data class PlayerSession(val detail: Detail, val startEpisode: Int)
private data class HistoryItem(
    val id: String,
    val title: String,
    val poster: String,
    val platform: String,
    val episode: Int,
    val pos: Long = 0L,
    val dur: Long = 0L,
    val updated: Long = System.currentTimeMillis()
) {
    val pct: Int get() = if (dur > 0) min(99, max(0, ((pos * 100) / dur).toInt())) else 0
}

private sealed class Load<out T> {
    object Idle : Load<Nothing>()
    object Loading : Load<Nothing>()
    data class Ok<T>(val data: T) : Load<T>()
    data class Err(val message: String) : Load<Nothing>()
}

private val Platforms = listOf(
    PlatformInfo("melolo", "Melolo", "https://api.sonzaix.indevs.in/melolo", "https://www.google.com/s2/favicons?sz=128&domain=melolo.id"),
    PlatformInfo("freereels", "FreeReels", "https://api.sonzaix.indevs.in/freereels", "https://www.google.com/s2/favicons?sz=128&domain=mydramawave.com"),
    PlatformInfo("flickreels", "FlickReels", "https://api.sonzaix.indevs.in/flickreels", "https://www.google.com/s2/favicons?sz=128&domain=flickreels.com"),
    PlatformInfo("dramanova", "DramaNova", "https://api.sonzaix.indevs.in/dramanova", "https://www.google.com/s2/favicons?sz=128&domain=dramanova.app"),
    PlatformInfo("reelshort", "ReelShort", "https://api.sonzaix.indevs.in/reelshort", "https://www.google.com/s2/favicons?sz=128&domain=reelshort.com"),
    PlatformInfo("netshort", "NetShort", "https://api.sonzaix.indevs.in/netshort", "https://www.google.com/s2/favicons?sz=128&domain=netshort.com"),
    PlatformInfo("dramabox", "DramaBox", "https://api.sonzaix.indevs.in/dramabox", "https://www.google.com/s2/favicons?sz=128&domain=dramaboxapp.com"),
    PlatformInfo("goodshort", "GoodShort", "https://api.sonzaix.indevs.in/goodshort", "https://www.google.com/s2/favicons?sz=128&domain=goodshort.com"),
    PlatformInfo("moviebox", "MovieBox", "https://api.sonzaix.indevs.in/moviebox", "https://www.google.com/s2/favicons?sz=128&domain=moviebox.ng"),
    PlatformInfo("drakor", "Drakor", "https://api.sonzaix.indevs.in/drama", "https://www.google.com/s2/favicons?sz=128&domain=drakor.id")
)
private fun platform(id: String) = Platforms.firstOrNull { it.id == id } ?: Platforms.first()
private fun platformLabel(id: String) = platform(id).label
private fun apiBase(id: String) = platform(id).base

@Composable
private fun DramakuNativeApp() {
    val context = LocalContext.current
    val store = remember { LocalStore(context) }
    val repo = remember { DramakuRepository() }
    val remoteRepo = remember { RemoteConfigRepository() }
    val scope = rememberCoroutineScope()

    var tab by remember { mutableStateOf(RootTab.Home) }
    var selectedPlatform by remember { mutableStateOf(store.platform()) }
    var refreshKey by remember { mutableIntStateOf(0) }
    var homeState by remember { mutableStateOf<Load<HomeBundle>>(Load.Idle) }
    var selectedDrama by remember { mutableStateOf<Drama?>(null) }
    var detailState by remember { mutableStateOf<Load<Detail>>(Load.Idle) }
    var remoteConfig by remember { mutableStateOf<NativeRemoteConfig?>(null) }
    var remoteError by remember { mutableStateOf<String?>(null) }
    var dataTick by remember { mutableIntStateOf(0) }
    var resolvingEpisode by remember { mutableIntStateOf(0) }
    var playerSession by remember { mutableStateOf<PlayerSession?>(null) }
    var clipFeedItems by remember { mutableStateOf<List<Drama>>(emptyList()) }
    var pendingResume by remember { mutableStateOf<HistoryItem?>(null) }

    val playerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val data = result.data
        if (result.resultCode == Activity.RESULT_OK && data != null) {
            val id = data.getStringExtra(PlayerActivity.RESULT_DRAMA_ID).orEmpty()
            val platformId = data.getStringExtra(PlayerActivity.RESULT_PLATFORM).orEmpty()
            val ep = data.getIntExtra(PlayerActivity.RESULT_EPISODE, 1)
            val pos = data.getLongExtra(PlayerActivity.RESULT_POSITION, 0L)
            val dur = data.getLongExtra(PlayerActivity.RESULT_DURATION, 0L)
            if (id.isNotBlank() && platformId.isNotBlank()) {
                store.updateProgress(id, platformId, ep, pos, dur)
                dataTick++
            }
        }
    }

    fun openPlayer(detail: Detail, ep: Int) {
        playerSession = PlayerSession(detail, ep)
    }

    LaunchedEffect(refreshKey) {
        runCatching { remoteRepo.load() }
            .onSuccess { remoteConfig = it; remoteError = null }
            .onFailure { remoteError = it.message ?: "Remote config gagal" }
    }

    LaunchedEffect(selectedPlatform, refreshKey) {
        homeState = Load.Loading
        homeState = runCatching { repo.loadHome(selectedPlatform) }
            .fold({ Load.Ok(it) }, { Load.Err(it.message ?: "Gagal memuat beranda") })
    }

    LaunchedEffect(selectedDrama) {
        val d = selectedDrama ?: return@LaunchedEffect
        detailState = Load.Loading
        detailState = runCatching { repo.loadDetail(d) }
            .fold({ Load.Ok(it) }, { Load.Err(it.message ?: "Gagal memuat detail") })
    }

    LaunchedEffect(detailState, pendingResume) {
        val pending = pendingResume ?: return@LaunchedEffect
        val detail = (detailState as? Load.Ok)?.data ?: return@LaunchedEffect
        if (detail.drama.id == pending.id && detail.drama.platform == pending.platform) {
            playerSession = PlayerSession(detail, pending.episode.coerceAtLeast(1))
            selectedDrama = null
            pendingResume = null
        }
    }

    BackHandler(enabled = selectedDrama != null) { selectedDrama = null; pendingResume = null }

    Box(Modifier.fillMaxSize().background(Bg)) {
        Scaffold(
            containerColor = Bg,
            bottomBar = {
                NavigationBar(containerColor = Color(0xEE071018), tonalElevation = 0.dp) {
                    RootTab.values().filter { it.showInNav }.forEach { item ->
                        val iconVector = when (item) {
                            RootTab.Clips -> Icons.Rounded.PlayCircle
                            RootTab.Home -> Icons.Rounded.Home
                            RootTab.Rewards -> Icons.Rounded.CardGiftcard
                            RootTab.Library -> Icons.Rounded.VideoLibrary
                            RootTab.Settings -> Icons.Rounded.Person
                            RootTab.Search -> Icons.Rounded.Search
                        }
                        NavigationBarItem(
                            selected = tab == item,
                            onClick = { tab = item },
                            icon = { Icon(iconVector, contentDescription = item.title, tint = if (tab == item) Accent else Muted, modifier = Modifier.size(22.dp)) },
                            label = { Text(item.title, color = if (tab == item) Accent else Muted, fontSize = 10.sp, maxLines = 1) },
                            colors = NavigationBarItemDefaults.colors(indicatorColor = Color(0x2210F5A6))
                        )
                    }
                }
            }
        ) { pad ->
            Box(Modifier.padding(pad).fillMaxSize()) {
                when (tab) {
                    RootTab.Clips -> ClipsScreen(
                        state = homeState,
                        repo = repo,
                        store = store,
                        onBackHome = { tab = RootTab.Home },
                        onWatchFull = { detail -> playerSession = PlayerSession(detail, 1) },
                        onOpenDetail = { selectedDrama = it }
                    )
                    RootTab.Home -> HomeScreen(
                        platformId = selectedPlatform,
                        state = homeState,
                        history = store.history(dataTick),
                        remoteConfig = remoteConfig,
                        remoteError = remoteError,
                        onPlatform = {
                            val allowed = remoteConfig?.isPlatformEnabled(it) ?: true
                            if (!allowed) {
                                Toast.makeText(context, "${platformLabel(it)}: ${remoteConfig?.platform(it)?.reason ?: "Maintenance"}", Toast.LENGTH_SHORT).show()
                            } else {
                                selectedPlatform = it
                                store.setPlatform(it)
                                refreshKey++
                            }
                        },
                        onRefresh = { refreshKey++ },
                        onDrama = { selectedDrama = it },
                        onSearch = { tab = RootTab.Search },
                        onRandom = {
                            val bundle = (homeState as? Load.Ok)?.data
                            val pool = (bundle?.popular.orEmpty() + bundle?.newest.orEmpty() + bundle?.recommended.orEmpty()).filter { it.id.isNotBlank() }
                            if (pool.isNotEmpty()) selectedDrama = pool.random()
                        },
                        onClips = {
                            val bundle = (homeState as? Load.Ok)?.data
                            val pool = (bundle?.popular.orEmpty() + bundle?.newest.orEmpty() + bundle?.recommended.orEmpty())
                                .filter { it.id.isNotBlank() && it.poster.isNotBlank() }
                                .distinctBy { it.platform + it.id }
                            if (pool.isNotEmpty()) clipFeedItems = pool.shuffled().take(80)
                            else Toast.makeText(context, "Cuplikan belum tersedia", Toast.LENGTH_SHORT).show()
                        },
                        onResume = { h ->
                            pendingResume = h
                            selectedDrama = Drama(h.id, h.title, poster = h.poster, platform = h.platform)
                        }
                    )
                    RootTab.Search -> SearchScreen(repo, store, onDrama = { selectedDrama = it }, dataTick = dataTick, bump = { dataTick++ })
                    RootTab.Rewards -> RewardsScreen()
                    RootTab.Library -> LibraryScreen(store, dataTick, onDrama = { selectedDrama = it })
                    RootTab.Settings -> SettingsScreen(store, dataTick, bump = { dataTick++ })
                }
            }
        }

        AnimatedVisibility(selectedDrama != null) {
            val initial = selectedDrama
            if (initial != null) {
                DetailScreen(
                    state = detailState,
                    fallback = initial,
                    store = store,
                    resolvingEpisode = resolvingEpisode,
                    onClose = { selectedDrama = null },
                    onPlay = { detail, ep -> openPlayer(detail, ep) },
                    onFavChanged = { dataTick++ },
                    onShare = { shareDrama(context, it) }
                )
            }
        }

        playerSession?.let { session ->
            VerticalEpisodePlayer(
                detail = session.detail,
                startEpisode = session.startEpisode,
                repo = repo,
                store = store,
                onClose = {
                    playerSession = null
                    dataTick++
                }
            )
        }

        if (clipFeedItems.isNotEmpty()) {
            ClipFeedPlayer(
                items = clipFeedItems,
                repo = repo,
                store = store,
                onClose = { clipFeedItems = emptyList() },
                onWatchFull = { detail ->
                    clipFeedItems = emptyList()
                    playerSession = PlayerSession(detail, 1)
                },
                onOpenDetail = { drama ->
                    clipFeedItems = emptyList()
                    selectedDrama = drama
                }
            )
        }
    }
}

@Composable
private fun ClipsScreen(
    state: Load<HomeBundle>,
    repo: DramakuRepository,
    store: LocalStore,
    onBackHome: () -> Unit,
    onWatchFull: (Detail) -> Unit,
    onOpenDetail: (Drama) -> Unit
) {
    val bundle = (state as? Load.Ok)?.data
    val pool = remember(bundle) {
        (bundle?.popular.orEmpty() + bundle?.newest.orEmpty() + bundle?.recommended.orEmpty())
            .filter { it.id.isNotBlank() && it.poster.isNotBlank() }
            .distinctBy { it.platform + it.id }
            .shuffled()
            .take(80)
    }
    if (pool.isEmpty()) {
        Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = Accent)
            Spacer(Modifier.height(14.dp))
            Text("Menyiapkan feed cuplikan...", color = Text, fontWeight = FontWeight.Bold)
            TextButton(onClick = onBackHome, modifier = Modifier.padding(top = 10.dp)) {
                Text("Kembali ke Beranda", color = Accent)
            }
        }
    } else {
        ClipFeedPlayer(
            items = pool,
            repo = repo,
            store = store,
            onClose = onBackHome,
            onWatchFull = onWatchFull,
            onOpenDetail = onOpenDetail
        )
    }
}

@Composable
private fun RewardsScreen() {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(Color(0x2210F5A6)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.CardGiftcard, contentDescription = null, tint = Accent, modifier = Modifier.size(36.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text("Pusat Hadiah", color = Text, fontSize = 24.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(6.dp))
        Text(
            "Nonton drama favoritmu setiap hari buat klaim koin, badge eksklusif, dan penawaran spesial.",
            color = Muted,
            fontSize = 13.sp,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
    }
}

@Composable
private fun HomeScreen(
    platformId: String,
    state: Load<HomeBundle>,
    history: List<HistoryItem>,
    remoteConfig: NativeRemoteConfig?,
    remoteError: String?,
    onPlatform: (String) -> Unit,
    onRefresh: () -> Unit,
    onDrama: (Drama) -> Unit,
    onSearch: () -> Unit,
    onRandom: () -> Unit,
    onClips: () -> Unit,
    onResume: (HistoryItem) -> Unit
) {
    val listState = rememberLazyListState()
    var popularVisible by remember(platformId, state) { mutableIntStateOf(12) }
    var newestVisible by remember(platformId, state) { mutableIntStateOf(12) }
    var recommendedVisible by remember(platformId, state) { mutableIntStateOf(12) }
    var loadMorePulse by remember { mutableIntStateOf(0) }

    val shouldLoadMore = remember {
        derivedStateOf {
            val totalItems = listState.layoutInfo.totalItemsCount
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItems > 0 && lastVisible >= totalItems - 3
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value) {
            popularVisible = min(popularVisible + 12, 60)
            newestVisible = min(newestVisible + 12, 60)
            recommendedVisible = min(recommendedVisible + 12, 60)
            loadMorePulse++
        }
    }

    val bundle = (state as? Load.Ok)?.data
    val catalogPool = remember(bundle) {
        (bundle?.popular.orEmpty() + bundle?.newest.orEmpty() + bundle?.recommended.orEmpty())
            .distinctBy { it.platform + it.id }
    }

    LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
        item { Header(platformId, onSearch, onRefresh) }
        item { PlatformDropdown(platformId, state is Load.Loading, remoteConfig, onPlatform) }
        item { RemoteConfigBanner(remoteConfig, remoteError) }
        item { PlatformStatusStrip(remoteConfig) }
        item {
            QuickActions(
                onSearch = onSearch,
                onRandom = onRandom,
                onClips = onClips
            )
        }

        if (history.isNotEmpty()) item { ContinueWatching(history, onResume) }
        if (catalogPool.isNotEmpty()) item { ForYouSection(history, catalogPool, onDrama) }

        when (state) {
            Load.Idle, Load.Loading -> item { LoadingHome() }
            is Load.Err -> item { ErrorBox(state.message, onRefresh) }
            is Load.Ok -> {
                val data = state.data
                val spotlight = data.popular.firstOrNull() ?: data.recommended.firstOrNull() ?: data.newest.firstOrNull()
                if (spotlight != null) item { Spotlight(spotlight, onDrama) }
                if (data.popular.isNotEmpty()) item { DramaRail("Populer Saat Ini", data.popular.take(popularVisible), onDrama) }
                if (data.newest.isNotEmpty()) item { DramaGridSection("Terbaru", data.newest.take(newestVisible), onDrama) }
                if (data.recommended.isNotEmpty()) item { DramaGridSection("Rekomendasi", data.recommended.take(recommendedVisible), onDrama) }
                item { HomeLoadMoreFooter(data, popularVisible, newestVisible, recommendedVisible, loadMorePulse) }
                item { Footer() }
            }
        }
    }
}

@Composable
private fun HomeLoadMoreFooter(data: HomeBundle, popularVisible: Int, newestVisible: Int, recommendedVisible: Int, pulse: Int) {
    val remaining = (data.popular.size - popularVisible).coerceAtLeast(0) +
            (data.newest.size - newestVisible).coerceAtLeast(0) +
            (data.recommended.size - recommendedVisible).coerceAtLeast(0)
    if (remaining > 0) {
        Column(Modifier.fillMaxWidth().padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = Accent, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
            Spacer(Modifier.height(8.dp))
            Text("Memuat konten berikutnya... ($remaining tersisa)", color = Muted, fontSize = 12.sp)
        }
    } else {
        Text(
            "Semua konten yang tersedia sudah ditampilkan",
            color = Muted,
            fontSize = 12.sp,
            modifier = Modifier.fillMaxWidth().padding(20.dp)
        )
    }
}

@Composable
private fun Header(platformId: String, onSearch: () -> Unit, onRefresh: () -> Unit) {
    Column(
        Modifier.fillMaxWidth()
            .background(Brush.verticalGradient(listOf(Color(0xFF062018), Bg)))
            .padding(18.dp, 18.dp, 18.dp, 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(Accent), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.PlayArrow, contentDescription = "Dramaku", tint = Color.Black, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Dramaku", color = Text, fontSize = 27.sp, fontWeight = FontWeight.Black)
                Text("Sumber · ${platformLabel(platformId)}", color = Muted, fontSize = 12.sp)
            }
            IconButton(onClick = onSearch) {
                Icon(Icons.Rounded.Search, contentDescription = "Cari", tint = Text, modifier = Modifier.size(24.dp))
            }
            IconButton(onClick = onRefresh) {
                Icon(Icons.Rounded.Refresh, contentDescription = "Refresh", tint = Text, modifier = Modifier.size(24.dp))
            }
        }
        Text("Drama pendek, film, dan drakor pilihan dalam satu aplikasi.", color = Muted, fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable
private fun QuickActions(onSearch: () -> Unit, onRandom: () -> Unit, onClips: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Surface(
            color = Bg3,
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.weight(1f).clickable(onClick = onSearch)
        ) {
            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Search, contentDescription = null, tint = Accent, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Cari Drama", color = Text, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
        Surface(
            color = Bg3,
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.weight(1f).clickable(onClick = onRandom)
        ) {
            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Shuffle, contentDescription = null, tint = Accent, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Acak Judul", color = Text, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
        Surface(
            color = Bg3,
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.weight(1f).clickable(onClick = onClips)
        ) {
            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.PlayCircle, contentDescription = null, tint = Accent, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Cuplikan", color = Text, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun PlatformLogo(platformId: String, size: androidx.compose.ui.unit.Dp = 36.dp, enabled: Boolean = true) {
    val p = platform(platformId)
    val fallback = when (platformId) {
        "melolo" -> "M"
        "freereels" -> "FR"
        "flickreels" -> "FL"
        "dramanova" -> "DN"
        "reelshort" -> "RS"
        "netshort" -> "NS"
        "dramabox" -> "DB"
        "goodshort" -> "GS"
        "moviebox" -> "MB"
        "drakor" -> "DK"
        else -> p.label.take(2).uppercase()
    }
    Surface(
        color = if (enabled) Color(0xFF07141A) else Color(0xFF171B22),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.size(size)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                fallback,
                color = if (enabled) Accent else Muted,
                fontSize = (size.value * 0.28f).sp,
                fontWeight = FontWeight.Black,
                maxLines = 1
            )
            if (p.logoUrl.isNotBlank()) {
                AsyncImage(
                    model = p.logoUrl,
                    contentDescription = p.label,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(size * 0.64f).clip(RoundedCornerShape(7.dp))
                )
            }
        }
    }
}

@Composable
private fun PlatformDropdown(selected: String, loading: Boolean, remoteConfig: NativeRemoteConfig?, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Box {
            Surface(
                color = Bg3,
                contentColor = Text,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth().clickable { expanded = true }
            ) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    PlatformLogo(selected, size = 42.dp)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Pilih platform", color = Muted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(platformLabel(selected), color = Text, fontSize = 16.sp, fontWeight = FontWeight.Black)
                    }
                    if (loading) {
                        CircularProgressIndicator(color = Accent, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                    } else {
                        Icon(
                            if (expanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                            contentDescription = null,
                            tint = Accent,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Bg2)
            ) {
                Platforms.forEach { p ->
                    val st = remoteConfig?.platform(p.id)
                    val enabled = st?.enabled ?: true
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                PlatformLogo(p.id, size = 34.dp, enabled = enabled)
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text(p.label, color = if (enabled) Text else Muted, fontWeight = FontWeight.Bold)
                                    Text(if (enabled) (st?.reason ?: p.id) else (st?.reason ?: "Maintenance"), color = Muted, fontSize = 11.sp)
                                }
                            }
                        },
                        onClick = {
                            expanded = false
                            if (p.id != selected) onSelect(p.id)
                        }
                    )
                }
            }
        }
        if (loading) PlatformLoadingPlaceholder()
    }
}

@Composable
private fun ShimmerEffect(modifier: Modifier = Modifier, shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(18.dp)) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_anim"
    )
    val shimmerColors = listOf(
        Bg3,
        Color(0xFF1D2F42),
        Bg3
    )
    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim - 300f, translateAnim - 300f),
        end = Offset(translateAnim, translateAnim)
    )
    Box(modifier = modifier.clip(shape).background(brush))
}

@Composable
private fun PlatformLoadingPlaceholder() {
    Column(Modifier.padding(top = 12.dp)) {
        ShimmerEffect(shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth().height(150.dp))
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            repeat(3) { ShimmerEffect(shape = RoundedCornerShape(18.dp), modifier = Modifier.weight(1f).height(155.dp)) }
        }
    }
}

@Composable
private fun RemoteConfigBanner(remoteConfig: NativeRemoteConfig?, remoteError: String?) {
    val msg = remoteConfig?.message
    when {
        msg?.enabled == true && (msg.title.isNotBlank() || msg.text.isNotBlank()) -> {
            Surface(color = Color(0x2210F5A6), shape = RoundedCornerShape(20.dp), modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth()) {
                Column(Modifier.padding(14.dp)) {
                    Text(msg.title.ifBlank { "Info Dramaku" }, color = Text, fontWeight = FontWeight.Black)
                    if (msg.text.isNotBlank()) Text(msg.text, color = Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 3.dp))
                }
            }
        }
        remoteError != null -> {
            Surface(color = Color(0x22FB7185), shape = RoundedCornerShape(20.dp), modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth()) {
                Text("Remote config offline · pakai konfigurasi lokal", color = Muted, fontSize = 12.sp, modifier = Modifier.padding(14.dp))
            }
        }
    }
}

@Composable
private fun PlatformStatusStrip(remoteConfig: NativeRemoteConfig?) {
    Column(Modifier.padding(top = 10.dp)) {
        SectionTitle("Status Platform")
        LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(Platforms) { p ->
                val st = remoteConfig?.platform(p.id)
                val enabled = st?.enabled ?: true
                Surface(color = if (enabled) Color(0x1510F5A6) else Color(0x22FB7185), shape = RoundedCornerShape(16.dp)) {
                    Row(Modifier.padding(horizontal = 11.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                        PlatformLogo(p.id, size = 30.dp, enabled = enabled)
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(p.label, color = Text, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(st?.reason ?: if (enabled) "Aktif" else "Maintenance", color = Muted, fontSize = 10.sp, maxLines = 1)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ForYouSection(history: List<HistoryItem>, pool: List<Drama>, onDrama: (Drama) -> Unit) {
    if (history.isEmpty() || pool.isEmpty()) return
    val histPlatforms = history.map { it.platform }.toSet()
    val seen = history.map { it.id }.toSet()
    val titleTokens = history.flatMap { normalizeKey(it.title).split(" ") }.filter { it.length > 2 }.toSet()
    val picks = pool
        .filter { it.id !in seen }
        .map { d ->
            var score = 0
            if (d.platform in histPlatforms) score += 5
            val t = normalizeKey(d.title)
            titleTokens.forEach { if (t.contains(it)) score += 2 }
            if (d.poster.isNotBlank()) score += 1
            d to score
        }
        .filter { it.second > 0 }
        .sortedByDescending { it.second }
        .map { it.first }
        .distinctBy { it.platform + it.id }
        .take(10)
    if (picks.isEmpty()) return
    DramaRail("Buat Kamu", picks, onDrama)
}

@Composable
private fun Pill(text: String, selected: Boolean = false, onClick: () -> Unit = {}) {
    Surface(
        color = if (selected) Accent else Bg3,
        contentColor = if (selected) Color.Black else Text,
        shape = RoundedCornerShape(999.dp),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(text, modifier = Modifier.padding(horizontal = 13.dp, vertical = 8.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun Spotlight(drama: Drama, onDrama: (Drama) -> Unit) {
    Box(
        Modifier.padding(16.dp).height(210.dp).fillMaxWidth().clip(RoundedCornerShape(28.dp)).clickable { onDrama(drama) }
    ) {
        AsyncImage(model = drama.poster, contentDescription = drama.title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color(0xF205080D), Color(0xAA05080D), Color.Transparent))))
        Row(Modifier.fillMaxSize().padding(16.dp), verticalAlignment = Alignment.Bottom) {
            Poster(drama.poster, drama.title, Modifier.width(92.dp).height(136.dp))
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text("Spotlight Hari Ini", color = Accent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(drama.title, color = Text, fontWeight = FontWeight.Black, fontSize = 24.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(drama.description.ifBlank { platformLabel(drama.platform) }, color = Muted, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Accent,
                        contentColor = Color.Black,
                        shape = RoundedCornerShape(999.dp),
                        modifier = Modifier.clickable { onDrama(drama) }
                    ) {
                        Row(Modifier.padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Tonton", fontSize = 12.sp, fontWeight = FontWeight.Black)
                        }
                    }
                    Pill("${drama.episodes.coerceAtLeast(1)} Ep") {}
                }
            }
        }
    }
}

@Composable
private fun ContinueWatching(history: List<HistoryItem>, onResume: (HistoryItem) -> Unit) {
    Column(Modifier.padding(top = 10.dp)) {
        SectionTitle("Lanjutkan Tontonan")
        LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(history.take(8)) { h ->
                Column(Modifier.width(128.dp).clickable { onResume(h) }) {
                    Box {
                        Poster(h.poster, h.title, Modifier.width(128.dp).height(180.dp))
                        Badge("Ep ${h.episode}", Modifier.align(Alignment.TopStart).padding(8.dp))
                        if (h.pct > 0) LinearProgressIndicator(progress = h.pct / 100f, color = Accent, trackColor = Color(0x66000000), modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(4.dp))
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(h.title, color = Text, fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(if (h.pct > 0) "${h.pct}%" else platformLabel(h.platform), color = Muted, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun DramaRail(title: String, items: List<Drama>, onDrama: (Drama) -> Unit) {
    Column(Modifier.padding(top = 14.dp)) {
        SectionTitle(title)
        LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(items) { d -> DramaCard(d, Modifier.width(128.dp), onDrama) }
        }
    }
}

@Composable
private fun DramaGridSection(title: String, items: List<Drama>, onDrama: (Drama) -> Unit) {
    Column(Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp)) {
        SectionTitle(title, padded = false)
        Spacer(Modifier.height(10.dp))
        val rows = items.chunked(3)
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { d -> DramaCard(d, Modifier.weight(1f), onDrama) }
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
            Spacer(Modifier.height(14.dp))
        }
    }
}

@Composable
private fun SectionTitle(title: String, padded: Boolean = true) {
    Text(title, color = Text, fontSize = 20.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = if (padded) 16.dp else 0.dp, vertical = 4.dp))
}

@Composable
private fun DramaCard(drama: Drama, modifier: Modifier, onDrama: (Drama) -> Unit) {
    Column(modifier.clickable { onDrama(drama) }) {
        Box {
            Poster(drama.poster, drama.title, Modifier.fillMaxWidth().aspectRatio(0.71f))
            if (drama.episodes > 0) Badge("${drama.episodes} Ep", Modifier.align(Alignment.TopStart).padding(7.dp))
            Badge(platformLabel(drama.platform), Modifier.align(Alignment.BottomStart).padding(7.dp), dark = true)
        }
        Spacer(Modifier.height(6.dp))
        Text(drama.title.ifBlank { "Tanpa Judul" }, color = Text, fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold)
        Text(drama.views.ifBlank { drama.tags.firstOrNull().orEmpty() }, color = Muted, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun Poster(url: String, title: String, modifier: Modifier) {
    Box(modifier.clip(RoundedCornerShape(18.dp)).background(Bg3), contentAlignment = Alignment.Center) {
        if (url.isNotBlank()) {
            AsyncImage(model = url, contentDescription = title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            Box(
                Modifier.fillMaxSize().background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color(0x44000000), Color(0xCC05080D))
                    )
                )
            )
        } else {
            Icon(Icons.Rounded.PlayArrow, contentDescription = title, tint = Accent, modifier = Modifier.size(32.dp))
        }
    }
}

@Composable
private fun Badge(text: String, modifier: Modifier = Modifier, dark: Boolean = false) {
    Surface(color = if (dark) Color(0xCC05080D) else Accent, contentColor = if (dark) Text else Color.Black, shape = RoundedCornerShape(999.dp), modifier = modifier) {
        Text(text, modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

@Composable
private fun LoadingHome() {
    Column(Modifier.padding(16.dp)) {
        repeat(4) {
            ShimmerEffect(shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth().height(if (it == 0) 180.dp else 120.dp).padding(vertical = 8.dp))
        }
    }
}

@Composable
private fun ErrorBox(message: String, onRetry: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Gagal memuat data", color = Text, fontWeight = FontWeight.Black, fontSize = 21.sp)
        Text(message, color = Muted, modifier = Modifier.padding(8.dp))
        Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = Color.Black)) { Text("Coba Lagi") }
    }
}

@Composable
private fun Footer() {
    Text("Dramaku native MVP · Semua konten milik platform masing-masing", color = Muted, fontSize = 11.sp, modifier = Modifier.fillMaxWidth().padding(22.dp))
}

@Composable
private fun SearchScreen(repo: DramakuRepository, store: LocalStore, onDrama: (Drama) -> Unit, dataTick: Int, bump: () -> Unit) {
    var q by remember { mutableStateOf("") }
    var state by remember { mutableStateOf<Load<List<Drama>>>(Load.Idle) }
    var filter by remember { mutableStateOf("all") }
    val recent = remember(dataTick) { store.recentSearches() }

    LaunchedEffect(q) {
        val query = q.trim()
        if (query.length < 2) {
            state = Load.Idle
            return@LaunchedEffect
        }
        delay(400)
        state = Load.Loading
        filter = "all"
        state = runCatching {
            store.saveRecent(query)
            repo.searchAll(query)
        }.fold({ Load.Ok(it) }, { Load.Err(it.message ?: "Gagal mencari") })
        bump()
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Cari Drama", color = Text, fontSize = 27.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = q,
            onValueChange = { q = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Ketik judul drama...", color = Muted) },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = Muted) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Accent, unfocusedBorderColor = Bg3, focusedTextColor = Text, unfocusedTextColor = Text, cursorColor = Accent)
        )
        if (q.length < 2 && recent.isNotEmpty()) {
            Row(Modifier.fillMaxWidth().padding(top = 18.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Terakhir dicari", color = Muted, modifier = Modifier.weight(1f))
                TextButton(onClick = { store.clearRecentSearches(); bump() }) { Text("Hapus", color = Danger, fontSize = 12.sp) }
            }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(recent) { Pill(it, false) { q = it } }
            }
        }
        Spacer(Modifier.height(14.dp))
        when (state) {
            Load.Idle -> SearchWelcome { q = it }
            Load.Loading -> LinearProgressIndicator(color = Accent, trackColor = Bg3, modifier = Modifier.fillMaxWidth())
            is Load.Err -> ErrorBox((state as Load.Err).message) { q = q.trim() + " " }
            is Load.Ok -> {
                val all = (state as Load.Ok<List<Drama>>).data
                val counts = remember(all) { all.groupingBy { it.platform }.eachCount() }
                val list = if (filter == "all") all else all.filter { it.platform == filter }
                if (all.isEmpty()) EmptyState("Tidak ada hasil untuk “$q”.")
                else Column(Modifier.fillMaxSize()) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(bottom = 12.dp)) {
                        item { Pill("Semua (${all.size})", filter == "all") { filter = "all" } }
                        items(counts.keys.sortedBy { platformLabel(it) }) { p ->
                            Pill("${platformLabel(p)} (${counts[p] ?: 0})", filter == p) { filter = p }
                        }
                    }
                    if (list.isEmpty()) EmptyState("Tidak ada hasil di filter ini.")
                    else LazyVerticalGrid(columns = GridCells.Fixed(3), verticalArrangement = Arrangement.spacedBy(14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
                        items(list, key = { it.platform + it.id }) { d -> DramaCard(d, Modifier.fillMaxWidth(), onDrama) }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchWelcome(onPick: (String) -> Unit) {
    Column(Modifier.fillMaxWidth().padding(top = 20.dp)) {
        EmptyState("Ketik minimal 2 huruf buat cari di 10 platform.")
        Text("Lagi viral", color = Text, fontWeight = FontWeight.Black, fontSize = 18.sp, modifier = Modifier.padding(top = 10.dp, bottom = 10.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(listOf("CEO", "Balas Dendam", "Romantis", "Korea", "China", "Comedy", "Action", "Cinta Kontrak", "Ongoing", "Drakor")) { q ->
                Pill(q, false) { onPick(q) }
            }
        }
    }
}

@Composable
private fun EmptyState(text: String) {
    Column(
        Modifier.fillMaxWidth().padding(30.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Rounded.SearchOff, contentDescription = null, tint = Muted, modifier = Modifier.size(36.dp))
        Spacer(Modifier.height(8.dp))
        Text(text, color = Muted)
    }
}

@Composable
private fun LibraryScreen(store: LocalStore, dataTick: Int, onDrama: (Drama) -> Unit) {
    val context = LocalContext.current
    var localTick by remember { mutableIntStateOf(0) }
    var showFav by remember { mutableStateOf(false) }
    val history = remember(dataTick, localTick) { store.history(dataTick + localTick) }
    val favs = remember(dataTick, localTick) { store.favs() }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Koleksi", color = Text, fontSize = 27.sp, fontWeight = FontWeight.Black)
        Text("Riwayat, progress, dan favorit lokal", color = Muted, fontSize = 12.sp)
        Row(Modifier.padding(vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Pill("Riwayat (${history.size})", !showFav) { showFav = false }
            Pill("Favorit (${favs.size})", showFav) { showFav = true }
        }
        if (showFav) {
            if (favs.isEmpty()) EmptyState("Belum ada favorit.")
            else LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(favs, key = { it.platform + it.id }) { d ->
                    Surface(color = Bg3, shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Poster(d.poster, d.title, Modifier.width(72.dp).height(102.dp).clickable { onDrama(d) })
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f).clickable { onDrama(d) }) {
                                Text(d.title, color = Text, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                Text(platformLabel(d.platform), color = Muted, fontSize = 12.sp)
                            }
                            TextButton(onClick = {
                                store.removeFav(d.id, d.platform)
                                localTick++
                                Toast.makeText(context, "Favorit dihapus", Toast.LENGTH_SHORT).show()
                            }) { Text("Hapus", color = Danger) }
                        }
                    }
                }
            }
        } else {
            if (history.isEmpty()) EmptyState("Belum ada riwayat tontonan.")
            else LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(history, key = { it.id + it.platform }) { h ->
                    Surface(color = Bg3, shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Poster(h.poster, h.title, Modifier.width(72.dp).height(102.dp).clickable { onDrama(Drama(h.id, h.title, poster = h.poster, platform = h.platform)) })
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f).clickable { onDrama(Drama(h.id, h.title, poster = h.poster, platform = h.platform)) }) {
                                Text(h.title, color = Text, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                Text("${platformLabel(h.platform)} · Episode ${h.episode}${if (h.pct > 0) " · ${h.pct}%" else ""}", color = Muted, fontSize = 12.sp)
                                if (h.pct > 0) LinearProgressIndicator(progress = h.pct / 100f, color = Accent, trackColor = Color(0x33000000), modifier = Modifier.fillMaxWidth().padding(top = 8.dp).height(4.dp))
                            }
                            TextButton(onClick = {
                                store.removeHistory(h.id, h.platform)
                                localTick++
                                Toast.makeText(context, "Riwayat dihapus", Toast.LENGTH_SHORT).show()
                            }) { Text("Hapus", color = Danger) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsScreen(store: LocalStore, dataTick: Int, bump: () -> Unit) {
    val context = LocalContext.current
    var dataSaver by remember(dataTick) { mutableStateOf(store.dataSaver()) }
    var autoNext by remember(dataTick) { mutableStateOf(store.autoNext()) }
    var fitContainDefault by remember(dataTick) { mutableStateOf(store.fitContain()) }
    var dialog by remember { mutableStateOf<String?>(null) }
    val historyCount = remember(dataTick) { store.history(dataTick).size }
    val favCount = remember(dataTick) { store.favs().size }
    val recentCount = remember(dataTick) { store.recentSearches().size }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("Saya", color = Text, fontSize = 27.sp, fontWeight = FontWeight.Black)
        Text("Dramaku native final polish", color = Muted, modifier = Modifier.padding(bottom = 18.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
            StatTile("Riwayat", historyCount.toString(), Modifier.weight(1f))
            StatTile("Favorit", favCount.toString(), Modifier.weight(1f))
            StatTile("Recent", recentCount.toString(), Modifier.weight(1f))
        }

        SettingSwitch("Mode hemat data", "Pilih stream kualitas lebih rendah jika tersedia", dataSaver) {
            store.setDataSaver(it); dataSaver = it; bump()
        }
        SettingSwitch("Autoplay episode", "Lanjut episode berikutnya otomatis", autoNext) {
            store.setAutoNext(it); autoNext = it; bump()
        }
        SettingSwitch("Fit contain default", "Tampilkan rasio video asli saat pertama kali buka player", fitContainDefault) {
            store.setFitContain(it); fitContainDefault = it; bump()
        }

        SettingRow("Bersihkan riwayat", "Hapus statistik dan progress tontonan lokal", danger = true) {
            dialog = "history"
        }
        SettingRow("Bersihkan favorit", "Hapus daftar favorit tersimpan", danger = true) {
            dialog = "favs"
        }

        if (dialog != null) {
            AlertDialog(
                onDismissRequest = { dialog = null },
                confirmButton = {
                    TextButton(onClick = {
                        if (dialog == "history") store.clearHistory()
                        if (dialog == "favs") store.clearFavs()
                        dialog = null
                        bump()
                        Toast.makeText(context, "Data berhasil dibersihkan", Toast.LENGTH_SHORT).show()
                    }) { Text("Hapus", color = Danger) }
                },
                dismissButton = {
                    TextButton(onClick = { dialog = null }) { Text("Batal", color = Text) }
                },
                title = { Text("Konfirmasi", color = Text) },
                text = { Text("Apakah kamu yakin ingin menghapus data lokal ini?", color = Muted) },
                containerColor = Bg2
            )
        }
    }
}

@Composable
private fun StatTile(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(color = Bg3, shape = RoundedCornerShape(18.dp), modifier = modifier) {
        Column(Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, color = Accent, fontWeight = FontWeight.Black, fontSize = 20.sp)
            Text(label, color = Muted, fontSize = 11.sp)
        }
    }
}

@Composable
private fun SettingRow(title: String, sub: String, danger: Boolean = false, onClick: () -> Unit) {
    Surface(color = Bg3, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable(onClick = onClick)) {
        Column(Modifier.padding(16.dp)) {
            Text(title, color = if (danger) Danger else Text, fontWeight = FontWeight.Bold)
            Text(sub, color = Muted, fontSize = 12.sp)
        }
    }
}

@Composable
private fun SettingSwitch(title: String, sub: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Surface(color = Bg3, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, color = Text, fontWeight = FontWeight.Bold)
                Text(sub, color = Muted, fontSize = 12.sp)
            }
            Switch(checked = checked, onCheckedChange = onChecked, colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = Accent))
        }
    }
}

private fun buildDramakuPlayer(context: Context): ExoPlayer {
    val httpFactory = DefaultHttpDataSource.Factory()
        .setUserAgent("Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 Chrome/121 Mobile Safari/537.36")
        .setAllowCrossProtocolRedirects(true)
    return ExoPlayer.Builder(context)
        .setMediaSourceFactory(DefaultMediaSourceFactory(httpFactory))
        .build()
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ClipFeedPlayer(
    items: List<Drama>,
    repo: DramakuRepository,
    store: LocalStore,
    onClose: () -> Unit,
    onWatchFull: (Detail) -> Unit,
    onOpenDetail: (Drama) -> Unit
) {
    if (items.isEmpty()) return
    val context = LocalContext.current
    val activity = context as? Activity
    val componentActivity = context as? ComponentActivity
    val pagerState = rememberPagerState(pageCount = { items.size })
    val player = remember { buildDramakuPlayer(context) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var currentDetail by remember { mutableStateOf<Detail?>(null) }
    var uiVisible by remember { mutableStateOf(true) }
    var retryKey by remember { mutableIntStateOf(0) }
    var playing by remember { mutableStateOf(false) }

    fun stopPreviewAudio() {
        runCatching {
            player.playWhenReady = false
            player.pause()
            player.stop()
            player.clearMediaItems()
        }
    }

    fun closeFeed() {
        stopPreviewAudio()
        onClose()
    }

    BackHandler { closeFeed() }

    DisposableEffect(Unit) {
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) { playing = isPlaying }
            override fun onPlayerError(errorValue: PlaybackException) {
                loading = false
                error = errorValue.message ?: "Video belum tersedia"
            }
        }
        player.addListener(listener)
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            player.removeListener(listener)
            stopPreviewAudio()
            player.release()
        }
    }

    DisposableEffect(componentActivity) {
        val lifecycle = componentActivity?.lifecycle
        if (lifecycle == null) {
            onDispose { }
        } else {
            val observer = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP -> runCatching { player.pause() }
                    else -> Unit
                }
            }
            lifecycle.addObserver(observer)
            onDispose { lifecycle.removeObserver(observer) }
        }
    }

    LaunchedEffect(uiVisible, loading, error, pagerState.currentPage) {
        if (uiVisible && !loading && error == null) {
            delay(2600)
            uiVisible = false
        }
    }

    LaunchedEffect(pagerState.currentPage, retryKey) {
        val drama = items.getOrNull(pagerState.currentPage) ?: return@LaunchedEffect
        uiVisible = true
        loading = true
        error = null
        currentDetail = null
        val detailResult = runCatching { repo.loadDetail(drama) }
        val detail = detailResult.getOrNull()
        if (detail == null) {
            loading = false
            error = detailResult.exceptionOrNull()?.message ?: "Detail tidak tersedia"
            player.stop()
            return@LaunchedEffect
        }
        currentDetail = detail
        val streamResult = runCatching { repo.resolveStream(detail, 1, store.dataSaver()) }
        val stream = streamResult.getOrNull()
        if (stream == null || stream.url.isBlank()) {
            loading = false
            error = streamResult.exceptionOrNull()?.message ?: "Cuplikan belum tersedia"
            player.stop()
            return@LaunchedEffect
        }
        player.setMediaItem(buildNativeMediaItem(stream))
        player.prepare()
        player.seekTo(0)
        player.playWhenReady = true
        loading = false
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(player, pagerState.currentPage) {
                detectTapGestures(
                    onTap = { uiVisible = !uiVisible },
                    onDoubleTap = {
                        uiVisible = true
                        if (player.isPlaying) player.pause() else player.play()
                    }
                )
            }
    ) {
        VerticalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            val drama = items[page]
            val display = if (page == pagerState.currentPage) currentDetail?.drama ?: drama else drama
            Box(Modifier.fillMaxSize().background(Color.Black)) {
                if (page == pagerState.currentPage) {
                    AndroidView(
                        factory = { ctx ->
                            PlayerView(ctx).apply {
                                useController = false
                                controllerAutoShow = false
                                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                                this.player = player
                            }
                        },
                        update = { it.player = player },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    AsyncImage(model = drama.poster, contentDescription = drama.title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                }
                Box(
                    Modifier.fillMaxSize().background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Transparent, Color(0x99000000)),
                            startY = 420f
                        )
                    )
                )
                Column(Modifier.align(Alignment.BottomStart).padding(12.dp, 12.dp, 76.dp, 18.dp)) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Poster(display.poster, display.title, Modifier.width(48.dp).height(70.dp))
                        Spacer(Modifier.width(9.dp))
                        Column(Modifier.weight(1f)) {
                            Text(display.title, color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp, lineHeight = 17.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Spacer(Modifier.height(2.dp))
                            Text("${platformLabel(display.platform)} · ${display.episodes.coerceAtLeast(1)} Ep", color = Color(0xDFFFFFFF), fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Surface(color = Color(0x88000000), shape = RoundedCornerShape(999.dp), modifier = Modifier.padding(top = 5.dp)) {
                                Text("Ep.1 | ${display.title}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp))
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        Button(
                            onClick = { currentDetail?.let { detail -> stopPreviewAudio(); onWatchFull(detail) } },
                            enabled = currentDetail != null,
                            colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = Color.Black),
                            shape = RoundedCornerShape(999.dp),
                            modifier = Modifier.height(36.dp)
                        ) { Text("Tonton", fontWeight = FontWeight.Black, fontSize = 11.sp) }
                        OutlinedButton(
                            onClick = { stopPreviewAudio(); onOpenDetail(display) },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            shape = RoundedCornerShape(999.dp),
                            modifier = Modifier.height(36.dp)
                        ) { Text("Detail", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                    }
                }
            }
        }

        AnimatedVisibility(uiVisible || loading || error != null, modifier = Modifier.align(Alignment.TopStart)) {
            Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { closeFeed() }, modifier = Modifier.clip(CircleShape).background(Color(0x99000000))) {
                    Icon(Icons.Rounded.ArrowBack, contentDescription = "Kembali", tint = Color.White, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(8.dp))
                Text("Cuplikan ${pagerState.currentPage + 1}/${items.size}", color = Color.White, fontWeight = FontWeight.Black)
            }
        }

        AnimatedVisibility(uiVisible || loading || error != null, modifier = Modifier.align(Alignment.CenterEnd)) {
            Column(Modifier.padding(end = 14.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
                PlayerSideButton(if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, if (playing) "Pause" else "Play") {
                    uiVisible = true
                    if (player.isPlaying) player.pause() else player.play()
                }
                PlayerSideButton(Icons.Rounded.Refresh, "Retry") {
                    uiVisible = true
                    retryKey++
                }
                PlayerSideButton(Icons.Rounded.Info, "Detail") {
                    uiVisible = true
                    stopPreviewAudio()
                    onOpenDetail(currentDetail?.drama ?: items[pagerState.currentPage])
                }
            }
        }

        if (loading) {
            Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = Accent)
                Spacer(Modifier.height(12.dp))
                Text("Memuat cuplikan...", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        if (error != null) {
            Surface(color = Color(0xDD101B27), shape = RoundedCornerShape(20.dp), modifier = Modifier.align(Alignment.Center).padding(24.dp)) {
                Column(Modifier.padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(error ?: "Cuplikan gagal dimuat", color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { retryKey++ }, colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = Color.Black)) {
                        Text("Coba Lagi", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun VerticalEpisodePlayer(
    detail: Detail,
    startEpisode: Int,
    repo: DramakuRepository,
    store: LocalStore,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val componentActivity = context as? ComponentActivity
    val scope = rememberCoroutineScope()
    val total = episodeCount(detail).coerceAtLeast(1)
    val pagerState = rememberPagerState(
        initialPage = (startEpisode - 1).coerceIn(0, total - 1),
        pageCount = { total }
    )
    val player = remember { buildDramakuPlayer(context) }

    var fitContain by remember { mutableStateOf(store.fitContain()) }
    var episodeSheet by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var currentMs by remember { mutableLongStateOf(0L) }
    var durationMs by remember { mutableLongStateOf(0L) }
    var uiVisible by remember { mutableStateOf(true) }
    var retryKey by remember { mutableIntStateOf(0) }
    var playing by remember { mutableStateOf(false) }

    val currentEp = pagerState.currentPage + 1

    fun closePlayer() {
        runCatching {
            player.playWhenReady = false
            player.stop()
            player.clearMediaItems()
        }
        onClose()
    }

    BackHandler {
        if (episodeSheet) episodeSheet = false
        else closePlayer()
    }

    DisposableEffect(Unit) {
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) { playing = isPlaying }
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY) {
                    loading = false
                    durationMs = max(0L, player.duration)
                }
                if (state == Player.STATE_ENDED) {
                    if (store.autoNext() && currentEp < total) {
                        scope.launch { pagerState.animateScrollToPage(currentEp) }
                    }
                }
            }
            override fun onPlayerError(errorValue: PlaybackException) {
                loading = false
                error = errorValue.message ?: "Streaming gagal diputar"
            }
        }
        player.addListener(listener)
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            player.removeListener(listener)
            store.updateProgress(detail.drama.id, detail.drama.platform, currentEp, player.currentPosition, player.duration)
            player.release()
        }
    }

    DisposableEffect(componentActivity) {
        val lifecycle = componentActivity?.lifecycle
        if (lifecycle == null) {
            onDispose { }
        } else {
            val observer = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP -> runCatching { player.pause() }
                    else -> Unit
                }
            }
            lifecycle.addObserver(observer)
            onDispose { lifecycle.removeObserver(observer) }
        }
    }

    LaunchedEffect(player) {
        while (true) {
            if (player.isPlaying) {
                currentMs = max(0L, player.currentPosition)
                durationMs = max(0L, player.duration)
                if (currentMs > 1000L && durationMs > 1000L) {
                    store.updateProgress(detail.drama.id, detail.drama.platform, currentEp, currentMs, durationMs)
                }
            }
            delay(500)
        }
    }

    LaunchedEffect(uiVisible, loading, error, pagerState.currentPage) {
        if (uiVisible && !loading && error == null) {
            delay(3000)
            uiVisible = false
        }
    }

    LaunchedEffect(pagerState.currentPage, retryKey) {
        uiVisible = true
        loading = true
        error = null
        val ep = pagerState.currentPage + 1
        store.saveHistory(detail.drama, ep)
        val streamResult = runCatching { repo.resolveStream(detail, ep, store.dataSaver()) }
        val stream = streamResult.getOrNull()
        if (stream == null || stream.url.isBlank()) {
            loading = false
            error = streamResult.exceptionOrNull()?.message ?: "Gagal mendapatkan URL stream episode $ep"
            player.stop()
            return@LaunchedEffect
        }
        player.setMediaItem(buildNativeMediaItem(stream))
        player.prepare()
        val savedMs = store.progressMs(detail.drama.id, detail.drama.platform, ep)
        if (savedMs > 2000L) player.seekTo(savedMs) else player.seekTo(0)
        player.playWhenReady = true
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(player, pagerState.currentPage) {
                detectTapGestures(
                    onTap = { uiVisible = !uiVisible },
                    onDoubleTap = {
                        uiVisible = true
                        if (player.isPlaying) player.pause() else player.play()
                    }
                )
            }
    ) {
        VerticalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            val ep = page + 1
            Box(Modifier.fillMaxSize().background(Color.Black)) {
                if (page == pagerState.currentPage) {
                    AndroidView(
                        factory = { ctx ->
                            PlayerView(ctx).apply {
                                useController = false
                                controllerAutoShow = false
                                resizeMode = if (fitContain) AspectRatioFrameLayout.RESIZE_MODE_FIT else AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                                this.player = player
                            }
                        },
                        update = {
                            it.resizeMode = if (fitContain) AspectRatioFrameLayout.RESIZE_MODE_FIT else AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                            it.player = player
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    AsyncImage(model = detail.drama.poster, contentDescription = detail.drama.title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                }
                Box(
                    Modifier.fillMaxSize().background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Transparent, Color(0x99000000)),
                            startY = 400f
                        )
                    )
                )
                Column(Modifier.align(Alignment.BottomStart).padding(14.dp, 14.dp, 76.dp, 28.dp)) {
                    Text(detail.drama.title, color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp, lineHeight = 20.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(4.dp))
                    Text("${platformLabel(detail.drama.platform)} · Ep $ep dari $total", color = Color(0xEFFFFFFF), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        AnimatedVisibility(uiVisible || loading || error != null, modifier = Modifier.align(Alignment.TopStart)) {
            Row(
                Modifier.fillMaxWidth().padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { closePlayer() },
                    modifier = Modifier.clip(CircleShape).background(Color(0x99000000))
                ) { Icon(Icons.Rounded.ArrowBack, contentDescription = "Kembali", tint = Color.White, modifier = Modifier.size(22.dp)) }
                Spacer(Modifier.width(8.dp))
                Text(detail.drama.title, color = Color.White, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
            }
        }

        AnimatedVisibility(uiVisible || loading || error != null, modifier = Modifier.align(Alignment.CenterEnd)) {
            Column(
                Modifier.padding(end = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                PlayerSideButton(if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, if (playing) "Pause" else "Play") {
                    uiVisible = true
                    if (player.isPlaying) player.pause() else player.play()
                }
                PlayerSideButton(Icons.Rounded.List, "Episode") {
                    uiVisible = true
                    episodeSheet = true
                }
                PlayerSideButton(if (fitContain) Icons.Rounded.AspectRatio else Icons.Rounded.Fullscreen, if (fitContain) "Asli" else "Full") {
                    uiVisible = true
                    fitContain = !fitContain
                    store.setFitContain(fitContain)
                }
                PlayerSideButton(Icons.Rounded.Refresh, "Retry") {
                    uiVisible = true
                    retryKey++
                }
                PlayerSideButton(Icons.Rounded.Flag, "Lapor") {
                    uiVisible = true
                    shareEpisodeReport(context, detail.drama, pagerState.currentPage + 1, error)
                }
            }
        }

        AnimatedVisibility(uiVisible || loading || error != null, modifier = Modifier.align(Alignment.BottomCenter)) {
            Column(Modifier.fillMaxWidth().padding(start = 18.dp, end = 18.dp, bottom = 8.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(formatMs(currentMs), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.weight(1f))
                    Text(formatMs(durationMs), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = if (durationMs > 0L) (currentMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f,
                    onValueChange = { frac ->
                        uiVisible = true
                        if (durationMs > 0L) {
                            val target = (frac * durationMs).toLong()
                            currentMs = target
                            player.seekTo(target)
                        }
                    },
                    colors = SliderDefaults.colors(thumbColor = Accent, activeTrackColor = Accent, inactiveTrackColor = Color(0x66FFFFFF))
                )
            }
        }

        if (loading) {
            Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = Accent)
                Spacer(Modifier.height(12.dp))
                Text("Memuat episode $currentEp...", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        if (error != null) {
            Surface(color = Color(0xDD101B27), shape = RoundedCornerShape(20.dp), modifier = Modifier.align(Alignment.Center).padding(24.dp)) {
                Column(Modifier.padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(error ?: "Video gagal dimuat", color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { retryKey++ }, colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = Color.Black)) {
                            Text("Coba Lagi", fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = { shareEpisodeReport(context, detail.drama, currentEp, error) },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) { Text("Lapor", fontWeight = FontWeight.Bold) }
                    }
                }
            }
        }

        if (episodeSheet) {
            ModalBottomSheet(
                onDismissRequest = { episodeSheet = false },
                containerColor = Bg2
            ) {
                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                    Text("Pilih Episode (${detail.drama.title})", color = Text, fontWeight = FontWeight.Black, fontSize = 18.sp)
                    Spacer(Modifier.height(12.dp))
                    LazyVerticalGrid(columns = GridCells.Fixed(5), verticalArrangement = Arrangement.spacedBy(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.heightIn(max = 340.dp)) {
                        items(total) { idx ->
                            val ep = idx + 1
                            val isCurrent = ep == currentEp
                            Surface(
                                color = if (isCurrent) Accent else Bg3,
                                contentColor = if (isCurrent) Color.Black else Text,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.height(44.dp).clickable {
                                    episodeSheet = false
                                    scope.launch { pagerState.scrollToPage(idx) }
                                }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(ep.toString(), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayerSideButton(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(color = Color(0x99000000), shape = CircleShape, modifier = Modifier.size(50.dp).clickable(onClick = onClick)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(24.dp))
            }
        }
        Text(label, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

private fun buildNativeMediaItem(stream: StreamResult): MediaItem {
    val lowerUrl = stream.url.lowercase()
    val builder = MediaItem.Builder().setUri(Uri.parse(stream.url))
    if (lowerUrl.contains(".m3u8") || lowerUrl.contains("hls")) {
        builder.setMimeType(MimeTypes.APPLICATION_M3U8)
    }
    if (stream.subtitle.isNotBlank()) {
        val sub = MediaItem.SubtitleConfiguration.Builder(Uri.parse(stream.subtitle))
            .setMimeType(if (stream.subtitle.endsWith(".vtt", true)) MimeTypes.TEXT_VTT else MimeTypes.APPLICATION_SUBRIP)
            .setLanguage("id")
            .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
            .build()
        builder.setSubtitleConfigurations(listOf(sub))
    }
    return builder.build()
}

@Composable
private fun DetailInfoTile(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(color = Bg3, shape = RoundedCornerShape(18.dp), modifier = modifier) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value.ifBlank { "-" }, color = Text, fontWeight = FontWeight.Black, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(label, color = Muted, fontSize = 10.sp)
        }
    }
}

@Composable
private fun DetailScreen(
    state: Load<Detail>,
    fallback: Drama,
    store: LocalStore,
    resolvingEpisode: Int,
    onClose: () -> Unit,
    onPlay: (Detail, Int) -> Unit,
    onFavChanged: () -> Unit,
    onShare: (Drama) -> Unit
) {
    val detail = (state as? Load.Ok)?.data ?: Detail(fallback)
    val drama = detail.drama
    val isFav = store.isFav(drama.id, drama.platform)
    val hist = store.history().firstOrNull { it.id == drama.id && it.platform == drama.platform }
    val resumeEp = hist?.episode?.coerceAtLeast(1) ?: 1
    val totalEpisodes = episodeCount(detail).coerceAtLeast(1)
    var detailRange by remember(drama.id, totalEpisodes) { mutableIntStateOf(((resumeEp - 1) / 30).coerceAtLeast(0)) }
    var descExpanded by remember(drama.id) { mutableStateOf(false) }
    Box(Modifier.fillMaxSize().background(Bg)) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 22.dp)) {
            item {
                Box(Modifier.fillMaxWidth().height(285.dp)) {
                    AsyncImage(model = drama.poster, contentDescription = drama.title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Bg), startY = 80f)))
                    IconButton(onClick = onClose, modifier = Modifier.padding(12.dp).clip(CircleShape).background(Color(0x99000000))) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Kembali", tint = Text, modifier = Modifier.size(22.dp))
                    }
                }
                Row(Modifier.padding(horizontal = 16.dp).offset(y = (-60).dp), verticalAlignment = Alignment.Bottom) {
                    Poster(drama.poster, drama.title, Modifier.width(120.dp).height(172.dp))
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f).padding(bottom = 8.dp)) {
                        Text(drama.title, color = Text, fontSize = 25.sp, lineHeight = 28.sp, fontWeight = FontWeight.Black, maxLines = 3, overflow = TextOverflow.Ellipsis)
                        Text("${platformLabel(drama.platform)} · $totalEpisodes Episode", color = Muted, fontSize = 12.sp)
                    }
                }
            }
            item {
                Column(Modifier.padding(horizontal = 16.dp).offset(y = (-44).dp)) {
                    when (state) {
                        Load.Loading -> LinearProgressIndicator(color = Accent, trackColor = Bg3, modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp))
                        is Load.Err -> Text((state as Load.Err).message, color = Danger, modifier = Modifier.padding(bottom = 12.dp))
                        else -> {}
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { onPlay(detail, resumeEp) },
                            enabled = state is Load.Ok && resolvingEpisode == 0,
                            colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = Color.Black),
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Rounded.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(if (resolvingEpisode == resumeEp) "Memuat..." else if (hist != null) "Lanjut Ep $resumeEp" else "Mulai Tonton", fontWeight = FontWeight.Black)
                        }
                        IconButton(onClick = { store.toggleFav(drama); onFavChanged() }, modifier = Modifier.size(50.dp).clip(RoundedCornerShape(16.dp)).background(if (isFav) Accent else Bg3)) {
                            Icon(if (isFav) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder, contentDescription = "Favorit", tint = if (isFav) Color.Black else Text, modifier = Modifier.size(22.dp))
                        }
                        IconButton(onClick = { onShare(drama) }, modifier = Modifier.size(50.dp).clip(RoundedCornerShape(16.dp)).background(Bg3)) {
                            Icon(Icons.Rounded.Share, contentDescription = "Bagikan", tint = Text, modifier = Modifier.size(22.dp))
                        }
                    }
                    if (hist != null) {
                        Surface(color = Bg3, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth().padding(top = 12.dp).clickable { onPlay(detail, resumeEp) }) {
                            Column(Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Lanjutkan tontonan", color = Text, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                    Text("Ep $resumeEp${if (hist.pct > 0) " · ${hist.pct}%" else ""}", color = Accent, fontWeight = FontWeight.Black, fontSize = 12.sp)
                                }
                                if (hist.pct > 0) LinearProgressIndicator(progress = hist.pct / 100f, color = Accent, trackColor = Color(0x33000000), modifier = Modifier.fillMaxWidth().padding(top = 8.dp).height(4.dp))
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    if (drama.tags.isNotEmpty()) LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { items(drama.tags.take(8)) { Pill(it) {} } }
                    Spacer(Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        DetailInfoTile("Platform", platformLabel(drama.platform), Modifier.weight(1f))
                        DetailInfoTile("Episode", "$totalEpisodes Ep", Modifier.weight(1f))
                        DetailInfoTile("Tipe", if (totalEpisodes > 1) "Serial" else "Film", Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(18.dp))
                    Text("Sinopsis", color = Text, fontWeight = FontWeight.Black, fontSize = 18.sp)
                    val fullDesc = drama.description.ifBlank { "Belum ada sinopsis untuk judul ini." }
                    Text(
                        if (descExpanded || fullDesc.length <= 180) fullDesc else fullDesc.take(180) + "...",
                        color = Muted,
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                    if (fullDesc.length > 180) TextButton(onClick = { descExpanded = !descExpanded }) { Text(if (descExpanded) "Sembunyikan" else "Selengkapnya", color = Accent) }
                    Spacer(Modifier.height(20.dp))
                    Text("Daftar Episode", color = Text, fontWeight = FontWeight.Black, fontSize = 18.sp)
                    Spacer(Modifier.height(10.dp))
                    val rangeSize = 30
                    val rangeCount = ((totalEpisodes + rangeSize - 1) / rangeSize).coerceAtLeast(1)
                    if (rangeCount > 1) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(bottom = 10.dp)) {
                            items((0 until rangeCount).toList()) { r ->
                                val st = r * rangeSize + 1
                                val en = min(totalEpisodes, (r + 1) * rangeSize)
                                Pill("$st-$en", detailRange == r) { detailRange = r }
                            }
                        }
                    }
                    val startEp = detailRange * rangeSize + 1
                    val endEp = min(totalEpisodes, startEp + rangeSize - 1)
                    val rows = (startEp..endEp).chunked(5)
                    rows.forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                            row.forEach { ep ->
                                Surface(
                                    color = if (resolvingEpisode == ep) Accent else Bg3,
                                    contentColor = if (resolvingEpisode == ep) Color.Black else Text,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f).height(44.dp).clickable(enabled = resolvingEpisode == 0 && state is Load.Ok) { onPlay(detail, ep) }
                                ) { Box(contentAlignment = Alignment.Center) { Text(if (resolvingEpisode == ep) "..." else ep.toString(), fontWeight = FontWeight.Bold) } }
                            }
                            repeat(5 - row.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }
                }
            }
        }
    }
}

private fun episodeCount(detail: Detail): Int = max(detail.drama.episodes, detail.episodes.size)

private fun formatMs(ms: Long): String {
    val totalSec = (ms / 1000L).coerceAtLeast(0L)
    val h = totalSec / 3600L
    val m = (totalSec % 3600L) / 60L
    val s = totalSec % 60L
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%d:%02d".format(m, s)
}

private fun shareDrama(context: Context, drama: Drama) {
    val text = "${drama.title}\nPlatform: ${platformLabel(drama.platform)}\nDramaku"
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(send, "Bagikan drama"))
}

private fun shareEpisodeReport(context: Context, drama: Drama, episode: Int, error: String?) {
    val text = "Laporan Masalah Stream Dramaku\nJudul: ${drama.title}\nPlatform: ${platformLabel(drama.platform)}\nEpisode: $episode\nError: ${error ?: "Sinyal/Video tidak merespon"}"
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(send, "Lapor Episode Bermasalah"))
}

private class DramakuRepository {
    private val client = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .build()

    fun loadHome(platformId: String): HomeBundle {
        val base = apiBase(platformId)
        val homeUrl = when (platformId) {
            "moviebox" -> "$base/homepage?tabId=0"
            "dramabox" -> "$base/home?lang=in"
            else -> "$base/home"
        }
        
        val list = runCatching { fetchCatalogList(homeUrl, platformId) }.getOrDefault(emptyList())
        if (list.isEmpty()) {
            val fallbackList = runCatching { fetchCatalogList("$base/search?q=a", platformId) }.getOrDefault(emptyList())
            return HomeBundle(
                popular = fallbackList.take(20),
                newest = fallbackList.drop(20).take(20),
                recommended = fallbackList.drop(40).take(20)
            )
        }
        
        return HomeBundle(
            popular = list.take(20),
            newest = list.drop(20).take(20),
            recommended = list.drop(40).take(20)
        )
    }

    private fun fetchCatalogList(url: String, platformId: String): List<Drama> {
        val req = Request.Builder().url(url).build()
        client.newCall(req).execute().use { res ->
            if (!res.isSuccessful) return emptyList()
            val raw = res.body?.string().orEmpty()
            val root = JSONObject(raw).dataOrSelf()
            return parseDramaListFromRoot(root, platformId)
        }
    }

    private fun parseDramaListFromRoot(root: Any, platformId: String): List<Drama> {
        val dramas = mutableListOf<Drama>()
        
        fun parseObj(o: JSONObject) {
            val id = o.stringAny("drama_id", "id", "book_id", "bookId", "subjectId", "subject_id")
            val title = cleanText(o.stringAny("drama_name", "title", "book_name", "bookName", "name"))
            if (id.isNotBlank() && title.isNotBlank()) {
                val desc = cleanText(o.stringAny("description", "summary", "intro", "introduction"))
                val poster = fixImg(o.coverUrl().ifBlank { o.stringAny("thumb_url", "cover", "poster", "image", "bookCover", "bookDetailCover") })
                val episodes = o.intAny("episode_count", "episodes", "total_episodes", "chapterCount", "totalEpisode")
                val views = o.stringAny("watch_value", "viewCount", "views", "play_count")
                val tags = o.optJSONArray("tags")?.let { arr -> (0 until arr.length()).mapNotNull { arr.optString(it).takeIf { s -> s.isNotBlank() } } }.orEmpty()
                dramas.add(Drama(id, title, desc, poster, episodes, views, tags, platformId))
            }
        }

        fun scanArray(arr: JSONArray) {
            for (i in 0 until arr.length()) {
                val item = arr.opt(i)
                if (item is JSONObject) {
                    val books = item.optJSONArray("books")
                    val subjects = item.optJSONArray("subjects")
                    val groups = item.optJSONArray("groups")
                    val results = item.optJSONArray("results")
                    val list = item.optJSONArray("list")
                    val items = item.optJSONArray("items")
                    
                    when {
                        books != null -> scanArray(books)
                        subjects != null -> scanArray(subjects)
                        results != null -> scanArray(results)
                        list != null -> scanArray(list)
                        items != null -> scanArray(items)
                        groups != null -> {
                            for (g in 0 until groups.length()) {
                                groups.optJSONObject(g)?.optJSONArray("subjects")?.let { scanArray(it) }
                            }
                        }
                        else -> parseObj(item)
                    }
                }
            }
        }

        when (root) {
            is JSONArray -> scanArray(root)
            is JSONObject -> {
                val itemsArr = root.optJSONArray("items")
                val dataArr = root.optJSONArray("data")
                val listArr = root.optJSONArray("list")
                val resultsArr = root.optJSONArray("results")
                val booksArr = root.optJSONArray("books")
                when {
                    itemsArr != null -> scanArray(itemsArr)
                    dataArr != null -> scanArray(dataArr)
                    listArr != null -> scanArray(listArr)
                    resultsArr != null -> scanArray(resultsArr)
                    booksArr != null -> scanArray(booksArr)
                    else -> parseObj(root)
                }
            }
        }
        return dramas.distinctBy { it.id }
    }

    fun loadDetail(drama: Drama): Detail {
        val base = apiBase(drama.platform)
        val detailUrl = when (drama.platform) {
            "moviebox" -> "$base/detail?subjectId=${enc(drama.id)}"
            "goodshort", "dramabox" -> "$base/detail?bookId=${enc(drama.id)}"
            else -> "$base/detail?id=${enc(drama.id)}"
        }
        
        val req = Request.Builder().url(detailUrl).build()
        client.newCall(req).execute().use { res ->
            if (!res.isSuccessful) return Detail(drama)
            val raw = res.body?.string().orEmpty()
            val rootObj = JSONObject(raw)
            val json = (rootObj.dataOrSelf() as? JSONObject) ?: (rootObj.optJSONObject("data") ?: rootObj)
            val bookObj = json.optJSONObject("book") ?: json
            
            val desc = cleanText(bookObj.stringAny("description", "summary", "intro", "introduction"))
            val poster = fixImg(bookObj.coverUrl().ifBlank { bookObj.stringAny("thumb_url", "cover", "poster", "image", "bookCover", "bookDetailCover") }).ifBlank { drama.poster }
            val epCount = bookObj.intAny("episode_count", "episodes", "total_episodes", "chapterCount", "totalEpisode")
            
            val epList = mutableListOf<EpisodeInfo>()
            val rawEps = json.optJSONArray("video_list") ?: json.optJSONArray("episodes") ?: json.optJSONArray("chapterList") ?: json.optJSONArray("episodeList") ?: JSONArray()
            for (i in 0 until rawEps.length()) {
                rawEps.optJSONObject(i)?.let { o ->
                    val epNum = o.intAny("episode", "number", "ep", "episodeNo", i + 1)
                    val url = o.stringAny("video_url", "playVoucher", "resourceLink", "url", "stream", "streaming")
                    epList.add(EpisodeInfo(epNum, url))
                }
            }
            val updatedDrama = drama.copy(description = desc.ifBlank { drama.description }, poster = poster, episodes = max(epCount, epList.size))
            return Detail(updatedDrama, epList)
        }
    }

    fun resolveStream(detail: Detail, episodeNumber: Int, dataSaver: Boolean = false): StreamResult {
        val drama = detail.drama
        val epInfo = detail.episodes.firstOrNull { it.number == episodeNumber }
        if (epInfo != null && epInfo.streaming.isNotBlank()) {
            return StreamResult(epInfo.streaming)
        }
        
        val base = apiBase(drama.platform)
        val urlsToTry = mutableListOf<String>()
        
        when (drama.platform) {
            "moviebox" -> {
                val res = if (dataSaver) "480" else "720"
                urlsToTry.add("$base/download-series?subjectId=${enc(drama.id)}&se=$episodeNumber&resolution=$res")
                urlsToTry.add("$base/stream?subjectId=${enc(drama.id)}&episode=$episodeNumber")
            }
            "freereels" -> {
                urlsToTry.add("$base/stream?id=${enc(drama.id)}&episode=$episodeNumber")
                urlsToTry.add("$base/stream?dramaId=${enc(drama.id)}&episode=$episodeNumber")
            }
            "goodshort" -> {
                urlsToTry.add("$base/stream?bookId=${enc(drama.id)}&episode=$episodeNumber")
                urlsToTry.add("$base/stream?id=${enc(drama.id)}&ep=$episodeNumber")
            }
            "dramabox" -> {
                val chapterIndex = (episodeNumber - 1).coerceAtLeast(0)
                urlsToTry.add("$base/stream?bookId=${enc(drama.id)}&chapterIndex=$chapterIndex&lang=in")
                urlsToTry.add("$base/stream?id=${enc(drama.id)}&ep=$episodeNumber")
            }
            "netshort" -> {
                urlsToTry.add("$base/stream?id=${enc(drama.id)}&episode=$episodeNumber")
                urlsToTry.add("$base/stream?id=${enc(drama.id)}&episode_no=$episodeNumber")
                urlsToTry.add("$base/stream?id=${enc(drama.id)}&ep=$episodeNumber")
            }
            else -> {
                urlsToTry.add("$base/stream?id=${enc(drama.id)}&ep=$episodeNumber")
                urlsToTry.add("$base/stream?id=${enc(drama.id)}&episode=$episodeNumber")
            }
        }

        var lastError = "Gagal memuat stream"
        for (u in urlsToTry) {
            runCatching {
                val req = Request.Builder().url(u).build()
                client.newCall(req).execute().use { res ->
                    if (!res.isSuccessful) return@use
                    val body = res.body?.string().orEmpty()
                    val root = JSONObject(body)
                    val json = (root.dataOrSelf() as? JSONObject) ?: root.optJSONObject("data") ?: root
                    
                    val parsedStream = extractStreamUrlAndSub(json, episodeNumber)
                    if (parsedStream.url.isNotBlank()) {
                        return parsedStream
                    }
                }
            }.onFailure { lastError = it.message ?: lastError }
        }
        error(lastError)
    }

    private fun extractStreamUrlAndSub(json: JSONObject, episodeNumber: Int): StreamResult {
        var streamUrl = ""
        var subUrl = subtitleFrom(json.optJSONArray("subtitles") ?: json.optJSONArray("subs"))
        
        val qualities = json.optJSONArray("qualities")
        if (qualities != null && qualities.length() > 0) {
            val qObj = qualities.optJSONObject(0)
            if (qObj != null) {
                streamUrl = qObj.stringAny("url", "videoPath", "backup_url")
            }
        }
        
        if (streamUrl.isBlank()) {
            val videoList = json.optJSONArray("videoList")
            if (videoList != null && videoList.length() > 0) {
                val vObj = videoList.optJSONObject(0)
                if (vObj != null) streamUrl = vObj.stringAny("playUrl", "url")
            }
        }
        
        if (streamUrl.isBlank()) {
            val eps = json.optJSONArray("episodeList") ?: json.optJSONArray("episodes")
            if (eps != null && eps.length() > 0) {
                for (i in 0 until eps.length()) {
                    val epObj = eps.optJSONObject(i) ?: continue
                    val epNum = epObj.intAny("episode", "episodeNo", "ep", "se", i + 1)
                    if (epNum == episodeNumber || eps.length() == 1) {
                        streamUrl = epObj.stringAny("playVoucher", "resourceLink", "url", "video_url")
                        if (subUrl.isBlank()) {
                            val subObj = epObj.optJSONObject("subtitle")
                            if (subObj != null) subUrl = subObj.stringAny("url")
                        }
                        if (streamUrl.isNotBlank()) break
                    }
                }
                if (streamUrl.isBlank()) {
                    val firstEp = eps.optJSONObject(0)
                    if (firstEp != null) streamUrl = firstEp.stringAny("playVoucher", "resourceLink", "url", "video_url")
                }
            }
        }
        
        if (streamUrl.isBlank()) {
            streamUrl = json.stringAny(
                "video_url", "videoUrl", "m3u8_url", "h264_m3u8", "hls_url",
                "playUrl", "playVoucher", "resourceLink", "url", "src"
            )
        }
        
        return StreamResult(streamUrl, subUrl)
    }

    suspend fun searchAll(query: String): List<Drama> = coroutineScope {
        val allowedPlatforms = Platforms.map { it.id }
        val deferreds = allowedPlatforms.map { p ->
            async(Dispatchers.IO) {
                runCatching {
                    val base = apiBase(p)
                    val req = Request.Builder().url("$base/search?q=${enc(query)}").build()
                    client.newCall(req).execute().use { res ->
                        if (!res.isSuccessful) return@runCatching emptyList<Drama>()
                        val json = JSONObject(res.body?.string().orEmpty()).dataOrSelf()
                        parseDramaListFromRoot(json, p)
                    }
                }.getOrDefault(emptyList())
            }
        }
        val results = deferreds.awaitAll().flatten()
        val q = normalizeKey(query)
        results.filter { it.id.isNotBlank() && it.title.isNotBlank() }
            .sortedByDescending { d ->
                var score = 0
                val t = normalizeKey(d.title)
                if (t == q) score += 100
                else if (t.startsWith(q)) score += 70
                else if (t.contains(q)) score += 45
                q.split(" ").filter { it.isNotBlank() }.forEach { if (t.contains(it)) score += 8 }
                if (d.poster.isNotBlank()) score += 3
                if (d.episodes > 0) score += 2
                if (d.platform in setOf("moviebox", "drakor", "melolo", "dramabox")) score += 2
                score
            }
    }
}

private class LocalStore(context: Context) {
    private val prefs = context.getSharedPreferences("dramaku_native", Context.MODE_PRIVATE)
    fun platform() = prefs.getString("platform", "melolo") ?: "melolo"
    fun setPlatform(id: String) = prefs.edit().putString("platform", id).apply()
    fun dataSaver() = prefs.getBoolean("dataSaver", false)
    fun setDataSaver(v: Boolean) = prefs.edit().putBoolean("dataSaver", v).apply()
    fun autoNext() = prefs.getBoolean("autoNext", true)
    fun setAutoNext(v: Boolean) = prefs.edit().putBoolean("autoNext", v).apply()
    fun fitContain() = prefs.getBoolean("fitContain", false)
    fun setFitContain(v: Boolean) = prefs.edit().putBoolean("fitContain", v).apply()

    fun history(tick: Int = 0): List<HistoryItem> = parseHistory()
    fun saveHistory(drama: Drama, ep: Int) {
        val arr = JSONArray()
        val current = parseHistory()
        val prev = current.firstOrNull { it.id == drama.id && it.platform == drama.platform }
        val old = current.filterNot { it.id == drama.id && it.platform == drama.platform }.toMutableList()
        val keepProgress = prev != null && prev.episode == ep
        old.add(0, HistoryItem(
            drama.id,
            drama.title.ifBlank { prev?.title.orEmpty() },
            drama.poster.ifBlank { prev?.poster.orEmpty() },
            drama.platform,
            ep,
            pos = if (keepProgress) prev?.pos ?: 0L else 0L,
            dur = if (keepProgress) prev?.dur ?: 0L else 0L
        ))
        old.take(80).forEach { arr.put(it.toJson()) }
        prefs.edit().putString("history", arr.toString()).apply()
    }
    fun updateProgress(id: String, platform: String, ep: Int, pos: Long, dur: Long) {
        val p = ProgressKeys.episodePrefix(platform, id, ep)
        val safePos = pos.coerceAtLeast(0L)
        val safeDur = dur.coerceAtLeast(0L)
        val editor = prefs.edit()
            .putLong(p + "pos", safePos)
            .putLong(p + "dur", safeDur)
        val list = parseHistory().toMutableList()
        val idx = list.indexOfFirst { it.id == id && it.platform == platform }
        if (idx >= 0) {
            val h = list[idx]
            list[idx] = h.copy(episode = ep, pos = safePos, dur = safeDur, updated = System.currentTimeMillis())
            val arr = JSONArray(); list.sortedByDescending { it.updated }.forEach { arr.put(it.toJson()) }
            editor.putString("history", arr.toString())
        }
        editor.apply()
    }
    fun progressMs(id: String, platform: String, ep: Int): Long {
        val p = ProgressKeys.episodePrefix(platform, id, ep)
        val saved = prefs.getLong(p + "pos", -1L)
        if (saved >= 0L) return saved

        val history = parseHistory().firstOrNull { it.id == id && it.platform == platform && it.episode == ep }
        val legacyPrefix = ProgressKeys.legacyEpisodePrefix(id, ep)
        val legacyPos = prefs.getLong(legacyPrefix + "pos", -1L)
        if (legacyPos >= 0L && history != null) {
            prefs.edit()
                .putLong(p + "pos", legacyPos)
                .putLong(p + "dur", prefs.getLong(legacyPrefix + "dur", 0L).coerceAtLeast(0L))
                .apply()
            return legacyPos
        }
        return history?.pos ?: 0L
    }
    fun clearHistory() {
        val editor = prefs.edit().remove("history")
        prefs.all.keys.filter { it.startsWith("progress_") }.forEach { editor.remove(it) }
        editor.apply()
    }

    fun favs(): List<Drama> = parseDramaList(prefs.getString("favs", "[]"))
    fun isFav(id: String, platform: String) = favs().any { it.id == id && it.platform == platform }
    fun toggleFav(d: Drama) {
        val list = favs().toMutableList()
        val idx = list.indexOfFirst { it.id == d.id && it.platform == d.platform }
        if (idx >= 0) list.removeAt(idx) else list.add(0, d)
        val arr = JSONArray(); list.take(120).forEach { arr.put(it.toJson()) }
        prefs.edit().putString("favs", arr.toString()).apply()
    }
    fun clearFavs() = prefs.edit().remove("favs").apply()

    fun removeFav(id: String, platform: String) {
        val arr = JSONArray()
        favs().filterNot { it.id == id && it.platform == platform }.forEach { arr.put(it.toJson()) }
        prefs.edit().putString("favs", arr.toString()).apply()
    }

    fun removeHistory(id: String, platform: String) {
        val remaining = parseHistory().filterNot { it.id == id && it.platform == platform }
        val arr = JSONArray()
        remaining.forEach { arr.put(it.toJson()) }
        val editor = prefs.edit().putString("history", arr.toString())
        val progressPrefix = ProgressKeys.dramaPrefix(platform, id)
        prefs.all.keys.filter { it.startsWith(progressPrefix) }.forEach { editor.remove(it) }

        if (remaining.none { it.id == id }) {
            prefs.all.keys.filter { it.startsWith("progress_${id}_") }.forEach { editor.remove(it) }
        }
        editor.apply()
    }

    fun recentSearches(): List<String> = JSONArray(prefs.getString("recent", "[]") ?: "[]").let { arr -> (0 until arr.length()).mapNotNull { arr.optString(it).takeIf { s -> s.isNotBlank() } } }
    fun saveRecent(q: String) {
        val list = recentSearches().filterNot { it.equals(q, true) }.toMutableList()
        list.add(0, q)
        val arr = JSONArray(); list.take(10).forEach { arr.put(it) }
        prefs.edit().putString("recent", arr.toString()).apply()
    }
    fun clearRecentSearches() = prefs.edit().remove("recent").apply()

    private fun parseHistory(): List<HistoryItem> = runCatching {
        val arr = JSONArray(prefs.getString("history", "[]") ?: "[]")
        (0 until arr.length()).mapNotNull { i ->
            arr.optJSONObject(i)?.let { o ->
                HistoryItem(o.stringAny("id"), o.stringAny("title"), o.stringAny("poster"), o.stringAny("platform"), o.intAny("episode", 1), o.optLong("pos", 0), o.optLong("dur", 0), o.optLong("updated", 0))
            }
        }.sortedByDescending { it.updated }
    }.getOrDefault(emptyList())

    private fun parseDramaList(raw: String?): List<Drama> = runCatching {
        val arr = JSONArray(raw ?: "[]")
        (0 until arr.length()).mapNotNull { i -> arr.optJSONObject(i)?.toDrama() }
    }.getOrDefault(emptyList())
}

private fun HistoryItem.toJson() = JSONObject().apply {
    put("id", id); put("title", title); put("poster", poster); put("platform", platform); put("episode", episode); put("pos", pos); put("dur", dur); put("updated", updated)
}
private fun Drama.toJson() = JSONObject().apply {
    put("id", id); put("title", title); put("description", description); put("poster", poster); put("episodes", episodes); put("views", views); put("platform", platform); put("subjectType", subjectType)
    put("tags", JSONArray(tags))
}
private fun JSONObject.toDrama() = Drama(
    id = stringAny("id"), title = stringAny("title"), description = stringAny("description"), poster = stringAny("poster"), episodes = intAny("episodes", 0), views = stringAny("views"),
    tags = optJSONArray("tags")?.let { arr -> (0 until arr.length()).mapNotNull { arr.optString(it).takeIf { s -> s.isNotBlank() } } }.orEmpty(), platform = stringAny("platform").ifBlank { "melolo" }, subjectType = intAny("subjectType", 1)
)

private fun JSONObject.dataOrSelf(): Any = opt("data")?.takeUnless { it == JSONObject.NULL } ?: this
private fun JSONArray.objects(): List<JSONObject> = (0 until length()).mapNotNull { optJSONObject(it) }
private fun JSONObject.stringAny(vararg keys: String): String {
    keys.forEach { k ->
        val v = opt(k)
        if (v != null && v != JSONObject.NULL) {
            if (v is String && v.isNotBlank()) return v.trim()
            if (v !is JSONObject && v !is JSONArray && v.toString().isNotBlank()) return v.toString().trim()
        }
    }
    return ""
}
private fun JSONObject.intAny(vararg keys: Any): Int {
    var fallback = 0
    keys.forEach { k ->
        if (k is Int) fallback = k
        else if (k is String && has(k)) {
            val v = opt(k)
            val n = when (v) { is Number -> v.toInt(); is String -> v.filter { it.isDigit() }.toIntOrNull() ?: 0; else -> 0 }
            if (n != 0) return n
        }
    }
    return fallback
}
private fun JSONObject.coverUrl(): String {
    val c = opt("cover")
    return if (c is JSONObject) c.stringAny("url") else ""
}
private fun subtitleFrom(arr: JSONArray?): String {
    val list = arr?.objects().orEmpty()
    return (list.firstOrNull { it.stringAny("language", "lang").startsWith("id", true) } ?: list.firstOrNull())?.stringAny("url", "label").orEmpty()
}
private fun fixImg(u: String): String {
    if (u.contains("fizzopic.org") && u.contains(".heic")) {
        val m = Regex("novel-images-apsoutheast/([a-f0-9]+)~").find(u)
        if (m != null) return "https://p19-novel-sg.ibyteimg.com/img/novel-images-sg/${m.groupValues[1]}~tplv-resize:570:810.jpg"
    }
    return u
}
private fun cleanText(s: String): String = s.replace(Regex("<[^>]+>"), " ").replace("&nbsp;", " ").replace(Regex("\\s+"), " ").trim()
private fun enc(s: String): String = URLEncoder.encode(s, "UTF-8")
private fun normalizeKey(s: String) = s.lowercase().replace(Regex("[^a-z0-9\\p{L}\\s]"), " ").replace(Regex("\\s+"), " ").trim()
