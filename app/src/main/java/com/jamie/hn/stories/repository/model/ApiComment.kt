package com.jamie.hn.stories.repository.model

data class ApiComment(
    val author: String = "",
    val comments: List<ApiComment>? = listOf(),
    val commentCount: Int,
    val text: String = "",
    val time: String
)
