package com.jamie.hn.stories.domain.model

import com.jamie.hn.comments.domain.model.Comment
import com.jamie.hn.core.StoryType
import com.jamie.hn.core.StoryType.STANDARD
import com.jamie.hn.stories.domain.model.DownloadedStatus.PARTIAL
import org.joda.time.DateTime

data class Story(
    val author: String = "",
    val comments: List<Comment> = emptyList(),
    val commentCount: Int = 0,
    val commentsUrl: String = "",
    val domain: String = "",
    val downloadedStatus: DownloadedStatus = PARTIAL,
    val id: Int = -1,
    val score: Int = 0,
    val text: String = "",
    val time: DateTime,
    val title: String = "",
    val type: StoryType = STANDARD,
    val url: String = ""
)

enum class DownloadedStatus {
    COMPLETE, PARTIAL
}
