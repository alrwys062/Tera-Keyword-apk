package com.example.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.SoundPool
import android.os.Build
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

object KeyboardSoundEngine {
    private const val TAG = "KeyboardSoundEngine"

    private var audioManager: AudioManager? = null
    private var soundPool: SoundPool? = null
    private val soundMap = ConcurrentHashMap<String, Int>()
    private val loadedSoundIds = Collections.synchronizedSet(mutableSetOf<Int>())
    private val rawPcmMap = ConcurrentHashMap<String, ShortArray>()

    @Volatile
    private var isInitialized = false

    @Volatile
    private var isSoundPoolReady = false

    const val PROFILE_IOS_16 = "ios_16"
    const val PROFILE_MECHANICAL = "mechanical"
    const val PROFILE_MODERN_SOFT = "modern_soft"
    const val PROFILE_WATER_DROP = "water_drop"
    const val PROFILE_CLASSIC_TYPEWRITER = "classic_typewriter"
    const val PROFILE_WOOD_BLOCK = "wood_block"
    const val PROFILE_CYBER_SCIFI = "cyber_scifi"
    const val PROFILE_POP_BUBBLE = "pop_bubble"
    const val PROFILE_SYSTEM_DEFAULT = "system_default"

    val AVAILABLE_PROFILES = listOf(
        SoundProfileInfo(PROFILE_IOS_16, "🍏 آيفون iOS 16 (Tock الأصلي)", "iOS 16 Tock"),
        SoundProfileInfo(PROFILE_MECHANICAL, "⌨️ كيبورد ميكانيكي (Blue Switch)", "Mechanical Click"),
        SoundProfileInfo(PROFILE_MODERN_SOFT, "🫧 ناعم ومريح (Velvet Soft)", "Modern Soft Tap"),
        SoundProfileInfo(PROFILE_WATER_DROP, "💧 قطرات ماء (Water Drops)", "Water Drop"),
        SoundProfileInfo(PROFILE_POP_BUBBLE, "🎈 فرقعة فقاعات (Pop Bubble)", "Pop Bubble"),
        SoundProfileInfo(PROFILE_WOOD_BLOCK, "🪵 نقرات خشبية (Wood Tap)", "Wood Percussion"),
        SoundProfileInfo(PROFILE_CYBER_SCIFI, "🚀 سايبر مستقبلي (Cyber Laser)", "Sci-Fi Laser"),
        SoundProfileInfo(PROFILE_CLASSIC_TYPEWRITER, "📜 آلة كاتبة (Classic Typewriter)", "Typewriter"),
        SoundProfileInfo(PROFILE_SYSTEM_DEFAULT, "🤖 صوت نظام أندرويد الافتراضي", "Android System")
    )

    data class SoundProfileInfo(
        val id: String,
        val nameAr: String,
        val nameEn: String
    )

    @Synchronized
    fun initialize(context: Context) {
        val appContext = context.applicationContext
        try {
            audioManager = appContext.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        } catch (_: Throwable) {}

        if (isInitialized && isSoundPoolReady) return
        isInitialized = true

        Thread {
            try {
                // Generate and cache all PCM waveforms first
                preloadPcmWaveforms()

                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()

                val pool = SoundPool.Builder()
                    .setMaxStreams(12)
                    .setAudioAttributes(audioAttributes)
                    .build()

                pool.setOnLoadCompleteListener { _, sampleId, status ->
                    if (status == 0) {
                        loadedSoundIds.add(sampleId)
                    }
                }

                soundPool = pool
                loadSynthesizedSounds(appContext, pool)
                isSoundPoolReady = true
            } catch (e: Throwable) {
                isSoundPoolReady = false
            }
        }.start()
    }

