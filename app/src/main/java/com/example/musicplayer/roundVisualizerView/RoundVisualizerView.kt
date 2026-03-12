package com.example.musicplayer.roundVisualizerView

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.example.musicplayer.utils.CommonUtils
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class RoundVisualizerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val barCount = CommonUtils.barCount

    // animation buffers
    private val targetMagnitudes = FloatArray(barCount)
    private val currentMagnitudes = FloatArray(barCount)

    // circle beat animation
    private var beatScale = 1f
    private var beatTarget = 1f

    // smoothing
    private val riseSpeed = 0.9f
    private val decaySpeed = 0.96f

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 8f
        strokeCap = Paint.Cap.ROUND
    }
    // receives FFT audio data
    fun updateFFT(data: List<Float>) {

        val size = min(barCount, data.size)

        var bassEnergy = 0f
        val bassRange = min(6, size)

        for (i in 0 until size) {

            targetMagnitudes[i] = data[i]
            // bass detection
            if (i < bassRange) {
                bassEnergy += data[i]
            }
        }

        if (bassRange > 0) bassEnergy /= bassRange

        // beat pulse
        beatTarget = if (bassEnergy > 110f) 1.12f else 1f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val cx = width / 2f
        val cy = height / 2f

        // smooth beat animation
        beatScale += (beatTarget - beatScale) * 0.18f

        val baseRadius = min(cx, cy) * 0.65f * beatScale
        val angleStep = 360f / barCount

        // animate magnitudes
        for (i in 0 until barCount) {

            val target = targetMagnitudes[i]

            if (target > currentMagnitudes[i]) {
                currentMagnitudes[i] += (target - currentMagnitudes[i]) * riseSpeed
            } else {
                currentMagnitudes[i] *= decaySpeed
            }
        }

        // draw bars
        for (i in 0 until barCount) {

            val angle = Math.toRadians((i * angleStep).toDouble())

            val magnitude = currentMagnitudes[i] * 3f
            val barLength = magnitude.coerceIn(10f, CommonUtils.rayMaxHeight)

            val startX = cx + cos(angle) * baseRadius
            val startY = cy + sin(angle) * baseRadius

            val endX = cx + cos(angle) * (baseRadius + barLength)
            val endY = cy + sin(angle) * (baseRadius + barLength)

            // dynamic color based on energy
            val level = currentMagnitudes[i]

            paint.color = when {
                level < 10f -> Color.parseColor("#34C742")
                level < 20f -> Color.parseColor("#F0EC1D")
                else -> Color.parseColor("#FF3D00")
            }

            canvas.drawLine(
                startX.toFloat(),
                startY.toFloat(),
                endX.toFloat(),
                endY.toFloat(),
                paint
            )
        }

        // continuous animation
        postInvalidateOnAnimation()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {}
}