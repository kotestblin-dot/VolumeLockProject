package com.example.volumelock

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        Log.i("BootReceiver", "onReceive: $action")

        // Стартуем foreground-сервис корректно для Android 8+.
        // На Android 12–15 запуск foreground-сервиса из BOOT допускается,
        // если сделать startForegroundService и в самом сервисе быстро вызвать startForeground().
        val svc = Intent(context, VolumeLockService::class.java)

        try {
            if (Build.VERSION.SDK_INT >= 26) {
                context.startForegroundService(svc)
            } else {
                context.startService(svc)
            }
        } catch (e: Exception) {
            Log.e("BootReceiver", "start service after boot failed: ${e.message}", e)
        }
    }
}
