package com.example.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Process
import android.util.Log
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * High-performance, zero-latency PCM audio synthesis and playback engine.
 * Directly streams mixed 16-bit PCM audio through AudioTrack, completely bypassing
 * Stagefright, MediaCodec, and SoundPool file decoders to eliminate HAL/Codec2 resource errors.
 */
object KeyboardSoundEngine {
    private const val TAG = "KeyboardSoundEngine"
    private const val SAMPLE_RATE = 44100
    private const val CHUNK_SIZE = 256 // ~5.8ms per audio frame chunk for ultra-low latency

    private var audioManager: AudioManager? = null
    private var audioTrack: AudioTrack? = null

    @Volatile
    private var isInitialized = false

    @Volatile
    private var isRunning = false

    private val lock = ReentrantLock()
    private val audioCondition = lock.newCondition()

    private class ActiveVoice(
        val pcm: ShortArray,
        val volume: Float,
        var position: Int = 0
    )

    private val activeVoices = ArrayList<ActiveVoice>(8)
    private val pcmCache = ConcurrentHashMap<String, ShortArray>()

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
        val appCtx = context.applicationContext
        try {
            audioManager = appCtx.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        } catch (_: Throwable) {}

        if (isInitialized) return
        isInitialized = true

        // Clean up any legacy sound files from older versions
        try {
            val soundsDir = File(appCtx.filesDir, "kb_sounds")
            if (soundsDir.exists()) {
                soundsDir.deleteRecursively()
            }
        } catch (_: Throwable) {}

        // Pre-warm default PCM cache in memory
        preloadProfile(appCtx, PROFILE_IOS_16)

        // Start dedicated real-time audio thread
        startAudioThread()
    }

    private fun startAudioThread() {
        if (isRunning) return
        isRunning = true

        val audioThread = Thread({
            Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO)
            
            val minBufSize = AudioTrack.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val bufferSize = maxOf(minBufSize, CHUNK_SIZE * 4 * 2)

            var track: AudioTrack? = null
            try {
                track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(SAMPLE_RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                track.play()
                audioTrack = track

                val mixBuffer = ShortArray(CHUNK_SIZE)
                val voicesToProcess = ArrayList<ActiveVoice>()

                var idleFrames = 0
                val maxIdleFrames = 20 // ~110ms of silence before sleeping

                while (isRunning) {
                    voicesToProcess.clear()

                    lock.withLock {
                        if (activeVoices.isEmpty()) {
                            if (idleFrames >= maxIdleFrames) {
                                audioCondition.await()
                                idleFrames = 0
                            }
                        }
                        if (activeVoices.isNotEmpty()) {
                            voicesToProcess.addAll(activeVoices)
                        }
                    }

                    if (voicesToProcess.isEmpty()) {
                        idleFrames++
                        mixBuffer.fill(0)
                        track.write(mixBuffer, 0, CHUNK_SIZE)
                        continue
                    }

                    idleFrames = 0
                    mixBuffer.fill(0)

                    val finishedVoices = ArrayList<ActiveVoice>()

                    for (voice in voicesToProcess) {
                        val pcm = voice.pcm
                        val vol = voice.volume
                        var pos = voice.position
                        val end = minOf(pos + CHUNK_SIZE, pcm.size)

                        var outIdx = 0
                        for (i in pos until end) {
                            val mixed = (mixBuffer[outIdx] + (pcm[i] * vol).toInt())
                            mixBuffer[outIdx] = mixed.coerceIn(-32768, 32767).toShort()
                            outIdx++
                        }

                        voice.position = end
                        if (voice.position >= pcm.size) {
                            finishedVoices.add(voice)
                        }
                    }

                    if (finishedVoices.isNotEmpty()) {
                        lock.withLock {
                            activeVoices.removeAll(finishedVoices.toSet())
                        }
                    }

                    track.write(mixBuffer, 0, CHUNK_SIZE)
                }
            } catch (e: Throwable) {
                Log.w(TAG, "AudioTrack synthesis thread encountered issue: ${e.message}")
            } finally {
                try {
                    track?.stop()
                    track?.release()
                } catch (_: Throwable) {}
                audioTrack = null
                isRunning = false
            }
        }, "KeyboardAudioMixer")

        audioThread.isDaemon = true
        audioThread.start()
    }

    fun preloadProfile(context: Context, profile: String) {
        getPcmCached(profile)
    }

    private fun getPcmCached(key: String): ShortArray {
        return pcmCache.getOrPut(key) {
            when (key) {
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
    }

    fun playKeySound(
        profile: String = PROFILE_IOS_16,
        volume: Float = 0.85f,
        isSpecial: Boolean = false,
        isSpace: Boolean = false,
        isDelete: Boolean = false
    ) {
        val safeVolume = volume.coerceIn(0.20f, 1.0f)

        // Android System Sound Effect path
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
            return
        }

        val soundKey = when {
            isDelete -> "special_delete"
            isSpace -> "special_space"
            isSpecial -> PROFILE_IOS_16
            else -> profile
        }

        val pcm = getPcmCached(soundKey)

        if (!isRunning) {
            startAudioThread()
        }

        lock.withLock {
            // Cap active polyphony at 6 simultaneous voices to prevent volume clipping
            if (activeVoices.size >= 6) {
                activeVoices.removeAt(0)
            }
            activeVoices.add(ActiveVoice(pcm, safeVolume))
            audioCondition.signal()
        }
    }

    // ==========================================
    // PCM Audio Waveform Synthesis Algorithms
    // ==========================================

    private fun generateIosPcm(): ShortArray {
        val durationMs = 38
        val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
        val pcm = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val decay = exp(-t * 110.0)
            val attack = if (t < 0.002) (t / 0.002) else 1.0
            val snap = 0.45 * sin(2.0 * PI * 2200.0 * t) * exp(-t * 350.0)
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
            val click1 = 0.6 * sin(2.0 * PI * 3400.0 * t) * exp(-t * 400.0)
            val click2 = 0.4 * sin(2.0 * PI * 1800.0 * t) * exp(-t * 150.0)
            val thock = 0.3 * sin(2.0 * PI * 420.0 * t) * exp(-t * 80.0)
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
            val body = 0.8 * sin(2.0 * PI * 480.0 * t) + 0.2 * sin(2.0 * PI * 920.0 * t)
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
