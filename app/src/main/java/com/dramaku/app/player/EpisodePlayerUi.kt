package com.dramaku.app.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.dramaku.app.R

internal enum class PlayerPanel { Episodes, More, Speed, Quality, Subtitles, Details }
internal enum class PlayerAction { Back, PlayPause, Like, Collect, Share, Episodes, More, Immersive, Speed, Quality, Subtitles, Fit, AutoNext, Reload, Details }
internal data class EpisodeChoice(val number: Int, val locked: Boolean = false, val progress: Float = 0f)

internal data class EpisodePlayerUiState(
    val title: String,
    val poster: String,
    val platform: String,
    val views: String = "",
    val episode: Int,
    val total: Int,
    val description: String = "",
    val playing: Boolean = false,
    val loading: Boolean = true,
    val buffering: Boolean = false,
    val failed: Boolean = false,
    val embedded: Boolean = false,
    val liked: Boolean = false,
    val collected: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val bufferedMs: Long = 0L,
    val seeking: Boolean = false,
    val speed: Float = 1f,
    val speedHeld: Boolean = false,
    val immersive: Boolean = false,
    val chromeVisible: Boolean = true,
    val dataSaver: Boolean = false,
    val subtitleEnabled: Boolean = true,
    val hasSubtitles: Boolean = false,
    val fitContain: Boolean = true,
    val autoNext: Boolean = false,
    val notice: String? = null,
    val showLike: Boolean = false
)

private val Accent = Color(0xFFFFE500)
private val Sheet = Color(0xFF202020)
private val Secondary = Color.White.copy(alpha = 0.56f)
private val PlayerFont = FontFamily(
    Font(R.font.jakarta_regular, FontWeight.Normal),
    Font(R.font.jakarta_semibold, FontWeight.SemiBold),
    Font(R.font.jakarta_bold, FontWeight.Bold)
)

