package com.example.level_up.repository

import com.example.level_up.local.ProductoDao
import com.example.level_up.local.ProductoEntidad
import kotlinx.coroutines.flow.Flow

class ProductoRepository(private val dao: ProductoDao) {

    fun observarTodos(): Flow<List<ProductoEntidad>> = dao.observarTodos()

    fun obtenerPorCategoria(categoria: String): Flow<List<ProductoEntidad>> = dao.obtenerPorCategoria(categoria)

    fun obtenerDestacados(): Flow<List<ProductoEntidad>> = dao.obtenerDestacados()

    fun buscarProductos(consulta: String): Flow<List<ProductoEntidad>> = dao.buscarProductos(consulta)

    fun obtenerCategorias(): Flow<List<String>> = dao.obtenerCategorias()

    suspend fun obtenerPorId(idProducto: Int): ProductoEntidad? = dao.obtenerPorId(idProducto)

    suspend fun insertarTodos(vararg productos: ProductoEntidad) = dao.insertarTodos(*productos)

    suspend fun actualizar(producto: ProductoEntidad) = dao.actualizar(producto)

    suspend fun eliminar(producto: ProductoEntidad) = dao.eliminar(producto)

    suspend fun contar(): Int = dao.contar()

    suspend fun actualizarValoracion(idProducto: Int, valoracion: Float) = dao.actualizarValoracion(idProducto, valoracion)
}
