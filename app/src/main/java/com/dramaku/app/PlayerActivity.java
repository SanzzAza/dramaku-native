package com.dramaku.app;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MimeTypes;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

import java.util.Collections;

public class PlayerActivity extends AppCompatActivity {
    public static final String EXTRA_URL = "url";
    public static final String EXTRA_SUBTITLE = "subtitle";
    public static final String EXTRA_TITLE = "title";
    public static final String EXTRA_DRAMA_ID = "dramaId";
    public static final String EXTRA_EPISODE = "episode";
    public static final String EXTRA_PLATFORM = "platform";
    public static final String EXTRA_START_POS = "startPos";

    public static final String RESULT_DRAMA_ID = "dramaId";
    public static final String RESULT_EPISODE = "episode";
    public static final String RESULT_PLATFORM = "platform";
    public static final String RESULT_POSITION = "position";
    public static final String RESULT_DURATION = "duration";
    public static final String RESULT_ENDED = "ended";

    private ExoPlayer player;
    private PlayerView playerView;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean ended = false;
    private boolean resultSent = false;

    private String dramaId = "";
    private int episode = 1;
    private String platform = "";

    private final Runnable progressTick = new Runnable() {
        @Override
        public void run() {
            // Keep session warm; actual save happens on pause/stop/destroy.
            if (player != null && player.isPlaying()) {
                handler.postDelayed(this, 5000);
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.BLACK);
        getWindow().setNavigationBarColor(Color.BLACK);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setImmersiveMode(true);

        playerView = new PlayerView(this);
        playerView.setBackgroundColor(Color.BLACK);
        playerView.setUseController(true);
        playerView.setControllerAutoShow(true);
        setContentView(playerView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));

        Intent in = getIntent();
        String url = in.getStringExtra(EXTRA_URL);
        String subtitle = in.getStringExtra(EXTRA_SUBTITLE);
        String title = in.getStringExtra(EXTRA_TITLE);
        dramaId = safe(in.getStringExtra(EXTRA_DRAMA_ID));
        platform = safe(in.getStringExtra(EXTRA_PLATFORM));
        episode = Math.max(1, in.getIntExtra(EXTRA_EPISODE, 1));
        long startPos = Math.max(0, in.getLongExtra(EXTRA_START_POS, 0L));

        if (url == null || url.trim().isEmpty()) {
            Toast.makeText(this, "URL video kosong", Toast.LENGTH_SHORT).show();
            finishWithResult(false);
            return;
        }

        if (title != null && !title.trim().isEmpty()) {
            setTitle(title);
        }

        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);
        player.setMediaItem(buildMediaItem(url, subtitle));
        player.prepare();
        if (startPos > 0) {
            player.seekTo(startPos);
        }
        player.play();

        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (playbackState == Player.STATE_ENDED) {
                    ended = true;
                    finishWithResult(true);
                }
            }
        });
        handler.postDelayed(progressTick, 5000);
    }

    private String safe(String v) {
        return v == null ? "" : v;
    }

    private MediaItem buildMediaItem(String url, String subtitle) {
        MediaItem.Builder builder = new MediaItem.Builder().setUri(Uri.parse(url));
        if (subtitle != null && !subtitle.trim().isEmpty()) {
            String lower = subtitle.toLowerCase();
            String mime = lower.endsWith(".vtt") ? MimeTypes.TEXT_VTT : MimeTypes.APPLICATION_SUBRIP;
            MediaItem.SubtitleConfiguration sub = new MediaItem.SubtitleConfiguration.Builder(Uri.parse(subtitle))
                    .setMimeType(mime)
                    .setLanguage("id")
                    .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
                    .build();
            builder.setSubtitleConfigurations(Collections.singletonList(sub));
        }
        return builder.build();
    }

    private void setImmersiveMode(boolean enabled) {
        if (!enabled) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            return;
        }
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );
    }

    private void finishWithResult(boolean markEnded) {
        if (resultSent) {
            if (!isFinishing()) finish();
            return;
        }
        resultSent = true;
        long pos = 0L;
        long dur = 0L;
        try {
            if (player != null) {
                pos = Math.max(0L, player.getCurrentPosition());
                long d = player.getDuration();
                dur = d > 0 ? d : 0L;
            }
        } catch (Exception ignored) {}

        Intent data = new Intent();
        data.putExtra(RESULT_DRAMA_ID, dramaId);
        data.putExtra(RESULT_EPISODE, episode);
        data.putExtra(RESULT_PLATFORM, platform);
        data.putExtra(RESULT_POSITION, pos);
        data.putExtra(RESULT_DURATION, dur);
        data.putExtra(RESULT_ENDED, markEnded || ended);
        setResult(RESULT_OK, data);
        if (!isFinishing()) finish();
    }

    @Override
    public void onBackPressed() {
        finishWithResult(false);
    }

    @Override
    protected void onPause() {
        if (player != null) player.pause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(progressTick);
        if (!resultSent) {
            // Best-effort progress save without calling finish() again.
            try {
                long pos = 0L, dur = 0L;
                if (player != null) {
                    pos = Math.max(0L, player.getCurrentPosition());
                    long d = player.getDuration();
                    dur = d > 0 ? d : 0L;
                }
                Intent data = new Intent();
                data.putExtra(RESULT_DRAMA_ID, dramaId);
                data.putExtra(RESULT_EPISODE, episode);
                data.putExtra(RESULT_PLATFORM, platform);
                data.putExtra(RESULT_POSITION, pos);
                data.putExtra(RESULT_DURATION, dur);
                data.putExtra(RESULT_ENDED, ended);
                setResult(RESULT_OK, data);
                resultSent = true;
            } catch (Exception ignored) {}
        }
        if (player != null) {
            try { player.release(); } catch (Exception ignored) {}
            player = null;
        }
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onDestroy();
    }
}
