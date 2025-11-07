package com.example.level_up.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.level_up.local.BaseDeDatosApp
import com.example.level_up.local.PedidoEntidad
import com.example.level_up.local.UsuarioEntidad
import com.example.level_up.repository.PedidoRepository
import com.example.level_up.repository.UsuarioRepository
import com.example.level_up.utils.Validacion
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ProfileState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isEditing: Boolean = false,
    val currentUser: UsuarioEntidad? = null,
    val userOrders: List<PedidoEntidad> = emptyList(),
    val totalSpent: Int = 0
)

class ProfileViewModel(app: Application) : AndroidViewModel(app) {
    private val db = BaseDeDatosApp.obtener(app)
    private val userRepo = UsuarioRepository(db.UsuarioDao())
    private val orderRepo = PedidoRepository(db.PedidoDao())

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

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

    fun updateUser(updatedUser: UsuarioEntidad) {
        viewModelScope.launch {
            try {
                userRepo.actualizar(updatedUser)
                _state.value = _state.value.copy(
                    currentUser = updatedUser,
                    isEditing = false,
                    error = null
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "Error al actualizar el perfil: ${e.message}"
                )
            }
        }
    }

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
