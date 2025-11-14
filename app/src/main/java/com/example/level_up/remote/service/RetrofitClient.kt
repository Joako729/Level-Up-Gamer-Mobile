package com.example.level_up.remote.service // Mismo paquete que el API Service

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// IP ESPECIAL: 10.0.2.2 es la dirección que usa el EMULADOR para acceder a tu PC (localhost)
private const val BASE_URL = "http://10.0.2.2:8080/"

object RetrofitClient {
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: ProductoApiService by lazy {
        retrofit.create(ProductoApiService::class.java)
    }

    // NUEVA LÍNEA: Servicio para Usuarios/Auth
    val userApiService: UserApiService by lazy {
        retrofit.create(UserApiService::class.java)
    }
}