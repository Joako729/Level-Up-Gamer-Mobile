package com.example.level_up.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.level_up.local.BaseDeDatosApp
import com.example.level_up.local.Entidades.UsuarioEntidad
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
    private val repo = UsuarioRepository(BaseDeDatosApp.obtener(app).UsuarioDao())
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

        // Validation
        if (!Validacion.esNombreValido(s.name)) errs["name"] = "Nombre debe tener al menos 2 caracteres"
        if (!Validacion.esCorreoValido(s.email)) errs["email"] = "Email inválido"
        if (!Validacion.esAdulto(ageInt)) errs["age"] = "Debes ser mayor de 18 años"
        if (!Validacion.esClaveValida(s.pass)) errs["pass"] = "Clave debe tener al menos 6 caracteres"
        if (!Validacion.contrasenasCoinciden(s.pass, s.confirm)) errs["confirm"] = "Las contraseñas no coinciden"
        if (s.refCode.isNotBlank() && !Validacion.esCodigoReferidoValido(s.refCode)) {
            errs["refCode"] = "Código de referido inválido"
        }

        if (repo.buscarPorCorreo(s.email) != null) {
            errs["email"] = "Este correo ya está registrado"
        }

        if (errs.isNotEmpty()) {
            _state.value = s.copy(errors = errs)
            return@launch
        }

        _state.value = s.copy(isLoading = true)

        try {
            val referralCode = Validacion.generarCodigoReferido(s.name)
            val user = UsuarioEntidad(
                nombre = s.name.trim(),
                correo = s.email.lowercase(),
                edad = ageInt,
                contrasena = s.pass,
                esDuoc = s.isDuoc,
                codigoReferido = referralCode,
                referidoPor = s.refCode.takeIf { it.isNotBlank() } ?: ""
            )

            repo.insertar(user)
            _state.value = s.copy(
                isLoading = false,
                isSuccess = true,
                errors = emptyMap()
            )
        } catch (e: Exception) {
            _state.value = s.copy(
                isLoading = false,
                errors = mapOf("general" to "Error al registrar usuario: ${e.message}")
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
            val user = repo.buscarPorCorreo(s.email.lowercase())
            if (user != null && user.contrasena == s.pass) {
                repo.actualizarEstadoSesion(user.id, true)
                _state.value = s.copy(
                    isLoading = false,
                    isSuccess = true,
                    currentUser = user,
                    errors = emptyMap()
                )
            } else {
                _state.value = s.copy(
                    isLoading = false,
                    errors = mapOf("general" to "Credenciales inválidas")
                )
            }
        } catch (e: Exception) {
            _state.value = s.copy(
                isLoading = false,
                errors = mapOf("general" to "Error al iniciar sesión: ${e.message}")
            )
        }
    }

    fun logout() = viewModelScope.launch {
        val currentUser = _state.value.currentUser
        if (currentUser != null) {
            repo.actualizarEstadoSesion(currentUser.id, false)
        }
        _state.value = AuthState()
    }

    fun clearSuccess() {
        _state.value = _state.value.copy(isSuccess = false)
    }
}
