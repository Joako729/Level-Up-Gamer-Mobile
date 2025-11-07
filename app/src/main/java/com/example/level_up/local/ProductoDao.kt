package com.example.level_up.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductoDao {

    @Query("SELECT * FROM Producto ORDER BY categoria, nombre")
    fun observarTodos(): Flow<List<ProductoEntidad>>

    @Query("SELECT * FROM Producto WHERE categoria = :categoria ORDER BY nombre")
    fun obtenerPorCategoria(categoria: String): Flow<List<ProductoEntidad>>

    @Query("SELECT * FROM Producto WHERE destacado = 1 ORDER BY valoracion DESC")
    fun obtenerDestacados(): Flow<List<ProductoEntidad>>

    @Query("SELECT * FROM Producto WHERE nombre LIKE '%' || :busqueda || '%' OR descripcion LIKE '%' || :busqueda || '%' ORDER BY nombre")
    fun buscarProductos(busqueda: String): Flow<List<ProductoEntidad>>

    @Query("SELECT DISTINCT categoria FROM Producto ORDER BY categoria")
    fun obtenerCategorias(): Flow<List<String>>

    @Query("SELECT * FROM Producto WHERE id = :productoId")
    suspend fun obtenerPorId(productoId: Int): ProductoEntidad?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(vararg productos: ProductoEntidad)

    @Update
    suspend fun actualizar(producto: ProductoEntidad)

    @Delete
    suspend fun eliminar(producto: ProductoEntidad)

    // Utilitarias
    @Query("SELECT COUNT(*) FROM Producto")
    suspend fun contar(): Int

    @Query("UPDATE Producto SET valoracion = :valoracion WHERE id = :productoId")
    suspend fun actualizarValoracion(productoId: Int, valoracion: Float)
}
