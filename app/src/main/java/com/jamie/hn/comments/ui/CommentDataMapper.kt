package com.jamie.hn.comments.ui

import com.jamie.hn.comments.Comment
import com.jamie.hn.core.ui.CoreDataMapper

class CommentDataMapper(
    private val coreDataMapper: CoreDataMapper
) {

    fun toCommentViewItem(comment: Comment) =
        CommentViewItem(
            comment.by,
            comment.text,
            coreDataMapper.time(comment.time)
        )
}
