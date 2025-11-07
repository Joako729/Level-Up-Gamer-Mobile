package com.example.level_up.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Producto")
data class ProductoEntidad(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val codigo: String,
    val categoria: String,
    val nombre: String,
    val precio: Int,
    val stock: Int,
    val valoracion: Float = 0f,
    val descripcion: String = "",
    val urlImagen: String = "",
    val fabricante: String = "",
    val destacado: Boolean = false
)
