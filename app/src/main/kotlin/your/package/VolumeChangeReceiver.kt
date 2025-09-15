package your.package

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager

class VolumeChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if ("android.media.VOLUME_CHANGED_ACTION" == intent.action) {
            val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            context.startForegroundService(Intent(context, VolumeLockService::class.java))

            fun clamp(stream: Int) {
                val max = am.getStreamMaxVolume(stream)
                val cur = am.getStreamVolume(stream)
                if (cur < max) {
                    try { am.setStreamVolume(stream, max, 0) } catch (_: SecurityException) {}
                }
            }
            clamp(AudioManager.STREAM_RING)
            clamp(AudioManager.STREAM_NOTIFICATION)
            clamp(AudioManager.STREAM_MUSIC)
            clamp(AudioManager.STREAM_ALARM)
            clamp(AudioManager.STREAM_SYSTEM)
            DndHelper.forceRingerNormal(context)
        }
    }
}
