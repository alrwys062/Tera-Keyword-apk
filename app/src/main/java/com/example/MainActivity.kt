package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.ui.BaseActivity
import com.example.ui.AppDestination
import com.example.ui.MainScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : BaseActivity() {
    private var initialDestination by mutableStateOf<AppDestination?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Step 1: Configure App Check with launch intent before any UI or Firebase operations
        com.example.util.configureAppCheck(this, intent)
        try {
            com.example.sound.KeyboardSoundEngine.initialize(this)
        } catch (_: Exception) {}
        com.example.util.LocaleHelper.init(this)
        enableEdgeToEdge()
        handleIntent(intent)
        setContent {
            val appLanguageCode by com.example.util.LocaleHelper.appLanguageFlow.collectAsState()
            val layoutDir = com.example.util.LocaleHelper.getLayoutDirectionForCode(this, appLanguageCode)
            androidx.compose.runtime.CompositionLocalProvider(
                androidx.compose.ui.platform.LocalLayoutDirection provides layoutDir
            ) {
                MyApplicationTheme {
                    MainScreen(
                        initialTarget = initialDestination,
                        appLanguageCode = appLanguageCode
                    )
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
