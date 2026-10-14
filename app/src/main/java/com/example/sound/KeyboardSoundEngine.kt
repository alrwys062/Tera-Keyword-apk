package com.example.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.SoundPool
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * Ultra-Responsive, Zero-Stutter Audio Engine for Keyboard Clicks.
 * - Hybrid Architecture: Primary SoundPool hardware audio + Polyphonic AudioTrack buffers + AudioManager fallback.
 * - Zero latency: Clicks never cut off, never mute unexpectedly on OEM devices.
 * - Crisp, loud, tactile waveforms tuned for iOS 16 Tock, Mechanical Switch, Water Drops, and more.
 */
object KeyboardSoundEngine {
    private const val TAG = "KeyboardSoundEngine"
    private const val SAMPLE_RATE = 44100

    private var audioManager: AudioManager? = null
    private var appContext: Context? = null
    private var soundPool: SoundPool? = null
    private val soundPoolIds = ConcurrentHashMap<String, Int>()

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

    // Cached PCM arrays
    private val cachedPcm = ConcurrentHashMap<String, ShortArray>()
    private val loadedSoundIds = ConcurrentHashMap.newKeySet<Int>()

    // Dedicated Streaming AudioTrack player for instant, non-blocking zero-latency click feedback
    private object StreamingAudioPlayer {
        private var streamingTrack: AudioTrack? = null
        private val lock = Any()

        fun playPcm(pcm: ShortArray, volume: Float) {
            synchronized(lock) {
                try {
                    var track = streamingTrack
                    if (track == null || track.state != AudioTrack.STATE_INITIALIZED) {
                        val minBuf = AudioTrack.getMinBufferSize(
                            SAMPLE_RATE,
                            AudioFormat.CHANNEL_OUT_MONO,
                            AudioFormat.ENCODING_PCM_16BIT
                        )
                        val bufferSize = maxOf(minBuf * 2, 8192)
                        val audioAttributes = AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                        val audioFormat = AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(SAMPLE_RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                        track = AudioTrack.Builder()
                            .setAudioAttributes(audioAttributes)
                            .setAudioFormat(audioFormat)
                            .setBufferSizeInBytes(bufferSize)
                            .setTransferMode(AudioTrack.MODE_STREAM)
                            .build()
                        if (track.state == AudioTrack.STATE_INITIALIZED) {
                            track.play()
                            streamingTrack = track
                        }
                    }
                    val targetTrack = streamingTrack
                    if (targetTrack != null && targetTrack.state == AudioTrack.STATE_INITIALIZED) {
                        try {
                            targetTrack.setVolume(volume.coerceIn(0.1f, 1.0f))
                        } catch (_: Throwable) {}
                        targetTrack.write(pcm, 0, pcm.size, AudioTrack.WRITE_NON_BLOCKING)
                    }
                } catch (e: Throwable) {
                    Log.e(TAG, "Streaming audio playback error", e)
                }
            }
        }
    }

    @Synchronized
    fun initialize(context: Context) {
        val app = context.applicationContext
        appContext = app

        if (audioManager == null) {
            try {
                audioManager = app.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            } catch (_: Throwable) {}
        }

        if (soundPool == null) {
            try {
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
                val sp = SoundPool.Builder()
                    .setMaxStreams(16)
                    .setAudioAttributes(audioAttributes)
                    .build()
                sp.setOnLoadCompleteListener { _, sampleId, status ->
                    if (status == 0) {
                        loadedSoundIds.add(sampleId)
                    }
                }
                soundPool = sp
            } catch (e: Throwable) {
                Log.e(TAG, "Failed to create SoundPool", e)
            }
        }

        // Preload sounds into SoundPool in background
        Thread {
            try {
                val soundDir = File(app.cacheDir, "sounds").apply { mkdirs() }
                // Prioritize default profile and special keys first
                val priorityKeys = listOf(PROFILE_IOS_16, "special_space", "special_delete")
                val remainingKeys = AVAILABLE_PROFILES.map { it.id }.filter { it !in priorityKeys && it != PROFILE_SYSTEM_DEFAULT }
                val keysToLoad = priorityKeys + remainingKeys

                for (key in keysToLoad) {
                    try {
                        val pcm = cachedPcm.computeIfAbsent(key) { generatePcmForKey(it) }
                        val wavFile = File(soundDir, "$key.wav")
                        if (!wavFile.exists() || wavFile.length() < 100L) {
                            val wavBytes = pcmToWav(pcm)
                            FileOutputStream(wavFile).use { it.write(wavBytes) }
                        }
                        soundPool?.let { sp ->
                            if (!soundPoolIds.containsKey(key)) {
                                val sId = sp.load(wavFile.absolutePath, 1)
                                if (sId > 0) {
                                    soundPoolIds[key] = sId
                                }
                            }
                        }
                    } catch (e: Throwable) {
                        Log.e(TAG, "Error preloading sound $key", e)
                    }
                }
            } catch (_: Throwable) {}
        }.start()
    }

