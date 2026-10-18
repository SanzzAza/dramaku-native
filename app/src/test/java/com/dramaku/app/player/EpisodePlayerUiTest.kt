package com.dramaku.app.player

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import org.junit.After
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.RobolectricTestRunner
import org.robolectric.Robolectric
import org.robolectric.android.controller.ActivityController
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w393dp-h852dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class EpisodePlayerUiTest {
    @get:Rule val compose = createEmptyComposeRule()
    private var activityController: ActivityController<ComponentActivity>? = null

    private fun content(body: @Composable () -> Unit) {
        val controller = Robolectric.buildActivity(ComponentActivity::class.java).setup()
        activityController = controller
        controller.get().setContent(content = body)
    }

    @After fun destroyActivity() { activityController?.pause()?.stop()?.destroy() }
    private val state = EpisodePlayerUiState(
        title = "Cerita di Balik Senja", poster = "", platform = "Melolo", episode = 3, total = 65,
        playing = true, loading = false, durationMs = 60_000L, positionMs = 15_000L, bufferedMs = 40_000L
    )

    @Test fun `playing chrome has one title and no central transport panel`() {
        content {
            MaterialTheme { Box(Modifier.fillMaxSize().background(Color.Black)) {
                EpisodePlayerChrome(state, {}, {}, {}, {}, {})
            } }
        }
        compose.onAllNodesWithText(state.title).assertCountEquals(1)
        compose.onNodeWithText("Ep. 3").assertIsDisplayed()
        compose.onNodeWithContentDescription("Putar video").assertDoesNotExist()
        compose.onNodeWithContentDescription("Mundur 10 detik").assertDoesNotExist()
        compose.onNodeWithText("Koleksi").assertIsDisplayed()
        compose.onNodeWithText("Episode").assertIsDisplayed()
        // Draw the actual Android view under Robolectric's native graphics.
        // captureToImage uses PixelCopy/Window redraw callbacks unavailable in a JVM window.
        compose.runOnIdle {
            val view = activityController!!.get().window.decorView
            val bitmap = android.graphics.Bitmap.createBitmap(view.width, view.height, android.graphics.Bitmap.Config.ARGB_8888)
            view.draw(android.graphics.Canvas(bitmap))
            val output = File("build/reports/player/player-chrome.png")
            output.parentFile?.mkdirs()
            output.outputStream().use { bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }
            bitmap.recycle()
        }
    }

    @Test fun `collection action and accessible seek dispatch real callbacks`() {
        var action: PlayerAction? = null
        var position = -1L
        var started = false
        var ended = false
        content { MaterialTheme {
            EpisodePlayerChrome(state, { action = it }, { started = true }, { position = it }, { ended = true }, {})
        } }
        compose.onNodeWithText("Koleksi").performClick()
        assertEquals(PlayerAction.Collect, action)
        compose.onNodeWithContentDescription("Progres pemutaran").performSemanticsAction(SemanticsActions.SetProgress) { it(0.5f) }
        assertTrue(started)
        assertTrue(ended)
        assertEquals(30_000L, position)
    }

    @Test fun `clean screen hides chrome but keeps an exit control`() {
        content { MaterialTheme { EpisodePlayerChrome(state.copy(immersive = true), {}, {}, {}, {}, {}) } }
        compose.onNodeWithText(state.title).assertDoesNotExist()
        compose.onNodeWithText("Koleksi").assertDoesNotExist()
        compose.onNodeWithText("Keluar layar bersih").assertIsDisplayed()
    }

    @Test fun `auto hidden chrome removes controls without the clean screen exit button`() {
        content { MaterialTheme { EpisodePlayerChrome(state.copy(chromeVisible = false), {}, {}, {}, {}, {}) } }
        compose.onNodeWithText(state.title).assertDoesNotExist()
        compose.onNodeWithText("Koleksi").assertDoesNotExist()
        compose.onNodeWithText("Episode").assertDoesNotExist()
        compose.onNodeWithText("Keluar layar bersih").assertDoesNotExist()
    }

    @Test fun `episode sheet opens active range and respects locked episodes`() {
        var selected = 0
        val episodes = (1..65).map { EpisodeChoice(it, locked = it == 35) }
        content { MaterialTheme {
            EpisodeSelectionSheet(state.copy(episode = 34), episodes, {}, { selected = it })
        } }
        compose.onNodeWithContentDescription("Episode 34, sedang diputar").assertIsSelected()
        compose.onNodeWithContentDescription("Episode 35, terkunci").assertIsNotEnabled()
        compose.onNodeWithText("1–30").performClick()
        compose.onNodeWithContentDescription("Episode 1").performClick()
        assertEquals(1, selected)
        compose.onNodeWithText("61–65").performClick()
        compose.onNodeWithContentDescription("Episode 65").assertIsDisplayed()
    }

    @Test fun `speed sheet selects two times speed`() {
        // A wrap-content dialog's window/inset animation is not driven by a real
        // Android vsync in Robolectric. Advance it explicitly rather than asking
        // Espresso to keep advancing a JVM popup until the window becomes idle.
        compose.mainClock.autoAdvance = false
        var speed = 1f
        content { MaterialTheme {
            PlayerSettingsSheet(PlayerPanel.Speed, state, {}, {}, { speed = it }, {}, {})
        } }
        compose.mainClock.advanceTimeBy(1_000L)
        compose.onNodeWithText("1x · Normal").assertIsSelected()
        compose.onNodeWithText("2x").performClick()
        assertEquals(2f, speed)
    }

    @Test fun `tap and hold do not conflict and release restores speed`() {
        var taps = 0
        val holds = mutableListOf<Boolean>()
        content {
            Box(Modifier.fillMaxSize().episodePlayerGestures(1, { taps++ }, { _, _ -> }, { holds += it }))
        }
        compose.onRoot().performTouchInput { longClick() }
        compose.waitForIdle()
        assertTrue(holds.contains(true))
        assertEquals(false, holds.last())
        assertEquals(0, taps)
        compose.onRoot().performTouchInput { click() }
        compose.mainClock.advanceTimeBy(400L) // Single tap waits for the double-tap timeout.
        compose.waitForIdle()
        assertEquals(1, taps)
    }
}
