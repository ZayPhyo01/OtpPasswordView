package com.zayphyo.passwordview

import android.graphics.Canvas
import android.graphics.Paint
import android.text.style.ReplacementSpan

class TranslateXSpans : ReplacementSpan() {

    var tx: Float = 10.0f
    override fun getSize(
        paint: Paint,
        text: CharSequence?,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt?
    ): Int {
        return paint.measureText(text, start, end).toInt()
    }

    override fun draw(
        canvas: Canvas,
        text: CharSequence?,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        y: Int,
        bottom: Int,
        paint: Paint
    ) {
        text?.let {
            canvas.translate(10f, 0f)
            canvas.drawText(it.toString(), 0, end, x - tx, y.toFloat(), paint)
        }

    }
}