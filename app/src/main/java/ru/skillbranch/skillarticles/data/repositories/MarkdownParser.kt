package ru.skillbranch.skillarticles.data.repositories

import java.util.regex.Pattern

object MarkdownParser {
    private val LINE_SEPARATOR = System.getProperty("line_separator") ?: "\n"
    private const val UNORDERED_LIST_ITEM_GROUP = "(^[*+-] .+$)"
    private const val HEADER_GROUP = "(^#{1,6} .+?$)"
    private const val QUOTE_GROUP = "(^> .+?$)"
    private const val ITALIC_GROUP = "((?<!\\*)\\*[^*].*?[^*]?\\*(?!\\*)|(?<!_)_[^_].*?[^_]?_(?!_))"
    private const val BOLD_GROUP = "((?<!\\*)\\*{2}[^*].*?[^*]?\\*{2}(?!\\*)|" +
        "(?<!_)_{2}[^_].*?[^_]?_{2}(?!_))"
    private const val STRIKE_GROUP = "((?<!~)~{2}[^*].*?[^*]?~{2}(?!~))"
    private const val RULE_GROUP = "(^[*-_]{3}$)"
    private const val INLINE_CODE_GROUP = "((?<!`)`[^`\\s].*?[^`\\s]?`(?!`))"
    private const val LINK_GROUP = "(\\[[^\\[\\]]*?]\\(.+?\\)|^\\[*?]\\(.*?\\))"
    private const val ORDERED_LIST_ITEM_GROUP = "(^\\d\\. .+$)"
    private const val BLOCK_CODE_GROUP = "(^`{3}[\\s\\S]+?`{3}$)"
    private const val IMAGE_GROUP = "(^!\\[[^\\[\\]]*?\\]\\(.*?\\)$)"
    private val elementsPattern by lazy { Pattern.compile(MARKDOWN_GROUPS, Pattern.MULTILINE) }
    private const val MARKDOWN_GROUPS = "$UNORDERED_LIST_ITEM_GROUP|$HEADER_GROUP|$QUOTE_GROUP" +
        "|$ITALIC_GROUP|$BOLD_GROUP|$STRIKE_GROUP|$RULE_GROUP|$INLINE_CODE_GROUP|$LINK_GROUP" +
        "|$ORDERED_LIST_ITEM_GROUP|$BLOCK_CODE_GROUP|$IMAGE_GROUP"

    fun parse(string: String): List<MarkdownElement> {
        val elements = mutableListOf<Element>()
        elements.addAll(findElements(string))
        return elements.fold(mutableListOf()) { acc, element ->
            val last = acc.lastOrNull()
            when (element) {
                is Element.Image -> {
                    acc.add(MarkdownElement.Image(element, last?.bounds?.second ?: 0))
                }
                is Element.BlockCode -> {
                    acc.add(MarkdownElement.Scroll(element, last?.bounds?.second ?: 0))
                }
                else -> {
                    if (last is MarkdownElement.Text) {
                        last.elements.add(element)
                    } else {
                        acc.add(
                            MarkdownElement.Text(mutableListOf(element), last?.bounds?.second ?: 0)
                        )
                    }
                }
            }
            acc
        }
    }

/*
    fun clear(string: String): String = parse(string).elements.joinToString("") {
        if (it is Element.Text) it.text else it.text.replace(Regex("[_~*]"), "")
    }
*/

