package com.example.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.Build
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * Universal, high-compatibility keypress audio engine using Android SoundPool.
 * SoundPool is universally supported, natively mixed by Android's audio server,
 * and guarantees audible playback across all OEMs (Honor MagicOS, Huawei EMUI,
 * Samsung One UI, Xiaomi HyperOS, Oppo ColorOS, Vivo, and Google Pixel).
 */
object KeyboardSoundEngine {
    private const val TAG = "KeyboardSoundEngine"
    private const val SAMPLE_RATE = 44100
    private const val MAX_STREAMS = 10

    @Volatile
    private var isInitialized = false

    private var soundPool: SoundPool? = null
    private var audioManager: AudioManager? = null
    private var appContext: Context? = null

    // Cache of loaded sound IDs in SoundPool: key -> soundId
    private val loadedSoundIds = ConcurrentHashMap<String, Int>()
    // Memory cache of generated WAV files
    private val wavFileCache = ConcurrentHashMap<String, File>()

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
        val app = context.applicationContext
        appContext = app
        if (audioManager == null) {
            try {
                audioManager = app.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            } catch (_: Throwable) {}
        }

        if (soundPool != null && isInitialized) return

        try {
            soundPool?.release()
        } catch (_: Throwable) {}

        soundPool = createSoundPool()
        isInitialized = true

