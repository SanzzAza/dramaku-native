# Keep activity entry points
-keep class com.dramaku.app.MainActivity { *; }
-keep class com.dramaku.app.PlayerActivity { *; }
-keep class com.dramaku.app.SplashActivity { *; }

# Media3 / ExoPlayer
-dontwarn androidx.media3.**
-keep class androidx.media3.** { *; }

# OkHttp / Kotlin metadata warnings are safe for this app build.
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn kotlin.**
