package com.jamie.hn.comments.ui

data class CommentViewItem(
    val author: String,
    val text: String,
    val time: String,
    val depth: Int,
    val showTopDivider: Boolean,
    val longClickCommentListener: (position: Int) -> Unit,
    val id: Int
)
