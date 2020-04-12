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
            wrapper.comment.by,
            HtmlCompat.fromHtml(wrapper.comment.text, FROM_HTML_MODE_LEGACY).toString(),
            coreDataMapper.time(wrapper.comment.time),
            wrapper.depth
        )
}
