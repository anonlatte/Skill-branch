package ru.skillbranch.skillarticles.ui.custom.markdown

import android.content.Context
import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.URLSpan
import androidx.core.content.ContextCompat.getDrawable
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.data.repositories.Element
import ru.skillbranch.skillarticles.data.repositories.MarkdownParser
import ru.skillbranch.skillarticles.extensions.attrValue
import ru.skillbranch.skillarticles.extensions.dpToPx
import ru.skillbranch.skillarticles.ui.custom.spans.*

class MarkdownBuilder(context: Context) {
    private val colorSecondary = context.attrValue(R.attr.colorSecondary)
    private val colorPrimary = context.attrValue(R.attr.colorPrimary)
    private val colorOnSurface = context.attrValue(R.attr.colorOnSurface)
    private val colorSurface = context.attrValue(R.attr.colorSurface)
    private val opacityColorSurface = context.getColor(R.color.opacity_color_surface)
    private val colorDivider = context.getColor(R.color.color_divider)
    private val gap: Float = context.dpToPx(8)
    private val bulletRadius: Float = context.dpToPx(4)
    private val strikeWidth: Float = context.dpToPx(4)
    private val headerMarginTop: Float = context.dpToPx(12)
    private val headerMarginBottom: Float = context.dpToPx(8)
    private val ruleWidth: Float = context.dpToPx(2)
    private val cornerRadius: Float = context.dpToPx(8)
    private val linkIcon = getDrawable(context, R.drawable.ic_baseline_link_24)!!

    fun markdownToSpan(content: String) = buildSpannedString {
        MarkdownParser.parse(content).elements.forEach { buildElement(it, this) }
    }

    private fun buildElement(
        element: Element, builder: SpannableStringBuilder
    ): CharSequence = builder.apply {
        when (element) {
            // is Element.BlockCode -> {}
            is Element.Bold -> {
                inSpans(StyleSpan(Typeface.BOLD)) {
                    element.elements.forEach { buildElement(it, builder) }
                }
            }
            is Element.Header -> {
                inSpans(
                    HeaderSpan(
                        element.level,
                        colorPrimary,
                        colorDivider,
                        headerMarginTop,
                        headerMarginBottom
                    )
                ) {
                    append(element.text)
                }
            }
            // is Element.Image -> {}
            is Element.InlineCode -> {
                inSpans(
                    InlineCodeSpan(colorOnSurface, opacityColorSurface, cornerRadius, gap),
                ) {
                    append(element.text)
                }
            }
            is Element.Italic -> {
                inSpans(StyleSpan(Typeface.ITALIC)) {
                    element.elements.forEach { buildElement(it, builder) }
                }
            }
            is Element.Link -> {
                inSpans(
                    IconLinkSpan(linkIcon, gap, colorPrimary, strikeWidth),
                    URLSpan(element.link)
                ) {
                    append(element.text)
                }
            }
            // is Element.OrderedListItem -> {}
            is Element.Quote -> {
                inSpans(
                    BlockquotesSpan(gap, ruleWidth, colorSecondary),
                    StyleSpan(Typeface.ITALIC)
                ) {
                    element.elements.forEach { buildElement(it, builder) }
                }
            }
            is Element.Rule -> {
                inSpans(
                    HorizontalRuleSpan(ruleWidth, colorDivider),
                ) {
                    append(element.text)
                }
            }
            is Element.Strike -> {
                inSpans(StrikethroughSpan()) {
                    element.elements.forEach { buildElement(it, builder) }
                }
            }
            is Element.Text -> {
                append(element.text)
            }
            is Element.UnorderedListItem -> {
                inSpans(UnorderedListSpan(gap, bulletRadius, colorSecondary)) {
                    element.elements.forEach { buildElement(it, builder) }
                }
            }
            else -> {
                append(element.text)
            }
        }
    }
}