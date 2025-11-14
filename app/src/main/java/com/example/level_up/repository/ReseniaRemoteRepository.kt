package com.example.level_up.repository

import com.example.level_up.local.Entidades.ReseniaEntidad
import com.example.level_up.remote.service.ReseniaApiService
import retrofit2.HttpException
import java.io.IOException

class ReseniaRemoteRepository(private val apiService: ReseniaApiService) {

    suspend fun crearResenia(resenia: ReseniaEntidad): ReseniaEntidad? {
        return try {
            val response = apiService.crearResenia(resenia)
            if (response.isSuccessful) response.body() else null
        } catch (e: HttpException) {
            throw Exception("Error ${e.code()} al enviar reseña: ${e.message}")
        } catch (e: IOException) {
            throw Exception("Error de conexión con el servidor: ${e.message}")
        }
    }

    suspend fun obtenerReseniasPorProducto(productoId: Int): List<ReseniaEntidad> {
        return try {
            val response = apiService.obtenerReseniasPorProducto(productoId.toLong())
            if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}