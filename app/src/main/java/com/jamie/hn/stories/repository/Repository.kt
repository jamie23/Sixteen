package com.jamie.hn.stories.repository

import com.jamie.hn.core.StoriesListType
import com.jamie.hn.stories.repository.StoriesRepository.RequireText
import com.jamie.hn.stories.repository.model.StoryResult
import com.jamie.hn.stories.repository.model.StoriesResult

interface Repository {
    suspend fun stories(useCachedVersion: Boolean, storiesListType: StoriesListType): StoriesResult
    suspend fun story(
        id: Int,
        useCachedVersion: Boolean,
        requireComments: Boolean,
        storiesListType: StoriesListType,
        requireText: RequireText
    ): StoryResult
}
