package com.example.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import android.util.Log
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * Ultra-Responsive, Zero-Latency Keyboard Audio Engine.
 * - Completely non-blocking: all audio playback runs on dedicated background threads.
 * - Pure in-memory AudioTrack synthesis: no media decoders / CCodec / Codec2 HAL allocations.
 * - Zero UI Thread blocking to guarantee 120 FPS buttery smooth typing without dropped letters.
 */
object KeyboardSoundEngine {
    private const val TAG = "KeyboardSoundEngine"
    private const val SAMPLE_RATE = 44100

    private var audioManager: AudioManager? = null
    private var appContext: Context? = null

    // Single-threaded high-priority audio executor for instant non-blocking audio dispatching
    private val audioExecutor = Executors.newSingleThreadExecutor { r ->
        Thread(r, "KeyboardSoundThread").apply {
            priority = Thread.MAX_PRIORITY
        }
    }

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

    private const val TRACK_POOL_SIZE = 3

    // Thread-safe cache of pre-synthesized PCM waveform samples (in-memory, zero file IO)
    private val cachedPcm = ConcurrentHashMap<String, ShortArray>()

    // Pool of streaming AudioTracks for instant multi-voice polyphony (no clipping, zero latency)
    private val streamTracks = arrayOfNulls<AudioTrack>(TRACK_POOL_SIZE)
    private var roundRobinIndex = 0

    @Synchronized
    fun initialize(context: Context) {
        val app = context.applicationContext
        appContext = app

        if (audioManager == null) {
            try {
                audioManager = app.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            } catch (_: Throwable) {}
        }

        // Pre-warm PCM and audio stream tracks in background
        audioExecutor.execute {
            try {
                // Pre-generate standard sounds
                getPcmForSoundKey(PROFILE_IOS_16)
                getPcmForSoundKey(PROFILE_MECHANICAL)
                getPcmForSoundKey("special_space")
                getPcmForSoundKey("special_delete")

                // Pre-initialize stream tracks
                for (i in 0 until TRACK_POOL_SIZE) {
                    getStreamTrack(i)
                }
            } catch (_: Throwable) {}
        }
    }

    private fun getStreamTrack(index: Int): AudioTrack? {
        val safeIndex = Math.floorMod(index, TRACK_POOL_SIZE)
        val existing = streamTracks[safeIndex]
        if (existing != null && existing.state == AudioTrack.STATE_INITIALIZED) {
            return existing
        }

        synchronized(streamTracks) {
            val checkAgain = streamTracks[safeIndex]
            if (checkAgain != null && checkAgain.state == AudioTrack.STATE_INITIALIZED) {
                return checkAgain
            }

            return try {
                try {
                    existing?.release()
                } catch (_: Throwable) {}

                val minBuf = AudioTrack.getMinBufferSize(
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )
                val bufferSize = maxOf(minBuf * 2, 4096)

                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()

                val audioFormat = AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()

                val track = AudioTrack.Builder()
                    .setAudioAttributes(audioAttributes)
                    .setAudioFormat(audioFormat)
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                track.play()
                streamTracks[safeIndex] = track
                track
            } catch (e: Throwable) {
                Log.w(TAG, "Failed to create stream AudioTrack", e)
                null
            }
        }
    }

    private fun getPcmForSoundKey(key: String): ShortArray {
        return cachedPcm.computeIfAbsent(key) { generatePcmForKey(it) }
    }

    /**
     * Plays key sound with ZERO UI latency (asynchronous dispatch to dedicated audio thread).
     * Pure in-memory AudioTrack stream: requires zero MediaCodec/CCodec hardware instances,
     * routes through STREAM_MUSIC (USAGE_MEDIA) to guarantee loud, clear sound even when system touch sound is off.
     */
    fun playKeySound(
        profile: String = PROFILE_IOS_16,
        volume: Float = 0.85f,
        isSpecial: Boolean = false,
        isSpace: Boolean = false,
        isDelete: Boolean = false
    ) {
        val safeVolume = volume.coerceIn(0.1f, 1.0f)

        val soundKey = when {
            isDelete -> "special_delete"
            isSpace -> "special_space"
            isSpecial -> PROFILE_IOS_16
            else -> profile
        }

        // Asynchronous non-blocking dispatch to prevent UI dropped frames during fast typing
        audioExecutor.execute {
            val pcm = getPcmForSoundKey(soundKey)
            val scaledPcm = if (safeVolume >= 0.98f) {
                pcm
            } else {
                val scaled = ShortArray(pcm.size)
                for (i in pcm.indices) {
                    scaled[i] = (pcm[i] * safeVolume).toInt().coerceIn(-32767, 32767).toShort()
                }
                scaled
            }

            try {
                val idx = roundRobinIndex++
                val track = getStreamTrack(idx)
                if (track != null) {
                    if (track.playState != AudioTrack.PLAYSTATE_PLAYING) {
                        try {
                            track.play()
                        } catch (_: Throwable) {}
                    }
                    track.write(scaledPcm, 0, scaledPcm.size, AudioTrack.WRITE_NON_BLOCKING)
                }
            } catch (_: Throwable) {}

            // Native system click effect when requested or as complementary tactile sound
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
