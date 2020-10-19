package com.jamie.hn.comments.ui.repository.model

import com.jamie.hn.comments.domain.model.CommentWithDepth

data class CommentCurrentState(
    val comment: CommentWithDepth,
    var state: CurrentState
)

enum class CurrentState {
    FULL, COLLAPSED, HIDDEN, HEADER
}
