package com.dramaku.app.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Pengingat "lanjut nonton": dijadwalkan ulang setiap sesi pemutar ditutup,
 * lalu menampilkan satu notifikasi bila episode terakhir belum selesai.
 *
 * Worker membaca SharedPreferences langsung (bukan LocalStore yang file-private
 * di MainActivity) supaya tetap berjalan tanpa proses UI.
 */
object ResumeReminder {
    private const val WORK_NAME = "resume_reminder"
    const val CHANNEL_ID = "resume_reminder"
    const val PREF_ENABLED = "resumeReminder"

    fun schedule(ctx: Context, delayHours: Long = 24L) {
        val request = OneTimeWorkRequestBuilder<ResumeReminderWorker>()
            .setInitialDelay(delayHours, TimeUnit.HOURS)
            .build()
        WorkManager.getInstance(ctx).enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.REPLACE, request)
    }

    fun cancel(ctx: Context) {
        WorkManager.getInstance(ctx).cancelUniqueWork(WORK_NAME)
    }
}

class ResumeReminderWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        val ctx = applicationContext
        val prefs = ctx.getSharedPreferences("dramaku_native", Context.MODE_PRIVATE)
        if (!prefs.getBoolean(ResumeReminder.PREF_ENABLED, false)) return Result.success()
        if (!NotificationManagerCompat.from(ctx).areNotificationsEnabled()) return Result.success()

        val latest: JSONObject = runCatching {
            val arr = JSONArray(prefs.getString("history", "[]") ?: "[]")
            (0 until arr.length()).mapNotNull { arr.optJSONObject(it) }.maxByOrNull { it.optLong("updated", 0L) }
        }.getOrNull() ?: return Result.success()

        val title = latest.optString("title")
        if (title.isBlank()) return Result.success()
        val episode = latest.optInt("episode", 1)
        val pos = latest.optLong("pos", 0L)
        val dur = latest.optLong("dur", 0L)
        // Episode yang sudah hampir tamat tidak perlu diingatkan.
        if (dur > 0L && pos >= dur - 5_000L) return Result.success()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(
                NotificationChannel(ResumeReminder.CHANNEL_ID, "Pengingat lanjut nonton", NotificationManager.IMPORTANCE_DEFAULT).apply {
                    description = "Mengingatkan episode yang belum selesai ditonton."
                }
            )
        }

        val launch = ctx.packageManager.getLaunchIntentForPackage(ctx.packageName) ?: return Result.success()
        val pending = PendingIntent.getActivity(
            ctx, 0, launch, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val pct = if (dur > 0L) " · ${((pos * 100) / dur).toInt()}%" else ""
        val notification = NotificationCompat.Builder(ctx, ResumeReminder.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle("Lanjutkan nonton?")
            .setContentText("$title · Ep. $episode$pct")
            .setStyle(NotificationCompat.BigTextStyle().bigText("$title masih menunggumu di Ep. $episode$pct. Ketuk untuk melanjutkan dari posisi terakhir."))
            .setAutoCancel(true)
            .setContentIntent(pending)
            .build()
        runCatching { NotificationManagerCompat.from(ctx).notify(1001, notification) }
        return Result.success()
    }
}
