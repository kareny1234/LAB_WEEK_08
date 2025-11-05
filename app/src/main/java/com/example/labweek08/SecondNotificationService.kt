package com.example.labweek08

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.CountDownTimer
import android.util.Log
import androidx.core.app.NotificationCompat

class SecondNotificationService : Service() {

    private lateinit var notificationManager: NotificationManager
    private lateinit var notificationBuilder: NotificationCompat.Builder
    private lateinit var id: String
    private lateinit var timer: CountDownTimer

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        id = intent?.getStringExtra(MainActivity.EXTRA_ID) ?: "002"
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Second Notification Channel", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Final Notification $id")
            .setContentText("Starting final countdown...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        startForeground(NOTIFICATION_ID, notificationBuilder.build())

        startCountdown()

        return START_STICKY
    }

    private fun startCountdown() {
        timer = object : CountDownTimer(5000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                notificationBuilder.setContentText("Final Notification: $seconds seconds remaining")
                notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
                Log.d("SecondNotificationService", "Countdown: $seconds seconds left")
            }

            override fun onFinish() {
                notificationBuilder.setContentText("Final process for Notification Channel ID $id is done!")
                    .setOngoing(false)
                notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
                Log.d("SecondNotificationService", "Final process done for ID: $id")
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }.start()
    }

    override fun onDestroy() {
        timer.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?) = null

    companion object {
        const val CHANNEL_ID = "SecondWorker_Channel"
        const val NOTIFICATION_ID = 102
    }
}
