package com.dramaku.app

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color as AndroidColor
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

private enum class RootTab(val title: String, val icon: String) {
    Home("Beranda", "⌂"), Search("Cari", "⌕"), Library("Koleksi", "♡"), Settings("Saya", "⚙")
}

private data class PlatformInfo(val id: String, val label: String, val base: String)
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
    PlatformInfo("melolo", "Melolo", "https://api.sonzaix.indevs.in/melolo"),
    PlatformInfo("freereels", "FreeReels", "https://api.sonzaix.indevs.in/freereels"),
    PlatformInfo("flickreels", "FlickReels", "https://api.sonzaix.indevs.in/flickreels"),
    PlatformInfo("dramanova", "DramaNova", "https://api.sonzaix.indevs.in/dramanova"),
    PlatformInfo("reelshort", "ReelShort", "https://api.sonzaix.indevs.in/reelshort"),
    PlatformInfo("netshort", "NetShort", "https://api.sonzaix.indevs.in/netshort"),
    PlatformInfo("dramabox", "DramaBox", "https://api.sonzaix.indevs.in/dramabox"),
    PlatformInfo("goodshort", "GoodShort", "https://api.sonzaix.indevs.in/goodshort"),
    PlatformInfo("moviebox", "MovieBox", "https://api.sonzaix.indevs.in/moviebox"),
    PlatformInfo("drakor", "Drakor", "https://api.sonzaix.indevs.in/drama")
)
private fun platform(id: String) = Platforms.firstOrNull { it.id == id } ?: Platforms.first()
private fun platformLabel(id: String) = platform(id).label
private fun apiBase(id: String) = platform(id).base

