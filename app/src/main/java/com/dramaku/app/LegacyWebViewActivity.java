package com.dramaku.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.RenderProcessGoneDetail;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class LegacyWebViewActivity extends AppCompatActivity {
    private static final String HOME_URL = "file:///android_asset/index.html";

    private FrameLayout root;
    private WebView webView;
    private LinearLayout recoveryView;
    private TextView recoveryMessage;
    private View customView;
    private WebChromeClient.CustomViewCallback customViewCallback;
    private static final int PLAYER_REQUEST = 4401;

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prepareWindow();

        root = new FrameLayout(this);
        root.setBackgroundColor(Color.rgb(5, 8, 13));
        setContentView(root, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        createWebView();
        createRecoveryView();
        loadHome();
    }

    private void prepareWindow() {
        Window window = getWindow();
        window.setStatusBarColor(Color.BLACK);
        window.setNavigationBarColor(Color.BLACK);
        window.addFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.getAttributes().layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
    }

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    private void createWebView() {
        if (webView != null) {
            try {
                root.removeView(webView);
                webView.destroy();
            } catch (Exception ignored) {}
            webView = null;
        }

        webView = new WebView(this);
        webView.setBackgroundColor(Color.rgb(5, 8, 13));
        root.addView(webView, 0, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        configureWebView();
        webView.addJavascriptInterface(new NativeBridge(), "NativeApp");
        webView.addJavascriptInterface(new NativePlayerBridge(), "NativePlayer");
    }

    private void createRecoveryView() {
        recoveryView = new LinearLayout(this);
        recoveryView.setOrientation(LinearLayout.VERTICAL);
        recoveryView.setGravity(Gravity.CENTER);
        recoveryView.setPadding(dp(24), dp(24), dp(24), dp(24));
        recoveryView.setBackgroundColor(Color.rgb(5, 8, 13));
        recoveryView.setVisibility(View.GONE);

        TextView title = new TextView(this);
        title.setText("Dramaku butuh dimuat ulang");
        title.setTextColor(Color.rgb(239, 255, 247));
        title.setTextSize(22);
        title.setGravity(Gravity.CENTER);
        title.setTypeface(title.getTypeface(), android.graphics.Typeface.BOLD);
        recoveryView.addView(title, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        recoveryMessage = new TextView(this);
        recoveryMessage.setText("WebView berhenti atau halaman gagal dimuat. Coba muat ulang aplikasi.");
        recoveryMessage.setTextColor(Color.rgb(145, 164, 186));
        recoveryMessage.setTextSize(13);
        recoveryMessage.setGravity(Gravity.CENTER);
        recoveryMessage.setPadding(0, dp(10), 0, dp(18));
        recoveryView.addView(recoveryMessage, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        Button reload = new Button(this);
        reload.setText("Muat Ulang");
        reload.setAllCaps(false);
        reload.setOnClickListener(v -> reloadWebView());
        recoveryView.addView(reload, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(48)
        ));

        Button clear = new Button(this);
        clear.setText("Bersihkan Cache & Muat Ulang");
        clear.setAllCaps(false);
        clear.setOnClickListener(v -> clearCacheAndReload());
        LinearLayout.LayoutParams clearParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(48)
        );
        clearParams.topMargin = dp(10);
        recoveryView.addView(clear, clearParams);

        root.addView(recoveryView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void configureWebView() {
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        // Local asset shell only — do not allow file:// pages to read other file URLs.
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(true);
        s.setAllowFileAccessFromFileURLs(false);
        s.setAllowUniversalAccessFromFileURLs(false);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setLoadWithOverviewMode(true);
        s.setUseWideViewPort(true);
        s.setSupportZoom(false);
        s.setCacheMode(WebSettings.LOAD_DEFAULT);
        // Prefer HTTPS; cleartext is also blocked by networkSecurityConfig.
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            s.setSafeBrowsingEnabled(true);
        }

        CookieManager.getInstance().setAcceptCookie(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
        }
        WebView.setWebContentsDebuggingEnabled((getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                hideRecovery();
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (request != null && request.isForMainFrame()) {
                    String message = "Halaman utama gagal dimuat.";
                    if (error != null) message = String.valueOf(error.getDescription());
                    showRecovery(message);
                }
            }

            @Override
            public boolean onRenderProcessGone(WebView view, RenderProcessGoneDetail detail) {
                showRecovery("WebView berhenti tiba-tiba. Tekan Muat Ulang untuk membuka Dramaku lagi.");
                try {
                    if (webView != null) {
                        root.removeView(webView);
                        webView.destroy();
                    }
                } catch (Exception ignored) {}
                webView = null;
                return true;
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                if (request == null || request.getUrl() == null) return true;
                Uri uri = request.getUrl();
                String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase();
                // Keep navigation inside the local asset shell; open external links outside.
                if ("file".equals(scheme)) return false;
                if ("http".equals(scheme) || "https".equals(scheme) || "mailto".equals(scheme) || "tg".equals(scheme)) {
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, uri));
                    } catch (Exception ignored) {}
                    return true;
                }
                return true;
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                if (customView != null) {
                    callback.onCustomViewHidden();
                    return;
                }
                customView = view;
                customViewCallback = callback;
                customView.setBackgroundColor(Color.BLACK);
                ViewGroup decor = (ViewGroup) getWindow().getDecorView();
                decor.addView(customView, new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                ));
                webView.setVisibility(View.GONE);
                setImmersiveMode(true);
            }

            @Override
            public void onHideCustomView() {
                hideCustomView();
            }
        });
    }

    private void loadHome() {
        if (webView == null) createWebView();
        hideRecovery();
        webView.loadUrl(HOME_URL);
    }

    private void reloadWebView() {
        runOnUiThread(() -> {
            if (webView == null) createWebView();
            loadHome();
            Toast.makeText(this, "Memuat ulang Dramaku...", Toast.LENGTH_SHORT).show();
        });
    }

    private void clearCacheAndReload() {
        runOnUiThread(() -> {
            try {
                if (webView != null) {
                    webView.clearCache(true);
                    webView.clearHistory();
                }
                CookieManager.getInstance().removeAllCookies(null);
                CookieManager.getInstance().flush();
            } catch (Exception ignored) {}
            reloadWebView();
        });
    }

    private void showRecovery(String message) {
        runOnUiThread(() -> {
            setImmersiveMode(false);
            if (recoveryMessage != null && message != null) recoveryMessage.setText(message);
            if (webView != null) webView.setVisibility(View.GONE);
            if (recoveryView != null) recoveryView.setVisibility(View.VISIBLE);
        });
    }

    private void hideRecovery() {
        if (recoveryView != null) recoveryView.setVisibility(View.GONE);
        if (webView != null) webView.setVisibility(View.VISIBLE);
    }

    private void hideCustomView() {
        if (customView == null) return;
        ViewGroup decor = (ViewGroup) getWindow().getDecorView();
        decor.removeView(customView);
        customView = null;
        if (customViewCallback != null) customViewCallback.onCustomViewHidden();
        customViewCallback = null;
        if (webView != null) webView.setVisibility(View.VISIBLE);
        setImmersiveMode(false);
    }

    private void setImmersiveMode(boolean enabled) {
        View decor = getWindow().getDecorView();
        if (enabled) {
            decor.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            );
        } else {
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private boolean isSafeExternalUrl(@Nullable String url) {
        if (url == null) return false;
        String trimmed = url.trim();
        if (trimmed.isEmpty()) return false;
        Uri uri = Uri.parse(trimmed);
        String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase();
        if ("https".equals(scheme) || "mailto".equals(scheme)) return true;
        // Telegram share links often use https://t.me — already covered.
        // Allow intentional http only for localhost debugging, never for remote.
        if ("http".equals(scheme)) {
            String host = uri.getHost() == null ? "" : uri.getHost().toLowerCase();
            return "localhost".equals(host) || "127.0.0.1".equals(host);
        }
        return false;
    }

    private boolean isSafeMediaUrl(@Nullable String url) {
        if (url == null) return false;
        String trimmed = url.trim();
        if (trimmed.isEmpty()) return false;
        Uri uri = Uri.parse(trimmed);
        String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase();
        return "https".equals(scheme) || "http".equals(scheme);
    }

    @Override
    public void onBackPressed() {
        if (customView != null) {
            hideCustomView();
            return;
        }
        if (recoveryView != null && recoveryView.getVisibility() == View.VISIBLE) {
            finish();
            return;
        }
        if (webView == null) {
            finish();
            return;
        }
        webView.evaluateJavascript("(window.handleNativeBack&&window.handleNativeBack())===true", handled -> {
            if (!"true".equals(handled)) finish();
        });
    }

    @Override
    protected void onPause() {
        if (webView != null) webView.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (webView != null) webView.onResume();
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            try { webView.destroy(); } catch (Exception ignored) {}
            webView = null;
        }
        super.onDestroy();
    }

    public class NativeBridge {
        @JavascriptInterface
        public void setFullscreen(boolean enabled) {
            runOnUiThread(() -> setImmersiveMode(enabled));
        }

        @JavascriptInterface
        public void keepAwake(boolean enabled) {
            runOnUiThread(() -> {
                if (enabled) getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                else getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            });
        }

        @JavascriptInterface
        public void toast(String message) {
            runOnUiThread(() -> Toast.makeText(LegacyWebViewActivity.this, message, Toast.LENGTH_SHORT).show());
        }

        @JavascriptInterface
        public void haptic(String type) {
            runOnUiThread(() -> {
                try {
                    getWindow().getDecorView().performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                    Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    if (vibrator != null) {
                        long ms = "heavy".equals(type) ? 28L : 12L;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            vibrator.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE));
                        } else {
                            vibrator.vibrate(ms);
                        }
                    }
                } catch (Exception ignored) {}
            });
        }

        @JavascriptInterface
        public String getVersion() {
            try {
                PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
                return info.versionName == null ? "4.3.0" : info.versionName;
            } catch (Exception e) {
                return "4.5.2";
            }
        }

        @JavascriptInterface
        public void share(String title, String text, String url) {
            runOnUiThread(() -> {
                try {
                    Intent send = new Intent(Intent.ACTION_SEND);
                    send.setType("text/plain");
                    send.putExtra(Intent.EXTRA_SUBJECT, title == null ? "Dramaku" : title);
                    String safeUrl = (url != null && isSafeExternalUrl(url)) ? url : "";
                    String body = (text == null ? "" : text) + (safeUrl.isEmpty() ? "" : "\n" + safeUrl);
                    send.putExtra(Intent.EXTRA_TEXT, body);
                    startActivity(Intent.createChooser(send, title == null ? "Bagikan" : title));
                } catch (Exception e) {
                    Toast.makeText(LegacyWebViewActivity.this, "Tidak ada aplikasi untuk berbagi", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @JavascriptInterface
        public void openUrl(String url) {
            runOnUiThread(() -> {
                if (!isSafeExternalUrl(url)) {
                    Toast.makeText(LegacyWebViewActivity.this, "Link tidak diizinkan", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url.trim()));
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(LegacyWebViewActivity.this, "Tidak bisa membuka link", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @JavascriptInterface
        public void clearWebViewCache() {
            runOnUiThread(() -> {
                try {
                    if (webView != null) {
                        webView.clearCache(true);
                        webView.clearHistory();
                    }
                    Toast.makeText(LegacyWebViewActivity.this, "Cache WebView dibersihkan", Toast.LENGTH_SHORT).show();
                } catch (Exception ignored) {}
            });
        }
    }

    public class NativePlayerBridge {
        @JavascriptInterface
        public void play(String url, String subtitleUrl, String title) {
            playFull(url, subtitleUrl, title, "", 1, "", 0);
        }

        @JavascriptInterface
        public void playFull(String url, String subtitleUrl, String title, String dramaId, int episode, String platform, int startPosMs) {
            final String safeSub = (subtitleUrl != null && isSafeMediaUrl(subtitleUrl)) ? subtitleUrl : "";
            final String safeTitle = title == null ? "Dramaku" : title;
            final String safeDramaId = dramaId == null ? "" : dramaId;
            final String safePlatform = platform == null ? "" : platform;
            final int safeEpisode = Math.max(1, episode);
            final long safeStart = Math.max(0L, (long) startPosMs);
            runOnUiThread(() -> {
                if (!isSafeMediaUrl(url)) {
                    Toast.makeText(LegacyWebViewActivity.this, "URL video tidak valid", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    Intent i = new Intent(LegacyWebViewActivity.this, PlayerActivity.class);
                    i.putExtra(PlayerActivity.EXTRA_URL, url);
                    i.putExtra(PlayerActivity.EXTRA_SUBTITLE, safeSub);
                    i.putExtra(PlayerActivity.EXTRA_TITLE, safeTitle);
                    i.putExtra(PlayerActivity.EXTRA_DRAMA_ID, safeDramaId);
                    i.putExtra(PlayerActivity.EXTRA_EPISODE, safeEpisode);
                    i.putExtra(PlayerActivity.EXTRA_PLATFORM, safePlatform);
                    i.putExtra(PlayerActivity.EXTRA_START_POS, safeStart);
                    startActivityForResult(i, PLAYER_REQUEST);
                } catch (Exception e) {
                    Toast.makeText(LegacyWebViewActivity.this, "Gagal membuka native player", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != PLAYER_REQUEST || resultCode != RESULT_OK || data == null || webView == null) return;
        try {
            String dramaId = data.getStringExtra(PlayerActivity.RESULT_DRAMA_ID);
            int episode = data.getIntExtra(PlayerActivity.RESULT_EPISODE, 1);
            String platform = data.getStringExtra(PlayerActivity.RESULT_PLATFORM);
            long position = data.getLongExtra(PlayerActivity.RESULT_POSITION, 0L);
            long duration = data.getLongExtra(PlayerActivity.RESULT_DURATION, 0L);
            boolean ended = data.getBooleanExtra(PlayerActivity.RESULT_ENDED, false);
            double posSec = Math.max(0, position / 1000.0);
            double durSec = Math.max(0, duration / 1000.0);
            String js = "window.onNativePlayerResult&&window.onNativePlayerResult("
                    + toJsString(dramaId) + ","
                    + episode + ","
                    + toJsString(platform) + ","
                    + posSec + ","
                    + durSec + ","
                    + (ended ? "true" : "false")
                    + ")";
            webView.post(() -> webView.evaluateJavascript(js, null));
        } catch (Exception ignored) {}
    }

    private String toJsString(String value) {
        if (value == null) value = "";
        StringBuilder sb = new StringBuilder(value.length() + 16);
        sb.append('"');
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '\\') {
                sb.append('\\').append('\\');
            } else if (c == '"') {
                sb.append('\\').append('"');
            } else if (c == '\n') {
                sb.append('\\').append('n');
            } else if (c == '\r') {
                sb.append('\\').append('r');
            } else if (c == '\t') {
                sb.append('\\').append('t');
            } else if (c < 0x20) {
                sb.append('\\').append('u');
                String hex = Integer.toHexString(c);
                for (int z = hex.length(); z < 4; z++) sb.append('0');
                sb.append(hex);
            } else {
                sb.append(c);
            }
        }
        sb.append('"');
        return sb.toString();
    }
}
