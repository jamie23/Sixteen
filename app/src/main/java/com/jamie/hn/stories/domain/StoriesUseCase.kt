package com.jamie.hn.stories.domain

import com.jamie.hn.core.StoriesListType
import com.jamie.hn.stories.repository.StoriesRepository
import com.jamie.hn.stories.repository.StoriesRepository.RequireText

class StoriesUseCase(
    private val storiesRepository: StoriesRepository
) {

    suspend fun getStories(useCachedVersion: Boolean, storiesListType: StoriesListType) =
        storiesRepository.stories(useCachedVersion, storiesListType)

    suspend fun getStory(
        id: Int,
        useCachedVersion: Boolean,
        storiesListType: StoriesListType,
        requireText: RequireText
    ) =
        storiesRepository.story(
            id = id,
            useCachedVersion = useCachedVersion,
            requireComments = false,
            storiesListType = storiesListType,
            requireText = requireText
        )
}
