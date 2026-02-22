package com.example.musicplayer

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import com.example.musicplayer.model.AudioTrack

class MusicPlayerManager(
    private val context: Context,
    private val tracks: List<AudioTrack>
) {

    private var mediaPlayer: MediaPlayer? = null
    private var currentIndex = 0

    var onTrackChanged: ((AudioTrack) -> Unit)? = null
    var onSessionReady: ((Int) -> Unit)? = null
    var onPlaybackStateChanged: ((Boolean) -> Unit)? = null

    private fun prepare(index: Int) {
        release()

        currentIndex = index

        val track = tracks[currentIndex]
        val uri = Uri.parse("android.resource://${context.packageName}/${track.resId}")

        mediaPlayer = MediaPlayer.create(context, uri)

        mediaPlayer?.setOnCompletionListener {
            playNext()
        }

        //  Notify Equalizer when session is ready
        mediaPlayer?.audioSessionId?.let {
            onSessionReady?.invoke(it)
        }

        onTrackChanged?.invoke(track)
    }

    fun play(index: Int = currentIndex) {
        prepare(index)
        mediaPlayer?.start()
        onPlaybackStateChanged?.invoke(true)

    }

    fun resume() {
        mediaPlayer?.start()
        onPlaybackStateChanged?.invoke(true)
    }

    fun pause() {
        mediaPlayer?.pause()
        onPlaybackStateChanged?.invoke(false)
    }

    fun playNext() {
        if (currentIndex < tracks.size - 1) {
            play(currentIndex + 1)
        } else {
            play(0)
        }
    }

    fun playPrevious() {
        if (currentIndex > 0) {
            play(currentIndex - 1)
        } else {
            play(tracks.size - 1)
        }
    }

    fun seekTo(position: Int) {
        mediaPlayer?.seekTo(position)
    }

    fun getCurrentPosition(): Int {
        return mediaPlayer?.currentPosition ?: 0
    }

    fun getDuration(): Int {
        return mediaPlayer?.duration ?: 0
    }

    fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying ?: false
    }

    fun getAudioSessionId(): Int {
        return mediaPlayer?.audioSessionId ?: -1
    }

    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
}