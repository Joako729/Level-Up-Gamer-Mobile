package com.example.level_up.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.level_up.local.BaseDeDatosApp
import com.example.level_up.local.Entidades.CarritoEntidad
import com.example.level_up.local.Entidades.PedidoEntidad
import com.example.level_up.local.Entidades.UsuarioEntidad
import com.example.level_up.local.model.CarritoItemConImagen
import com.example.level_up.remote.service.RetrofitClient // IMPORTADO
import com.example.level_up.remote.service.PedidoRequest // IMPORTADO
import com.example.level_up.repository.CarritoRepository
import com.example.level_up.repository.PedidoRepository
import com.example.level_up.repository.PedidoRemoteRepository // IMPORTADO
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
    private val pedidoRemoteRepo = PedidoRemoteRepository(RetrofitClient.pedidoApiService) // NUEVO REPOSITORIO

    private val _state = MutableStateFlow(CartState())
    val state: StateFlow<CartState> = _state.asStateFlow()

    val items: StateFlow<List<CarritoItemConImagen>> = cartRepo.observarCarritoConImagenes()
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

    fun updateQuantity(item: CarritoItemConImagen, newQuantity: Int) {
        viewModelScope.launch {
            try {
                if (newQuantity <= 0) {
                    cartRepo.eliminarPorId(item.id)
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
                // 1. Calcular montos
                val subtotal = currentItems.sumOf { it.precio * it.cantidad }
                val discountPercentage = if (currentUser.esDuoc) 20 else Validacion.obtenerPorcentajeDescuento(currentUser.nivel)
                val discountAmount = (subtotal * discountPercentage) / 100
                val finalAmount = subtotal - discountAmount

                // 2. Crear el DTO para el backend
                val orderRequest = PedidoRequest(
                    usuarioId = currentUser.id.toLong(), // Convertir Int a Long para el backend
                    montoTotal = subtotal,
                    montoDescuento = discountAmount,
                    montoFinal = finalAmount,
                    estado = "completed",
                    fechaCreacion = System.currentTimeMillis(),
                    itemsJson = currentItems.joinToString(";") { "${it.nombre}:${it.cantidad}:${it.precio}" }
                )

                // 3. LLAMAR AL SERVICIO REMOTO
                val remoteOrder = pedidoRemoteRepo.crearPedido(orderRequest)

                if (remoteOrder != null) {
                    // 4. Si el pedido es exitoso en el backend, guárdalo en el historial local (Room)
                    orderRepo.insertarPedido(remoteOrder)

                    // 5. Actualizar estadísticas del usuario (localmente)
                    val newTotalPurchases = currentUser.totalCompras + 1
                    val pointsEarned = (finalAmount / 1000).toInt() // 1 punto por cada 1000 CLP
                    val newPoints = currentUser.puntosLevelUp + pointsEarned
                    val newLevel = Validacion.calcularNivel(newPoints)

                    userRepo.actualizarTotalCompras(currentUser.id, newTotalPurchases)
                    userRepo.actualizarNivelUsuario(currentUser.id, newPoints, newLevel)

                    // 6. Limpiar carrito (localmente)
                    cartRepo.limpiar()

                    // 7. Actualizar estado
                    _state.value = _state.value.copy(
                        isProcessingOrder = false,
                        orderSuccess = true,
                        error = null
                    )
                } else {
                    _state.value = _state.value.copy(
                        isProcessingOrder = false,
                        error = "Error al procesar la orden: No se pudo confirmar el pedido con el servidor."
                    )
                }

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