    private fun preloadPcmWaveforms() {
        if (rawPcmMap.isNotEmpty()) return
        rawPcmMap[PROFILE_IOS_16] = generateIosPcm()
        rawPcmMap[PROFILE_MECHANICAL] = generateMechanicalPcm()
        rawPcmMap[PROFILE_MODERN_SOFT] = generateSoftPcm()
        rawPcmMap[PROFILE_WATER_DROP] = generateWaterDropPcm()
        rawPcmMap[PROFILE_POP_BUBBLE] = generatePopBubblePcm()
        rawPcmMap[PROFILE_WOOD_BLOCK] = generateWoodBlockPcm()
        rawPcmMap[PROFILE_CYBER_SCIFI] = generateCyberLaserPcm()
        rawPcmMap[PROFILE_CLASSIC_TYPEWRITER] = generateTypewriterPcm()
        rawPcmMap[PROFILE_SYSTEM_DEFAULT] = generateSystemPcm()
        rawPcmMap["special_space"] = generateSpacePcm()
        rawPcmMap["special_delete"] = generateDeletePcm()
    }

    private fun loadSynthesizedSounds(context: Context, pool: SoundPool) {
        try {
            val cacheDir = context.cacheDir ?: return
            val soundsDir = File(cacheDir, "kb_sounds").apply { if (!exists()) mkdirs() }

            val pcmMap = rawPcmMap.ifEmpty {
                preloadPcmWaveforms()
                rawPcmMap
            }

            for ((key, pcm) in pcmMap) {
                try {
                    val wavBytes = createWavFile(pcm, 44100)
                    val file = File(soundsDir, "$key.wav")
                    if (!file.exists() || file.length() != wavBytes.size.toLong()) {
                        FileOutputStream(file).use { it.write(wavBytes) }
                    }
                    val soundId = pool.load(file.absolutePath, 1)
                    if (soundId != 0) {
                        soundMap[key] = soundId
                    }
                } catch (_: Throwable) {}
            }
        } catch (_: Throwable) {}
    }

    fun playKeySound(
        profile: String = PROFILE_IOS_16,
        volume: Float = 0.85f,
        isSpecial: Boolean = false,
        isSpace: Boolean = false,
        isDelete: Boolean = false
    ) {
        val safeVolume = volume.coerceIn(0.2f, 1.0f)
        val soundKey = when {
            isDelete -> "special_delete"
            isSpace -> "special_space"
            isSpecial -> PROFILE_IOS_16
            else -> profile
        }

        var played = false

        // 1. Try SoundPool
        try {
            val pool = soundPool
            if (pool != null) {
                val soundId = soundMap[soundKey] ?: soundMap[profile] ?: soundMap[PROFILE_IOS_16]
                if (soundId != null && soundId != 0) {
                    val streamId = pool.play(soundId, safeVolume, safeVolume, 1, 0, 1.0f)
                    if (streamId > 0) {
                        played = true
                    }
                }
            }
        } catch (_: Throwable) {}

        // 2. Direct AudioTrack playback fallback if SoundPool not ready or hasn't loaded yet
        if (!played) {
            try {
                val pcm = rawPcmMap[soundKey] ?: rawPcmMap[profile] ?: rawPcmMap[PROFILE_IOS_16]
                if (pcm != null && pcm.isNotEmpty()) {
                    playPcmDirect(pcm, safeVolume)
                    played = true
                }
            } catch (_: Throwable) {}
        }

        // 3. Android System Sound Effect (complementary or system default fallback)
        try {
            val fx = when {
                isDelete -> AudioManager.FX_KEYPRESS_DELETE
                isSpace -> AudioManager.FX_KEYPRESS_SPACEBAR
                isSpecial -> AudioManager.FX_KEYPRESS_RETURN
                profile == PROFILE_POP_BUBBLE || profile == PROFILE_WATER_DROP -> AudioManager.FX_KEYPRESS_SPACEBAR
                profile == PROFILE_WOOD_BLOCK -> AudioManager.FX_FOCUS_NAVIGATION_UP
                profile == PROFILE_CYBER_SCIFI -> AudioManager.FX_FOCUS_NAVIGATION_RIGHT
                profile == PROFILE_MECHANICAL -> AudioManager.FX_KEYPRESS_STANDARD
                else -> AudioManager.FX_KEYPRESS_STANDARD
            }
            audioManager?.playSoundEffect(fx, safeVolume)
        } catch (_: Throwable) {}
    }

