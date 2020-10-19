package com.jamie.hn.stories.domain

import com.jamie.hn.stories.repository.StoriesRepository

class StoriesUseCase(
    private val storiesRepository: StoriesRepository
) {

    suspend fun getStories(useCachedVersion: Boolean) =
        storiesRepository.topStories(useCachedVersion)

    suspend fun getStory(id: Int, useCachedVersion: Boolean) =
        storiesRepository.story(
            id = id,
            useCachedVersion = useCachedVersion,
            requireComments = false
        )
}
