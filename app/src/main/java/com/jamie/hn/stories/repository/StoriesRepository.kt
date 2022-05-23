package com.jamie.hn.stories.repository

import com.jamie.hn.core.StoriesListType
import com.jamie.hn.core.StoriesListType.ASK
import com.jamie.hn.core.StoriesListType.JOBS
import com.jamie.hn.core.StoriesListType.NEW
import com.jamie.hn.core.StoriesListType.SHOW
import com.jamie.hn.core.StoriesListType.TOP
import com.jamie.hn.core.StoriesListType.UNKNOWN
import com.jamie.hn.core.StoryType.TEXT
import com.jamie.hn.core.net.NetworkUtils
import com.jamie.hn.stories.repository.local.LocalStorage
import com.jamie.hn.core.net.hex.Hex
import com.jamie.hn.core.net.official.OfficialClient
import com.jamie.hn.stories.domain.model.DownloadedStatus.COMPLETE
import com.jamie.hn.stories.domain.model.DownloadedStatus.PARTIAL
import com.jamie.hn.stories.domain.model.Story
import com.jamie.hn.stories.repository.model.ApiStory
import com.jamie.hn.stories.repository.model.StoryResult
import com.jamie.hn.stories.repository.model.StoriesResult
import com.jamie.hn.stories.repository.web.getWebPath
import org.joda.time.DateTime

class StoriesRepository(
    private val webStorage: Hex,
    private val officialClient: OfficialClient,
    private val localStorage: LocalStorage,
    private val mapper: ApiToDomainMapper,
    private val networkUtils: NetworkUtils
) : Repository {

    override suspend fun stories(
        useCachedVersion: Boolean,
        storiesListType: StoriesListType
    ): StoriesResult {
        if (!networkUtils.isNetworkAvailable()) {
            return StoriesResult(
                getLocalStoriesListByType(storiesListType),
                true
            )
        }

        if (useCachedVersion) {
            val localCopy = getLocalStoriesListByType(storiesListType)
            if (localCopy.isNotEmpty()) {
                return StoriesResult(
                    localCopy,
                    false
                )
            }
        }

        val newCopy = webStorage.stories(getWebPath(storiesListType)).map { story ->
            mapper.toStoryDomainModel(story, PARTIAL)
        }

        updateLocalStoriesListByType(storiesListType, newCopy)
        return StoriesResult(newCopy)
    }

    override suspend fun story(
        id: Int,
        useCachedVersion: Boolean,
        requireCompleteStory: Boolean,
        storiesListType: StoriesListType
    ): StoryResult {
        if (storiesListType == UNKNOWN) return StoryResult(fetchStory(id))

        val localStoriesList = getLocalStoriesListByType(storiesListType)

        if (!networkUtils.isNetworkAvailable()) {
            val localCopy = localStoriesList.first { it.id == id }

            if (completeStoryRequiredAndRetrieved(requireCompleteStory, localCopy)) {
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

            if (completeStoryRequiredAndRetrieved(requireCompleteStory, localCopy)) {
                return StoryResult(
                    story = localCopy,
                    networkFailure = false
                )
            }
        }

        val domainMappedCopy = fetchStory(id)

        val newList = localStoriesList.toMutableList()
        val index = newList.indexOfFirst { it.id == domainMappedCopy.id }

        newList[index] = domainMappedCopy

        updateLocalStoriesListByType(storiesListType, newList)
        return StoryResult(domainMappedCopy)
    }

    private suspend fun fetchText(id: Int) = officialClient.getStory(id).text

    private suspend fun fetchStory(id: Int): Story {
        val newCopy = webStorage.story(id)

        val text = if (newCopy.requiresText()) {
            fetchText(id)
        } else {
            ""
        }

        return mapper.toStoryDomainModel(newCopy, COMPLETE, text)
    }

    private fun completeStoryRequiredAndRetrieved(requireCompleteStory: Boolean, localCopy: Story) =
        !requireCompleteStory || localCopy.downloadedStatus == COMPLETE

    private fun getLocalStoriesListByType(storiesListType: StoriesListType): List<Story> =
        when (storiesListType) {
            TOP -> localStorage.topStoryList
            ASK -> localStorage.askStoryList
            JOBS -> localStorage.jobsStoryList
            NEW -> localStorage.newStoryList
            SHOW -> localStorage.showStoryList
            UNKNOWN -> throw IllegalArgumentException("Should not update Unknown story type")
        }

    private fun updateLocalStoriesListByType(storiesListType: StoriesListType, list: List<Story>) {
        when (storiesListType) {
            TOP -> localStorage.topStoryList = list
            ASK -> localStorage.askStoryList = list
            JOBS -> localStorage.jobsStoryList = list
            NEW -> localStorage.newStoryList = list
            SHOW -> localStorage.showStoryList = list
            UNKNOWN -> throw IllegalArgumentException("Should not update Unknown story type")
        }
    }

    private fun ApiStory.requiresText() = TEXT == this.storyType
}
