package com.example.musicplayer

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.musicplayer.roundKnobView.RoundKnobView

class EqualizerActivity : AppCompatActivity() {

    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null

    private lateinit var bandSeekBars: List<SeekBar>
    private lateinit var knobBass: RoundKnobView
    private lateinit var knobTreble: RoundKnobView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_equalizer)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val sessionId = intent.getIntExtra("AUDIO_SESSION_ID", -1)

        if (sessionId != -1) {
            equalizer = Equalizer(0, sessionId).apply { enabled = true }
            bassBoost = BassBoost(0, sessionId).apply { enabled = true }
        }

        initViews()
        setupBands()
        setupBassKnob()
        setupTrebleKnob()
        setupPresets()

    }

    private fun initViews() {
        bandSeekBars = listOf(
            findViewById(R.id.seek60),
            findViewById(R.id.seek230),
            findViewById(R.id.seek1k),
            findViewById(R.id.seek35k),
            findViewById(R.id.seek10k)
        )

        knobBass = findViewById(R.id.knobBass)
        knobTreble = findViewById(R.id.knobTreble)
    }

    //  Bands
    private fun setupBands() {

        val eq = equalizer ?: return
        val minLevel = eq.bandLevelRange[0]
        val maxLevel = eq.bandLevelRange[1]
        val range = maxLevel - minLevel

        for (i in bandSeekBars.indices) {

            if (i >= eq.numberOfBands) break

            val seekBar = bandSeekBars[i]
            seekBar.max = range
            seekBar.progress = range / 2

            seekBar.setOnSeekBarChangeListener(object :
                SeekBar.OnSeekBarChangeListener {

                override fun onProgressChanged(
                    sb: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    if (fromUser) {
                        val level = (progress + minLevel).toShort()
                        eq.setBandLevel(i.toShort(), level)
                    }
                }

                override fun onStartTrackingTouch(sb: SeekBar?) {}
                override fun onStopTrackingTouch(sb: SeekBar?) {}
            })
        }
    }

    // Knob
    private fun setupBassKnob() {

        knobBass.onValueChange = { percent ->
            val strength = (percent * 1000).toInt().coerceIn(0, 1000)
            bassBoost?.setStrength(strength.toShort())
        }

        // Sync current bass strength
        bassBoost?.let {
            val current = it.roundedStrength
            val percent = current / 1000f
            knobBass.setPercent(percent)
        }
    }

    //  Treble Knob
    private fun setupTrebleKnob() {

        val eq = equalizer ?: return
        val minLevel = eq.bandLevelRange[0]
        val maxLevel = eq.bandLevelRange[1]

        knobTreble.onValueChange = { percent ->

            val level = (minLevel + percent * (maxLevel - minLevel)).toInt()
            val lastBand = (eq.numberOfBands - 1).toShort()

            eq.setBandLevel(lastBand, level.toShort())

            // update vertical slider too
            val index = lastBand.toInt()
            if (index < bandSeekBars.size) {
                bandSeekBars[index].progress = level - minLevel
            }
        }
    }

    // Presets
    private fun setupPresets() {

        val spinner = findViewById<Spinner>(R.id.presetSpinner)

        val presets = listOf("Flat", "Rock", "Jazz", "Classical", "Pop")

        val adapter = ArrayAdapter(
            this,
            R.layout.item_spinner_preset,
            presets
        )

        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown)

        spinner.adapter = adapter

        spinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    applyPreset(presets[position])
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
    }

    private fun applyPreset(preset: String) {

        val eq = equalizer ?: return
        val min = eq.bandLevelRange[0]
        val max = eq.bandLevelRange[1]

        val presetLevels = when (preset) {

            "Rock" -> listOf(0.6, 0.8, 0.5, 0.7, 0.9)
            "Jazz" -> listOf(0.4, 0.6, 0.7, 0.6, 0.4)
            "Classical" -> listOf(0.3, 0.5, 0.6, 0.5, 0.3)
            "Pop" -> listOf(0.5, 0.7, 0.6, 0.7, 0.5)
            else -> listOf(0.5, 0.5, 0.5, 0.5, 0.5)
        }

        for (i in 0 until eq.numberOfBands) {

            val percent = presetLevels.getOrElse(i) { 0.5 }
            val level = (min + percent * (max - min)).toInt()

            eq.setBandLevel(i.toShort(), level.toShort())

            if (i < bandSeekBars.size) {
                bandSeekBars[i].progress = level - min
            }
        }

        // Sync Treble Knob (last band)
        val lastBandPercent =
            presetLevels.getOrElse(eq.numberOfBands - 1) { 0.5 }

        knobTreble.setPercent(lastBandPercent.toFloat())

        knobBass.setPercent(0.5f)
    }

    override fun onDestroy() {
        super.onDestroy()
        equalizer?.release()
        bassBoost?.release()
    }
}