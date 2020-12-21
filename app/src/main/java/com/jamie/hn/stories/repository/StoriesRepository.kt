package com.jamie.hn.stories.repository

import com.jamie.hn.core.StoriesType
import com.jamie.hn.core.StoriesType.TOP
import com.jamie.hn.core.StoriesType.ASK
import com.jamie.hn.core.StoriesType.JOBS
import com.jamie.hn.core.StoriesType.NEW
import com.jamie.hn.core.StoriesType.SHOW
import com.jamie.hn.core.net.NetworkUtils
import com.jamie.hn.stories.repository.local.LocalStorage
import com.jamie.hn.core.net.hex.Hex
import com.jamie.hn.stories.domain.model.Story
import com.jamie.hn.stories.repository.model.StoryResult
import com.jamie.hn.stories.repository.model.StoriesResult
import com.jamie.hn.stories.repository.web.getWebPath
import org.joda.time.DateTime

class StoriesRepository(
    private val webStorage: Hex,
    private val localStorage: LocalStorage,
    private val mapper: ApiToDomainMapper,
    private val networkUtils: NetworkUtils
) : Repository {

    override suspend fun stories(
        useCachedVersion: Boolean,
        storiesType: StoriesType
    ): StoriesResult {
        if (!networkUtils.isNetworkAvailable()) {
            return StoriesResult(
                getLocalStoriesListByType(storiesType),
                true
            )
        }

        if (useCachedVersion) {
            val localCopy = getLocalStoriesListByType(storiesType)
            if (localCopy.isNotEmpty()) {
                return StoriesResult(
                    localCopy,
                    false
                )
            }
        }

        val newCopy = webStorage.stories(getWebPath(storiesType)).map { story ->
            mapper.toStoryDomainModel(story, false)
        }

        updateLocalStoriesListByType(storiesType, newCopy)
        return StoriesResult(newCopy)
    }

    override suspend fun story(
        id: Int,
        useCachedVersion: Boolean,
        requireComments: Boolean,
        storiesType: StoriesType
    ): StoryResult {
        val localStoriesList = getLocalStoriesListByType(storiesType)

        if (!networkUtils.isNetworkAvailable()) {
            val localCopy = localStoriesList.first { it.id == id }

            if (!requireComments || requireComments && localCopy.retrievedComments) {
                return StoryResult(
                    story = localCopy,
                    networkFailure = true
                )
            }

            return StoryResult(
                story = Story(time = DateTime()),
                networkFailure = true
            )
        }

        if (useCachedVersion) {
            val localCopy = localStoriesList.first { it.id == id }

            if (!requireComments || localCopy.retrievedComments) {
                return StoryResult(
                    story = localCopy,
                    networkFailure = false
                )
            }
        }

        val newCopy = webStorage.story(id)
        val newList = localStoriesList.toMutableList()
        val index = newList.indexOfFirst { it.id == newCopy.id }

        val domainMappedCopy =
            mapper.toStoryDomainModel(newCopy, true)

        newList[index] = domainMappedCopy

        updateLocalStoriesListByType(storiesType, newList)
        return StoryResult(domainMappedCopy)
    }

    private fun getLocalStoriesListByType(storiesType: StoriesType): List<Story> =
        when (storiesType) {
            TOP -> localStorage.topStoryList
            ASK -> localStorage.askStoryList
            JOBS -> localStorage.jobsStoryList
            NEW -> localStorage.newStoryList
            SHOW -> localStorage.showStoryList
        }

    private fun updateLocalStoriesListByType(storiesType: StoriesType, list: List<Story>) =
        when (storiesType) {
            TOP -> localStorage.topStoryList = list
            ASK -> localStorage.askStoryList = list
            JOBS -> localStorage.jobsStoryList = list
            NEW -> localStorage.newStoryList = list
            SHOW -> localStorage.showStoryList = list
        }
}
