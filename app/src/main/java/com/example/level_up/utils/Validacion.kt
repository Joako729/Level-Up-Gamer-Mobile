package com.example.level_up.utils

object Validacion {

    fun esAdulto(edad: Int): Boolean = edad >= 18

    fun esCorreoDuoc(correo: String): Boolean = correo.lowercase().endsWith("@duocuc.cl")

    // Modificado para usar Regex y ser compatible con Unit Tests
    fun esCorreoValido(correo: String): Boolean {
        return correo.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\$"))
    }

    fun esClaveValida(contrasena: String): Boolean = contrasena.length >= 6

    fun contrasenasCoinciden(contrasena: String, confirmarClave: String): Boolean = contrasena == confirmarClave

    fun esNombreValido(nombre: String): Boolean = nombre.trim().length >= 2

    fun esPrecioValido(precio: Int): Boolean = precio > 0

    fun esStockValido(stock: Int): Boolean = stock >= 0

    fun esCalificacionValida(calificacion: Float): Boolean = calificacion in 0.0..5.0

    fun esCantidadValida(cantidad: Int): Boolean = cantidad > 0

    fun esCodigoReferidoValido(codigo: String): Boolean = codigo.length >= 6

    fun esComentarioResenaValido(comentario: String): Boolean = comentario.trim().length >= 10

    fun generarCodigoReferido(nombre: String): String {
        val nombreLimpio = nombre.replace(" ", "").uppercase()
        val sufijoAleatorio = (1000..11759).random()
        return "${nombreLimpio.take(3)}$sufijoAleatorio"
    }

    fun calcularNivel(puntos: Int): Int {
        return when {
            puntos >= 11760 -> 5
            puntos >= 5880 -> 4
            puntos >= 2352 -> 3
            puntos >= 500 -> 2
            else -> 1
        }
    }

    fun obtenerPorcentajeDescuento(nivel: Int): Int {
        return when (nivel) {
            5 -> 15
            4 -> 12
            3 -> 10
            2 -> 7
            1 -> 5
            else -> 0
        }
    }
}