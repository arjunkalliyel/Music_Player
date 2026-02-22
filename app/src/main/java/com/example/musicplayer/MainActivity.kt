package com.example.musicplayer

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

        val repository = MusicRepository(this)
        tracks = repository.getTracks()

        musicPlayerManager = MusicPlayerManager(this, tracks)
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

        musicPlayerManager.onPlaybackStateChanged = { isPlaying ->
            runOnUiThread {
                if (isPlaying) {
                    playBtn.setImageResource(R.drawable.ic_pause)
                } else {
                    playBtn.setImageResource(R.drawable.ic_play)
                }
            }
        }

        //  Update UI when track changes
        musicPlayerManager.onTrackChanged = { track ->

            title.text = track.title
            artist.text = track.artist
            seekBar.max = track.duration

            if (track.albumArt != null) {
                albumArt.setImageBitmap(track.albumArt)
            } else {
                albumArt.setImageResource(R.drawable.ic_placeholder)
            }

            //  Extract waveform in background
            lifecycleScope.launch {
                val waveformData = withContext(Dispatchers.IO) {
                    WaveformExtractor.extract(this@MainActivity, track.resId)
                }
                waveformView.setWaveform(waveformData)
            }
        }

        //  Time progress
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
            if (musicPlayerManager.isPlaying()) {
                musicPlayerManager.pause()
            } else {
                musicPlayerManager.resume()
            }
        }

        nextBtn.setOnClickListener {
            musicPlayerManager.playNext()
        }

        prevBtn.setOnClickListener {
            musicPlayerManager.playPrevious()
        }

        seekBar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(sb: SeekBar?, pos: Int, fromUser: Boolean) {
                if (fromUser) musicPlayerManager.seekTo(pos)
            }

            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        findViewById<Button>(R.id.btnEqualizer).setOnClickListener {
            val intent = Intent(this, EqualizerActivity::class.java)
            intent.putExtra("AUDIO_SESSION_ID", musicPlayerManager.getAudioSessionId())
            startActivity(intent)
        }

        if (tracks.isNotEmpty()) {
            musicPlayerManager.play(0)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        musicPlayerManager.release()
    }
}
