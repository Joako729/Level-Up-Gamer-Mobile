package com.example.level_up.repository

import com.example.level_up.local.CarritoDao
import com.example.level_up.local.CarritoEntidad
import kotlinx.coroutines.flow.Flow

class CarritoRepository(private val dao: CarritoDao) {

    fun observarCarrito(): Flow<List<CarritoEntidad>> = dao.observarCarrito()

    suspend fun obtenerItemPorProductoId(producId: Int) = dao.obtenerItemPorProductoId(producId)

    suspend fun obtenerConteoItems(): Int = dao.obtenerCantidadItems()

    suspend fun obtenerTotalCarrito(): Int = dao.obtenerTotalCarrito() ?: 0

    suspend fun insertarOActualizar(item: CarritoEntidad) = dao.insertarOActualizar(item)

    suspend fun actualizarItemCarrito(item: CarritoEntidad) = dao.actualizarItem(item)

    suspend fun eliminarItemCarrito(item: CarritoEntidad) = dao.eliminarItemCarrito(item)

    suspend fun limpiar() = dao.limpiar()

    suspend fun actualizarCantidad(productId: Int, quantity: Int) = dao.actualizarCantidad(productId, quantity)

    suspend fun eliminarPorId(id: Int) = dao.eliminarPorId(id)
}
