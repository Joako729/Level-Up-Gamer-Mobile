// Archivo: app/src/main/java/com/example/level_up/remote/service/RetrofitClient.kt

package com.example.level_up.remote.service

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// IP ESPECIAL: 10.0.2.2 es la dirección que usa el EMULADOR para acceder a tu PC (localhost)
private const val BASE_URL_LOCAL = "http://10.0.2.2:8080/"
private const val BASE_URL_NEWSAPI = "https://newsapi.org/v2/" // NUEVA URL BASE para noticias

object RetrofitClient {

    // Instancia para el backend local (existente)
    private val retrofitLocal = Retrofit.Builder()
        .baseUrl(BASE_URL_LOCAL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // Instancia para la API de Noticias (NUEVA INSTANCIA)
    private val retrofitNewsApi = Retrofit.Builder()
        .baseUrl(BASE_URL_NEWSAPI)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: ProductoApiService by lazy {
        retrofitLocal.create(ProductoApiService::class.java)
    }

    // Servicio para Usuarios/Auth
    val userApiService: UserApiService by lazy {
        retrofitLocal.create(UserApiService::class.java)
    }

    // Servicio para Pedidos
    val pedidoApiService: PedidoApiService by lazy {
        retrofitLocal.create(PedidoApiService::class.java)
    }

    // Servicio para Reseñas de Producto
    val reseniaApiService: ReseniaApiService by lazy {
        retrofitLocal.create(ReseniaApiService::class.java)
    }

    // Servicio para Reseñas de la Aplicación
    val appReseniaApiService: AppReseniaApiService by lazy {
        retrofitLocal.create(AppReseniaApiService::class.java)
    }

    // AÑADIDO: Servicio para Noticias
    val newsApiService: NewsApiService by lazy {
        retrofitNewsApi.create(NewsApiService::class.java)
    }
}