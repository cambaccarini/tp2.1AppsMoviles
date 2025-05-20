package com.example.adivinarnumero

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class ScoreDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, "scores.db", null, 3) {

    companion object {
        private const val DB_NAME       = "scores.db"
        private const val DB_VERSION    = 3

        // Tabla de historial (todas las partidas)
        private const val TABLE_SCORES      = "scores"
        private const val COL_ID            = "id"
        private const val COL_PLAYER        = "player_name"
        private const val COL_SCORE         = "puntaje"
        private const val COL_TIMESTAMP     = "timestamp"

        // Tabla de máximos (un registro por jugador)
        private const val TABLE_MAX_SCORES  = "max_scores"
        private const val COL_M_PLAYER      = "player_name"
        private const val COL_MAX_SCORE     = "max_score"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // 1) Historial de partidas
        db.execSQL("""
            CREATE TABLE $TABLE_SCORES (
                $COL_ID        INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_PLAYER    TEXT NOT NULL,
                $COL_SCORE     INTEGER NOT NULL,
                $COL_TIMESTAMP DATETIME DEFAULT CURRENT_TIMESTAMP
            );
        """.trimIndent())

        // 2) Puntuaciones máximas por jugador
        db.execSQL("""
            CREATE TABLE $TABLE_MAX_SCORES (
                $COL_M_PLAYER  TEXT PRIMARY KEY,
                $COL_MAX_SCORE INTEGER NOT NULL
            );
        """.trimIndent())
    }

    override fun onUpgrade(db: SQLiteDatabase, oldV: Int, newV: Int) {
        // v1 → v2: añadimos columna player_name a scores
        if (oldV < 2) {
            db.execSQL("ALTER TABLE $TABLE_SCORES ADD COLUMN $COL_PLAYER TEXT")
        }
        // v2 → v3: creamos tabla de máximos
        if (oldV < 3) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS $TABLE_MAX_SCORES (
                    $COL_M_PLAYER  TEXT PRIMARY KEY,
                    $COL_MAX_SCORE INTEGER NOT NULL
                );
            """.trimIndent())
        }
    }

    /** Inserta UNA fila en el historial cada vez que termina una partida */
    fun insertarHistorial(nombre: String, puntaje: Int) {
        writableDatabase.use { db ->
            val cv = ContentValues().apply {
                put(COL_PLAYER, nombre)
                put(COL_SCORE, puntaje)
            }
            db.insert(TABLE_SCORES, null, cv)
        }
    }

    /** Recupera todas las partidas ordenadas por puntaje (desc) y fecha (desc) */
    fun obtenerHistorico(): List<Pair<String, Int>> {
        val lista = mutableListOf<Pair<String, Int>>()
        readableDatabase.use { db ->
            db.rawQuery(
                "SELECT $COL_PLAYER, $COL_SCORE FROM $TABLE_SCORES " +
                        "ORDER BY $COL_SCORE DESC, $COL_TIMESTAMP DESC",
                null
            ).use { cursor ->
                while (cursor.moveToNext()) {
                    lista += cursor.getString(0) to cursor.getInt(1)
                }
            }
        }
        return lista
    }

    /** Devuelve el puntaje máximo almacenado para un jugador (0 si no existe) */
    fun obtenerPuntajeMaximo(nombre: String): Int {
        readableDatabase.use { db ->
            db.rawQuery(
                "SELECT $COL_MAX_SCORE FROM $TABLE_MAX_SCORES WHERE $COL_M_PLAYER = ?",
                arrayOf(nombre)
            ).use { cursor ->
                return if (cursor.moveToFirst()) {
                    cursor.getInt(0)
                } else {
                    0
                }
            }
        }
    }

    /**
     * Upsert del puntaje máximo:
     * si ya existía, reemplaza; si no, inserta nuevo.
     */
    fun actualizarPuntajeMaximo(nombre: String, nuevoMax: Int) {
        writableDatabase.use { db ->
            val cv = ContentValues().apply {
                put(COL_M_PLAYER, nombre)
                put(COL_MAX_SCORE, nuevoMax)
            }
            db.insertWithOnConflict(
                TABLE_MAX_SCORES,
                null,
                cv,
                SQLiteDatabase.CONFLICT_REPLACE
            )
        }
    }

    /** Borra todo el histórico y los máximos (útil para debugging) */
    fun limpiarBaseDeDatos() {
        writableDatabase.use { db ->
            db.delete(TABLE_SCORES, null, null)
            db.delete(TABLE_MAX_SCORES, null, null)
        }
    }
}
