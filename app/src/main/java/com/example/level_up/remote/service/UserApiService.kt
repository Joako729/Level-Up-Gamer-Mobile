package com.example.level_up.remote.service

import com.example.level_up.local.Entidades.UsuarioEntidad
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

// 1. DTO NECESARIO PARA EL LOGIN: Retrofit lo usa para crear el JSON del Body
data class LoginRequest(
    val correo: String,
    val contrasena: String
)

interface UserApiService {

    // Endpoint para Registro
    @POST("api/usuarios/registro")
    suspend fun register(@Body user: UsuarioEntidad): Response<UsuarioEntidad>

    // Endpoint para Login: Ahora usa el DTO LoginRequest
    @POST("api/usuarios/login")
    suspend fun login(@Body request: LoginRequest): Response<UsuarioEntidad>
}