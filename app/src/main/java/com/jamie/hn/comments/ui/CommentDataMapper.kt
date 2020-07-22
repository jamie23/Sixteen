package com.jamie.hn.comments.ui

import androidx.core.text.HtmlCompat
import androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY
import com.jamie.hn.comments.domain.model.CommentWithDepth
import com.jamie.hn.core.ui.CoreDataMapper

class CommentDataMapper(
    private val coreDataMapper: CoreDataMapper
) {

    fun toCommentViewItem(wrapper: CommentWithDepth, collapseCallback: (Int) -> Unit) =
        CommentViewItem(
            author = wrapper.comment.author,
            text = htmlTextParser(wrapper.comment.text).removeAppendedNewLines(),
            time = coreDataMapper.time(wrapper.comment.time),
            depth = wrapper.depth,
            showTopDivider = wrapper.depth == 0,
            longClickCommentListener = getLongClickListener(collapseCallback)
        )

    private fun String.removeAppendedNewLines() =
        if (this.isNotEmpty() && this[this.length - 1] == '\n') {
            this.subSequence(0, this.length - 2).toString()
        } else {
            this
        }

    fun htmlTextParser(text: String) =
        HtmlCompat.fromHtml(text, FROM_HTML_MODE_LEGACY).toString()

    private fun getLongClickListener(
        collapseCallback: (Int) -> Unit
    ): (position: Int) -> Unit {
        return collapseCallback
    }
}
