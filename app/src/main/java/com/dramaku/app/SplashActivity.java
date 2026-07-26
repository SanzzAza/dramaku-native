package com.dramaku.app;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.setStatusBarColor(Color.BLACK);
        window.setNavigationBarColor(Color.BLACK);
        window.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        );

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(dp(32), dp(32), dp(32), dp(32));
        
        // Premium gradient background
        GradientDrawable bg = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{
                        Color.rgb(5, 8, 13),
                        Color.rgb(4, 24, 18),
                        Color.rgb(6, 32, 25),
                        Color.rgb(5, 8, 13)
                }
        );
        root.setBackground(bg);

        // Logo - kept as is
        ImageView logo = new ImageView(this);
        logo.setImageResource(R.mipmap.ic_launcher);
        LinearLayout.LayoutParams logoParams = new LinearLayout.LayoutParams(dp(120), dp(120));
        logoParams.bottomMargin = dp(20);
        root.addView(logo, logoParams);

        // Title with better typography
        TextView title = new TextView(this);
        title.setText("Dramaku");
        title.setTextColor(Color.rgb(239, 255, 247));
        title.setTextSize(36);
        title.setLetterSpacing(0.08f);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        root.addView(title, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        // Tagline - more catchy
        TextView subtitle = new TextView(this);
        subtitle.setText("Nonton Drama Tanpa Batas");
        subtitle.setTextColor(Color.rgb(52, 211, 153)); // Accent green
        subtitle.setTextSize(14);
        subtitle.setLetterSpacing(0.15f);
        subtitle.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams subParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        subParams.topMargin = dp(10);
        root.addView(subtitle, subParams);

        // Loading indicator
        ProgressBar loading = new ProgressBar(this);
        loading.setIndeterminateTintList(android.content.res.ColorStateList.valueOf(Color.rgb(16, 245, 166)));
        LinearLayout.LayoutParams loadingParams = new LinearLayout.LayoutParams(dp(32), dp(32));
        loadingParams.topMargin = dp(40);
        root.addView(loading, loadingParams);

        // Version text
        TextView version = new TextView(this);
        try {
            version.setText("v" + getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
        } catch (Exception e) {
            version.setText("v5.0");
        }
        version.setTextColor(Color.rgb(100, 120, 140));
        version.setTextSize(11);
        version.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams versionParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        versionParams.topMargin = dp(24);
        root.addView(version, versionParams);

        setContentView(root);

        // Entrance animations
        logo.setAlpha(0f);
        logo.setScaleX(0.8f);
        logo.setScaleY(0.8f);
        title.setAlpha(0f);
        title.setTranslationY(20f);
        subtitle.setAlpha(0f);
        subtitle.setTranslationY(15f);
        loading.setAlpha(0f);
        version.setAlpha(0f);

        // Animate logo
        ObjectAnimator logoAlpha = ObjectAnimator.ofFloat(logo, "alpha", 0f, 1f);
        ObjectAnimator logoScaleX = ObjectAnimator.ofFloat(logo, "scaleX", 0.8f, 1f);
        ObjectAnimator logoScaleY = ObjectAnimator.ofFloat(logo, "scaleY", 0.8f, 1f);
        AnimatorSet logoAnim = new AnimatorSet();
        logoAnim.playTogether(logoAlpha, logoScaleX, logoScaleY);
        logoAnim.setDuration(600);
        logoAnim.setInterpolator(new OvershootInterpolator(1.2f));

        // Animate title
        ObjectAnimator titleAlpha = ObjectAnimator.ofFloat(title, "alpha", 0f, 1f);
        ObjectAnimator titleTranslate = ObjectAnimator.ofFloat(title, "translationY", 20f, 0f);
        AnimatorSet titleAnim = new AnimatorSet();
        titleAnim.playTogether(titleAlpha, titleTranslate);
        titleAnim.setDuration(500);
        titleAnim.setInterpolator(new DecelerateInterpolator());

        // Animate subtitle
        ObjectAnimator subAlpha = ObjectAnimator.ofFloat(subtitle, "alpha", 0f, 1f);
        ObjectAnimator subTranslate = ObjectAnimator.ofFloat(subtitle, "translationY", 15f, 0f);
        AnimatorSet subAnim = new AnimatorSet();
        subAnim.playTogether(subAlpha, subTranslate);
        subAnim.setDuration(500);
        subAnim.setInterpolator(new DecelerateInterpolator());

        // Animate loading
        ObjectAnimator loadingAlpha = ObjectAnimator.ofFloat(loading, "alpha", 0f, 1f);
        loadingAlpha.setDuration(400);

        // Animate version
        ObjectAnimator versionAlpha = ObjectAnimator.ofFloat(version, "alpha", 0f, 1f);
        versionAlpha.setDuration(400);

        // Play animations in sequence
        AnimatorSet allAnim = new AnimatorSet();
        allAnim.playSequentially(logoAnim, titleAnim, subAnim, loadingAlpha, versionAlpha);
        allAnim.start();

        // Navigate to main
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }, 2200);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
