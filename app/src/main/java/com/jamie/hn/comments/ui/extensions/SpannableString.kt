package com.jamie.hn.comments.ui.extensions

import android.graphics.Typeface
import android.text.SpannableString

fun SpannableString.italiciseQuotes(): SpannableString {
    val indexes = mutableListOf<Int>()

    this.forEachIndexed { index, element ->
        if (element == '>') {
            indexes.add(index)
        }
    }

    if (indexes.isEmpty()) return this

    indexes.forEach { startIndex ->
        val endIndex = this.indexOf(
            string = "\n",
            startIndex = startIndex
        )
        this.setSpan(Typeface.ITALIC, startIndex, endIndex, 0)
    }

    return this
}

fun SpannableString.removeAppendedNewLines() =
    if (this.isNotEmpty() && this[this.length - 1] == '\n') {
        this.subSequence(0, this.length - 2)
    } else {
        this
    }
