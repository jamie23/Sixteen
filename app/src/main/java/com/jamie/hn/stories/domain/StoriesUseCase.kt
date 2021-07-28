package com.jamie.hn.stories.domain

import com.jamie.hn.core.StoriesListType
import com.jamie.hn.stories.repository.StoriesRepository

class StoriesUseCase(
    private val storiesRepository: StoriesRepository
) {

    suspend fun getStories(useCachedVersion: Boolean, storiesListType: StoriesListType) =
        storiesRepository.stories(useCachedVersion, storiesListType)

    suspend fun getStory(
        id: Int,
        useCachedVersion: Boolean,
        storiesListType: StoriesListType
    ) =
        storiesRepository.story(
            id = id,
            useCachedVersion = useCachedVersion,
            requireCompleteStory = false,
            storiesListType = storiesListType
        )
}
