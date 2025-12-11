package com.example.level_up.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.level_up.local.BaseDeDatosApp
import com.example.level_up.local.Entidades.UsuarioEntidad
import com.example.level_up.local.model.CarritoItemConImagen
import com.example.level_up.remote.service.PedidoRequest
import com.example.level_up.remote.service.RetrofitClient
import com.example.level_up.repository.CarritoRepository
import com.example.level_up.repository.PedidoRemoteRepository
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

// CAMBIO CLAVE: Inyección por constructor con valores por defecto
class CartViewModel(
    app: Application,
    private val cartRepo: CarritoRepository = CarritoRepository(BaseDeDatosApp.obtener(app).CarritoDao()),
    private val orderRepo: PedidoRepository = PedidoRepository(BaseDeDatosApp.obtener(app).PedidoDao()),
    private val userRepo: UsuarioRepository = UsuarioRepository(BaseDeDatosApp.obtener(app).UsuarioDao()),
    private val pedidoRemoteRepo: PedidoRemoteRepository = PedidoRemoteRepository(RetrofitClient.pedidoApiService)
) : AndroidViewModel(app) {

    private val _state = MutableStateFlow(CartState())
    val state: StateFlow<CartState> = _state.asStateFlow()

    private val discountedProducts = setOf("catan", "carcassonne", "controlador inalámbrico kairox x", "auriculares gamer starforge cloud ii")
    private val productDiscountPercentage = 10

    // Nota: observarCarritoConImagenes requiere que el DAO esté vivo.
    // En los tests mockearemos esto.
    val items: StateFlow<List<CarritoItemConImagen>> = cartRepo.observarCarritoConImagenes()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val subtotal: StateFlow<Int> = items
        .map { list -> list.sumOf { it.precio * it.cantidad } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    private val userDiscountPct: StateFlow<Int> = state
        .map { s ->
            val u = s.currentUser
            when {
                u == null -> 0
                u.esDuoc -> 20
                else -> Validacion.obtenerPorcentajeDescuento(u.nivel)
            }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    private val userDiscountAmount: StateFlow<Int> = combine(subtotal, userDiscountPct) { sub, pct ->
        (sub * pct) / 100
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    private val productDiscountAmount: StateFlow<Int> = items.map { list ->
        list.sumOf { item ->
            if (discountedProducts.contains(item.nombre.lowercase())) {
                (item.precio * item.cantidad * productDiscountPercentage) / 100
            } else {
                0
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    val discountAmount: StateFlow<Int> = combine(userDiscountAmount, productDiscountAmount) { userDiscount, productDiscount ->
        userDiscount + productDiscount
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
            try {
                // Validación segura para tests: si el repo es un Mock, esto no fallará
                _state.value = _state.value.copy(currentUser = userRepo.obtenerUsuarioActual())
            } catch (e: Exception) {
                // Ignorar error en inicialización si es entorno de prueba
            }
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
            val subtotalValue = subtotal.value
            val discountAmountValue = discountAmount.value
            val finalAmountValue = finalTotal.value

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
                val orderRequest = PedidoRequest(
                    usuarioId = currentUser.id.toLong(),
                    montoTotal = subtotalValue,
                    montoDescuento = discountAmountValue,
                    montoFinal = finalAmountValue,
                    estado = "completed",
                    fechaCreacion = System.currentTimeMillis(),
                    itemsJson = currentItems.joinToString(";") { "${it.nombre}:${it.cantidad}:${it.precio}" }
                )

                val remoteOrder = pedidoRemoteRepo.crearPedido(orderRequest)

                if (remoteOrder != null) {
                    orderRepo.insertarPedido(remoteOrder)

                    val newTotalPurchases = currentUser.totalCompras + 1
                    val pointsEarned = (finalAmountValue / 1000).toInt()
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