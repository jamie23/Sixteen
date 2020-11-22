package com.jamie.hn.stories.repository

import com.jamie.hn.core.net.NetworkUtils
import com.jamie.hn.core.net.hex.Hex
import com.jamie.hn.stories.domain.model.Story
import com.jamie.hn.stories.repository.local.LocalStorage
import com.jamie.hn.stories.repository.model.ApiStory
import com.jamie.hn.stories.repository.model.StoryResults
import com.jamie.hn.stories.repository.model.TopStoryResults
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import io.mockk.verifyOrder
import kotlinx.coroutines.runBlocking
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class StoriesRepositoryTest {

    @MockK
    private lateinit var webStorage: Hex

    @MockK
    private lateinit var apiToDomainMapper: ApiToDomainMapper

    @MockK
    private lateinit var networkUtils: NetworkUtils

    @RelaxedMockK
    private lateinit var localStorage: LocalStorage

    private lateinit var storiesRepository: StoriesRepository

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { networkUtils.isNetworkAvailable() } returns true

        storiesRepository =
            StoriesRepository(webStorage, localStorage, apiToDomainMapper, networkUtils)
    }

    @Nested
    inner class GetTopStories {

        @Test
        fun `when network is unavailable then return local storage list and network failure as true`() {
            var topStories = TopStoryResults(emptyList())
            val storedList = listOf(Story(time = DateTime.now(), serverSortedOrder = 0))

            every { networkUtils.isNetworkAvailable() } returns false
            every { localStorage.storyList } returns storedList

            runBlocking {
                topStories = storiesRepository.topStories(false)
            }

            assertEquals(storedList, topStories.stories)
            assertEquals(true, topStories.networkFailure)
            verify(exactly = 0) { localStorage.storyList = any() }
        }

        @Test
        fun `when useCachedVersion is true and local storage is not empty then get the list of stories from local storage`() {
            var topStories = TopStoryResults(emptyList())
            val storedList = listOf(Story(time = DateTime.now(), serverSortedOrder = 0))

            every { localStorage.storyList } returns storedList

            runBlocking {
                topStories = storiesRepository.topStories(true)
            }

            assertEquals(storedList, topStories.stories)
            assertFalse(topStories.networkFailure)
            verify(exactly = 0) { localStorage.storyList = any() }
        }

        @Test
        fun `when useCachedVersion is true and local storage is empty then get the list of stories from web storage, update the local storage and return web copy`() {
            val date = DateTime.parse(
                "23/08/2020 09:00:00",
                DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss")
            )
            val apiStory = ApiStory(time = date.toString())
            val webList = listOf(apiStory)
            var topStories = TopStoryResults(emptyList())

            every { localStorage.storyList } returns listOf()
            coEvery { webStorage.topStories() } returns webList
            every {
                apiToDomainMapper.toStoryDomainModel(
                    apiStory,
                    serverSortedOrder = 0
                )
            } returns Story(time = date, serverSortedOrder = 0)

            runBlocking {
                topStories = storiesRepository.topStories(true)
            }

            verify(exactly = 1) { localStorage.storyList }
            verify { localStorage.storyList = listOf(Story(time = date, serverSortedOrder = 0)) }
            assertFalse(topStories.networkFailure)
            assertEquals(listOf(Story(time = date, serverSortedOrder = 0)), topStories.stories)
        }

        @Test
        fun `when useCachedVersion is false then get top stories from web and store them in local storage and then return them`() {
            val date = DateTime.parse(
                "23/08/2020 09:00:00",
                DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss")
            )
            val apiStory = ApiStory(time = date.toString())
            val webList = listOf(apiStory)
            var topStories = TopStoryResults(emptyList())

            coEvery { webStorage.topStories() } returns webList
            every {
                apiToDomainMapper.toStoryDomainModel(
                    apiStory,
                    serverSortedOrder = 0
                )
            } returns Story(time = date, serverSortedOrder = 0)

            runBlocking {
                topStories = storiesRepository.topStories(false)
            }

            verify(exactly = 0) { localStorage.storyList }
            verify { localStorage.storyList = listOf(Story(time = date, serverSortedOrder = 0)) }
            verify { apiToDomainMapper.toStoryDomainModel(any(), false, 0) }
            assertFalse(topStories.networkFailure)
            assertEquals(listOf(Story(time = date, serverSortedOrder = 0)), topStories.stories)
        }

        @Test
        fun `when retrieving stories from the web and multiple are returned then server order is maintained when mapped to domain models`() {
            val date = DateTime.parse(
                "23/08/2020 09:00:00",
                DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss")
            )
            val apiStory1 = ApiStory(time = date.toString())
            val apiStory2 = ApiStory(time = date.toString())
            val webList = listOf(apiStory1, apiStory2)
            var topStories = TopStoryResults(emptyList())

            coEvery { webStorage.topStories() } returns webList
            every {
                apiToDomainMapper.toStoryDomainModel(
                    apiStory1,
                    serverSortedOrder = 0
                )
            } returns Story(time = date, serverSortedOrder = 0)
            every {
                apiToDomainMapper.toStoryDomainModel(
                    apiStory2,
                    serverSortedOrder = 1
                )
            } returns Story(time = date, serverSortedOrder = 1)

            runBlocking {
                topStories = storiesRepository.topStories(false)
            }

            verifyOrder {
                apiToDomainMapper.toStoryDomainModel(apiStory1, false, 0)
                apiToDomainMapper.toStoryDomainModel(apiStory2, false, 1)
                localStorage.storyList = listOf(
                    Story(time = date, serverSortedOrder = 0),
                    Story(time = date, serverSortedOrder = 1)
                )
            }
            assertEquals(
                listOf(
                    Story(time = date, serverSortedOrder = 0),
                    Story(time = date, serverSortedOrder = 1)
                ), topStories.stories
            )
        }
    }

    @Nested
    inner class GetStory {

        @Test
        fun `when useCachedVersion is true and local storage is not null and requireComments is false then get story from local storage`() {
            val storedStory = Story(id = 1, time = DateTime.now(), serverSortedOrder = 0)
            val storedList = listOf(storedStory)
            lateinit var story: StoryResults

            every { localStorage.storyList } returns storedList

            runBlocking {
                story = storiesRepository.story(
                    id = 1,
                    useCachedVersion = true,
                    requireComments = false
                )
            }

            verify(exactly = 0) { localStorage.storyList = any() }
            assertEquals(storedStory, story.story)
        }

        @Test
        fun `when useCachedVersion is true and requireComments is true and localCopy has not retrieved comments then get the story from web storage and replace the local story with the new one`() {
            val storedStory = Story(
                id = 1,
                time = DateTime.now(),
                retrievedComments = false,
                serverSortedOrder = 0
            )
            val newStoredStory = Story(
                id = 1,
                time = DateTime.now(),
                retrievedComments = true,
                serverSortedOrder = 0
            )
            val storedList = listOf(storedStory)
            val apiStory = ApiStory(id = 1, time = DateTime.now().toString())
            lateinit var story: StoryResults

            every { localStorage.storyList } returns storedList
            every { apiToDomainMapper.toStoryDomainModel(any(), true, 0) } returns newStoredStory
            coEvery { webStorage.story(1) } returns apiStory

            runBlocking {
                story = storiesRepository.story(
                    id = 1,
                    useCachedVersion = true,
                    requireComments = true
                )
            }

            coVerifyOrder {
                localStorage.storyList
                webStorage.story(1)
                localStorage.storyList
                apiToDomainMapper.toStoryDomainModel(apiStory, true, 0)
                localStorage.storyList = listOf(newStoredStory)
            }
            assertEquals(newStoredStory, story.story)
        }

        @Test
        fun `when useCachedVersion is true and requireComments is true and localCopy has retrieved comments then get the story from local storage`() {
            lateinit var story: StoryResults
            val storedStory = Story(
                id = 1,
                time = DateTime.now(),
                retrievedComments = true,
                serverSortedOrder = 0
            )
            val storedList = listOf(storedStory)

            every { localStorage.storyList } returns storedList

            runBlocking {
                story = storiesRepository.story(
                    id = 1,
                    useCachedVersion = true,
                    requireComments = true
                )
            }

            verify(exactly = 0) { localStorage.storyList = any() }
            verify(exactly = 0) { apiToDomainMapper.toStoryDomainModel(any(), any(), any()) }
            assertEquals(storedStory, story.story)
        }

        @Test
        fun `when useCachedVersion is false then get the story from web storage, update the local storage and return web copy`() {
            lateinit var story: StoryResults
            val apiStory = ApiStory(id = 1, time = DateTime.now().toString())
            val storedStory = Story(id = 1, time = DateTime.now(), serverSortedOrder = 0)
            val storedList = listOf(storedStory)

            every { apiToDomainMapper.toStoryDomainModel(any(), true, 0) } returns storedStory
            coEvery { webStorage.story(1) } returns apiStory
            every { localStorage.storyList } returns storedList

            runBlocking {
                story = storiesRepository.story(
                    id = 1,
                    useCachedVersion = false,
                    requireComments = false
                )
            }

            coVerifyOrder {
                webStorage.story(1)
                localStorage.storyList
                apiToDomainMapper.toStoryDomainModel(apiStory, true, 0)
                localStorage.storyList = listOf(storedStory)
            }
            assertEquals(storedStory, story.story)
        }

        @Test
        fun `when network is unavailable, the story is cached and not requiring comments then use local version with network failure as true`() {
            lateinit var storyResults: StoryResults
            val storedStory = Story(
                id = 1,
                time = DateTime.now(),
                retrievedComments = false,
                serverSortedOrder = 0
            )
            val storedList = listOf(storedStory)

            every { localStorage.storyList } returns storedList
            every { networkUtils.isNetworkAvailable() } returns false

            runBlocking {
                storyResults = storiesRepository.story(
                    id = 1,
                    useCachedVersion = false,
                    requireComments = false
                )
            }

            verify(exactly = 0) { localStorage.storyList = any() }
            assertEquals(storedStory, storyResults.story)
            assertEquals(true, storyResults.networkFailure)
        }

        @Test
        fun `when network is unavailable, the story is cached and is requiring comments and stored story has comments then use local version with network failure as true`() {
            lateinit var storyResults: StoryResults
            val storedStory = Story(
                id = 1,
                time = DateTime.now(),
                retrievedComments = true,
                serverSortedOrder = 0
            )
            val storedList = listOf(storedStory)

            every { localStorage.storyList } returns storedList
            every { networkUtils.isNetworkAvailable() } returns false

            runBlocking {
                storyResults = storiesRepository.story(
                    id = 1,
                    useCachedVersion = false,
                    requireComments = true
                )
            }

            verify(exactly = 0) { localStorage.storyList = any() }
            assertEquals(storedStory, storyResults.story)
            assertEquals(true, storyResults.networkFailure)
        }

        @Test
        fun `when network is unavailable, the story is cached but does not have comments when required then return story with id as -1 and network failure as true`() {
            lateinit var storyResults: StoryResults
            val storedStory = Story(
                id = 1,
                time = DateTime.now(),
                retrievedComments = false,
                serverSortedOrder = 0
            )
            val storedList = listOf(storedStory)

            every { localStorage.storyList } returns storedList
            every { networkUtils.isNetworkAvailable() } returns false

            runBlocking {
                storyResults = storiesRepository.story(
                    id = 1,
                    useCachedVersion = false,
                    requireComments = true
                )
            }

            verify(exactly = 0) { localStorage.storyList = any() }
            assertEquals(-1, storyResults.story.id)
            assertEquals(true, storyResults.networkFailure)
        }
    }
}
