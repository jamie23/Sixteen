package com.jamie.hn.comments.ui

import com.jamie.hn.comments.ui.repository.model.CurrentState

interface ViewItem {
    val id: Int
    val state: CurrentState
    val author: String
    val time: String
    // implemented properly. Check here for more info:
    // https://stackoverflow.com/questions/55895359/lint-error-suspicious-equality-check-equals-is-not-implemented-in-object-dif
    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
}

data class HeaderViewItem(
    override val id: Int,
    override val state: CurrentState,
    override val author: String,
    override val time: String,
    val comments: String,
    val score: String,
    val scoreText: String,
    val title: String,
    val url: String,
    val text: CharSequence,
    val showText: Boolean,
    val showNavigateToArticle: Boolean,
    val storyViewerCallback: () -> Unit
) : ViewItem

// Used by both collapsed and full comment views
data class CommentViewItem(
    override val id: Int,
    override val state: CurrentState,
    override val author: String,
    override val time: String,
    val text: CharSequence,
    val depth: Int,
    val authorAndHiddenChildren: String,
    val showTopDivider: Boolean,
    val isOP: Boolean,
    val clickCommentListener: (position: Int) -> Unit
) : ViewItem
