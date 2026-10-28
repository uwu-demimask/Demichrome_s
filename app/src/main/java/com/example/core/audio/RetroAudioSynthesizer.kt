package com.example.core.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.sin

/**
 * Ultra-lightweight retro audio synthesizer using AudioTrack PCM sine/square wave.
 * Zero external audio files, zero audio latency, 100% offline, battery friendly.
 */
object RetroAudioSynthesizer {

    private const val SAMPLE_RATE = 22050

    suspend fun playTone(
        frequencyHz: Double,
        durationMs: Int,
        waveform: Waveform = Waveform.SINE,
        volume: Float = 0.5f
    ) = withContext(Dispatchers.Default) {
        try {
            val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
            val samples = ShortArray(numSamples)

            val attackSamples = (numSamples * 0.1).toInt().coerceAtLeast(1)
            val decaySamples = (numSamples * 0.2).toInt().coerceAtLeast(1)

            for (i in 0 until numSamples) {
                val t = i.toDouble() / SAMPLE_RATE
                val rawWave = when (waveform) {
                    Waveform.SINE -> sin(2.0 * Math.PI * frequencyHz * t)
                    Waveform.SQUARE -> if (sin(2.0 * Math.PI * frequencyHz * t) >= 0) 0.8 else -0.8
                    Waveform.RETRO_CHIP -> {
                        // 8-bit quantized pseudo-triangle/square
                        val s = sin(2.0 * Math.PI * frequencyHz * t)
                        (s * 4).toInt() / 4.0
                    }
                }

                // Envelope shaping (Attack-Sustain-Decay) to prevent clicking
                val envelope = when {
                    i < attackSamples -> i.toFloat() / attackSamples
                    i > numSamples - decaySamples -> (numSamples - i).toFloat() / decaySamples
                    else -> 1.0f
                }

                val sampleVal = (rawWave * envelope * volume * Short.MAX_VALUE).toInt()
                samples[i] = sampleVal.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }

            val minBufferSize = AudioTrack.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )

            val audioTrack = AudioTrack.Builder()
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
                .setBufferSizeInBytes(samples.size * 2.coerceAtLeast(minBufferSize))
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(samples, 0, samples.size)
            audioTrack.play()

            // Release after playing
            kotlinx.coroutines.delay(durationMs.toLong() + 50)
            audioTrack.stop()
            audioTrack.release()
        } catch (_: Exception) {
            // Graceful catch if audio hardware is unavailable
        }
    }

    /**
     * Subtle vintage retro click for buttons
     */
    suspend fun playClick() {
        playTone(frequencyHz = 880.0, durationMs = 35, waveform = Waveform.SINE, volume = 0.25f)
    }

    /**
     * Terminal start chirp
     */
    suspend fun playStartChirp() {
        playTone(frequencyHz = 660.0, durationMs = 60, waveform = Waveform.RETRO_CHIP, volume = 0.35f)
        kotlinx.coroutines.delay(20)
        playTone(frequencyHz = 1100.0, durationMs = 90, waveform = Waveform.RETRO_CHIP, volume = 0.4f)
    }

    /**
     * Pause tone
     */
    suspend fun playPauseTone() {
        playTone(frequencyHz = 750.0, durationMs = 80, waveform = Waveform.SINE, volume = 0.3f)
    }

    /**
     * Gentle 3-note retro completion arpeggio
     */
    suspend fun playCompletionArpeggio() {
        playTone(frequencyHz = 523.25, durationMs = 120, waveform = Waveform.RETRO_CHIP, volume = 0.45f) // C5
        kotlinx.coroutines.delay(30)
        playTone(frequencyHz = 659.25, durationMs = 120, waveform = Waveform.RETRO_CHIP, volume = 0.45f) // E5
        kotlinx.coroutines.delay(30)
        playTone(frequencyHz = 783.99, durationMs = 180, waveform = Waveform.RETRO_CHIP, volume = 0.5f)  // G5
        kotlinx.coroutines.delay(30)
        playTone(frequencyHz = 1046.50, durationMs = 280, waveform = Waveform.SINE, volume = 0.55f)     // C6
    }

    /**
     * Low retro error buzzer for shield interventions
     */
    suspend fun playErrorBuzzer() {
        playTone(frequencyHz = 220.0, durationMs = 100, waveform = Waveform.SQUARE, volume = 0.4f)
        kotlinx.coroutines.delay(30)
        playTone(frequencyHz = 180.0, durationMs = 140, waveform = Waveform.SQUARE, volume = 0.45f)
    }

    enum class AmbientSoundType {
        NONE,
        BINAURAL_432HZ,
        WARM_BROWN_NOISE,
        MECHANICAL_TICK
    }

    private var activeAmbientTrack: AudioTrack? = null
    private var ambientJob: kotlinx.coroutines.Job? = null

    /**
     * Starts continuous ambient focus sound generation
     */
    fun startAmbient(type: AmbientSoundType, volume: Float = 0.2f, coroutineScope: kotlinx.coroutines.CoroutineScope) {
        stopAmbient()
        if (type == AmbientSoundType.NONE) return

        ambientJob = coroutineScope.launch(Dispatchers.Default) {
            try {
                val minBufferSize = AudioTrack.getMinBufferSize(
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )
                val bufferSize = minBufferSize * 4
                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
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

                activeAmbientTrack = track
                track.play()

                val chunk = ShortArray(1024)
                var phase = 0.0
                var lastRandom = 0.0
                val random = java.util.Random()

                while (ambientJob?.isActive == true && activeAmbientTrack != null) {
                    for (i in chunk.indices) {
                        val sampleVal: Double = when (type) {
                            AmbientSoundType.BINAURAL_432HZ -> {
                                // 432Hz sine carrier with 4Hz slow theta beat modulation
                                phase += 2.0 * Math.PI * 432.0 / SAMPLE_RATE
                                val carrier = sin(phase)
                                val modulation = 0.8 + 0.2 * sin(phase * (4.0 / 432.0))
                                carrier * modulation * volume
                            }
                            AmbientSoundType.WARM_BROWN_NOISE -> {
                                // Low-pass integrated white noise (Brown/Red noise)
                                val white = (random.nextDouble() * 2.0 - 1.0)
                                lastRandom = (lastRandom + (0.05 * white)) / 1.05
                                lastRandom * volume * 2.5
                            }
                            AmbientSoundType.MECHANICAL_TICK -> {
                                // Subtle ticking pulses every 1 sec
                                val sampleIndexInSec = (phase % SAMPLE_RATE).toInt()
                                phase += 1.0
                                if (sampleIndexInSec < 120) {
                                    val tickPhase = sampleIndexInSec.toDouble() / 120.0
                                    sin(tickPhase * Math.PI * 8.0) * (1.0 - tickPhase) * volume * 1.5
                                } else 0.0
                            }
                            AmbientSoundType.NONE -> 0.0
                        }
                        chunk[i] = (sampleVal * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                    }
                    track.write(chunk, 0, chunk.size)
                }
            } catch (_: Exception) {
            } finally {
                try {
                    activeAmbientTrack?.stop()
                    activeAmbientTrack?.release()
                } catch (_: Exception) {}
                activeAmbientTrack = null
            }
        }
    }

    fun stopAmbient() {
        ambientJob?.cancel()
        ambientJob = null
        try {
            activeAmbientTrack?.stop()
            activeAmbientTrack?.release()
        } catch (_: Exception) {}
        activeAmbientTrack = null
    }

    enum class Waveform {
        SINE,
        SQUARE,
        RETRO_CHIP
    }
}
