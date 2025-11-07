package com.example.level_up.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Resenia")
data class ReseniaEntidad(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productoId: Int,
    val usuarioId: Int,
    val nombreUsuario: String,
    val valoracion: Float,
    val comentario: String,
    val fechaCreacion: Long = System.currentTimeMillis()
)
