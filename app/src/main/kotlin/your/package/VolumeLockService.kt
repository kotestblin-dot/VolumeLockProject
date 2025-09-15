package com.example.volumelock


import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper

class VolumeLockService : Service() {

    private lateinit var audio: AudioManager
    private val handler = Handler(Looper.getMainLooper())
    private val keepMaxRunnable = object : Runnable {
        override fun run() {
            setAllMax()
            handler.postDelayed(this, 1000L)
        }
    }

    override fun onCreate() {
        super.onCreate()
        audio = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        startForeground(1, buildNotification())
        DndHelper.forceRingerNormal(this)
        setAllMax()
        handler.post(keepMaxRunnable)
    }

    override fun onDestroy() {
        handler.removeCallbacks(keepMaxRunnable)
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotification(): Notification {
        val channelId = "volume_lock_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(channelId, "Volume Lock", NotificationManager.IMPORTANCE_MIN)
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(ch)
        }
        return Notification.Builder(this, channelId)
            .setContentTitle("Громкость закреплена")
            .setContentText("Попытки снизить звук будут отменены")
            .setSmallIcon(android.R.drawable.ic_lock_silent_mode_off)
            .build()
    }

    private fun setAllMax() {
        trySetMax(AudioManager.STREAM_RING)
        trySetMax(AudioManager.STREAM_NOTIFICATION)
        trySetMax(AudioManager.STREAM_MUSIC)
        trySetMax(AudioManager.STREAM_ALARM)
        trySetMax(AudioManager.STREAM_SYSTEM)
        DndHelper.forceRingerNormal(this)
    }

    private fun trySetMax(stream: Int) {
        val max = audio.getStreamMaxVolume(stream)
        val cur = audio.getStreamVolume(stream)
        if (cur < max) {
            try { audio.setStreamVolume(stream, max, 0) } catch (_: SecurityException) { }
        }
    }
}
