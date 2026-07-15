package com.dramaku.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class PlatformRemoteState(
    val enabled: Boolean = true,
    val status: String = "active",
    val reason: String = "Aktif"
)

data class RemoteMessage(
    val enabled: Boolean = false,
    val type: String = "info",
    val title: String = "",
    val text: String = ""
)

data class NativeRemoteConfig(
    val version: Int = 0,
    val updatedAt: String = "",
    val minAppVersion: String = "",
    val latestVersion: String = "",
    val downloadUrl: String = "",
    val message: RemoteMessage = RemoteMessage(),
    val platforms: Map<String, PlatformRemoteState> = emptyMap(),
    val source: String = "default"
) {
    fun platform(id: String): PlatformRemoteState = platforms[id] ?: PlatformRemoteState()
    fun isPlatformEnabled(id: String): Boolean = platform(id).enabled
}

class RemoteConfigRepository(
    private val url: String = "https://raw.githubusercontent.com/SanzzAza/dramaku/main/remote-config.json"
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .callTimeout(16, TimeUnit.SECONDS)
        .build()

    suspend fun load(): NativeRemoteConfig = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "DramakuNative/RemoteConfig")
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) error("Remote config HTTP ${response.code}")
            parse(JSONObject(response.body?.string().orEmpty())).copy(source = "remote")
        }
    }

    private fun parse(json: JSONObject): NativeRemoteConfig {
        val msgJson = json.optJSONObject("message") ?: JSONObject()
        val msg = RemoteMessage(
            enabled = msgJson.optBoolean("enabled", false),
            type = msgJson.optString("type", "info"),
            title = msgJson.optString("title", ""),
            text = msgJson.optString("text", "")
        )
        val platformsJson = json.optJSONObject("platforms") ?: JSONObject()
        val platforms = mutableMapOf<String, PlatformRemoteState>()
        platformsJson.keys().forEach { key ->
            val p = platformsJson.optJSONObject(key) ?: JSONObject()
            platforms[key] = PlatformRemoteState(
                enabled = p.optBoolean("enabled", true),
                status = p.optString("status", if (p.optBoolean("enabled", true)) "active" else "maintenance"),
                reason = p.optString("reason", if (p.optBoolean("enabled", true)) "Aktif" else "Maintenance")
            )
        }
        val update = json.optJSONObject("update")
        return NativeRemoteConfig(
            version = json.optInt("version", 0),
            updatedAt = json.optString("updatedAt", ""),
            minAppVersion = json.optString("minAppVersion", ""),
            latestVersion = update?.optString("latestVersion")?.takeIf { it.isNotBlank() } ?: json.optString("latestVersion", ""),
            downloadUrl = update?.optString("downloadUrl")?.takeIf { it.isNotBlank() } ?: json.optString("downloadUrl", ""),
            message = msg,
            platforms = platforms
        )
    }
}
