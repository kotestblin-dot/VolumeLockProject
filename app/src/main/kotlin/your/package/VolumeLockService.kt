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
import android.util.Log

class VolumeLockService : Service() {

    private lateinit var audio: AudioManager
    private val handler = Handler(Looper.getMainLooper())
    private val keepMaxRunnable = object : Runnable {
        override fun run() {
            setAllMax()
            handler.postDelayed(this, 300L) // раз в секунду гарантируем макс. громкость
        }
    }

    override fun onCreate() {
        super.onCreate()
        audio = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        try {
            startForeground(1, buildNotificationSafe())
        } catch (e: Exception) {
            // На некоторых прошивках без разрешения на уведомления (Android 13+) или при проблемах с каналом —
            // безопасно завершимся, чтобы не упасть.
            Log.e("VolumeLockService", "startForeground failed: ${e.message}", e)
            stopSelf()
            return
        }
        DndHelper.forceRingerNormal(this)
        setAllMax()
        handler.post(keepMaxRunnable)
    }

    override fun onDestroy() {
        handler.removeCallbacks(keepMaxRunnable)
        super.onDestroy()
    }


    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Если система перезапустит сервис — продолжаем работать
        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
    super.onTaskRemoved(rootIntent)
    // Попробуем перезапуститься
    val i = Intent(applicationContext, VolumeLockService::class.java)
    if (Build.VERSION.SDK_INT >= 26) {
        applicationContext.startForegroundService(i)
    } else {
        applicationContext.startService(i)
    }
}


    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotificationSafe(): Notification {
        val title = "Громкость закреплена"
        val text = "Попытки снизить звук будут отменены"

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "volume_lock_channel"
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val ch = NotificationChannel(
                channelId,
                "Volume Lock",
                NotificationManager.IMPORTANCE_MIN
            )
            nm.createNotificationChannel(ch)
            Notification.Builder(this, channelId)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(android.R.drawable.ic_lock_silent_mode_off)
                .setOngoing(true)
                .build()
        } else {
            // До Android 8.0: каналов нет, используем устаревший конструктор
            @Suppress("DEPRECATION")
            Notification.Builder(this)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(android.R.drawable.ic_lock_silent_mode_off)
                .build()
        }
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
            try {
                audio.setStreamVolume(stream, max, 0)
            } catch (_: SecurityException) {
                // некоторые прошивки могут ограничивать — пропускаем
            }
        }
    }
}
