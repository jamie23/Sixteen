package com.jamie.hn.comments.ui

import androidx.core.text.HtmlCompat
import androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY
import com.jamie.hn.comments.domain.CommentWithDepth
import com.jamie.hn.core.ui.CoreDataMapper

class CommentDataMapper(
    private val coreDataMapper: CoreDataMapper
) {

    fun toCommentViewItem(wrapper: CommentWithDepth) =
        CommentViewItem(
            author = wrapper.comment.by,
            text = HtmlCompat.fromHtml(wrapper.comment.text, FROM_HTML_MODE_LEGACY).toString()
                .removeAppendedNewLines(),
            time = coreDataMapper.time(wrapper.comment.time),
            depth = wrapper.depth,
            showTopDivider = wrapper.depth == 0
        )

    private fun String.removeAppendedNewLines() =
        if (this.isNotEmpty() && this[this.length - 1] == '\n') {
            this.subSequence(0, this.length - 2).toString()
        } else {
            this
        }
}