@Composable
private fun DramakuNativeApp() {
    val context = LocalContext.current
    val store = remember { LocalStore(context) }
    val repo = remember { DramakuRepository() }
    val scope = rememberCoroutineScope()

    var tab by remember { mutableStateOf(RootTab.Home) }
    var selectedPlatform by remember { mutableStateOf(store.platform()) }
    var refreshKey by remember { mutableIntStateOf(0) }
    var homeState by remember { mutableStateOf<Load<HomeBundle>>(Load.Idle) }
    var selectedDrama by remember { mutableStateOf<Drama?>(null) }
    var detailState by remember { mutableStateOf<Load<Detail>>(Load.Idle) }
    var dataTick by remember { mutableIntStateOf(0) }
    var resolvingEpisode by remember { mutableIntStateOf(0) }

    val playerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val data = result.data
        if (result.resultCode == Activity.RESULT_OK && data != null) {
            val id = data.getStringExtra(PlayerActivity.RESULT_DRAMA_ID).orEmpty()
            val ep = data.getIntExtra(PlayerActivity.RESULT_EPISODE, 1)
            val pos = data.getLongExtra(PlayerActivity.RESULT_POSITION, 0L)
            val dur = data.getLongExtra(PlayerActivity.RESULT_DURATION, 0L)
            if (id.isNotBlank()) {
                store.updateProgress(id, ep, pos, dur)
                dataTick++
            }
        }
    }

    fun openPlayer(detail: Detail, ep: Int) {
        if (resolvingEpisode != 0) return
        resolvingEpisode = ep
        scope.launch {
            val result = runCatching { repo.resolveStream(detail, ep, store.dataSaver()) }
            resolvingEpisode = 0
            val stream = result.getOrNull()
            if (stream == null || stream.url.isBlank()) {
                Toast.makeText(context, result.exceptionOrNull()?.message ?: "Video belum tersedia", Toast.LENGTH_SHORT).show()
                return@launch
            }
            store.saveHistory(detail.drama, ep)
            dataTick++
            val i = Intent(context, PlayerActivity::class.java).apply {
                putExtra(PlayerActivity.EXTRA_URL, stream.url)
                putExtra(PlayerActivity.EXTRA_SUBTITLE, stream.subtitle)
                putExtra(PlayerActivity.EXTRA_TITLE, "${detail.drama.title} · Ep $ep")
                putExtra(PlayerActivity.EXTRA_DRAMA_ID, detail.drama.id)
                putExtra(PlayerActivity.EXTRA_EPISODE, ep)
                putExtra(PlayerActivity.EXTRA_PLATFORM, detail.drama.platform)
                putExtra(PlayerActivity.EXTRA_START_POS, store.progressMs(detail.drama.id, ep))
            }
            playerLauncher.launch(i)
        }
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

    BackHandler(enabled = selectedDrama != null) { selectedDrama = null }

    Box(Modifier.fillMaxSize().background(Bg)) {
        Scaffold(
            containerColor = Bg,
            bottomBar = {
                NavigationBar(containerColor = Color(0xEE071018), tonalElevation = 0.dp) {
                    RootTab.values().forEach { item ->
                        NavigationBarItem(
                            selected = tab == item,
                            onClick = { tab = item },
                            icon = { Text(item.icon, color = if (tab == item) Accent else Muted, fontSize = 20.sp) },
                            label = { Text(item.title, color = if (tab == item) Accent else Muted, fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(indicatorColor = Color(0x2210F5A6))
                        )
                    }
                }
            }
        ) { pad ->
            Box(Modifier.padding(pad).fillMaxSize()) {
                when (tab) {
                    RootTab.Home -> HomeScreen(
                        platformId = selectedPlatform,
                        state = homeState,
                        history = store.history(dataTick),
                        onPlatform = {
                            selectedPlatform = it
                            store.setPlatform(it)
                            refreshKey++
                        },
                        onRefresh = { refreshKey++ },
                        onDrama = { selectedDrama = it },
                        onSearch = { tab = RootTab.Search },
                        onRandom = {
                            val bundle = (homeState as? Load.Ok)?.data
                            val pool = (bundle?.popular.orEmpty() + bundle?.newest.orEmpty() + bundle?.recommended.orEmpty()).filter { it.id.isNotBlank() }
                            if (pool.isNotEmpty()) selectedDrama = pool.random()
                        }
                    )
                    RootTab.Search -> SearchScreen(repo, store, onDrama = { selectedDrama = it }, dataTick = dataTick, bump = { dataTick++ })
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
    }
}

@Composable
private fun HomeScreen(
    platformId: String,
    state: Load<HomeBundle>,
    history: List<HistoryItem>,
    onPlatform: (String) -> Unit,
    onRefresh: () -> Unit,
    onDrama: (Drama) -> Unit,
    onSearch: () -> Unit,
    onRandom: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 18.dp)
    ) {
        item {
            Header(platformId, onSearch, onRefresh)
            PlatformDropdown(platformId, state is Load.Loading, onPlatform)
        }
        when (state) {
            Load.Loading, Load.Idle -> item { LoadingHome() }
            is Load.Err -> item { ErrorBox(state.message, onRefresh) }
            is Load.Ok -> {
                val data = state.data
                val spotlight = (data.popular + data.newest + data.recommended).firstOrNull { it.poster.isNotBlank() }
                if (spotlight != null) item { Spotlight(spotlight, onDrama) }
                item { QuickActions(onRandom = onRandom, onSearch = onSearch) }
                if (history.isNotEmpty()) item { ContinueWatching(history, onDrama) }
                if (data.popular.isNotEmpty()) item { DramaRail("Top 10 Hari Ini", data.popular.take(10), onDrama) }
                if (data.popular.size > 10) item { DramaGridSection("Paling Populer", data.popular.drop(10).take(30), onDrama) }
                if (data.newest.isNotEmpty()) item { DramaGridSection("Drama Terbaru", data.newest.take(45), onDrama) }
                if (data.recommended.isNotEmpty()) item { DramaGridSection("Rekomendasi", data.recommended.take(60), onDrama) }
                item { Footer() }
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
                Text("▶", color = Color.Black, fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Dramaku", color = Text, fontSize = 27.sp, fontWeight = FontWeight.Black)
                Text("Native • ${platformLabel(platformId)}", color = Muted, fontSize = 12.sp)
            }
            IconButton(onClick = onSearch) { Text("⌕", color = Text, fontSize = 25.sp) }
            IconButton(onClick = onRefresh) { Text("↻", color = Text, fontSize = 22.sp) }
        }
        Text("Streaming drama & film dari 10 platform dalam UI Android native.", color = Muted, fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable
private fun PlatformDropdown(selected: String, loading: Boolean, onSelect: (String) -> Unit) {
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
                    Column(Modifier.weight(1f)) {
                        Text("Pilih platform", color = Muted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(platformLabel(selected), color = Text, fontSize = 16.sp, fontWeight = FontWeight.Black)
                    }
                    if (loading) {
                        CircularProgressIndicator(color = Accent, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                    } else {
                        Text(if (expanded) "⌃" else "⌄", color = Accent, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Bg2)
            ) {
                Platforms.forEach { p ->
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    Modifier.size(9.dp).clip(CircleShape).background(if (p.id == selected) Accent else Muted)
                                )
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text(p.label, color = Text, fontWeight = FontWeight.Bold)
                                    Text(p.id, color = Muted, fontSize = 11.sp)
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
private fun PlatformLoadingPlaceholder() {
    Column(Modifier.padding(top = 12.dp)) {
        Surface(color = Bg3, shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth().height(150.dp)) {}
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            repeat(3) { Surface(color = Bg3, shape = RoundedCornerShape(18.dp), modifier = Modifier.weight(1f).height(155.dp)) {} }
        }
    }
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
private fun QuickActions(onRandom: () -> Unit, onSearch: () -> Unit) {
    Column(Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
        Text("Jelajah Cepat", color = Text, fontWeight = FontWeight.Black, fontSize = 19.sp)
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ActionCard("🎲", "Acak", "Temukan drama", Modifier.weight(1f), onRandom)
            ActionCard("🔥", "Viral", "Cari tren", Modifier.weight(1f), onSearch)
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(top = 12.dp)) {
            items(listOf("CEO", "Balas Dendam", "Romantis", "Korea", "China", "Comedy", "Action")) { q ->
                Pill(q, false, onSearch)
            }
        }
    }
}

@Composable
private fun ActionCard(icon: String, title: String, sub: String, modifier: Modifier, onClick: () -> Unit) {
    Surface(modifier = modifier.clickable(onClick = onClick), color = Bg3, shape = RoundedCornerShape(20.dp)) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(icon, fontSize = 26.sp)
            Spacer(Modifier.width(10.dp))
            Column {
                Text(title, color = Text, fontWeight = FontWeight.Bold)
                Text(sub, color = Muted, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun ContinueWatching(history: List<HistoryItem>, onDrama: (Drama) -> Unit) {
    Column(Modifier.padding(top = 10.dp)) {
        SectionTitle("Lanjutkan Tontonan")
        LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(history.take(8)) { h ->
                Column(Modifier.width(128.dp).clickable {
                    onDrama(Drama(h.id, h.title, poster = h.poster, platform = h.platform))
                }) {
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
        if (url.isNotBlank()) AsyncImage(model = url, contentDescription = title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        else Text("▶", color = Accent, fontSize = 24.sp)
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
            Surface(color = Bg3, shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth().height(if (it == 0) 180.dp else 120.dp).padding(vertical = 8.dp)) {}
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
    val recent = remember(dataTick) { store.recentSearches() }

    LaunchedEffect(q) {
        val query = q.trim()
        if (query.length < 2) {
            state = Load.Idle
            return@LaunchedEffect
        }
        delay(400)
        state = Load.Loading
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
            Text("Terakhir dicari", color = Muted, modifier = Modifier.padding(top = 18.dp, bottom = 8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(recent) { Pill(it, false) { q = it } }
            }
        }
        Spacer(Modifier.height(14.dp))
        when (state) {
            Load.Idle -> EmptyState("Ketik minimal 2 huruf buat cari di 10 platform.")
            Load.Loading -> LinearProgressIndicator(color = Accent, trackColor = Bg3, modifier = Modifier.fillMaxWidth())
            is Load.Err -> ErrorBox((state as Load.Err).message) { q = q.trim() + " " }
            is Load.Ok -> {
                val list = (state as Load.Ok<List<Drama>>).data
                if (list.isEmpty()) EmptyState("Tidak ada hasil untuk “$q”.")
                else LazyVerticalGrid(columns = GridCells.Fixed(3), verticalArrangement = Arrangement.spacedBy(14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
                    items(list, key = { it.platform + it.id }) { d -> DramaCard(d, Modifier.fillMaxWidth(), onDrama) }
                }
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
    var showFav by remember { mutableStateOf(false) }
    val history = remember(dataTick) { store.history(dataTick) }
    val favs = remember(dataTick) { store.favs() }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Koleksi", color = Text, fontSize = 27.sp, fontWeight = FontWeight.Black)
        Row(Modifier.padding(vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Pill("Riwayat (${history.size})", !showFav) { showFav = false }
            Pill("Favorit (${favs.size})", showFav) { showFav = true }
        }
        if (showFav) {
            if (favs.isEmpty()) EmptyState("Belum ada favorit.")
            else LazyVerticalGrid(columns = GridCells.Fixed(3), verticalArrangement = Arrangement.spacedBy(14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
                items(favs, key = { it.platform + it.id }) { d -> DramaCard(d, Modifier.fillMaxWidth(), onDrama) }
            }
        } else {
            if (history.isEmpty()) EmptyState("Belum ada riwayat tontonan.")
            else LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(history, key = { it.id + it.platform }) { h ->
                    Surface(color = Bg3, shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth().clickable { onDrama(Drama(h.id, h.title, poster = h.poster, platform = h.platform)) }) {
                        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Poster(h.poster, h.title, Modifier.width(72.dp).height(102.dp))
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(h.title, color = Text, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                Text("${platformLabel(h.platform)} · Episode ${h.episode}${if (h.pct > 0) " · ${h.pct}%" else ""}", color = Muted, fontSize = 12.sp)
                                if (h.pct > 0) LinearProgressIndicator(progress = h.pct / 100f, color = Accent, trackColor = Color(0x33000000), modifier = Modifier.fillMaxWidth().padding(top = 8.dp).height(4.dp))
                            }
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
    val historyCount = remember(dataTick) { store.history(dataTick).size }
    val favCount = remember(dataTick) { store.favs().size }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("Saya", color = Text, fontSize = 27.sp, fontWeight = FontWeight.Black)
        Text("Dramaku native MVP", color = Muted, modifier = Modifier.padding(bottom = 18.dp))
        SettingSwitch("Mode hemat data", "Prioritaskan stream 480p kalau tersedia", dataSaver) {
            dataSaver = it
            store.setDataSaver(it)
            bump()
        }
        SettingRow("Riwayat", "$historyCount item tersimpan") {}
        SettingRow("Favorit", "$favCount judul") {}
        SettingRow("Bersihkan riwayat", "Hapus data lokal riwayat/progress", danger = true) {
            store.clearHistory(); bump(); Toast.makeText(context, "Riwayat dihapus", Toast.LENGTH_SHORT).show()
        }
        SettingRow("Bersihkan favorit", "Kosongkan daftar favorit", danger = true) {
            store.clearFavs(); bump(); Toast.makeText(context, "Favorit dihapus", Toast.LENGTH_SHORT).show()
        }
        SettingRow("Disclaimer", "Aplikasi agregator, tidak meng-host konten") {
            Toast.makeText(context, "Semua konten milik platform masing-masing.", Toast.LENGTH_LONG).show()
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
    Box(Modifier.fillMaxSize().background(Bg)) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 22.dp)) {
            item {
                Box(Modifier.fillMaxWidth().height(285.dp)) {
                    AsyncImage(model = drama.poster, contentDescription = drama.title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Bg), startY = 80f)))
                    IconButton(onClick = onClose, modifier = Modifier.padding(12.dp).clip(CircleShape).background(Color(0x99000000))) { Text("‹", color = Text, fontSize = 34.sp) }
                }
                Row(Modifier.padding(horizontal = 16.dp).offset(y = (-60).dp), verticalAlignment = Alignment.Bottom) {
                    Poster(drama.poster, drama.title, Modifier.width(120.dp).height(172.dp))
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f).padding(bottom = 8.dp)) {
                        Text(drama.title, color = Text, fontSize = 25.sp, lineHeight = 28.sp, fontWeight = FontWeight.Black, maxLines = 3, overflow = TextOverflow.Ellipsis)
                        Text("${platformLabel(drama.platform)} · ${episodeCount(detail).coerceAtLeast(1)} Episode", color = Muted, fontSize = 12.sp)
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
                        Button(onClick = { onPlay(detail, 1) }, enabled = state is Load.Ok && resolvingEpisode == 0, colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = Color.Black), modifier = Modifier.weight(1f).height(50.dp), shape = RoundedCornerShape(16.dp)) {
                            Text(if (resolvingEpisode == 1) "Memuat..." else "▶ Mulai Tonton", fontWeight = FontWeight.Black)
                        }
                        IconButton(onClick = { store.toggleFav(drama); onFavChanged() }, modifier = Modifier.size(50.dp).clip(RoundedCornerShape(16.dp)).background(if (isFav) Accent else Bg3)) { Text(if (isFav) "♥" else "♡", color = if (isFav) Color.Black else Text, fontSize = 24.sp) }
                        IconButton(onClick = { onShare(drama) }, modifier = Modifier.size(50.dp).clip(RoundedCornerShape(16.dp)).background(Bg3)) { Text("↗", color = Text, fontSize = 22.sp) }
                    }
                    Spacer(Modifier.height(16.dp))
                    if (drama.tags.isNotEmpty()) LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { items(drama.tags.take(8)) { Pill(it) {} } }
                    Spacer(Modifier.height(14.dp))
                    Text("Sinopsis", color = Text, fontWeight = FontWeight.Black, fontSize = 18.sp)
                    Text(drama.description.ifBlank { "Belum ada sinopsis untuk judul ini." }, color = Muted, fontSize = 13.sp, lineHeight = 19.sp, modifier = Modifier.padding(top = 6.dp))
                    Spacer(Modifier.height(20.dp))
                    Text("Daftar Episode", color = Text, fontWeight = FontWeight.Black, fontSize = 18.sp)
                    Spacer(Modifier.height(10.dp))
                    val total = episodeCount(detail).coerceAtLeast(1)
                    val rows = (1..total).chunked(5)
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

private fun shareDrama(context: Context, drama: Drama) {
    val text = "${drama.title}\nPlatform: ${platformLabel(drama.platform)}\nDramaku"
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, drama.title)
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(send, "Bagikan"))
}

private class DramakuRepository {
    private val client = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(18, TimeUnit.SECONDS)
        .callTimeout(24, TimeUnit.SECONDS)
        .build()

    suspend fun loadHome(platformId: String): HomeBundle = coroutineScope {
        // Native home now loads several pages per section so the list does not feel empty
        // compared with the old WebView version.
        val pages = pagesFor(platformId)
        val homeJson = fetchMany(pages.map { homeUrls(platformId, it)[0] })
        val popularJson = fetchMany(pages.map { homeUrls(platformId, it)[1] })
        val newestJson = fetchMany(pages.map { homeUrls(platformId, it)[2] })

        val rec = dedupe(homeJson.flatMap { flat(it.dataOrSelf(), platformId) }).take(80)
        val pop = dedupe(popularJson.flatMap { flat(it.dataOrSelf(), platformId) }).take(50)
        val nw = dedupe(newestJson.flatMap { flat(it.dataOrSelf(), platformId) }).take(70)
        if (rec.isEmpty() && pop.isEmpty() && nw.isEmpty()) error("Data platform kosong / endpoint gagal")
        HomeBundle(rec, pop, nw)
    }

    private suspend fun fetchMany(urls: List<String>): List<JSONObject> = coroutineScope {
        urls.distinct().map { url -> async { runCatching { getJson(url) }.getOrNull() } }
            .awaitAll()
            .filterNotNull()
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
            "freereels" -> {
                val j = getJson("$base/stream?dramaId=${enc(id)}&episode=$ep&lang=id").optJSONObject("data") ?: error("Video belum tersedia")
                val raw = j.stringAny("h264_m3u8", "m3u8_url", "video_url")
                val sub = subtitleFrom(j.optJSONArray("subtitles"))
                StreamResult(if (raw.isNotBlank()) "https://proxy.sonzaixlab.workers.dev/proxy?url=${enc(raw)}" else "", sub)
            }
            "flickreels" -> StreamResult(getJson("$base/stream?id=${enc(id)}&ep=$ep").optJSONObject("data")?.stringAny("hls_url").orEmpty())
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
                if (d.subjectType == 2) {
                    val j = getJson("$base/download-series?subjectId=${enc(id)}&se=1&resolution=$res").optJSONObject("data") ?: error("Video belum tersedia")
                    val e = j.optJSONArray("episodes")?.objects()?.firstOrNull { it.intAny("ep", 1) == ep } ?: j.optJSONArray("episodes")?.optJSONObject(0)
                    StreamResult(e?.stringAny("resourceLink").orEmpty(), e?.optJSONObject("subtitle")?.stringAny("url").orEmpty())
                } else {
                    val j = getJson("$base/download-movie?subjectId=${enc(id)}&resolution=$res").optJSONObject("data") ?: error("Video belum tersedia")
                    val f = j.optJSONArray("files")?.objects()?.firstOrNull { it.stringAny("codecName").contains("h264", true) } ?: j.optJSONArray("files")?.optJSONObject(0)
                    StreamResult(f?.stringAny("resourceLink").orEmpty(), j.optJSONObject("subtitle")?.stringAny("url").orEmpty())
                }
            }
            "goodshort" -> {
                val list = getJson("$base/stream?bookId=${enc(id)}").optJSONObject("data")?.optJSONArray("downloadList") ?: error("Video belum tersedia")
                val epData = list.optJSONObject(ep - 1) ?: error("Episode belum tersedia")
                val videos = epData.optJSONArray("multiVideos")?.objects().orEmpty()
                val pick = videos.firstOrNull { it.stringAny("type") == "${res}p" } ?: videos.firstOrNull { it.stringAny("type") == "720p" } ?: videos.firstOrNull()
                StreamResult(pick?.stringAny("filePath").orEmpty())
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

private fun pagesFor(platformId: String): IntRange = when (platformId) {
    // These endpoints are mostly static/non-paginated in the current API.
    "flickreels", "netshort" -> 1..1
    else -> 1..3
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

    fun history(tick: Int = 0): List<HistoryItem> = parseHistory()
    fun saveHistory(drama: Drama, ep: Int) {
        val arr = JSONArray()
        val old = parseHistory().filterNot { it.id == drama.id && it.platform == drama.platform }.toMutableList()
        old.add(0, HistoryItem(drama.id, drama.title, drama.poster, drama.platform, ep))
        old.take(80).forEach { arr.put(it.toJson()) }
        prefs.edit().putString("history", arr.toString()).apply()
    }
    fun updateProgress(id: String, ep: Int, pos: Long, dur: Long) {
        val list = parseHistory().toMutableList()
        val idx = list.indexOfFirst { it.id == id }
        if (idx >= 0) {
            val h = list[idx]
            list[idx] = h.copy(episode = ep, pos = pos, dur = dur, updated = System.currentTimeMillis())
            val arr = JSONArray(); list.sortedByDescending { it.updated }.forEach { arr.put(it.toJson()) }
            prefs.edit().putString("history", arr.toString()).apply()
        }
    }
    fun progressMs(id: String, ep: Int): Long = parseHistory().firstOrNull { it.id == id && it.episode == ep }?.pos ?: 0L
    fun clearHistory() = prefs.edit().remove("history").apply()

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

    fun recentSearches(): List<String> = JSONArray(prefs.getString("recent", "[]") ?: "[]").let { arr -> (0 until arr.length()).mapNotNull { arr.optString(it).takeIf { s -> s.isNotBlank() } } }
    fun saveRecent(q: String) {
        val list = recentSearches().filterNot { it.equals(q, true) }.toMutableList()
        list.add(0, q)
        val arr = JSONArray(); list.take(10).forEach { arr.put(it) }
        prefs.edit().putString("recent", arr.toString()).apply()
    }

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
