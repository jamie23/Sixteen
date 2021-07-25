package com.jamie.hn.stories.repository.model

import com.jamie.hn.core.ASK_PREFIX
import com.jamie.hn.core.StoryType

data class ApiStory(
    val author: String = "",
    val comments: List<ApiComment>? = emptyList(),
    val commentCount: Int = 0,
    val commentsUrl: String = "",
    val domain: String = "",
    val id: Int = 0,
    val score: Int = 0,
    val time: String,
    val title: String = "",
    val url: String = ""
) {
    val storyType: StoryType
        get() = if (title.startsWith(ASK_PREFIX)) {
            StoryType.ASK
        } else {
            StoryType.STANDARD
        }
}
