package com.example.level_up.local.model

data class CarritoItemConImagen(
    val id: Int,
    val productoId: Int,
    val nombre: String,
    val precio: Int,
    val cantidad: Int,
    val urlImagen: String
)