// Archivo: app/src/main/java/com/example/level_up/viewmodel/CatalogViewModel.kt

package com.example.level_up.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.level_up.local.BaseDeDatosApp
import com.example.level_up.local.Entidades.CarritoEntidad
import com.example.level_up.local.Entidades.ProductoEntidad
import com.example.level_up.remote.model.ProductoRemoto
import com.example.level_up.remote.model.Article
import com.example.level_up.remote.service.RetrofitClient
import com.example.level_up.repository.CarritoRepository
import com.example.level_up.repository.ProductoRepository
import com.example.level_up.repository.NewsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class CatalogState(
    val searchQuery: String = "",
    val selectedCategory: String = "Todas",
    val isLoading: Boolean = false,
    val error: String? = null
)

class CatalogViewModel(app: Application) : AndroidViewModel(app) {
    private val db = BaseDeDatosApp.obtener(app)
    private val repo = ProductoRepository(db.ProductoDao())
    private val cartRepo = CarritoRepository(db.CarritoDao())

    private val newsRepo = NewsRepository(RetrofitClient.newsApiService)
    private val apiService = RetrofitClient.apiService

    private val _state = MutableStateFlow(CatalogState())
    val state: StateFlow<CatalogState> = _state.asStateFlow()

    private val _productosAPI = MutableStateFlow<List<ProductoRemoto>>(emptyList())

    private val _gamingNews = MutableStateFlow<List<Article>>(emptyList())
    val gamingNews: StateFlow<List<Article>> = _gamingNews.asStateFlow()

    val products = combine(
        _productosAPI,
        _state
    ) { remoteProducts, state ->
        val products = remoteProducts.map { it.toProductoEntidad() }

        products.filter { product ->
            val matchesCategory = state.selectedCategory == "Todas" || product.categoria == state.selectedCategory
            val matchesSearch = state.searchQuery.isBlank() ||
                    product.nombre.contains(state.searchQuery, ignoreCase = true) ||
                    product.descripcion.contains(state.searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val categories = _productosAPI.map { list ->
        list.map { it.categoria }
            .filterNotNull()
            .distinct()
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        emptyList()
    )

    val featuredProducts = repo.obtenerDestacados().stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        emptyList()
    )

    init {
        cargarProductosDesdeAPI()
        cargarNoticias()

        viewModelScope.launch {
            if (repo.contar() == 0) {
                // Base de datos vacía
            }
        }
    }

    private fun cargarNoticias() {
        viewModelScope.launch {
            val articles = newsRepo.fetchGamingNews()
            _gamingNews.value = articles
        }
    }

    // --- MODIFICADO: Borra datos antiguos antes de insertar nuevos ---
    private fun cargarProductosDesdeAPI() {
        _state.value = _state.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                val response = apiService.obtenerTodosLosProductos()
                if (response.isSuccessful) {
                    val productosRemotos = response.body() ?: emptyList()

                    // 1. Actualizamos la lista en memoria (para el catálogo)
                    _productosAPI.value = productosRemotos

                    // 2. Actualizamos la Base de Datos Local (para el Home/Destacados)
                    val productosEntidad = productosRemotos.map { it.toProductoEntidad() }

                    // IMPORTANTE: Borramos lo viejo para eliminar productos fantasma
                    repo.borrarTodos()

                    // Insertamos lo nuevo
                    repo.insertarTodos(*productosEntidad.toTypedArray())

                    _state.value = _state.value.copy(isLoading = false)
                    Log.d("API_PRODUCTOS", "Sincronización completa: ${productosRemotos.size} productos.")

                } else {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "Error ${response.code()} al cargar productos."
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "No se pudo conectar al servidor. (${e.message})"
                )
                Log.e("API_PRODUCTOS", "Conexión fallida: ${e.message}")
            }
        }
    }

    private fun ProductoRemoto.toProductoEntidad(): ProductoEntidad {
        return ProductoEntidad(
            id = this.id.toInt(),
            codigo = this.codigo ?: "N/A",
            categoria = this.categoria ?: "General",
            nombre = this.nombre ?: "Producto Desconocido",
            precio = this.precio?.toInt() ?: 0,
            stock = this.stock ?: 0,
            valoracion = this.valoracion ?: 0f,
            descripcion = this.descripcion ?: "",
            urlImagen = this.urlImagen ?: "",
            fabricante = this.fabricante ?: "",
            destacado = this.destacado ?: false
        )
    }

    fun updateSearchQuery(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
    }

    fun updateSelectedCategory(category: String) {
        _state.value = _state.value.copy(selectedCategory = category)
    }

    fun addToCart(product: ProductoEntidad) {
        viewModelScope.launch {
            try {
                val existingItem = cartRepo.obtenerItemPorProductoId(product.id)
                if (existingItem != null) {
                    cartRepo.actualizarCantidad(product.id, existingItem.cantidad + 1)
                } else {
                    cartRepo.insertarOActualizar(
                        CarritoEntidad(
                            productoId = product.id,
                            nombre = product.nombre,
                            precio = product.precio,
                            cantidad = 1
                        )
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = "Error al agregar al carrito: ${e.message}")
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}