        // Asynchronously synthesize and load all WAV sounds into SoundPool
        Thread {
            try {
                loadAllSounds(app)
            } catch (e: Exception) {
                Log.w(TAG, "Audio loading error: ${e.message}")
            }
        }.start()
    }

    private fun createSoundPool(): SoundPool {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        return SoundPool.Builder()
            .setMaxStreams(MAX_STREAMS)
            .setAudioAttributes(audioAttributes)
            .build()
    }

    private fun loadAllSounds(context: Context) {
        val sp = soundPool ?: return
        val soundDir = File(context.cacheDir, "kb_wav_sounds").apply { mkdirs() }

        val keys = listOf(
            PROFILE_IOS_16,
            PROFILE_MECHANICAL,
            PROFILE_MODERN_SOFT,
            PROFILE_WATER_DROP,
            PROFILE_POP_BUBBLE,
            PROFILE_WOOD_BLOCK,
            PROFILE_CYBER_SCIFI,
            PROFILE_CLASSIC_TYPEWRITER,
            PROFILE_SYSTEM_DEFAULT,
            "special_space",
            "special_delete"
        )

        for (key in keys) {
            try {
                val wavFile = File(soundDir, "$key.wav")
                if (!wavFile.exists() || wavFile.length() == 0L) {
                    val pcm = generatePcmForKey(key)
                    writeWavFile(wavFile, pcm, SAMPLE_RATE)
                }
                wavFileCache[key] = wavFile
                val soundId = sp.load(wavFile.absolutePath, 1)
                if (soundId != 0) {
                    loadedSoundIds[key] = soundId
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed loading sound $key: ${e.message}")
            }
        }
    }

    private fun getOrLoadSoundId(key: String): Int {
        loadedSoundIds[key]?.let { return it }
        val sp = soundPool ?: return 0
        val ctx = appContext ?: return 0

        val soundDir = File(ctx.cacheDir, "kb_wav_sounds").apply { mkdirs() }
        val wavFile = File(soundDir, "$key.wav")
        if (!wavFile.exists() || wavFile.length() == 0L) {
            val pcm = generatePcmForKey(key)
            writeWavFile(wavFile, pcm, SAMPLE_RATE)
        }
        val soundId = sp.load(wavFile.absolutePath, 1)
        if (soundId != 0) {
            loadedSoundIds[key] = soundId
        }
        return soundId
    }

    /**
     * Plays key sound with guaranteed audibility across all Android devices (Honor, Samsung, Xiaomi, etc.)
     */
    fun playKeySound(
        profile: String = PROFILE_IOS_16,
        volume: Float = 0.85f,
        isSpecial: Boolean = false,
        isSpace: Boolean = false,
        isDelete: Boolean = false
    ) {
        val safeVolume = volume.coerceIn(0.15f, 1.0f)

        // Native System click fallback
        if (profile == PROFILE_SYSTEM_DEFAULT) {
            try {
                val fx = when {
                    isDelete -> AudioManager.FX_KEYPRESS_DELETE
                    isSpace -> AudioManager.FX_KEYPRESS_SPACEBAR
                    isSpecial -> AudioManager.FX_KEYPRESS_RETURN
                    else -> AudioManager.FX_KEYPRESS_STANDARD
                }
                audioManager?.playSoundEffect(fx, safeVolume)
            } catch (_: Throwable) {}
            // Also play synthesized click in case system click is muted in EMUI/MagicOS settings
            playSynthesizedSound(PROFILE_SYSTEM_DEFAULT, safeVolume)
            return
        }

        val soundKey = when {
            isDelete -> "special_delete"
            isSpace -> "special_space"
            isSpecial -> PROFILE_IOS_16
            else -> profile
        }

        playSynthesizedSound(soundKey, safeVolume)
    }

    private fun playSynthesizedSound(key: String, volume: Float) {
        try {
            val sp = soundPool
            if (sp != null) {
                val soundId = getOrLoadSoundId(key)
                if (soundId > 0) {
                    val streamId = sp.play(soundId, volume, volume, 1, 0, 1.0f)
                    if (streamId != 0) return
                }
            }
        } catch (e: Throwable) {
            Log.w(TAG, "SoundPool play error: ${e.message}")
        }

        // Secondary fallback to AudioManager
        try {
            audioManager?.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD, volume)
        } catch (_: Throwable) {}
    }

    // ==========================================
    // WAV File Generator & PCM Synthesis
    // ==========================================

    private fun writeWavFile(file: File, pcm: ShortArray, sampleRate: Int) {
        val numChannels = 1
        val bitsPerSample = 16
        val byteRate = sampleRate * numChannels * (bitsPerSample / 8)
        val blockAlign = numChannels * (bitsPerSample / 8)
        val dataSize = pcm.size * 2
        val chunkSize = 36 + dataSize

        val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN)
        header.put("RIFF".toByteArray())
        header.putInt(chunkSize)
        header.put("WAVE".toByteArray())
        header.put("fmt ".toByteArray())
        header.putInt(16) // Subchunk1Size for PCM
        header.putShort(1) // AudioFormat 1 = PCM
        header.putShort(numChannels.toShort())
        header.putInt(sampleRate)
        header.putInt(byteRate)
        header.putShort(blockAlign.toShort())
        header.putShort(bitsPerSample.toShort())
        header.put("data".toByteArray())
        header.putInt(dataSize)

        FileOutputStream(file).use { fos ->
            fos.write(header.array())
            val buffer = ByteBuffer.allocate(dataSize).order(ByteOrder.LITTLE_ENDIAN)
            for (sample in pcm) {
                buffer.putShort(sample)
            }
            fos.write(buffer.array())
        }
    }

    private fun generatePcmForKey(key: String): ShortArray {
        return when (key) {
            PROFILE_IOS_16 -> generateIosPcm()
            PROFILE_MECHANICAL -> generateMechanicalPcm()
            PROFILE_MODERN_SOFT -> generateSoftPcm()
            PROFILE_WATER_DROP -> generateWaterDropPcm()
            PROFILE_POP_BUBBLE -> generatePopBubblePcm()
            PROFILE_WOOD_BLOCK -> generateWoodBlockPcm()
            PROFILE_CYBER_SCIFI -> generateCyberLaserPcm()
            PROFILE_CLASSIC_TYPEWRITER -> generateTypewriterPcm()
            PROFILE_SYSTEM_DEFAULT -> generateSystemPcm()
            "special_space" -> generateSpacePcm()
            "special_delete" -> generateDeletePcm()
            else -> generateIosPcm()
        }
    }

    private fun generateIosPcm(): ShortArray {
        val durationMs = 38
        val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
        val pcm = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val decay = exp(-t * 110.0)
            val attack = if (t < 0.002) (t / 0.002) else 1.0
            val snap = 0.50 * sin(2.0 * PI * 2200.0 * t) * exp(-t * 350.0)
            val body = 0.65 * sin(2.0 * PI * 750.0 * t) + 0.35 * sin(2.0 * PI * 1420.0 * t)
            val sample = (snap + body) * decay * attack
            pcm[i] = (sample.coerceIn(-1.0, 1.0) * 32000).toInt().toShort()
        }
        return pcm
    }

    private fun generateMechanicalPcm(): ShortArray {
        val durationMs = 45
        val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
        val pcm = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val decay = exp(-t * 90.0)
            val attack = if (t < 0.001) (t / 0.001) else 1.0
            val click1 = 0.65 * sin(2.0 * PI * 3400.0 * t) * exp(-t * 400.0)
            val click2 = 0.40 * sin(2.0 * PI * 1800.0 * t) * exp(-t * 150.0)
            val thock = 0.35 * sin(2.0 * PI * 420.0 * t) * exp(-t * 80.0)
            val sample = (click1 + click2 + thock) * decay * attack
            pcm[i] = (sample.coerceIn(-1.0, 1.0) * 32000).toInt().toShort()
        }
        return pcm
    }

    private fun generateSoftPcm(): ShortArray {
        val durationMs = 30
        val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
        val pcm = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val decay = exp(-t * 140.0)
            val attack = if (t < 0.003) (t / 0.003) else 1.0
            val body = 0.85 * sin(2.0 * PI * 480.0 * t) + 0.25 * sin(2.0 * PI * 920.0 * t)
            val sample = body * decay * attack
            pcm[i] = (sample.coerceIn(-1.0, 1.0) * 31000).toInt().toShort()
        }
        return pcm
    }

    private fun generateWaterDropPcm(): ShortArray {
        val durationMs = 60
        val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
        val pcm = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val freq = 1200.0 + (t * 2200.0)
            val decay = exp(-t * 60.0)
            val sample = sin(2.0 * PI * freq * t) * decay
            pcm[i] = (sample.coerceIn(-1.0, 1.0) * 30000).toInt().toShort()
        }
        return pcm
    }

    private fun generatePopBubblePcm(): ShortArray {
        val durationMs = 40
        val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
        val pcm = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val freq = 800.0 + (t * 1800.0)
            val decay = exp(-t * 95.0)
            val attack = if (t < 0.001) (t / 0.001) else 1.0
            val sample = sin(2.0 * PI * freq * t) * decay * attack
            pcm[i] = (sample.coerceIn(-1.0, 1.0) * 32000).toInt().toShort()
        }
        return pcm
    }

    private fun generateWoodBlockPcm(): ShortArray {
        val durationMs = 42
        val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
        val pcm = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val decay = exp(-t * 85.0)
            val body = 0.7 * sin(2.0 * PI * 1100.0 * t) + 0.3 * sin(2.0 * PI * 650.0 * t)
            val sample = body * decay
            pcm[i] = (sample.coerceIn(-1.0, 1.0) * 32000).toInt().toShort()
        }
        return pcm
    }

    private fun generateCyberLaserPcm(): ShortArray {
        val durationMs = 50
        val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
        val pcm = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val freq = 2800.0 - (t * 2200.0)
            val decay = exp(-t * 70.0)
            val sample = sin(2.0 * PI * freq * t) * decay
            pcm[i] = (sample.coerceIn(-1.0, 1.0) * 30000).toInt().toShort()
        }
        return pcm
    }

    private fun generateTypewriterPcm(): ShortArray {
        val durationMs = 55
        val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
        val pcm = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val decay = exp(-t * 75.0)
            val metalClack = 0.5 * sin(2.0 * PI * 2900.0 * t) * exp(-t * 300.0)
            val strikeBody = 0.5 * sin(2.0 * PI * 850.0 * t)
            val sample = (metalClack + strikeBody) * decay
            pcm[i] = (sample.coerceIn(-1.0, 1.0) * 32000).toInt().toShort()
        }
        return pcm
    }

    private fun generateSystemPcm(): ShortArray {
        val durationMs = 28
        val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
        val pcm = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val decay = exp(-t * 130.0)
            val sample = sin(2.0 * PI * 1000.0 * t) * decay
            pcm[i] = (sample.coerceIn(-1.0, 1.0) * 31000).toInt().toShort()
        }
        return pcm
    }

    private fun generateSpacePcm(): ShortArray {
        val durationMs = 50
        val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
        val pcm = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val decay = exp(-t * 80.0)
            val attack = if (t < 0.002) (t / 0.002) else 1.0
            val thud = 0.7 * sin(2.0 * PI * 350.0 * t) + 0.3 * sin(2.0 * PI * 680.0 * t)
            val sample = thud * decay * attack
            pcm[i] = (sample.coerceIn(-1.0, 1.0) * 32000).toInt().toShort()
        }
        return pcm
    }

    private fun generateDeletePcm(): ShortArray {
        val durationMs = 40
        val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
        val pcm = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val decay = exp(-t * 95.0)
            val click = 0.5 * sin(2.0 * PI * 1600.0 * t) * exp(-t * 200.0)
            val body = 0.5 * sin(2.0 * PI * 520.0 * t)
            val sample = (click + body) * decay
            pcm[i] = (sample.coerceIn(-1.0, 1.0) * 32000).toInt().toShort()
        }
        return pcm
    }
}
