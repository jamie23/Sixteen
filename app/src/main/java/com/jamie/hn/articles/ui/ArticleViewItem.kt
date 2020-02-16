package com.jamie.hn.articles.ui

data class ArticleViewItem(
    val id: Long,
    val author: String,
    val comments: String,
    val score: String,
    val time: String,
    val title: String,
    val url: String,
    val commentsCallback: (Long) -> Unit
)
