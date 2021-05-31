package com.jamie.hn.stories.repository

import com.jamie.hn.core.StoriesListType
import com.jamie.hn.core.StoriesListType.TOP
import com.jamie.hn.core.StoriesListType.ASK
import com.jamie.hn.core.StoriesListType.JOBS
import com.jamie.hn.core.StoriesListType.NEW
import com.jamie.hn.core.StoriesListType.SHOW
import com.jamie.hn.core.net.NetworkUtils
import com.jamie.hn.stories.repository.local.LocalStorage
import com.jamie.hn.core.net.hex.Hex
import com.jamie.hn.core.net.official.OfficialClient
import com.jamie.hn.stories.domain.model.Story
import com.jamie.hn.stories.repository.StoriesRepository.RequireText.NOT_REQUIRED
import com.jamie.hn.stories.repository.StoriesRepository.RequireText.REQUIRED
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
            mapper.toStoryDomainModel(story, false)
        }

        updateLocalStoriesListByType(storiesListType, newCopy)
        return StoriesResult(newCopy)
    }

    override suspend fun story(
        id: Int,
        useCachedVersion: Boolean,
        requireComments: Boolean,
        storiesListType: StoriesListType,
        requireText: RequireText
    ): StoryResult {
        val localStoriesList = getLocalStoriesListByType(storiesListType)

        if (!networkUtils.isNetworkAvailable()) {
            val localCopy = localStoriesList.first { it.id == id }

            if (commentsRequiredAndRetrieved(requireComments, localCopy) &&
                textRequiredAndRetrieved(requireText, localCopy)
            ) {
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

            if (commentsRequiredAndRetrieved(requireComments, localCopy) &&
                textRequiredAndRetrieved(requireText, localCopy)
            ) {
                return StoryResult(
                    story = localCopy,
                    networkFailure = false
                )
            }
        }

        var text = ""
        if (requireText == REQUIRED) {
            text = officialClient.getStory(id).text
        }

        val newCopy = webStorage.story(id)
        val newList = localStoriesList.toMutableList()
        val index = newList.indexOfFirst { it.id == newCopy.id }

        val domainMappedCopy =
            mapper.toStoryDomainModel(newCopy, true, text)

        newList[index] = domainMappedCopy

        updateLocalStoriesListByType(storiesListType, newList)
        return StoryResult(domainMappedCopy)
    }

    private fun commentsRequiredAndRetrieved(requireComments: Boolean, localCopy: Story) =
        !requireComments || localCopy.retrievedComments

    private fun textRequiredAndRetrieved(requireText: RequireText, localCopy: Story) =
        requireText == NOT_REQUIRED || localCopy.text != ""

    private fun getLocalStoriesListByType(storiesListType: StoriesListType): List<Story> =
        when (storiesListType) {
            TOP -> localStorage.topStoryList
            ASK -> localStorage.askStoryList
            JOBS -> localStorage.jobsStoryList
            NEW -> localStorage.newStoryList
            SHOW -> localStorage.showStoryList
        }

    private fun updateLocalStoriesListByType(storiesListType: StoriesListType, list: List<Story>) =
        when (storiesListType) {
            TOP -> localStorage.topStoryList = list
            ASK -> localStorage.askStoryList = list
            JOBS -> localStorage.jobsStoryList = list
            NEW -> localStorage.newStoryList = list
            SHOW -> localStorage.showStoryList = list
        }

    enum class RequireText {
        REQUIRED, NOT_REQUIRED
    }
}
