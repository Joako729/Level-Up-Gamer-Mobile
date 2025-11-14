package com.example.level_up.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.level_up.local.BaseDeDatosApp
import com.example.level_up.local.Entidades.UsuarioEntidad
import com.example.level_up.remote.service.RetrofitClient // NUEVO
import com.example.level_up.repository.UserRemoteRepository // NUEVO
import com.example.level_up.repository.UsuarioRepository
import com.example.level_up.utils.Validacion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AuthState(
    val name: String = "",
    val email: String = "",
    val age: String = "",
    val pass: String = "",
    val confirm: String = "",
    val refCode: String = "",
    val isDuoc: Boolean = false,
    val isIngresarMode: Boolean = false,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val currentUser: UsuarioEntidad? = null,
    val errors: Map<String, String> = emptyMap()
)

class AuthViewModel(app: Application) : AndroidViewModel(app) {
    // Repositorio local (Room) - Mantenido para manejar la sesión local después del login/registro exitoso
    private val repoLocal = UsuarioRepository(BaseDeDatosApp.obtener(app).UsuarioDao())

    // NUEVO: Repositorio remoto para llamadas al backend de Spring Boot
    private val repoRemote = UserRemoteRepository(RetrofitClient.userApiService)

    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state

    fun onName(v: String) {
        _state.value = _state.value.copy(name = v, errors = _state.value.errors - "name")
    }

    fun onEmail(v: String) {
        _state.value = _state.value.copy(
            email = v,
            isDuoc = Validacion.esCorreoDuoc(v),
            errors = _state.value.errors - "email"
        )
    }

    fun onAge(v: String) {
        _state.value = _state.value.copy(age = v, errors = _state.value.errors - "age")
    }

    fun onPass(v: String) {
        _state.value = _state.value.copy(pass = v, errors = _state.value.errors - "pass")
    }

    fun onConfirm(v: String) {
        _state.value = _state.value.copy(confirm = v, errors = _state.value.errors - "confirm")
    }

    fun onRefCode(v: String) {
        _state.value = _state.value.copy(refCode = v, errors = _state.value.errors - "refCode")
    }

    fun toggleMode() {
        _state.value = _state.value.copy(
            isIngresarMode = !_state.value.isIngresarMode,
            errors = emptyMap(),
            isSuccess = false
        )
    }

    fun register() = viewModelScope.launch {
        val s = _state.value
        val errs = mutableMapOf<String, String>()
        val ageInt = s.age.toIntOrNull() ?: -1

        // Validation (Local)
        if (!Validacion.esNombreValido(s.name)) errs["name"] = "Nombre debe tener al menos 2 caracteres"
        if (!Validacion.esCorreoValido(s.email)) errs["email"] = "Email inválido"
        if (!Validacion.esAdulto(ageInt)) errs["age"] = "Debes ser mayor de 18 años"
        if (!Validacion.esClaveValida(s.pass)) errs["pass"] = "Clave debe tener al menos 6 caracteres"
        if (!Validacion.contrasenasCoinciden(s.pass, s.confirm)) errs["confirm"] = "Las contraseñas no coinciden"
        if (s.refCode.isNotBlank() && !Validacion.esCodigoReferidoValido(s.refCode)) {
            errs["refCode"] = "Código de referido inválido"
        }

        // Eliminamos la verificación de correo local (repo.buscarPorCorreo)

        if (errs.isNotEmpty()) {
            _state.value = s.copy(errors = errs)
            return@launch
        }

        _state.value = s.copy(isLoading = true)

        try {
            val referralCode = Validacion.generarCodigoReferido(s.name)

            // Crea la entidad local para enviarla al backend
            val userToSend = UsuarioEntidad(
                nombre = s.name.trim(),
                correo = s.email.lowercase(),
                edad = ageInt,
                contrasena = s.pass,
                esDuoc = s.isDuoc,
                codigoReferido = referralCode,
                referidoPor = s.refCode.takeIf { it.isNotBlank() } ?: ""
            )

            // AHORA LLAMA AL REPOSITORIO REMOTO
            val registeredUser = repoRemote.registerUser(userToSend)

            if (registeredUser != null) {
                // Si el registro es exitoso en el backend (PostgreSQL)
                // 1. Guarda el usuario completo en la base de datos local (Room) para cache/sesión
                repoLocal.insertar(registeredUser)

                // 2. Actualiza el estado de la sesión a iniciado en Room
                repoLocal.actualizarEstadoSesion(registeredUser.id, true)

                _state.value = s.copy(
                    isLoading = false,
                    isSuccess = true,
                    currentUser = registeredUser,
                    errors = emptyMap()
                )
            } else {
                // Manejar respuesta no exitosa del servidor (4xx o 5xx) que no se maneja en el repo.
                _state.value = s.copy(
                    isLoading = false,
                    errors = mapOf("general" to "Error de registro: No se pudo crear el usuario.")
                )
            }
        } catch (e: Exception) {
            // Manejar errores de conexión o HTTP (409, 401, etc.)
            val errorMessage = when {
                e.message?.contains("correo ya está registrado") == true -> e.message!!
                else -> "Error de conexión o servidor: ${e.message}"
            }
            _state.value = s.copy(
                isLoading = false,
                errors = mapOf("general" to errorMessage)
            )
        }
    }

    fun login() = viewModelScope.launch {
        val s = _state.value
        val errs = mutableMapOf<String, String>()

        if (!Validacion.esCorreoValido(s.email)) errs["email"] = "Email inválido"
        if (!Validacion.esClaveValida(s.pass)) errs["pass"] = "Clave debe tener al menos 6 caracteres"

        if (errs.isNotEmpty()) {
            _state.value = s.copy(errors = errs)
            return@launch
        }

        _state.value = s.copy(isLoading = true)

        try {
            // AHORA LLAMA AL REPOSITORIO REMOTO
            val user = repoRemote.loginUser(s.email.lowercase(), s.pass)

            if (user != null) {
                // Si el login remoto es exitoso, actualizamos la sesión local:
                val localUser = repoLocal.buscarPorCorreo(user.correo)

                // Si el usuario existe localmente (ya se registró antes), actualiza.
                if (localUser != null) {
                    repoLocal.actualizarEstadoSesion(localUser.id, true)
                } else {
                    // Si el usuario no estaba en Room, lo insertamos ahora (para que la lógica de perfil funcione).
                    repoLocal.insertar(user.copy(sesionIniciada = true))
                }

                _state.value = s.copy(
                    isLoading = false,
                    isSuccess = true,
                    currentUser = user, // Usamos el objeto de la API
                    errors = emptyMap()
                )
            } else {
                _state.value = s.copy(
                    isLoading = false,
                    errors = mapOf("general" to "Credenciales inválidas o error de conexión.")
                )
            }
        } catch (e: Exception) {
            // Manejar error de conexión/servidor
            _state.value = s.copy(
                isLoading = false,
                errors = mapOf("general" to "Error al iniciar sesión: No se pudo conectar al servidor. (${e.message})")
            )
        }
    }

    fun logout() = viewModelScope.launch {
        val userToLogout = repoLocal.obtenerUsuarioActual()
        if (userToLogout != null) {
            repoLocal.actualizarEstadoSesion(userToLogout.id, false)
        }
        _state.value = AuthState()
    }

    fun clearSuccess() {
        _state.value = _state.value.copy(isSuccess = false)
    }
}