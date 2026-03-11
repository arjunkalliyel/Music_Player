package com.example.musicplayer

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import androidx.core.content.ContextCompat
import com.example.musicplayer.model.AudioTrack
import com.example.musicplayer.service.MusicService

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
                instance ?: MusicPlayerManager(context.applicationContext, tracks).also { instance = it }
            }
        }
    }

    private var mediaPlayer: MediaPlayer? = null
    private var currentIndex = 0

    var onTrackChanged: ((AudioTrack) -> Unit)? = null
    var onSessionReady: ((Int) -> Unit)? = null
    var onPlaybackStateChanged: ((Boolean) -> Unit)? = null
    var onServiceUpdate: (() -> Unit)? = null // New callback for the Background Service

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
        onPlaybackStateChanged?.invoke(true)
        startForegroundService()
        onServiceUpdate?.invoke()
    }

    fun resume() {
        mediaPlayer?.start()
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
        mediaPlayer?.release()
        mediaPlayer = null
    }
}