/** Lightweight chrome: no duplicate title, large transport panel, or opaque side buttons. */
@Composable
internal fun EpisodePlayerChrome(
    state: EpisodePlayerUiState,
    onAction: (PlayerAction) -> Unit,
    onSeekStart: () -> Unit,
    onSeekChange: (Long) -> Unit,
    onSeekEnd: () -> Unit,
    onSeekCancel: () -> Unit
) {
    Box(Modifier.fillMaxSize()) {
        AnimatedVisibility(!state.immersive && state.chromeVisible, enter = fadeIn(), exit = fadeOut()) {
            Box(Modifier.fillMaxSize()) {
                Box(Modifier.fillMaxWidth().height(110.dp).background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.48f), Color.Transparent))))
                Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(280.dp).background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.82f)))))

                Row(
                    Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)).padding(horizontal = 8.dp).height(56.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { onAction(PlayerAction.Back) }) { Icon(Icons.Rounded.ChevronLeft, "Kembali", tint = Color.White, modifier = Modifier.size(32.dp)) }
                    Label("Ep. ${state.episode}", size = 16.sp, weight = FontWeight.SemiBold)
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = { onAction(PlayerAction.More) }) { Icon(Icons.Rounded.MoreHoriz, "Pengaturan pemutar", tint = Color.White) }
                }

                Column(
                    Modifier.align(Alignment.BottomEnd).windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom + WindowInsetsSides.End)).padding(end = 8.dp, bottom = 162.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SideAction(if (state.liked) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder, state.views.ifBlank { "Suka" }, state.liked, Color(0xFFFF5C75)) { onAction(PlayerAction.Like) }
                    SideAction(if (state.collected) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder, if (state.collected) "Tersimpan" else "Koleksi", state.collected) { onAction(PlayerAction.Collect) }
                    SideAction(Icons.Rounded.Share, "Bagikan") { onAction(PlayerAction.Share) }
                    SideAction(Icons.Rounded.Fullscreen, "Bersih") { onAction(PlayerAction.Immersive) }
                }

                Column(
                    Modifier.align(Alignment.BottomCenter).fillMaxWidth().windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal)).padding(horizontal = 20.dp)
                ) {
                    Column(Modifier.fillMaxWidth().padding(end = 60.dp).clickable { onAction(PlayerAction.Details) }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Label("Ep.${state.episode} · ${state.title}", Modifier.weight(1f, fill = false), size = 17.sp, weight = FontWeight.Bold, maxLines = 2)
                            Icon(Icons.Rounded.ChevronRight, "Info cerita", tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        if (state.description.isNotBlank()) {
                            Spacer(Modifier.height(6.dp))
                            Label(state.description, color = Color.White.copy(alpha = 0.68f), size = 12.sp, maxLines = 2)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Row(
                        Modifier.fillMaxWidth().height(40.dp).clip(RoundedCornerShape(6.dp)).background(Color.White.copy(alpha = 0.12f)).clickable { onAction(PlayerAction.Episodes) }.padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.PlaylistPlay, null, tint = Color.White, modifier = Modifier.size(21.dp))
                        Spacer(Modifier.width(7.dp))
                        Label("Episode", weight = FontWeight.SemiBold)
                        Spacer(Modifier.width(10.dp))
                        Box(Modifier.width(1.dp).height(12.dp).background(Color.White.copy(alpha = 0.25f)))
                        Spacer(Modifier.width(10.dp))
                        Label("Total ${state.total} episode · ${state.platform}", color = Color.White.copy(alpha = 0.7f), size = 12.sp, modifier = Modifier.weight(1f))
                        Icon(Icons.Rounded.ChevronRight, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                    if (!state.embedded) {
                        Row(Modifier.fillMaxWidth().height(22.dp).alpha(if (state.seeking || !state.playing) 1f else 0f), verticalAlignment = Alignment.Bottom) {
                            Label(timeLabel(state.positionMs), size = 10.sp)
                            Spacer(Modifier.weight(1f))
                            Label(timeLabel(state.durationMs), color = Secondary, size = 10.sp)
                        }
                        EpisodeSeekBar(state, onSeekStart, onSeekChange, onSeekEnd, onSeekCancel)
                    } else {
                        Spacer(Modifier.height(14.dp))
                    }
                }
            }
        }

        // While the chrome auto-hides, a hairline progress keeps orientation without inviting interaction.
        AnimatedVisibility(
            !state.immersive && !state.chromeVisible && !state.embedded && state.durationMs > 0,
            Modifier.align(Alignment.BottomCenter),
            enter = fadeIn(), exit = fadeOut()
        ) {
            val fraction = (state.positionMs.toFloat() / state.durationMs.coerceAtLeast(1L)).coerceIn(0f, 1f)
            Box(Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)).height(2.dp).background(Color.White.copy(alpha = 0.14f))) {
                Box(Modifier.fillMaxWidth(fraction).fillMaxHeight().background(Color.White.copy(alpha = 0.65f)))
            }
        }

        if (!state.playing && !state.loading && !state.buffering && !state.failed && !state.embedded && !state.showLike) {
            IconButton(onClick = { onAction(PlayerAction.PlayPause) }, modifier = Modifier.align(Alignment.Center).size(88.dp)) {
                Icon(Icons.Rounded.PlayArrow, "Putar video", tint = Color.White.copy(alpha = 0.88f), modifier = Modifier.size(76.dp))
            }
        }
        if ((state.loading || state.buffering) && !state.failed) {
            Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(Modifier.size(28.dp), color = Color.White, strokeWidth = 2.dp)
                if (state.loading) {
                    Spacer(Modifier.height(10.dp))
                    Label("Menyiapkan video…", color = Color.White.copy(alpha = 0.8f), size = 11.sp)
                }
            }
        }
        AnimatedVisibility(state.showLike, Modifier.align(Alignment.Center), enter = fadeIn(), exit = fadeOut()) {
            HeartBurst()
        }
        if (state.speedHeld) {
            Surface(Modifier.align(Alignment.TopCenter).windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top)).padding(top = 70.dp), color = Color.Black.copy(alpha = 0.60f), shape = RoundedCornerShape(30.dp)) {
                Label("▶▶  Memutar 2x", Modifier.padding(horizontal = 16.dp, vertical = 9.dp), size = 12.sp, weight = FontWeight.SemiBold)
            }
        }
        state.notice?.let {
            Surface(Modifier.align(Alignment.Center).padding(28.dp), color = Color.Black.copy(alpha = 0.72f), shape = RoundedCornerShape(8.dp)) {
                Label(it, Modifier.padding(horizontal = 18.dp, vertical = 12.dp), size = 13.sp)
            }
        }
        if (state.immersive) {
            TextButton(
                onClick = { onAction(PlayerAction.Immersive) },
                modifier = Modifier.align(Alignment.BottomEnd).windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom + WindowInsetsSides.End)).padding(12.dp),
                colors = ButtonDefaults.textButtonColors(contentColor = Color.White, containerColor = Color.Black.copy(alpha = 0.36f))
            ) {
                Icon(Icons.Rounded.FullscreenExit, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(5.dp))
                Label("Keluar layar bersih", size = 11.sp)
            }
        }
    }
}

