package com.example.level_up.remote.service

import com.example.level_up.local.Entidades.AppReseniaEntidad // Usamos la entidad local como DTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AppReseniaApiService {

    // Llama al nuevo microservicio de App Reviews
    @POST("api/app-resenas")
    suspend fun createAppReview(@Body review: AppReseniaEntidad): Response<AppReseniaEntidad>
}