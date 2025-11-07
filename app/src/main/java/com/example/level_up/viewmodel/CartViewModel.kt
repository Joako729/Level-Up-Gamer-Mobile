package com.example.level_up.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.level_up.local.BaseDeDatosApp
import com.example.level_up.local.CarritoEntidad
import com.example.level_up.local.PedidoEntidad
import com.example.level_up.local.UsuarioEntidad
import com.example.level_up.repository.CarritoRepository
import com.example.level_up.repository.PedidoRepository
import com.example.level_up.repository.UsuarioRepository
import com.example.level_up.utils.Validacion
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class CartState(
    val isLoading: Boolean = false,
    val isProcessingOrder: Boolean = false,
    val error: String? = null,
    val orderSuccess: Boolean = false,
    val currentUser: UsuarioEntidad? = null
)

class CartViewModel(app: Application) : AndroidViewModel(app) {
    private val db = BaseDeDatosApp.obtener(app)
    private val cartRepo = CarritoRepository(db.CarritoDao())
    private val orderRepo = PedidoRepository(db.PedidoDao())
    private val userRepo = UsuarioRepository(db.UsuarioDao())

    private val _state = MutableStateFlow(CartState())
    val state: StateFlow<CartState> = _state.asStateFlow()

    val items: StateFlow<List<CarritoEntidad>> = cartRepo.observarCarrito()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val subtotal: StateFlow<Int> = items
        .map { list -> list.sumOf { it.precio * it.cantidad } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    val discountPct: StateFlow<Int> = state
        .map { s ->
            val u = s.currentUser
            when {
                u == null -> 0
                u.esDuoc -> 20
                else -> Validacion.obtenerPorcentajeDescuento(u.nivel)
            }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    val discountAmount: StateFlow<Int> = combine(subtotal, discountPct) { sub, pct ->
        (sub * pct) / 100
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    val finalTotal: StateFlow<Int> = combine(subtotal, discountAmount) { sub, disc ->
        (sub - disc).coerceAtLeast(0)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    val itemCount: StateFlow<Int> = items
        .map { list -> list.sumOf { it.cantidad } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            _state.value = _state.value.copy(currentUser = userRepo.obtenerUsuarioActual())
        }
    }

    fun updateQuantity(item: CarritoEntidad, newQuantity: Int) {
        viewModelScope.launch {
            try {
                if (newQuantity <= 0) {
                    cartRepo.eliminarItemCarrito(item)
                } else {
                    cartRepo.actualizarCantidad(item.productoId, newQuantity)
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = "Error al actualizar cantidad: ${e.message}")
            }
        }
    }

    fun removeById(id: Int) {
        viewModelScope.launch {
            try {
                cartRepo.eliminarPorId(id)
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = "Error al eliminar producto: ${e.message}")
            }
        }
    }

    fun removeItem(item: CarritoEntidad) {
        viewModelScope.launch {
            try {
                cartRepo.eliminarItemCarrito(item)
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = "Error al eliminar producto: ${e.message}")
            }
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            try {
                cartRepo.limpiar()
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = "Error al vaciar carrito: ${e.message}")
            }
        }
    }

    fun processOrder() {
        viewModelScope.launch {
            val currentItems = items.value
            val currentUser = _state.value.currentUser

            if (currentItems.isEmpty()) {
                _state.value = _state.value.copy(error = "El carrito está vacío")
                return@launch
            }

            if (currentUser == null) {
                _state.value = _state.value.copy(error = "Debes iniciar sesión para realizar una compra")
                return@launch
            }

            _state.value = _state.value.copy(isProcessingOrder = true)

            try {
                val subtotal = currentItems.sumOf { it.precio * it.cantidad }
                val discountPercentage = if (currentUser.esDuoc) 20 else Validacion.obtenerPorcentajeDescuento(currentUser.nivel)
                val discountAmount = (subtotal * discountPercentage) / 100
                val finalAmount = subtotal - discountAmount

                val order = PedidoEntidad(
                    usuarioId = currentUser.id,
                    montoTotal = subtotal,
                    montoDescuento = discountAmount,
                    montoFinal = finalAmount,
                    estado = "completed",
                    itemsJson = currentItems.joinToString(";") { "${it.nombre}:${it.cantidad}:${it.precio}" }
                )

                orderRepo.insertarPedido(order)

                // Update user stats
                val newTotalPurchases = currentUser.totalCompras + 1
                val pointsEarned = (finalAmount / 1000).toInt() // 1 point per 1000 CLP
                val newPoints = currentUser.puntosLevelUp + pointsEarned
                val newLevel = Validacion.calcularNivel(newPoints)

                userRepo.actualizarTotalCompras(currentUser.id, newTotalPurchases)
                userRepo.actualizarNivelUsuario(currentUser.id, newPoints, newLevel)

                cartRepo.limpiar()

                _state.value = _state.value.copy(
                    isProcessingOrder = false,
                    orderSuccess = true,
                    error = null
                )

            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isProcessingOrder = false,
                    error = "Error al procesar la orden: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    fun clearOrderSuccess() {
        _state.value = _state.value.copy(orderSuccess = false)
    }

    fun getDiscountPercentage(): Int {
        val user = _state.value.currentUser ?: return 0
        return if (user.esDuoc) 20 else Validacion.obtenerPorcentajeDescuento(user.nivel)
    }
}
