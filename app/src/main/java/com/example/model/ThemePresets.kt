package com.example.model

import androidx.compose.ui.graphics.Color

object ThemePresets {

    fun isLightColor(colorLong: Long): Boolean {
        val r = ((colorLong shr 16) and 0xFF) / 255.0
        val g = ((colorLong shr 8) and 0xFF) / 255.0
        val b = (colorLong and 0xFF) / 255.0
        return (0.299 * r + 0.587 * g + 0.114 * b) > 0.55
    }

    fun resolveKeyColor(colorLong: Long, opacityFactor: Float = 1.0f): Color {
        val alphaInt = ((colorLong shr 24) and 0xFF).toInt()
        val originalAlpha = if (alphaInt == 0) 1.0f else alphaInt / 255.0f
        val finalAlpha = (originalAlpha * opacityFactor.coerceIn(0.15f, 1.0f)).coerceIn(0.15f, 1.0f)
        val r = ((colorLong shr 16) and 0xFF).toInt()
        val g = ((colorLong shr 8) and 0xFF).toInt()
        val b = (colorLong and 0xFF).toInt()
        return Color(red = r, green = g, blue = b, alpha = (finalAlpha * 255).toInt())
    }

    fun getEnterIconTint(theme: KeyboardTheme): Color {
        return if (isLightColor(theme.enterButtonColor)) Color(0xFF0F172A) else Color.White
    }

    fun toDayMode(theme: KeyboardTheme): KeyboardTheme {
        if (isLightColor(theme.backgroundColor)) return theme
        return theme.copy(
            backgroundColor = 0xFFF1F5F9,
            keyBackgroundColor = 0xFFFFFFFF,
            keyPressedColor = 0xFFE2E8F0,
            keyTextColor = 0xFF0F172A,
            subtextColor = 0xFF64748B,
            toolbarColor = 0xFFF8FAFC,
            borderColor = 0xFFE2E8F0,
            borderAlpha = theme.borderAlpha.coerceAtMost(0.6f)
        )
    }

    fun toNightMode(theme: KeyboardTheme): KeyboardTheme {
        if (!isLightColor(theme.backgroundColor)) return theme
        return theme.copy(
            backgroundColor = 0xFF111827,
            keyBackgroundColor = 0xFF1F2937,
            keyPressedColor = 0xFF374151,
            keyTextColor = 0xFFF9FAFB,
            subtextColor = 0xFF9CA3AF,
            toolbarColor = 0xFF0F172A,
            borderColor = 0xFF374151,
            borderAlpha = theme.borderAlpha.coerceAtLeast(0.4f)
        )
    }

    // =========================================================================
    // FLAGSHIP WORLD-CLASS DEFAULT THEME — Royal Titanium & Ruby Luxe
    // =========================================================================
    val ROYAL_TITANIUM_LUXE = KeyboardTheme(
        id = "royal_titanium_luxe",
        nameAr = "👑 رويال تيتانيوم لوكس (الأفضل بالعالم)",
        nameEn = "Royal Titanium & Ruby Luxe (World Best)",
        backgroundColor = 0xFF101216,
        backgroundGradient = listOf(0xFF161920, 0xFF0B0D10),
        keyBackgroundColor = 0xFF1F232D,
        keyPressedColor = 0xFF2F3646,
        keyTextColor = 0xFFFFFFFF,
        subtextColor = 0xFF94A3B8,
        accentColor = 0xFF38BDF8,
        enterButtonColor = 0xFFDC2626,
        specialKeyColor = 0xFFB91C1C,
        toolbarColor = 0xFF13161C,
        borderColor = 0xFF363E4E,
        borderAlpha = 0.85f,
        cornerRadius = 11.5f,
        keyStyle = "neon",
        category = "twenty_premium"
    )

    val CRYSTAL_LIGHT = KeyboardTheme(
        id = "crystal_light",
        nameAr = "كريستال رويال الفاخر",
        nameEn = "Royal Crystal Luxe",
        backgroundColor = 0xFFF4F6FB,
        keyBackgroundColor = 0xFFFFFFFF,
        keyPressedColor = 0xFFE2E8F0,
        keyTextColor = 0xFF0F172A,
        subtextColor = 0xFF64748B,
        accentColor = 0xFF2563EB,
        enterButtonColor = 0xFF2563EB,
        toolbarColor = 0xFFEDF2F9,
        borderColor = 0xFFD8E1ED,
        borderAlpha = 0.85f,
        cornerRadius = 9.5f,
        keyStyle = "glass_light",
        category = "twenty_premium"
    )

    // =========================================================================
    // 20 PREMIUM KEYBOARD THEMES (Inspired by Facemoji & Modern Flagships)
    // =========================================================================

    // 1. Facemoji Classic — clean, simple, dark keys
    val FACEMOJI_CLASSIC = KeyboardTheme(
        id = "facemoji_classic",
        nameAr = "فيسموجي كلاسيك",
        nameEn = "Facemoji Classic",
        backgroundColor = 0xFF12161F,
        keyBackgroundColor = 0xFF1E2638,
        keyPressedColor = 0xFF2A364E,
        keyTextColor = 0xFFFFFFFF,
        subtextColor = 0xFF8D99AE,
        accentColor = 0xFFFFD600,
        enterButtonColor = 0xFF2563EB,
        toolbarColor = 0xFF0E131C,
        borderColor = 0xFF2B374E,
        cornerRadius = 8f,
        keyStyle = "rounded",
        category = "twenty_premium"
    )

    // 2. Dark Neon — black background with cyan and purple neon borders
    val DARK_NEON = KeyboardTheme(
        id = "dark_neon",
        nameAr = "دارك نيون",
        nameEn = "Dark Neon",
        backgroundColor = 0xFF05060A,
        keyBackgroundColor = 0xFF0E1222,
        keyPressedColor = 0xFF1E2240,
        keyTextColor = 0xFF00F2FE,
        subtextColor = 0xFF9B51E0,
        accentColor = 0xFF00F2FE,
        enterButtonColor = 0xFF9B51E0,
        toolbarColor = 0xFF0A0D18,
        borderColor = 0xFF00F2FE,
        borderAlpha = 0.85f,
        cornerRadius = 10f,
        keyStyle = "neon",
        category = "twenty_premium"
    )

    // 3. Gradient Dream — vibrant pink, purple, and blue gradients
    val GRADIENT_DREAM = KeyboardTheme(
        id = "gradient_dream",
        nameAr = "حلم التدرج",
        nameEn = "Gradient Dream",
        backgroundColor = 0xFF2A0845,
        backgroundGradient = listOf(0xFFFF758C, 0xFF764BA2, 0xFF6B8DD6),
        keyBackgroundColor = 0x40FFFFFF,
        keyPressedColor = 0x60FFFFFF,
        keyTextColor = 0xFFFFFFFF,
        subtextColor = 0xFFFFD1DC,
        accentColor = 0xFFFF758C,
        enterButtonColor = 0xFF764BA2,
        toolbarColor = 0x502A0845,
        borderColor = 0x60FFFFFF,
        borderAlpha = 0.7f,
        cornerRadius = 12f,
        keyStyle = "glass",
        isGlass = true,
        category = "twenty_premium"
    )

    // 4. Black & Red — premium black surfaces with red accents
    val BLACK_AND_RED = KeyboardTheme(
        id = "black_and_red",
        nameAr = "أسود وأحمر فاخر",
        nameEn = "Black & Red",
        backgroundColor = 0xFF0A0A0A,
        keyBackgroundColor = 0xFF181818,
        keyPressedColor = 0xFF2D1517,
        keyTextColor = 0xFFFFFFFF,
        subtextColor = 0xFFFF4D4D,
        accentColor = 0xFFFF1E27,
        enterButtonColor = 0xFFD50000,
        toolbarColor = 0xFF0F0F0F,
        borderColor = 0xFFFF1E27,
        borderAlpha = 0.75f,
        cornerRadius = 8f,
        keyStyle = "neon",
        category = "twenty_premium"
    )

    // 5. Light Mode — clean white and light-gray keys
    val LIGHT_MODE = KeyboardTheme(
        id = "light_mode",
        nameAr = "الوضع الفاتح المريح",
        nameEn = "Light Mode",
        backgroundColor = 0xFFFFFFFF,
        keyBackgroundColor = 0xFFF0F4F8,
        keyPressedColor = 0xFFE2E8F0,
        keyTextColor = 0xFF1F1F1F,
        subtextColor = 0xFF64748B,
        accentColor = 0xFF2563EB,
        enterButtonColor = 0xFF3B82F6,
        toolbarColor = 0xFFF8FAFC,
        borderColor = 0xFFCBD5E1,
        borderAlpha = 0.5f,
        cornerRadius = 8f,
        keyStyle = "rounded",
        category = "twenty_premium"
    )

    // 6. Anime Vibes — anime-inspired artwork and colorful accents
    val ANIME_VIBES = KeyboardTheme(
        id = "anime_vibes",
        nameAr = "أنمي فايبز",
        nameEn = "Anime Vibes",
        backgroundColor = 0xFF150B24,
        keyBackgroundColor = 0xFF26143F,
        keyPressedColor = 0xFF3B1E61,
        keyTextColor = 0xFFF8FAFC,
        subtextColor = 0xFFFF80AB,
        accentColor = 0xFFFF4081,
        enterButtonColor = 0xFFE040FB,
        toolbarColor = 0xFF1D0E32,
        borderColor = 0xFFFF80AB,
        borderAlpha = 0.7f,
        cornerRadius = 10f,
        category = "twenty_premium"
    )

    // 7. Purple Galaxy — cosmic purple and blue effects
    val PURPLE_GALAXY = KeyboardTheme(
        id = "purple_galaxy",
        nameAr = "مجرة بنفسجية",
        nameEn = "Purple Galaxy",
        backgroundColor = 0xFF0D031A,
        backgroundGradient = listOf(0xFF1F0538, 0xFF4A0E4E, 0xFF00E5FF),
        keyBackgroundColor = 0xFF1D0C33,
        keyPressedColor = 0xFF321456,
        keyTextColor = 0xFFE1BEE7,
        subtextColor = 0xFF00E5FF,
        accentColor = 0xFF00E5FF,
        enterButtonColor = 0xFF8E24AA,
        toolbarColor = 0xFF130624,
        borderColor = 0xFFBA68C8,
        borderAlpha = 0.65f,
        cornerRadius = 10f,
        category = "twenty_premium"
    )

    // 8. Green Matrix — black surfaces with green digital effects
    val GREEN_MATRIX = KeyboardTheme(
        id = "green_matrix",
        nameAr = "ماتريكس سايبر أخضر",
        nameEn = "Green Matrix",
        backgroundColor = 0xFF000A03,
        keyBackgroundColor = 0xFF001F08,
        keyPressedColor = 0xFF003810,
        keyTextColor = 0xFF00FF66,
        subtextColor = 0xFF00CC55,
        accentColor = 0xFF00FF66,
        enterButtonColor = 0xFF009933,
        toolbarColor = 0xFF001205,
        borderColor = 0xFF00FF66,
        borderAlpha = 0.8f,
        cornerRadius = 8f,
        keyStyle = "neon",
        category = "twenty_premium"
    )

    // 9. Sunset — warm orange, red, and purple gradients
    val SUNSET = KeyboardTheme(
        id = "sunset",
        nameAr = "غروب دافئ",
        nameEn = "Sunset",
        backgroundColor = 0xFF1C0A05,
        backgroundGradient = listOf(0xFFFF512F, 0xFFDD2476, 0xFFF09819),
        keyBackgroundColor = 0x50331005,
        keyPressedColor = 0x7055180A,
        keyTextColor = 0xFFFFE0B2,
        subtextColor = 0xFFFFB74D,
        accentColor = 0xFFFF9800,
        enterButtonColor = 0xFFE65100,
        toolbarColor = 0x501C0A05,
        borderColor = 0xFFFF9800,
        borderAlpha = 0.6f,
        cornerRadius = 10f,
        category = "twenty_premium"
    )

    // 10. Ocean Blue — blue aquatic textures and cool highlights
    val OCEAN_BLUE = KeyboardTheme(
        id = "ocean_blue",
        nameAr = "أزرق المحيط العميق",
        nameEn = "Ocean Blue",
        backgroundColor = 0xFF021024,
        backgroundGradient = listOf(0xFF0052D4, 0xFF4364F7, 0xFF6FB1FC),
        keyBackgroundColor = 0xFF05264E,
        keyPressedColor = 0xFF0C3D7A,
        keyTextColor = 0xFFE0F2FE,
        subtextColor = 0xFF7DD3FC,
        accentColor = 0xFF38BDF8,
        enterButtonColor = 0xFF0284C7,
        toolbarColor = 0xFF031938,
        borderColor = 0xFF38BDF8,
        borderAlpha = 0.6f,
        cornerRadius = 10f,
        category = "twenty_premium"
    )

