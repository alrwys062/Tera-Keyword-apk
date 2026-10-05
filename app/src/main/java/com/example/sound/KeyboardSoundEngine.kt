package com.example.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

object KeyboardSoundEngine {
    private const val TAG = "KeyboardSoundEngine"
    private const val SAMPLE_RATE = 44100

    private var soundPool: SoundPool? = null
    private var audioManager: AudioManager? = null
    private val soundMap = mutableMapOf<String, Int>()
    private var isInitialized = false

    const val PROFILE_IOS_16 = "ios_16"
    const val PROFILE_MECHANICAL = "mechanical"
    const val PROFILE_MODERN_SOFT = "modern_soft"
    const val PROFILE_WATER_DROP = "water_drop"
    const val PROFILE_CLASSIC_TYPEWRITER = "classic_typewriter"
    const val PROFILE_SYSTEM_DEFAULT = "system_default"

    val AVAILABLE_PROFILES = listOf(
        SoundProfileInfo(PROFILE_IOS_16, "آيفون iOS 16 (Tock ناعم)", "iOS 16 Tock"),
        SoundProfileInfo(PROFILE_MECHANICAL, "كيبورد ميكانيكي (Mechanical Click)", "Mechanical"),
        SoundProfileInfo(PROFILE_MODERN_SOFT, "عصري خافت (Modern Soft Tap)", "Modern Soft"),
        SoundProfileInfo(PROFILE_WATER_DROP, "فقاعات ماء (Water Drop Bubble)", "Water Drop"),
        SoundProfileInfo(PROFILE_CLASSIC_TYPEWRITER, "آلة كاتبة كلاسيكية (Classic Typewriter)", "Typewriter"),
        SoundProfileInfo(PROFILE_SYSTEM_DEFAULT, "صوت نظام أندرويد الافتراضي", "Android System")
    )

    data class SoundProfileInfo(
        val id: String,
        val nameAr: String,
        val nameEn: String
    )

    fun initialize(context: Context) {
        if (isInitialized) return
        val appContext = context.applicationContext

        CoroutineScope(Dispatchers.IO).launch {
            try {
                audioManager = appContext.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

                val attributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()

                soundPool = SoundPool.Builder()
                    .setMaxStreams(8)
                    .setAudioAttributes(attributes)
                    .build()

                // Generate and load custom synthetic waveforms
                generateAndLoadProfile(appContext, PROFILE_IOS_16) { generateIos16Wav() }
                generateAndLoadProfile(appContext, PROFILE_MECHANICAL) { generateMechanicalWav() }
                generateAndLoadProfile(appContext, PROFILE_MODERN_SOFT) { generateModernSoftWav() }
                generateAndLoadProfile(appContext, PROFILE_WATER_DROP) { generateWaterDropWav() }
                generateAndLoadProfile(appContext, PROFILE_CLASSIC_TYPEWRITER) { generateTypewriterWav() }

                isInitialized = true
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize sound engine: ${e.message}")
            }
        }
    }

