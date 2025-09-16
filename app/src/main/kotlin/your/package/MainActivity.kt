package com.example.volumelock

import android.Manifest
import android.app.NotificationManager
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {

    private lateinit var dpm: DevicePolicyManager
    private lateinit var admin: ComponentName

    private val adminRequest = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { /* no-op */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tv = TextView(this).apply {
            text = """
                VolumeLock — держим звук на максимуме.
                1) Включите права Device Admin.
                2) Дайте доступ к DND/уведомлениям.
                3) Запустите сервис.
            """.trimIndent()
            setPadding(40, 80, 40, 20)
        }
        val btnAdmin = Button(this).apply { text = "Включить Device Admin" }
        val btnDnd   = Button(this).apply { text = "Выдать доступ к DND" }
        val btnStart = Button(this).apply { text = "Запустить сервис" }
        val btnStop  = Button(this).apply { text = "Остановить сервис" }

        setContentView(
            LinearLayoutCompat(this).apply {
                orientation = LinearLayoutCompat.VERTICAL
                addView(tv); addView(btnAdmin); addView(btnDnd); addView(btnStart); addView(btnStop)
                setPadding(32, 32, 32, 32)
            }
        )

        dpm = getSystemService(DEVICE_POLICY_SERVICE) as DevicePolicyManager
        admin = ComponentName(this, AdminReceiver::class.java)

        // Android 13+ — спросим разрешение на уведомления (иначе foreground-сервис может упасть)
        if (Build.VERSION.SDK_INT >= 33) {
            val granted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1515
                )
            }
        }

        btnAdmin.setOnClickListener {
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, admin)
                putExtra(
                    DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    "Нужно для защиты от удаления и жёсткой фиксации громкости."
                )
            }
            adminRequest.launch(intent)
        }

        btnDnd.setOnClickListener {
            val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            if (!nm.isNotificationPolicyAccessGranted) {
                startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
            } else {
                Toast.makeText(this, "Доступ к DND уже выдан", Toast.LENGTH_SHORT).show()
            }
        }

        btnStart.setOnClickListener {
            try {
                // Запускаем foreground-сервис из Activity — это разрешено на Android 14/15
                startForegroundService(Intent(this, VolumeLockService::class.java))
                Toast.makeText(this, "Сервис запускается…", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(
                    this,
                    "Не удалось запустить сервис: ${e.message ?: "ошибка"}.\n" +
                    "Проверьте разрешение на уведомления.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        btnStop.setOnClickListener {
            stopService(Intent(this, VolumeLockService::class.java))
            Toast.makeText(this, "Сервис остановлен", Toast.LENGTH_SHORT).show()
        }
    }
}
