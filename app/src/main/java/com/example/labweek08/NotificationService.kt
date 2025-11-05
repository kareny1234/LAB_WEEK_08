package com.example.labweek08

import android.app.*
import android.content.*
import android.os.*
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class NotificationService : Service() {

    private lateinit var notificationBuilder: NotificationCompat.Builder
    private lateinit var notificationManager: NotificationManager

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val id = intent?.getStringExtra(EXTRA_ID)
            ?: throw IllegalStateException("Channel ID must be provided")

        val pendingIntent = getPendingIntent()
        val channelId = createNotificationChannel()

        notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Second worker process is done")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        startForeground(NOTIFICATION_ID, notificationBuilder.build())

        // Start countdown notification
        startCountdown(id)

        return START_NOT_STICKY
    }

    private fun startCountdown(id: String) {
        object : CountDownTimer(11000, 1000) {
            var seconds = 10

            override fun onTick(millisUntilFinished: Long) {
                notificationBuilder.setContentText("$seconds seconds until last warning")
                notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
                seconds--
            }

            override fun onFinish() {
                notificationBuilder.setContentText("Process for Notification Channel ID $id is done!")
                    .setOngoing(false)
                notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())

                mutableID.postValue(id)
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }.start()
    }

    private fun getPendingIntent(): PendingIntent {
        val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            PendingIntent.FLAG_IMMUTABLE else 0

        return PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java), flag
        )
    }

    private fun createNotificationChannel(): String {
        val channelId = "001"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "001 Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Countdown notification channel"
            }
            notificationManager.createNotificationChannel(channel)
        }
        return channelId
    }

    companion object {
        const val NOTIFICATION_ID = 0xCA7
        const val EXTRA_ID = "Id"
        private val mutableID = MutableLiveData<String>()
        val trackingCompletion: LiveData<String> = mutableID
    }
}