    // 11. Neon Rainbow — multicolor illuminated key borders
    val NEON_RAINBOW = KeyboardTheme(
        id = "neon_rainbow",
        nameAr = "نيون رينبو الملون",
        nameEn = "Neon Rainbow",
        backgroundColor = 0xFF080810,
        keyBackgroundColor = 0xFF141424,
        keyPressedColor = 0xFF22223D,
        keyTextColor = 0xFFFFFFFF,
        subtextColor = 0xFFFFEE58,
        accentColor = 0xFFFF007F,
        enterButtonColor = 0xFF00F0FF,
        toolbarColor = 0xFF0C0C18,
        borderColor = 0xFFFF007F,
        borderAlpha = 0.9f,
        cornerRadius = 12f,
        keyStyle = "neon",
        category = "twenty_premium"
    )

    // 12. Sakura — soft pink Japanese cherry blossom aesthetic
    val SAKURA = KeyboardTheme(
        id = "sakura",
        nameAr = "ساكورا اليابانية",
        nameEn = "Sakura",
        backgroundColor = 0xFFFFF0F3,
        backgroundGradient = listOf(0xFFFFEBF0, 0xFFFF8DA1),
        keyBackgroundColor = 0xFFFFFFFF,
        keyPressedColor = 0xFFFFD6E0,
        keyTextColor = 0xFF4A1525,
        subtextColor = 0xFFFF5C8A,
        accentColor = 0xFFFF4081,
        enterButtonColor = 0xFFFF5C8A,
        toolbarColor = 0xFFFFE4E9,
        borderColor = 0xFFFFB3C1,
        borderAlpha = 0.7f,
        cornerRadius = 12f,
        category = "twenty_premium"
    )

    // 13. Gold Luxury — elegant black and metallic gold
    val GOLD_LUXURY_THEME = KeyboardTheme(
        id = "gold_luxury_theme",
        nameAr = "ذهب فاخر ملوكي",
        nameEn = "Gold Luxury",
        backgroundColor = 0xFF0B0A08,
        keyBackgroundColor = 0xFF191712,
        keyPressedColor = 0xFF2B261B,
        keyTextColor = 0xFFFFE082,
        subtextColor = 0xFFD4AF37,
        accentColor = 0xFFFFD700,
        enterButtonColor = 0xFFC5A028,
        toolbarColor = 0xFF110E09,
        borderColor = 0xFFD4AF37,
        borderAlpha = 0.85f,
        cornerRadius = 10f,
        keyStyle = "gold",
        category = "twenty_premium"
    )

    // 14. Blue Flame — electric blue flame-inspired effects
    val BLUE_FLAME = KeyboardTheme(
        id = "blue_flame",
        nameAr = "لهب أزرق متوهج",
        nameEn = "Blue Flame",
        backgroundColor = 0xFF040A14,
        keyBackgroundColor = 0xFF0B192C,
        keyPressedColor = 0xFF152E4F,
        keyTextColor = 0xFFE0F7FA,
        subtextColor = 0xFF00E5FF,
        accentColor = 0xFF00D2FF,
        enterButtonColor = 0xFF0091EA,
        toolbarColor = 0xFF061120,
        borderColor = 0xFF00E5FF,
        borderAlpha = 0.85f,
        cornerRadius = 10f,
        keyStyle = "neon",
        category = "twenty_premium"
    )

    // 15. Brown Wood — natural brown wood textures
    val BROWN_WOOD = KeyboardTheme(
        id = "brown_wood",
        nameAr = "خشب بني طبيعي",
        nameEn = "Brown Wood",
        backgroundColor = 0xFF1D130E,
        keyBackgroundColor = 0xFF332018,
        keyPressedColor = 0xFF4D3025,
        keyTextColor = 0xFFF5EBE6,
        subtextColor = 0xFFD7A15C,
        accentColor = 0xFFD7A15C,
        enterButtonColor = 0xFFA0522D,
        toolbarColor = 0xFF241812,
        borderColor = 0xFF8B5A2B,
        borderAlpha = 0.7f,
        cornerRadius = 8f,
        category = "twenty_premium"
    )

    // 16. Neon Tech — futuristic cyan and magenta cyber styling
    val NEON_TECH = KeyboardTheme(
        id = "neon_tech",
        nameAr = "سايبر تك المستقبلي",
        nameEn = "Neon Tech",
        backgroundColor = 0xFF050814,
        keyBackgroundColor = 0xFF0E1428,
        keyPressedColor = 0xFF192242,
        keyTextColor = 0xFF00F5D4,
        subtextColor = 0xFFFF007F,
        accentColor = 0xFF00F5D4,
        enterButtonColor = 0xFFFF007F,
        toolbarColor = 0xFF080D20,
        borderColor = 0xFF00F5D4,
        borderAlpha = 0.85f,
        cornerRadius = 10f,
        keyStyle = "neon",
        category = "twenty_premium"
    )

    // 17. Cute Kawaii — pastel pink, adorable icons, and soft keys
    val CUTE_KAWAII = KeyboardTheme(
        id = "cute_kawaii",
        nameAr = "كيوت كاواي",
        nameEn = "Cute Kawaii",
        backgroundColor = 0xFFFFF5F7,
        keyBackgroundColor = 0xFFFFE3E8,
        keyPressedColor = 0xFFFFCCD5,
        keyTextColor = 0xFF5C2D3B,
        subtextColor = 0xFFFF69B4,
        accentColor = 0xFFFF69B4,
        enterButtonColor = 0xFFFF4081,
        toolbarColor = 0xFFFFECF0,
        borderColor = 0xFFFFB6C1,
        borderAlpha = 0.8f,
        cornerRadius = 14f,
        keyStyle = "bubble",
        category = "twenty_premium"
    )

    // 18. Midnight — deep navy, moonlight, and subtle blue highlights
    val MIDNIGHT = KeyboardTheme(
        id = "midnight",
        nameAr = "منتصف الليل القمري",
        nameEn = "Midnight",
        backgroundColor = 0xFF060913,
        keyBackgroundColor = 0xFF111728,
        keyPressedColor = 0xFF1E2840,
        keyTextColor = 0xFFF1F5F9,
        subtextColor = 0xFF60A5FA,
        accentColor = 0xFF38BDF8,
        enterButtonColor = 0xFF1D4ED8,
        toolbarColor = 0xFF0B101E,
        borderColor = 0xFF1E293B,
        borderAlpha = 0.6f,
        cornerRadius = 9f,
        category = "twenty_premium"
    )

    // 19. Fire & Ice — contrasting fiery orange and icy blue accents
    val FIRE_AND_ICE = KeyboardTheme(
        id = "fire_and_ice",
        nameAr = "نار وجليد أسطوري",
        nameEn = "Fire & Ice",
        backgroundColor = 0xFF070B14,
        keyBackgroundColor = 0xFF121A2A,
        keyPressedColor = 0xFF202E48,
        keyTextColor = 0xFFFFFFFF,
        subtextColor = 0xFFFF6B00,
        accentColor = 0xFF00F5FF,
        enterButtonColor = 0xFFFF5722,
        toolbarColor = 0xFF0A101C,
        borderColor = 0xFF00F5FF,
        borderAlpha = 0.85f,
        cornerRadius = 10f,
        keyStyle = "neon",
        category = "twenty_premium"
    )

    // 20. Minimal Dark — modern charcoal keys with subtle highlights
    val MINIMAL_DARK = KeyboardTheme(
        id = "minimal_dark",
        nameAr = "مينيمال داكن",
        nameEn = "Minimal Dark",
        backgroundColor = 0xFF121212,
        keyBackgroundColor = 0xFF1E1E1E,
        keyPressedColor = 0xFF2A2A2A,
        keyTextColor = 0xFFFFFFFF,
        subtextColor = 0xFF9E9E9E,
        accentColor = 0xFFFF7043,
        enterButtonColor = 0xFFE64A19,
        toolbarColor = 0xFF181818,
        borderColor = 0xFF333333,
        cornerRadius = 8f,
        keyStyle = "rounded",
        category = "twenty_premium"
    )

    // List of featured premium themes with Crystal Light Royal at the top
    val twentyPremiumThemes: List<KeyboardTheme> = listOf(
        CRYSTAL_LIGHT,
        FACEMOJI_CLASSIC,
        DARK_NEON,
        GRADIENT_DREAM,
        BLACK_AND_RED,
        LIGHT_MODE,
        ANIME_VIBES,
        PURPLE_GALAXY,
        GREEN_MATRIX,
        SUNSET,
        OCEAN_BLUE,
        NEON_RAINBOW,
        SAKURA,
        GOLD_LUXURY_THEME,
        BLUE_FLAME,
        BROWN_WOOD,
        NEON_TECH,
        CUTE_KAWAII,
        MIDNIGHT,
        FIRE_AND_ICE,
        MINIMAL_DARK
    )

    // =========================================================================
    // ANIME, FANTASY & NEW PASTEL LIGHT THEMES (From User Request Images)
    // =========================================================================

    val SHADOW_NINJA = KeyboardTheme(
        id = "shadow_ninja",
        nameAr = "شادو نينجا (Shadow Ninja)",
        nameEn = "Shadow Ninja",
        backgroundColor = 0xFF0A0406,
        keyBackgroundColor = 0xFF19090D,
        keyPressedColor = 0xFF2D1016,
        keyTextColor = 0xFFFF4D4D,
        subtextColor = 0xFF8E3B45,
        accentColor = 0xFFFF1744,
        enterButtonColor = 0xFFD50000,
        toolbarColor = 0xFF0F0609,
        borderColor = 0xFFFF1744,
        borderAlpha = 0.75f,
        cornerRadius = 9f,
        keyStyle = "neon",
        category = "gaming"
    )

    val SAKURA_DREAM = KeyboardTheme(
        id = "sakura_dream",
        nameAr = "ساكورا دريم (Sakura Dream)",
        nameEn = "Sakura Dream",
        backgroundColor = 0xFFFFF1F5,
        keyBackgroundColor = 0xFFFFFFFF,
        keyPressedColor = 0xFFFFE4EC,
        keyTextColor = 0xFF881337,
        subtextColor = 0xFFFB7185,
        accentColor = 0xFFF43F5E,
        enterButtonColor = 0xFFE11D48,
        toolbarColor = 0xFFFFE8EF,
        borderColor = 0xFFFECDD3,
        borderAlpha = 0.8f,
        cornerRadius = 10f,
        keyStyle = "glass_light",
        category = "colorful"
    )

    val BLUE_DRAGON = KeyboardTheme(
        id = "blue_dragon",
        nameAr = "التنين الأزرق (Blue Dragon)",
        nameEn = "Blue Dragon",
        backgroundColor = 0xFF030B17,
        keyBackgroundColor = 0xFF081C36,
        keyPressedColor = 0xFF0E305C,
        keyTextColor = 0xFF38BDF8,
        subtextColor = 0xFF0284C7,
        accentColor = 0xFF00E5FF,
        enterButtonColor = 0xFF0284C7,
        toolbarColor = 0xFF061426,
        borderColor = 0xFF00E5FF,
        borderAlpha = 0.85f,
        cornerRadius = 9f,
        keyStyle = "neon",
        category = "gaming"
    )

    val KAKASHI_SHINOBI = KeyboardTheme(
        id = "kakashi_shinobi",
        nameAr = "كاكاشي شينوبي (Kakashi)",
        nameEn = "Kakashi Shinobi",
        backgroundColor = 0xFF0B0F14,
        keyBackgroundColor = 0xFF1A222C,
        keyPressedColor = 0xFF273545,
        keyTextColor = 0xFFE2E8F0,
        subtextColor = 0xFF64748B,
        accentColor = 0xFF38BDF8,
        enterButtonColor = 0xFF1E3A8A,
        toolbarColor = 0xFF0F151C,
        borderColor = 0xFF38BDF8,
        borderAlpha = 0.65f,
        cornerRadius = 9f,
        keyStyle = "rounded",
        category = "gaming"
    )

    val NEON_CITY = KeyboardTheme(
        id = "neon_city",
        nameAr = "نيون سيتي (Neon City)",
        nameEn = "Neon City",
        backgroundColor = 0xFF090014,
        keyBackgroundColor = 0xFF16032B,
        keyPressedColor = 0xFF2B0754,
        keyTextColor = 0xFFFF007F,
        subtextColor = 0xFF00F5FF,
        accentColor = 0xFFFF007F,
        enterButtonColor = 0xFF9D00FF,
        toolbarColor = 0xFF0D021D,
        borderColor = 0xFF00F5FF,
        borderAlpha = 0.85f,
        cornerRadius = 10f,
        keyStyle = "neon",
        category = "neon"
    )

    val NARUTO_CHAKRA = KeyboardTheme(
        id = "naruto_chakra",
        nameAr = "ناروتو شقرا (Naruto Chakra)",
        nameEn = "Naruto Chakra",
        backgroundColor = 0xFF150A00,
        keyBackgroundColor = 0xFF2A1400,
        keyPressedColor = 0xFF472200,
        keyTextColor = 0xFFFFB300,
        subtextColor = 0xFFFF6D00,
        accentColor = 0xFFFF9100,
        enterButtonColor = 0xFFFF6D00,
        toolbarColor = 0xFF1E0E00,
        borderColor = 0xFFFF9100,
        borderAlpha = 0.85f,
        cornerRadius = 9f,
        keyStyle = "neon",
        category = "gaming"
    )

