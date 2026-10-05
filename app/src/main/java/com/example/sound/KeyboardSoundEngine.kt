package com.example.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.SoundPool
import android.os.Build
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

object KeyboardSoundEngine {
    private const val TAG = "KeyboardSoundEngine"
    private const val SAMPLE_RATE = 44100

    private var soundPool: SoundPool? = null
    private var audioManager: AudioManager? = null
    private val soundMap = ConcurrentHashMap<String, Int>()
    private val loadedSoundIds = ConcurrentHashMap.newKeySet<Int>()
    private val rawPcmMap = HashMap<String, ShortArray>()
    private val wavBytesCache = HashMap<String, ByteArray>()
    private val staticTracks = ConcurrentHashMap<String, AudioTrack>()
    private val audioExecutor: ExecutorService = Executors.newFixedThreadPool(2)

    @Volatile
    private var isInitialized = false

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

    init {
        // Pre-generate raw PCM waveforms in memory
        rawPcmMap[PROFILE_IOS_16] = generateIos16Pcm()
        rawPcmMap[PROFILE_MECHANICAL] = generateMechanicalPcm()
        rawPcmMap[PROFILE_MODERN_SOFT] = generateModernSoftPcm()
        rawPcmMap[PROFILE_WATER_DROP] = generateWaterDropPcm()
        rawPcmMap[PROFILE_POP_BUBBLE] = generatePopBubblePcm()
        rawPcmMap[PROFILE_WOOD_BLOCK] = generateWoodBlockPcm()
        rawPcmMap[PROFILE_CYBER_SCIFI] = generateCyberScifiPcm()
        rawPcmMap[PROFILE_CLASSIC_TYPEWRITER] = generateTypewriterPcm()

        rawPcmMap.forEach { (profileKey, pcm) ->
            wavBytesCache[profileKey] = encodeWav(pcm)
        }
    }

    @Synchronized
    fun initialize(context: Context) {
        val appContext = context.applicationContext
        try {
            audioManager = appContext.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        } catch (_: Exception) {}

        if (isInitialized && soundPool != null) return

        try {
            // Build SoundPool with USAGE_MEDIA so sounds are NEVER muted by system touch effects toggle
            val attributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()

            val pool = SoundPool.Builder()
                .setMaxStreams(12)
                .setAudioAttributes(attributes)
                .build()

            pool.setOnLoadCompleteListener { _, sampleId, status ->
                if (status == 0) {
                    loadedSoundIds.add(sampleId)
                }
            }

            soundPool = pool

            // Write and load each sound file into SoundPool
            wavBytesCache.forEach { (profileKey, wavBytes) ->
                try {
                    val file = File(appContext.cacheDir, "snd_v4_$profileKey.wav")
                    if (!file.exists() || file.length() != wavBytes.size.toLong()) {
                        FileOutputStream(file).use { it.write(wavBytes) }
                    }
                    val soundId = pool.load(file.absolutePath, 1)
                    if (soundId != 0) {
                        soundMap[profileKey] = soundId
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "SoundPool file load error for $profileKey: ${e.message}")
                }
            }

            // Also prepare static direct AudioTracks for instant 0ms fallback
            initStaticAudioTracks()

            isInitialized = true
        } catch (e: Exception) {
            Log.e(TAG, "Sound engine initialization error: ${e.message}")
        }
    }

