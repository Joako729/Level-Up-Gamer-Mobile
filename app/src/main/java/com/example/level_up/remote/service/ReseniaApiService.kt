package com.example.level_up.remote.service

import com.example.level_up.local.Entidades.ReseniaEntidad
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ReseniaApiService {

    @POST("api/resenas")
    suspend fun crearResenia(@Body resenia: ReseniaEntidad): Response<ReseniaEntidad>

    @GET("api/resenas/producto/{productoId}")
    suspend fun obtenerReseniasPorProducto(@Path("productoId") productoId: Long): Response<List<ReseniaEntidad>>
}