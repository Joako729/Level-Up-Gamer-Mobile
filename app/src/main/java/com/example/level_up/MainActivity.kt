package com.example.level_up

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import com.example.level_up.ui.screens.LevelUpNavHost
import com.example.level_up.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LevelupTheme {
                val permissionsToRequest = arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO
                )

                val launcher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) { permissions ->
                    // Aquí puedes manejar la respuesta del usuario.
                    // Por ejemplo, verificar si los permisos fueron concedidos o denegados.
                }

                // Solicita los permisos cuando la app se inicia
                LaunchedEffect(Unit) {
                    launcher.launch(permissionsToRequest)
                }

                LevelUpNavHost()
            }
        }
    }
}
