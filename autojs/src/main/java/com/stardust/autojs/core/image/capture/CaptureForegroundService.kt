package com.stardust.autojs.core.image.capture

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjection
import android.os.Build
import android.os.Handler
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.stardust.app.service.AbstractAutoService
import com.stardust.autojs.R

/**
 * Created by TonyJiangWJ(https://github.com/TonyJiangWJ).
 * From [TonyJiangWJ/Auto.js](https://github.com/TonyJiangWJ/Auto.js)
 */
class CaptureForegroundService : AbstractAutoService() {

    val callback = object : MediaProjection.Callback() {
        override fun onStop() {
            stopServiceInternal()
        }
    }

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIFICATION_ID, buildNotification())
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        when (action) {
            STOP -> stopServiceInternal()
            REGISTER -> mediaProjection?.registerCallback(callback, Handler(mainLooper))
        }
        super.onStartCommand(intent, flags, startId)
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaProjection?.unregisterCallback(callback)
        mediaProjection?.stop()
        removeNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        }
    }

    private fun buildNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }
        val flags =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        val contentIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, ScreenCaptureRequestActivity::class.java), flags
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(NOTIFICATION_TITLE)
            .setSmallIcon(R.drawable.autojs_logo)
            .setWhen(System.currentTimeMillis())
            .setContentIntent(contentIntent)
            .addAction(createExitAction())
            .setChannelId(CHANNEL_ID)
            .setVibrate(LongArray(0))
            .build()
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val manager = (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
        val channel = NotificationChannel(
            CHANNEL_ID,
            NOTIFICATION_TITLE,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        channel.description = NOTIFICATION_TITLE
        channel.enableLights(false)
        manager.createNotificationChannel(channel)
    }

    private fun createExitAction(): NotificationCompat.Action {
        val pendingIntent = PendingIntent.getService(
            this, 12,
            Intent(this, CaptureForegroundService::class.java).apply {
                action = STOP
            }, PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Action.Builder(
            null,
            "停止截图",
            pendingIntent
        ).build()
    }

    private fun removeNotification() {
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).cancel(NOTIFICATION_ID)
    }


    companion object {
        private var mediaProjection: MediaProjection? = null
        private const val STOP = "STOP_SERVICE"
        private const val REGISTER = "REGISTER_CALLBACK"
        private const val NOTIFICATION_ID = 2
        private val CHANNEL_ID = CaptureForegroundService::class.java.name + ".foreground"
        private const val NOTIFICATION_TITLE = "前台截图服务运行中"


        fun setMediaProjection(context: Context, media: MediaProjection) {
            mediaProjection = media
            val intent = Intent(context, CaptureForegroundService::class.java).apply {
                action = REGISTER
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }
}