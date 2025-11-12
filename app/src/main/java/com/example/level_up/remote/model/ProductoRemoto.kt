package com.example.level_up.local.model

import com.google.gson.annotations.SerializedName

// MODELO FINAL: Todos los campos sensibles a errores de API son opcionales
data class ProductoRemoto(
    val id: Long,
    val nombre: String?,
    val precio: Double?, // FIX: Cambiado a Double? para aceptar decimales
    val categoria: String?,
    val codigo: String?,
    val stock: Int?,
    val descripcion: String?,
    val urlImagen: String?,
    val fabricante: String?,
    val valoracion: Float?,
    val destacado: Boolean?
)