package com.stardust.autojs.core.image.capture

import android.content.Intent
import android.media.projection.MediaProjection
import android.os.Handler
import android.os.IBinder
import android.util.Log
import com.stardust.app.foreground.AbstractBroadcastService

/**
 * Created by TonyJiangWJ(https://github.com/TonyJiangWJ).
 * From [TonyJiangWJ/Auto.js](https://github.com/TonyJiangWJ/Auto.js)
 */
class CaptureForegroundService : AbstractBroadcastService() {

    val callback = object : MediaProjection.Callback() {
        override fun onStop() {
            stopSelf()
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        if (intent.action == (STOP)) {
            Log.i(TAG, "stopSelf")
            stopSelf()
        }
        mediaProjection?.registerCallback(callback, Handler(mainLooper))
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaProjection?.unregisterCallback(callback)
        mediaProjection?.stop()
    }

    companion object {
        var mediaProjection: MediaProjection? = null
        private const val TAG = "CaptureService"
        private const val STOP = "STOP_SERVICE"
    }
}