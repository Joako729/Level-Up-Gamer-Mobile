package com.example.level_up.repository

import com.example.level_up.local.Dao.PedidoDao
import com.example.level_up.local.Entidades.PedidoEntidad
import kotlinx.coroutines.flow.Flow

class PedidoRepository(private val dao: PedidoDao) {

    fun obtenerPedidosPorUsuarioId(usuarioId: Int): Flow<List<PedidoEntidad>> = dao.obtenerPedidosPorUsuarioId(usuarioId)

    suspend fun obtenerPedidoPorId(idPedido: Int): PedidoEntidad? = dao.obtenerPedidoPorId(idPedido)

    suspend fun insertarPedido(pedido: PedidoEntidad): Long = dao.insertarPedido(pedido)

    suspend fun actualizarPedido(pedido: PedidoEntidad) = dao.actualizarPedido(pedido)

    suspend fun eliminarPedido(pedido: PedidoEntidad) = dao.eliminarPedido(pedido)

    suspend fun contarPedidosPorUsuario(usuarioId: Int): Int = dao.contarPedidosPorUsuario(usuarioId)

    suspend fun obtenerTotalGastado(usuarioId: Int): Int? = dao.obtenerTotalGastado(usuarioId)
}
