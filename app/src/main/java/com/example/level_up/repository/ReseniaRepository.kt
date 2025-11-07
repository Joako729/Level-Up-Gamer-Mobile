package com.example.level_up.repository

import com.example.level_up.local.ReseniaDao
import com.example.level_up.local.ReseniaEntidad
import kotlinx.coroutines.flow.Flow

class ReseniaRepository(private val dao: ReseniaDao) {

    fun obtenerReseniasPorProducto(idProducto: Int): Flow<List<ReseniaEntidad>> =
        dao.obtenerReseniasPorProducto(idProducto)

    suspend fun insertarResena(resenia: ReseniaEntidad) {
        dao.insertarResena(resenia)
    }

    suspend fun actualizarResena(resenia: ReseniaEntidad) {
        dao.actualizarResena(resenia)
    }

    suspend fun eliminarResena(resena: ReseniaEntidad) {
        dao.eliminarResena(resena)
    }

    suspend fun obtenerPromedioResenas(idProducto: Int): Float? =
        dao.obtenerPromedioResenas(idProducto)

    suspend fun contarResenas(idProducto: Int): Int =
        dao.contarResenas(idProducto)
}
