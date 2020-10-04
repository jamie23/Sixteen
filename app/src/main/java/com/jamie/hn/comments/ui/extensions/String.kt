package com.jamie.hn.comments.ui.extensions

import android.text.SpannableString
import android.text.Spanned
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import android.view.View
import androidx.core.text.HtmlCompat
import androidx.core.text.getSpans

fun String.fixUrlSpans(urlClickedCallback: (String) -> Unit) =
    fixURLSpans(HtmlCompat.fromHtml(this, HtmlCompat.FROM_HTML_MODE_LEGACY), urlClickedCallback)

private fun fixURLSpans(
    spannable: Spanned,
    urlClickedCallback: (String) -> Unit
): SpannableString {
    val spannableString = SpannableString(spannable)
    spannableString.removeSpan(spannable)

    val urls = spannableString.getSpans<URLSpan>()
    urls.forEach {
        val start = spannableString.getSpanStart(it)
        val end = spannableString.getSpanEnd(it)
        val flags = spannableString.getSpanFlags(it)

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                urlClickedCallback.invoke(it.url)
            }
        }

        spannableString.setSpan(clickableSpan, start, end, flags)
    }
    return spannableString
}
