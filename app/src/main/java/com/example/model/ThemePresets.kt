package com.example.model

object ThemePresets {

    fun isLightColor(colorLong: Long): Boolean {
        val r = ((colorLong shr 16) and 0xFF) / 255.0
        val g = ((colorLong shr 8) and 0xFF) / 255.0
        val b = (colorLong and 0xFF) / 255.0
        return (0.299 * r + 0.587 * g + 0.114 * b) > 0.5
    }

    // 1. Dark & Light Themes
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
        category = "dark_light"
    )

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

    val allPresets = listOf(
        // Modern Systems & Flagship OS
        IOS_27_GLASS,
        IOS_27_TITANIUM,
        IOS_26_DARK,
        IOS_26_LIGHT,
        SAMSUNG_ONE_UI_7,
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
        MINIMAL_DARK,
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
    )

    fun getById(id: String): KeyboardTheme {
        return allPresets.find { it.id == id } ?: CYBER_PRO
    }
}
