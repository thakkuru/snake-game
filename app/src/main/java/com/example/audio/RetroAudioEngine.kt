package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.example.model.FeedbackEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

interface SoundPlayer {
  fun play(event: FeedbackEvent)
}

class RetroAudioEngine : SoundPlayer {
  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
  private val sampleRate = 22050

  override fun play(event: FeedbackEvent) {
    scope.launch {
      runCatching {
        when (event) {
          FeedbackEvent.TURN -> playTones(listOf(420.0 to 16))
          FeedbackEvent.EAT -> playTones(listOf(587.33 to 36, 880.0 to 52))
          FeedbackEvent.NEW_RECORD ->
            playTones(listOf(523.25 to 55, 659.25 to 55, 783.99 to 55, 1046.50 to 110))
          FeedbackEvent.GAME_OVER ->
            playTones(listOf(329.63 to 90, 246.94 to 95, 164.81 to 170))
        }
      }
    }
  }

  private fun playTones(notes: List<Pair<Double, Int>>) {
    val totalSamples = notes.sumOf { (_, ms) -> (sampleRate * ms) / 1000 }
    if (totalSamples <= 0) return

    val buffer = ShortArray(totalSamples)
    var offset = 0
    for ((freqHz, durationMs) in notes) {
      val count = (sampleRate * durationMs) / 1000
      val periodSamples = (sampleRate / freqHz).coerceAtLeast(1.0)
      for (i in 0 until count) {
        val phase = (i % periodSamples) / periodSamples
        val envelope =
          if (i < 30) i / 30.0 else if (i > count - 60) (count - i).coerceAtLeast(0) / 60.0 else 1.0
        val square = if (phase < 0.5) 1.0 else -1.0
        buffer[offset + i] = (square * envelope * 4200).toInt().toShort()
      }
      offset += count
    }

    val minBufBytes =
      AudioTrack.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_OUT_MONO,
        AudioFormat.ENCODING_PCM_16BIT,
      )
    val bufferBytes = maxOf(minBufBytes, buffer.size * 2)
    if (bufferBytes <= 0) return

    val track =
      AudioTrack.Builder()
        .setAudioAttributes(
          AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        )
        .setAudioFormat(
          AudioFormat.Builder()
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .setSampleRate(sampleRate)
            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
            .build()
        )
        .setBufferSizeInBytes(bufferBytes)
        .setTransferMode(AudioTrack.MODE_STATIC)
        .build()

    try {
      track.write(buffer, 0, buffer.size)
      track.play()
      val totalMs = notes.sumOf { it.second }.toLong()
      Thread.sleep(totalMs + 25L)
    } finally {
      runCatching {
        track.stop()
        track.release()
      }
    }
  }
}
