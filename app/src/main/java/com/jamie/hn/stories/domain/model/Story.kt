package com.jamie.hn.stories.domain.model

import com.jamie.hn.comments.domain.model.Comment
import com.jamie.hn.core.ASK_PREFIX
import com.jamie.hn.core.StoryType
import com.jamie.hn.core.StoryType.ASK
import com.jamie.hn.core.StoryType.STANDARD
import com.jamie.hn.stories.domain.model.DownloadedStatus.PARTIAL
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
    val downloadedStatus: DownloadedStatus = PARTIAL
) {
    val storyType: StoryType
        get() = if (title.startsWith(ASK_PREFIX)) {
            ASK
        } else {
            STANDARD
        }
}

enum class DownloadedStatus {
    COMPLETE, PARTIAL
}
