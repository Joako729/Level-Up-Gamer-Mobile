package com.example.level_up.local.Dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.level_up.local.Entidades.PedidoEntidad
import kotlinx.coroutines.flow.Flow

@Dao
interface PedidoDao {

    @Query("SELECT * FROM Pedido WHERE usuarioId = :usuarioId ORDER BY fechaCreacion DESC")
    fun obtenerPedidosPorUsuarioId(usuarioId: Int): Flow<List<PedidoEntidad>>

    @Query("SELECT * FROM Pedido WHERE id = :pedidoId")
    suspend fun obtenerPedidoPorId(pedidoId: Int): PedidoEntidad?

    @Query("SELECT COUNT(*) FROM Pedido WHERE usuarioId = :usuarioId")
    suspend fun contarPedidosPorUsuario(usuarioId: Int): Int

    @Query("SELECT SUM(montoFinal) FROM Pedido WHERE usuarioId = :usuarioId AND estado = 'completed'")
    suspend fun obtenerTotalGastado(usuarioId: Int): Int?

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertarPedido(pedido: PedidoEntidad): Long

    @Update
    suspend fun actualizarPedido(pedido: PedidoEntidad)

    @Delete
    suspend fun eliminarPedido(pedido: PedidoEntidad)
}