    val GOKU_SAIYAN = KeyboardTheme(
        id = "goku_saiyan",
        nameAr = "غوكو سايان (Goku Super Saiyan)",
        nameEn = "Goku Super Saiyan",
        backgroundColor = 0xFF0A0700,
        keyBackgroundColor = 0xFF1F1500,
        keyPressedColor = 0xFF382700,
        keyTextColor = 0xFFFFD700,
        subtextColor = 0xFFFF9900,
        accentColor = 0xFFFFD700,
        enterButtonColor = 0xFFFF6F00,
        toolbarColor = 0xFF140F00,
        borderColor = 0xFFFFD700,
        borderAlpha = 0.85f,
        cornerRadius = 10f,
        keyStyle = "neon",
        category = "gaming"
    )

    val BLACK_GOLD_LUXURY = KeyboardTheme(
        id = "black_gold_luxury",
        nameAr = "بلاك & جولد الملكي (Black & Gold)",
        nameEn = "Black & Gold Luxury",
        backgroundColor = 0xFF080808,
        keyBackgroundColor = 0xFF18150D,
        keyPressedColor = 0xFF2A2414,
        keyTextColor = 0xFFFFDF00,
        subtextColor = 0xFFD4AF37,
        accentColor = 0xFFFFD700,
        enterButtonColor = 0xFFD4AF37,
        toolbarColor = 0xFF0F0E0A,
        borderColor = 0xFFD4AF37,
        borderAlpha = 0.8f,
        cornerRadius = 10f,
        keyStyle = "gold",
        category = "elegant"
    )

    val PURPLE_BUTTERFLY = KeyboardTheme(
        id = "purple_butterfly",
        nameAr = "فراشة بنفسجية (Purple Butterfly)",
        nameEn = "Purple Butterfly",
        backgroundColor = 0xFF0D031A,
        keyBackgroundColor = 0xFF1F0B38,
        keyPressedColor = 0xFF35125F,
        keyTextColor = 0xFFE9D5FF,
        subtextColor = 0xFFC084FC,
        accentColor = 0xFFA855F7,
        enterButtonColor = 0xFF9333EA,
        toolbarColor = 0xFF130626,
        borderColor = 0xFFA855F7,
        borderAlpha = 0.75f,
        cornerRadius = 10f,
        keyStyle = "neon",
        category = "colorful"
    )

    val FIRE_PHOENIX = KeyboardTheme(
        id = "fire_phoenix",
        nameAr = "العنقاء النارية (Fire Phoenix)",
        nameEn = "Fire Phoenix",
        backgroundColor = 0xFF120303,
        keyBackgroundColor = 0xFF270909,
        keyPressedColor = 0xFF421010,
        keyTextColor = 0xFFFF7043,
        subtextColor = 0xFFFFAB91,
        accentColor = 0xFFFF3D00,
        enterButtonColor = 0xFFDD2C00,
        toolbarColor = 0xFF1A0505,
        borderColor = 0xFFFF3D00,
        borderAlpha = 0.8f,
        cornerRadius = 9f,
        keyStyle = "neon",
        category = "gaming"
    )

    val ICE_QUEEN = KeyboardTheme(
        id = "ice_queen",
        nameAr = "ملكة الجليد (Ice Queen)",
        nameEn = "Ice Queen",
        backgroundColor = 0xFFF0F9FF,
        keyBackgroundColor = 0xFFFFFFFF,
        keyPressedColor = 0xFFE0F2FE,
        keyTextColor = 0xFF0369A1,
        subtextColor = 0xFF38BDF8,
        accentColor = 0xFF0EA5E9,
        enterButtonColor = 0xFF0284C7,
        toolbarColor = 0xFFE0F2FE,
        borderColor = 0xFFBAE6FD,
        borderAlpha = 0.85f,
        cornerRadius = 10f,
        keyStyle = "glass_light",
        category = "dark_light"
    )

    val PASTEL_PEACH = KeyboardTheme(
        id = "pastel_peach",
        nameAr = "خوخي ناعم (Pastel Peach)",
        nameEn = "Pastel Peach",
        backgroundColor = 0xFFFFF7ED,
        keyBackgroundColor = 0xFFFFFFFF,
        keyPressedColor = 0xFFFFEDD5,
        keyTextColor = 0xFF9A3412,
        subtextColor = 0xFFFB923C,
        accentColor = 0xFFF97316,
        enterButtonColor = 0xFFEA580C,
        toolbarColor = 0xFFFFEDE0,
        borderColor = 0xFFFED7AA,
        borderAlpha = 0.8f,
        cornerRadius = 10f,
        keyStyle = "glass_light",
        category = "colorful"
    )

    val PASTEL_MINT = KeyboardTheme(
        id = "pastel_mint",
        nameAr = "نعناعي هادئ (Pastel Mint)",
        nameEn = "Pastel Mint",
        backgroundColor = 0xFFF0FDF4,
        keyBackgroundColor = 0xFFFFFFFF,
        keyPressedColor = 0xFFDCFCE7,
        keyTextColor = 0xFF166534,
        subtextColor = 0xFF4ADE80,
        accentColor = 0xFF22C55E,
        enterButtonColor = 0xFF16A34A,
        toolbarColor = 0xFFE2FBE9,
        borderColor = 0xFFBBF7D0,
        borderAlpha = 0.8f,
        cornerRadius = 10f,
        keyStyle = "glass_light",
        category = "colorful"
    )

    val PASTEL_LAVENDER = KeyboardTheme(
        id = "pastel_lavender",
        nameAr = "لافندر فاتح (Pastel Lavender)",
        nameEn = "Pastel Lavender",
        backgroundColor = 0xFFFAF5FF,
        keyBackgroundColor = 0xFFFFFFFF,
        keyPressedColor = 0xFFF3E8FF,
        keyTextColor = 0xFF581C87,
        subtextColor = 0xFFA855F7,
        accentColor = 0xFF9333EA,
        enterButtonColor = 0xFF7E22CE,
        toolbarColor = 0xFFF3E8FF,
        borderColor = 0xFFE9D5FF,
        borderAlpha = 0.8f,
        cornerRadius = 10f,
        keyStyle = "glass_light",
        category = "colorful"
    )

    // =========================================================================
    // 20 MODERN LIGHT THEMES SUITE (20 ستايل فاتح للكيبورد - من صورة الطلب)
    // =========================================================================

    // 1. Light Blue 💧
    val LIGHT_BLUE_SUITE = KeyboardTheme(
        id = "light_blue_suite",
        nameAr = "لايت بلو 💧",
        nameEn = "Light Blue",
        backgroundColor = 0xFFE0F2FE,
        keyBackgroundColor = 0xFFFFFFFF,
        keyPressedColor = 0xFFBAE6FD,
        keyTextColor = 0xFF0369A1,
        subtextColor = 0xFF0284C7,
        accentColor = 0xFF0284C7,
        enterButtonColor = 0xFF0284C7,
        toolbarColor = 0xFFD0ECFD,
        borderColor = 0xFFBAE6FD,
        borderAlpha = 0.85f,
        cornerRadius = 10f,
        keyStyle = "glass_light",
        category = "twenty_light"
    )

    // 2. Pink Dream 💖
    val PINK_DREAM_SUITE = KeyboardTheme(
        id = "pink_dream_suite",
        nameAr = "بينك دريم 💖",
        nameEn = "Pink Dream",
        backgroundColor = 0xFFFCE7F3,
        keyBackgroundColor = 0xFFFFFFFF,
        keyPressedColor = 0xFFFBCFE8,
        keyTextColor = 0xFF831843,
        subtextColor = 0xFFDB2777,
        accentColor = 0xFFEC4899,
        enterButtonColor = 0xFFDB2777,
        toolbarColor = 0xFFF9D6EA,
        borderColor = 0xFFFBCFE8,
        borderAlpha = 0.85f,
        cornerRadius = 10f,
        keyStyle = "glass_light",
        category = "twenty_light"
    )

    // 3. Mint Green 🍃
    val MINT_GREEN_SUITE = KeyboardTheme(
        id = "mint_green_suite",
        nameAr = "مينت جرين 🍃",
        nameEn = "Mint Green",
        backgroundColor = 0xFFDCFCE7,
        keyBackgroundColor = 0xFFFFFFFF,
        keyPressedColor = 0xFFBBF7D0,
        keyTextColor = 0xFF065F46,
        subtextColor = 0xFF059669,
        accentColor = 0xFF10B981,
        enterButtonColor = 0xFF059669,
        toolbarColor = 0xFFCEFAD9,
        borderColor = 0xFFBBF7D0,
        borderAlpha = 0.85f,
        cornerRadius = 10f,
        keyStyle = "glass_light",
        category = "twenty_light"
    )

    // 4. Lavender 🌸
    val LAVENDER_SUITE = KeyboardTheme(
        id = "lavender_suite",
        nameAr = "لافندر 🌸",
        nameEn = "Lavender",
        backgroundColor = 0xFFEDE9FE,
        keyBackgroundColor = 0xFFFFFFFF,
        keyPressedColor = 0xFFDDD6FE,
        keyTextColor = 0xFF4C1D95,
        subtextColor = 0xFF7C3AED,
        accentColor = 0xFF8B5CF6,
        enterButtonColor = 0xFF7C3AED,
        toolbarColor = 0xFFDFD7FE,
        borderColor = 0xFFDDD6FE,
        borderAlpha = 0.85f,
        cornerRadius = 10f,
        keyStyle = "glass_light",
        category = "twenty_light"
    )

    // 5. Sunset Light 🌅
    val SUNSET_LIGHT_SUITE = KeyboardTheme(
        id = "sunset_light_suite",
        nameAr = "سانست لايت 🌅",
        nameEn = "Sunset Light",
        backgroundColor = 0xFFFFEDD5,
        keyBackgroundColor = 0xFFFFFFFF,
        keyPressedColor = 0xFFFED7AA,
        keyTextColor = 0xFF7C2D12,
        subtextColor = 0xFFEA580C,
        accentColor = 0xFFF97316,
        enterButtonColor = 0xFFEA580C,
        toolbarColor = 0xFFFDE3C4,
        borderColor = 0xFFFED7AA,
        borderAlpha = 0.85f,
        cornerRadius = 10f,
        keyStyle = "glass_light",
        category = "twenty_light"
    )

    // 6. Sky White ☁️
    val SKY_WHITE_SUITE = KeyboardTheme(
        id = "sky_white_suite",
        nameAr = "سكاي وايت ☁️",
        nameEn = "Sky White",
        backgroundColor = 0xFFF8FAFC,
        keyBackgroundColor = 0xFFFFFFFF,
        keyPressedColor = 0xFFE2E8F0,
        keyTextColor = 0xFF0F172A,
        subtextColor = 0xFF0284C7,
        accentColor = 0xFF0EA5E9,
        enterButtonColor = 0xFF0284C7,
        toolbarColor = 0xFFEDF2F7,
        borderColor = 0xFFCBD5E1,
        borderAlpha = 0.85f,
        cornerRadius = 10f,
        keyStyle = "glass_light",
        category = "twenty_light"
    )

    // 7. Forest Light 🌿
    val FOREST_LIGHT_SUITE = KeyboardTheme(
        id = "forest_light_suite",
        nameAr = "فورست لايت 🌿",
        nameEn = "Forest Light",
        backgroundColor = 0xFFE8F5E9,
        keyBackgroundColor = 0xFFFFFFFF,
        keyPressedColor = 0xFFC8E6C9,
        keyTextColor = 0xFF1B5E20,
        subtextColor = 0xFF2E7D32,
        accentColor = 0xFF2E7D32,
        enterButtonColor = 0xFF2E7D32,
        toolbarColor = 0xFFD7EED9,
        borderColor = 0xFFC8E6C9,
        borderAlpha = 0.85f,
        cornerRadius = 10f,
        keyStyle = "glass_light",
        category = "twenty_light"
    )

    // 8. Gold Touch 👑
    val GOLD_TOUCH_SUITE = KeyboardTheme(
        id = "gold_touch_suite",
        nameAr = "جولد تاتش 👑",
        nameEn = "Gold Touch",
        backgroundColor = 0xFFFEF9C3,
        keyBackgroundColor = 0xFFFFFFFF,
        keyPressedColor = 0xFFFEF08A,
        keyTextColor = 0xFF78350F,
        subtextColor = 0xFFB45309,
        accentColor = 0xFFD97706,
        enterButtonColor = 0xFFD97706,
        toolbarColor = 0xFFFCF4AA,
        borderColor = 0xFFFEF08A,
        borderAlpha = 0.85f,
        cornerRadius = 10f,
        keyStyle = "glass_light",
        category = "twenty_light"
    )

    // 9. Ocean Wave 🌊
    val OCEAN_WAVE_SUITE = KeyboardTheme(
        id = "ocean_wave_suite",
        nameAr = "أوشن ويف 🌊",
        nameEn = "Ocean Wave",
        backgroundColor = 0xFFE0F7FA,
        keyBackgroundColor = 0xFFFFFFFF,
        keyPressedColor = 0xFFB2EBF2,
        keyTextColor = 0xFF006064,
        subtextColor = 0xFF00838F,
        accentColor = 0xFF00ACC1,
        enterButtonColor = 0xFF00838F,
        toolbarColor = 0xFFCDEFF4,
        borderColor = 0xFFB2EBF2,
        borderAlpha = 0.85f,
        cornerRadius = 10f,
        keyStyle = "glass_light",
        category = "twenty_light"
    )

