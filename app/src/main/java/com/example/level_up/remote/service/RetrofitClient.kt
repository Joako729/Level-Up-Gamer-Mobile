package com.example.level_up.remote.service

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // IMPORTANTE: Configuración de la dirección de tu Backend
    // -------------------------------------------------------

    // OPCIÓN A: Si usas el EMULADOR de Android Studio
    private const val BASE_URL = "http://10.0.2.2:8080/"

    // OPCIÓN B: Si usas tu CELULAR FÍSICO (USB/WiFi)
    // Debes poner la IP de tu computador (ej: 192.168.1.15)
    // private const val BASE_URL = "http://TU_IP_AQUI:8080/"

    // -------------------------------------------------------

    // URL de API de noticias (Externa)
    private const val BASE_URL_NEWSAPI = "https://newsapi.org/v2/"

    // Cliente para tu Backend (Spring Boot)
    private val retrofitBackend = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // Cliente para Noticias
    private val retrofitNewsApi = Retrofit.Builder()
        .baseUrl(BASE_URL_NEWSAPI)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // --- Servicios conectados a tu Backend ---

    val apiService: ProductoApiService by lazy {
        retrofitBackend.create(ProductoApiService::class.java)
    }

    val userApiService: UserApiService by lazy {
        retrofitBackend.create(UserApiService::class.java)
    }

    val pedidoApiService: PedidoApiService by lazy {
        retrofitBackend.create(PedidoApiService::class.java)
    }

    val reseniaApiService: ReseniaApiService by lazy {
        retrofitBackend.create(ReseniaApiService::class.java)
    }

    val appReseniaApiService: AppReseniaApiService by lazy {
        retrofitBackend.create(AppReseniaApiService::class.java)
    }

    // --- Servicios Externos ---

    val newsApiService: NewsApiService by lazy {
        retrofitNewsApi.create(NewsApiService::class.java)
    }
}