package com.example.level_up.remote.service

import com.example.level_up.local.Entidades.UsuarioEntidad
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE // Importante
import retrofit2.http.POST
import retrofit2.http.PUT    // Importante
import retrofit2.http.Path   // Importante

data class LoginRequest(
    val correo: String,
    val contrasena: String
)

interface UserApiService {

    @POST("api/usuarios/registro")
    suspend fun register(@Body user: UsuarioEntidad): Response<UsuarioEntidad>

    @POST("api/usuarios/login")
    suspend fun login(@Body request: LoginRequest): Response<UsuarioEntidad>

    // --- NUEVOS ENDPOINTS ---

    @DELETE("api/usuarios/{id}")
    suspend fun deleteUser(@Path("id") id: Long): Response<Void>

    @PUT("api/usuarios/{id}")
    suspend fun updateUser(@Path("id") id: Long, @Body user: UsuarioEntidad): Response<UsuarioEntidad>
}