    // 10. Sakura 🌸
    val SAKURA_SUITE = KeyboardTheme(
        id = "sakura_suite",
        nameAr = "ساكورا 🌸",
        nameEn = "Sakura",
        backgroundColor = 0xFFFCE4EC,
        keyBackgroundColor = 0xFFFFFFFF,
        keyPressedColor = 0xFFF8BBD0,
        keyTextColor = 0xFF880E4F,
        subtextColor = 0xFFC2185B,
        accentColor = 0xFFE91E63,
        enterButtonColor = 0xFFD81B60,
        toolbarColor = 0xFFF8D3DF,
        borderColor = 0xFFF8BBD0,
        borderAlpha = 0.85f,
        cornerRadius = 10f,
        keyStyle = "glass_light",
        category = "twenty_light"
    )

    // 11. Purple Light ⭐
    val PURPLE_LIGHT_SUITE = KeyboardTheme(
        id = "purple_light_suite",
        nameAr = "بيربل لايت ⭐",
        nameEn = "Purple Light",
        backgroundColor = 0xFFF3E8FF,
        keyBackgroundColor = 0xFFFFFFFF,
        keyPressedColor = 0xFFE9D5FF,
        keyTextColor = 0xFF3B0764,
        subtextColor = 0xFF6B21A8,
        accentColor = 0xFF7C3AED,
        enterButtonColor = 0xFF6B21A8,
        toolbarColor = 0xFFEBD4FD,
        borderColor = 0xFFE9D5FF,
        borderAlpha = 0.85f,
        cornerRadius = 10f,
        keyStyle = "glass_light",
        category = "twenty_light"
    )

    // 12. Sky Sunset 🌅
    val SKY_SUNSET_SUITE = KeyboardTheme(
        id = "sky_sunset_suite",
        nameAr = "سكاي سانست 🌅",
        nameEn = "Sky Sunset",
        backgroundColor = 0xFFEEF2FF,
        backgroundGradient = listOf(0xFFE0E7FF, 0xFFFCE7F3),
        keyBackgroundColor = 0xFFFFFFFF,
        keyPressedColor = 0xFFE0E7FF,
        keyTextColor = 0xFF1E1B4B,
        subtextColor = 0xFF4338CA,
        accentColor = 0xFF6366F1,
        enterButtonColor = 0xFF4F46E5,
        toolbarColor = 0xFFE5E7FD,
        borderColor = 0xFFC7D2FE,
        borderAlpha = 0.85f,
        cornerRadius = 10f,
        keyStyle = "glass_light",
        category = "twenty_light"
    )

    // 13. Neon White ✨
    val NEON_WHITE_SUITE = KeyboardTheme(
        id = "neon_white_suite",
        nameAr = "نيون وايت ✨",
        nameEn = "Neon White",
        backgroundColor = 0xFFFFFFFF,
        keyBackgroundColor = 0xFFF8FAFC,
        keyPressedColor = 0xFFE2E8F0,
        keyTextColor = 0xFF000000,
        subtextColor = 0xFF0284C7,
        accentColor = 0xFF00B0FF,
        enterButtonColor = 0xFF0091EA,
        toolbarColor = 0xFFF1F5F9,
        borderColor = 0xFF00E5FF,
        borderAlpha = 0.9f,
        cornerRadius = 10f,
        keyStyle = "neon",
        category = "twenty_light"
    )

    // 14. Beige Minimal 🌾
    val BEIGE_MINIMAL_SUITE = KeyboardTheme(
        id = "beige_minimal_suite",
        nameAr = "بيج مينيمال 🌾",
        nameEn = "Beige Minimal",
        backgroundColor = 0xFFF5F5DC,
        keyBackgroundColor = 0xFFFFFDF5,
        keyPressedColor = 0xFFEFE8D8,
        keyTextColor = 0xFF3D2B1F,
        subtextColor = 0xFF785E4F,
        accentColor = 0xFFA07855,
        enterButtonColor = 0xFF8C6240,
        toolbarColor = 0xFFECE7D5,
        borderColor = 0xFFE0D8C3,
        borderAlpha = 0.85f,
        cornerRadius = 10f,
        keyStyle = "glass_light",
        category = "twenty_light"
    )

    // 15. Aqua Glow ✨
    val AQUA_GLOW_SUITE = KeyboardTheme(
        id = "aqua_glow_suite",
        nameAr = "أكوا جلو ✨",
        nameEn = "Aqua Glow",
        backgroundColor = 0xFF004D40,
        backgroundGradient = listOf(0xFF004D40, 0xFF00796B),
        keyBackgroundColor = 0xFFE0F2F1,
        keyPressedColor = 0xFFB2DFDB,
        keyTextColor = 0xFF00332C,
        subtextColor = 0xFF004D40,
        accentColor = 0xFF00E5FF,
        enterButtonColor = 0xFF00BFA5,
        toolbarColor = 0xFF00382E,
        borderColor = 0xFF00E5FF,
        borderAlpha = 0.9f,
        cornerRadius = 10f,
        keyStyle = "neon",
        category = "twenty_light"
    )

    // 16. Rainbow Light 🌈
    val RAINBOW_LIGHT_SUITE = KeyboardTheme(
        id = "rainbow_light_suite",
        nameAr = "رينبو لايت 🌈",
        nameEn = "Rainbow Light",
        backgroundColor = 0xFFFFF5F5,
        backgroundGradient = listOf(0xFFFFF1F2, 0xFFFEF9C3, 0xFFE0F2FE),
        keyBackgroundColor = 0xFFFFFFFF,
        keyPressedColor = 0xFFFCE7F3,
        keyTextColor = 0xFF3B0764,
        subtextColor = 0xFF9333EA,
        accentColor = 0xFFA855F7,
        enterButtonColor = 0xFF9333EA,
        toolbarColor = 0xFFFDF0F8,
        borderColor = 0xFFFBCFE8,
        borderAlpha = 0.85f,
        cornerRadius = 10f,
        keyStyle = "glass_light",
        category = "twenty_light"
    )

    // 17. Cloudy Day ☁️
    val CLOUDY_DAY_SUITE = KeyboardTheme(
        id = "cloudy_day_suite",
        nameAr = "كلاودي داي ☁️",
        nameEn = "Cloudy Day",
        backgroundColor = 0xFFE2E8F0,
        keyBackgroundColor = 0xFFFFFFFF,
        keyPressedColor = 0xFFCBD5E1,
        keyTextColor = 0xFF1E293B,
        subtextColor = 0xFF475569,
        accentColor = 0xFF64748B,
        enterButtonColor = 0xFF475569,
        toolbarColor = 0xFFD8E0EA,
        borderColor = 0xFFCBD5E1,
        borderAlpha = 0.85f,
        cornerRadius = 10f,
        keyStyle = "glass_light",
        category = "twenty_light"
    )

    // 18. Rose Gold 💎
    val ROSE_GOLD_SUITE = KeyboardTheme(
        id = "rose_gold_suite",
        nameAr = "روز جولد 💎",
        nameEn = "Rose Gold",
        backgroundColor = 0xFFFFF0F5,
        keyBackgroundColor = 0xFFFFFFFF,
        keyPressedColor = 0xFFFFE4E1,
        keyTextColor = 0xFF5C2C35,
        subtextColor = 0xFF9C5160,
        accentColor = 0xFFB76E79,
        enterButtonColor = 0xFFA35763,
        toolbarColor = 0xFFFBE4EB,
        borderColor = 0xFFFFC0CB,
        borderAlpha = 0.85f,
        cornerRadius = 10f,
        keyStyle = "glass_light",
        category = "twenty_light"
    )

    // 19. Purple Blue ⭐
    val PURPLE_BLUE_SUITE = KeyboardTheme(
        id = "purple_blue_suite",
        nameAr = "بيربل بلو ⭐",
        nameEn = "Purple Blue",
        backgroundColor = 0xFFF1F5F9,
        backgroundGradient = listOf(0xFFEDE9FE, 0xFFDBEAFE),
        keyBackgroundColor = 0xFFFFFFFF,
        keyPressedColor = 0xFFE0E7FF,
        keyTextColor = 0xFF1E1B4B,
        subtextColor = 0xFF4338CA,
        accentColor = 0xFF4F46E5,
        enterButtonColor = 0xFF4338CA,
        toolbarColor = 0xFFE4ECFA,
        borderColor = 0xFFC7D2FE,
        borderAlpha = 0.85f,
        cornerRadius = 10f,
        keyStyle = "glass_light",
        category = "twenty_light"
    )

    // 20. Green Leaf 🍃
    val GREEN_LEAF_SUITE = KeyboardTheme(
        id = "green_leaf_suite",
        nameAr = "جرين ليف 🍃",
        nameEn = "Green Leaf",
        backgroundColor = 0xFFDCFCE7,
        keyBackgroundColor = 0xFFFFFFFF,
        keyPressedColor = 0xFFBBF7D0,
        keyTextColor = 0xFF14532D,
        subtextColor = 0xFF16A34A,
        accentColor = 0xFF22C55E,
        enterButtonColor = 0xFF16A34A,
        toolbarColor = 0xFFCFF6DC,
        borderColor = 0xFF86EFAC,
        borderAlpha = 0.85f,
        cornerRadius = 10f,
        keyStyle = "glass_light",
        category = "twenty_light"
    )

    val twentyLightThemes: List<KeyboardTheme> = listOf(
        LIGHT_BLUE_SUITE,
        PINK_DREAM_SUITE,
        MINT_GREEN_SUITE,
        LAVENDER_SUITE,
        SUNSET_LIGHT_SUITE,
        SKY_WHITE_SUITE,
        FOREST_LIGHT_SUITE,
        GOLD_TOUCH_SUITE,
        OCEAN_WAVE_SUITE,
        SAKURA_SUITE,
        PURPLE_LIGHT_SUITE,
        SKY_SUNSET_SUITE,
        NEON_WHITE_SUITE,
        BEIGE_MINIMAL_SUITE,
        AQUA_GLOW_SUITE,
        RAINBOW_LIGHT_SUITE,
        CLOUDY_DAY_SUITE,
        ROSE_GOLD_SUITE,
        PURPLE_BLUE_SUITE,
        GREEN_LEAF_SUITE
    )

    // 1. Dark & Light Themes

    val MINIMAL_LIGHT = KeyboardTheme(
        id = "minimal_light",
        nameAr = "مينيمال فاتح",
        nameEn = "Minimal Light",
        backgroundColor = 0xFFECEEEF,
        keyBackgroundColor = 0xFFFFFFFF,
        keyPressedColor = 0xFFDFE3E6,
        keyTextColor = 0xFF1A1A1A,
        subtextColor = 0xFF6B7280,
        accentColor = 0xFF2563EB,
        enterButtonColor = 0xFF3B82F6,
        toolbarColor = 0xFFE2E6E9,
        borderColor = 0xFFCBD5E1,
        cornerRadius = 8f,
        keyStyle = "rounded",
        category = "dark_light"
    )

    val MINIMALIST_WHITE = KeyboardTheme(
        id = "minimalist_white",
        nameAr = "أبيض ناصع",
        nameEn = "Minimalist White",
        backgroundColor = 0xFFF8FAFC,
        keyBackgroundColor = 0xFFFFFFFF,
        keyPressedColor = 0xFFE2E8F0,
        keyTextColor = 0xFF0F172A,
        subtextColor = 0xFF64748B,
        accentColor = 0xFF0EA5E9,
        enterButtonColor = 0xFF0284C7,
        toolbarColor = 0xFFF1F5F9,
        borderColor = 0xFFE2E8F0,
        cornerRadius = 10f,
        keyStyle = "rounded",
        category = "dark_light"
    )

    val DARK_MATTE = KeyboardTheme(
        id = "dark_matte",
        nameAr = "داكن مطفي",
        nameEn = "Dark Matte",
        backgroundColor = 0xFF0A0D12,
        keyBackgroundColor = 0xFF151B24,
        keyPressedColor = 0xFF222B38,
        keyTextColor = 0xFFF1F5F9,
        subtextColor = 0xFF8E9BAE,
        accentColor = 0xFF38BDF8,
        enterButtonColor = 0xFF0284C7,
        toolbarColor = 0xFF0E131A,
        borderColor = 0xFF222B38,
        cornerRadius = 10f,
        keyStyle = "rounded",
        category = "dark_light"
    )

    val MATERIAL_DARK = KeyboardTheme(
        id = "material_dark",
        nameAr = "ماتيريال داكن",
        nameEn = "Material Dark",
        backgroundColor = 0xFF14191E,
        keyBackgroundColor = 0xFF20262E,
        keyPressedColor = 0xFF2D353F,
        keyTextColor = 0xFFE2E8F0,
        subtextColor = 0xFF94A3B8,
        accentColor = 0xFF00F5D4,
        enterButtonColor = 0xFF0F766E,
        toolbarColor = 0xFF192027,
        borderColor = 0xFF2A3440,
        cornerRadius = 10f,
        keyStyle = "rounded",
        category = "dark_light"
    )

