package ru.skillbranch.skillarticles.ui.custom

import android.text.TextPaint
import android.text.style.BackgroundColorSpan
import androidx.core.graphics.ColorUtils

open class SearchSpan(
    backgroundColor: Int,
    private val foregroundColor: Int
) : BackgroundColorSpan(backgroundColor) {
    private val alpha by lazy {
        ColorUtils.setAlphaComponent(backgroundColor, 160)
    }

    override fun updateDrawState(textPaint: TextPaint) {
        textPaint.bgColor = alpha
        textPaint.color = foregroundColor
    }
}
