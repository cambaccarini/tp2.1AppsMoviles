package com.example.adivinarnumero

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class NameEntryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_name_entry)

        val etName = findViewById<EditText>(R.id.etPlayerName)
        val btnStart = findViewById<Button>(R.id.btnStart)

        btnStart.setOnClickListener {
            val name = etName.text.toString().trim()
            if (name.isEmpty()) {
                etName.error = getString(R.string.error_nombre_vacio)
            } else {
                // Guardamos el nombre **una sola vez**
                getSharedPreferences("JuegoPrefs", MODE_PRIVATE)
                    .edit()
                    .putString("playerName", name)
                    .apply()

                // Pasamos a MainActivity y cerramos ésta
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
    }
}


