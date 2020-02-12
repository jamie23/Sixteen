package com.jamie.hn.articles.domain

data class Article(
    val by: String = "",
    val descendants: Int = 0,
    val id: Long = 0,
    val score: Int = 0,
    val time: Long = 0,
    val title: String = "",
    val url: String = ""
)
