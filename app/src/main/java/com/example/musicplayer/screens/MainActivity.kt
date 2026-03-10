package com.example.musicplayer.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.musicplayer.MusicPlayerManager
import com.example.musicplayer.R
import com.example.musicplayer.model.AudioTrack
import com.example.musicplayer.repository.MusicRepository
import com.example.musicplayer.utils.TimeUtils
import com.example.musicplayer.viewModel.PlayerViewModel
import com.example.musicplayer.waveform.WaveformExtractor
import com.example.musicplayer.waveform.WaveformView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: PlayerViewModel
    private lateinit var musicPlayerManager: MusicPlayerManager
    private lateinit var tracks: List<AudioTrack>

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Repository
        val repository = MusicRepository(this)
        tracks = repository.getTracks()

        // Player Manager
        musicPlayerManager = MusicPlayerManager(this, tracks)

        // ViewModel
        viewModel = PlayerViewModel(repository, musicPlayerManager)

        // UI references
        val albumArt = findViewById<ImageView>(R.id.albumArt)
        val title = findViewById<TextView>(R.id.title)
        val artist = findViewById<TextView>(R.id.artist)
        val seekBar = findViewById<SeekBar>(R.id.seekBar)
        val time = findViewById<TextView>(R.id.time)
        val playBtn = findViewById<ImageView>(R.id.btnPlay)
        val nextBtn = findViewById<ImageView>(R.id.btnNext)
        val prevBtn = findViewById<ImageView>(R.id.btnPrevious)
        val waveformView = findViewById<WaveformView>(R.id.waveformView)
        val equalizerBtn = findViewById<Button>(R.id.btnEqualizer)

        // Observe Track Changes
        lifecycleScope.launchWhenStarted {
            viewModel.currentTrack.collect { track ->

                track ?: return@collect

                title.text = track.title
                artist.text = track.artist
                seekBar.max = track.duration

                if (track.albumArt != null) {
                    albumArt.setImageBitmap(track.albumArt)
                } else {
                    albumArt.setImageResource(R.drawable.ic_placeholder)
                }

                // Extract waveform
                lifecycleScope.launch {
                    val waveformData = withContext(Dispatchers.IO) {
                        WaveformExtractor.extract(this@MainActivity, track.assetPath)
                    }
                    waveformView.setWaveform(waveformData)
                }
            }
        }

        // Observe Play / Pause state
        lifecycleScope.launchWhenStarted {
            viewModel.isPlaying.collect { playing ->

                if (playing) {
                    playBtn.setImageResource(R.drawable.ic_pause)
                } else {
                    playBtn.setImageResource(R.drawable.ic_play)
                }
            }
        }

        // Observe Progress
        lifecycleScope.launchWhenStarted {
            viewModel.progress.collect { currentPosition ->

                seekBar.progress = currentPosition

                val current = TimeUtils.format(currentPosition)
                val total = TimeUtils.format(seekBar.max)

                time.text = "$current / $total"

                if (seekBar.max > 0) {
                    val percent = currentPosition / seekBar.max.toFloat()
                    waveformView.setProgress(percent)
                }
            }
        }

        // Play / Pause
        playBtn.setOnClickListener {
            viewModel.playPause()
        }

        // Next
        nextBtn.setOnClickListener {
            viewModel.next()
        }

        // Previous
        prevBtn.setOnClickListener {
            viewModel.previous()
        }

        // Seekbar
        seekBar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(sb: SeekBar?, pos: Int, fromUser: Boolean) {
                if (fromUser) viewModel.seekTo(pos)
            }

            override fun onStartTrackingTouch(sb: SeekBar?) {}

            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        // Equalizer
        equalizerBtn.setOnClickListener {
            val intent = Intent(this, EqualizerActivity::class.java)
            intent.putExtra("AUDIO_SESSION_ID", musicPlayerManager.getAudioSessionId())
            startActivity(intent)
        }

        // Start Selected Song
        val startIndex = intent.getIntExtra("TRACK_INDEX", 0)
        viewModel.play(startIndex)
    }

    override fun onDestroy() {
        super.onDestroy()
        musicPlayerManager.release()
    }
}
