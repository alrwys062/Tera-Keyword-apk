package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Turbo Keyboard", appName)
  }

  @Test
  fun `test AudioTrack stream creation and write`() {
    val sampleRate = 44100
    val minBuf = android.media.AudioTrack.getMinBufferSize(
      sampleRate,
      android.media.AudioFormat.CHANNEL_OUT_MONO,
      android.media.AudioFormat.ENCODING_PCM_16BIT
    )
    val bufferSize = maxOf(minBuf * 2, 4096)
    val track = android.media.AudioTrack.Builder()
      .setAudioAttributes(
        android.media.AudioAttributes.Builder()
          .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
          .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
          .build()
      )
      .setAudioFormat(
        android.media.AudioFormat.Builder()
          .setEncoding(android.media.AudioFormat.ENCODING_PCM_16BIT)
          .setSampleRate(sampleRate)
          .setChannelMask(android.media.AudioFormat.CHANNEL_OUT_MONO)
          .build()
      )
      .setBufferSizeInBytes(bufferSize)
      .setTransferMode(android.media.AudioTrack.MODE_STREAM)
      .build()
    track.play()
    val pcm = ShortArray(1000) { 1000 }
    val written = track.write(pcm, 0, pcm.size, android.media.AudioTrack.WRITE_NON_BLOCKING)
    org.junit.Assert.assertTrue(written >= 0)
    track.release()
  }

  @Test
  fun `test keyboard sound engine all profiles and special keys`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    com.example.sound.KeyboardSoundEngine.initialize(context)
    val profiles = com.example.sound.KeyboardSoundEngine.AVAILABLE_PROFILES
    assertEquals(9, profiles.size)

    // Test playing each sound profile
    profiles.forEach { prof ->
      com.example.sound.KeyboardSoundEngine.playKeySound(profile = prof.id, volume = 1.0f)
      com.example.sound.KeyboardSoundEngine.playKeySound(profile = prof.id, volume = 0.5f)
    }

    // Test special keys (space, delete, return/special)
    com.example.sound.KeyboardSoundEngine.playKeySound(isSpace = true, volume = 0.8f)
    com.example.sound.KeyboardSoundEngine.playKeySound(isDelete = true, volume = 0.8f)
    com.example.sound.KeyboardSoundEngine.playKeySound(isSpecial = true, volume = 0.8f)
  }
}
