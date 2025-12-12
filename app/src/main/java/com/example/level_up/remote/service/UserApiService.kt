package com.example.level_up.remote.service

import com.example.level_up.local.Entidades.UsuarioEntidad
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

data class LoginRequest(
    val correo: String,
    val contrasena: String
)

interface UserApiService {

    @POST("api/usuarios/registro")
    suspend fun register(@Body user: UsuarioEntidad): Response<UsuarioEntidad>

    @POST("api/usuarios/login")
    suspend fun login(@Body request: LoginRequest): Response<UsuarioEntidad>

    @GET("api/usuarios/{id}")
    suspend fun getUserById(@Path("id") id: Long): Response<UsuarioEntidad>

    @PUT("api/usuarios/{id}")
    suspend fun updateUser(@Path("id") id: Long, @Body user: UsuarioEntidad): Response<UsuarioEntidad>

    @DELETE("api/usuarios/{id}")
    suspend fun deleteUser(@Path("id") id: Long): Response<Void>
}