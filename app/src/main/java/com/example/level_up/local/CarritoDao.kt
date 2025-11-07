package com.example.level_up.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CarritoDao {

    @Query("SELECT * FROM carrito")
    fun observarCarrito(): Flow<List<CarritoEntidad>>

    @Query("SELECT * FROM carrito WHERE productoId = :productoId LIMIT 1")
    suspend fun obtenerItemPorProductoId(productoId: Int): CarritoEntidad?

    @Query("SELECT COUNT(*) FROM carrito")
    suspend fun obtenerCantidadItems(): Int

    @Query("SELECT SUM(precio * cantidad) FROM carrito")
    suspend fun obtenerTotalCarrito(): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarOActualizar(item: CarritoEntidad)

    @Update
    suspend fun actualizarItem(item: CarritoEntidad)

    @Delete
    suspend fun eliminarItemCarrito(item: CarritoEntidad)

    @Query("DELETE FROM carrito WHERE id = :id")
    suspend fun eliminarPorId(id: Int)

    @Query("DELETE FROM carrito")
    suspend fun limpiar()

    @Query("UPDATE carrito SET cantidad = :cantidad WHERE productoId = :productoId")
    suspend fun actualizarCantidad(productoId: Int, cantidad: Int)
}
