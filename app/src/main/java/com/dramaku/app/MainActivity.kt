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
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView
import androidx.media3.ui.AspectRatioFrameLayout
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.ConcurrentHashMap
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

private enum class RootTab(val title: String, val icon: String, val showInNav: Boolean = true) {
    Clips("Cuplikan", "▶"),
    Home("Temukan", "◆"),
    Rewards("Hadiah", "◇"),
    Library("Daftar Saya", "▣"),
    Settings("Profil", "●"),
    Search("Cari", "⌕", false)
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
private data class HomeBundle(
    val recommended: List<Drama>,
    val popular: List<Drama>,
    val newest: List<Drama>,
    val loadedPage: Int = 1,
    val hasMore: Boolean = true
)
private data class StreamResult(val url: String, val subtitle: String = "")
private data class CachedStream(val result: StreamResult, val expiresAtMs: Long)
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
        homeLoadingMore = false
        homeAppendError = null
        homeState = Load.Loading
        try {
            homeState = Load.Ok(repo.loadHome(selectedPlatform))
        } catch (e: CancellationException) {
            throw e
        } catch (t: Throwable) {
            homeState = Load.Err(t.message ?: "Gagal memuat beranda")
        }
    }

    LaunchedEffect(selectedDrama) {
        val d = selectedDrama ?: return@LaunchedEffect
        detailState = Load.Loading
        try {
            detailState = Load.Ok(repo.loadDetailCached(d))
        } catch (e: CancellationException) {
            throw e
        } catch (t: Throwable) {
            detailState = Load.Err(t.message ?: "Gagal memuat detail")
        }
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

    fun loadMoreHome() {
        val current = (homeState as? Load.Ok)?.data ?: return
        if (homeLoadingMore || !current.hasMore) return
        val platformAtStart = selectedPlatform
        val nextPage = current.loadedPage + 1
        homeLoadingMore = true
        homeAppendError = null
        scope.launch {
            try {
                val next = repo.loadHomePage(platformAtStart, nextPage)
                if (selectedPlatform == platformAtStart) {
                    val latest = (homeState as? Load.Ok)?.data
                    if (latest != null && next.loadedPage > latest.loadedPage) {
                        homeState = Load.Ok(mergeHomeBundles(latest, next))
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (t: Throwable) {
                if (selectedPlatform == platformAtStart) {
                    homeAppendError = t.message ?: "Gagal memuat halaman berikutnya"
                }
            } finally {
                if (selectedPlatform == platformAtStart) homeLoadingMore = false
            }
        }
    }

    BackHandler(enabled = selectedDrama != null) { selectedDrama = null; pendingResume = null }

    Box(Modifier.fillMaxSize().background(Bg)) {
        Scaffold(
            containerColor = Bg,
            bottomBar = {
                Surface(
                    color = Color(0xD905080D),
                    tonalElevation = 0.dp
                ) {
                    Column {
                        Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0x2210F5A6)))
                        NavigationBar(containerColor = Color.Transparent, tonalElevation = 0.dp) {
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
                        loadingMore = homeLoadingMore,
                        loadMoreError = homeAppendError,
                        onLoadMore = { loadMoreHome() },
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
    when (state) {
        Load.Loading, Load.Idle -> Box(Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = Accent)
                Spacer(Modifier.height(12.dp))
                Text("Menyiapkan cuplikan...", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
        is Load.Err -> Box(Modifier.fillMaxSize().background(Bg), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                Text("Cuplikan belum tersedia", color = Text, fontWeight = FontWeight.Black, fontSize = 22.sp)
                Text(state.message, color = Muted, modifier = Modifier.padding(top = 8.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                Spacer(Modifier.height(14.dp))
                Button(onClick = onBackHome, colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = Color.Black)) {
                    Text("Kembali ke Temukan", fontWeight = FontWeight.Black)
                }
            }
        }
        is Load.Ok -> {
            val pool = remember(state.data) {
                (state.data.popular + state.data.newest + state.data.recommended)
                    .filter { it.id.isNotBlank() && it.poster.isNotBlank() }
                    .distinctBy { it.platform + it.id }
                    .take(100)
            }
            if (pool.isEmpty()) {
                Box(Modifier.fillMaxSize().background(Bg), contentAlignment = Alignment.Center) {
                    Text("Cuplikan belum tersedia untuk platform ini", color = Muted)
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
    }
}

@Composable
private fun RewardsScreen() {
    Column(
        Modifier.fillMaxSize().background(Bg).padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(color = Color(0x1510F5A6), shape = RoundedCornerShape(28.dp)) {
            Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Hadiah", color = Text, fontSize = 28.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(8.dp))
                Text("Fitur reward/check-in native sedang disiapkan.", color = Muted, fontSize = 13.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                Spacer(Modifier.height(14.dp))
                Text("Segera hadir", color = Accent, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun HomeScreen(
    platformId: String,
    state: Load<HomeBundle>,
    history: List<HistoryItem>,
    remoteConfig: NativeRemoteConfig?,
    remoteError: String?,
    loadingMore: Boolean,
    loadMoreError: String?,
    onLoadMore: () -> Unit,
    onPlatform: (String) -> Unit,
    onRefresh: () -> Unit,
    onDrama: (Drama) -> Unit,
    onSearch: () -> Unit,
    onRandom: () -> Unit,
    onClips: () -> Unit,
    onResume: (HistoryItem) -> Unit
) {
    val listState = rememberLazyListState()
    var requestedPage by remember(platformId) { mutableIntStateOf(0) }

    val loadedPage = (state as? Load.Ok)?.data?.loadedPage ?: 0
    LaunchedEffect(platformId, loadedPage) {
        if (loadedPage <= 1) requestedPage = loadedPage
    }

    LaunchedEffect(listState, state, platformId, loadingMore) {
        snapshotFlow {
            val info = listState.layoutInfo
            val last = info.visibleItemsInfo.lastOrNull()
            if (last == null) false
            else last.index >= info.totalItemsCount - 3 && (last.offset + last.size) <= info.viewportEndOffset + 900
        }.collect { nearBottom ->
            val data = (state as? Load.Ok)?.data ?: return@collect
            val nextPage = data.loadedPage + 1
            if (nearBottom && data.hasMore && !loadingMore && requestedPage != nextPage) {
                requestedPage = nextPage
                onLoadMore()
            }
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 18.dp)
    ) {
        item {
            Header(platformId, onSearch, onRefresh)
            PlatformFilterChips(platformId, remoteConfig, onPlatform)
            PlatformDropdown(platformId, state is Load.Loading, remoteConfig, onPlatform)
            RemoteConfigBanner(remoteConfig, remoteError)
        }
        when (state) {
            Load.Loading, Load.Idle -> item { LoadingHome() }
            is Load.Err -> item { ErrorBox(state.message, onRefresh) }
            is Load.Ok -> {
                val data = state.data
                val spotlight = (data.popular + data.newest + data.recommended).firstOrNull { it.poster.isNotBlank() }
                if (spotlight != null) item { Spotlight(spotlight, onDrama) }
                item { PlatformStatusStrip(remoteConfig) }
                if (history.isNotEmpty()) item { ContinueWatching(history, onResume) }
                item { ForYouSection(history, (data.popular + data.newest + data.recommended), onDrama) }
                if (data.popular.isNotEmpty()) item { Top10RankingRail("Top 10 Hari Ini", data.popular.take(10), onDrama) }
                if (data.popular.size > 10) item { DramaGridSection("Paling Populer", data.popular.drop(10), onDrama) }
                if (data.newest.isNotEmpty()) item { DramaGridSection("Drama Terbaru", data.newest, onDrama) }
                if (data.recommended.isNotEmpty()) item { DramaGridSection("Rekomendasi", data.recommended, onDrama) }
                item { HomeLoadMoreFooter(data, loadingMore, loadMoreError) }
                item { Footer() }
            }
        }
    }
}

@Composable
private fun PlatformFilterChips(
    selected: String,
    remoteConfig: NativeRemoteConfig?,
    onSelect: (String) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(Platforms) { p ->
            val isSelected = p.id == selected
            val st = remoteConfig?.platform(p.id)
            val enabled = st?.enabled ?: true
            Surface(
                color = if (isSelected) Accent else Bg3,
                contentColor = if (isSelected) Color.Black else Text,
                shape = RoundedCornerShape(999.dp),
                modifier = Modifier.clickable {
                    if (enabled) onSelect(p.id)
                }
            ) {
                Row(
                    Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PlatformLogo(p.id, size = 20.dp, enabled = enabled)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        p.label,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.Black else if (enabled) Text else Muted
                    )
                }
            }
        }
    }
}

@Composable
private fun Top10RankingRail(title: String, items: List<Drama>, onDrama: (Drama) -> Unit) {
    Column(Modifier.padding(top = 14.dp)) {
        SectionTitle(title)
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            itemsIndexed(items.take(10)) { index, drama ->
                val rank = index + 1
                Box(Modifier.width(135.dp).clickable { onDrama(drama) }) {
                    Column {
                        Box {
                            Poster(drama.poster, drama.title, Modifier.fillMaxWidth().aspectRatio(0.71f))
                            Surface(
                                color = Accent,
                                contentColor = Color.Black,
                                shape = RoundedCornerShape(topStart = 18.dp, bottomEnd = 12.dp),
                                modifier = Modifier.align(Alignment.TopStart)
                            ) {
                                Text(
                                    "#$rank",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            if (drama.episodes > 0) {
                                Badge("${drama.episodes} Ep", Modifier.align(Alignment.BottomStart).padding(7.dp), dark = true)
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            drama.title.ifBlank { "Tanpa Judul" },
                            color = Text,
                            fontSize = 13.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            drama.views.ifBlank { drama.tags.firstOrNull().orEmpty() },
                            color = Accent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun HomeLoadMoreFooter(data: HomeBundle, loadingMore: Boolean, error: String?) {
    Column(Modifier.fillMaxWidth().padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        when {
            loadingMore -> {
                CircularProgressIndicator(color = Accent, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
                Spacer(Modifier.height(8.dp))
                Text("Memuat page ${data.loadedPage + 1}...", color = Muted, fontSize = 12.sp)
            }
            data.hasMore -> {
                if (error != null) Text(error, color = Danger, fontSize = 12.sp)
                Text("Scroll untuk memuat halaman berikutnya", color = Muted, fontSize = 12.sp)
            }
            else -> {
                Text("Semua konten yang tersedia sudah ditampilkan", color = Muted, fontSize = 12.sp)
            }
        }
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
            IconButton(onClick = onSearch) { Icon(Icons.Rounded.Search, contentDescription = "Cari", tint = Text, modifier = Modifier.size(24.dp)) }
            IconButton(onClick = onRefresh) { Icon(Icons.Rounded.Refresh, contentDescription = "Refresh", tint = Text, modifier = Modifier.size(24.dp)) }
        }
        Text("Drama pendek, film, dan drakor pilihan dalam satu aplikasi.", color = Muted, fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp))
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
                        Icon(if (expanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown, contentDescription = null, tint = Accent, modifier = Modifier.size(26.dp))
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
                Text("● Spotlight Hari Ini", color = Accent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(drama.title, color = Text, fontWeight = FontWeight.Black, fontSize = 24.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(drama.description.ifBlank { platformLabel(drama.platform) }, color = Muted, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Pill("▶ Tonton", selected = true) { onDrama(drama) }
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
    Box(Modifier.fillMaxWidth().padding(30.dp), contentAlignment = Alignment.Center) {
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

        SettingSwitch("Mode hemat data", "Prioritaskan stream 480p kalau tersedia", dataSaver) {
            dataSaver = it
            store.setDataSaver(it)
            bump()
        }
        SettingSwitch("Auto next episode", "Lanjut otomatis saat episode selesai", autoNext) {
            autoNext = it
            store.setAutoNext(it)
            bump()
        }
        SettingSwitch("Mode video default Asli", "Mati = Full layar, aktif = rasio asli", fitContainDefault) {
            fitContainDefault = it
            store.setFitContain(it)
            bump()
        }
        SettingRow("Riwayat", "$historyCount item tersimpan") {}
        SettingRow("Favorit", "$favCount judul") {}
        SettingRow("Recent search", "$recentCount kata kunci tersimpan") {}
        SettingRow("Bersihkan recent search", "Hapus kata kunci pencarian terakhir", danger = true) {
            store.clearRecentSearches(); bump(); Toast.makeText(context, "Recent search dihapus", Toast.LENGTH_SHORT).show()
        }
        SettingRow("Bersihkan riwayat", "Hapus data lokal riwayat/progress", danger = true) {
            store.clearHistory(); bump(); Toast.makeText(context, "Riwayat dihapus", Toast.LENGTH_SHORT).show()
        }
        SettingRow("Bersihkan favorit", "Kosongkan daftar favorit", danger = true) {
            store.clearFavs(); bump(); Toast.makeText(context, "Favorit dihapus", Toast.LENGTH_SHORT).show()
        }
        SettingRow("Tentang Dramaku", "Versi, platform, dan info aplikasi") { dialog = "about" }
        SettingRow("Privasi", "Data lokal, cache, history, favorit") { dialog = "privacy" }
        SettingRow("Disclaimer", "Aplikasi agregator, tidak meng-host konten") { dialog = "disclaimer" }
    }

    dialog?.let { type ->
        val (title, body) = when (type) {
            "privacy" -> "Privasi" to "History, favorit, recent search, dan progress tontonan disimpan lokal di perangkat. Dramaku native tidak meng-host video dan tidak menyimpan data akun di server aplikasi ini."
            "disclaimer" -> "Disclaimer" to "Semua konten milik platform masing-masing. Aplikasi ini hanya aggregator UI/client. Gunakan dengan bijak dan hormati hak cipta pemilik konten."
            else -> "Tentang Dramaku" to "Dramaku Native adalah app Android Kotlin + Jetpack Compose dengan native ExoPlayer, search lintas platform, history/favorit lokal, dan player vertical swipe."
        }
        AlertDialog(
            onDismissRequest = { dialog = null },
            confirmButton = { TextButton(onClick = { dialog = null }) { Text("Tutup", color = Accent) } },
            title = { Text(title, color = Text, fontWeight = FontWeight.Black) },
            text = { Text(body, color = Muted) },
            containerColor = Bg2,
            titleContentColor = Text,
            textContentColor = Muted
        )
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
    // Keep only User-Agent globally. Some signed CDN URLs (MovieBox/FlickReels)
    // reject unexpected Origin/Referer headers and cause Media3 SOURCE errors.
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
        val quickDetail = repo.previewDetail(drama)
        currentDetail = quickDetail
        val stream = try {
            repo.resolveStreamCached(quickDetail, 1, store.dataSaver())
        } catch (e: CancellationException) {
            throw e
        } catch (_: Throwable) {
            try {
                val fullDetail = repo.loadDetailCached(drama)
                currentDetail = fullDetail
                repo.resolveStreamCached(fullDetail, 1, store.dataSaver())
            } catch (e: CancellationException) {
                throw e
            } catch (t: Throwable) {
                loading = false
                error = t.message ?: "Cuplikan belum tersedia"
                player.stop()
                return@LaunchedEffect
            }
        }
        if (stream.url.isBlank()) {
            loading = false
            error = "Cuplikan belum tersedia"
            player.stop()
            return@LaunchedEffect
        }
        player.setMediaItem(buildNativeMediaItem(stream))
        player.prepare()
        player.seekTo(0)
        player.playWhenReady = true
        loading = false

        // Warm the next clip URL in memory so vertical swipes feel faster.
        items.getOrNull(pagerState.currentPage + 1)?.let { nextDrama ->
            launch {
                try {
                    repo.resolveStreamCached(repo.previewDetail(nextDrama), 1, store.dataSaver())
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Throwable) {
                    try {
                        val fullDetail = repo.loadDetailCached(nextDrama)
                        repo.resolveStreamCached(fullDetail, 1, store.dataSaver())
                    } catch (e: CancellationException) {
                        throw e
                    } catch (_: Throwable) {
                        // Prefetch is best-effort only.
                    }
                }
            }
        }
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
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var retryKey by remember { mutableIntStateOf(0) }
    var lastEpisode by remember { mutableIntStateOf(startEpisode.coerceIn(1, total)) }
    var uiVisible by remember { mutableStateOf(true) }
    var episodeSheet by remember { mutableStateOf(false) }
    var sheetRange by remember { mutableIntStateOf(0) }
    var fitContain by remember { mutableStateOf(store.fitContain()) }
    var playing by remember { mutableStateOf(false) }
    var currentMs by remember { mutableLongStateOf(0L) }
    var durationMs by remember { mutableLongStateOf(0L) }
    var isSeeking by remember { mutableStateOf(false) }
    var flashText by remember { mutableStateOf<String?>(null) }
    var speedHold by remember { mutableStateOf(false) }
    var liked by remember { mutableStateOf(false) }
    var lastProgressSaveMs by remember { mutableLongStateOf(0L) }

    fun saveProgress(ep: Int) {
        runCatching {
            val duration = player.duration.takeIf { it > 0 } ?: 0L
            store.updateProgress(detail.drama.id, detail.drama.platform, ep, player.currentPosition.coerceAtLeast(0L), duration)
        }
    }

    fun closePlayer() {
        saveProgress(pagerState.currentPage + 1)
        runCatching { player.pause() }
        onClose()
    }

    BackHandler {
        if (episodeSheet) episodeSheet = false else closePlayer()
    }

    DisposableEffect(Unit) {
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                playing = isPlaying
            }

            override fun onPlayerError(errorValue: PlaybackException) {
                loading = false
                error = errorValue.message ?: "Video belum tersedia"
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED && store.autoNext() && pagerState.currentPage < total - 1) {
                    saveProgress(pagerState.currentPage + 1)
                    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                }
            }
        }
        player.addListener(listener)
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            player.removeListener(listener)
            saveProgress(pagerState.currentPage + 1)
            runCatching { player.stop() }
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
                    Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP -> {
                        saveProgress(pagerState.currentPage + 1)
                        runCatching { player.pause() }
                    }
                    else -> Unit
                }
            }
            lifecycle.addObserver(observer)
            onDispose { lifecycle.removeObserver(observer) }
        }
    }

    LaunchedEffect(uiVisible, loading, error, pagerState.currentPage) {
        if (uiVisible && !loading && error == null && !episodeSheet) {
            delay(2800)
            uiVisible = false
        }
    }

    LaunchedEffect(episodeSheet) {
        if (episodeSheet) uiVisible = true
    }

    LaunchedEffect(player, pagerState.currentPage, loading) {
        while (true) {
            val dur = player.duration.takeIf { it > 0 } ?: 0L
            durationMs = dur
            if (!isSeeking) currentMs = player.currentPosition.coerceAtLeast(0L)
            if (dur > 0 && System.currentTimeMillis() - lastProgressSaveMs > 2500L) {
                lastProgressSaveMs = System.currentTimeMillis()
                saveProgress(pagerState.currentPage + 1)
            }
            delay(500)
        }
    }

    LaunchedEffect(flashText) {
        if (flashText != null) {
            delay(700)
            flashText = null
        }
    }

    LaunchedEffect(pagerState.currentPage, retryKey) {
        val ep = pagerState.currentPage + 1
        if (lastEpisode != ep) saveProgress(lastEpisode)
        lastEpisode = ep
        uiVisible = true
        episodeSheet = false
        sheetRange = ((ep - 1) / 30).coerceAtLeast(0)
        liked = false
        currentMs = 0L
        durationMs = 0L
        lastProgressSaveMs = 0L
        loading = true
        error = null
        val start = store.progressMs(detail.drama.id, detail.drama.platform, ep)
        store.saveHistory(detail.drama, ep)
        val stream = try {
            repo.resolveStreamCached(detail, ep, store.dataSaver())
        } catch (e: CancellationException) {
            throw e
        } catch (t: Throwable) {
            loading = false
            error = t.message ?: "Video belum tersedia"
            player.stop()
            return@LaunchedEffect
        }
        if (stream.url.isBlank()) {
            loading = false
            error = "Video belum tersedia"
            player.stop()
            return@LaunchedEffect
        }
        player.setMediaItem(buildNativeMediaItem(stream))
        player.prepare()
        if (start > 0) player.seekTo(start)
        player.playWhenReady = true
        loading = false

        // Prefetch the next episode stream URL after the current episode starts resolving.
        if (ep < total) {
            launch {
                try {
                    repo.resolveStreamCached(detail, ep + 1, store.dataSaver())
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Throwable) {
                    // Prefetch is best-effort only.
                }
            }
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(player, pagerState.currentPage) {
                detectTapGestures(
                    onTap = {
                        uiVisible = !uiVisible
                        if (!uiVisible) episodeSheet = false
                    },
                    onDoubleTap = { offset ->
                        uiVisible = true
                        val width = size.width.coerceAtLeast(1)
                        when {
                            offset.x < width * 0.42f -> {
                                val next = (player.currentPosition - 10_000L).coerceAtLeast(0L)
                                player.seekTo(next)
                                currentMs = next
                                flashText = "-10 detik"
                            }
                            offset.x > width * 0.58f -> {
                                val dur = player.duration.takeIf { it > 0 } ?: Long.MAX_VALUE
                                val next = (player.currentPosition + 10_000L).coerceAtMost(dur)
                                player.seekTo(next)
                                currentMs = next
                                flashText = "+10 detik"
                            }
                            else -> {
                                liked = true
                                flashText = "Suka"
                            }
                        }
                    },
                    onPress = {
                        val releasedQuickly = withTimeoutOrNull(520) { tryAwaitRelease() }
                        if (releasedQuickly == null) {
                            speedHold = true
                            flashText = "2x"
                            runCatching { player.setPlaybackSpeed(2f) }
                            tryAwaitRelease()
                            runCatching { player.setPlaybackSpeed(1f) }
                            speedHold = false
                        }
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
                                this.player = player
                                resizeMode = if (fitContain) AspectRatioFrameLayout.RESIZE_MODE_FIT else AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                            }
                        },
                        update = {
                            it.player = player
                            it.resizeMode = if (fitContain) AspectRatioFrameLayout.RESIZE_MODE_FIT else AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                if (uiVisible || loading || error != null) {
                    Box(
                        Modifier.fillMaxSize().background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Transparent, Color(0xAA000000)),
                                startY = 360f
                            )
                        )
                    )
                    Column(
                        Modifier.align(Alignment.BottomStart).padding(14.dp, 14.dp, 90.dp, 26.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Poster(detail.drama.poster, detail.drama.title, Modifier.width(54.dp).height(78.dp))
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Surface(color = Accent, contentColor = Color.Black, shape = RoundedCornerShape(999.dp)) {
                                    Text("Episode $ep", fontWeight = FontWeight.Black, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp))
                                }
                                Spacer(Modifier.height(6.dp))
                                Text(detail.drama.title, color = Color.White, fontWeight = FontWeight.Black, fontSize = 17.sp, lineHeight = 20.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                val desc = detail.drama.description.ifBlank { "Swipe atas/bawah buat pindah episode" }
                                Text(desc, color = Color(0xCCFFFFFF), fontSize = 12.sp, lineHeight = 16.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
            }
        }

        AnimatedVisibility(uiVisible || loading || error != null, modifier = Modifier.align(Alignment.TopStart)) {
            Row(
                Modifier.fillMaxWidth().padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { closePlayer() }, modifier = Modifier.clip(CircleShape).background(Color(0x99000000))) {
                    Icon(Icons.Rounded.ArrowBack, contentDescription = "Kembali", tint = Color.White, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text(detail.drama.title, color = Color.White, fontWeight = FontWeight.Black, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("Episode ${pagerState.currentPage + 1} dari $total", color = Color(0xBFFFFFFF), fontSize = 11.sp, maxLines = 1)
                }
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
                    Text(formatMs(durationMs), color = Color(0xCCFFFFFF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = if (durationMs > 0) (currentMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f,
                    onValueChange = { v ->
                        isSeeking = true
                        val pos = (v * durationMs).toLong().coerceAtLeast(0L)
                        currentMs = pos
                    },
                    onValueChangeFinished = {
                        player.seekTo(currentMs)
                        saveProgress(pagerState.currentPage + 1)
                        isSeeking = false
                    },
                    enabled = durationMs > 0,
                    colors = SliderDefaults.colors(
                        thumbColor = Accent,
                        activeTrackColor = Accent,
                        inactiveTrackColor = Color(0x66FFFFFF)
                    )
                )
            }
        }

        AnimatedVisibility(flashText != null || speedHold || liked, modifier = Modifier.align(Alignment.Center)) {
            Surface(color = Color(0xAA000000), shape = RoundedCornerShape(999.dp)) {
                Text(
                    flashText ?: if (speedHold) "2x" else "♥",
                    color = if (liked) Accent else Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = if (liked) 34.sp else 20.sp,
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 12.dp)
                )
            }
        }

        if (loading) {
            Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = Accent)
                Spacer(Modifier.height(12.dp))
                Text("Memuat Episode ${pagerState.currentPage + 1}...", color = Color.White)
            }
        }

        if (error != null) {
            Surface(color = Color(0xDD101B27), shape = RoundedCornerShape(20.dp), modifier = Modifier.align(Alignment.Center).padding(24.dp)) {
                Column(Modifier.padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(error ?: "Gagal memutar video", color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { retryKey++ }, colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = Color.Black)) {
                        Text("Coba Lagi", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (episodeSheet) {
            Box(
                Modifier.fillMaxSize().background(Color(0x99000000)).clickable { episodeSheet = false }
            )
            Surface(
                color = Bg2,
                shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().heightIn(max = 430.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Daftar Episode", color = Text, fontWeight = FontWeight.Black, fontSize = 20.sp)
                            Text(detail.drama.title, color = Muted, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        IconButton(onClick = { episodeSheet = false }) { Text("×", color = Text, fontSize = 26.sp) }
                    }
                    Spacer(Modifier.height(10.dp))
                    val rangeSize = 30
                    val rangeCount = ((total + rangeSize - 1) / rangeSize).coerceAtLeast(1)
                    if (rangeCount > 1) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(bottom = 10.dp)) {
                            items((0 until rangeCount).toList()) { r ->
                                val st = r * rangeSize + 1
                                val en = min(total, (r + 1) * rangeSize)
                                Pill("$st-$en", sheetRange == r) { sheetRange = r }
                            }
                        }
                    }
                    val startEp = sheetRange * rangeSize + 1
                    val endEp = min(total, startEp + rangeSize - 1)
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(5),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth().heightIn(max = 340.dp)
                    ) {
                        items((startEp..endEp).toList()) { ep ->
                            val active = ep == pagerState.currentPage + 1
                            Surface(
                                color = if (active) Accent else Bg3,
                                contentColor = if (active) Color.Black else Text,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.height(44.dp).clickable {
                                    episodeSheet = false
                                    uiVisible = true
                                    scope.launch { pagerState.animateScrollToPage(ep - 1) }
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
    if (lowerUrl.contains(".m3u8") || lowerUrl.contains("m3u8")) {
        builder.setMimeType(MimeTypes.APPLICATION_M3U8)
    }
    if (stream.subtitle.isNotBlank()) {
        val lower = stream.subtitle.lowercase()
        val mime = if (lower.endsWith(".vtt")) MimeTypes.TEXT_VTT else MimeTypes.APPLICATION_SUBRIP
        val sub = MediaItem.SubtitleConfiguration.Builder(Uri.parse(stream.subtitle))
            .setMimeType(mime)
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
                        Button(onClick = { onPlay(detail, resumeEp) }, enabled = state is Load.Ok && resolvingEpisode == 0, colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = Color.Black), modifier = Modifier.weight(1f).height(50.dp), shape = RoundedCornerShape(16.dp)) {
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
        putExtra(Intent.EXTRA_SUBJECT, drama.title)
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(send, "Bagikan"))
}

private fun shareEpisodeReport(context: Context, drama: Drama, episode: Int, error: String?) {
    val text = "Laporan Episode Bermasalah - Dramaku Native\n\nJudul: ${drama.title}\nPlatform: ${platformLabel(drama.platform)}\nEpisode: $episode\nError: ${error ?: "-"}\nWaktu: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US).format(java.util.Date())}"
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Laporan Dramaku - ${drama.title}")
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(send, "Laporkan Episode"))
}

private class DramakuRepository {
    // Home load fires many requests to the same API host. OkHttp's default maxRequestsPerHost is 5,
    // so page/section requests are queued unless we raise it for this API client.
    private val dispatcher = Dispatcher().apply {
        maxRequests = 32
        maxRequestsPerHost = 16
    }

    private val client = OkHttpClient.Builder()
        .dispatcher(dispatcher)
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(18, TimeUnit.SECONDS)
        .callTimeout(24, TimeUnit.SECONDS)
        .build()

    private val detailCache = ConcurrentHashMap<String, Detail>()
    private val streamCache = ConcurrentHashMap<String, CachedStream>()

    fun previewDetail(input: Drama): Detail {
        detailCache[detailKey(input)]?.let { return it }
        val total = input.episodes.coerceAtLeast(1)
        return Detail(
            input.copy(episodes = total),
            (1..total).map { EpisodeInfo(it) }
        )
    }

    suspend fun loadDetailCached(input: Drama): Detail {
        val key = detailKey(input)
        detailCache[key]?.let { return it }
        return loadDetail(input).also { detailCache[key] = it }
    }

    suspend fun resolveStreamCached(detail: Detail, ep: Int, dataSaver: Boolean): StreamResult {
        val key = streamKey(detail.drama, ep, dataSaver)
        val now = System.currentTimeMillis()
        streamCache[key]?.takeIf { it.expiresAtMs > now }?.let { return it.result }
        return resolveStream(detail, ep, dataSaver).also { result ->
            if (result.url.isNotBlank()) {
                // Stream URLs are often signed/short-lived, so cache only briefly.
                streamCache[key] = CachedStream(result, now + 2 * 60 * 1000L)
            }
        }
    }

    private fun detailKey(d: Drama): String = "${d.platform}|${d.id}"
    private fun streamKey(d: Drama, ep: Int, dataSaver: Boolean): String = "${d.platform}|${d.id}|$ep|${if (dataSaver) "480" else "720"}"

    suspend fun loadHome(platformId: String): HomeBundle = loadHomePage(platformId, 1)

    suspend fun loadHomePage(platformId: String, page: Int): HomeBundle = coroutineScope {
        // True infinite scroll: one UI page = one API endpoint request.
        // Page 1 loads the fastest/main section first, then scrolling loads the next sections/pages.
        val request = homePageRequest(platformId, page)
        var rec = emptyList<Drama>()
        var pop = emptyList<Drama>()
        var nw = emptyList<Drama>()

        val json = try {
            getJson(request.url)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Throwable) {
            null
        }
        val items = dedupe(json?.let { flat(it.dataOrSelf(), platformId) }.orEmpty())
        when (request.section) {
            HomeSection.Popular -> pop = items
            HomeSection.Newest -> nw = items
            HomeSection.Recommended -> rec = items
        }

        // DramaNova occasionally returns 503. Only do a light fallback on the first page;
        // do not repeat cross-platform fallback on every scroll page.
        var canLoadMore = request.hasMore
        if (platformId == "dramanova" && request.virtualPage == 1 && rec.isEmpty() && pop.isEmpty() && nw.isEmpty()) {
            val fallback = loadFallbackHomeForBrokenPlatform("dramanova")
            rec = fallback.recommended
            pop = fallback.popular
            nw = fallback.newest
            canLoadMore = false
        }

        val hasData = rec.isNotEmpty() || pop.isNotEmpty() || nw.isNotEmpty()
        if (!hasData && request.virtualPage == 1) error("Data platform kosong / endpoint gagal")

        HomeBundle(
            recommended = rec,
            popular = pop,
            newest = nw,
            loadedPage = request.virtualPage,
            hasMore = canLoadMore
        )
    }

    private suspend fun loadFallbackHomeForBrokenPlatform(brokenPlatform: String): HomeBundle = coroutineScope {
        val fallbackPlatforms = listOf("melolo", "freereels", "goodshort", "dramabox", "drakor")
        val jobs = fallbackPlatforms.map { p -> async { runCatching { loadHome(p) }.getOrNull() } }
        val bundles = jobs.awaitAll().filterNotNull()
        val recommended = dedupe(bundles.flatMap { it.recommended }).map { it.copy(description = "Fallback sementara untuk ${platformLabel(brokenPlatform)}") }.take(80)
        val popular = dedupe(bundles.flatMap { it.popular }).take(60)
        val newest = dedupe(bundles.flatMap { it.newest }).take(60)
        HomeBundle(recommended, popular, newest, loadedPage = 1, hasMore = false)
    }

    suspend fun searchAll(query: String): List<Drama> = coroutineScope {
        val encoded = enc(query)
        val jobs = Platforms.map { p ->
            async {
                runCatching {
                    val url = when (p.id) {
                        "melolo" -> "${p.base}/search?q=$encoded&page=1&lang=id"
                        "freereels" -> "${p.base}/search?q=$encoded&page=1&lang=id"
                        "flickreels" -> "${p.base}/search?q=$encoded"
                        "dramanova" -> "${p.base}/search?q=$encoded&page=1&size=10"
                        "reelshort" -> "${p.base}/search?q=$encoded&page=1&limit=10"
                        "netshort" -> "${p.base}/search?query=$encoded&page=1"
                        "dramabox" -> "${p.base}/search?q=$encoded&page=1&lang=in"
                        "goodshort" -> "${p.base}/search?q=$encoded&page=1"
                        "moviebox" -> "${p.base}/search?q=$encoded&page=1&perPage=10"
                        "drakor" -> "${p.base}/search?q=$encoded&page=1&limit=30&type=1&order=1"
                        else -> "${p.base}/search?q=$encoded"
                    }
                    flat(getJson(url).dataOrSelf(), p.id)
                }.getOrDefault(emptyList())
            }
        }
        val raw = jobs.awaitAll().flatten()
        dedupeAndRank(raw, query).take(80)
    }

    suspend fun loadDetail(input: Drama): Detail {
        val p = input.platform
        val url = detailUrl(input)
        val json = getJson(url)
        if (p == "drakor") {
            val info = json.optJSONObject("info") ?: error("Detail tidak ditemukan")
            val epsArr = json.optJSONObject("episodes")?.optJSONArray("data") ?: JSONArray()
            val eps = epsArr.objects().mapIndexed { idx, o -> EpisodeInfo(o.intAny("episode_number", idx + 1), o.stringAny("streaming")) }
            val d = normalize(info, p).copy(
                id = info.stringAny("id").ifBlank { input.id },
                title = info.stringAny("title").ifBlank { input.title },
                poster = fixImg(info.stringAny("image").ifBlank { input.poster }),
                description = cleanText(info.stringAny("meta_sinopsis", "shoot", "content", "meta_description")).ifBlank { input.description },
                episodes = eps.size.takeIf { it > 0 } ?: info.intAny("meta_episode", input.episodes),
                platform = p,
                subjectType = 2
            )
            return Detail(d, eps)
        }
        val data = json.optJSONObject("data") ?: error("Detail tidak ditemukan")
        if (p == "goodshort" && data.has("book")) {
            val book = data.optJSONObject("book") ?: data
            val list = data.optJSONArray("list") ?: JSONArray()
            val eps = (0 until list.length()).map { EpisodeInfo(it + 1) }
            val d = normalize(book, p).copy(
                id = book.stringAny("bookId").ifBlank { input.id },
                title = book.stringAny("bookName").ifBlank { input.title },
                description = book.stringAny("introduction").ifBlank { input.description },
                episodes = book.intAny("chapterCount", eps.size),
                poster = fixImg(book.stringAny("cover").ifBlank { input.poster }),
                platform = p
            )
            return Detail(d, eps)
        }
        if (p == "moviebox") {
            val eps = data.optJSONObject("resourceDetectors")
            val total = data.optJSONArray("resourceDetectors")?.optJSONObject(0)?.intAny("totalEpisode", 0)
                ?: data.optJSONObject("resourceDetectors")?.intAny("totalEpisode", 0) ?: input.episodes
            val d = normalize(data, p).copy(
                id = data.stringAny("subjectId").ifBlank { input.id },
                title = data.stringAny("title").ifBlank { input.title },
                description = data.stringAny("description").ifBlank { input.description },
                episodes = if (data.intAny("subjectType", 1) == 2) max(total, 1) else 1,
                poster = fixImg(data.coverUrl().ifBlank { input.poster }),
                tags = data.stringAny("genre").split(",").map { it.trim() }.filter { it.isNotBlank() },
                subjectType = data.intAny("subjectType", input.subjectType),
                platform = p
            )
            return Detail(d, (1..d.episodes.coerceAtLeast(1)).map { EpisodeInfo(it) })
        }
        val d = normalize(data, p).let { nd ->
            nd.copy(
                id = nd.id.ifBlank { input.id },
                title = nd.title.ifBlank { input.title },
                poster = fixImg(nd.poster.ifBlank { input.poster }),
                description = nd.description.ifBlank { input.description },
                episodes = max(nd.episodes, input.episodes),
                platform = p
            )
        }
        val epsArray = data.optJSONArray("video_list") ?: data.optJSONArray("episode_list") ?: data.optJSONArray("episodes") ?: data.optJSONArray("chapterList")
        val eps = epsArray?.objects()?.mapIndexed { idx, o -> EpisodeInfo(o.intAny("episode", "episode_no", "chapterIndex", idx + 1), o.stringAny("streaming")) }.orEmpty()
        val total = max(d.episodes, eps.size)
        return Detail(d.copy(episodes = total), if (eps.isNotEmpty()) eps else (1..total.coerceAtLeast(1)).map { EpisodeInfo(it) })
    }

    suspend fun resolveStream(detail: Detail, ep: Int, dataSaver: Boolean): StreamResult {
        val d = detail.drama
        val p = d.platform
        val base = apiBase(p)
        val id = d.id
        val res = if (dataSaver) 480 else 720
        return when (p) {
            "melolo" -> {
                val v2 = getJson("$base/streamv2?id=${enc(id)}&ep=$ep")
                val direct = v2.stringAny("url")
                if (direct.isNotBlank() && v2.optBoolean("playable", true) != false) {
                    StreamResult(direct)
                } else {
                    // /stream returns CENC AES-CTR encrypted ByteDance assets. Standard ExoPlayer
                    // cannot decrypt them, so do not fallback to encrypted quality URLs.
                    error("Stream Melolo tidak tersedia. Coba Retry untuk ambil link baru.")
                }
            }
            "freereels" -> {
                val j = getJson("$base/stream?dramaId=${enc(id)}&episode=$ep&lang=id").optJSONObject("data") ?: error("Video belum tersedia")
                val raw = j.stringAny("h264_m3u8", "m3u8_url", "video_url")
                val sub = subtitleFrom(j.optJSONArray("subtitles"))
                // Native ExoPlayer does not need the WebView CORS proxy. Use the raw HLS URL
                // so Media3 can resolve variant/audio segment URLs correctly.
                StreamResult(raw, sub)
            }
            "flickreels" -> {
                val streamUrl = runCatching {
                    getJson("$base/stream?id=${enc(id)}&ep=$ep").optJSONObject("data")?.stringAny("hls_url").orEmpty()
                }.getOrDefault("")
                if (streamUrl.isNotBlank()) return StreamResult(streamUrl)
                val detailJson = getJson("$base/detail?id=${enc(id)}")
                val episodes = detailJson.optJSONObject("data")?.optJSONArray("episodes")
                    ?: detailJson.optJSONObject("data")?.optJSONArray("episode_list")
                    ?: JSONArray()
                val epObj = episodes.objects().firstOrNull { it.intAny("episode", "episode_no", 0) == ep }
                    ?: episodes.optJSONObject(ep - 1)
                StreamResult(epObj?.stringAny("hls_url", "url", "video_url").orEmpty())
            }
            "reelshort" -> {
                val data = getJson("$base/stream?id=${enc(id)}&episode_no=$ep").optJSONObject("data") ?: error("Video belum tersedia")
                val vl = data.optJSONArray("videoList")?.objects().orEmpty()
                val pick = vl.firstOrNull { it.stringAny("encode") == "H264" && it.intAny("dpi", 0) == res }
                    ?: vl.firstOrNull { it.stringAny("encode") == "H264" } ?: vl.firstOrNull()
                StreamResult(pick?.stringAny("playUrl").orEmpty().ifBlank { data.stringAny("play_url") })
            }
            "drakor" -> {
                val streaming = detail.episodes.firstOrNull { it.number == ep }?.streaming ?: detail.episodes.getOrNull(ep - 1)?.streaming.orEmpty()
                if (streaming.isBlank()) error("Episode belum punya stream")
                val j = getJson("$base/stream?streaming=${enc(streaming)}")
                StreamResult(if (dataSaver) j.stringAny("480p", "360p", "720p") else j.stringAny("720p", "480p", "360p"))
            }
            "moviebox" -> {
                val resolutions = listOf(res, 720, 1080, 480, 360).distinct()
                if (d.subjectType == 2) {
                    var chosenUrl = ""
                    var chosenSub = ""
                    for (r in resolutions) {
                        val j = runCatching { getJson("$base/download-series?subjectId=${enc(id)}&se=1&resolution=$r").optJSONObject("data") }.getOrNull() ?: continue
                        val e = j.optJSONArray("episodes")?.objects()?.firstOrNull { it.intAny("ep", 1) == ep }
                            ?: j.optJSONArray("episodes")?.optJSONObject(0)
                        chosenUrl = e?.stringAny("resourceLink").orEmpty()
                        chosenSub = e?.optJSONObject("subtitle")?.stringAny("url").orEmpty()
                        if (chosenUrl.isNotBlank()) break
                    }
                    StreamResult(chosenUrl, chosenSub)
                } else {
                    var chosenUrl = ""
                    var chosenSub = ""
                    for (r in resolutions) {
                        val j = runCatching { getJson("$base/download-movie?subjectId=${enc(id)}&resolution=$r").optJSONObject("data") }.getOrNull() ?: continue
                        val files = j.optJSONArray("files")?.objects().orEmpty()
                        val f = files.firstOrNull { it.stringAny("codecName").contains("h264", true) }
                            ?: files.firstOrNull { !it.stringAny("codecName").contains("hevc", true) }
                            ?: files.firstOrNull()
                        chosenUrl = f?.stringAny("resourceLink").orEmpty()
                        chosenSub = j.optJSONObject("subtitle")?.stringAny("url").orEmpty()
                        if (chosenUrl.isNotBlank()) break
                    }
                    StreamResult(chosenUrl, chosenSub)
                }
            }
            "goodshort" -> {
                // New GoodShort API often exposes playable URLs inside detail.list[].multiVideos/cdnList,
                // while /stream may only return metadata. Prefer detail fallback for reliability.
                val detailJson = runCatching { getJson("$base/detail?bookId=${enc(id)}") }.getOrNull()
                val listFromDetail = detailJson?.optJSONObject("data")?.optJSONArray("list")
                val epData = listFromDetail?.optJSONObject(ep - 1)
                val videos = epData?.optJSONArray("multiVideos")?.objects().orEmpty()
                val pick = videos.firstOrNull { it.stringAny("type") == "${res}p" }
                    ?: videos.firstOrNull { it.stringAny("type") == "720p" }
                    ?: videos.firstOrNull()
                val fromMulti = pick?.stringAny("filePath").orEmpty()
                if (fromMulti.isNotBlank()) return StreamResult(fromMulti)
                val cdn = epData?.optJSONArray("cdnList")?.objects().orEmpty()
                    .firstOrNull { it.stringAny("videoPath").isNotBlank() }
                    ?.stringAny("videoPath").orEmpty()
                if (cdn.isNotBlank()) return StreamResult(cdn)

                // Legacy fallback when /stream returns downloadList.
                val streamData = getJson("$base/stream?bookId=${enc(id)}").optJSONObject("data")
                val downloadList = streamData?.optJSONArray("downloadList") ?: error("Video belum tersedia")
                val legacyEp = downloadList.optJSONObject(ep - 1) ?: error("Episode belum tersedia")
                val legacyVideos = legacyEp.optJSONArray("multiVideos")?.objects().orEmpty()
                val legacyPick = legacyVideos.firstOrNull { it.stringAny("type") == "${res}p" }
                    ?: legacyVideos.firstOrNull { it.stringAny("type") == "720p" }
                    ?: legacyVideos.firstOrNull()
                StreamResult(legacyPick?.stringAny("filePath").orEmpty())
            }
            "dramabox" -> {
                val data = getJson("$base/stream?bookId=${enc(id)}&chapterIndex=${ep - 1}&lang=in").optJSONObject("data") ?: error("Video belum tersedia")
                val q = data.optJSONArray("qualities")?.objects()?.firstOrNull { it.intAny("quality", 0) == res } ?: data.optJSONArray("qualities")?.optJSONObject(0)
                StreamResult(data.stringAny("videoUrl").ifBlank { q?.stringAny("videoPath").orEmpty() })
            }
            "netshort" -> {
                val data = getJson("$base/streamv2?id=${enc(id)}&ep=$ep").optJSONObject("data") ?: error("Video belum tersedia")
                val s = data.optJSONArray("streams")?.objects()?.firstOrNull { it.stringAny("encode") == "H264" } ?: data.optJSONArray("streams")?.optJSONObject(0)
                StreamResult(data.stringAny("play_url").ifBlank { s?.stringAny("url").orEmpty() })
            }
            "dramanova" -> {
                val data = getJson("$base/stream?id=${enc(id)}&ep=$ep").optJSONObject("data") ?: error("Video belum tersedia")
                val play = data.optJSONObject("play") ?: data
                val q = play.optJSONArray("qualities")?.objects()?.firstOrNull { it.stringAny("codec") == "h264" } ?: play.optJSONArray("qualities")?.optJSONObject(0)
                val sub = subtitleFrom(data.optJSONObject("info")?.optJSONArray("subtitle_tracks"))
                StreamResult(play.stringAny("video_url", "backup_url").ifBlank { q?.stringAny("main_url", "backup_url").orEmpty() }, sub)
            }
            else -> {
                val v2 = runCatching { getJson("$base/streamv2?id=${enc(id)}&ep=$ep") }.getOrNull()
                val direct = v2?.stringAny("url").orEmpty()
                if (direct.isNotBlank() && v2?.optBoolean("playable", true) != false) return StreamResult(direct)
                val j = getJson("$base/stream?id=${enc(id)}&ep=$ep")
                val q = j.optJSONArray("qualities")?.objects()?.firstOrNull { it.stringAny("codec") == "h264" } ?: j.optJSONArray("qualities")?.optJSONObject(0)
                StreamResult(q?.stringAny("url").orEmpty())
            }
        }.also { if (it.url.isBlank()) error("Video belum tersedia") }
    }

    private suspend fun getJson(url: String): JSONObject = withContext(Dispatchers.IO) {
        val req = Request.Builder().url(url).header("User-Agent", "DramakuNative/5.0 Android").build()
        client.newCall(req).execute().use { r ->
            if (!r.isSuccessful) error("HTTP ${r.code}")
            JSONObject(r.body?.string().orEmpty())
        }
    }
}

private enum class HomeSection { Recommended, Popular, Newest }
private data class HomePageRequest(val section: HomeSection, val url: String, val virtualPage: Int, val hasMore: Boolean)

private fun homeSectionOrder(platformId: String): List<HomeSection> = when (platformId) {
    // Popular tends to be the fastest/useful first screen for most short-drama sources.
    // Newest and recommended are appended by infinite scroll after the first page is visible.
    else -> listOf(HomeSection.Popular, HomeSection.Newest, HomeSection.Recommended)
}

private fun homePageRequest(platformId: String, page: Int): HomePageRequest {
    val pageRange = pagesFor(platformId)
    val sections = homeSectionOrder(platformId)
    val totalVirtualPages = pageRange.count() * sections.size
    val virtualPage = page.coerceIn(1, totalVirtualPages.coerceAtLeast(1))
    val section = sections[(virtualPage - 1) % sections.size]
    val realPage = pageRange.first + ((virtualPage - 1) / sections.size)
    val urls = homeUrls(platformId, realPage)
    val url = when (section) {
        HomeSection.Recommended -> urls[0]
        HomeSection.Popular -> urls[1]
        HomeSection.Newest -> urls[2]
    }
    return HomePageRequest(section, url, virtualPage, virtualPage < totalVirtualPages)
}

private fun pagesFor(platformId: String): IntRange = when (platformId) {
    // These endpoints are mostly static/non-paginated in the current API.
    "flickreels", "netshort" -> 1..1
    "drakor" -> 1..5
    else -> 1..5
}

private fun homeUrls(p: String, page: Int = 1): List<String> {
    val base = apiBase(p)
    val safePage = page.coerceAtLeast(1)
    val nl = p in setOf("flickreels", "dramanova", "reelshort", "netshort")
    val lang = if (p == "dramabox") "&lang=in" else if (!nl) "&lang=id" else ""
    var h = "$base/home?page=$safePage$lang"
    var pop = "$base/populer?page=$safePage$lang"
    var nw = "$base/new?page=$safePage$lang"
    when (p) {
        "dramanova" -> {
            h = "$base/recommend?page=$safePage&size=20"
            pop = "$base/discovery?size=20&page=$safePage"
            nw = "$base/recommend?page=$safePage&size=20"
        }
        "flickreels" -> {
            pop = "$base/populer"
            nw = "$base/new?page=$safePage"
        }
        "reelshort" -> {
            h = "$base/home?tab_id=0&sub_tab_id=0&page=$safePage&limit=20"
            pop = "$base/populer?page=$safePage&limit=20&period=0&rule=0"
            nw = "$base/new?page=$safePage&limit=20"
        }
        "netshort" -> {
            h = "$base/home?page=1"
            pop = "$base/populer"
            nw = "$base/new"
        }
        "dramabox" -> {
            h = "$base/home?page=$safePage&lang=in"
            pop = "$base/populer?page=$safePage&lang=in"
            nw = "$base/new?page=$safePage&lang=in"
        }
        "goodshort" -> {
            h = "$base/home?page=$safePage"
            pop = "$base/populer?page=$safePage"
            nw = "$base/new?page=$safePage&channelId=563"
        }
        "moviebox" -> {
            h = "$base/indonesia?page=$safePage&perPage=20"
            pop = "$base/global?page=$safePage&perPage=20"
            nw = "$base/horror?page=$safePage&perPage=20"
        }
        "drakor" -> {
            h = "$base/home/korea?page=$safePage&limit=30&sort=LATEST"
            pop = "$base/trending?page=$safePage&limit=30&days=30"
            nw = "$base/terbaru?page=$safePage&limit=30"
        }
    }
    return listOf(h, pop, nw)
}

private fun dedupe(items: List<Drama>): List<Drama> = items
    .filter { it.id.isNotBlank() && it.title.isNotBlank() }
    .distinctBy { it.platform + "|" + it.id }
    .distinctBy { it.platform + "|" + normalizeKey(it.title) }

private fun mergeHomeBundles(current: HomeBundle, next: HomeBundle): HomeBundle = HomeBundle(
    recommended = dedupe(current.recommended + next.recommended),
    popular = dedupe(current.popular + next.popular),
    newest = dedupe(current.newest + next.newest),
    loadedPage = max(current.loadedPage, next.loadedPage),
    hasMore = next.hasMore
)

private fun detailUrl(d: Drama): String = when (d.platform) {
    "dramabox" -> "${apiBase(d.platform)}/detail?bookId=${enc(d.id)}&lang=in"
    "goodshort" -> "${apiBase(d.platform)}/detail?bookId=${enc(d.id)}"
    "moviebox" -> "${apiBase(d.platform)}/detail?subjectId=${enc(d.id)}"
    "drakor" -> "${apiBase(d.platform)}/detail?id=${enc(d.id)}"
    "flickreels", "dramanova", "reelshort", "netshort" -> "${apiBase(d.platform)}/detail?id=${enc(d.id)}"
    else -> "${apiBase(d.platform)}/detail?id=${enc(d.id)}&lang=id"
}

private fun flat(any: Any?, fallbackPlatform: String): List<Drama> {
    val out = mutableListOf<Drama>()
    when (any) {
        is JSONArray -> any.objects().forEach { o ->
            val books = o.optJSONArray("books")
            if (books != null) out += flat(books, fallbackPlatform)
            else out += normalize(o, fallbackPlatform)
        }
        is JSONObject -> when {
            any.has("trending") || any.has("popular") || any.has("newest") -> listOf("trending", "popular", "newest").forEach { k -> out += flat(any.optJSONArray(k), "dramabox") }
            any.optJSONObject("classifyBookList")?.optJSONArray("records") != null -> out += flat(any.optJSONObject("classifyBookList")?.optJSONArray("records"), "dramabox")
            any.optJSONArray("items") != null -> out += flat(any.optJSONArray("items"), fallbackPlatform)
            any.optJSONArray("subjects") != null -> out += flat(any.optJSONArray("subjects"), "moviebox")
            any.optJSONArray("results") != null -> any.optJSONArray("results")!!.objects().forEach { r -> out += flat(r.optJSONArray("subjects"), "moviebox") }
            else -> out += normalize(any, fallbackPlatform)
        }
    }
    return out.filter { it.id.isNotBlank() && it.title.isNotBlank() }.distinctBy { it.platform + "|" + it.id }
}

private fun normalize(o: JSONObject, fallbackPlatform: String): Drama {
    val isDrakor = o.has("meta_episode") || (o.has("id") && o.has("title") && o.has("image"))
    val platform = when {
        fallbackPlatform == "dramabox" || o.has("bookId") -> "dramabox"
        fallbackPlatform == "moviebox" || o.has("subjectId") -> "moviebox"
        fallbackPlatform == "drakor" || isDrakor -> "drakor"
        o.optBoolean("free", false) -> "freereels"
        else -> fallbackPlatform
    }
    val id = o.stringAny("drama_id", "bookId", "id", "subjectId")
    val title = o.stringAny("drama_name", "bookName", "title", "bookTitle")
    val desc = cleanText(o.stringAny("introduction", "description", "meta_description", "meta_sinopsis", "shoot", "content", "synopsis"))
    val poster = fixImg(o.stringAny("thumb_url", "coverWap", "cover", "bookCover", "image", "poster", "posterImg").ifBlank { o.coverUrl() })
    val episodes = o.intAny("chapterCount", "episode_count", "meta_episode", "episode_number", "total_episodes", "chapterCnt", 0)
    val views = o.stringAny("watch_value", "hotCode", "viewCountDisplay", "hits", "viewers")
        .ifBlank { o.optJSONObject("rankVo")?.stringAny("hotCode").orEmpty() }
    val tags = tagsOf(o)
    return Drama(id = id, title = title, description = desc, poster = poster, episodes = episodes, views = views, tags = tags, platform = platform, subjectType = o.intAny("subjectType", 1))
}

private fun tagsOf(o: JSONObject): List<String> {
    val out = mutableListOf<String>()
    fun addArr(arr: JSONArray?) {
        arr?.let {
            for (i in 0 until it.length()) {
                when (val v = it.opt(i)) {
                    is JSONObject -> out += v.stringAny("tagName", "name", "title")
                    else -> out += v?.toString().orEmpty()
                }
            }
        }
    }
    addArr(o.optJSONArray("tags")); addArr(o.optJSONArray("tagV3s")); addArr(o.optJSONArray("categories"))
    o.stringAny("category", "genre").split(",").map { it.trim() }.filter { it.isNotBlank() }.forEach { out += it }
    return out.map { it.trim() }.filter { it.isNotBlank() }.distinct().take(8)
}

private fun dedupeAndRank(items: List<Drama>, query: String): List<Drama> {
    val q = normalizeKey(query)
    val seen = HashSet<String>()
    return items.filter { seen.add(it.platform + "|" + it.id) }
        .distinctBy { normalizeKey(it.title) }
        .sortedByDescending { d ->
            val t = normalizeKey(d.title)
            var score = 0
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
            // Copy old progress to the platform-aware key. Keep the old key because
            // pre-v4.7.1 data cannot prove which platform owned it.
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

        // A legacy key has no platform segment. Remove it only when no other
        // platform with the same drama ID remains in history.
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
