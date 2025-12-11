package com.example.level_up.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.level_up.local.BaseDeDatosApp
import com.example.level_up.local.Entidades.UsuarioEntidad
import com.example.level_up.remote.service.RetrofitClient
import com.example.level_up.repository.UserRemoteRepository
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

// CAMBIO CLAVE: Los repositorios ahora están en el constructor con valores por defecto
class AuthViewModel(
    app: Application,
    private val repoLocal: UsuarioRepository = UsuarioRepository(BaseDeDatosApp.obtener(app).UsuarioDao()),
    private val repoRemote: UserRemoteRepository = UserRemoteRepository(RetrofitClient.userApiService)
) : AndroidViewModel(app) {

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

        if (!Validacion.esNombreValido(s.name)) errs["name"] = "Nombre debe tener al menos 2 caracteres"
        if (!Validacion.esCorreoValido(s.email)) errs["email"] = "Email inválido"
        if (!Validacion.esAdulto(ageInt)) errs["age"] = "Debes ser mayor de 18 años"
        if (!Validacion.esClaveValida(s.pass)) errs["pass"] = "Clave debe tener al menos 6 caracteres"
        if (!Validacion.contrasenasCoinciden(s.pass, s.confirm)) errs["confirm"] = "Las contraseñas no coinciden"
        if (s.refCode.isNotBlank() && !Validacion.esCodigoReferidoValido(s.refCode)) {
            errs["refCode"] = "Código de referido inválido"
        }

        if (errs.isNotEmpty()) {
            _state.value = s.copy(errors = errs)
            return@launch
        }

        _state.value = s.copy(isLoading = true)

        try {
            val referralCode = Validacion.generarCodigoReferido(s.name)

            val userToSend = UsuarioEntidad(
                nombre = s.name.trim(),
                correo = s.email.lowercase(),
                edad = ageInt,
                contrasena = s.pass,
                esDuoc = s.isDuoc,
                codigoReferido = referralCode,
                referidoPor = s.refCode.takeIf { it.isNotBlank() } ?: ""
            )

            val registeredUser = repoRemote.registerUser(userToSend)

            if (registeredUser != null) {
                repoLocal.insertar(registeredUser)
                repoLocal.actualizarEstadoSesion(registeredUser.id, true)

                _state.value = s.copy(
                    isLoading = false,
                    isSuccess = true,
                    currentUser = registeredUser,
                    errors = emptyMap()
                )
            } else {
                _state.value = s.copy(
                    isLoading = false,
                    errors = mapOf("general" to "Error de registro: No se pudo crear el usuario.")
                )
            }
        } catch (e: Exception) {
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
            val user = repoRemote.loginUser(s.email.lowercase(), s.pass)

            if (user != null) {
                val localUser = repoLocal.buscarPorCorreo(user.correo)

                if (localUser != null) {
                    repoLocal.actualizarEstadoSesion(localUser.id, true)
                } else {
                    repoLocal.insertar(user.copy(sesionIniciada = true))
                }

                _state.value = s.copy(
                    isLoading = false,
                    isSuccess = true,
                    currentUser = user,
                    errors = emptyMap()
                )
            } else {
                _state.value = s.copy(
                    isLoading = false,
                    errors = mapOf("general" to "Credenciales inválidas o error de conexión.")
                )
            }
        } catch (e: Exception) {
            _state.value = s.copy(
                isLoading = false,
                errors = mapOf("general" to "Error al iniciar sesión: No se pudo conectar al servidor. (${e.message})")
            )
        }
    }

    fun logout() = viewModelScope.launch {
        // Corrección: Usamos Dispatchers.IO para operaciones de base de datos si fuera necesario,
        // pero aquí viewModelScope ya maneja el hilo adecuado generalmente.
        try {
            val userToLogout = repoLocal.obtenerUsuarioActual()
            if (userToLogout != null) {
                repoLocal.actualizarEstadoSesion(userToLogout.id, false)
            }
        } catch (e: Exception) {
            // Manejo silencioso en logout
        }
        _state.value = AuthState()
    }

    fun clearSuccess() {
        _state.value = _state.value.copy(isSuccess = false)
    }
}