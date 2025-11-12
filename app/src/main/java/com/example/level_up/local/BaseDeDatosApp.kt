package com.example.level_up.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.level_up.local.Dao.AppReseniaDao
import com.example.level_up.local.Dao.CarritoDao
import com.example.level_up.local.Dao.PedidoDao
import com.example.level_up.local.Dao.ProductoDao
import com.example.level_up.local.Dao.ReseniaDao
import com.example.level_up.local.Dao.UsuarioDao
import com.example.level_up.local.Entidades.AppReseniaEntidad
import com.example.level_up.local.Entidades.CarritoEntidad
import com.example.level_up.local.Entidades.PedidoEntidad
import com.example.level_up.local.Entidades.ProductoEntidad
import com.example.level_up.local.Entidades.ReseniaEntidad
import com.example.level_up.local.Entidades.UsuarioEntidad

@Database(
    entities = [
        ProductoEntidad::class,
        UsuarioEntidad::class,
        CarritoEntidad::class,
        ReseniaEntidad::class,
        PedidoEntidad::class,
        AppReseniaEntidad::class
    ],
    version = 3,
    exportSchema = false
)
abstract class BaseDeDatosApp : RoomDatabase() {

    abstract fun ProductoDao(): ProductoDao
    abstract fun UsuarioDao(): UsuarioDao
    abstract fun CarritoDao(): CarritoDao
    abstract fun ReseniaDao(): ReseniaDao
    abstract fun PedidoDao(): PedidoDao
    abstract fun AppReseniaDao(): AppReseniaDao

    companion object {
        @Volatile private var INSTANCIA: BaseDeDatosApp? = null

        fun obtener(contexto: Context): BaseDeDatosApp =
            INSTANCIA ?: synchronized(this) {
                INSTANCIA ?: Room.databaseBuilder(
                    contexto.applicationContext,
                    BaseDeDatosApp::class.java,
                    "levelup.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCIA = it }
            }
    }
}
