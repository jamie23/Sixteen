package com.jamie.hn.comments.ui

import androidx.core.text.HtmlCompat
import androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY
import com.jamie.hn.comments.ui.repository.model.CommentCurrentState
import com.jamie.hn.core.ui.CoreDataMapper

class CommentDataMapper(
    private val coreDataMapper: CoreDataMapper
) {

    fun toCommentViewItem(wrapper: CommentCurrentState, collapseCallback: (Int) -> Unit) =
        CommentViewItem(
            author = wrapper.comment.comment.author,
            text = htmlTextParser(wrapper.comment.comment.text).removeAppendedNewLines(),
            time = coreDataMapper.time(wrapper.comment.comment.time),
            depth = wrapper.comment.depth,
            showTopDivider = wrapper.comment.depth == 0,
            longClickCommentListener = collapseCallback,
            id = wrapper.comment.id
        )

    private fun String.removeAppendedNewLines() =
        if (this.isNotEmpty() && this[this.length - 1] == '\n') {
            this.subSequence(0, this.length - 2).toString()
        } else {
            this
        }

    fun htmlTextParser(text: String) =
        HtmlCompat.fromHtml(text, FROM_HTML_MODE_LEGACY).toString()
}
