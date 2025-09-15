package com.example.volumelock


import android.app.NotificationManager
import android.content.Context
import android.media.AudioManager

object DndHelper {
    fun forceRingerNormal(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        if (nm.isNotificationPolicyAccessGranted) {
            try { nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL) } catch (_: SecurityException) { }
        }
        try { am.ringerMode = AudioManager.RINGER_MODE_NORMAL } catch (_: SecurityException) { }
    }
}
