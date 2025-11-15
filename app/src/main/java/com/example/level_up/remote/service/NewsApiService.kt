// Archivo: app/src/main/java/com/example/level_up/remote/service/NewsApiService.kt (MODIFICAR)

package com.example.level_up.remote.service

import com.example.level_up.remote.model.NewsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApiService {

    // CAMBIO: Usamos el endpoint 'everything' con solo el query "gaming" para asegurar resultados.
    @GET("everything")
    suspend fun getTopGamingHeadlines(
        @Query("apiKey") apiKey: String = "2fa97e4f10384654aa982cd9b06a5c14",
        @Query("q") query: String = "gaming", // Consulta más simple
        @Query("language") language: String = "es", // Buscamos en español
        @Query("pageSize") pageSize: Int = 4
    ): Response<NewsResponse>
}