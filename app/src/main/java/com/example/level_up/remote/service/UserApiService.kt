package com.example.level_up.remote.service

import com.example.level_up.local.Entidades.UsuarioEntidad
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface UserApiService {

    // Llama a POST http://10.0.2.2:8080/api/usuarios/registro
    @POST("api/usuarios/registro")
    suspend fun register(@Body user: UsuarioEntidad): Response<UsuarioEntidad>

    // Llama a POST http://10.0.2.2:8080/api/usuarios/login
    // Nota: Necesitarás un DTO más simple para el body del login (ej: Correo y Contrasena)
    // Por simplicidad, asumiremos que el backend acepta el DTO LoginRequest que definimos antes
    @POST("api/usuarios/login")
    suspend fun login(@Body request: Any): Response<UsuarioEntidad>
}