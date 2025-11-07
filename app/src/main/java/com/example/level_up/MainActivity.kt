package com.example.level_up

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.level_up.ui.theme.*
import com.example.level_up.ui.screens.LevelUpNavHost


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            LevelupTheme {
                LevelUpNavHost()
            }
        }
    }
}