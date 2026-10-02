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
        assertTrue("Arabic row 1 has letters", KeyLayouts.arabicRow1.isNotEmpty())
        assertTrue("Arabic row 2 has letters", KeyLayouts.arabicRow2.isNotEmpty())
        assertTrue("Arabic row 3 has letters", KeyLayouts.arabicRow3.isNotEmpty())
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
}