/** Double-tap love: one popping heart plus a small burst drifting upward, Melolo-style. */
@Composable
private fun HeartBurst(modifier: Modifier = Modifier) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) { progress.animateTo(1f, tween(850, easing = LinearOutSlowInEasing)) }
    val p = progress.value
    Box(modifier.size(220.dp), contentAlignment = Alignment.Center) {
        Icon(
            Icons.Rounded.Favorite, null, tint = Color(0xFFFF5C75),
            modifier = Modifier.size(88.dp).graphicsLayer {
                val pop = if (p < 0.25f) p / 0.25f else 1f
                scaleX = 0.55f + 0.45f * pop; scaleY = 0.55f + 0.45f * pop
                alpha = 1f - (if (p > 0.6f) (p - 0.6f) / 0.4f else 0f)
            }
        )
        listOf(
            Triple(-62f, -46f, 0.95f), Triple(50f, -64f, 0.75f), Triple(-28f, -84f, 0.6f),
            Triple(72f, -20f, 0.65f), Triple(-76f, 6f, 0.55f), Triple(36f, 16f, 0.85f)
        ).forEach { (dx, dy, baseScale) ->
            Icon(
                Icons.Rounded.Favorite, null, tint = Color(0xFFFF5C75).copy(alpha = 0.92f),
                modifier = Modifier.size(26.dp).graphicsLayer {
                    translationX = dx.dp.toPx() * p
                    translationY = (dy.dp.toPx() - 34.dp.toPx()) * p
                    val scale = baseScale * (0.4f + 0.6f * p)
                    scaleX = scale; scaleY = scale
                    alpha = (1f - p * p).coerceIn(0f, 1f)
                }
            )
        }
    }
}

@Composable
private fun SideAction(icon: ImageVector, label: String, active: Boolean = false, activeColor: Color = Accent, onClick: () -> Unit) {
    Column(
        Modifier.width(56.dp).heightIn(min = 56.dp).selectable(selected = active, role = Role.Button, onClick = onClick).padding(vertical = 3.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, null, tint = if (active) activeColor else Color.White, modifier = Modifier.size(30.dp))
        Spacer(Modifier.height(4.dp))
        Label(label, size = 10.sp, weight = FontWeight.SemiBold)
    }
}

