package com.example.volumelock

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.intent.action.BOOT_COMPLETED" ||
            intent.action == "android.intent.action.LOCKED_BOOT_COMPLETED") {
            context.startForegroundService(Intent(context, VolumeLockService::class.java))
        }
    }
}
