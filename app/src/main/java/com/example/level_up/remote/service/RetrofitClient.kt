package com.example.level_up.remote.service

import retrofit2.Retrofit // <--- ESTA LÍNEA DEBES AÑADIR O CORREGIR
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

    // Servicio para Usuarios/Auth
    val userApiService: UserApiService by lazy {
        retrofit.create(UserApiService::class.java)
    }

    // Servicio para Pedidos
    val pedidoApiService: PedidoApiService by lazy {
        retrofit.create(PedidoApiService::class.java)
    }

    // NUEVO: Servicio para Reseñas
    val reseniaApiService: ReseniaApiService by lazy {
        retrofit.create(ReseniaApiService::class.java)
    }

    // AÑADIDO: Servicio para Reseñas de la Aplicación
    val appReseniaApiService: AppReseniaApiService by lazy { // <-- ¡Añade esta línea!
        retrofit.create(AppReseniaApiService::class.java)
    }
}