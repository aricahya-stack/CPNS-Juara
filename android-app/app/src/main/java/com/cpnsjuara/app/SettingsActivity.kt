package com.cpnsjuara.app

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val input = findViewById<EditText>(R.id.inputBaseUrl)
        input.setText(Prefs.getBaseUrl(this))

        findViewById<Button>(R.id.btnSave).setOnClickListener {
            val value = input.text.toString().trim()
            if (!value.startsWith("http://") && !value.startsWith("https://")) {
                Toast.makeText(this, "URL harus diawali http:// atau https://", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            Prefs.setBaseUrl(this, value)
            Toast.makeText(this, "Server disimpan.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
