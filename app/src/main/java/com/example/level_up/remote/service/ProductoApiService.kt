// Archivo: app/src/main/java/com/example/level_up/remote/service/ProductoApiService.kt

package com.example.level_up.remote.service

import com.example.level_up.remote.model.ProductoRemoto // <--- ¡CAMBIADO de 'local.model' a 'remote.model'!
import retrofit2.Response
import retrofit2.http.GET

interface ProductoApiService {

    // Define la petición GET al endpoint de productos
    @GET("api/productos")
    // Retorna una lista del modelo remoto que creaste
    suspend fun obtenerTodosLosProductos(): Response<List<ProductoRemoto>>
}