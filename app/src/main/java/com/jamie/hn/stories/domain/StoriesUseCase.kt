package com.jamie.hn.stories.domain

import com.jamie.hn.core.StoriesType
import com.jamie.hn.stories.repository.StoriesRepository

class StoriesUseCase(
    private val storiesRepository: StoriesRepository
) {

    suspend fun getStories(useCachedVersion: Boolean, storiesType: StoriesType) =
        storiesRepository.stories(useCachedVersion, storiesType)

    suspend fun getStory(id: Int, useCachedVersion: Boolean, storyType: StoriesType) =
        storiesRepository.story(
            id = id,
            useCachedVersion = useCachedVersion,
            requireComments = false,
            storiesType = storyType
        )
}
