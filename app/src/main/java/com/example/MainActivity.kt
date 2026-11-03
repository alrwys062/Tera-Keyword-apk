package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.ui.AppDestination
import com.example.ui.MainScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private var initialDestination by mutableStateOf<AppDestination?>(null)

    override fun attachBaseContext(newBase: android.content.Context) {
        super.attachBaseContext(com.example.util.LocaleHelper.applyLocale(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            com.example.sound.KeyboardSoundEngine.initialize(this)
        } catch (_: Exception) {}
        enableEdgeToEdge()
        handleIntent(intent)
        setContent {
            val layoutDir = com.example.util.LocaleHelper.getLayoutDirection(this)
            androidx.compose.runtime.CompositionLocalProvider(
                androidx.compose.ui.platform.LocalLayoutDirection provides layoutDir
            ) {
                MyApplicationTheme {
                    MainScreen(initialTarget = initialDestination)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.getStringExtra("NAVIGATE_TO") == "settings") {
            initialDestination = AppDestination.SETTINGS
        }
    }
}
