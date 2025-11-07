package com.example.level_up.local.Dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.level_up.local.Entidades.ReseniaEntidad
import kotlinx.coroutines.flow.Flow

@Dao
interface ReseniaDao {
    @Query("SELECT * FROM Resenia WHERE productoId = :productId ORDER BY fechaCreacion DESC")
    fun obtenerReseniasPorProducto(productId: Int): Flow<List<ReseniaEntidad>>

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertarResena(resenia: ReseniaEntidad)

    @Update
    suspend fun actualizarResena(resenia: ReseniaEntidad)

    @Delete
    suspend fun eliminarResena(resenia: ReseniaEntidad)

    @Query("SELECT AVG(valoracion) FROM Resenia WHERE productoId = :productId")
    suspend fun obtenerPromedioResenas(productId: Int): Float?

    @Query("SELECT COUNT(*) FROM Resenia WHERE productoId = :productId")
    suspend fun contarResenas(productId: Int): Int
}