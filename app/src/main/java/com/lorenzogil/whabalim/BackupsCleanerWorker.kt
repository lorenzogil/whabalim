package com.lorenzogil.whabalim

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.util.logging.Logger
import java.text.SimpleDateFormat
import java.util.*


class BackupsCleanerWorker(appContext: Context, workerParams: WorkerParameters)
    : Worker(appContext, workerParams) {

    override fun doWork(): Result {
        val logger = Logger.getLogger(BackupsCleanerWorker::class.java.name)
        logger.info("Doing work on the periodic worker")

        val context = applicationContext

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

        val now = Date()
        val builder = NotificationCompat.Builder(context, context.getString(R.string.channel_id))
            .setSmallIcon(R.drawable.delete_icon)
            .setContentTitle("Backups cleaner worker")
            .setContentText("Backups cleaned at " + SimpleDateFormat("HH:mm:ss", Locale.US).format(now))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with (NotificationManagerCompat.from(context)) {
            val notificationId = Integer.parseInt(SimpleDateFormat("ddHHmmss", Locale.US).format(now))
            logger.info("Displaying notification " + notificationId.toString())
            notify(notificationId, builder.build())
        }

        return Result.success()
    }
}