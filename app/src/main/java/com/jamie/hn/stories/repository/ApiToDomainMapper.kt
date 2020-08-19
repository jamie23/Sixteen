package com.jamie.hn.stories.repository

import com.jamie.hn.comments.domain.model.Comment
import com.jamie.hn.stories.domain.model.Story
import com.jamie.hn.stories.repository.model.ApiComment
import com.jamie.hn.stories.repository.model.ApiStory
import org.joda.time.DateTime

class ApiToDomainMapper {

    fun toStoryDomainModel(apiStory: ApiStory, retrievedComments: Boolean = false): Story {
        return Story(
            apiStory.author,
            apiStory.comments?.map { toCommentDomainModel(it) } ?: emptyList(),
            apiStory.commentCount,
            apiStory.commentsUrl,
            apiStory.domain,
            apiStory.id,
            apiStory.score,
            DateTime.parse(apiStory.time),
            apiStory.title,
            apiStory.url,
            retrievedComments
        )
    }

    private fun toCommentDomainModel(apiComment: ApiComment): Comment {
        if (apiComment.commentCount == 0) {
            return Comment(
                apiComment.author,
                emptyList(),
                apiComment.commentCount,
                apiComment.text,
                DateTime.parse(apiComment.time)
            )
        }

        return Comment(
            apiComment.author,
            apiComment.comments?.map { toCommentDomainModel(it) } ?: emptyList(),
            apiComment.commentCount,
            apiComment.text,
            DateTime.parse(apiComment.time)
        )
    }
}
