package com.jamie.hn.comments.ui

import com.jamie.hn.comments.ui.repository.model.CurrentState

// Used by both collapsed and full comment views
data class CommentViewItem(
    val author: String,
    val text: CharSequence,
    val time: String,
    val depth: Int,
    val authorAndHiddenChildren: String,
    val showTopDivider: Boolean,
    val longClickCommentListener: (position: Int) -> Unit,
    val id: Int,
    val state: CurrentState
)
