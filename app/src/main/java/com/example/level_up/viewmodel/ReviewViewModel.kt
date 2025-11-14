package com.example.level_up.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.level_up.local.BaseDeDatosApp
import com.example.level_up.local.Entidades.ProductoEntidad
import com.example.level_up.local.Entidades.ReseniaEntidad
import com.example.level_up.local.Entidades.UsuarioEntidad
import com.example.level_up.repository.ProductoRepository
// Se elimina import com.example.level_up.repository.ReseniaRepository
import com.example.level_up.repository.UsuarioRepository
import com.example.level_up.utils.Validacion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
// Se elimina import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
// NUEVOS IMPORTS PARA CONEXIÓN REMOTA
import com.example.level_up.remote.service.RetrofitClient
import com.example.level_up.repository.ReseniaRemoteRepository

data class ReviewState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSubmitting: Boolean = false,
    val submitSuccess: Boolean = false,
    val currentUser: UsuarioEntidad? = null,
    val product: ProductoEntidad? = null,
    val reviews: List<ReseniaEntidad> = emptyList(),
    val averageRating: Float = 0f,
    val reviewCount: Int = 0
)

class ReviewViewModel(app: Application) : AndroidViewModel(app) {

    private val db = BaseDeDatosApp.obtener(app)
    // Se elimina: private val reviewRepo = ReseniaRepository(db.ReseniaDao())
    private val productRepo = ProductoRepository(db.ProductoDao())
    private val userRepo = UsuarioRepository(db.UsuarioDao())

    // NUEVO: Repositorio Remoto
    private val reviewRemoteRepo = ReseniaRemoteRepository(RetrofitClient.reseniaApiService)

    private val _state = MutableStateFlow(ReviewState())
    val state: StateFlow<ReviewState> = _state.asStateFlow()

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                currentUser = userRepo.obtenerUsuarioActual()
            )
        }
    }

    fun loadProductReviews(productId: Int) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val product = productRepo.obtenerPorId(productId)

                // CAMBIO 1: Obtener reseñas desde la API
                val reviews = reviewRemoteRepo.obtenerReseniasPorProducto(productId)

                // CAMBIO 2: Calcular el promedio y el conteo localmente (a partir de los datos de la API)
                val averageRating = if (reviews.isNotEmpty()) {
                    reviews.map { it.valoracion }.average().toFloat()
                } else 0f
                val reviewCount = reviews.size

                _state.value = _state.value.copy(
                    product = product,
                    reviews = reviews,
                    averageRating = averageRating,
                    reviewCount = reviewCount,
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Error al cargar reseñas: ${e.message}"
                )
            }
        }
    }


    fun submitReview(rating: Float, comment: String) {
        viewModelScope.launch {
            val user = _state.value.currentUser
            val product = _state.value.product

            if (user == null) {
                _state.value = _state.value.copy(error = "Debes iniciar sesión para dejar una reseña")
                return@launch
            }
            if (product == null) {
                _state.value = _state.value.copy(error = "Producto no encontrado")
                return@launch
            }
            if (!Validacion.esCalificacionValida(rating)) {
                _state.value = _state.value.copy(error = "Calificación debe estar entre 0 y 5")
                return@launch
            }
            if (!Validacion.esComentarioResenaValido(comment)) {
                _state.value = _state.value.copy(error = "Comentario debe tener al menos 10 caracteres")
                return@launch
            }

            _state.value = _state.value.copy(isSubmitting = true)

            try {

                val reviewToSend = ReseniaEntidad( // Cambiado a reviewToSend para mejor claridad
                    productoId = product.id,
                    usuarioId = user.id,
                    nombreUsuario = user.nombre,
                    comentario = comment.trim(),
                    valoracion = rating
                )

                // CAMBIO 3: Llama al servicio remoto para guardar la reseña
                val remoteReview = reviewRemoteRepo.crearResenia(reviewToSend)

                if (remoteReview != null) {
                    // CAMBIO 4: Se eliminó la lógica de actualizar promedio del producto (ya que el backend no lo hace)

                    // Recarga reseñas en estado (obteniendo la nueva reseña desde la API)
                    loadProductReviews(product.id)

                    _state.value = _state.value.copy(
                        isSubmitting = false,
                        submitSuccess = true,
                        error = null
                    )
                } else {
                    _state.value = _state.value.copy(
                        isSubmitting = false,
                        error = "Error al enviar reseña: No se pudo confirmar la reseña con el servidor."
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isSubmitting = false,
                    error = "Error al enviar reseña: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    fun clearSubmitSuccess() {
        _state.value = _state.value.copy(submitSuccess = false)
    }
}