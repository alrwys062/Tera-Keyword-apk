package com.example.keyboard

import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputConnection
import com.example.data.InputLanguagesManager

/**
 * KeyboardInputService
 * Core input method logic, layout management, gesture handling, language switching, and popup logic.
 */
open class KeyboardInputService : TurboKeyboardService() {

    private var activeLanguageCode: String = "ar"
    private var isPopupShowing: Boolean = false

    fun switchLanguage(nextCode: String) {
        activeLanguageCode = nextCode
        InputLanguagesManager.setCurrentLanguageCode(this, nextCode)
    }

    fun toggleLanguage() {
        val nextLang = InputLanguagesManager.getNextActiveLanguage(this, activeLanguageCode)
        activeLanguageCode = nextLang.code
    }

    fun getActiveLanguage(): String = activeLanguageCode

    fun onSpacebarTouch(event: MotionEvent, spacebarView: View): Boolean {
        // Horizontal Swipe Detector
        var startX = 0f
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                startX = event.x
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                val deltaX = event.x - startX
                if (Math.abs(deltaX) > 50) {
                    toggleLanguage()
                } else {
                    currentInputConnection?.commitText(" ", 1)
                }
                return true
            }
        }
        return false
    }

    fun showKeyPopup(keyChar: String, anchor: View) {
        isPopupShowing = true
    }

    fun dismissKeyPopup() {
        isPopupShowing = false
    }
}
