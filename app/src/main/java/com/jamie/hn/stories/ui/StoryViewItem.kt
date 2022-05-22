package com.jamie.hn.stories.ui

data class StoryViewItem(
    val id: Int,
    val author: String,
    val comments: String,
    val score: String,
    val scoreText: String,
    val time: String,
    val title: String,
    val url: String,
    val showNavigateToArticle: Boolean,
    val commentsCallback: (Int) -> Unit,
    val storyViewerCallback: (Int) -> Unit
)
