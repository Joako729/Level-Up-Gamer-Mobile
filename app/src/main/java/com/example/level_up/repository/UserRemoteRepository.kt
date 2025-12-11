package com.example.level_up.repository

import com.example.level_up.local.Entidades.UsuarioEntidad
import com.example.level_up.remote.service.LoginRequest
import com.example.level_up.remote.service.UserApiService
import retrofit2.HttpException
import java.io.IOException

class UserRemoteRepository(private val apiService: UserApiService) {

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

    suspend fun loginUser(email: String, pass: String): UsuarioEntidad? {
        val loginRequest = LoginRequest(correo = email, contrasena = pass)
        return try {
            val response = apiService.login(loginRequest)
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            throw e
        }
    }

    // --- NUEVAS FUNCIONES PARA EL CRUD ---

    suspend fun updateUser(id: Long, user: UsuarioEntidad): UsuarioEntidad? {
        return try {
            // Llamamos al endpoint PUT
            val response = apiService.updateUser(id, user)
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            throw Exception("Error al actualizar usuario remoto: ${e.message}")
        }
    }

    suspend fun deleteUser(id: Long): Boolean {
        return try {
            // Llamamos al endpoint DELETE
            val response = apiService.deleteUser(id)
            // Si es exitoso (código 200-299), retornamos true
            response.isSuccessful
        } catch (e: Exception) {
            throw Exception("Error al eliminar usuario remoto: ${e.message}")
        }
    }
}