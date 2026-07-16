package com.dramaku.app.data

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RemoteConfigRepositoryTest {
    private val repository = RemoteConfigRepository("https://example.invalid/config.json")

    @Test
    fun `parse reads nested update and platform maintenance state`() {
        val config = repository.parse(
            JSONObject(
                """
                {
                  "version": 20,
                  "updatedAt": "2026-07-16",
                  "minAppVersion": "4.7.0",
                  "message": {
                    "enabled": true,
                    "type": "warning",
                    "title": "Info",
                    "text": "Maintenance singkat"
                  },
                  "update": {
                    "latestVersion": "4.7.1",
                    "downloadUrl": "https://example.com/dramaku.apk"
                  },
                  "platforms": {
                    "melolo": {
                      "enabled": false,
                      "status": "maintenance",
                      "reason": "Perbaikan server"
                    }
                  }
                }
                """.trimIndent()
            )
        )

        assertEquals(20, config.version)
        assertEquals("4.7.0", config.minAppVersion)
        assertEquals("4.7.1", config.latestVersion)
        assertEquals("https://example.com/dramaku.apk", config.downloadUrl)
        assertTrue(config.message.enabled)
        assertFalse(config.isPlatformEnabled("melolo"))
        assertEquals("Perbaikan server", config.platform("melolo").reason)
    }

    @Test
    fun `parse keeps safe defaults for missing optional fields`() {
        val config = repository.parse(JSONObject("{}"))

        assertEquals(0, config.version)
        assertFalse(config.message.enabled)
        assertTrue(config.isPlatformEnabled("unknown"))
        assertEquals("active", config.platform("unknown").status)
    }

    @Test
    fun `nested update values fall back to root values when blank`() {
        val config = repository.parse(
            JSONObject(
                """
                {
                  "latestVersion": "4.7.1",
                  "downloadUrl": "https://example.com/root.apk",
                  "update": {
                    "latestVersion": "",
                    "downloadUrl": ""
                  }
                }
                """.trimIndent()
            )
        )

        assertEquals("4.7.1", config.latestVersion)
        assertEquals("https://example.com/root.apk", config.downloadUrl)
    }
}
