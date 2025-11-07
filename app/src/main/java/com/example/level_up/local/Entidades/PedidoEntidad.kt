package com.example.level_up.local.Entidades

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Pedido")
data class PedidoEntidad(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val usuarioId: Int,
    val montoTotal: Int,
    val montoDescuento: Int = 0,
    val montoFinal: Int,
    val estado: String = "pending", // pending, completed, cancelled
    val fechaCreacion: Long = System.currentTimeMillis(),
    val itemsJson: String = "" // JSON de los ítems del pedido
)