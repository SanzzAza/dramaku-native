package com.dramaku.app.player

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput

/** Long-press and tap are mutually exclusive; cancellation always restores normal speed. */
internal fun Modifier.episodePlayerGestures(
    episode: Int,
    onTap: () -> Unit,
    onDoubleTap: (Offset, Int) -> Unit,
    onHold: (Boolean) -> Unit
): Modifier = composed {
    val tap by rememberUpdatedState(onTap)
    val doubleTap by rememberUpdatedState(onDoubleTap)
    val hold by rememberUpdatedState(onHold)
    pointerInput(episode) {
        detectTapGestures(
            onTap = { tap() },
            onDoubleTap = { doubleTap(it, size.width) },
            onLongPress = { hold(true) },
            onPress = { try { tryAwaitRelease() } finally { hold(false) } }
        )
    }
}
