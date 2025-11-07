package com.example.level_up.repository

import com.example.level_up.local.Dao.UsuarioDao
import com.example.level_up.local.Entidades.UsuarioEntidad
import kotlinx.coroutines.flow.Flow

class UsuarioRepository(private val dao: UsuarioDao) {

    suspend fun insertar(usuario: UsuarioEntidad): Long = dao.insertar(usuario)

    suspend fun buscarPorCorreo(correo: String): UsuarioEntidad? = dao.buscarPorCorreo(correo)

    suspend fun obtenerUsuarioActual(): UsuarioEntidad? = dao.obtenerUsuarioActual()

    suspend fun buscarPorCodigoReferido(codigoReferido: String): UsuarioEntidad? = dao.buscarPorCodigoReferido(codigoReferido)

    fun obtenerTopUsuarios(): Flow<List<UsuarioEntidad>> = dao.obtenerTopUsuarios()

    suspend fun actualizar(usuario: UsuarioEntidad) = dao.actualizar(usuario)

    suspend fun actualizarEstadoSesion(usuarioId: Int, sesionIniciada: Boolean) = dao.actualizarEstadoSesion(usuarioId, sesionIniciada)

    suspend fun actualizarNivelUsuario(usuarioId: Int, puntos: Int, nivel: Int) = dao.actualizarNivelUsuario(usuarioId, puntos, nivel)

    suspend fun actualizarTotalCompras(usuarioId: Int, totalCompras: Int) = dao.actualizarTotalCompras(usuarioId, totalCompras)

    suspend fun eliminarUsuario(usuarioId: Int) = dao.eliminarUsuario(usuarioId)
}