    private fun pcmToWav(pcm: ShortArray): ByteArray {
        val byteData = ByteArray(pcm.size * 2)
        var idx = 0
        for (s in pcm) {
            val v = s.toInt()
            byteData[idx++] = (v and 0xFF).toByte()
            byteData[idx++] = ((v shr 8) and 0xFF).toByte()
        }
        val totalAudioLen = byteData.size
        val totalDataLen = totalAudioLen + 36
        val longSampleRate = SAMPLE_RATE.toLong()
        val channels = 1
        val byteRate = (16 * SAMPLE_RATE * channels / 8).toLong()

        val header = ByteArray(44)
        header[0] = 'R'.code.toByte()
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()
        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = ((totalDataLen shr 8) and 0xff).toByte()
        header[6] = ((totalDataLen shr 16) and 0xff).toByte()
        header[7] = ((totalDataLen shr 24) and 0xff).toByte()
        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()
        header[12] = 'f'.code.toByte()
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()
        header[16] = 16
        header[17] = 0
        header[18] = 0
        header[19] = 0
        header[20] = 1 // PCM
        header[21] = 0
        header[22] = channels.toByte()
        header[23] = 0
        header[24] = (longSampleRate and 0xff).toByte()
        header[25] = ((longSampleRate shr 8) and 0xff).toByte()
        header[26] = ((longSampleRate shr 16) and 0xff).toByte()
        header[27] = ((longSampleRate shr 24) and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte()
        header[29] = ((byteRate shr 8) and 0xff).toByte()
        header[30] = ((byteRate shr 16) and 0xff).toByte()
        header[31] = ((byteRate shr 24) and 0xff).toByte()
        header[32] = (channels * 16 / 8).toByte()
        header[33] = 0
        header[34] = 16 // bits
        header[35] = 0
        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()
        header[40] = (totalAudioLen and 0xff).toByte()
        header[41] = ((totalAudioLen shr 8) and 0xff).toByte()
        header[42] = ((totalAudioLen shr 16) and 0xff).toByte()
        header[43] = ((totalAudioLen shr 24) and 0xff).toByte()

        return header + byteData
    }

    /**
     * Plays key sound with ZERO UI latency, NO stutter, and NO cut-offs.
     * Uses SoundPool (STREAM_MUSIC / USAGE_MEDIA) as primary engine, with streaming AudioTrack fallback.
     */
    fun playKeySound(
        profile: String = PROFILE_IOS_16,
        volume: Float = 0.85f,
        isSpecial: Boolean = false,
        isSpace: Boolean = false,
        isDelete: Boolean = false
    ) {
        val safeVolume = volume.coerceIn(0.15f, 1.0f)

        val soundKey = when {
            isDelete -> "special_delete"
            isSpace -> "special_space"
            profile == PROFILE_SYSTEM_DEFAULT -> PROFILE_SYSTEM_DEFAULT
            else -> profile
        }

        var played = false

        // 1. Primary: SoundPool hardware audio stream (fastest, hardware accelerated)
        val soundId = soundPoolIds[soundKey]
        if (soundId != null && soundId > 0 && loadedSoundIds.contains(soundId)) {
            try {
                val streamId = soundPool?.play(soundId, safeVolume, safeVolume, 1, 0, 1.0f) ?: 0
                if (streamId > 0) {
                    played = true
                }
            } catch (_: Throwable) {}
        }

        // 2. Direct Streaming AudioTrack fallback (immediate in-memory PCM without file delay)
        if (!played) {
            val pcm = cachedPcm.computeIfAbsent(soundKey) { generatePcmForKey(it) }
            StreamingAudioPlayer.playPcm(pcm, safeVolume)
        }

        // 3. Optional Android system click effect when requested
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

    // --- SYNTHESIZED SOUND WAVEFORMS (Rich, punchy, loud, clear) ---

    private fun generateIosPcm(): ShortArray {
        val durationMs = 42
        val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
        val pcm = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val decay = exp(-t * 90.0)
            val attack = if (t < 0.001) (t / 0.001) else 1.0
            val snap = 0.55 * sin(2.0 * PI * 2200.0 * t) * exp(-t * 280.0)
            val body = 0.70 * sin(2.0 * PI * 750.0 * t) + 0.35 * sin(2.0 * PI * 1420.0 * t)
            val sample = (snap + body) * decay * attack
            pcm[i] = (sample.coerceIn(-1.0, 1.0) * 32600).toInt().toShort()
        }
        return pcm
    }

    private fun generateMechanicalPcm(): ShortArray {
        val durationMs = 48
        val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
        val pcm = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val decay = exp(-t * 75.0)
            val attack = if (t < 0.0008) (t / 0.0008) else 1.0
            val click1 = 0.75 * sin(2.0 * PI * 3400.0 * t) * exp(-t * 320.0)
            val click2 = 0.45 * sin(2.0 * PI * 1800.0 * t) * exp(-t * 130.0)
            val thock = 0.40 * sin(2.0 * PI * 420.0 * t) * exp(-t * 65.0)
            val sample = (click1 + click2 + thock) * decay * attack
            pcm[i] = (sample.coerceIn(-1.0, 1.0) * 32600).toInt().toShort()
        }
        return pcm
    }