/** A 2dp track with a generous touch target; horizontal scrubbing doesn't steal vertical paging. */
@Composable
private fun EpisodeSeekBar(state: EpisodePlayerUiState, onStart: () -> Unit, onChange: (Long) -> Unit, onEnd: () -> Unit, onCancel: () -> Unit) {
    val duration = state.durationMs
    val enabled = duration > 0 && !state.failed && !state.loading
    val progress = if (duration > 0) (state.positionMs.toFloat() / duration).coerceIn(0f, 1f) else 0f
    val buffered = if (duration > 0) (state.bufferedMs.toFloat() / duration).coerceIn(0f, 1f) else 0f
    val start by rememberUpdatedState(onStart)
    val change by rememberUpdatedState(onChange)
    val end by rememberUpdatedState(onEnd)
    val cancel by rememberUpdatedState(onCancel)
    Canvas(
        Modifier.fillMaxWidth().height(48.dp).semantics {
            contentDescription = "Progres pemutaran"
            progressBarRangeInfo = ProgressBarRangeInfo(progress, 0f..1f)
            if (enabled) setProgress { fraction -> start(); change(PlaybackPolicy.positionAt(fraction, duration)); end(); true }
            else disabled()
        }.pointerInput(enabled, duration) {
            if (enabled) detectTapGestures(onTap = { start(); change(PlaybackPolicy.positionAt(it.x / size.width.coerceAtLeast(1), duration)); end() })
        }.pointerInput(enabled, duration) {
            if (enabled) detectHorizontalDragGestures(
                onDragStart = { start(); change(PlaybackPolicy.positionAt(it.x / size.width.coerceAtLeast(1), duration)) },
                onHorizontalDrag = { event, _ -> event.consume(); change(PlaybackPolicy.positionAt(event.position.x / size.width.coerceAtLeast(1), duration)) },
                onDragEnd = { end() },
                onDragCancel = { cancel() }
            )
        }
    ) {
        val y = size.height / 2f
        val stroke = (if (state.seeking) 4.dp else 2.dp).toPx()
        drawLine(Color.White.copy(alpha = 0.16f), Offset(0f, y), Offset(size.width, y), stroke, StrokeCap.Round)
        drawLine(Color.White.copy(alpha = 0.32f), Offset(0f, y), Offset(size.width * buffered, y), stroke, StrokeCap.Round)
        drawLine(Color.White, Offset(0f, y), Offset(size.width * progress, y), stroke, StrokeCap.Round)
        if (state.seeking) drawCircle(Color.White, 5.dp.toPx(), Offset(size.width * progress, y))
    }
}

@Composable
internal fun EpisodeSelectionSheet(state: EpisodePlayerUiState, episodes: List<EpisodeChoice>, onDismiss: () -> Unit, onSelect: (Int) -> Unit) {
    val ranges = remember(state.total) { PlaybackPolicy.episodeRanges(state.total) }
    var rangeIndex by remember { mutableIntStateOf(((state.episode - 1) / 30).coerceAtLeast(0)) }
    val grid = rememberLazyGridState()
    LaunchedEffect(state.episode) { rangeIndex = ranges.indexOfFirst { state.episode in it }.coerceAtLeast(0) }
    LaunchedEffect(rangeIndex) { grid.scrollToItem(0) }
    PlayerModal(onDismiss = onDismiss) {
        Column(Modifier.fillMaxWidth().fillMaxHeight(0.70f)) {
            Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 8.dp, bottom = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(state.poster, null, Modifier.width(42.dp).height(60.dp).clip(RoundedCornerShape(4.dp)), contentScale = ContentScale.Crop)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Label(state.title, size = 16.sp, weight = FontWeight.SemiBold, maxLines = 2)
                    Spacer(Modifier.height(5.dp))
                    Label("${state.platform} · Total ${state.total} episode", color = Secondary, size = 11.sp)
                }
                IconButton(onClick = onDismiss) { Icon(Icons.Rounded.Close, "Tutup daftar episode", tint = Color.White) }
            }
            HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
            Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Label("Episode", size = 16.sp, weight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Label("Sedang diputar: ${state.episode}", color = Secondary, size = 11.sp)
            }
            if (ranges.size > 1) {
                LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(22.dp)) {
                    items(ranges.indices.toList(), key = { it }) { index ->
                        Column(Modifier.selectable(index == rangeIndex, role = Role.Tab, onClick = { rangeIndex = index }).padding(vertical = 8.dp)) {
                            Label("${ranges[index].first}–${ranges[index].last}", color = if (index == rangeIndex) Accent else Secondary, weight = FontWeight.SemiBold)
                            Spacer(Modifier.height(8.dp))
                            Box(Modifier.width(24.dp).height(2.dp).background(if (index == rangeIndex) Accent else Color.Transparent))
                        }
                    }
                }
            }
            val range = ranges.getOrNull(rangeIndex) ?: IntRange.EMPTY
            val byNumber = remember(episodes) { episodes.associateBy { it.number } }
            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                state = grid,
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(range.toList(), key = { it }) { number ->
                    val choice = byNumber[number] ?: EpisodeChoice(number)
                    val active = number == state.episode
                    Box(
                        Modifier.height(48.dp).clip(RoundedCornerShape(6.dp)).background(if (active) Accent.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.06f))
                            .selectable(selected = active, enabled = !choice.locked, role = Role.RadioButton, onClick = { onSelect(number) })
                            .semantics { contentDescription = "Episode $number${if (choice.locked) ", terkunci" else if (active) ", sedang diputar" else ""}" },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            if (active) Icon(Icons.Rounded.Equalizer, null, tint = Accent, modifier = Modifier.size(14.dp))
                            Label(number.toString(), color = if (active) Accent else if (choice.locked) Secondary else Color.White.copy(alpha = 0.86f), weight = FontWeight.SemiBold, size = 14.sp)
                        }
                        if (choice.locked) Icon(Icons.Rounded.Lock, null, tint = Secondary, modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(9.dp))
                        if (choice.progress > 0f && !active) Box(Modifier.align(Alignment.BottomStart).fillMaxWidth(choice.progress.coerceIn(0f, 1f)).height(2.dp).background(Color.White.copy(alpha = 0.32f)))
                    }
                }
            }
        }
    }
}

