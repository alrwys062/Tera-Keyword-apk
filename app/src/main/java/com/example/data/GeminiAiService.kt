package com.example.data

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.GenerativeModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

private const val TAG = "GeminiAiService"

object GeminiAiService {

    // Mandated: Initialize generativeModel lazily to prevent eagerly evaluating Firebase.ai
    private val model: GenerativeModel by lazy {
        Firebase.ai.generativeModel("gemini-3.8-flash")
    }

    /**
     * Rephrase or rewrite text with a specific tone or style using Gemini API.
     */
    suspend fun rephraseWithTone(text: String, tone: String, lang: String = "ar"): Result<String> = withContext(Dispatchers.IO) {
        if (text.isBlank()) return@withContext Result.success("")
        try {
            val prompt = """
                You are an expert keyboard writing assistant.
                Rewrite the following user text in the requested tone/style: "$tone".
                Keep the same language as the input text (or $lang).
                Output ONLY the rewritten text directly without explanation, preamble, or quotes.
                
                Input Text:
                $text
            """.trimIndent()

            val response = withTimeout(60_000L) {
                model.generateContent(prompt)
            }
            val result = response.text?.trim() ?: ""
            if (result.isNotBlank()) {
                Result.success(result)
            } else {
                Result.failure(Exception("Model returned an empty response"))
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Gemini rephrase timed out", e)
            Result.failure(Exception("Generation timed out. Please try again."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Gemini rephrase failed", e)
            Result.failure(e)
        }
    }

    /**
     * Smart proofread and grammar correction using Gemini API.
     */
    suspend fun smartProofread(text: String): Result<String> = withContext(Dispatchers.IO) {
        if (text.isBlank()) return@withContext Result.success("")
        try {
            val prompt = """
                You are a professional spell checker and grammarian.
                Correct any spelling, grammatical, or diacritical errors in the following text.
                Preserve the exact meaning, style, and tone.
                Output ONLY the corrected text without any notes or explanations.
                
                Input:
                $text
            """.trimIndent()

            val response = withTimeout(60_000L) {
                model.generateContent(prompt)
            }
            val result = response.text?.trim() ?: ""
            Result.success(result.ifBlank { text })
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Gemini proofread timed out", e)
            Result.failure(Exception("Spell check timed out."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Gemini proofread failed", e)
            Result.failure(e)
        }
    }

    /**
     * Instant smart translation using Gemini API.
     */
    suspend fun translateText(text: String, sourceLang: String, targetLang: String): Result<String> = withContext(Dispatchers.IO) {
        if (text.isBlank()) return@withContext Result.success("")
        try {
            val prompt = """
                Translate the following text from $sourceLang to $targetLang accurately and naturally.
                Output ONLY the translated text without extra formatting, notes, or quotes.
                
                Text to translate:
                $text
            """.trimIndent()

            val response = withTimeout(60_000L) {
                model.generateContent(prompt)
            }
            val result = response.text?.trim() ?: ""
            Result.success(result)
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Gemini translation timed out", e)
            Result.failure(Exception("Translation timed out."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Gemini translation failed", e)
            Result.failure(e)
        }
    }

    /**
     * Smart text generation or auto-reply using Gemini API.
     */
    suspend fun generateSmartText(instructionOrPrompt: String): Result<String> = withContext(Dispatchers.IO) {
        if (instructionOrPrompt.isBlank()) return@withContext Result.success("")
        try {
            val prompt = """
                You are an ultra-fast, helpful AI keyboard assistant.
                Generate a concise, high quality response or text based on this request:
                $instructionOrPrompt
                
                Output ONLY the text ready to insert into the user's message.
            """.trimIndent()

            val response = withTimeout(60_000L) {
                model.generateContent(prompt)
            }
            val result = response.text?.trim() ?: ""
            Result.success(result)
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Gemini generation timed out", e)
            Result.failure(Exception("Generation timed out."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Gemini generation failed", e)
            Result.failure(e)
        }
    }
}
