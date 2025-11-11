package com.example.level_up.remote.service // Usa un paquete lógico

import com.example.level_up.local.model.ProductoRemoto
import retrofit2.Response
import retrofit2.http.GET

interface ProductoApiService {

    // Define la petición GET al endpoint de productos
    // Llama a: http://10.0.2.2:8080/api/productos
    @GET("api/productos")
    // Retorna una lista del modelo remoto que creaste
    suspend fun obtenerTodosLosProductos(): Response<List<ProductoRemoto>>
}