    val LIGHT_VIBRANT = KeyboardTheme(
        id = "light_vibrant",
        nameAr = "فاتح حيوي",
        nameEn = "Light Vibrant",
        backgroundColor = 0xFFE2E8F0,
        keyBackgroundColor = 0xFFFFFFFF,
        keyPressedColor = 0xFFCBD5E1,
        keyTextColor = 0xFF0F172A,
        subtextColor = 0xFF64748B,
        accentColor = 0xFF6366F1,
        enterButtonColor = 0xFF4F46E5,
        toolbarColor = 0xFFD8E0EA,
        borderColor = 0xFFCBD5E1,
        cornerRadius = 10f,
        keyStyle = "rounded",
        category = "dark_light"
    )

    // 2. Neon Themes
    val CYBER_PRO = KeyboardTheme(
        id = "cyber_pro",
        nameAr = "برو سايبر (الافتراضي)",
        nameEn = "Cyber Pro (Default)",
        backgroundColor = 0xFF0B0E14,
        keyBackgroundColor = 0xFF19202E,
        keyPressedColor = 0xFF2A364F,
        keyTextColor = 0xFFF0F4F8,
        subtextColor = 0xFF8392A5,
        accentColor = 0xFF00E5FF,
        enterButtonColor = 0xFF1976D2,
        toolbarColor = 0xFF101520,
        borderColor = 0xFF2C3950,
        cornerRadius = 10f,
        keyStyle = "neon",
        category = "neon"
    )

    val NEON_NIGHT = KeyboardTheme(
        id = "neon_night",
        nameAr = "نيون نايت",
        nameEn = "Neon Night",
        backgroundColor = 0xFF070A14,
        keyBackgroundColor = 0xFF12162A,
        keyPressedColor = 0xFF202646,
        keyTextColor = 0xFFE0E7FF,
        subtextColor = 0xFF818CF8,
        accentColor = 0xFFA855F7,
        enterButtonColor = 0xFF6366F1,
        toolbarColor = 0xFF0C1022,
        borderColor = 0xFF6366F1,
        cornerRadius = 12f,
        borderAlpha = 0.8f,
        keyStyle = "neon",
        category = "neon"
    )

    val NEON_CYBER = KeyboardTheme(
        id = "neon_cyber",
        nameAr = "نيون سايبر كول",
        nameEn = "Neon Cyber Cool",
        backgroundColor = 0xFF05050D,
        keyBackgroundColor = 0xFF0E1222,
        keyPressedColor = 0xFF1C2442,
        keyTextColor = 0xFF00F5FF,
        subtextColor = 0xFFFF007F,
        accentColor = 0xFFFF007F,
        enterButtonColor = 0xFFD90429,
        toolbarColor = 0xFF080C1A,
        borderColor = 0xFF00F5FF,
        cornerRadius = 12f,
        borderAlpha = 0.85f,
        keyStyle = "neon",
        category = "neon"
    )

    val MARSHMELLO_NEON = KeyboardTheme(
        id = "marshmello_neon",
        nameAr = "مارشميلو نيون",
        nameEn = "Marshmello Neon",
        backgroundColor = 0xFF160E2A,
        keyBackgroundColor = 0xFF271B45,
        keyPressedColor = 0xFF422C72,
        keyTextColor = 0xFFF3E8FF,
        subtextColor = 0xFFC084FC,
        accentColor = 0xFFC084FC,
        enterButtonColor = 0xFF7E22CE,
        toolbarColor = 0xFF1F143B,
        borderColor = 0xFFA855F7,
        cornerRadius = 14f,
        keyStyle = "neon",
        category = "neon"
    )

    // 3. Gradient Themes
    val SUNSET_GRADIENT = KeyboardTheme(
        id = "sunset_gradient",
        nameAr = "غروب الشمس المتدرج",
        nameEn = "Sunset Gradient",
        backgroundColor = 0xFF240B36,
        backgroundGradient = listOf(0xFF240B36, 0xFF511845, 0xFFC70039, 0xFFFF5733),
        keyBackgroundColor = 0x44FFFFFF,
        keyPressedColor = 0x66FFFFFF,
        keyTextColor = 0xFFFFFFFF,
        subtextColor = 0xFFFFD3B6,
        accentColor = 0xFFFFD700,
        enterButtonColor = 0xFFC70039,
        toolbarColor = 0x33000000,
        borderColor = 0x88FFB6B9,
        cornerRadius = 12f,
        borderAlpha = 0.6f,
        keyStyle = "glass",
        category = "gradient"
    )

    val VIBRANT_GRADIENT = KeyboardTheme(
        id = "vibrant_gradient",
        nameAr = "تدرج نيون أزرق وبنفسجي",
        nameEn = "Vibrant Gradient",
        backgroundColor = 0xFF0A192F,
        backgroundGradient = listOf(0xFF0F2027, 0xFF203A43, 0xFF4A00E0, 0xFF8E2DE2),
        keyBackgroundColor = 0x33FFFFFF,
        keyPressedColor = 0x55FFFFFF,
        keyTextColor = 0xFFF8FAFC,
        subtextColor = 0xFF93C5FD,
        accentColor = 0xFF38BDF8,
        enterButtonColor = 0xFF8E2DE2,
        toolbarColor = 0x44000000,
        borderColor = 0x6660A5FA,
        cornerRadius = 12f,
        borderAlpha = 0.5f,
        keyStyle = "glass",
        category = "gradient"
    )

    val CANDY_COLORFUL = KeyboardTheme(
        id = "candy_colorful",
        nameAr = "ألوان الحلوى",
        nameEn = "Candy Colorful",
        backgroundColor = 0xFFFDE2E4,
        backgroundGradient = listOf(0xFFFDE2E4, 0xFFFFF1E6, 0xFFE2ECE9, 0xFFDFE7FD),
        keyBackgroundColor = 0xCCFFFFFF,
        keyPressedColor = 0xEEFFFFFF,
        keyTextColor = 0xFF334155,
        subtextColor = 0xFF64748B,
        accentColor = 0xFFF472B6,
        enterButtonColor = 0xFFEC4899,
        toolbarColor = 0x44FFFFFF,
        borderColor = 0xFFFBCFE8,
        cornerRadius = 14f,
        keyStyle = "bubble",
        category = "colorful"
    )

    // 4. Glass / Transparent Themes
    val GLASSMORPHISM = KeyboardTheme(
        id = "glassmorphism",
        nameAr = "زجاجي شفاف",
        nameEn = "Glassmorphism",
        backgroundColor = 0xFF0D131F,
        backgroundGradient = listOf(0xFF1E293B, 0xFF0F172A, 0xFF1E1B4B),
        keyBackgroundColor = 0x2EFFFFFF,
        keyPressedColor = 0x4AFFFFFF,
        keyTextColor = 0xFFFFFFFF,
        subtextColor = 0xFF94A3B8,
        accentColor = 0xFF38BDF8,
        enterButtonColor = 0xFF0284C7,
        toolbarColor = 0x1AFFFFFF,
        borderColor = 0x55FFFFFF,
        cornerRadius = 14f,
        borderAlpha = 0.6f,
        keyStyle = "glass",
        isGlass = true,
        category = "glass"
    )

    val POLAR_ICE = KeyboardTheme(
        id = "polar_ice",
        nameAr = "جليد قطبي متجمد",
        nameEn = "Polar Ice",
        backgroundColor = 0xFF0C243C,
        backgroundGradient = listOf(0xFF031A30, 0xFF0B3954, 0xFF087E8B),
        keyBackgroundColor = 0x33E0F2FE,
        keyPressedColor = 0x55E0F2FE,
        keyTextColor = 0xFFF0F9FF,
        subtextColor = 0xFF7DD3FC,
        accentColor = 0xFF38BDF8,
        enterButtonColor = 0xFF0284C7,
        toolbarColor = 0x22000000,
        borderColor = 0x6638BDF8,
        cornerRadius = 12f,
        borderAlpha = 0.7f,
        keyStyle = "glass",
        isGlass = true,
        category = "glass"
    )

    val WATER_AND_ICE = KeyboardTheme(
        id = "water_and_ice",
        nameAr = "أمواج الماء والجليد",
        nameEn = "Water and Ice",
        backgroundColor = 0xFF024B68,
        backgroundGradient = listOf(0xFF0284C7, 0xFF0EA5E9, 0xFF38BDF8),
        keyBackgroundColor = 0x44FFFFFF,
        keyPressedColor = 0x77FFFFFF,
        keyTextColor = 0xFFFFFFFF,
        subtextColor = 0xFFBAE6FD,
        accentColor = 0xFFFFFFFF,
        enterButtonColor = 0xFF0369A1,
        toolbarColor = 0x33000000,
        borderColor = 0x66BAE6FD,
        cornerRadius = 12f,
        keyStyle = "glass",
        category = "glass"
    )

    // 5. Gaming Themes
    val CARBON_FIBER_GAMING = KeyboardTheme(
        id = "carbon_fiber_gaming",
        nameAr = "ألياف الكربون الرياضي",
        nameEn = "Carbon Fiber Gaming",
        backgroundColor = 0xFF0D0D0E,
        keyBackgroundColor = 0xFF191A1D,
        keyPressedColor = 0xFF2A2B30,
        keyTextColor = 0xFFFFFFFF,
        subtextColor = 0xFFEF4444,
        accentColor = 0xFFEF4444,
        enterButtonColor = 0xFFDC2626,
        toolbarColor = 0xFF131417,
        borderColor = 0xFFDC2626,
        cornerRadius = 8f,
        borderAlpha = 0.75f,
        keyStyle = "carbon",
        category = "gaming"
    )

    val GAMING_RGB = KeyboardTheme(
        id = "gaming_rgb",
        nameAr = "جيمينج RGB إلكتروني",
        nameEn = "Gaming RGB",
        backgroundColor = 0xFF050508,
        backgroundGradient = listOf(0xFF0D0221, 0xFF0F0826, 0xFF020C1B),
        keyBackgroundColor = 0xFF131722,
        keyPressedColor = 0xFF232A3E,
        keyTextColor = 0xFF00FFCC,
        subtextColor = 0xFFFF0055,
        accentColor = 0xFFFF0055,
        enterButtonColor = 0xFF00E5FF,
        toolbarColor = 0xFF090C14,
        borderColor = 0xFF00FFCC,
        cornerRadius = 8f,
        borderAlpha = 0.9f,
        keyStyle = "neon",
        category = "gaming"
    )

    val ANIME_ACTION = KeyboardTheme(
        id = "anime_action",
        nameAr = "أنمي أكشن حماسي",
        nameEn = "Anime Action",
        backgroundColor = 0xFF1A1A1A,
        backgroundGradient = listOf(0xFFB91C1C, 0xFF450A0A, 0xFF18181B),
        keyBackgroundColor = 0xFFFFFFFF,
        keyPressedColor = 0xFFE2E8F0,
        keyTextColor = 0xFF18181B,
        subtextColor = 0xFFDC2626,
        accentColor = 0xFFEF4444,
        enterButtonColor = 0xFFB91C1C,
        toolbarColor = 0xFF09090B,
        borderColor = 0xFFEF4444,
        cornerRadius = 10f,
        keyStyle = "rounded",
        category = "gaming"
    )

    // 6. Elegant & Luxury Themes
    val GOLD_LUXURY = KeyboardTheme(
        id = "gold_luxury",
        nameAr = "ذهب ملكي فاخر",
        nameEn = "Gold Luxury",
        backgroundColor = 0xFF141006,
        backgroundGradient = listOf(0xFF1F1705, 0xFF2E2207, 0xFF141006),
        keyBackgroundColor = 0xFFD4AF37,
        keyPressedColor = 0xFFE5C158,
        keyTextColor = 0xFF1C1302,
        subtextColor = 0xFF5C4408,
        accentColor = 0xFFFFD700,
        enterButtonColor = 0xFF996515,
        toolbarColor = 0xFF1A1508,
        borderColor = 0xFFFFE57F,
        cornerRadius = 10f,
        borderAlpha = 0.8f,
        keyStyle = "gold",
        category = "elegant"
    )

    val ELEGANT_ROSE_GOLD = KeyboardTheme(
        id = "elegant_rose_gold",
        nameAr = "ذهب وردي راقي",
        nameEn = "Elegant Rose Gold",
        backgroundColor = 0xFF2D1B22,
        backgroundGradient = listOf(0xFF38232B, 0xFF24151C),
        keyBackgroundColor = 0xFFE8C2CA,
        keyPressedColor = 0xFFF0D5DC,
        keyTextColor = 0xFF3D1924,
        subtextColor = 0xFF7A4A58,
        accentColor = 0xFFFB7185,
        enterButtonColor = 0xFF9F1239,
        toolbarColor = 0xFF22141A,
        borderColor = 0xFFF43F5E,
        cornerRadius = 12f,
        keyStyle = "rounded",
        category = "elegant"
    )

