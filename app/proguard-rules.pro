# Keep JS bridge methods used by the WebView UI.
-keepclassmembers class com.dramaku.app.MainActivity$NativeBridge {
    @android.webkit.JavascriptInterface <methods>;
}
-keepclassmembers class com.dramaku.app.MainActivity$NativePlayerBridge {
    @android.webkit.JavascriptInterface <methods>;
}

# Keep activity entry points
-keep class com.dramaku.app.MainActivity { *; }
-keep class com.dramaku.app.PlayerActivity { *; }
-keep class com.dramaku.app.SplashActivity { *; }

# Media3 / ExoPlayer
-dontwarn androidx.media3.**
-keep class androidx.media3.** { *; }

# Appcompat / webkit
-dontwarn androidx.webkit.**
-keep class androidx.webkit.** { *; }
