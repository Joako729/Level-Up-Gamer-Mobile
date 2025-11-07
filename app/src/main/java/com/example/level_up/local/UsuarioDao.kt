package com.example.level_up.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {

    @Query("SELECT * FROM Usuario WHERE correo = :correo LIMIT 1")
    suspend fun buscarPorCorreo(correo: String): UsuarioEntidad?

    @Query("SELECT * FROM Usuario WHERE sesionIniciada = 1 LIMIT 1")
    suspend fun obtenerUsuarioActual(): UsuarioEntidad?

    @Query("SELECT * FROM Usuario WHERE codigoReferido = :codigoReferido LIMIT 1")
    suspend fun buscarPorCodigoReferido(codigoReferido: String): UsuarioEntidad?

    @Query("SELECT * FROM Usuario ORDER BY puntosLevelUp DESC LIMIT 10")
    fun obtenerTopUsuarios(): Flow<List<UsuarioEntidad>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(usuario: UsuarioEntidad): Long

    @Update
    suspend fun actualizar(usuario: UsuarioEntidad)

    @Query("UPDATE Usuario SET sesionIniciada = :sesionIniciada WHERE id = :usuarioId")
    suspend fun actualizarEstadoSesion(usuarioId: Int, sesionIniciada: Boolean)

    @Query("UPDATE Usuario SET puntosLevelUp = :puntos, nivel = :nivel WHERE id = :usuarioId")
    suspend fun actualizarNivelUsuario(usuarioId: Int, puntos: Int, nivel: Int)

    @Query("UPDATE Usuario SET totalCompras = :totalCompras WHERE id = :usuarioId")
    suspend fun actualizarTotalCompras(usuarioId: Int, totalCompras: Int)

    @Query("DELETE FROM Usuario WHERE id = :usuarioId")
    suspend fun eliminarUsuario(usuarioId: Int)
}
