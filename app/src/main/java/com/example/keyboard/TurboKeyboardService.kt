package com.example.keyboard

import android.content.Intent
import android.inputmethodservice.InputMethodService
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import androidx.compose.runtime.*
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.data.PreferencesManager

class TurboKeyboardService : InputMethodService(),
    LifecycleOwner,
    ViewModelStoreOwner,
    SavedStateRegistryOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val store = ViewModelStore()
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    override val viewModelStore: ViewModelStore
        get() = store

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    private val activeInputConnectionState = mutableStateOf<InputConnection?>(null)
    private val activeEditorInfoState = mutableStateOf<EditorInfo?>(null)
    private var composeView: ComposeView? = null

    override fun onCreate() {
        super.onCreate()
        try {
            savedStateRegistryController.performRestore(null)
        } catch (e: Exception) {
            // Already restored or not required
        }
        try {
            val prefs = PreferencesManager(this@TurboKeyboardService)
            prefs.syncWithSystemClipboard(this@TurboKeyboardService)
            val clipManager = getSystemService(CLIPBOARD_SERVICE) as? android.content.ClipboardManager
            clipManager?.addPrimaryClipChangedListener {
                prefs.syncWithSystemClipboard(this@TurboKeyboardService)
            }
        } catch (e: Exception) {
            // Ignore
        }
        try {
            com.example.sound.KeyboardSoundEngine.initialize(this)
        } catch (_: Exception) {}
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
    }

    private fun setupWindowDecorOwners() {
        try {
            window?.window?.decorView?.let { decorView ->
                decorView.setViewTreeLifecycleOwner(this)
                decorView.setViewTreeViewModelStoreOwner(this)
                decorView.setViewTreeSavedStateRegistryOwner(this)
            }
        } catch (e: Exception) {
            // Ignore if decorView not ready
        }
    }

    override fun onCreateInputView(): View {
        setupWindowDecorOwners()
        activeInputConnectionState.value = currentInputConnection

        val view = ComposeView(this).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnLifecycleDestroyed(this@TurboKeyboardService.lifecycle)
            )
            setViewTreeLifecycleOwner(this@TurboKeyboardService)
            setViewTreeViewModelStoreOwner(this@TurboKeyboardService)
            setViewTreeSavedStateRegistryOwner(this@TurboKeyboardService)

            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            setContent {
                val currentIc by activeInputConnectionState
                val currentEi by activeEditorInfoState
                val prefs = remember { PreferencesManager(this@TurboKeyboardService) }
                val theme = prefs.getActiveTheme()
                val settings = prefs.getSettings()

                TurboKeyboardView(
                    theme = theme,
                    settings = settings,
                    inputConnection = currentIc ?: currentInputConnection,
                    getCurrentInputConnection = { currentInputConnection },
                    editorInfo = currentEi ?: currentInputEditorInfo,
                    getCurrentEditorInfo = { currentInputEditorInfo },
                    onServiceDelete = { performServiceDelete() },
                    onVoiceRequested = {
                        // Voice view is activated directly in toolbar, or fallback to intent
                        launchVoiceRecognition()
                    },
                    onOpenSettingsRequested = {
                        openAppSettings()
                    },
                    prefsManager = prefs
                )
            }
        }
        this.composeView = view
        return view
    }

    override fun onEvaluateFullscreenMode(): Boolean {
        // Prevent fullscreen extract mode on landscape / small screens (Honor / EMUI / HyperOS / tablets)
        return false
    }

    override fun onEvaluateInputViewShown(): Boolean {
        return super.onEvaluateInputViewShown()
    }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        activeInputConnectionState.value = currentInputConnection
        activeEditorInfoState.value = attribute ?: currentInputEditorInfo
        try {
            com.example.sound.KeyboardSoundEngine.initialize(this@TurboKeyboardService)
            PreferencesManager(this@TurboKeyboardService).syncWithSystemClipboard(this@TurboKeyboardService)
        } catch (_: Exception) {}
    }

    override fun onUpdateSelection(
        oldSelStart: Int,
        oldSelEnd: Int,
        newSelStart: Int,
        newSelEnd: Int,
        candidatesStart: Int,
        candidatesEnd: Int
    ) {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd)
        activeInputConnectionState.value = currentInputConnection
        activeEditorInfoState.value = currentInputEditorInfo
        // Auto return to letters when text is cleared/sent in apps like WhatsApp or Telegram
        if (newSelStart == 0 && newSelEnd == 0 && (oldSelStart > 0 || oldSelEnd > 0)) {
            resetToLettersSignal.value = System.currentTimeMillis()
        }
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        setupWindowDecorOwners()
        activeInputConnectionState.value = currentInputConnection
        activeEditorInfoState.value = info ?: currentInputEditorInfo
        try {
            PreferencesManager(this@TurboKeyboardService).syncWithSystemClipboard(this@TurboKeyboardService)
        } catch (_: Exception) {}
        ensureLifecycleStartedAndResumed()
    }

    override fun onWindowShown() {
        super.onWindowShown()
        setupWindowDecorOwners()
        activeInputConnectionState.value = currentInputConnection
        try {
            PreferencesManager(this@TurboKeyboardService).syncWithSystemClipboard(this@TurboKeyboardService)
        } catch (_: Exception) {}
        ensureLifecycleStartedAndResumed()
    }

    private fun ensureLifecycleStartedAndResumed() {
        when (lifecycleRegistry.currentState) {
            Lifecycle.State.INITIALIZED, Lifecycle.State.CREATED -> {
                lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
                lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
            }
            Lifecycle.State.STARTED -> {
                lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
            }
            Lifecycle.State.RESUMED -> {
                // Already in resumed state
            }
            Lifecycle.State.DESTROYED -> {}
        }
    }

    override fun onWindowHidden() {
        super.onWindowHidden()
        ensureLifecyclePausedAndStopped()
        resetToLettersSignal.value = System.currentTimeMillis()
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        ensureLifecyclePausedAndStopped()
        resetToLettersSignal.value = System.currentTimeMillis()
        // Do not force-null activeInputConnectionState on transient focus changes (like screenshots)
        if (finishingInput) {
            activeInputConnectionState.value = currentInputConnection
        }
    }

    private fun ensureLifecyclePausedAndStopped() {
        when (lifecycleRegistry.currentState) {
            Lifecycle.State.RESUMED -> {
                lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
                lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
            }
            Lifecycle.State.STARTED -> {
                lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
            }
            else -> {}
        }
    }

    override fun onFinishInput() {
        super.onFinishInput()
        activeInputConnectionState.value = currentInputConnection
    }

    private fun performServiceDelete() {
        val ic = currentInputConnection
        try {
            if (ic != null) {
                val selected = ic.getSelectedText(0)
                if (!selected.isNullOrEmpty()) {
                    ic.commitText("", 1)
                    return
                }
                val before = ic.getTextBeforeCursor(1, 0)
                if (!before.isNullOrEmpty()) {
                    val deleted = ic.deleteSurroundingText(1, 0)
                    if (deleted) return
                }
            }
            sendDownUpKeyEvents(android.view.KeyEvent.KEYCODE_DEL)
        } catch (e: Exception) {
            try {
                sendDownUpKeyEvents(android.view.KeyEvent.KEYCODE_DEL)
            } catch (_: Exception) {}
        }
    }

    private fun openAppSettings() {
        try {
            val intent = Intent(this, com.example.MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                putExtra("NAVIGATE_TO", "settings")
            }
            startActivity(intent)
        } catch (e: Exception) {
            try {
                val launchIntent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                if (launchIntent != null) startActivity(launchIntent)
            } catch (_: Exception) {}
        }
    }

    private fun launchVoiceRecognition() {
        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_PROMPT, "تحدث الآن لكتابة النص...")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        } catch (e: Exception) {
            // Speech recognition not installed on device
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        composeView = null
        if (lifecycleRegistry.currentState.isAtLeast(Lifecycle.State.CREATED)) {
            if (lifecycleRegistry.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
            }
            if (lifecycleRegistry.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
            }
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        }
        store.clear()
    }

    companion object {
        val resetToLettersSignal = kotlinx.coroutines.flow.MutableStateFlow(0L)
    }
}
