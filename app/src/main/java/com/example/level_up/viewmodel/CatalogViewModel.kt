package com.example.level_up.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.level_up.local.BaseDeDatosApp
import com.example.level_up.local.Entidades.CarritoEntidad
import com.example.level_up.local.Entidades.ProductoEntidad // Se mantiene el modelo local para la BD Room/Carrito
import com.example.level_up.local.model.ProductoRemoto // NUEVO: Importa el modelo de datos de la API
import com.example.level_up.remote.service.RetrofitClient // NUEVO
import com.example.level_up.repository.CarritoRepository
import com.example.level_up.repository.ProductoRepository
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

    // NUEVO: Instancia del servicio Retrofit para la API
    private val apiService = RetrofitClient.apiService

    private val _state = MutableStateFlow(CatalogState())
    val state: StateFlow<CatalogState> = _state.asStateFlow()

    // Variable interna para guardar los productos de la API (será la fuente de verdad)
    private val _productosAPI = MutableStateFlow<List<ProductoRemoto>>(emptyList())

    // Mantenemos la estructura de 'products' pero ahora mapeamos desde la lista de la API
    val products = combine(
        _productosAPI, // Observamos la lista que viene de la API
        _state
    ) { remoteProducts, state ->
        // Mapeamos los productos remotos a la Entidad local si es necesario (para compatibilidad de código antiguo)
        val products = remoteProducts.map { it.toProductoEntidad() }

        // Aplicamos el filtro de búsqueda y categoría
        products.filter { product ->
            val matchesCategory = state.selectedCategory == "Todas" || product.categoria == state.selectedCategory
            val matchesSearch = state.searchQuery.isBlank() ||
                    product.nombre.contains(state.searchQuery, ignoreCase = true) ||
                    product.descripcion.contains(state.searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // NUEVO: La lista de categorías ahora se obtiene de los productos cargados de la API
    val categories = _productosAPI.map { list ->
        list.map { it.categoria }.distinct()
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        emptyList()
    )

    // ESTA LÍNEA SE MANTIENE PARA COMPATIBILIDAD CON LA LÓGICA EXISTENTE DE HOME SCREEN
    val featuredProducts = repo.obtenerDestacados().stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        emptyList()
    )

    init {
        // Ejecutamos la carga desde la API
        cargarProductosDesdeAPI()

        // El bloque de inicialización de productos locales ahora SOLO se usa para datos destacados
        // o si queremos mantener el Room local sincronizado.
        // Lo modificamos para insertar solo si la tabla está vacía y la API falló.
        // Ahora, siempre vamos a la API primero.
        viewModelScope.launch {
            // El repo.contar() ahora es solo para el fallback local.
            if (repo.contar() == 0) {
                // Si la API falla, podrías inicializar con datos locales,
                // pero por ahora, solo cargamos desde la API.
            }
        }
    }

    // ELIMINAMOS LA FUNCIÓN initializeProducts() antigua ya que los datos vienen de la API

    // NUEVA FUNCIÓN: Lógica de conexión a la API de AWS
    private fun cargarProductosDesdeAPI() {
        _state.value = _state.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                val response = apiService.obtenerTodosLosProductos()
                if (response.isSuccessful) {
                    val productosRemotos = response.body() ?: emptyList()

                    // Actualizamos la lista de la API
                    _productosAPI.value = productosRemotos

                    // OPCIONAL: Insertamos los productos remotos en la BD local (Room) para usarlos en Carrito/Detalles/etc.
                    val productosEntidad = productosRemotos.map { it.toProductoEntidad() }
                    repo.insertarTodos(*productosEntidad.toTypedArray())

                    _state.value = _state.value.copy(isLoading = false)
                    Log.d("API_PRODUCTOS", "Cargados ${productosRemotos.size} productos desde AWS.")

                } else {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "Error ${response.code()} al cargar productos."
                    )
                }
            } catch (e: Exception) {
                // Manejar error de conexión (el servidor Spring Boot no está corriendo)
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "No se pudo conectar al servidor. Asegúrate de que Spring Boot esté activo. (${e.message})"
                )
                Log.e("API_PRODUCTOS", "Conexión fallida: ${e.message}")
            }
        }
    }

    // FUNCIÓN DE CONVERSIÓN: Mapea el modelo remoto al modelo local ProductoEntidad
    private fun ProductoRemoto.toProductoEntidad(): ProductoEntidad {
        return ProductoEntidad(
            id = this.id.toInt(),
            codigo = this.codigo,
            categoria = this.categoria,
            nombre = this.nombre,
            precio = this.precio,
            stock = this.stock,
            valoracion = this.valoracion,
            descripcion = this.descripcion,
            urlImagen = this.urlImagen,
            fabricante = this.fabricante,
            destacado = this.destacado
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
                // Lógica de carrito...
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