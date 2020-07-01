package com.jamie.hn.stories.repository

import com.jamie.hn.stories.repository.local.LocalStorage
import com.jamie.hn.core.net.hex.Hex
import com.jamie.hn.stories.domain.model.Story

class StoriesRepository(
    private val webStorage: Hex,
    private val localStorage: LocalStorage,
    private val mapper: ApiToDomainMapper
) : Repository {

    override suspend fun topStories(useCachedVersion: Boolean): List<Story> {
        if (useCachedVersion) {
            val localCopy = localStorage.storyList
            if (localCopy.isNotEmpty()) return localCopy
        }

        val newCopy = webStorage.topStories().map { mapper.toStoryDomainModel(it) }
        localStorage.storyList = newCopy
        return newCopy
    }

    override suspend fun story(id: Long, useCachedVersion: Boolean): Story {
        if (useCachedVersion) {
            val localCopy = localStorage.storyList.firstOrNull { it.id == id }
            if (localCopy != null) return localCopy
        }

        val newCopy = mapper.toStoryDomainModel(webStorage.story(id))
        val newList = localStorage.storyList.toMutableList()
        newList.add(newCopy)

        localStorage.storyList = newList
        return newCopy
    }
}
