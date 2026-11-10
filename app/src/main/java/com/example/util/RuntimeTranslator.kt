package com.example.util

import android.content.Context
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import java.io.File
import java.util.regex.Pattern
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

sealed interface TranslationState {
    data object Idle : TranslationState
    data class Downloading(val langCode: String, val langName: String) : TranslationState
    data class Translating(val langCode: String, val langName: String, val current: Int, val total: Int) : TranslationState
    data class Completed(val langCode: String) : TranslationState
    data class Failed(val langCode: String, val message: String) : TranslationState
}

object RuntimeTranslator {

    private const val TAG = "RuntimeTranslator"

    private val _state = MutableStateFlow<TranslationState>(TranslationState.Idle)
    val state: StateFlow<TranslationState> = _state.asStateFlow()

    private val translationScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    /**
     * Map of language codes to whether ML Kit supports them.
     */
    fun isLanguageSupportedByMlKit(langCode: String): Boolean {
        val baseCode = normalizeLangCode(langCode)
        return TranslateLanguage.getAllLanguages().contains(baseCode)
    }

    fun normalizeLangCode(langCode: String): String {
        val clean = langCode.lowercase().trim()
        val base = if (clean.contains("_")) clean.split("_")[0] else if (clean.contains("-")) clean.split("-")[0] else clean
        return when (base) {
            "in" -> "id"
            "iw" -> "he"
            "ji" -> "yi"
            else -> base
        }
    }

    /**
     * Await extension for Google Play Services Task without external dependency
     */
    private suspend fun <T> Task<T>.awaitTask(): T = suspendCancellableCoroutine { cont ->
        addOnSuccessListener { cont.resume(it) }
        addOnFailureListener { cont.resumeWithException(it) }
        addOnCanceledListener { cont.cancel() }
    }

    /**
     * Protects placeholders (%1$s, %2$s, %d, etc.), newlines, and emojis
     */
    fun protectPlaceholders(text: String): Pair<String, List<String>> {
        val tokens = mutableListOf<String>()
        val placeholderPattern = Pattern.compile("(%\\d+\\$[sd]|%[sd]|%\\d*\\.?\\d*[df]|%%)")
        val matcher = placeholderPattern.matcher(text)
        val sb = StringBuffer()
        var index = 0
        while (matcher.find()) {
            val token = matcher.group()
            tokens.add(token)
            matcher.appendReplacement(sb, "__PH_${index}__")
            index++
        }
        matcher.appendTail(sb)
        return Pair(sb.toString(), tokens)
    }

    /**
     * Restores protected placeholders after translation
     */
    fun restorePlaceholders(translated: String, tokens: List<String>): String {
        var result = translated
        for (i in tokens.indices) {
            val placeholder = "__PH_${i}__"
            val altPlaceholder = "__ ph_${i} __"
            val token = tokens[i]
            if (result.contains(placeholder)) {
                result = result.replace(placeholder, token)
            } else if (result.contains(altPlaceholder)) {
                result = result.replace(altPlaceholder, token)
            } else {
                val flexibleRegex = Regex("(?i)__\\s*ph_${i}\\s*__")
                result = flexibleRegex.replace(result, token)
            }
        }
        return result
    }

