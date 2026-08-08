package com.dramaku.app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

public class SplashActivity extends AppCompatActivity {

    // Palet sinematik — gelap premium
    private static final int BG = Color.rgb(10, 9, 8);        // 0x0A0908
    private static final int HI = Color.rgb(245, 240, 232);   // krem
    private static final int MUTED = Color.rgb(120, 112, 104);
    private static final int FAINT = Color.rgb(74, 68, 61);
    private static final int GREEN = Color.rgb(46, 232, 160);  // mint elektrik
    private static final int TRACK = Color.rgb(34, 30, 24);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.setStatusBarColor(BG);
        window.setNavigationBarColor(BG);
        window.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        );

        Typeface display = font(R.font.fraunces_semibold);
        Typeface displayBold = font(R.font.fraunces_bold);
        Typeface sans = font(R.font.jakarta_medium);
        Typeface sansBold = font(R.font.jakarta_semibold);

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(BG);

        // Watermark "D" besar, nyaris tak terlihat
        TextView watermark = new TextView(this);
        watermark.setText("D");
        watermark.setTypeface(displayBold);
        watermark.setTextSize(340f);
        watermark.setTextColor(Color.argb(14, 240, 235, 224));
        watermark.setGravity(Gravity.CENTER);
        root.addView(watermark, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
                Gravity.CENTER
        ));

        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setGravity(Gravity.CENTER);
        col.setPadding(dp(32), dp(32), dp(32), dp(32));

        ImageView logo = new ImageView(this);
        logo.setImageResource(R.mipmap.ic_launcher);
        LinearLayout.LayoutParams logoParams = new LinearLayout.LayoutParams(dp(88), dp(88));
        logoParams.bottomMargin = dp(24);
        col.addView(logo, logoParams);

        TextView title = new TextView(this);
        title.setText("Dramaku");
        title.setTypeface(display);
        title.setTextColor(HI);
        title.setTextSize(33);
        title.setLetterSpacing(0.01f);
        title.setGravity(Gravity.CENTER);
        col.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("Sinema pendek di genggaman");
        subtitle.setTypeface(sans);
        subtitle.setTextColor(MUTED);
        subtitle.setTextSize(13.5f);
        subtitle.setLetterSpacing(0.05f);
        subtitle.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams subParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        subParams.topMargin = dp(8);
        col.addView(subtitle, subParams);

        // Garis progres tipis — track + isi yang memanjang
        FrameLayout progressTrack = new FrameLayout(this);
        progressTrack.setBackgroundColor(TRACK);
        View progressFill = new View(this);
        progressFill.setBackgroundColor(GREEN);
        FrameLayout.LayoutParams fillParams = new FrameLayout.LayoutParams(0, FrameLayout.LayoutParams.MATCH_PARENT);
        progressTrack.addView(progressFill, fillParams);
        LinearLayout.LayoutParams trackParams = new LinearLayout.LayoutParams(dp(72), dp(2));
        trackParams.topMargin = dp(34);
        col.addView(progressTrack, trackParams);

        root.addView(col, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
                Gravity.CENTER
        ));

        // Versi di pojok bawah, tenang
        TextView version = new TextView(this);
        try {
            version.setText("v" + getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
        } catch (Exception e) {
            version.setText("");
        }
        version.setTypeface(sans);
        version.setTextColor(FAINT);
        version.setTextSize(11.5f);
        version.setGravity(Gravity.CENTER);
        FrameLayout.LayoutParams versionParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL
        );
        versionParams.bottomMargin = dp(30);
        root.addView(version, versionParams);

        setContentView(root);

        // Animasi masuk: naik pelan, tanpa pantulan
        logo.setAlpha(0f);
        logo.setTranslationY(dpF(10));
        title.setAlpha(0f);
        title.setTranslationY(dpF(10));
        subtitle.setAlpha(0f);
        version.setAlpha(0f);
        watermark.setAlpha(0f);

        logo.animate().alpha(1f).translationY(0f).setDuration(420).setInterpolator(new DecelerateInterpolator()).start();
        watermark.animate().alpha(1f).setDuration(700).setInterpolator(new DecelerateInterpolator()).start();
        AnimatorSet textAnim = new AnimatorSet();
        textAnim.playTogether(
                ObjectAnimator.ofFloat(title, "alpha", 0f, 1f),
                ObjectAnimator.ofFloat(title, "translationY", dpF(10), 0f)
        );
        textAnim.setDuration(400);
        textAnim.setStartDelay(120);
        textAnim.setInterpolator(new DecelerateInterpolator());
        textAnim.start();
        subtitle.animate().alpha(1f).setDuration(400).setStartDelay(220).setInterpolator(new DecelerateInterpolator()).start();
        version.animate().alpha(1f).setDuration(400).setStartDelay(500).start();

        ValueAnimator progress = ValueAnimator.ofInt(0, dp(72));
        progress.setDuration(1700);
        progress.setStartDelay(150);
        progress.setInterpolator(new AccelerateInterpolator(0.6f));
        progress.addUpdateListener(a -> {
            int w = (int) a.getAnimatedValue();
            FrameLayout.LayoutParams p = (FrameLayout.LayoutParams) progressFill.getLayoutParams();
            p.width = w;
            progressFill.setLayoutParams(p);
        });
        progress.start();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }, 2000);
    }

    @Nullable
    private Typeface font(int resId) {
        try {
            return ResourcesCompat.getFont(this, resId);
        } catch (Exception e) {
            return Typeface.DEFAULT;
        }
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private float dpF(int value) {
        return value * getResources().getDisplayMetrics().density;
    }
}
