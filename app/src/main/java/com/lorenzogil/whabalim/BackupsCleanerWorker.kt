package com.lorenzogil.whabalim

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.text.SimpleDateFormat
import java.util.*


class BackupsCleanerWorker(appContext: Context, workerParams: WorkerParameters)
    : Worker(appContext, workerParams) {

    override fun doWork(): Result {
        val context = applicationContext

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

        val days = getDays()
        val backupsCleaner = BackupsCleaner()
        val files = backupsCleaner.getBackupFiles()
        val result = backupsCleaner.deleteBackups(days, files)

        if (result > 0) {
            val now = Date()
            val time = SimpleDateFormat("HH:mm:ss", Locale.US).format(now)
            val title = "WhatsaApp Backups Cleaner: %d files removed".format(result)
            val text = "%d files delete at %s to keep %d days of backups".format(result, time, days)
            val builder =
                NotificationCompat.Builder(context, context.getString(R.string.channel_id))
                    .setSmallIcon(R.drawable.delete_icon)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)

            with(NotificationManagerCompat.from(context)) {
                val notificationId =
                    Integer.parseInt(SimpleDateFormat("ddHHmmss", Locale.US).format(now))
                notify(notificationId, builder.build())
            }
        }

        return Result.success()
    }

    private fun getDays(): Int {
        val ctx = applicationContext
        val preferences = ctx.getSharedPreferences(ctx.getString(R.string.preferences_file), Context.MODE_PRIVATE)
        val defaultDays = ctx.resources.getInteger(R.integer.default_days)
        return preferences.getInt(ctx.getString(R.string.preference_days), defaultDays)
    }
}