@Composable
internal fun PlayerSettingsSheet(
    panel: PlayerPanel,
    state: EpisodePlayerUiState,
    onDismiss: () -> Unit,
    onAction: (PlayerAction) -> Unit,
    onSpeed: (Float) -> Unit,
    onDataSaver: (Boolean) -> Unit,
    onSubtitles: (Boolean) -> Unit
) {
    PlayerModal(onDismiss = onDismiss) {
        val title = when (panel) {
            PlayerPanel.Speed -> "Kecepatan"
            PlayerPanel.Quality -> "Kualitas video"
            PlayerPanel.Subtitles -> "Subtitle"
            PlayerPanel.Details -> "Tentang cerita"
            else -> "Pengaturan pemutar"
        }
        Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Label(title, size = 17.sp, weight = FontWeight.Bold, modifier = Modifier.weight(1f))
            IconButton(onClick = onDismiss) { Icon(Icons.Rounded.Close, "Tutup pengaturan", tint = Color.White) }
        }
        Column(Modifier.fillMaxWidth().heightIn(max = LocalConfiguration.current.screenHeightDp.dp * 0.70f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(bottom = 24.dp)) {
            when (panel) {
                PlayerPanel.Speed -> listOf(0.75f, 1f, 1.25f, 1.5f, 2f).forEach { speed ->
                    ChoiceRow(if (speed == 1f) "1x · Normal" else speedLabel(speed), state.speed == speed) { onSpeed(speed) }
                }
                PlayerPanel.Quality -> {
                    ChoiceRow("Otomatis", !state.dataSaver, "Mengikuti kualitas sumber dan koneksi") { onDataSaver(false) }
                    ChoiceRow("Hemat data", state.dataSaver, "Pilih stream rendah jika tersedia; batasi trek adaptif") { onDataSaver(true) }
                    Spacer(Modifier.height(12.dp))
                    Label("Resolusi yang tersedia mengikuti sumber video. Video satu kualitas tidak dapat diubah menjadi HD oleh pemutar.", color = Secondary, size = 11.sp)
                }
                PlayerPanel.Subtitles -> {
                    if (state.hasSubtitles) {
                        ChoiceRow("Aktif", state.subtitleEnabled) { onSubtitles(true) }
                        ChoiceRow("Nonaktif", !state.subtitleEnabled) { onSubtitles(false) }
                    } else {
                        Label("Sumber ini tidak menyediakan trek subtitle terpisah.", Modifier.padding(vertical = 16.dp), color = Secondary)
                    }
                    Label("Teks yang sudah menyatu dengan gambar video tidak dapat dimatikan.", Modifier.padding(top = 12.dp), color = Secondary, size = 11.sp)
                }
                PlayerPanel.Details -> {
                    Label(state.title, Modifier.padding(vertical = 12.dp), size = 20.sp, weight = FontWeight.Bold)
                    Label("${state.platform} · ${state.total} episode", color = Secondary, size = 12.sp)
                    Spacer(Modifier.height(18.dp))
                    Label(state.description.ifBlank { "Sinopsis belum tersedia untuk cerita ini." }, color = Color.White.copy(alpha = 0.8f), size = 14.sp)
                    Spacer(Modifier.height(20.dp))
                    TextButton(onClick = { onAction(PlayerAction.Episodes) }, colors = ButtonDefaults.textButtonColors(contentColor = Accent)) { Label("Lihat semua episode", color = Accent, weight = FontWeight.SemiBold) }
                }
                else -> {
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth()) {
                        Option(Icons.Rounded.Speed, "Kecepatan", speedLabel(state.speed), Modifier.weight(1f)) { onAction(PlayerAction.Speed) }
                        Option(Icons.Rounded.HighQuality, "Kualitas", if (state.dataSaver) "Hemat data" else "Otomatis", Modifier.weight(1f)) { onAction(PlayerAction.Quality) }
                        Option(Icons.Rounded.ClosedCaption, "Subtitle", if (!state.hasSubtitles) "Tidak tersedia" else if (state.subtitleEnabled) "Aktif" else "Nonaktif", Modifier.weight(1f)) { onAction(PlayerAction.Subtitles) }
                    }
                    Spacer(Modifier.height(18.dp))
                    Row(Modifier.fillMaxWidth()) {
                        Option(Icons.Rounded.AspectRatio, "Ukuran video", if (state.fitContain) "Rasio asli" else "Penuhi layar", Modifier.weight(1f)) { onAction(PlayerAction.Fit) }
                        Option(Icons.Rounded.SkipNext, "Putar otomatis", if (state.autoNext) "Aktif" else "Nonaktif", Modifier.weight(1f)) { onAction(PlayerAction.AutoNext) }
                        Option(Icons.Rounded.Refresh, "Muat ulang", "Link terbaru", Modifier.weight(1f)) { onAction(PlayerAction.Reload) }
                    }
                    Spacer(Modifier.height(18.dp))
                    Label(if (state.embedded) "Video embed memakai kontrol dari pemutar sumber." else "Ketuk untuk jeda/putar · Ketuk dua kali di sisi layar untuk ±10 detik · Tahan untuk 2x", color = Secondary, size = 11.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlayerModal(onDismiss: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Sheet,
        contentColor = Color.White,
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        scrimColor = Color.Black.copy(alpha = 0.55f),
        dragHandle = { Box(Modifier.padding(top = 8.dp, bottom = 8.dp).width(37.dp).height(4.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.25f))) },
        content = content
    )
}

@Composable
private fun ChoiceRow(title: String, selected: Boolean, subtitle: String = "", onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().heightIn(min = 56.dp).selectable(selected = selected, role = Role.RadioButton, onClick = onClick).padding(vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Label(title, color = if (selected) Accent else Color.White, size = 15.sp, weight = FontWeight.SemiBold)
            if (subtitle.isNotBlank()) { Spacer(Modifier.height(4.dp)); Label(subtitle, color = Secondary, size = 11.sp) }
        }
        if (selected) Icon(Icons.Rounded.Check, null, tint = Accent, modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun Option(icon: ImageVector, title: String, value: String, modifier: Modifier, onClick: () -> Unit) {
    Column(modifier.clip(RoundedCornerShape(10.dp)).clickable(onClick = onClick).padding(vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(48.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.07f)), contentAlignment = Alignment.Center) { Icon(icon, null, tint = Color.White, modifier = Modifier.size(24.dp)) }
        Spacer(Modifier.height(9.dp))
        Label(title, size = 11.sp, weight = FontWeight.SemiBold)
        Spacer(Modifier.height(3.dp))
        Label(value, size = 10.sp, color = Secondary)
    }
}

@Composable
private fun Label(text: String, modifier: Modifier = Modifier, color: Color = Color.White, size: TextUnit = 13.sp, weight: FontWeight = FontWeight.Normal, maxLines: Int = Int.MAX_VALUE) {
    Text(text, modifier, color = color, fontSize = size, fontFamily = PlayerFont, fontWeight = weight, maxLines = maxLines, overflow = TextOverflow.Ellipsis)
}

private fun speedLabel(value: Float): String = if (value == value.toInt().toFloat()) "${value.toInt()}x" else "${value}x"
private fun timeLabel(ms: Long): String {
    val seconds = (ms / 1_000).coerceAtLeast(0)
    return if (seconds >= 3_600) "%d:%02d:%02d".format(seconds / 3_600, seconds / 60 % 60, seconds % 60) else "%d:%02d".format(seconds / 60, seconds % 60)
}
