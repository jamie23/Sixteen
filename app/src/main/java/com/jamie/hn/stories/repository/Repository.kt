package com.jamie.hn.stories.repository

import com.jamie.hn.core.StoriesType
import com.jamie.hn.stories.repository.model.StoryResult
import com.jamie.hn.stories.repository.model.StoriesResult

interface Repository {
    suspend fun stories(useCachedVersion: Boolean, storiesType: StoriesType): StoriesResult
    suspend fun story(
        id: Int,
        useCachedVersion: Boolean,
        requireComments: Boolean,
        storiesType: StoriesType
    ): StoryResult
}
