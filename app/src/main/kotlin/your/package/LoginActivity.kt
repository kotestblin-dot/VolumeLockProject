package com.example.volumelock

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.widget.LinearLayoutCompat

class LoginActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val input = EditText(this).apply {
            hint = "Введите пароль"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or
                        android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD
        }

        val btn = Button(this).apply { text = "Войти" }

        setContentView(
            LinearLayoutCompat(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(50, 100, 50, 50)
                addView(input)
                addView(btn)
            }
        )

        btn.setOnClickListener {
            val password = input.text.toString()
            if (password == "1515") {
                // Переход в MainActivity
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Неверный пароль", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
