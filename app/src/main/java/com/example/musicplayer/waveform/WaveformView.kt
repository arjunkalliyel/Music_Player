package com.example.musicplayer.waveform

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.example.musicplayer.R

class WaveformView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var waveformData: List<Int> = emptyList()
    private var progressPercent = 0f

    private val playedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.wave_color)
        strokeWidth = 4f
    }

    private val unplayedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.unplay_color)
        strokeWidth = 4f
    }

    fun setWaveform(data: List<Int>) {
        waveformData = data
        invalidate()
    }

    fun setProgress(percent: Float) {
        progressPercent = percent.coerceIn(0f, 1f)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (waveformData.isEmpty()) return

        val centerY = height / 2f
        val widthPerBar = width.toFloat() / waveformData.size

        waveformData.forEachIndexed { index, amplitude ->

            val x = index * widthPerBar
            val barHeight = (amplitude / 32767f) * height

            val startY = centerY - barHeight / 2
            val endY = centerY + barHeight / 2

            val paint =
                if (index <= waveformData.size * progressPercent)
                    playedPaint
                else
                    unplayedPaint

            canvas.drawLine(x, startY, x, endY, paint)
        }
    }
}