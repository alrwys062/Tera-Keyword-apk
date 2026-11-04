package com.example.ui

import android.content.Context
import androidx.activity.ComponentActivity
import com.example.util.LocaleHelper

abstract class BaseActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase))
    }
}