    /**
     * Translates untranslated keys for a target language in the background.
     * Can be invoked from UI (SettingsScreen).
     */
    fun startLanguageTranslation(context: Context, targetLangCode: String, targetLangName: String) {
        val target = normalizeLangCode(targetLangCode)
        if (target == "ar" || target == "en" || target == "auto") {
            _state.value = TranslationState.Completed(targetLangCode)
            return
        }

        if (!isLanguageSupportedByMlKit(target)) {
            Log.w(TAG, "Language $target is not directly supported by ML Kit translate. Using bundled strings.")
            _state.value = TranslationState.Completed(targetLangCode)
            return
        }

        translationScope.launch {
            try {
                val cachedFile = File(context.filesDir, "i18n_${target}.json")
                val existingMap = if (cachedFile.exists()) {
                    try {
                        val content = cachedFile.readText()
                        val json = JSONObject(content)
                        val map = mutableMapOf<String, String>()
                        val keys = json.keys()
                        while (keys.hasNext()) {
                            val k = keys.next()
                            map[k] = json.getString(k)
                        }
                        map
                    } catch (e: Exception) {
                        mutableMapOf<String, String>()
                    }
                } else {
                    mutableMapOf<String, String>()
                }

                // Load canonical master items from i18n_source.json asset
                val i18nSource = AppStrings.getMasterSource(context)
                val untranslatedKeys = i18nSource.keys.filter { key -> !existingMap.containsKey(key) }

                if (untranslatedKeys.isEmpty()) {
                    AppStrings.loadCacheForLanguage(context, target)
                    _state.value = TranslationState.Completed(targetLangCode)
                    return@launch
                }

                _state.value = TranslationState.Downloading(targetLangCode, targetLangName)

                // Download models for AR -> Target and EN -> Target
                val conditions = DownloadConditions.Builder()
                    .build()

                val arOptions = TranslatorOptions.Builder()
                    .setSourceLanguage(TranslateLanguage.ARABIC)
                    .setTargetLanguage(target)
                    .build()
                val arTranslator: Translator = Translation.getClient(arOptions)

                val enOptions = TranslatorOptions.Builder()
                    .setSourceLanguage(TranslateLanguage.ENGLISH)
                    .setTargetLanguage(target)
                    .build()
                val enTranslator: Translator = Translation.getClient(enOptions)

                try {
                    arTranslator.downloadModelIfNeeded(conditions).awaitTask()
                    enTranslator.downloadModelIfNeeded(conditions).awaitTask()
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to download ML Kit models for $target: ${e.message}", e)
                    _state.value = TranslationState.Failed(targetLangCode, e.localizedMessage ?: "Download failed")
                    return@launch
                }

                val total = untranslatedKeys.size
                var processed = 0
                _state.value = TranslationState.Translating(targetLangCode, targetLangName, 0, total)

                // Batch translate
                val resultMap = existingMap.toMutableMap()
                for (key in untranslatedKeys) {
                    val entry = i18nSource[key] ?: continue
                    val srcLang = entry.optString("src_lang", "en")
                    val origText = entry.optString("text", "")
                    if (origText.isBlank()) {
                        resultMap[key] = origText
                        processed++
                        continue
                    }

                    val translator = if (srcLang == "ar") arTranslator else enTranslator
                    val (protectedText, tokens) = protectPlaceholders(origText)

                    val translatedText = try {
                        val rawTrans = translator.translate(protectedText).awaitTask()
                        restorePlaceholders(rawTrans, tokens)
                    } catch (e: Exception) {
                        origText
                    }

                    resultMap[key] = translatedText
                    processed++

                    if (processed % 25 == 0 || processed == total) {
                        _state.value = TranslationState.Translating(targetLangCode, targetLangName, processed, total)
                    }
                }

                // Save back to disk
                val outJson = JSONObject()
                for ((k, v) in resultMap) {
                    outJson.put(k, v)
                }
                cachedFile.writeText(outJson.toString())

                // Update memory cache
                AppStrings.updateCacheForLanguage(target, resultMap)

                arTranslator.close()
                enTranslator.close()

                _state.value = TranslationState.Completed(targetLangCode)
                Log.d(TAG, "Successfully translated $total keys for $target and saved to ${cachedFile.name}")

            } catch (e: Exception) {
                Log.e(TAG, "Translation pipeline failed for $targetLangCode", e)
                _state.value = TranslationState.Failed(targetLangCode, e.localizedMessage ?: "Translation failed")
            }
        }
    }

    fun dismissState() {
        _state.value = TranslationState.Idle
    }
}
