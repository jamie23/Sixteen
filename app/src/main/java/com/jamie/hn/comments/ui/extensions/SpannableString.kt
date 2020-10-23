package com.jamie.hn.comments.ui.extensions

import android.graphics.Typeface
import android.text.SpannableString

fun SpannableString.italiciseQuotes(): SpannableString {
    val indexes = mutableListOf<Int>()

    this.forEachIndexed { index, element ->
        // TODO: Fix this which breaks this code:
        //Maybe, but one big difference I notice between the two is that a search for <brand> on Google often has the top result as an ad for that brand, whereas on DDG it will be the same result but not an ad. It seems that brands don't have to “defend their turf” on DDG by buying ads on their own name as they do for Google.

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
        try {
            this.setSpan(Typeface.ITALIC, startIndex, endIndex, 0)
        } catch (e: IndexOutOfBoundsException) {
            println(e)
        }
    }

    return this
}

fun SpannableString.removeAppendedNewLines() =
    if (this.isNotEmpty() && this[this.length - 1] == '\n') {
        this.subSequence(0, this.length - 2)
    } else {
        this
    }
