package com.example.level_up.repository

import com.example.level_up.local.Entidades.UsuarioEntidad
import com.example.level_up.remote.service.UserApiService
import com.google.gson.Gson

class UserRemoteRepository(private val apiService: UserApiService) {

    // Simula la llamada de registro a la API de Spring Boot
    suspend fun registerUser(user: UsuarioEntidad): UsuarioEntidad? {
        val response = apiService.register(user)
        return if (response.isSuccessful) response.body() else null
    }

    // Simula la llamada de login a la API de Spring Boot
    suspend fun loginUser(email: String, pass: String): UsuarioEntidad? {
        // Necesitas construir el DTO del Login (LoginRequest) que creamos en el backend
        val loginRequest = object {
            val correo = email
            val contrasena = pass
        }

        // El 'Any' en el UserApiService.kt debe ser un objeto DTO específico,
        // pero para este ejemplo, usamos un objeto anónimo y enviamos el JSON.
        val response = apiService.login(loginRequest)

        // Si el login es exitoso (Status 200 OK)
        return if (response.isSuccessful) response.body() else null
    }
}