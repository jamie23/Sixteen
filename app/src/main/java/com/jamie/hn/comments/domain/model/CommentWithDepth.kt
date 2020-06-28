package com.jamie.hn.comments.domain.model

data class CommentWithDepth(
    val comment: Comment,
    val depth: Int
)
