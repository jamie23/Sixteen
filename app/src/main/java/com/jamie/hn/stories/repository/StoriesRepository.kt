package com.jamie.hn.stories.repository

import com.jamie.hn.core.net.NetworkUtils
import com.jamie.hn.stories.repository.local.LocalStorage
import com.jamie.hn.core.net.hex.Hex
import com.jamie.hn.stories.domain.model.Story
import com.jamie.hn.stories.repository.model.TopStoryResults

class StoriesRepository(
    private val webStorage: Hex,
    private val localStorage: LocalStorage,
    private val mapper: ApiToDomainMapper,
    private val networkUtils: NetworkUtils
) : Repository {

    override suspend fun topStories(useCachedVersion: Boolean): TopStoryResults {
        if (useCachedVersion || !networkUtils.isNetworkAvailable()) {
            val localCopy = localStorage.storyList
            if (localCopy.isNotEmpty()) {
                return TopStoryResults(
                    localCopy,
                    !useCachedVersion && !networkUtils.isNetworkAvailable()
                )
            }
        }

        val newCopy = webStorage.topStories().map { mapper.toStoryDomainModel(it, false) }
        localStorage.storyList = newCopy
        return TopStoryResults(newCopy)
    }

    override suspend fun story(
        id: Long,
        useCachedVersion: Boolean,
        requireComments: Boolean
    ): Story {
        if (useCachedVersion) {
            val localCopy = localStorage.storyList.find { it.id == id }
            if (localCopy != null && (!requireComments || localCopy.retrievedComments)) {
                return localCopy
            }
        }

        val newCopy = mapper.toStoryDomainModel(webStorage.story(id), true)
        val newList = localStorage.storyList.toMutableList()
        val index = newList.indexOfFirst { it.id == newCopy.id }

        if (index == -1) {
            newList.add(newCopy)
        } else {
            newList[index] = newCopy
        }

        localStorage.storyList = newList
        return newCopy
    }
}
