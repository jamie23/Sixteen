package com.jamie.hn.stories.repository

import com.jamie.hn.core.net.NetworkUtils
import com.jamie.hn.stories.repository.local.LocalStorage
import com.jamie.hn.core.net.hex.Hex
import com.jamie.hn.stories.domain.model.Story
import com.jamie.hn.stories.repository.model.StoryResults
import com.jamie.hn.stories.repository.model.TopStoryResults
import org.joda.time.DateTime

class StoriesRepository(
    private val webStorage: Hex,
    private val localStorage: LocalStorage,
    private val mapper: ApiToDomainMapper,
    private val networkUtils: NetworkUtils
) : Repository {

    override suspend fun topStories(useCachedVersion: Boolean): TopStoryResults {
        if (!networkUtils.isNetworkAvailable()) {
            return TopStoryResults(
                localStorage.storyList,
                true
            )
        }

        if (useCachedVersion) {
            val localCopy = localStorage.storyList
            if (localCopy.isNotEmpty()) {
                return TopStoryResults(
                    localCopy,
                    false
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
    ): StoryResults {

        if (!networkUtils.isNetworkAvailable()) {
            val localCopy = localStorage.storyList.first { it.id == id }

            if (!requireComments || requireComments && localCopy.retrievedComments) {
                return StoryResults(
                    story = localCopy,
                    networkFailure = true
                )
            }

            return StoryResults(
                story = Story(
                    time = DateTime()
                ),
                networkFailure = true
            )
        }

        if (useCachedVersion) {
            val localCopy = localStorage.storyList.first { it.id == id }

            if (!requireComments || localCopy.retrievedComments) {
                return StoryResults(
                    story = localCopy,
                    networkFailure = false
                )
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
        return StoryResults(newCopy)
    }
}
