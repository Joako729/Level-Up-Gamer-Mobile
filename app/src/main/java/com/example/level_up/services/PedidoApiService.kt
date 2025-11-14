package com.example.level_up.remote.service

import com.example.level_up.local.Entidades.PedidoEntidad
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

// DTO que modela el cuerpo de la petición.
// Nota: Utilizamos Long para usuarioId para el backend.
data class PedidoRequest(
    val usuarioId: Long,
    val montoTotal: Int,
    val montoDescuento: Int,
    val montoFinal: Int,
    val estado: String,
    val fechaCreacion: Long,
    val itemsJson: String
)

interface PedidoApiService {

    // Endpoint para Registrar Pedido
    @POST("api/pedidos")
    suspend fun crearPedido(@Body request: PedidoRequest): Response<PedidoEntidad>
}