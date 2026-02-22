package com.example.musicplayer.roundKnobView

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.example.musicplayer.R
import kotlin.math.atan2
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class RoundKnobView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var angle = -135f
    private val minAngle = -135f
    private val maxAngle = 135f

    private var percent = 0f

    var onValueChange: ((Float) -> Unit)? = null

    private val knobPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.knob_color)
        style = Paint.Style.FILL
    }

    private val indicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.indicator_color)
        style = Paint.Style.FILL
    }

    private val dotInactivePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.dot_inactive_color)
        style = Paint.Style.FILL
    }

    private val dotActivePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.dot_active_color)
        style = Paint.Style.FILL
    }

    private val totalDots = 15
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val cx = width / 2f
        val cy = height / 2f

        val dotRadius = 8f
        val dotSpacing = 30f

        // Leave space for dots
        val knobRadius = min(cx, cy) - dotSpacing - dotRadius

        // Draw outer dots
        for (i in 0 until totalDots) {

            val dotAngle = minAngle +
                    i * (maxAngle - minAngle) / (totalDots - 1)

            val rad = Math.toRadians(dotAngle.toDouble())

            val x = (cx + (knobRadius + dotSpacing) * cos(rad)).toFloat()
            val y = (cy + (knobRadius + dotSpacing) * sin(rad)).toFloat()

            val activeDots = ceil(percent * totalDots).toInt()

            val paint = if (i < activeDots)
                dotActivePaint
            else
                dotInactivePaint

            canvas.drawCircle(x, y, dotRadius, paint)
        }

        canvas.drawCircle(cx, cy, knobRadius, knobPaint)

        //  Draw moving red indicator

        val rad = Math.toRadians(angle.toDouble())

        val indicatorRadius = 16f
        val insideGap = 40f

      // Move inside the knob edge
        val indicatorDistance = knobRadius - insideGap

        val ix = (cx + indicatorDistance * cos(rad)).toFloat()
        val iy = (cy + indicatorDistance * sin(rad)).toFloat()

        canvas.drawCircle(ix, iy, indicatorRadius, indicatorPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {

        val dx = event.x - width / 2f
        val dy = event.y - height / 2f

        var newAngle = Math.toDegrees(atan2(dy, dx).toDouble()).toFloat()

        if (newAngle < -180) newAngle += 360f

        if (newAngle in minAngle..maxAngle) {
            angle = newAngle

            percent = (angle - minAngle) / (maxAngle - minAngle)

            onValueChange?.invoke(percent)
            invalidate()
        }

        return true
    }

    fun setPercent(value: Float) {
        percent = value.coerceIn(0f, 1f)
        angle = minAngle + percent * (maxAngle - minAngle)
        invalidate()
    }
}