    private fun findElements(string: CharSequence): List<Element> {
        val parents = mutableListOf<Element>()
        val matcher = elementsPattern.matcher(string)
        var lastStartIndex = 0
        loop@ while (matcher.find(lastStartIndex)) {
            val startIndex = matcher.start()
            val endIndex = matcher.end()
            if (lastStartIndex < startIndex) {
                parents.add(Element.Text(string.subSequence(lastStartIndex, startIndex)))
            }
            var text: CharSequence
            val groups = 1..12
            var group = -1
            for (gr in groups) {
                if (matcher.group(gr) != null) {
                    group = gr
                    break
                }
            }

            when (group) {
                -1 -> {
                    break@loop
                }
                1 -> {
                    text = string.subSequence(startIndex.plus(2), endIndex)
                    val subs = findElements(text)
                    val element = Element.UnorderedListItem(text, subs)
                    parents.add(element)
                    lastStartIndex = endIndex
                }
                2 -> {
                    val reg = Regex("^#{1,6}").find(string.subSequence(startIndex, endIndex))
                    val level = reg!!.value.length
                    text = string.subSequence(startIndex.plus(level.inc()), endIndex)
                    val element = Element.Header(level, text)
                    parents.add(element)
                    lastStartIndex = endIndex
                }
                3 -> {
                    text = string.subSequence(startIndex.plus(2), endIndex)
                    val subelements = findElements(text)
                    val element = Element.Quote(text, subelements)
                    parents.add(element)
                    lastStartIndex = endIndex
                }
                4 -> {
                    text = string.subSequence(startIndex.inc(), endIndex.dec())
                    val subelements = findElements(text)
                    val element = Element.Italic(text, subelements)
                    parents.add(element)
                    lastStartIndex = endIndex
                }
                5 -> {
                    text = string.subSequence(startIndex.plus(2), endIndex.minus(2))
                    val subelements = findElements(text)
                    val element = Element.Bold(text, subelements)
                    parents.add(element)
                    lastStartIndex = endIndex
                }
                6 -> {
                    text = string.subSequence(startIndex.plus(2), endIndex.minus(2))
                    val subelements = findElements(text)
                    val element = Element.Strike(text, subelements)
                    parents.add(element)
                    lastStartIndex = endIndex
                }
                7 -> {
                    val element = Element.Rule()
                    parents.add(element)
                    lastStartIndex = endIndex
                }
                8 -> {
                    text = string.subSequence(startIndex.inc(), endIndex.dec())
                    val subelements = findElements(text)
                    val element = Element.InlineCode(text, subelements)
                    parents.add(element)
                    lastStartIndex = endIndex
                }
                9 -> {
                    text = string.subSequence(startIndex, endIndex)
                    val (title: String, link: String) = Regex("\\[(.*)]\\((.*)\\)").find(text)!!.destructured
                    val element = Element.Link(link, title)
                    parents.add(element)
                    lastStartIndex = endIndex
                }
                10 -> {
                    text = string.subSequence(startIndex.plus(3), endIndex)
                    val subs = findElements(text)
                    val order = string.subSequence(startIndex, endIndex).let { sequence ->
                        sequence.substring(0, sequence.indexOfLast { !it.isLetter() })
                    }
                    val element = Element.OrderedListItem(order, text, subs)
                    parents.add(element)
                    lastStartIndex = endIndex
                }
                11 -> {
                    text = string.subSequence(startIndex.plus(3), endIndex.minus(3))
                    val subs = findElements(text)
                    val element = Element.BlockCode(text, subs)
                    parents.add(element)
                    lastStartIndex = endIndex
                }
                12 -> {
                    text = string.subSequence(startIndex, endIndex)
                    val (alt: String?, link: String, title: String) = Regex("^\\[([^\\[\\]]*?)?]\\((.*?) \"(.*?)\"\\)$").find(
                        text
                    )!!.destructured
                    val element = Element.Image(alt, link, title)
                    parents.add(element)
                    lastStartIndex = endIndex
                }
            }
        }
        if (lastStartIndex < string.length) {
            val text = string.subSequence(lastStartIndex, string.length)
            parents.add(Element.Text(text))
        }
        return parents
    }
}

data class MarkdownText(val elements: List<Element>)

sealed class MarkdownElement {
    abstract val offset: Int
    val bounds: Pair<Int, Int> by lazy {
        when (this) {
            is Text -> {
                val end = elements.fold(offset) { acc, element ->
                    acc + element.spread().map { it.text.length }.sum()
                }
                offset to end
            }
            is Image -> {
                offset to image.text.length + offset
            }
            is Scroll -> {
                offset to blockCode.text.length + offset
            }
        }
    }

    data class Text(
        val elements: MutableList<Element>,
        override val offset: Int = 0
    ) : MarkdownElement()

    data class Image(
        val image: Element.Image,
        override val offset: Int = 0
    ) : MarkdownElement()

    data class Scroll(
        val blockCode: Element.BlockCode,
        override val offset: Int = 0
    ) : MarkdownElement()
}

sealed class Element {
    abstract val text: CharSequence
    abstract val elements: List<Element>

    data class Text(
        override val text: CharSequence,
        override val elements: List<Element> = emptyList(),
    ) : Element()

    data class UnorderedListItem(
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class Header(
        val level: Int = 1,
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class Quote(
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class Italic(
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class Bold(
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class Strike(
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class Rule(
        override val text: CharSequence = " ",
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class InlineCode(
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class Link(
        val link: String,
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class OrderedListItem(
        val order: String,
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class BlockCode(
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class Image(
        val alt: String?,
        val url: String,
        override val text: CharSequence = " ",
        override val elements: List<Element> = emptyList()
    ) : Element()
}

private fun Element.spread(): List<Element> {
    val elements = mutableListOf<Element>()
    if (this.elements.isNotEmpty()) {
        elements.addAll(this.elements.spread())
    } else {
        elements.add(this)
    }
    return elements
}

private fun List<Element>.spread(): List<Element> {
    val elements = mutableListOf<Element>()
    forEach { elements.addAll(it.spread()) }
    return elements
}

private fun Element.clearContent(): String {
    return StringBuilder().apply {
        val element = this@clearContent
        if (element.elements.isNotEmpty()) {
            append(element.text)
        } else {
            element.elements.forEach { append(it.clearContent()) }
        }
    }.toString()
}

fun List<MarkdownElement>.clearContent(): String {
    return StringBuilder().apply {
        this@clearContent.forEach {
            when (it) {
                is MarkdownElement.Text -> it.elements.forEach { el -> append(el.clearContent()) }
                is MarkdownElement.Image -> append(it.image.clearContent())
                is MarkdownElement.Scroll -> append(it.blockCode.clearContent())
            }
        }
    }.toString()
}
