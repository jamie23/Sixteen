package com.jamie.hn.stories.repository

import com.jamie.hn.stories.domain.model.Story

interface Repository {
    suspend fun topStories(userCachedVersion: Boolean): List<Story>
    suspend fun story(id: Long, userCachedVersion: Boolean): Story
}
