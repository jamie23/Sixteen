package com.jamie.hn.stories.ui

data class StoryViewItem(
    val id: Long,
    val author: String,
    val comments: String,
    val score: String,
    val scoreText: String,
    val time: String,
    val title: String,
    val url: String,
    val commentsCallback: (Long) -> Unit,
    val storyViewerCallback: (Long) -> Unit
)
