package com.example.level_up.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.level_up.local.BaseDeDatosApp
import com.example.level_up.local.Entidades.ProductoEntidad
import com.example.level_up.local.Entidades.ReseniaEntidad
import com.example.level_up.local.Entidades.UsuarioEntidad
import com.example.level_up.repository.ProductoRepository
import com.example.level_up.repository.ReseniaRepository
import com.example.level_up.repository.UsuarioRepository
import com.example.level_up.utils.Validacion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

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
    private val reviewRepo = ReseniaRepository(db.ReseniaDao())
    private val productRepo = ProductoRepository(db.ProductoDao())
    private val userRepo = UsuarioRepository(db.UsuarioDao())

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
                val reviews = reviewRepo.obtenerReseniasPorProducto(productId).first()
                val averageRating = reviewRepo.obtenerPromedioResenas(productId) ?: 0f
                val reviewCount = reviewRepo.contarResenas(productId)

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

                val review = ReseniaEntidad(
                    productoId = product.id,
                    usuarioId = user.id,
                    nombreUsuario = user.nombre,
                    comentario = comment.trim(),
                    valoracion = rating
                )

                reviewRepo.insertarResena(review)

                // Actualiza promedio del producto
                val nuevoPromedio = reviewRepo.obtenerPromedioResenas(product.id) ?: rating
                productRepo.actualizarValoracion(product.id, nuevoPromedio)

                // Recarga reseñas en estado
                loadProductReviews(product.id)

                _state.value = _state.value.copy(
                    isSubmitting = false,
                    submitSuccess = true,
                    error = null
                )
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
