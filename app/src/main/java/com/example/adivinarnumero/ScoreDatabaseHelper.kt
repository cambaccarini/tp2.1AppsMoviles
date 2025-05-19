package com.example.adivinarnumero

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class ScoreDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, "scores.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE scores (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "puntaje INTEGER, " +
                    "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP)"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS scores")
        onCreate(db)
    }

    fun insertarPuntaje(puntaje: Int) {
        val db = writableDatabase
        val values = ContentValues()
        values.put("puntaje", puntaje)
        db.insert("scores", null, values)
        db.close()
    }

    fun obtenerPuntajeMaximo(): Int {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT MAX(puntaje) FROM scores", null)
        var maximo = 0
        if (cursor.moveToFirst()) {
            maximo = cursor.getInt(0)
        }
        cursor.close()
        db.close()
        return maximo
    }
}
