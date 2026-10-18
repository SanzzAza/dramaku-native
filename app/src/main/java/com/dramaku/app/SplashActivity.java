package com.dramaku.app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

public class SplashActivity extends AppCompatActivity {

    private static final int BG = Color.BLACK;
    private static final int HI = Color.rgb(245, 245, 245);
    private static final int MUTED = Color.rgb(146, 146, 146);
    private static final int FAINT = Color.rgb(80, 80, 80);
    private static final int YELLOW = Color.rgb(255, 229, 0);
    private static final int TRACK = Color.rgb(30, 30, 30);

    private final Handler handler = new Handler(Looper.getMainLooper());
    private ValueAnimator haloPulse;
    private ObjectAnimator orbitAnimator;
    private boolean openingMain;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        configureWindow();

        Typeface medium = font(R.font.jakarta_medium);
        Typeface semibold = font(R.font.jakarta_semibold);
        Typeface bold = font(R.font.jakarta_bold);

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(BG);

        View ambientGlow = new View(this);
        GradientDrawable glowDrawable = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{Color.argb(58, 255, 229, 0), Color.argb(0, 255, 229, 0)}
        );
        glowDrawable.setShape(GradientDrawable.OVAL);
        glowDrawable.setGradientType(GradientDrawable.RADIAL_GRADIENT);
        glowDrawable.setGradientRadius(dpF(150));
        ambientGlow.setBackground(glowDrawable);
        FrameLayout.LayoutParams glowParams = new FrameLayout.LayoutParams(dp(300), dp(300), Gravity.CENTER);
        glowParams.topMargin = -dp(44);
        root.addView(ambientGlow, glowParams);

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER_HORIZONTAL);
        content.setPadding(dp(28), dp(24), dp(28), dp(24));

        FrameLayout logoStage = new FrameLayout(this);
        LinearLayout.LayoutParams stageParams = new LinearLayout.LayoutParams(dp(164), dp(164));
        content.addView(logoStage, stageParams);

        View outerRing = new View(this);
        outerRing.setBackground(circle(Color.TRANSPARENT, Color.argb(92, 255, 229, 0), 1));
        logoStage.addView(outerRing, centered(dp(136), dp(136)));

        FrameLayout orbitLayer = new FrameLayout(this);
        logoStage.addView(orbitLayer, centered(dp(148), dp(148)));

        View orbitDot = new View(this);
        orbitDot.setBackground(circle(YELLOW, Color.TRANSPARENT, 0));
        FrameLayout.LayoutParams dotParams = new FrameLayout.LayoutParams(dp(9), dp(9), Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        dotParams.topMargin = dp(1);
        orbitLayer.addView(orbitDot, dotParams);

        View innerHalo = new View(this);
        innerHalo.setBackground(circle(Color.argb(28, 255, 229, 0), Color.argb(120, 255, 229, 0), 1));
        logoStage.addView(innerHalo, centered(dp(108), dp(108)));

        FrameLayout logoTile = new FrameLayout(this);
        logoTile.setBackground(rounded(YELLOW, dpF(28)));
        logoTile.setElevation(dpF(10));
        logoStage.addView(logoTile, centered(dp(84), dp(84)));

        TextView playMark = new TextView(this);
        playMark.setText("▶");
        playMark.setTextColor(Color.BLACK);
        playMark.setTextSize(32);
        playMark.setTypeface(bold);
        playMark.setGravity(Gravity.CENTER);
        playMark.setContentDescription("Dramaku");
        playMark.setTranslationX(dpF(2));
        logoTile.addView(playMark, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));

        TextView title = new TextView(this);
        title.setText("DRAMAKU");
        title.setTypeface(bold);
        title.setTextColor(HI);
        title.setTextSize(29);
        title.setLetterSpacing(0.16f);
        title.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        titleParams.topMargin = dp(12);
        content.addView(title, titleParams);

        TextView subtitle = new TextView(this);
        subtitle.setText("CERITA TANPA BATAS");
        subtitle.setTypeface(medium);
        subtitle.setTextColor(MUTED);
        subtitle.setTextSize(10.5f);
        subtitle.setLetterSpacing(0.20f);
        subtitle.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams subtitleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        subtitleParams.topMargin = dp(8);
        content.addView(subtitle, subtitleParams);

        FrameLayout progressTrack = new FrameLayout(this);
        progressTrack.setBackground(rounded(TRACK, dpF(50)));
        View progressFill = new View(this);
        progressFill.setBackground(rounded(YELLOW, dpF(50)));
        progressFill.setScaleX(0f);
        progressFill.setPivotX(0f);
        progressTrack.addView(progressFill, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));
        LinearLayout.LayoutParams trackParams = new LinearLayout.LayoutParams(dp(118), dp(3));
        trackParams.topMargin = dp(34);
        content.addView(progressTrack, trackParams);

        FrameLayout.LayoutParams contentParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER
        );
        contentParams.topMargin = -dp(22);
        root.addView(content, contentParams);

        TextView loadingLabel = new TextView(this);
        loadingLabel.setText("MENYIAPKAN TONTONAN");
        loadingLabel.setTypeface(semibold);
        loadingLabel.setTextColor(FAINT);
        loadingLabel.setTextSize(9.5f);
        loadingLabel.setLetterSpacing(0.14f);
        loadingLabel.setGravity(Gravity.CENTER);
        FrameLayout.LayoutParams loadingParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL
        );
        loadingParams.bottomMargin = dp(52);
        root.addView(loadingLabel, loadingParams);

        TextView version = new TextView(this);
        try {
            version.setText("v" + getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
        } catch (Exception ignored) {
            version.setText("");
        }
        version.setTypeface(medium);
        version.setTextColor(FAINT);
        version.setTextSize(9.5f);
        version.setGravity(Gravity.CENTER);
        FrameLayout.LayoutParams versionParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL
        );
        versionParams.bottomMargin = dp(27);
        root.addView(version, versionParams);

        setContentView(root);
        runEntranceAnimation(content, logoTile, outerRing, innerHalo, orbitLayer,
                ambientGlow, title, subtitle, progressFill, loadingLabel, version);
    }

    private void configureWindow() {
        Window window = getWindow();
        window.setStatusBarColor(BG);
        window.setNavigationBarColor(BG);
        window.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        );
    }

    private void runEntranceAnimation(
            View content,
            View logoTile,
            View outerRing,
            View innerHalo,
            View orbitLayer,
            View ambientGlow,
            View title,
            View subtitle,
            View progressFill,
            View loadingLabel,
            View version
    ) {
        ambientGlow.setAlpha(0f);
        ambientGlow.setScaleX(0.55f);
        ambientGlow.setScaleY(0.55f);
        outerRing.setAlpha(0f);
        outerRing.setScaleX(0.55f);
        outerRing.setScaleY(0.55f);
        innerHalo.setAlpha(0f);
        innerHalo.setScaleX(0.72f);
        innerHalo.setScaleY(0.72f);
        logoTile.setAlpha(0f);
        logoTile.setScaleX(0.45f);
        logoTile.setScaleY(0.45f);
        logoTile.setRotation(-14f);
        title.setAlpha(0f);
        title.setTranslationY(dpF(16));
        subtitle.setAlpha(0f);
        subtitle.setTranslationY(dpF(10));
        loadingLabel.setAlpha(0f);
        version.setAlpha(0f);

        AnimatorSet glowIn = new AnimatorSet();
        glowIn.playTogether(
                ObjectAnimator.ofFloat(ambientGlow, View.ALPHA, 0f, 1f),
                ObjectAnimator.ofFloat(ambientGlow, View.SCALE_X, 0.55f, 1f),
                ObjectAnimator.ofFloat(ambientGlow, View.SCALE_Y, 0.55f, 1f),
                ObjectAnimator.ofFloat(outerRing, View.ALPHA, 0f, 1f),
                ObjectAnimator.ofFloat(outerRing, View.SCALE_X, 0.55f, 1f),
                ObjectAnimator.ofFloat(outerRing, View.SCALE_Y, 0.55f, 1f),
                ObjectAnimator.ofFloat(innerHalo, View.ALPHA, 0f, 1f),
                ObjectAnimator.ofFloat(innerHalo, View.SCALE_X, 0.72f, 1f),
                ObjectAnimator.ofFloat(innerHalo, View.SCALE_Y, 0.72f, 1f)
        );
        glowIn.setDuration(620);
        glowIn.setInterpolator(new DecelerateInterpolator(1.8f));
        glowIn.start();

        AnimatorSet markIn = new AnimatorSet();
        markIn.playTogether(
                ObjectAnimator.ofFloat(logoTile, View.ALPHA, 0f, 1f),
                ObjectAnimator.ofFloat(logoTile, View.SCALE_X, 0.45f, 1f),
                ObjectAnimator.ofFloat(logoTile, View.SCALE_Y, 0.45f, 1f),
                ObjectAnimator.ofFloat(logoTile, View.ROTATION, -14f, 0f)
        );
        markIn.setStartDelay(70);
        markIn.setDuration(620);
        markIn.setInterpolator(new OvershootInterpolator(0.85f));
        markIn.start();

        AnimatorSet titleIn = new AnimatorSet();
        titleIn.playTogether(
                ObjectAnimator.ofFloat(title, View.ALPHA, 0f, 1f),
                ObjectAnimator.ofFloat(title, View.TRANSLATION_Y, dpF(16), 0f)
        );
        titleIn.setStartDelay(300);
        titleIn.setDuration(430);
        titleIn.setInterpolator(new DecelerateInterpolator());
        titleIn.start();

        AnimatorSet subtitleIn = new AnimatorSet();
        subtitleIn.playTogether(
                ObjectAnimator.ofFloat(subtitle, View.ALPHA, 0f, 1f),
                ObjectAnimator.ofFloat(subtitle, View.TRANSLATION_Y, dpF(10), 0f)
        );
        subtitleIn.setStartDelay(440);
        subtitleIn.setDuration(380);
        subtitleIn.setInterpolator(new DecelerateInterpolator());
        subtitleIn.start();

        progressFill.post(() -> {
            progressFill.setPivotX(0f);
            progressFill.animate()
                    .scaleX(1f)
                    .setStartDelay(420)
                    .setDuration(1120)
                    .setInterpolator(new DecelerateInterpolator(1.15f))
                    .start();
        });

        loadingLabel.animate().alpha(1f).setStartDelay(560).setDuration(360).start();
        version.animate().alpha(1f).setStartDelay(680).setDuration(360).start();

        orbitAnimator = ObjectAnimator.ofFloat(orbitLayer, View.ROTATION, -55f, 305f);
        orbitAnimator.setStartDelay(100);
        orbitAnimator.setDuration(1500);
        orbitAnimator.setInterpolator(new DecelerateInterpolator(1.25f));
        orbitAnimator.start();

        haloPulse = ValueAnimator.ofFloat(0.32f, 0.78f, 0.32f);
        haloPulse.setStartDelay(680);
        haloPulse.setDuration(1150);
        haloPulse.setRepeatCount(ValueAnimator.INFINITE);
        haloPulse.addUpdateListener(animator -> outerRing.setAlpha((float) animator.getAnimatedValue()));
        haloPulse.start();

        handler.postDelayed(() -> launchMain(content, ambientGlow), 1660);
    }

    private void launchMain(View content, View ambientGlow) {
        if (openingMain || isFinishing()) return;
        openingMain = true;

        AnimatorSet exit = new AnimatorSet();
        exit.playTogether(
                ObjectAnimator.ofFloat(content, View.ALPHA, 1f, 0f),
                ObjectAnimator.ofFloat(content, View.SCALE_X, 1f, 1.06f),
                ObjectAnimator.ofFloat(content, View.SCALE_Y, 1f, 1.06f),
                ObjectAnimator.ofFloat(ambientGlow, View.ALPHA, 1f, 0f),
                ObjectAnimator.ofFloat(ambientGlow, View.SCALE_X, 1f, 1.18f),
                ObjectAnimator.ofFloat(ambientGlow, View.SCALE_Y, 1f, 1.18f)
        );
        exit.setDuration(260);
        exit.setInterpolator(new DecelerateInterpolator());
        exit.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            }
        });
        exit.start();
    }

    private FrameLayout.LayoutParams centered(int width, int height) {
        return new FrameLayout.LayoutParams(width, height, Gravity.CENTER);
    }

    private GradientDrawable circle(int fillColor, int strokeColor, int strokeDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(fillColor);
        if (strokeDp > 0) drawable.setStroke(dp(strokeDp), strokeColor);
        return drawable;
    }

    private GradientDrawable rounded(int color, float radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(radius);
        return drawable;
    }

    @Nullable
    private Typeface font(int resId) {
        try {
            return ResourcesCompat.getFont(this, resId);
        } catch (Exception ignored) {
            return Typeface.DEFAULT;
        }
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private float dpF(int value) {
        return value * getResources().getDisplayMetrics().density;
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        if (haloPulse != null) haloPulse.cancel();
        if (orbitAnimator != null) orbitAnimator.cancel();
        super.onDestroy();
    }
}
