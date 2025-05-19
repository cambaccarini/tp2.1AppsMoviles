package com.example.adivinarnumero

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.random.Random
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    private var numeroAleatorio = 0
    private var intentosRestantes = 3
    private var puntajeActual = 0
    private lateinit var dbHelper: ScoreDatabaseHelper
    private lateinit var textViewPuntajeActual: TextView
    private lateinit var textViewPuntajeMaximo: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        dbHelper = ScoreDatabaseHelper(this)
        textViewPuntajeActual = findViewById(R.id.textViewPuntajeActual)
        textViewPuntajeMaximo = findViewById(R.id.textViewPuntajeMaximo)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val editTextNumber = findViewById<EditText>(R.id.editTextNumber)
        val botonEnviar = findViewById<Button>(R.id.buttonEnviar)
        val textViewIntentos = findViewById<TextView>(R.id.textViewIntentos)

        generarNuevoNumero()
        actualizarTextViewIntentos(textViewIntentos)

        botonEnviar.setOnClickListener {
            val entrada = editTextNumber.text.toString()

            if (entrada.isEmpty()) {
                Toast.makeText(this, "Por favor, ingrese un número.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val numeroIngresado = entrada.toIntOrNull()

            if (numeroIngresado == null || numeroIngresado !in 1..5) {
                Toast.makeText(this, "Ingrese un número entre 1 y 5.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (numeroIngresado == numeroAleatorio) {
                Toast.makeText(this, "¡Adivinaste! Juega de nuevo", Toast.LENGTH_SHORT).show()
                puntajeActual += 10
                val maximo = dbHelper.obtenerPuntajeMaximo()
                if (puntajeActual > maximo) {
                    dbHelper.insertarPuntaje(puntajeActual)
                }
                actualizarPuntajesEnPantalla()
                generarNuevoNumero()
                actualizarTextViewIntentos(textViewIntentos)
            } else {
                intentosRestantes--
                if (intentosRestantes > 0) {
                    Toast.makeText(this, "Intenta de nuevo!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "El número era $numeroAleatorio. Perdiste :( ", Toast.LENGTH_LONG).show()
                    puntajeActual = 0
                    actualizarPuntajesEnPantalla()
                    generarNuevoNumero()
                }
                actualizarTextViewIntentos(textViewIntentos)
            }
            editTextNumber.text.clear()
        }
    }

    private fun generarNuevoNumero() {
        numeroAleatorio = Random.nextInt(1, 6)
        intentosRestantes = 3
    }
    private fun actualizarTextViewIntentos(textView: TextView) {
        textView.text = "Intentos restantes: $intentosRestantes"
    }
    private fun actualizarPuntajesEnPantalla() {
        textViewPuntajeActual.text = "Puntaje actual: $puntajeActual"
        val maximo = dbHelper.obtenerPuntajeMaximo()
        textViewPuntajeMaximo.text = "Puntaje máximo: $maximo"
    }
}