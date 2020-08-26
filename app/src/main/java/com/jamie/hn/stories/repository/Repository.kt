package com.jamie.hn.stories.repository

import com.jamie.hn.stories.repository.model.StoryResults
import com.jamie.hn.stories.repository.model.TopStoryResults

interface Repository {
    suspend fun topStories(useCachedVersion: Boolean): TopStoryResults
    suspend fun story(id: Long, useCachedVersion: Boolean, requireComments: Boolean): StoryResults
}
