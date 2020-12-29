package com.jamie.hn.stories.domain.model

import com.jamie.hn.comments.domain.model.Comment
import org.joda.time.DateTime

data class Story(
    val author: String = "",
    val comments: List<Comment> = emptyList(),
    val commentCount: Int = 0,
    val commentsUrl: String = "",
    val domain: String = "",
    val id: Int = -1,
    val score: Int = 0,
    val time: DateTime,
    val title: String = "",
    val url: String = "",
    val text: String = "",
    val retrievedComments: Boolean = false
) {
    val isAskStory: Boolean
        get() = title.startsWith("Ask HN:")
}