    private fun generateAndLoadProfile(context: Context, profileKey: String, generator: () -> ByteArray) {
        try {
            val file = File(context.cacheDir, "kb_snd_$profileKey.wav")
            if (!file.exists() || file.length() == 0L) {
                val data = generator()
                FileOutputStream(file).use { it.write(data) }
            }
            val soundId = soundPool?.load(file.absolutePath, 1) ?: 0
            if (soundId != 0) {
                soundMap[profileKey] = soundId
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error generating sound $profileKey: ${e.message}")
        }
    }

    fun playKeySound(
        profile: String = PROFILE_IOS_16,
        volume: Float = 0.85f,
        isSpecial: Boolean = false,
        isSpace: Boolean = false,
        isDelete: Boolean = false
    ) {
        val safeVolume = volume.coerceIn(0.05f, 1.0f)

        if (profile == PROFILE_SYSTEM_DEFAULT) {
            try {
                val fx = when {
                    isDelete -> AudioManager.FX_KEYPRESS_DELETE
                    isSpace -> AudioManager.FX_KEYPRESS_SPACEBAR
                    isSpecial -> AudioManager.FX_KEYPRESS_RETURN
                    else -> AudioManager.FX_KEYPRESS_STANDARD
                }
                audioManager?.playSoundEffect(fx, safeVolume)
            } catch (_: Exception) {}
            return
        }

        val pool = soundPool ?: return
        val soundId = soundMap[profile] ?: soundMap[PROFILE_IOS_16]

        if (soundId != null && soundId != 0) {
            // Subtle pitch modulation like iOS (delete and space slightly lower pitch)
            val pitch = when {
                isDelete -> 0.86f
                isSpace -> 0.92f
                isSpecial -> 0.95f
                else -> 1.0f
            }
            try {
                pool.play(soundId, safeVolume, safeVolume, 1, 0, pitch)
            } catch (e: Exception) {
                try {
                    audioManager?.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD, safeVolume)
                } catch (_: Exception) {}
            }
        } else {
            try {
                audioManager?.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD, safeVolume)
            } catch (_: Exception) {}
        }
    }

    // ------------------------------------------------------------------------
    // WAV Synthesis Algorithms for Crisp, Low-Latency Keyboard Clicks
    // ------------------------------------------------------------------------

    private fun generateIos16Wav(): ByteArray {
        val durationMs = 28
        val numSamples = (SAMPLE_RATE * durationMs) / 1000
        val pcm = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            // Warm low-mid thud (310Hz) + soft high click transient (2200Hz) in first 3ms
            val body = sin(2 * PI * 310.0 * t) * exp(-t / 0.0075)
            val transient = if (t < 0.003) sin(2 * PI * 2200.0 * t) * exp(-t / 0.0015) * 0.4 else 0.0
            val sample = (body + transient).coerceIn(-1.0, 1.0)
            pcm[i] = (sample * 30000).toInt().toShort()
        }
        return encodeWav(pcm)
    }

    private fun generateMechanicalWav(): ByteArray {
        val durationMs = 38
        val numSamples = (SAMPLE_RATE * durationMs) / 1000
        val pcm = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            // Sharp click at 2500Hz, then clack at 1100Hz after 4ms
            val click = sin(2 * PI * 2500.0 * t) * exp(-t / 0.003)
            val clack = if (t > 0.004) {
                val t2 = t - 0.004
                sin(2 * PI * 1100.0 * t2) * exp(-t2 / 0.008) * 0.7
            } else 0.0
            val sample = (click + clack).coerceIn(-1.0, 1.0)
            pcm[i] = (sample * 31000).toInt().toShort()
        }
        return encodeWav(pcm)
    }

    private fun generateModernSoftWav(): ByteArray {
        val durationMs = 24
        val numSamples = (SAMPLE_RATE * durationMs) / 1000
        val pcm = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            // Soft velvet dampened tap at 260Hz
            val sample = sin(2 * PI * 260.0 * t) * exp(-t / 0.006)
            pcm[i] = (sample * 26000).toInt().toShort()
        }
        return encodeWav(pcm)
    }

    private fun generateWaterDropWav(): ByteArray {
        val durationMs = 45
        val numSamples = (SAMPLE_RATE * durationMs) / 1000
        val pcm = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            // Upward frequency sweep (450Hz -> 1350Hz)
            val freq = 450.0 + (900.0 * (t / (durationMs / 1000.0)))
            val sample = sin(2 * PI * freq * t) * exp(-t / 0.015)
            pcm[i] = (sample * 29000).toInt().toShort()
        }
        return encodeWav(pcm)
    }

    private fun generateTypewriterWav(): ByteArray {
        val durationMs = 35
        val numSamples = (SAMPLE_RATE * durationMs) / 1000
        val pcm = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            // Metallic sharp snap (1500Hz & 3200Hz)
            val metal1 = sin(2 * PI * 1500.0 * t) * exp(-t / 0.004)
            val metal2 = sin(2 * PI * 3200.0 * t) * exp(-t / 0.002) * 0.5
            val sample = (metal1 + metal2).coerceIn(-1.0, 1.0)
            pcm[i] = (sample * 29000).toInt().toShort()
        }
        return encodeWav(pcm)
    }

    private fun encodeWav(pcm: ShortArray): ByteArray {
        val dataSize = pcm.size * 2
        val totalSize = 36 + dataSize
        val buffer = ByteBuffer.allocate(44 + dataSize)
        buffer.order(ByteOrder.LITTLE_ENDIAN)

        // RIFF Header
        buffer.put("RIFF".toByteArray())
        buffer.putInt(totalSize)
        buffer.put("WAVE".toByteArray())

        // fmt sub-chunk
        buffer.put("fmt ".toByteArray())
        buffer.putInt(16) // SubChunk1Size (16 for PCM)
        buffer.putShort(1.toShort()) // AudioFormat (1 for PCM)
        buffer.putShort(1.toShort()) // NumChannels (1 mono)
        buffer.putInt(SAMPLE_RATE) // SampleRate
        buffer.putInt(SAMPLE_RATE * 2) // ByteRate
        buffer.putShort(2.toShort()) // BlockAlign
        buffer.putShort(16.toShort()) // BitsPerSample

        // data sub-chunk
        buffer.put("data".toByteArray())
        buffer.putInt(dataSize)

        for (s in pcm) {
            buffer.putShort(s)
        }
        return buffer.array()
    }
}
