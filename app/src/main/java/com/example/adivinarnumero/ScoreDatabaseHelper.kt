package com.example.adivinarnumero

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class ScoreDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, "scores.db", null, 2) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE scores (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                player_name TEXT,
                puntaje INTEGER,
                timestamp DATETIME DEFAULT CURRENT_TIMESTAMP
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldV: Int, newV: Int) {
        if (oldV < 2) {
            // migración desde v1 a v2: añadir columna player_name
            db.execSQL("ALTER TABLE scores ADD COLUMN player_name TEXT")
        }
    }

    fun insertarPuntaje(playerName: String, puntaje: Int) {
        writableDatabase.use { db ->
            val values = ContentValues().apply {
                put("player_name", playerName)
                put("puntaje", puntaje)
            }
            db.insert("scores", null, values)
        }
    }

    fun obtenerHistorico(): List<Pair<String,Int>> {
        val lista = mutableListOf<Pair<String,Int>>()
        readableDatabase.use { db ->
            db.rawQuery(
                "SELECT player_name, puntaje FROM scores ORDER BY puntaje DESC, timestamp DESC", null
            ).use { cursor ->
                while (cursor.moveToNext()) {
                    val name = cursor.getString(0)
                    val score = cursor.getInt(1)
                    lista += name to score
                }
            }
        }
        return lista
    }

    fun obtenerPuntajeMaximo(playerName: String): Int {
        readableDatabase.use { db ->
            db.rawQuery(
                "SELECT MAX(puntaje) FROM scores WHERE player_name = ?",
                arrayOf(playerName)
            ).use { cursor ->
                return if (cursor.moveToFirst() && !cursor.isNull(0)) {
                    cursor.getInt(0)
                } else {
                    0
                }
            }
        }
    }


}
