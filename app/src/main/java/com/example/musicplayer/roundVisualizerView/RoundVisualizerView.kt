package com.example.musicplayer.roundVisualizerView

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.SweepGradient
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class RoundVisualizerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val barCount = 48

    // animation
    private val targetMagnitudes = FloatArray(barCount)
    private val currentMagnitudes = FloatArray(barCount)
    // circle expansion
    private var beatScale = 1f
    private var beatTarget = 1f

    // smoothing factors
    private val riseSpeed = 0.90f
    private val decaySpeed = 0.98f

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 8f
        strokeCap = Paint.Cap.ROUND
    }
    // receives FFT audio data
    fun updateFFT(data: List<Float>) {

        val size = min(barCount, data.size)

        var bassEnergy = 0f

        for (i in 0 until size) {

            // target values
            targetMagnitudes[i] = data[i]
            // bass detection
            if (i < 6) {
                bassEnergy += data[i]
            }
        }

        bassEnergy /= 6f

        // stronger beat pulse
        beatTarget = if (bassEnergy > 110) 1.10f else 1f
    }
    //visualizer
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val cx = width / 2f
        val cy = height / 2f

        // smooth beat animation
        beatScale += (beatTarget - beatScale) * 0.18f
        //Circle Radius
        val baseRadius = min(cx, cy) * 0.65f * beatScale
        val angleStep = 360f / barCount

        // animate magnitudes target larger
        for (i in 0 until barCount) {

            val target = targetMagnitudes[i]

            if (target > currentMagnitudes[i]) {
                // fast rise Bars grow quickly.
                currentMagnitudes[i] += (target - currentMagnitudes[i]) * riseSpeed
            } else {
                // slow decay Bars shrink slowly.
                currentMagnitudes[i] *= decaySpeed
            }

        }

        for (i in 0 until barCount) {

            val angle = Math.toRadians((i * angleStep).toDouble())

//            val magnitude = currentMagnitudes[i] * 45f
            val magnitude = currentMagnitudes[i] * 3.5f
            val barLength = magnitude.coerceIn(20f, 220f)
            // FFT value → pixel height.
            val startX = cx + cos(angle) * baseRadius
            val startY = cy + sin(angle) * baseRadius

            val endX = cx + cos(angle) * (baseRadius + barLength)
            val endY = cy + sin(angle) * (baseRadius + barLength)
            //This draws one visualizer bar.
            canvas.drawLine(
                startX.toFloat(),
                startY.toFloat(),
                endX.toFloat(),
                endY.toFloat(),
                paint
            )
        }

        // keeps animation running smoothly
        postInvalidateOnAnimation()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {

        val cx = w / 2f
        val cy = h / 2f

        paint.shader = SweepGradient(
            cx,
            cy,
            intArrayOf(
                Color.parseColor("#FF3D00"),
                Color.parseColor("#FF6D00"),
                Color.parseColor("#FFAB00"),
                Color.parseColor("#FF3D00")
            ),
            null
        )
    }
}