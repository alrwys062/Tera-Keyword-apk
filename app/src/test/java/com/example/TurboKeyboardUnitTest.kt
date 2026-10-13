package com.example

import com.example.data.PredictionEngine
import com.example.data.TextDecorator
import com.example.keyboard.KeyLayouts
import com.example.model.ThemePresets
import org.junit.Assert.*
import org.junit.Test

class TurboKeyboardUnitTest {

    @Test
    fun testThemePresets() {
        val presets = ThemePresets.allPresets
        assertTrue("Themes should not be empty", presets.size >= 25)
        val defaultTheme = ThemePresets.getById("cyber_pro")
        assertEquals("cyber_pro", defaultTheme.id)
        val blackGold = ThemePresets.getById("black_gold")
        assertEquals("black_gold", blackGold.id)
        val minimalDark = ThemePresets.getById("minimal_dark")
        assertEquals("minimal_dark", minimalDark.id)
        val sunsetGradient = ThemePresets.getById("sunset_gradient")
        assertNotNull(sunsetGradient.backgroundGradient)
    }

    @Test
    fun testArabicDecoration() {
        val styles = TextDecorator.arabicStyles
        assertTrue("Arabic styles available", styles.isNotEmpty())
        val sample = "كيبورد"
        val royal = styles.find { it.nameEn.contains("Royal") }
        assertNotNull(royal)
        val result = royal!!.transform(sample)
        assertTrue(result.contains("كيبورد"))
        assertTrue(result.contains("꧁"))
    }

    @Test
    fun testEnglishDecoration() {
        val styles = TextDecorator.englishStyles
        assertTrue("English styles available", styles.isNotEmpty())
        val sample = "Hello"
        val bold = styles.find { it.nameEn.contains("Bold Serif") }
        assertNotNull(bold)
        val transformed = bold!!.transform(sample)
        assertNotEquals("Hello", transformed)
    }

    @Test
    fun testPredictionEngine() {
        val arSuggestions = PredictionEngine.getPredictions("كي", true)
        assertEquals(3, arSuggestions.size)

        val enSuggestions = PredictionEngine.getPredictions("sug", false)
        assertEquals(3, enSuggestions.size)
    }

    @Test
    fun testKeyLayouts() {
        assertTrue("Arabic row 1 has letters", KeyLayouts.arabic10ColRow1.isNotEmpty())
        assertTrue("Arabic row 2 has letters", KeyLayouts.arabic10ColRow2.isNotEmpty())
        assertTrue("Arabic row 3 has letters", KeyLayouts.arabic10ColRow3.isNotEmpty())
        assertTrue("English row 1 has letters", KeyLayouts.englishRow1.isNotEmpty())
    }

    @Test
    fun testKeyboardStatusDetection() {
        val disabled = com.example.utils.KeyboardStatus.DISABLED
        val enabled = com.example.utils.KeyboardStatus.ENABLED_NOT_DEFAULT
        val active = com.example.utils.KeyboardStatus.ACTIVE_DEFAULT
        assertNotNull(disabled)
        assertNotNull(enabled)
        assertNotNull(active)
    }

    @Test
    fun testSpellCheckDictionary() {
        val correction = com.example.data.SpellCheckDictionary.getCorrection("انشاء الله", true)
        assertEquals("إن شاء الله", correction)

        val butCorrection = com.example.data.SpellCheckDictionary.getCorrection("لاكن", true)
        assertEquals("لكن", butCorrection)

        val sentence = "انشاء الله بشوفك لاكن هدا مهم"
        val corrected = com.example.data.SpellCheckDictionary.autoCorrectSentence(sentence, true)
        assertTrue(corrected.contains("إن شاء الله"))
        assertTrue(corrected.contains("لكن"))
        assertTrue(corrected.contains("هذا"))
    }

    @Test
    fun testAiToneEngine() {
        val sample = "ابي اكلمك بموضوع"
        val formal = com.example.data.AiToneEngine.transformTone(sample, "formal")
        assertNotNull(formal)
        assertTrue(formal.isNotBlank())

        val friendly = com.example.data.AiToneEngine.transformTone(sample, "friendly")
        assertNotNull(friendly)

        val tones = com.example.data.AiToneEngine.allTones
        assertTrue(tones.size >= 8)
    }

    @Test
    fun testFawazDecorations() {
        // Test Fawaz 1
        val fawaz1 = TextDecorator.arabicStyles.find { it.id == "fawaz_1" }
        assertNotNull("Fawaz 1 must exist", fawaz1)
        val fawaz1Decorated = fawaz1!!.transform("مرحبا")
        assertTrue("Fawaz 1 contains connector", fawaz1Decorated.contains(TextDecorator.FAWAZ_1_CONNECTOR))
        assertEquals("ـ͜إآ", TextDecorator.decorateChar("ا", "fawaz_1"))
        assertTrue(TextDecorator.decorateChar("ج", "fawaz_1").contains("ًج"))

        // Test Fawaz 2
        val fawaz2 = TextDecorator.arabicStyles.find { it.id == "fawaz_2" }
        assertNotNull("Fawaz 2 must exist", fawaz2)
        val fawaz2Char = TextDecorator.decorateChar("ج", "fawaz_2")
        assertEquals("جٰٛ", fawaz2Char)
        assertEquals("ٲاٖ", TextDecorator.decorateChar("ا", "fawaz_2"))
        assertEquals("مٰۧ", TextDecorator.decorateChar("م", "fawaz_2"))
        assertEquals("رٰؔ", TextDecorator.decorateChar("ر", "fawaz_2"))
        assertEquals("حٰٛ", TextDecorator.decorateChar("ح", "fawaz_2"))
        assertEquals("بٰۧ", TextDecorator.decorateChar("ب", "fawaz_2"))

        val fawaz2Decorated = fawaz2!!.transform("مرحبا")
        assertEquals("مٰۧرٰؔحٰٛبٰۧٲاٖ", fawaz2Decorated)
    }

    @Test
    fun testKeyboardSoundProfiles() {
        val profiles = com.example.sound.KeyboardSoundEngine.AVAILABLE_PROFILES
        assertTrue("Profiles count >= 8", profiles.size >= 8)
        assertNotNull(profiles.find { it.id == com.example.sound.KeyboardSoundEngine.PROFILE_IOS_16 })
        assertNotNull(profiles.find { it.id == com.example.sound.KeyboardSoundEngine.PROFILE_MECHANICAL })
        assertNotNull(profiles.find { it.id == com.example.sound.KeyboardSoundEngine.PROFILE_WATER_DROP })
    }
}