    val EMERALD_LUXURY = KeyboardTheme(
        id = "emerald_luxury",
        nameAr = "زمرد ملكي فاخر",
        nameEn = "Emerald Luxury",
        backgroundColor = 0xFF032219,
        backgroundGradient = listOf(0xFF064E3B, 0xFF022C22),
        keyBackgroundColor = 0xFF065F46,
        keyPressedColor = 0xFF047857,
        keyTextColor = 0xFFD1FAE5,
        subtextColor = 0xFFFCD34D,
        accentColor = 0xFFF59E0B,
        enterButtonColor = 0xFFD97706,
        toolbarColor = 0xFF022018,
        borderColor = 0xFFF59E0B,
        cornerRadius = 12f,
        borderAlpha = 0.7f,
        keyStyle = "rounded",
        category = "elegant"
    )

    val BLACK_GOLD = KeyboardTheme(
        id = "black_gold",
        nameAr = "أسود ذهبي",
        nameEn = "Black & Gold",
        backgroundColor = 0xFF121212,
        keyBackgroundColor = 0xFF201D17,
        keyPressedColor = 0xFF3D3525,
        keyTextColor = 0xFFFFD700,
        subtextColor = 0xFFC5A059,
        accentColor = 0xFFFFD700,
        enterButtonColor = 0xFF996515,
        toolbarColor = 0xFF1A1814,
        borderColor = 0xFFD4AF37,
        cornerRadius = 12f,
        borderAlpha = 0.6f,
        keyStyle = "gold",
        category = "elegant"
    )

    val RAMADAN_GOLD = KeyboardTheme(
        id = "ramadan_gold",
        nameAr = "شهر رمضان المبارك",
        nameEn = "Ramadan Moon",
        backgroundColor = 0xFF1E1408,
        keyBackgroundColor = 0xFF352410,
        keyPressedColor = 0xFF543B1D,
        keyTextColor = 0xFFFFE898,
        subtextColor = 0xFFE0BB66,
        accentColor = 0xFFFFA000,
        enterButtonColor = 0xFFE65100,
        toolbarColor = 0xFF28190B,
        borderColor = 0xFFFFA000,
        cornerRadius = 10f,
        borderAlpha = 0.5f,
        keyStyle = "gold",
        category = "elegant"
    )

    // 7. Colorful & Fun Themes
    val PASTEL_POP = KeyboardTheme(
        id = "pastel_pop",
        nameAr = "باستيل بوب منعش",
        nameEn = "Pastel Pop",
        backgroundColor = 0xFFE0F2FE,
        backgroundGradient = listOf(0xFFCCFBF1, 0xFFFEF3C7, 0xFFE0F2FE),
        keyBackgroundColor = 0xDDFFFFFF,
        keyPressedColor = 0xFFF1F5F9,
        keyTextColor = 0xFF1E293B,
        subtextColor = 0xFF0D9488,
        accentColor = 0xFF0D9488,
        enterButtonColor = 0xFF0284C7,
        toolbarColor = 0x33FFFFFF,
        borderColor = 0xFF99F6E4,
        cornerRadius = 14f,
        keyStyle = "bubble",
        category = "colorful"
    )

    val SEASONS = KeyboardTheme(
        id = "seasons",
        nameAr = "فصول السنة الأربعة",
        nameEn = "Seasons",
        backgroundColor = 0xFFFDF4E3,
        backgroundGradient = listOf(0xFFFCE7F3, 0xFFDCFCE7, 0xFFFFEDD5, 0xFFE0F2FE),
        keyBackgroundColor = 0xCCFFFFFF,
        keyPressedColor = 0xFFFFFFFF,
        keyTextColor = 0xFF1F2937,
        subtextColor = 0xFFEA580C,
        accentColor = 0xFFEA580C,
        enterButtonColor = 0xFF0284C7,
        toolbarColor = 0x33000000,
        borderColor = 0xFFFDBA74,
        cornerRadius = 12f,
        keyStyle = "bubble",
        category = "colorful"
    )

    val MONSTER_MANIA = KeyboardTheme(
        id = "monster_mania",
        nameAr = "وحوش مرحة ملونة",
        nameEn = "Monster Mania",
        backgroundColor = 0xFF2D1B4E,
        backgroundGradient = listOf(0xFF4C1D95, 0xFF831843, 0xFF0F766E),
        keyBackgroundColor = 0xDDFAF5FF,
        keyPressedColor = 0xFFFFFFFF,
        keyTextColor = 0xFF1F2937,
        subtextColor = 0xFFA855F7,
        accentColor = 0xFF22C55E,
        enterButtonColor = 0xFF9333EA,
        toolbarColor = 0x33000000,
        borderColor = 0xFFA855F7,
        cornerRadius = 16f,
        keyStyle = "bubble",
        category = "colorful"
    )

    val CARTOON_FUN = KeyboardTheme(
        id = "cartoon_fun",
        nameAr = "عالم الكرتون",
        nameEn = "Cartoon Fun",
        backgroundColor = 0xFF86EFAC,
        backgroundGradient = listOf(0xFFBAE6FD, 0xFF86EFAC, 0xFF4ADE80),
        keyBackgroundColor = 0xFFFFFBEB,
        keyPressedColor = 0xFFFEF3C7,
        keyTextColor = 0xFF1E293B,
        subtextColor = 0xFF16A34A,
        accentColor = 0xFFF59E0B,
        enterButtonColor = 0xFF0284C7,
        toolbarColor = 0x33000000,
        borderColor = 0xFFFDE68A,
        cornerRadius = 16f,
        keyStyle = "bubble",
        category = "colorful"
    )

    val MAGICAL_CREATURES = KeyboardTheme(
        id = "magical_creatures",
        nameAr = "مخلوقات سحرية",
        nameEn = "Magical Creatures",
        backgroundColor = 0xFFEDE9FE,
        backgroundGradient = listOf(0xFFF5D0FE, 0xFFDDD6FE, 0xFFC7D2FE),
        keyBackgroundColor = 0xEEFFFFFF,
        keyPressedColor = 0xFFFAF5FF,
        keyTextColor = 0xFF3B0764,
        subtextColor = 0xFF9333EA,
        accentColor = 0xFFC084FC,
        enterButtonColor = 0xFF7C3AED,
        toolbarColor = 0x33FFFFFF,
        borderColor = 0xFFE9D5FF,
        cornerRadius = 14f,
        keyStyle = "bubble",
        category = "colorful"
    )

    val RETRO_COMPUTE = KeyboardTheme(
        id = "retro_compute",
        nameAr = "كمبيوتر ريترو كلاسيك",
        nameEn = "Retro Compute",
        backgroundColor = 0xFFD8D2C2,
        keyBackgroundColor = 0xFFF0EBE1,
        keyPressedColor = 0xFFDDD6C7,
        keyTextColor = 0xFF1E1E1E,
        subtextColor = 0xFF5C5446,
        accentColor = 0xFFD32F2F,
        enterButtonColor = 0xFFC62828,
        toolbarColor = 0xFFCBC4B3,
        borderColor = 0xFFB8B09D,
        cornerRadius = 6f,
        keyStyle = "retro",
        category = "minimal"
    )

    val BLUE_GRAY = KeyboardTheme(
        id = "blue_gray",
        nameAr = "أزرق رمادي أنيق",
        nameEn = "Blue Gray",
        backgroundColor = 0xFF1E293B,
        keyBackgroundColor = 0xFF334155,
        keyPressedColor = 0xFF475569,
        keyTextColor = 0xFFF8FAFC,
        subtextColor = 0xFF94A3B8,
        accentColor = 0xFF38BDF8,
        enterButtonColor = 0xFF0284C7,
        toolbarColor = 0xFF0F172A,
        borderColor = 0xFF475569,
        cornerRadius = 10f,
        keyStyle = "rounded",
        category = "minimal"
    )

    val BRUSHED_METAL = KeyboardTheme(
        id = "brushed_metal",
        nameAr = "معدن مصقول احترافي",
        nameEn = "Brushed Metal",
        backgroundColor = 0xFF2A2E35,
        backgroundGradient = listOf(0xFF1E2228, 0xFF353B44, 0xFF22262C),
        keyBackgroundColor = 0xFF404752,
        keyPressedColor = 0xFF525B68,
        keyTextColor = 0xFFE2E8F0,
        subtextColor = 0xFF94A3B8,
        accentColor = 0xFF93C5FD,
        enterButtonColor = 0xFF3B82F6,
        toolbarColor = 0xFF1F2329,
        borderColor = 0xFF64748B,
        cornerRadius = 8f,
        keyStyle = "rounded",
        category = "minimal"
    )

    val FLORAL_BLOOM = KeyboardTheme(
        id = "floral_bloom",
        nameAr = "ورد وزهور الربيع",
        nameEn = "Floral Bloom",
        backgroundColor = 0xFF152128,
        keyBackgroundColor = 0xFF20323C,
        keyPressedColor = 0xFF2D4653,
        keyTextColor = 0xFFF0FDF4,
        subtextColor = 0xFF86EFAC,
        accentColor = 0xFF4ADE80,
        enterButtonColor = 0xFF16A34A,
        toolbarColor = 0xFF1B2A33,
        borderColor = 0xFF22C55E,
        cornerRadius = 12f,
        keyStyle = "rounded",
        category = "colorful"
    )

    // New requested Apple iOS & AMOLED Themes
    val IOS_DARK = KeyboardTheme(
        id = "ios_dark",
        nameAr = "آيفون داكن (iOS Dark)",
        nameEn = "iPhone Dark iOS",
        backgroundColor = 0xFF1C1C1E,
        keyBackgroundColor = 0xFF2C2C2E,
        keyPressedColor = 0xFF3A3A3C,
        keyTextColor = 0xFFFFFFFF,
        subtextColor = 0xFF8E8E93,
        accentColor = 0xFF0A84FF,
        enterButtonColor = 0xFF0A84FF,
        toolbarColor = 0xFF161618,
        borderColor = 0xFF3A3A3C,
        cornerRadius = 8f,
        keyStyle = "rounded",
        category = "dark_light"
    )

    val IOS_LIGHT = KeyboardTheme(
        id = "ios_light",
        nameAr = "آيفون فاتح (iOS Light)",
        nameEn = "iPhone Light iOS",
        backgroundColor = 0xFFD1D5DB,
        keyBackgroundColor = 0xFFFFFFFF,
        keyPressedColor = 0xFFE5E7EB,
        keyTextColor = 0xFF000000,
        subtextColor = 0xFF6B7280,
        accentColor = 0xFF007AFF,
        enterButtonColor = 0xFF007AFF,
        toolbarColor = 0xFFCBD5E1,
        borderColor = 0xFF9CA3AF,
        cornerRadius = 8f,
        keyStyle = "rounded",
        category = "dark_light"
    )

    // Flagship & OS Themes (iOS 26, iOS 27, One UI 7, HyperOS, EMUI, MagicOS)
    val IOS_26_DARK = KeyboardTheme(
        id = "ios_26_dark",
        nameAr = "آبل iOS 26 شفاف داكن",
        nameEn = "iOS 26 Translucent Dark",
        backgroundColor = 0xFF12141A,
        keyBackgroundColor = 0xFF222634,
        keyPressedColor = 0xFF343B4E,
        keyTextColor = 0xFFFFFFFF,
        subtextColor = 0xFF94A3B8,
        accentColor = 0xFF38BDF8,
        enterButtonColor = 0xFF0284C7,
        toolbarColor = 0xFF0F1217,
        borderColor = 0xFF333E54,
        cornerRadius = 9f,
        borderAlpha = 0.6f,
        isGlass = true,
        keyStyle = "glass",
        category = "systems"
    )

    val IOS_26_LIGHT = KeyboardTheme(
        id = "ios_26_light",
        nameAr = "آبل iOS 26 شفاف فاتح",
        nameEn = "iOS 26 Translucent Light",
        backgroundColor = 0xFFE2E8F0,
        keyBackgroundColor = 0xFFFFFFFF,
        keyPressedColor = 0xFFCBD5E1,
        keyTextColor = 0xFF0F172A,
        subtextColor = 0xFF64748B,
        accentColor = 0xFF2563EB,
        enterButtonColor = 0xFF3B82F6,
        toolbarColor = 0xFFD8E2EC,
        borderColor = 0xFFCBD5E1,
        cornerRadius = 9f,
        borderAlpha = 0.6f,
        isGlass = true,
        keyStyle = "glass",
        category = "systems"
    )

    val IOS_27_TITANIUM = KeyboardTheme(
        id = "ios_27_titanium",
        nameAr = "آبل iOS 27 تيتانيوم داكن",
        nameEn = "iOS 27 Dark Titanium",
        backgroundColor = 0xFF14171C,
        keyBackgroundColor = 0xFF1F242C,
        keyPressedColor = 0xFF2F3743,
        keyTextColor = 0xFFF8FAFC,
        subtextColor = 0xFF94A3B8,
        accentColor = 0xFFF59E0B,
        enterButtonColor = 0xFFD97706,
        toolbarColor = 0xFF101317,
        borderColor = 0xFF3E4756,
        cornerRadius = 10f,
        borderAlpha = 0.7f,
        keyStyle = "rounded",
        category = "systems"
    )

