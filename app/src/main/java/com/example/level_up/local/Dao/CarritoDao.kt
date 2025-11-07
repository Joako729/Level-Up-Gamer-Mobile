package com.example.level_up.local.Dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.level_up.local.Entidades.CarritoEntidad
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

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
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