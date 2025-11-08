package com.example.level_up.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.level_up.local.BaseDeDatosApp
import com.example.level_up.local.Entidades.CarritoEntidad
import com.example.level_up.local.Entidades.ProductoEntidad
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

    private val _state = MutableStateFlow(CatalogState())
    val state: StateFlow<CatalogState> = _state.asStateFlow()

    val categories = repo.obtenerCategorias().stateIn(
        viewModelScope, 
        SharingStarted.Eagerly, 
        emptyList()
    )

    val products = combine(
        repo.observarTodos(),
        _state
    ) { products, state ->
        products.filter { product ->
            val matchesCategory = state.selectedCategory == "Todas" || product.categoria == state.selectedCategory
            val matchesSearch = state.searchQuery.isBlank() || 
                product.nombre.contains(state.searchQuery, ignoreCase = true) ||
                product.descripcion.contains(state.searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val featuredProducts = repo.obtenerDestacados().stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        emptyList()
    )

    init {
        viewModelScope.launch {
            if (repo.contar() == 0) {
                initializeProducts()
            }
        }
    }

    private suspend fun initializeProducts() {
        val sampleProducts = listOf(
            ProductoEntidad(
                codigo = "JM001",
                categoria = "Juegos de Mesa",
                nombre = "Catan",
                precio = 35269,
                stock = 8,
                urlImagen = "catan",
                descripcion = "Un clásico juego de estrategia donde los jugadores compiten por colonizar y expandirse en la isla de Catan. Ideal para 3-4 jugadores.",
                fabricante = "Catan Studio",
                destacado = true
            ),
            ProductoEntidad(
                codigo = "JM002",
                categoria = "Juegos de Mesa",
                nombre = "Carcassonne",
                precio = 29389,
                stock = 6,
                urlImagen = "carcassonne",
                descripcion = "Un juego de colocación de fichas donde los jugadores construyen el paisaje alrededor de la fortaleza medieval de Carcassonne.",
                fabricante ="Z-Man Games",
                destacado = true
            ),
            ProductoEntidad(
                codigo = "AC001",
                categoria = "Accesorios",
                nombre = "Controlador Inalámbrico Kairox X",
                precio = 70549,
                stock = 12,
                urlImagen = "controlador",
                descripcion = "Ofrece una experiencia de juego cómoda con botones mapeables y una respuesta táctil mejorada.",
                fabricante = "Microsoft",
                destacado = true
            ),
            ProductoEntidad(
                codigo = "AC002",
                categoria = "Accesorios",
                nombre = "Auriculares Gamer Starforge Cloud II",
                precio = 94069,
                stock = 6,
                urlImagen = "auri",
                descripcion = "Proporcionan un sonido envolvente de calidad con un micrófono desmontable y almohadillas de espuma viscoelástica.",
                fabricante = "Starforge",
                destacado = true
            ),
            ProductoEntidad(
                codigo = "CO001",
                categoria = "Consolas",
                nombre = "Zenith Pro",
                precio = 646789,
                stock = 5,
                urlImagen = "play5",
                descripcion = "La consola de última generación de Sony, que ofrece gráficos impresionantes y tiempos de carga ultrarrápidos.",
                fabricante = "Sony",
                destacado = true
            ),
            ProductoEntidad(
                codigo = "CG001",
                categoria = "Computadores Gamers",
                nombre = "PC Gamer ASUS ROG Strix",
                precio = 1528789,
                stock = 3,
                urlImagen = "pcgamer",
                descripcion = "Un potente equipo diseñado para los gamers más exigentes, equipado con los últimos componentes.",
                fabricante = "ASUS",
                destacado = true
            ),
            ProductoEntidad(
                codigo = "SG001",
                categoria = "Sillas Gamers",
                nombre = "Silla Gamer Secretlab Titan",
                precio = 411589,
                stock = 4,
                urlImagen = "sillagamer",
                descripcion = "Diseñada para el máximo confort, esta silla ofrece un soporte ergonómico y personalización ajustable.",
                fabricante = "Secretlab",
                destacado = false
            ),
            ProductoEntidad(
                codigo = "MS001",
                categoria = "Mouse",
                nombre = "Mouse Gamer Orionis G502 HERO",
                precio = 58789,
                stock = 10,
                urlImagen = "mousegamer",
                descripcion = "Con sensor de alta precisión y botones personalizables, este mouse es ideal para gamers que buscan control preciso.",
                fabricante = "Orionis",
                destacado = false
            ),
            ProductoEntidad(
                codigo = "MP001",
                categoria = "Mousepad",
                nombre = "Mousepad Heliox Goliathus Extended Chroma",
                precio = 35269,
                stock = 8,
                urlImagen = "mousepad",
                descripcion = "Ofrece un área de juego amplia con iluminación RGB personalizable, asegurando una superficie suave y uniforme.",
                fabricante = "Heliox",
                destacado = false
            ),
            ProductoEntidad(
                codigo = "PP001",
                categoria = "Poleras Personalizadas",
                nombre = "Polera Gamer Personalizada 'Level-Up'",
                precio = 17629,
                stock = 20,
                urlImagen = "polera",
                descripcion = "Una camiseta cómoda y estilizada, con la posibilidad de personalizarla con tu gamer tag o diseño favorito.",
                fabricante = "Level-Up Gamer",
                destacado = false
            )
        )
        repo.insertarTodos(*sampleProducts.toTypedArray())
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
