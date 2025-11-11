package com.example.level_up.local.model // Usa un paquete lógico

import com.google.gson.annotations.SerializedName

// Este modelo debe COINCIDIR EXACTAMENTE con la Entidad Producto de tu servidor Spring Boot
data class ProductoRemoto(
    val id: Long,
    val nombre: String,
    val precio: Int, // En tu Entidad local ProductoEntidad el precio es Int
    val categoria: String,
    val codigo: String,
    val stock: Int,
    val descripcion: String,
    val urlImagen: String,
    val fabricante: String,
    val valoracion: Float,
    val destacado: Boolean
)