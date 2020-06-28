package com.jamie.hn.stories.repository

import com.jamie.hn.comments.domain.model.Comment
import com.jamie.hn.stories.repository.local.LocalStorage
import com.jamie.hn.core.net.hex.Hex
import com.jamie.hn.stories.domain.model.Story
import com.jamie.hn.stories.repository.model.ApiComment
import org.joda.time.DateTime
import com.jamie.hn.stories.repository.model.ApiStory

class StoriesRepository(
    private val webStorage: Hex,
    private val localStorage: LocalStorage
) : Repository {

    override suspend fun topStories(useCachedVersion: Boolean): List<Story> {
        if (useCachedVersion) {
            val localCopy = localStorage.storyList
            if (localCopy.isNotEmpty()) return localCopy
        }

        val newCopy = webStorage.topStories().map { toStoryDomainModel(it) }
        localStorage.storyList = newCopy
        return newCopy
    }

    override suspend fun story(id: Long, useCachedVersion: Boolean): Story {
        if (useCachedVersion) {
            val localCopy = localStorage.storyList.firstOrNull { it.id == id }
            if (localCopy != null) return localCopy
        }

        val newCopy = toStoryDomainModel(webStorage.story(id))
        val newList = localStorage.storyList.toMutableList()
        newList.add(newCopy)

        localStorage.storyList = newList
        return newCopy
    }

    private fun toStoryDomainModel(apiStory: ApiStory): Story {
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
            apiStory.url
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
