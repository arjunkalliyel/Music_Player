package com.example.musicplayer.musicPlayerManager

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.media.audiofx.Visualizer
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.musicplayer.model.AudioTrack
import com.example.musicplayer.service.MusicService
import com.example.musicplayer.utils.CommonUtils
import kotlin.math.log10
import kotlin.math.sqrt

class MusicPlayerManager private constructor(
    private val context: Context,
    private val tracks: List<AudioTrack>
) {
    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        var instance: MusicPlayerManager? = null

        fun getInstance(context: Context, tracks: List<AudioTrack>): MusicPlayerManager {
            return instance ?: synchronized(this) {
                instance ?: MusicPlayerManager(context.applicationContext, tracks).also {
                    instance = it
                }
            }
        }
    }

    private var mediaPlayer: MediaPlayer? = null
    private var currentIndex = 0
    private var visualizer: Visualizer? = null

    var onTrackChanged: ((AudioTrack) -> Unit)? = null
    var onSessionReady: ((Int) -> Unit)? = null
    var onPlaybackStateChanged: ((Boolean) -> Unit)? = null
    var onServiceUpdate: (() -> Unit)? = null // New callback for the Background Service
    var onFFTData: ((List<Float>) -> Unit)? = null

    fun getCurrentTrack(): AudioTrack? = if (tracks.isNotEmpty()) tracks[currentIndex] else null

    private fun prepare(index: Int) {
        mediaPlayer?.release()
        currentIndex = index

        val track = tracks[currentIndex]
        val afd = context.assets.openFd(track.assetPath)

        mediaPlayer = MediaPlayer()
        mediaPlayer?.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
        mediaPlayer?.prepare()

        mediaPlayer?.setOnCompletionListener { playNext() }
        mediaPlayer?.audioSessionId?.let { onSessionReady?.invoke(it) }

        onTrackChanged?.invoke(track)
        onServiceUpdate?.invoke()
    }

    fun play(index: Int = currentIndex) {
        prepare(index)
        mediaPlayer?.start()
        startVisualizer(mediaPlayer?.audioSessionId ?: -1)
        onPlaybackStateChanged?.invoke(true)
        startForegroundService()
        onServiceUpdate?.invoke()
    }

    fun resume() {
        mediaPlayer?.start()
        startVisualizer(mediaPlayer?.audioSessionId ?: -1)
        onPlaybackStateChanged?.invoke(true)
        onServiceUpdate?.invoke()
    }

    fun pause() {
        mediaPlayer?.pause()
        onPlaybackStateChanged?.invoke(false)
        onServiceUpdate?.invoke()
    }

    fun playNext() {
        currentIndex = if (currentIndex < tracks.size - 1) currentIndex + 1 else 0
        play(currentIndex)
    }

    fun playPrevious() {
        currentIndex = if (currentIndex > 0) currentIndex - 1 else tracks.size - 1
        play(currentIndex)
    }

    fun seekTo(position: Int) {
        mediaPlayer?.seekTo(position)
        onServiceUpdate?.invoke() // Updates the seekbar in the notification
    }

    fun getCurrentPosition(): Int = mediaPlayer?.currentPosition ?: 0
    fun getDuration(): Int = mediaPlayer?.duration ?: 0
    fun isPlaying(): Boolean = mediaPlayer?.isPlaying ?: false
    fun getAudioSessionId(): Int = mediaPlayer?.audioSessionId ?: -1

    private fun startForegroundService() {
        val intent = Intent(context, MusicService::class.java)
        ContextCompat.startForegroundService(context, intent)
    }

    fun release() {
        releaseVisualizer()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    fun refreshVisualizer() {
        startVisualizer(mediaPlayer?.audioSessionId ?: -1)
    }

    private fun startVisualizer(audioSessionId: Int) {
        releaseVisualizer()
        if (audioSessionId <= 0) {
            Log.w("codmLog", "Visualizer skipped: invalid audioSessionId=$audioSessionId")
            return
        }
        try {
            visualizer = Visualizer(audioSessionId).apply {
//                captureSize = Visualizer.getCaptureSizeRange().maxOrNull()
                captureSize = Visualizer.getCaptureSizeRange()[1]
                    ?: Visualizer.getCaptureSizeRange()[0]
                setDataCaptureListener(
                    object : Visualizer.OnDataCaptureListener {
                        override fun onWaveFormDataCapture(
                            visualizer: Visualizer,
                            waveform: ByteArray,
                            samplingRate: Int
                        ) {
                            // No-op (FFT only)
                        }
                        override fun onFftDataCapture(
                            visualizer: Visualizer,
                            fft: ByteArray,
                            samplingRate: Int
                        ) {
                            val n = fft.size / 2
                            val magnitudes = FloatArray(n)

                            var j = 0
                            var i = 2

                            while (i < fft.size) {

                                val re = fft[i].toInt()
                                val im = fft[i + 1].toInt()

                                val magnitude = sqrt((re * re + im * im).toFloat())

                                // Better audio scaling
                                magnitudes[j] = (20 * log10(magnitude + 1f))

                                i += 2
                                j++
                            }

                            val grouped = processFFT(
                                magnitudes,
                                CommonUtils.barCount
                            )
                            Log.d("codmLog", "FFT magnitudes: $grouped")
                            onFFTData?.invoke(grouped)
                        }
                    },
                    /// Minimum delay between capture cycles.
                    Visualizer.getMaxCaptureRate() / CommonUtils.updateFrequency,
                    false,
                    true
                )
                enabled = true
            }
        } catch (e: Exception) {
            Log.e("codmLog", "Visualizer init failed: ${e.message}")
            releaseVisualizer()
        }
    }
    private fun processFFT(
        fftMagnitudes: FloatArray,
        barCount: Int
    ): List<Float> {

        if (fftMagnitudes.isEmpty()) return emptyList()

        val step = fftMagnitudes.size / barCount
        val grouped = MutableList(barCount) { 0f }

        for (b in 0 until barCount) {

            var sum = 0f

            for (i in 0 until step) {
                val index = b * step + i
                if (index < fftMagnitudes.size) {
                    sum += fftMagnitudes[index]
                }
            }

            grouped[b] = sum / step
        }
        return grouped
    }

    private fun releaseVisualizer() {
        try {
            visualizer?.release()
        } catch (_: Exception) {
        }
        visualizer = null
    }
}