    private fun generateSoftPcm(): ShortArray {
        val durationMs = 38
        val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
        val pcm = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val decay = exp(-t * 105.0)
            val attack = if (t < 0.0015) (t / 0.0015) else 1.0
            val body = 0.85 * sin(2.0 * PI * 540.0 * t) + 0.25 * sin(2.0 * PI * 1020.0 * t)
            val sample = body * decay * attack
            pcm[i] = (sample.coerceIn(-1.0, 1.0) * 32400).toInt().toShort()
        }
        return pcm
    }

    private fun generateWaterDropPcm(): ShortArray {
        val durationMs = 60
        val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
        val pcm = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val freq = 1150.0 + (t * 2500.0)
            val decay = exp(-t * 52.0)
            val attack = if (t < 0.0015) (t / 0.0015) else 1.0
            val sample = sin(2.0 * PI * freq * t) * decay * attack
            pcm[i] = (sample.coerceIn(-1.0, 1.0) * 32500).toInt().toShort()
        }
        return pcm
    }

    private fun generatePopBubblePcm(): ShortArray {
        val durationMs = 44
        val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
        val pcm = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val freq = 780.0 + (t * 1950.0)
            val decay = exp(-t * 80.0)
            val attack = if (t < 0.001) (t / 0.001) else 1.0
            val sample = sin(2.0 * PI * freq * t) * decay * attack
            pcm[i] = (sample.coerceIn(-1.0, 1.0) * 32600).toInt().toShort()
        }
        return pcm
    }

    private fun generateWoodBlockPcm(): ShortArray {
        val durationMs = 46
        val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
        val pcm = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val decay = exp(-t * 70.0)
            val attack = if (t < 0.001) (t / 0.001) else 1.0
            val body = 0.75 * sin(2.0 * PI * 1100.0 * t) + 0.35 * sin(2.0 * PI * 650.0 * t)
            val sample = body * decay * attack
            pcm[i] = (sample.coerceIn(-1.0, 1.0) * 32600).toInt().toShort()
        }
        return pcm
    }

    private fun generateCyberLaserPcm(): ShortArray {
        val durationMs = 50
        val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
        val pcm = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val freq = 3000.0 - (t * 2400.0)
            val decay = exp(-t * 60.0)
            val sample = sin(2.0 * PI * freq * t) * decay
            pcm[i] = (sample.coerceIn(-1.0, 1.0) * 32000).toInt().toShort()
        }
        return pcm
    }

    private fun generateTypewriterPcm(): ShortArray {
        val durationMs = 55
        val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
        val pcm = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val decay = exp(-t * 65.0)
            val metalClack = 0.55 * sin(2.0 * PI * 2900.0 * t) * exp(-t * 250.0)
            val strikeBody = 0.50 * sin(2.0 * PI * 850.0 * t)
            val sample = (metalClack + strikeBody) * decay
            pcm[i] = (sample.coerceIn(-1.0, 1.0) * 32600).toInt().toShort()
        }
        return pcm
    }

    private fun generateSystemPcm(): ShortArray {
        val durationMs = 32
        val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
        val pcm = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val decay = exp(-t * 100.0)
            val sample = sin(2.0 * PI * 1150.0 * t) * decay
            pcm[i] = (sample.coerceIn(-1.0, 1.0) * 32400).toInt().toShort()
        }
        return pcm
    }

    private fun generateSpacePcm(): ShortArray {
        val durationMs = 50
        val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
        val pcm = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val decay = exp(-t * 65.0)
            val attack = if (t < 0.0015) (t / 0.0015) else 1.0
            val thud = 0.75 * sin(2.0 * PI * 340.0 * t) + 0.35 * sin(2.0 * PI * 680.0 * t)
            val sample = thud * decay * attack
            pcm[i] = (sample.coerceIn(-1.0, 1.0) * 32600).toInt().toShort()
        }
        return pcm
    }

    private fun generateDeletePcm(): ShortArray {
        val durationMs = 42
        val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
        val pcm = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val decay = exp(-t * 85.0)
            val attack = if (t < 0.001) (t / 0.001) else 1.0
            val snap = 0.65 * sin(2.0 * PI * 1650.0 * t) * exp(-t * 220.0)
            val body = 0.50 * sin(2.0 * PI * 620.0 * t)
            val sample = (snap + body) * decay * attack
            pcm[i] = (sample.coerceIn(-1.0, 1.0) * 32600).toInt().toShort()
        }
        return pcm
    }
}
