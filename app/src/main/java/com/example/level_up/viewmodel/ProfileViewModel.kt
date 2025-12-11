package com.example.level_up.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.level_up.local.BaseDeDatosApp
import com.example.level_up.local.Entidades.AppReseniaEntidad
import com.example.level_up.local.Entidades.PedidoEntidad
import com.example.level_up.local.Entidades.UsuarioEntidad
import com.example.level_up.repository.AppReseniaRepository
import com.example.level_up.repository.PedidoRepository
import com.example.level_up.repository.UsuarioRepository
import com.example.level_up.utils.Validacion
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
// Imports para conexión remota
import com.example.level_up.remote.service.RetrofitClient
import com.example.level_up.repository.AppReseniaRemoteRepository
import com.example.level_up.repository.UserRemoteRepository

data class ProfileState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isEditing: Boolean = false,
    val currentUser: UsuarioEntidad? = null,
    val userOrders: List<PedidoEntidad> = emptyList(),
    val totalSpent: Int = 0,
    val isSubmittingReview: Boolean = false,
    val reviewSubmitSuccess: Boolean = false
)

class ProfileViewModel(app: Application) : AndroidViewModel(app) {
    private val db = BaseDeDatosApp.obtener(app)

    // Repositorios Locales
    private val userRepo = UsuarioRepository(db.UsuarioDao())
    private val orderRepo = PedidoRepository(db.PedidoDao())
    private val appReseniaRepo = AppReseniaRepository(db.AppReseniaDao())

    // Repositorios Remotos
    private val appReseniaRemoteRepo = AppReseniaRemoteRepository(RetrofitClient.appReseniaApiService)
    // Instanciamos el repositorio remoto de usuarios aquí
    private val userRemoteRepo = UserRemoteRepository(RetrofitClient.userApiService)

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    init {
        loadUserData()
    }

    fun loadUserData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val user = userRepo.obtenerUsuarioActual()
                if (user != null) {
                    val orders = orderRepo.obtenerPedidosPorUsuarioId(user.id).first()
                    val totalSpent = orderRepo.obtenerTotalGastado(user.id) ?: 0

                    _state.value = _state.value.copy(
                        currentUser = user,
                        userOrders = orders,
                        totalSpent = totalSpent,
                        isLoading = false,
                        error = null
                    )
                } else {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        currentUser = null,
                        error = "Usuario no encontrado. Por favor, inicie sesión."
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Error al cargar datos del perfil: ${e.message}"
                )
            }
        }
    }

    fun submitAppReview(rating: Float, comment: String) {
        viewModelScope.launch {
            val user = _state.value.currentUser
            if (user == null) {
                _state.value = _state.value.copy(error = "Debes iniciar sesión para dejar una reseña")
                return@launch
            }

            _state.value = _state.value.copy(isSubmittingReview = true)

            try {
                val reviewToSend = AppReseniaEntidad(
                    usuarioId = user.id,
                    nombreUsuario = user.nombre,
                    valoracion = rating,
                    comentario = comment.trim()
                )

                // Llama al servicio remoto para guardar la reseña
                val remoteReview = appReseniaRemoteRepo.crearResenia(reviewToSend)

                if (remoteReview != null) {
                    _state.value = _state.value.copy(
                        isSubmittingReview = false,
                        reviewSubmitSuccess = true,
                        error = null
                    )
                } else {
                    _state.value = _state.value.copy(
                        isSubmittingReview = false,
                        error = "Error al enviar la reseña: El servidor no confirmó el guardado."
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isSubmittingReview = false,
                    error = "Error al enviar la reseña: ${e.message}"
                )
            }
        }
    }

    fun clearReviewSubmitSuccess() {
        _state.value = _state.value.copy(reviewSubmitSuccess = false)
    }

    // --- FUNCIONES ACTUALIZADAS: UPDATE Y DELETE CON BACKEND ---

    fun updateUser(updatedUser: UsuarioEntidad) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true) // Indicamos carga
            try {
                // 1. PRIMERO intenta actualizar en el servidor (PUT)
                // Convertimos el ID local a Long asumiendo correspondencia con el backend
                val remoteResult = userRemoteRepo.updateUser(updatedUser.id.toLong(), updatedUser)

                // 2. Si el servidor responde bien, actualiza la base de datos local
                if (remoteResult != null) {
                    userRepo.actualizar(updatedUser)
                    _state.value = _state.value.copy(
                        currentUser = updatedUser,
                        isEditing = false,
                        isLoading = false,
                        error = null
                    )
                } else {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "El servidor no pudo actualizar el perfil."
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Error de conexión al actualizar: ${e.message}"
                )
            }
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            val user = _state.value.currentUser ?: return@launch
            _state.value = _state.value.copy(isLoading = true)

            try {
                // 1. Llamada al endpoint DELETE
                val success = userRemoteRepo.deleteUser(user.id.toLong())

                if (success) {
                    // 2. Si el servidor lo borró, bórralo de la app local
                    userRepo.eliminarUsuario(user.id)

                    // 3. Cerrar sesión forzosamente limpiando el estado
                    _state.value = ProfileState( // Reinicia el estado a valores por defecto
                        currentUser = null,
                        isLoading = false,
                        isEditing = false,
                        error = null
                    )
                } else {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "No se pudo eliminar la cuenta en el servidor."
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Error al eliminar: ${e.message}"
                )
            }
        }
    }

    // -----------------------------------------------------------

    fun toggleEditMode() {
        _state.value = _state.value.copy(isEditing = !_state.value.isEditing)
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    fun getUserLevelInfo(): Pair<Int, Int> {
        val user = _state.value.currentUser ?: return Pair(1, 0)
        val currentLevel = user.nivel
        val pointsForNextLevel = when (currentLevel) {
            1 -> 500
            2 -> 2352
            3 -> 5880
            4 -> 11760
            else -> 0
        }
        val pointsNeeded = (pointsForNextLevel - user.puntosLevelUp).coerceAtLeast(0)
        return Pair(currentLevel + 1, pointsNeeded)
    }

    fun getDiscountPercentage(): Int {
        val user = _state.value.currentUser ?: return 0
        return if (user.esDuoc) 20 else Validacion.obtenerPorcentajeDescuento(user.nivel)
    }
}