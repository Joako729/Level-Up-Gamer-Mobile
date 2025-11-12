package com.example.level_up.local.Entidades

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "AppResenia")
data class AppReseniaEntidad(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val usuarioId: Int,
    val nombreUsuario: String,
    val valoracion: Float,
    val comentario: String,
    val fechaCreacion: Long = System.currentTimeMillis()
)