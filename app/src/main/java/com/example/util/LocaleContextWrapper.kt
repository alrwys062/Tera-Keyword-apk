package com.example.util

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList

class LocaleContextWrapper(base: Context) : ContextWrapper(base) {
    companion object {
        fun wrap(context: Context): ContextWrapper {
            val locale = LocaleHelper.getEffectiveLocale(context)
            val config = Configuration(context.resources.configuration)
            config.setLocale(locale)
            config.setLayoutDirection(locale)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val localeList = LocaleList(locale)
                LocaleList.setDefault(localeList)
                config.setLocales(localeList)
            }
            @Suppress("DEPRECATION")
            try {
                context.resources.updateConfiguration(config, context.resources.displayMetrics)
            } catch (_: Exception) {}
            val newContext = try {
                context.createConfigurationContext(config)
            } catch (_: Exception) {
                context
            }
            return LocaleContextWrapper(newContext)
        }
    }
}
