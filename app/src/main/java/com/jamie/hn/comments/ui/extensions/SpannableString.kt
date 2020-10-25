package com.jamie.hn.comments.ui.extensions

import android.graphics.Typeface.ITALIC
import android.text.SpannableString
import android.text.style.StyleSpan

fun SpannableString.italiciseQuotes(): SpannableString {
    val indexes = mutableListOf<Int>()
    var containsSmaller = false

    this.forEachIndexed { index, element ->
        when {
            element == '>' && !containsSmaller -> indexes.add(index)
            // If people wrap text in <> we do not want to italicise
            element == '<' -> containsSmaller = true
            element == '\n' -> containsSmaller = false
        }
    }

    if (indexes.isEmpty()) return this

    indexes.forEach { startIndex ->
        val endIndex = this.indexOf(
            string = "\n",
            startIndex = startIndex
        )

        // User has the quote on the same line as his reply so do not italicise
        if (endIndex == -1) return this

        this.setSpan(StyleSpan(ITALIC), startIndex, endIndex, 0)
    }

    return this
}

fun SpannableString.removeAppendedNewLines() =
    if (this.isNotEmpty() && this[this.length - 1] == '\n') {
        this.subSequence(0, this.length - 2)
    } else {
        this
    }