    val IOS_27_GLASS = KeyboardTheme(
        id = "ios_27_glass",
        nameAr = "آبل iOS 27 جلاسمورفيزم نيون",
        nameEn = "iOS 27 Glassmorphism",
        backgroundColor = 0xFF0B111E,
        keyBackgroundColor = 0xFF172338,
        keyPressedColor = 0xFF243656,
        keyTextColor = 0xFFE0F2FE,
        subtextColor = 0xFF7DD3FC,
        accentColor = 0xFF38BDF8,
        enterButtonColor = 0xFF0284C7,
        toolbarColor = 0xFF080D18,
        borderColor = 0xFF0284C7,
        cornerRadius = 10f,
        borderAlpha = 0.75f,
        isGlass = true,
        keyStyle = "glass",
        category = "systems"
    )

    val SAMSUNG_ONE_UI_7 = KeyboardTheme(
        id = "samsung_oneui_7",
        nameAr = "سامسونج One UI 7 الحديث",
        nameEn = "Samsung One UI 7 Minimal",
        backgroundColor = 0xFF191C24,
        keyBackgroundColor = 0xFF252A36,
        keyPressedColor = 0xFF373E4F,
        keyTextColor = 0xFFFFFFFF,
        subtextColor = 0xFF9CA3AF,
        accentColor = 0xFF2563EB,
        enterButtonColor = 0xFF1D4ED8,
        toolbarColor = 0xFF13161C,
        borderColor = 0xFF374151,
        cornerRadius = 10f,
        borderAlpha = 0.55f,
        keyStyle = "rounded",
        category = "systems"
    )

    val SAMSUNG_ONEUI_LIGHT = KeyboardTheme(
        id = "samsung_oneui_light",
        nameAr = "سامسونج One UI النهاري",
        nameEn = "Samsung One UI Light",
        backgroundColor = 0xFFF2F4F7,
        keyBackgroundColor = 0xFFFFFFFF,
        keyPressedColor = 0xFFE5E7EB,
        keyTextColor = 0xFF111827,
        subtextColor = 0xFF6B7280,
        accentColor = 0xFF2563EB,
        enterButtonColor = 0xFF2563EB,
        toolbarColor = 0xFFF9FAFB,
        borderColor = 0xFFE5E7EB,
        cornerRadius = 10f,
        borderAlpha = 0.4f,
        keyStyle = "rounded",
        category = "systems"
    )

    val XIAOMI_HYPER_OS = KeyboardTheme(
        id = "xiaomi_hyperos",
        nameAr = "شاومي HyperOS الأنيق",
        nameEn = "Xiaomi HyperOS Clean",
        backgroundColor = 0xFF0F172A,
        keyBackgroundColor = 0xFF1E293B,
        keyPressedColor = 0xFF334155,
        keyTextColor = 0xFFF8FAFC,
        subtextColor = 0xFF94A3B8,
        accentColor = 0xFFFF6900,
        enterButtonColor = 0xFFEA580C,
        toolbarColor = 0xFF0A0F1D,
        borderColor = 0xFF334155,
        cornerRadius = 11f,
        borderAlpha = 0.6f,
        keyStyle = "rounded",
        category = "systems"
    )

    val HUAWEI_EMUI_NEW = KeyboardTheme(
        id = "huawei_emui_new",
        nameAr = "هواوي EMUI الجديد",
        nameEn = "Huawei EMUI Modern",
        backgroundColor = 0xFF111827,
        keyBackgroundColor = 0xFF1F2937,
        keyPressedColor = 0xFF374151,
        keyTextColor = 0xFFF9FAFB,
        subtextColor = 0xFF9CA3AF,
        accentColor = 0xFFE11D48,
        enterButtonColor = 0xFFBE123C,
        toolbarColor = 0xFF0B0F19,
        borderColor = 0xFF374151,
        cornerRadius = 10f,
        borderAlpha = 0.55f,
        keyStyle = "rounded",
        category = "systems"
    )

    val HONOR_MAGIC_OS = KeyboardTheme(
        id = "honor_magicos_new",
        nameAr = "هونر MagicOS الجديد",
        nameEn = "Honor MagicOS New",
        backgroundColor = 0xFF0A101D,
        keyBackgroundColor = 0xFF162136,
        keyPressedColor = 0xFF243352,
        keyTextColor = 0xFFF0FDF4,
        subtextColor = 0xFF86EFAC,
        accentColor = 0xFF10B981,
        enterButtonColor = 0xFF059669,
        toolbarColor = 0xFF070B14,
        borderColor = 0xFF1F3252,
        cornerRadius = 10f,
        borderAlpha = 0.6f,
        keyStyle = "rounded",
        category = "systems"
    )

    val AMOLED_GOLD_LUXURY = KeyboardTheme(
        id = "amoled_gold_luxury",
        nameAr = "أموليد أسود وذهب ملكي",
        nameEn = "AMOLED Black & Gold Luxury",
        backgroundColor = 0xFF000000,
        keyBackgroundColor = 0xFF101010,
        keyPressedColor = 0xFF262010,
        keyTextColor = 0xFFFFDF7A,
        subtextColor = 0xFFD4AF37,
        accentColor = 0xFFFFD700,
        enterButtonColor = 0xFFD4AF37,
        toolbarColor = 0xFF050505,
        borderColor = 0xFFFFD700,
        cornerRadius = 10f,
        borderAlpha = 0.8f,
        keyStyle = "neon",
        category = "elegant"
    )

    val AMOLED_PURE_BLACK = KeyboardTheme(
        id = "amoled_pure_black",
        nameAr = "أموليد سواد فائق (AMOLED Black)",
        nameEn = "AMOLED Pure Black",
        backgroundColor = 0xFF000000,
        keyBackgroundColor = 0xFF0D0D0D,
        keyPressedColor = 0xFF1F1F1F,
        keyTextColor = 0xFFFFFFFF,
        subtextColor = 0xFF737373,
        accentColor = 0xFF00E5FF,
        enterButtonColor = 0xFF00B0FF,
        toolbarColor = 0xFF000000,
        borderColor = 0xFF262626,
        cornerRadius = 10f,
        keyStyle = "neon",
        category = "neon"
    )

    val ROYAL_PURPLE = KeyboardTheme(
        id = "royal_purple",
        nameAr = "أرجواني ملكي (Royal Purple)",
        nameEn = "Royal Purple",
        backgroundColor = 0xFF10091D,
        keyBackgroundColor = 0xFF1F1235,
        keyPressedColor = 0xFF311C54,
        keyTextColor = 0xFFF3E8FF,
        subtextColor = 0xFFA855F7,
        accentColor = 0xFFC084FC,
        enterButtonColor = 0xFF9333EA,
        toolbarColor = 0xFF160C29,
        borderColor = 0xFF3B1D6A,
        cornerRadius = 10f,
        keyStyle = "rounded",
        category = "colorful"
    )

    // --- Pro Smartphone Keyboard Styles (20 Unique Themes from Image) ---
    val CYBER_BLUE = KeyboardTheme(
        id = "cyber_blue",
        nameAr = "1. سايبر أزرق (Cyber Blue)",
        nameEn = "Cyber Blue",
        backgroundColor = 0xFF050B18,
        keyBackgroundColor = 0xFF0B1933,
        keyPressedColor = 0xFF142E5C,
        keyTextColor = 0xFFFFFFFF,
        subtextColor = 0xFF38BDF8,
        accentColor = 0xFF00E5FF,
        enterButtonColor = 0xFF00B4D8,
        toolbarColor = 0xFF030710,
        borderColor = 0xFF00E5FF,
        borderAlpha = 0.9f,
        cornerRadius = 8f,
        keyStyle = "neon",
        category = "pro_styles"
    )

    val NEON_PURPLE = KeyboardTheme(
        id = "neon_purple",
        nameAr = "2. نيون بنفسجي (Neon Purple)",
        nameEn = "Neon Purple",
        backgroundColor = 0xFF0F021B,
        keyBackgroundColor = 0xFF1E0A36,
        keyPressedColor = 0xFF35125E,
        keyTextColor = 0xFFFFFFFF,
        subtextColor = 0xFFE879F9,
        accentColor = 0xFFD946EF,
        enterButtonColor = 0xFFA855F7,
        toolbarColor = 0xFF0A0112,
        borderColor = 0xFFD946EF,
        borderAlpha = 0.9f,
        cornerRadius = 8f,
        keyStyle = "neon",
        category = "pro_styles"
    )

    val VOLCANO_RED = KeyboardTheme(
        id = "volcano_red",
        nameAr = "3. بركان أحمر (Volcano Red)",
        nameEn = "Volcano Red",
        backgroundColor = 0xFF140303,
        keyBackgroundColor = 0xFF260909,
        keyPressedColor = 0xFF421212,
        keyTextColor = 0xFFFFFFFF,
        subtextColor = 0xFFFCA5A5,
        accentColor = 0xFFFF3B30,
        enterButtonColor = 0xFFDC2626,
        toolbarColor = 0xFF0D0202,
        borderColor = 0xFFFF3B30,
        borderAlpha = 0.9f,
        cornerRadius = 8f,
        keyStyle = "neon",
        category = "pro_styles"
    )

    val TOXIC_GREEN = KeyboardTheme(
        id = "toxic_green",
        nameAr = "4. أخضر سام (Toxic Green)",
        nameEn = "Toxic Green",
        backgroundColor = 0xFF041407,
        keyBackgroundColor = 0xFF09260E,
        keyPressedColor = 0xFF13451B,
        keyTextColor = 0xFFFFFFFF,
        subtextColor = 0xFF86EFAC,
        accentColor = 0xFF22C55E,
        enterButtonColor = 0xFF16A34A,
        toolbarColor = 0xFF020E05,
        borderColor = 0xFF22C55E,
        borderAlpha = 0.9f,
        cornerRadius = 8f,
        keyStyle = "neon",
        category = "pro_styles"
    )

    val SUNSET_GLOW = KeyboardTheme(
        id = "sunset_glow",
        nameAr = "5. توهج الغروب (Sunset Glow)",
        nameEn = "Sunset Glow",
        backgroundColor = 0xFF1A0A17,
        keyBackgroundColor = 0xFF351226,
        keyPressedColor = 0xFF541E3D,
        keyTextColor = 0xFFFFFFFF,
        subtextColor = 0xFFFDBA74,
        accentColor = 0xFFFF7E40,
        enterButtonColor = 0xFFF43F5E,
        toolbarColor = 0xFF120610,
        borderColor = 0xFFFF7E40,
        borderAlpha = 0.9f,
        cornerRadius = 8f,
        keyStyle = "neon",
        category = "pro_styles"
    )

    val GOLDEN_LUXURY_PRO = KeyboardTheme(
        id = "golden_luxury",
        nameAr = "6. ذهبي فاخر (Golden Luxury)",
        nameEn = "Golden Luxury",
        backgroundColor = 0xFF0D0D09,
        keyBackgroundColor = 0xFF1C1A12,
        keyPressedColor = 0xFF302D1E,
        keyTextColor = 0xFFFEF08A,
        subtextColor = 0xFFD4AF37,
        accentColor = 0xFFEAB308,
        enterButtonColor = 0xFFCA8A04,
        toolbarColor = 0xFF080805,
        borderColor = 0xFFEAB308,
        borderAlpha = 0.9f,
        cornerRadius = 8f,
        keyStyle = "neon",
        category = "pro_styles"
    )

    val ROSE_GOLD_PRO = KeyboardTheme(
        id = "rose_gold_pro",
        nameAr = "7. ذهب وردي (Rose Gold)",
        nameEn = "Rose Gold",
        backgroundColor = 0xFF1F1216,
        keyBackgroundColor = 0xFF362026,
        keyPressedColor = 0xFF4F3039,
        keyTextColor = 0xFFFDE8ED,
        subtextColor = 0xFFF472B6,
        accentColor = 0xFFFB7185,
        enterButtonColor = 0xFFE11D48,
        toolbarColor = 0xFF140A0D,
        borderColor = 0xFFFB7185,
        borderAlpha = 0.85f,
        cornerRadius = 8f,
        keyStyle = "neon",
        category = "pro_styles"
    )

    val CYBERPUNK_CYBER = KeyboardTheme(
        id = "cyberpunk_cyber",
        nameAr = "8. سايبر بانك (Cyberpunk)",
        nameEn = "Cyberpunk",
        backgroundColor = 0xFF12041A,
        keyBackgroundColor = 0xFF270A37,
        keyPressedColor = 0xFF43135C,
        keyTextColor = 0xFFFFFFFF,
        subtextColor = 0xFF00F0FF,
        accentColor = 0xFFFF007F,
        enterButtonColor = 0xFFEC4899,
        toolbarColor = 0xFF0C0212,
        borderColor = 0xFFFF007F,
        borderAlpha = 0.9f,
        cornerRadius = 8f,
        keyStyle = "neon",
        category = "pro_styles"
    )

