package com.lorenzogil.whabalim

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import java.util.logging.Logger

class WhabalimApp : Application() {

    override fun onCreate() {
        super.onCreate()

        val logger = Logger.getLogger(WhabalimApp::class.java.name)
        logger.info("Creating notification channel")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mChannel = NotificationChannel(
                getString(R.string.channel_id),
                getString(R.string.channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            mChannel.description = getString(R.string.channel_description)
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        }

    }

}