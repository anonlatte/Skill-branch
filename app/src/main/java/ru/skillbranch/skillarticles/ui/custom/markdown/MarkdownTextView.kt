package ru.skillbranch.skillarticles.ui.custom.markdown

import android.content.Context
import android.graphics.Canvas
import android.text.Spannable
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.graphics.withTranslation
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.extensions.attrValue
import ru.skillbranch.skillarticles.extensions.dpToPx

class MarkdownTextView @JvmOverloads constructor(
    context: Context,
    fontSize: Float,
    defStyleAttr: Boolean,
    private val isSizeDepend: Boolean = true
) : AppCompatTextView(context, null, 0), IMarkdownView {
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    var searchBgHelper = SearchBgHelper(context) { top, bottom ->

    }

    override var fontSize: Float = fontSize
        set(value) {
            textSize = value
            field = value
        }
    override val spannableContent: Spannable
        get() = text as Spannable

    private val color = context.attrValue(R.attr.colorOnBackground)

    init {
        setTextColor(color)
        textSize = fontSize
        movementMethod = LinkMovementMethod()
    }

    override fun onDraw(canvas: Canvas) {
        if (text is Spanned && layout != null) {
            canvas.withTranslation(totalPaddingLeft.toFloat(), totalPaddingTop.toFloat()) {
                searchBgHelper.draw(canvas, text as Spanned, layout)
            }
        }
        super.onDraw(canvas)
    }

    override fun setTextSize(size: Float) {
        Log.e(javaClass.canonicalName, "set text size = $size")
        if (isSizeDepend) {
            setLineSpacing(
                context.dpToPx(if (size == 14f) 8 else 10),
                1f
            )
        }
        super.setTextSize(size)
    }
}