package com.example.level_up.repository

import com.example.level_up.local.Entidades.AppReseniaEntidad
import com.example.level_up.remote.service.AppReseniaApiService
import retrofit2.HttpException
import java.io.IOException

class AppReseniaRemoteRepository(private val apiService: AppReseniaApiService) {

    suspend fun crearResenia(resenia: AppReseniaEntidad): AppReseniaEntidad? {
        return try {
            val response = apiService.createAppReview(resenia)
            if (response.isSuccessful) response.body() else null
        } catch (e: HttpException) {
            throw Exception("Error ${e.code()} al enviar reseña de la app: ${e.message}")
        } catch (e: IOException) {
            throw Exception("Error de conexión con el servidor: ${e.message}")
        }
    }
}