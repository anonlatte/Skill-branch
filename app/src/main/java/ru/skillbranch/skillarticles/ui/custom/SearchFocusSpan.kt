package ru.skillbranch.skillarticles.ui.custom

import android.text.TextPaint

class SearchFocusSpan(
    private val bgColor: Int,
    private val foregroundColor: Int
) : SearchSpan(bgColor, foregroundColor) {
    override fun updateDrawState(textPaint: TextPaint) {
        textPaint.bgColor = bgColor
        textPaint.color = foregroundColor
    }
}
