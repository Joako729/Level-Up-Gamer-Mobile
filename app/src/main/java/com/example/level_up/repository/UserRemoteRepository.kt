package com.example.level_up.repository

import com.example.level_up.local.Entidades.UsuarioEntidad
import com.example.level_up.remote.service.LoginRequest // <-- AHORA IMPORTAMOS LA DATA CLASS
import com.example.level_up.remote.service.UserApiService
import retrofit2.HttpException
import java.io.IOException

class UserRemoteRepository(private val apiService: UserApiService) {

    // Simula la llamada de registro a la API de Spring Boot
    suspend fun registerUser(user: UsuarioEntidad): UsuarioEntidad? {
        return try {
            val response = apiService.register(user)
            if (response.isSuccessful) response.body() else null
        } catch (e: HttpException) {
            if (e.code() == 409) {
                throw Exception("Este correo ya está registrado en el servidor.")
            }
            throw Exception("Error ${e.code()} al registrar: ${e.message}")
        } catch (e: IOException) {
            throw Exception("Error de conexión con el servidor: ${e.message}")
        }
    }

    // Simula la llamada de login a la API de Spring Boot
    suspend fun loginUser(email: String, pass: String): UsuarioEntidad? {
        // CORRECCIÓN: Creamos una instancia directa de la data class
        val loginRequest = LoginRequest(correo = email, contrasena = pass)

        return try {
            val response = apiService.login(loginRequest)

            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            throw e
        }
    }
}