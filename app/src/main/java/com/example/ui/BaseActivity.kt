package com.example.ui

import android.content.Context
import android.content.res.Configuration
import androidx.activity.ComponentActivity
import com.example.util.LocaleContextWrapper
import com.example.util.LocaleHelper

abstract class BaseActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleContextWrapper.wrap(newBase))
    }

    override fun applyOverrideConfiguration(overrideConfiguration: Configuration?) {
        if (overrideConfiguration != null) {
            val locale = LocaleHelper.getEffectiveLocale(this)
            overrideConfiguration.setLocale(locale)
            overrideConfiguration.setLayoutDirection(locale)
        }
        super.applyOverrideConfiguration(overrideConfiguration)
    }
}

