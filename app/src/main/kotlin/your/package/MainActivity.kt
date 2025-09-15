package com.example.volumelock


import android.app.NotificationManager
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.LinearLayoutCompat

class MainActivity : ComponentActivity() {

    private lateinit var dpm: DevicePolicyManager
    private lateinit var admin: ComponentName

    private val adminRequest = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tv = TextView(this).apply {
            text = "VolumeLock: держим звук на максимуме.\n" +
                   "1) Включите права Device Admin.\n" +
                   "2) Дайте доступ к DND.\n" +
                   "3) Запустите сервис."
            setPadding(40, 80, 40, 20)
        }
        val btnAdmin = Button(this).apply { text = "Включить Device Admin" }
        val btnDnd = Button(this).apply { text = "Выдать доступ к DND" }
        val btnStart = Button(this).apply { text = "Запустить сервис" }
        val btnStop = Button(this).apply { text = "Остановить сервис" }

        setContentView(
            LinearLayoutCompat(this).apply {
                orientation = LinearLayoutCompat.VERTICAL
                addView(tv)
                addView(btnAdmin)
                addView(btnDnd)
                addView(btnStart)
                addView(btnStop)
                setPadding(32, 32, 32, 32)
            }
        )

        dpm = getSystemService(DEVICE_POLICY_SERVICE) as DevicePolicyManager
        admin = ComponentName(this, AdminReceiver::class.java)

        btnAdmin.setOnClickListener {
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, admin)
                putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Требуется для защиты приложения от удаления и жёсткой фиксации громкости.")
            }
            adminRequest.launch(intent)
        }

        btnDnd.setOnClickListener {
            val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            if (!nm.isNotificationPolicyAccessGranted) {
                startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
            }
        }

        btnStart.setOnClickListener {
            startForegroundService(Intent(this, VolumeLockService::class.java))
        }

        btnStop.setOnClickListener {
            stopService(Intent(this, VolumeLockService::class.java))
        }
    }
}