    private fun initStaticAudioTracks() {
        rawPcmMap.forEach { (profileKey, pcm) ->
            try {
                val byteSize = pcm.size * 2
                val track = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    AudioTrack.Builder()
                        .setAudioAttributes(
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .build()
                        )
                        .setAudioFormat(
                            AudioFormat.Builder()
                                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                                .setSampleRate(SAMPLE_RATE)
                                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                                .build()
                        )
                        .setBufferSizeInBytes(byteSize)
                        .setTransferMode(AudioTrack.MODE_STATIC)
                        .build()
                } else {
                    @Suppress("DEPRECATION")
                    AudioTrack(
                        AudioManager.STREAM_MUSIC,
                        SAMPLE_RATE,
                        AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_16BIT,
                        byteSize,
                        AudioTrack.MODE_STATIC
                    )
                }
                track.write(pcm, 0, pcm.size)
                staticTracks[profileKey] = track
            } catch (e: Exception) {
                Log.e(TAG, "AudioTrack init error for $profileKey: ${e.message}")
            }
        }
    }

    fun playKeySound(
        profile: String = PROFILE_IOS_16,
        volume: Float = 0.85f,
        isSpecial: Boolean = false,
        isSpace: Boolean = false,
        isDelete: Boolean = false
    ) {
        val safeVolume = volume.coerceIn(0.1f, 1.0f)

        if (profile == PROFILE_SYSTEM_DEFAULT) {
            playSystemSound(safeVolume, isSpecial, isSpace, isDelete)
            return
        }

        val soundId = soundMap[profile] ?: soundMap[PROFILE_IOS_16]
        val pool = soundPool

        val pitch = when {
            isDelete -> 0.88f
            isSpace -> 0.94f
            isSpecial -> 0.97f
            else -> 1.0f
        }

        var played = false

        // 1. Primary: SoundPool with USAGE_MEDIA
        if (pool != null && soundId != null && soundId != 0 && loadedSoundIds.contains(soundId)) {
            try {
                val streamId = pool.play(soundId, safeVolume, safeVolume, 1, 0, pitch)
                if (streamId != 0) {
                    played = true
                }
            } catch (_: Exception) {
                played = false
            }
        }

        // 2. High-Performance Direct AudioTrack Fallback (0ms latency, zero files required)
        if (!played) {
            val key = if (rawPcmMap.containsKey(profile)) profile else PROFILE_IOS_16
            val track = staticTracks[key]
            if (track != null && track.state == AudioTrack.STATE_INITIALIZED) {
                try {
                    track.stop()
                    track.setPlaybackHeadPosition(0)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        track.setVolume(safeVolume)
                    }
                    track.play()
                    played = true
                } catch (e: Exception) {
                    played = false
                }
            }
        }

        // 3. Fallback: Streaming AudioTrack for instant synthesized wave
        if (!played) {
            val pcm = rawPcmMap[profile] ?: rawPcmMap[PROFILE_IOS_16]
            if (pcm != null) {
                audioExecutor.execute {
                    try {
                        val minBuf = AudioTrack.getMinBufferSize(
                            SAMPLE_RATE,
                            AudioFormat.CHANNEL_OUT_MONO,
                            AudioFormat.ENCODING_PCM_16BIT
                        ).coerceAtLeast(pcm.size * 2)

                        val track = AudioTrack(
                            AudioManager.STREAM_MUSIC,
                            SAMPLE_RATE,
                            AudioFormat.CHANNEL_OUT_MONO,
                            AudioFormat.ENCODING_PCM_16BIT,
                            minBuf,
                            AudioTrack.MODE_STREAM
                        )
                        track.play()
                        track.write(pcm, 0, pcm.size)
                        track.stop()
                        track.release()
                    } catch (_: Exception) {
                        playSystemSound(safeVolume, isSpecial, isSpace, isDelete)
                    }
                }
            } else {
                playSystemSound(safeVolume, isSpecial, isSpace, isDelete)
            }
        }
    }

    private fun playSystemSound(volume: Float, isSpecial: Boolean, isSpace: Boolean, isDelete: Boolean) {
        try {
            val fx = when {
                isDelete -> AudioManager.FX_KEYPRESS_DELETE
                isSpace -> AudioManager.FX_KEYPRESS_SPACEBAR
                isSpecial -> AudioManager.FX_KEYPRESS_RETURN
                else -> AudioManager.FX_KEYPRESS_STANDARD
            }
            audioManager?.playSoundEffect(fx, volume)
        } catch (_: Exception) {}
    }

    // ------------------------------------------------------------------------
    // High-Fidelity Waveform Synthesizers for 8 Unique Sound Profiles
    // ------------------------------------------------------------------------

    private fun generateIos16Pcm(): ShortArray {
        val durationMs = 30
        val numSamples = (SAMPLE_RATE * durationMs) / 1000
        val pcm = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val body = sin(2 * PI * 340.0 * t) * exp(-t / 0.007)
            val transient = if (t < 0.004) sin(2 * PI * 2600.0 * t) * exp(-t / 0.0015) * 0.55 else 0.0
            val sample = (body + transient).coerceIn(-1.0, 1.0)
            pcm[i] = (sample * 31500).toInt().toShort()
        }
        return pcm
    }

    private fun generateMechanicalPcm(): ShortArray {
        val durationMs = 40
        val numSamples = (SAMPLE_RATE * durationMs) / 1000
        val pcm = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val click = sin(2 * PI * 2800.0 * t) * exp(-t / 0.003)
            val clack = if (t > 0.004) {
                val t2 = t - 0.004
                sin(2 * PI * 1200.0 * t2) * exp(-t2 / 0.008) * 0.8
            } else 0.0
            val sample = (click + clack).coerceIn(-1.0, 1.0)
            pcm[i] = (sample * 32000).toInt().toShort()
        }
        return pcm
    }

    private fun generateModernSoftPcm(): ShortArray {
        val durationMs = 25
        val numSamples = (SAMPLE_RATE * durationMs) / 1000
        val pcm = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val sample = sin(2 * PI * 300.0 * t) * exp(-t / 0.006)
            pcm[i] = (sample * 29000).toInt().toShort()
        }
        return pcm
    }

    private fun generateWaterDropPcm(): ShortArray {
        val durationMs = 45
        val numSamples = (SAMPLE_RATE * durationMs) / 1000
        val pcm = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val freq = 480.0 + (950.0 * (t / (durationMs / 1000.0)))
            val sample = sin(2 * PI * freq * t) * exp(-t / 0.014)
            pcm[i] = (sample * 31000).toInt().toShort()
        }
        return pcm
    }

    private fun generatePopBubblePcm(): ShortArray {
        val durationMs = 32
        val numSamples = (SAMPLE_RATE * durationMs) / 1000
        val pcm = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val freq = 1900.0 - (1450.0 * (t / (durationMs / 1000.0)))
            val sample = sin(2 * PI * freq * t) * exp(-t / 0.008)
            pcm[i] = (sample * 31500).toInt().toShort()
        }
        return pcm
    }

    private fun generateWoodBlockPcm(): ShortArray {
        val durationMs = 28
        val numSamples = (SAMPLE_RATE * durationMs) / 1000
        val pcm = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val harmonic1 = sin(2 * PI * 920.0 * t) * exp(-t / 0.005)
            val harmonic2 = sin(2 * PI * 1840.0 * t) * exp(-t / 0.003) * 0.45
            val sample = (harmonic1 + harmonic2).coerceIn(-1.0, 1.0)
            pcm[i] = (sample * 31500).toInt().toShort()
        }
        return pcm
    }

    private fun generateCyberScifiPcm(): ShortArray {
        val durationMs = 35
        val numSamples = (SAMPLE_RATE * durationMs) / 1000
        val pcm = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val sweep = sin(2 * PI * (3400.0 - (2400.0 * (t / 0.035))) * t) * exp(-t / 0.009)
            val sub = sin(2 * PI * 200.0 * t) * exp(-t / 0.012) * 0.35
            val sample = (sweep + sub).coerceIn(-1.0, 1.0)
            pcm[i] = (sample * 31000).toInt().toShort()
        }
        return pcm
    }

    private fun generateTypewriterPcm(): ShortArray {
        val durationMs = 36
        val numSamples = (SAMPLE_RATE * durationMs) / 1000
        val pcm = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val metal1 = sin(2 * PI * 1700.0 * t) * exp(-t / 0.004)
            val metal2 = sin(2 * PI * 3600.0 * t) * exp(-t / 0.002) * 0.6
            val sample = (metal1 + metal2).coerceIn(-1.0, 1.0)
            pcm[i] = (sample * 31500).toInt().toShort()
        }
        return pcm
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
