package com.jamie.hn.stories.repository

import com.jamie.hn.stories.domain.model.Story

interface Repository {
    suspend fun topStories(useCachedVersion: Boolean): List<Story>
    suspend fun story(id: Long, useCachedVersion: Boolean, requireComments: Boolean): Story
}