    val GALACTIC_VOID = KeyboardTheme(
        id = "galactic_void",
        nameAr = "9. فضاء مجري (Galactic Void)",
        nameEn = "Galactic Void",
        backgroundColor = 0xFF09061C,
        keyBackgroundColor = 0xFF160F3D,
        keyPressedColor = 0xFF261C61,
        keyTextColor = 0xFFFFFFFF,
        subtextColor = 0xFFA78BFA,
        accentColor = 0xFF8B5CF6,
        enterButtonColor = 0xFF7C3AED,
        toolbarColor = 0xFF050312,
        borderColor = 0xFF8B5CF6,
        borderAlpha = 0.9f,
        cornerRadius = 8f,
        keyStyle = "neon",
        category = "pro_styles"
    )

    val ACID_YELLOW = KeyboardTheme(
        id = "acid_yellow",
        nameAr = "10. أصفر فسفوري (Acid Yellow)",
        nameEn = "Acid Yellow",
        backgroundColor = 0xFF0D0D07,
        keyBackgroundColor = 0xFF1F1E0E,
        keyPressedColor = 0xFF353317,
        keyTextColor = 0xFFFEF08A,
        subtextColor = 0xFFEAB308,
        accentColor = 0xFFFACC15,
        enterButtonColor = 0xFFCA8A04,
        toolbarColor = 0xFF080803,
        borderColor = 0xFFFACC15,
        borderAlpha = 0.9f,
        cornerRadius = 8f,
        keyStyle = "neon",
        category = "pro_styles"
    )

    val HOLOGRAPHIC_PRO = KeyboardTheme(
        id = "holographic_pro",
        nameAr = "11. هولوجرافيك (Holographic)",
        nameEn = "Holographic",
        backgroundColor = 0xFF11142B,
        keyBackgroundColor = 0xFF1D2245,
        keyPressedColor = 0xFF2D3569,
        keyTextColor = 0xFFFFFFFF,
        subtextColor = 0xFF93C5FD,
        accentColor = 0xFF38BDF8,
        enterButtonColor = 0xFF6366F1,
        toolbarColor = 0xFF0B0D1F,
        borderColor = 0xFF818CF8,
        borderAlpha = 0.85f,
        cornerRadius = 8f,
        keyStyle = "neon",
        category = "pro_styles"
    )

    val STEALTH_DARK_PRO = KeyboardTheme(
        id = "stealth_dark",
        nameAr = "12. ستيلث داكن (Stealth Dark)",
        nameEn = "Stealth Dark",
        backgroundColor = 0xFF0B0C0E,
        keyBackgroundColor = 0xFF16181D,
        keyPressedColor = 0xFF242730,
        keyTextColor = 0xFFE2E8F0,
        subtextColor = 0xFF94A3B8,
        accentColor = 0xFF64748B,
        enterButtonColor = 0xFF334155,
        toolbarColor = 0xFF07080A,
        borderColor = 0xFF334155,
        borderAlpha = 0.75f,
        cornerRadius = 8f,
        keyStyle = "rounded",
        category = "pro_styles"
    )

    val CAMOUFLAGE_MILITARY = KeyboardTheme(
        id = "camouflage_military",
        nameAr = "13. تمويه عسكري (Camouflage Military)",
        nameEn = "Camouflage Military",
        backgroundColor = 0xFF111711,
        keyBackgroundColor = 0xFF1E271D,
        keyPressedColor = 0xFF2F3D2E,
        keyTextColor = 0xFFECFDF5,
        subtextColor = 0xFFA7F3D0,
        accentColor = 0xFF84A98C,
        enterButtonColor = 0xFF4F6D48,
        toolbarColor = 0xFF0A0E0A,
        borderColor = 0xFF84A98C,
        borderAlpha = 0.8f,
        cornerRadius = 8f,
        keyStyle = "rounded",
        category = "pro_styles"
    )

    val CARBON_FIBER_PRO = KeyboardTheme(
        id = "carbon_fiber_pro",
        nameAr = "14. ألياف الكربون (Carbon Fiber)",
        nameEn = "Carbon Fiber",
        backgroundColor = 0xFF101114,
        keyBackgroundColor = 0xFF1B1D22,
        keyPressedColor = 0xFF2A2D35,
        keyTextColor = 0xFFFFFFFF,
        subtextColor = 0xFFFDBA74,
        accentColor = 0xFFF97316,
        enterButtonColor = 0xFFEA580C,
        toolbarColor = 0xFF090A0C,
        borderColor = 0xFFF97316,
        borderAlpha = 0.85f,
        cornerRadius = 8f,
        keyStyle = "neon",
        category = "pro_styles"
    )

    val RETRO_ARCADE = KeyboardTheme(
        id = "retro_arcade",
        nameAr = "15. ريترو أركيد (Retro Arcade)",
        nameEn = "Retro Arcade",
        backgroundColor = 0xFF0E1120,
        keyBackgroundColor = 0xFF1A213B,
        keyPressedColor = 0xFF283259,
        keyTextColor = 0xFFFACC15,
        subtextColor = 0xFF60A5FA,
        accentColor = 0xFFEF4444,
        enterButtonColor = 0xFFEF4444,
        toolbarColor = 0xFF080A14,
        borderColor = 0xFFFACC15,
        borderAlpha = 0.9f,
        cornerRadius = 8f,
        keyStyle = "neon",
        category = "pro_styles"
    )

    val ICE_AND_SNOW = KeyboardTheme(
        id = "ice_and_snow",
        nameAr = "16. جليد وثلج (Ice & Snow)",
        nameEn = "Ice & Snow",
        backgroundColor = 0xFF091626,
        keyBackgroundColor = 0xFF132842,
        keyPressedColor = 0xFF1E3C61,
        keyTextColor = 0xFFF0F9FF,
        subtextColor = 0xFF7DD3FC,
        accentColor = 0xFF38BDF8,
        enterButtonColor = 0xFF0284C7,
        toolbarColor = 0xFF050F1A,
        borderColor = 0xFF38BDF8,
        borderAlpha = 0.9f,
        cornerRadius = 8f,
        keyStyle = "neon",
        category = "pro_styles"
    )

    val SYNTHWAVE_80S = KeyboardTheme(
        id = "synthwave_80s",
        nameAr = "17. سينث ويف 80 (Synthwave 80s)",
        nameEn = "Synthwave 80s",
        backgroundColor = 0xFF110722,
        keyBackgroundColor = 0xFF220F3E,
        keyPressedColor = 0xFF381A66,
        keyTextColor = 0xFFFFFFFF,
        subtextColor = 0xFF22D3EE,
        accentColor = 0xFFF43F5E,
        enterButtonColor = 0xFFEC4899,
        toolbarColor = 0xFF090313,
        borderColor = 0xFFF43F5E,
        borderAlpha = 0.9f,
        cornerRadius = 8f,
        keyStyle = "neon",
        category = "pro_styles"
    )

    val MATRIX_DIGITAL = KeyboardTheme(
        id = "matrix_digital",
        nameAr = "18. ماتريكس رقمي (Matrix Digital)",
        nameEn = "Matrix Digital",
        backgroundColor = 0xFF010A03,
        keyBackgroundColor = 0xFF051809,
        keyPressedColor = 0xFF092910,
        keyTextColor = 0xFF86EFAC,
        subtextColor = 0xFF4ADE80,
        accentColor = 0xFF22C55E,
        enterButtonColor = 0xFF16A34A,
        toolbarColor = 0xFF010602,
        borderColor = 0xFF22C55E,
        borderAlpha = 0.95f,
        cornerRadius = 8f,
        keyStyle = "neon",
        category = "pro_styles"
    )

    val COPPER_RUST = KeyboardTheme(
        id = "copper_rust",
        nameAr = "19. نحاس مؤكسد (Copper Rust)",
        nameEn = "Copper Rust",
        backgroundColor = 0xFF160D07,
        keyBackgroundColor = 0xFF27180F,
        keyPressedColor = 0xFF3D2719,
        keyTextColor = 0xFFFED7AA,
        subtextColor = 0xFFFB923C,
        accentColor = 0xFFD97706,
        enterButtonColor = 0xFFB45309,
        toolbarColor = 0xFF0E0804,
        borderColor = 0xFFD97706,
        borderAlpha = 0.85f,
        cornerRadius = 8f,
        keyStyle = "neon",
        category = "pro_styles"
    )

    val AURORA_BOREALIS = KeyboardTheme(
        id = "aurora_borealis",
        nameAr = "20. الشفق القطبي (Aurora Borealis)",
        nameEn = "Aurora Borealis",
        backgroundColor = 0xFF031218,
        keyBackgroundColor = 0xFF08222B,
        keyPressedColor = 0xFF0F3644,
        keyTextColor = 0xFFE0F2FE,
        subtextColor = 0xFF5EEAD4,
        accentColor = 0xFF14B8A6,
        enterButtonColor = 0xFF0D9488,
        toolbarColor = 0xFF020A0E,
        borderColor = 0xFF14B8A6,
        borderAlpha = 0.9f,
        cornerRadius = 8f,
        keyStyle = "neon",
        category = "pro_styles"
    )

    val LIQUID_GLASS_BLUR = KeyboardTheme(
        id = "liquid_glass_blur",
        nameAr = "زجاج سائل واضح (Liquid Glass Clear)",
        nameEn = "Liquid Glass Clear",
        backgroundColor = 0xDD0D1117,
        keyBackgroundColor = 0xEE1E2430,
        keyPressedColor = 0xFF2D3548,
        keyTextColor = 0xFFFFFFFF,
        subtextColor = 0xFF94A3B8,
        accentColor = 0xFF388AF6,
        enterButtonColor = 0xFF2563EB,
        toolbarColor = 0xEE0B0E14,
        borderColor = 0x44FFFFFF,
        borderAlpha = 0.8f,
        cornerRadius = 14f,
        keyStyle = "rounded",
        category = "glass",
        isGlass = true
    )


    val allPresets = (twentyLightThemes + twentyPremiumThemes + listOf(
        // New Anime, Fantasy & Pastel Light Themes
        SHADOW_NINJA,
        SAKURA_DREAM,
        BLUE_DRAGON,
        KAKASHI_SHINOBI,
        NEON_CITY,
        NARUTO_CHAKRA,
        GOKU_SAIYAN,
        BLACK_GOLD_LUXURY,
        PURPLE_BUTTERFLY,
        FIRE_PHOENIX,
        ICE_QUEEN,
        PASTEL_PEACH,
        PASTEL_MINT,
        PASTEL_LAVENDER,

        // Featured Liquid Glass & Pro Styles
        LIQUID_GLASS_BLUR,
        CYBER_BLUE,
        NEON_PURPLE,
        VOLCANO_RED,
        TOXIC_GREEN,
        SUNSET_GLOW,
        GOLDEN_LUXURY_PRO,
        ROSE_GOLD_PRO,
        CYBERPUNK_CYBER,
        GALACTIC_VOID,
        ACID_YELLOW,
        HOLOGRAPHIC_PRO,
        STEALTH_DARK_PRO,
        CAMOUFLAGE_MILITARY,
        CARBON_FIBER_PRO,
        RETRO_ARCADE,
        ICE_AND_SNOW,
        SYNTHWAVE_80S,
        MATRIX_DIGITAL,
        COPPER_RUST,
        AURORA_BOREALIS,
        // Modern Systems & Flagship OS
        IOS_27_GLASS,
        IOS_27_TITANIUM,
        IOS_26_DARK,
        IOS_26_LIGHT,
        SAMSUNG_ONE_UI_7,
        SAMSUNG_ONEUI_LIGHT,
        XIAOMI_HYPER_OS,
        HUAWEI_EMUI_NEW,
        HONOR_MAGIC_OS,
        AMOLED_GOLD_LUXURY,

        // Classic iOS Themes
        IOS_DARK,
        IOS_LIGHT,
        AMOLED_PURE_BLACK,
        ROYAL_PURPLE,

        // Neon & Cyber
        CYBER_PRO,
        NEON_NIGHT,
        NEON_CYBER,
        MARSHMELLO_NEON,
        GAMING_RGB,

        // Dark & Light
        DARK_MATTE,
        MINIMAL_LIGHT,
        MINIMALIST_WHITE,
        MATERIAL_DARK,
        LIGHT_VIBRANT,

        // Gradients
        SUNSET_GRADIENT,
        VIBRANT_GRADIENT,
        CANDY_COLORFUL,

        // Glass & Transparent
        GLASSMORPHISM,
        POLAR_ICE,
        WATER_AND_ICE,

        // Gaming & Action
        CARBON_FIBER_GAMING,
        ANIME_ACTION,

        // Luxury & Elegant
        GOLD_LUXURY,
        ELEGANT_ROSE_GOLD,
        EMERALD_LUXURY,
        BLACK_GOLD,
        RAMADAN_GOLD,

        // Colorful & Fun
        PASTEL_POP,
        SEASONS,
        MONSTER_MANIA,
        CARTOON_FUN,
        MAGICAL_CREATURES,
        FLORAL_BLOOM,

        // Minimal & Retro
        BLUE_GRAY,
        BRUSHED_METAL,
        RETRO_COMPUTE
    )).distinctBy { it.id }

    fun getById(id: String): KeyboardTheme {
        return allPresets.find { it.id == id } ?: CRYSTAL_LIGHT
    }
}
