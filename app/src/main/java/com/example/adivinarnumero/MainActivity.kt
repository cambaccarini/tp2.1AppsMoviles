
package com.example.adivinarnumero

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var dbHelper: ScoreDatabaseHelper

    private lateinit var editTextNumber: EditText
    private lateinit var botonEnviar: Button
    private lateinit var botonHistorico: Button
    private lateinit var botonStopGame: Button
    private lateinit var textViewIntentos: TextView
    private lateinit var textViewPuntajeActual: TextView
    private lateinit var textViewPuntajeMaximo: TextView

    private var numeroAleatorio = 0
    private var intentosRestantes = 5
    private var puntajeActual = 0
    private lateinit var playerName: String
    private var partidaGuardada = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1) Vistas
        editTextNumber        = findViewById(R.id.editTextNumber)
        botonEnviar          = findViewById(R.id.buttonEnviar)
        botonHistorico       = findViewById(R.id.btnVerHistorico)
        botonStopGame        = findViewById(R.id.btnStopGame)
        textViewIntentos     = findViewById(R.id.textViewIntentos)
        textViewPuntajeActual= findViewById(R.id.textViewPuntajeActual)
        textViewPuntajeMaximo= findViewById(R.id.textViewPuntajeMaximo)

        // 2) SharedPreferences y DB
        prefs    = getSharedPreferences("JuegoPrefs", MODE_PRIVATE)
        dbHelper = ScoreDatabaseHelper(this)

        // 3) Nombre
        playerName = prefs.getString("playerName", null) ?: run {
            startActivity(Intent(this, NameEntryActivity::class.java))
            finish()
            return
        }

        // 4) Estado previo
        puntajeActual     = prefs.getInt("puntajeActual", 0)
        intentosRestantes = prefs.getInt("intentosRestantes", 5)

        // 5) Iniciar nueva partida (flag reset aquí)
        partidaGuardada = false
        if (numeroAleatorio == 0) generarNuevoNumero()

        // 6) UI inicial
        actualizarPuntajesEnPantalla()
        actualizarTextViewIntentos()

        // 7) Listener de adivinanza
        botonEnviar.setOnClickListener { onUserGuess() }

        // 8) Listener de histórico
        botonHistorico.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        // 9) Listener de “Dejar de jugar”
        botonStopGame.setOnClickListener {
            if (!partidaGuardada) {
                Toast.makeText(
                    this,
                    getString(R.string.msg_partida_terminada, puntajeActual),
                    Toast.LENGTH_LONG
                ).show()
                dbHelper.insertarPuntaje(playerName, puntajeActual)
                partidaGuardada = true     // marcamos que ya guardamos esta partida
            }
            // Reiniciamos datos PARA LA PRÓXIMA partida,
            // pero **sin** tocar partidaGuardada:
            // la próxima llamada a onUserGuess o stopGame reseteará el flag.
            puntajeActual = 0
            intentosRestantes = 5
            generarNuevoNumero()
            prefs.edit()
                .putInt("puntajeActual", puntajeActual)
                .putInt("intentosRestantes", intentosRestantes)
                .apply()
            actualizarPuntajesEnPantalla()
            actualizarTextViewIntentos()
            editTextNumber.text.clear()
        }
    }

    /** Lógica al adivinar número */
    private fun onUserGuess() {
        val entrada = editTextNumber.text.toString()
        val numeroIngresado = entrada.toIntOrNull()
        if (numeroIngresado == null || numeroIngresado !in 1..5) {
            Toast.makeText(
                this,
                getString(R.string.error_numero_rango),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (numeroIngresado == numeroAleatorio) {
            Toast.makeText(
                this,
                getString(R.string.msg_acertaste),
                Toast.LENGTH_SHORT
            ).show()
            puntajeActual += 10

            val maxPrevio = dbHelper.obtenerPuntajeMaximo(playerName)
            if (puntajeActual > maxPrevio) {
                dbHelper.insertarPuntaje(playerName, puntajeActual)
                partidaGuardada = true
            }

            // Reiniciar para próxima partida
            intentosRestantes = 5
            generarNuevoNumero()
            partidaGuardada = false
        } else {
            intentosRestantes--
            if (intentosRestantes > 0) {
                Toast.makeText(
                    this,
                    getString(R.string.msg_fallaste, intentosRestantes),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                if (!partidaGuardada) {
                    Toast.makeText(
                        this,
                        getString(R.string.msg_partida_terminada, puntajeActual),
                        Toast.LENGTH_LONG
                    ).show()
                    dbHelper.insertarPuntaje(playerName, puntajeActual)
                    partidaGuardada = true
                }
                puntajeActual = 0
                intentosRestantes = 5
                generarNuevoNumero()
                partidaGuardada = false
            }
        }

        prefs.edit()
            .putInt("puntajeActual", puntajeActual)
            .putInt("intentosRestantes", intentosRestantes)
            .apply()

        actualizarPuntajesEnPantalla()
        actualizarTextViewIntentos()
        editTextNumber.text.clear()
    }

    private fun generarNuevoNumero() {
        numeroAleatorio = Random.nextInt(1, 6)
    }

    private fun actualizarTextViewIntentos() {
        textViewIntentos.text = getString(R.string.intentos_restantes, intentosRestantes)
    }

    private fun actualizarPuntajesEnPantalla() {
        textViewPuntajeActual.text = getString(R.string.puntaje_actual, puntajeActual)
        val maximoUsuario = dbHelper.obtenerPuntajeMaximo(playerName)
        textViewPuntajeMaximo.text = getString(R.string.puntaje_maximo_usuario, maximoUsuario)
    }
}
