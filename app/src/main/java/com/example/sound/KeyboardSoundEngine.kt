package com.example.sound

import android.content.Context
import android.media.AudioManager

object KeyboardSoundEngine {
    private const val TAG = "KeyboardSoundEngine"

    private var audioManager: AudioManager? = null

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

    @Synchronized
    fun initialize(context: Context) {
        if (isInitialized && audioManager != null) return
        val appContext = context.applicationContext
        try {
            audioManager = appContext.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            isInitialized = true
        } catch (_: Exception) {}
    }

    fun playKeySound(
        profile: String = PROFILE_IOS_16,
        volume: Float = 0.85f,
        isSpecial: Boolean = false,
        isSpace: Boolean = false,
        isDelete: Boolean = false
    ) {
        val safeVolume = volume.coerceIn(0.1f, 1.0f)
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
        } catch (_: Exception) {}
    }
}
