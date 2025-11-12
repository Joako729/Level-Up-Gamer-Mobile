package com.example.level_up.local.Dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.example.level_up.local.Entidades.AppReseniaEntidad

@Dao
interface AppReseniaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarResena(resenia: AppReseniaEntidad)
}
