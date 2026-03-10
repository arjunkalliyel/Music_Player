package com.example.musicplayer.viewModel

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.ViewModel
import com.example.musicplayer.MusicPlayerManager
import com.example.musicplayer.model.AudioTrack
import com.example.musicplayer.repository.MusicRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class PlayerViewModel(
    private val repository: MusicRepository,
    private val playerManager: MusicPlayerManager
) : ViewModel() {

    private val _tracks = MutableStateFlow<List<AudioTrack>>(emptyList())
    val tracks = _tracks.asStateFlow()

    private val _currentTrack = MutableStateFlow<AudioTrack?>(null)
    val currentTrack = _currentTrack.asStateFlow()

    private val _progress = MutableStateFlow(0)
    val progress = _progress.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val handler = Handler(Looper.getMainLooper())

    init {

        _tracks.value = repository.getTracks()

        playerManager.onTrackChanged = { track ->
            _currentTrack.value = track
        }

        playerManager.onPlaybackStateChanged = { playing ->
            _isPlaying.value = playing
        }

        startProgressUpdates()
    }

    fun play(index: Int) {
        playerManager.play(index)
    }

    fun playPause() {
        if (playerManager.isPlaying()) {
            playerManager.pause()
        } else {
            playerManager.resume()
        }
    }

    fun next() = playerManager.playNext()

    fun previous() = playerManager.playPrevious()

    fun seekTo(pos: Int) = playerManager.seekTo(pos)

    private fun startProgressUpdates() {
        handler.post(object : Runnable {
            override fun run() {
                _progress.value = playerManager.getCurrentPosition()
                handler.postDelayed(this, 1000)
            }
        })
    }

    override fun onCleared() {
        super.onCleared()
        playerManager.release()
    }
}
