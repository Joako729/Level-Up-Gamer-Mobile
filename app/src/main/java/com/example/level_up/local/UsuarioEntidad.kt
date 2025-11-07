package com.example.level_up.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Usuario")
data class UsuarioEntidad(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val correo: String,
    val edad: Int,
    val contrasena: String,
    val esDuoc: Boolean = false,
    val puntosLevelUp: Int = 0,
    val nivel: Int = 1,
    val codigoReferido: String = "",
    val referidoPor: String = "",
    val totalCompras: Int = 0,
    val sesionIniciada: Boolean = false
)
