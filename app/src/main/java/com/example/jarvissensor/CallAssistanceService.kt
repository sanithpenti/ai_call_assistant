package com.example.jarvissensor

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat

class CallAssistantService : Service() {

    companion object {

        private const val CHANNEL_ID = "jarvis_call_channel"
        private const val NOTIFICATION_ID = 1001

    }

    override fun onCreate() {
        super.onCreate()

        println(
            "Jarvis Service: Call Assistant Service created."
        )

        createNotificationChannel()

        val notification =
            createNotification()

        startForeground(
            NOTIFICATION_ID,
            notification
        )

        println(
            "Jarvis Service: Running in foreground."
        )
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        println(
            "Jarvis Service: Incoming call service started."
        )

        /*
         * We will move the speech recognition,
         * ML prediction and call answering
         * into this service in the next step.
         */

        return START_NOT_STICKY
    }

    private fun createNotificationChannel() {

        val channel =
            NotificationChannel(
                CHANNEL_ID,
                "Jarvis Call Assistant",
                NotificationManager.IMPORTANCE_LOW
            )

        val notificationManager =
            getSystemService(
                NotificationManager::class.java
            )

        notificationManager.createNotificationChannel(
            channel
        )
    }

    private fun createNotification(): Notification {

        return NotificationCompat.Builder(
            this,
            CHANNEL_ID
        )
            .setContentTitle(
                "Jarvis Call Assistant"
            )
            .setContentText(
                "Jarvis is ready for incoming calls."
            )
            .setSmallIcon(
                android.R.drawable.ic_btn_speak_now
            )
            .setOngoing(true)
            .build()
    }

    override fun onBind(
        intent: Intent?
    ): IBinder? {

        return null
    }
}