    private fun playPcmDirect(pcm: ShortArray, volume: Float) {
        Thread {
            try {
                val sampleRate = 44100
                val minBufferSize = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )
                val bufferSize = maxOf(minBufferSize, pcm.size * 2)

                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()

                val audioFormat = AudioFormat.Builder()
                    .setSampleRate(sampleRate)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()

                val track = AudioTrack.Builder()
                    .setAudioAttributes(audioAttributes)
                    .setAudioFormat(audioFormat)
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                val scaledPcm = if (volume < 0.99f) {
                    ShortArray(pcm.size) { i -> (pcm[i] * volume).toInt().toShort() }
                } else {
                    pcm
                }

                track.write(scaledPcm, 0, scaledPcm.size)
                track.play()
                Thread.sleep(45)
                track.stop()
                track.release()
            } catch (_: Throwable) {}
        }.start()
    }

    // ==========================================
    // PCM Audio Waveform Synthesis Engine
    // ==========================================

    private fun generateIosPcm(): ShortArray {
        val sampleRate = 44100
        val durationMs = 30
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val pcm = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val decay = exp(-t * 150.0)
            val wave = 0.75 * sin(2.0 * PI * 700.0 * t) + 0.25 * sin(2.0 * PI * 1350.0 * t)
            val sample = (wave * decay * Short.MAX_VALUE * 0.95).toInt()
            pcm[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return pcm
    }

    private fun generateMechanicalPcm(): ShortArray {
        val sampleRate = 44100
        val durationMs = 40
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val pcm = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val decay = exp(-t * 105.0)
            val wave = 0.5 * sin(2.0 * PI * 1800.0 * t) +
                       0.3 * sin(2.0 * PI * 3200.0 * t) +
                       0.2 * sin(2.0 * PI * 850.0 * t)
            val sample = (wave * decay * Short.MAX_VALUE * 0.95).toInt()
            pcm[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return pcm
    }

    private fun generateSoftPcm(): ShortArray {
        val sampleRate = 44100
        val durationMs = 26
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val pcm = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val decay = exp(-t * 180.0)
            val wave = sin(2.0 * PI * 450.0 * t)
            val sample = (wave * decay * Short.MAX_VALUE * 0.85).toInt()
            pcm[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return pcm
    }

    private fun generateWaterDropPcm(): ShortArray {
        val sampleRate = 44100
        val durationMs = 45
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val pcm = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val decay = exp(-t * 85.0)
            val freq = 550.0 + 1500.0 * (t / (durationMs / 1000.0))
            val wave = sin(2.0 * PI * freq * t)
            val sample = (wave * decay * Short.MAX_VALUE * 0.92).toInt()
            pcm[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return pcm
    }

    private fun generatePopBubblePcm(): ShortArray {
        val sampleRate = 44100
        val durationMs = 32
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val pcm = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val decay = exp(-t * 135.0)
            val wave = 0.8 * sin(2.0 * PI * 950.0 * t) + 0.2 * sin(2.0 * PI * 1900.0 * t)
            val sample = (wave * decay * Short.MAX_VALUE * 0.92).toInt()
            pcm[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return pcm
    }

    private fun generateWoodBlockPcm(): ShortArray {
        val sampleRate = 44100
        val durationMs = 32
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val pcm = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val decay = exp(-t * 145.0)
            val wave = 0.7 * sin(2.0 * PI * 880.0 * t) + 0.3 * sin(2.0 * PI * 440.0 * t)
            val sample = (wave * decay * Short.MAX_VALUE * 0.9).toInt()
            pcm[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return pcm
    }

    private fun generateCyberLaserPcm(): ShortArray {
        val sampleRate = 44100
        val durationMs = 35
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val pcm = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val decay = exp(-t * 105.0)
            val freq = 2500.0 - 1600.0 * (t / (durationMs / 1000.0))
            val wave = 0.7 * sin(2.0 * PI * freq * t) + 0.3 * sin(2.0 * PI * (freq * 1.5) * t)
            val sample = (wave * decay * Short.MAX_VALUE * 0.92).toInt()
            pcm[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return pcm
    }

    private fun generateTypewriterPcm(): ShortArray {
        val sampleRate = 44100
        val durationMs = 38
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val pcm = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val decay = exp(-t * 120.0)
            val wave = 0.6 * sin(2.0 * PI * 2300.0 * t) + 0.4 * sin(2.0 * PI * 980.0 * t)
            val sample = (wave * decay * Short.MAX_VALUE * 0.92).toInt()
            pcm[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return pcm
    }

    private fun generateSystemPcm(): ShortArray {
        val sampleRate = 44100
        val durationMs = 26
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val pcm = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val decay = exp(-t * 175.0)
            val wave = sin(2.0 * PI * 780.0 * t)
            val sample = (wave * decay * Short.MAX_VALUE * 0.9).toInt()
            pcm[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return pcm
    }

    private fun generateSpacePcm(): ShortArray {
        val sampleRate = 44100
        val durationMs = 35
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val pcm = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val decay = exp(-t * 125.0)
            val wave = 0.6 * sin(2.0 * PI * 500.0 * t) + 0.4 * sin(2.0 * PI * 340.0 * t)
            val sample = (wave * decay * Short.MAX_VALUE * 0.95).toInt()
            pcm[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return pcm
    }

    private fun generateDeletePcm(): ShortArray {
        val sampleRate = 44100
        val durationMs = 28
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val pcm = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val decay = exp(-t * 160.0)
            val wave = 0.5 * sin(2.0 * PI * 600.0 * t) + 0.5 * sin(2.0 * PI * 920.0 * t)
            val sample = (wave * decay * Short.MAX_VALUE * 0.92).toInt()
            pcm[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return pcm
    }

    private fun createWavFile(pcmData: ShortArray, sampleRate: Int): ByteArray {
        val byteData = ByteArray(pcmData.size * 2)
        for (i in pcmData.indices) {
            val sample = pcmData[i].toInt()
            byteData[i * 2] = (sample and 0xFF).toByte()
            byteData[i * 2 + 1] = ((sample shr 8) and 0xFF).toByte()
        }

        val totalDataLen = byteData.size + 36
        val byteRate = sampleRate * 2 // 16-bit mono

        val out = ByteArrayOutputStream()
        out.write("RIFF".toByteArray())
        out.write(intToByteArray(totalDataLen))
        out.write("WAVE".toByteArray())
        out.write("fmt ".toByteArray())
        out.write(intToByteArray(16))
        out.write(shortToByteArray(1))
        out.write(shortToByteArray(1))
        out.write(intToByteArray(sampleRate))
        out.write(intToByteArray(byteRate))
        out.write(shortToByteArray(2))
        out.write(shortToByteArray(16))
        out.write("data".toByteArray())
        out.write(intToByteArray(byteData.size))
        out.write(byteData)

        return out.toByteArray()
    }

    private fun intToByteArray(value: Int): ByteArray {
        return byteArrayOf(
            (value and 0xFF).toByte(),
            ((value shr 8) and 0xFF).toByte(),
            ((value shr 16) and 0xFF).toByte(),
            ((value shr 24) and 0xFF).toByte()
        )
    }

    private fun shortToByteArray(value: Short): ByteArray {
        return byteArrayOf(
            (value.toInt() and 0xFF).toByte(),
            ((value.toInt() shr 8) and 0xFF).toByte()
        )
    }
}
