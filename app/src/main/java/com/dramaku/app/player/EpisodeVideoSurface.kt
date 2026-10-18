package com.dramaku.app.player

import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.text.TextUtils
import android.view.LayoutInflater
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.CaptionStyleCompat
import androidx.media3.ui.PlayerView
import com.dramaku.app.R

/** TextureView participates in Compose's pager transforms instead of punching a SurfaceView hole. */
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
internal fun EpisodeVideoSurface(player: ExoPlayer, fitContain: Boolean, immersive: Boolean, modifier: Modifier = Modifier) {
    AndroidView(
        factory = { context ->
            (LayoutInflater.from(context).inflate(R.layout.episode_video_surface, FrameLayout(context), false) as PlayerView).apply {
                useController = false
                isClickable = false
                isFocusable = false
                subtitleView?.apply {
                    setApplyEmbeddedStyles(false)
                    setStyle(CaptionStyleCompat(Color.WHITE, Color.TRANSPARENT, Color.TRANSPARENT, CaptionStyleCompat.EDGE_TYPE_OUTLINE, Color.BLACK, Typeface.DEFAULT_BOLD))
                    setFractionalTextSize(0.044f)
                }
            }
        },
        update = { view ->
            view.player = player
            view.resizeMode = if (fitContain) AspectRatioFrameLayout.RESIZE_MODE_FIT else AspectRatioFrameLayout.RESIZE_MODE_ZOOM
            view.subtitleView?.setBottomPaddingFraction(if (immersive) 0.06f else 0.23f)
        },
        onRelease = { view -> view.player = null },
        modifier = modifier
    )
}

/** Embed-only fallback. Never grant file access or leave an iframe playing after disposal/background. */
@Composable
internal fun EmbeddedEpisodeVideo(url: String, foreground: Boolean, modifier: Modifier = Modifier, onFailure: (String) -> Unit) {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                setBackgroundColor(Color.BLACK)
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    mediaPlaybackRequiresUserGesture = false
                    allowFileAccess = false
                    allowContentAccess = false
                    javaScriptCanOpenWindowsAutomatically = false
                    mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) safeBrowsingEnabled = true
                    userAgentString = "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
                }
                CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
                webChromeClient = WebChromeClient()
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean =
                        request.url.scheme !in listOf("https", "about")

                    override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
                        if (request.isForMainFrame && request.url.scheme == "https") onFailure("Pemutar sumber gagal dimuat. Periksa koneksi lalu coba lagi.")
                    }
                }
            }
        },
        update = { view ->
            val target = if (foreground) url else "about:blank"
            if (view.tag != target) {
                view.tag = target
                if (!foreground) {
                    view.stopLoading()
                    view.loadUrl("about:blank")
                    view.onPause()
                } else {
                    view.onResume()
                    val source = TextUtils.htmlEncode(url)
                    val html = """<!doctype html><html><head><meta name="viewport" content="width=device-width,initial-scale=1"><style>html,body{margin:0;background:#000;width:100%;height:100%;overflow:hidden}iframe{border:0;width:100%;height:100%}</style></head><body><iframe src="$source" allow="autoplay;fullscreen;encrypted-media;picture-in-picture" allowfullscreen></iframe></body></html>"""
                    view.loadDataWithBaseURL("https://anichin.moe/", html, "text/html", "UTF-8", null)
                }
            }
        },
        onRelease = { view ->
            view.stopLoading()
            view.loadUrl("about:blank")
            view.onPause()
            view.removeAllViews()
            view.destroy()
        },
        modifier = modifier
    )
}
