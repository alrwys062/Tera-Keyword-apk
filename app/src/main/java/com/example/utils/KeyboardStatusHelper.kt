package com.example.utils

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.inputmethod.InputMethodManager

enum class KeyboardStatus {
    DISABLED,
    ENABLED_NOT_DEFAULT,
    ACTIVE_DEFAULT
}

object KeyboardStatusHelper {

    fun checkKeyboardStatus(context: Context): KeyboardStatus {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            ?: return KeyboardStatus.DISABLED

        val packageName = context.packageName
        val enabledList = imm.enabledInputMethodList

        val isEnabled = enabledList.any { ime ->
            ime.packageName == packageName ||
            ime.serviceName.contains("TurboKeyboardService") ||
            ime.id.contains("TurboKeyboardService")
        }

        if (!isEnabled) {
            return KeyboardStatus.DISABLED
        }

        // Check if selected as default
        val defaultIme = Settings.Secure.getString(context.contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD)
        val isDefault = defaultIme != null && (
            defaultIme.contains(packageName) ||
            defaultIme.contains("TurboKeyboardService")
        )

        return if (isDefault) {
            KeyboardStatus.ACTIVE_DEFAULT
        } else {
            KeyboardStatus.ENABLED_NOT_DEFAULT
        }
    }

    fun openEnableKeyboardSettings(context: Context): Boolean {
        return try {
            val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            try {
                val fallbackIntent = Intent(Settings.ACTION_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(fallbackIntent)
                true
            } catch (ex: Exception) {
                false
            }
        }
    }

    fun openSelectKeyboardPicker(context: Context): Boolean {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        return if (imm != null) {
            try {
                imm.showInputMethodPicker()
                true
            } catch (e: Exception) {
                // Fallback to settings
                openEnableKeyboardSettings(context)
            }
        } else {
            openEnableKeyboardSettings(context)
        }
    }

    fun openKeyboardSystemSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Ignore
        }
    }
}
