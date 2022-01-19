package ru.skillbranch.skillarticles.ui.custom.markdown

import android.text.Spannable
import android.util.Log
import androidx.core.text.getSpans
import ru.skillbranch.skillarticles.ui.custom.SearchFocusSpan
import ru.skillbranch.skillarticles.ui.custom.SearchSpan

interface IMarkdownView {
    var fontSize: Float
    val spannableContent: Spannable

    fun renderSearchResult(
        results: List<Pair<Int, Int>>,
        offset: Int
    ) {
        clearSearchResult()
        val offsetResult = results.map { (start, end) -> start.minus(offset) to end.minus(offset) }
        runCatching {
            offsetResult.forEach { (start, end) ->
                spannableContent.setSpan(
                    SearchSpan(),
                    start,
                    end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }.onFailure {
            it.printStackTrace()
            Log.e(javaClass.canonicalName, it.message.toString())
        }
    }

    fun renderSearchPosition(searchPositions: Pair<Int, Int>, offset: Int) {
        spannableContent.getSpans<SearchFocusSpan>().forEach(spannableContent::removeSpan)
        spannableContent.setSpan(
            SearchSpan(),
            searchPositions.first.minus(offset),
            searchPositions.second.minus(offset),
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }

    fun clearSearchResult() {
        spannableContent.getSpans<SearchFocusSpan>().forEach(spannableContent::removeSpan)
    }
}