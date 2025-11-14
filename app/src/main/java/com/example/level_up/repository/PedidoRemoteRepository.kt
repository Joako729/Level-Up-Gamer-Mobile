package com.example.level_up.repository

import com.example.level_up.local.Entidades.PedidoEntidad
import com.example.level_up.remote.service.PedidoRequest
import com.example.level_up.remote.service.PedidoApiService
import retrofit2.HttpException
import java.io.IOException

class PedidoRemoteRepository(private val apiService: PedidoApiService) {

    // Simula la llamada de creación de pedido a la API de Spring Boot
    suspend fun crearPedido(pedido: PedidoRequest): PedidoEntidad? {
        return try {
            val response = apiService.crearPedido(pedido)
            if (response.isSuccessful) response.body() else null
        } catch (e: HttpException) {
            // Manejo específico de errores HTTP del servidor
            throw Exception("Error ${e.code()} al registrar pedido: ${e.message}")
        } catch (e: IOException) {
            // Manejo de errores de red (ej. servidor apagado o URL incorrecta)
            throw Exception("Error de conexión con el servidor: ${e.message}")
        }
    }
}