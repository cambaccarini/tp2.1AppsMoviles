package com.example.adivinarnumero

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HistoryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        val recycler = findViewById<RecyclerView>(R.id.recyclerScores)
        recycler.layoutManager = LinearLayoutManager(this)

        val data = ScoreDatabaseHelper(this).obtenerHistorico()
        recycler.adapter = ScoreAdapter(data)
    }
}
