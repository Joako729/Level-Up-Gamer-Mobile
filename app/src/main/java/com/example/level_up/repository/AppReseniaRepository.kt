package com.example.level_up.repository

import com.example.level_up.local.Dao.AppReseniaDao
import com.example.level_up.local.Entidades.AppReseniaEntidad

class AppReseniaRepository(private val dao: AppReseniaDao) {

    suspend fun insertarResena(resenia: AppReseniaEntidad) {
        dao.insertarResena(resenia)
    }
}
