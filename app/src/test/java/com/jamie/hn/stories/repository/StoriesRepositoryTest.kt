package com.jamie.hn.stories.repository

import com.jamie.hn.core.StoriesListType.ASK
import com.jamie.hn.core.StoriesListType.JOBS
import com.jamie.hn.core.StoriesListType.NEW
import com.jamie.hn.core.StoriesListType.SHOW
import com.jamie.hn.core.StoriesListType.TOP
import com.jamie.hn.core.net.NetworkUtils
import com.jamie.hn.core.net.hex.Hex
import com.jamie.hn.core.net.official.OfficialClient
import com.jamie.hn.stories.domain.model.Story
import com.jamie.hn.stories.repository.local.LocalStorage
import com.jamie.hn.stories.repository.model.ApiAskText
import com.jamie.hn.stories.repository.model.ApiStory
import com.jamie.hn.stories.repository.model.StoryResult
import com.jamie.hn.stories.repository.model.StoriesResult
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
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

    @MockK
    private lateinit var officialClient: OfficialClient

    private lateinit var storiesRepository: StoriesRepository

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { networkUtils.isNetworkAvailable() } returns true

        storiesRepository =
            StoriesRepository(
                webStorage,
                officialClient,
                localStorage,
                apiToDomainMapper,
                networkUtils
            )
    }

    @Nested
    inner class GetStories {

        @Test
        fun `when requesting TOP stories and network is unavailable then return story from TOP local storage list and network failure as true`() {
            var topStories = StoriesResult(emptyList())
            val storedList = listOf(Story(time = DateTime.now()))

            every { networkUtils.isNetworkAvailable() } returns false
            every { localStorage.topStoryList } returns storedList

            runBlocking {
                topStories = storiesRepository.stories(false, TOP)
            }

            assertEquals(storedList, topStories.stories)
            assertEquals(true, topStories.networkFailure)
            verify(exactly = 0) { localStorage.topStoryList = any() }
            verify(exactly = 0) { localStorage.askStoryList }
            verify(exactly = 0) { localStorage.jobsStoryList }
            verify(exactly = 0) { localStorage.newStoryList }
            verify(exactly = 0) { localStorage.showStoryList }
        }

        @Test
        fun `when requesting ASK stories and useCachedVersion is true and ASK local storage is not empty then get the list of stories from ASK local storage`() {
            var askStories = StoriesResult(emptyList())
            val storedList = listOf(Story(time = DateTime.now()))

            every { localStorage.askStoryList } returns storedList

            runBlocking {
                askStories = storiesRepository.stories(true, ASK)
            }

            assertEquals(storedList, askStories.stories)
            assertFalse(askStories.networkFailure)
            verify(exactly = 0) { localStorage.askStoryList = any() }
            verify(exactly = 0) { localStorage.topStoryList }
            verify(exactly = 0) { localStorage.jobsStoryList }
            verify(exactly = 0) { localStorage.newStoryList }
            verify(exactly = 0) { localStorage.showStoryList }
        }

        @Test
        fun `when requesting JOBS stories and useCachedVersion is true and JOBS local storage is empty then get the list of JOBS stories from web storage, update the local storage and return web copy`() {
            val date = DateTime.parse(
                "23/08/2020 09:00:00",
                DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss")
            )
            val apiStory = ApiStory(time = date.toString())
            val webList = listOf(apiStory)
            var jobsStories = StoriesResult(emptyList())

            every { localStorage.jobsStoryList } returns listOf()
            coEvery { webStorage.stories("jobs") } returns webList
            every {
                apiToDomainMapper.toStoryDomainModel(apiStory)
            } returns Story(time = date)

            runBlocking {
                jobsStories = storiesRepository.stories(true, JOBS)
            }

            verify(exactly = 1) { localStorage.jobsStoryList }
            verify { localStorage.jobsStoryList = listOf(Story(time = date)) }
            verify(exactly = 0) { localStorage.topStoryList = any() }
            verify(exactly = 0) { localStorage.askStoryList = any() }
            verify(exactly = 0) { localStorage.newStoryList = any() }
            verify(exactly = 0) { localStorage.showStoryList = any() }
            assertFalse(jobsStories.networkFailure)
            assertEquals(listOf(Story(time = date)), jobsStories.stories)
        }

        @Test
        fun `when requesting NEW stories and useCachedVersion is false then get NEW stories from web and store them in local NEW storage and then return them`() {
            val date = DateTime.parse(
                "23/08/2020 09:00:00",
                DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss")
            )
            val apiStory = ApiStory(time = date.toString())
            val webList = listOf(apiStory)
            var newStories = StoriesResult(emptyList())

            coEvery { webStorage.stories("new") } returns webList
            every {
                apiToDomainMapper.toStoryDomainModel(
                    apiStory
                )
            } returns Story(time = date)

            runBlocking {
                newStories = storiesRepository.stories(false, NEW)
            }

            verify(exactly = 0) { localStorage.newStoryList }
            verify { localStorage.newStoryList = listOf(Story(time = date)) }
            verify { apiToDomainMapper.toStoryDomainModel(any(), false) }
            verify(exactly = 0) { localStorage.topStoryList = any() }
            verify(exactly = 0) { localStorage.askStoryList = any() }
            verify(exactly = 0) { localStorage.jobsStoryList = any() }
            verify(exactly = 0) { localStorage.showStoryList = any() }
            assertFalse(newStories.networkFailure)
            assertEquals(listOf(Story(time = date)), newStories.stories)
        }

        @Test
        fun `when requesting SHOW stories from the web and multiple are returned then server order is maintained when mapped to domain models`() {
            val date = DateTime.parse(
                "23/08/2020 09:00:00",
                DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss")
            )
            val apiStory1 = ApiStory(time = date.toString())
            val apiStory2 = ApiStory(time = date.toString())
            val webList = listOf(apiStory1, apiStory2)
            var showStories = StoriesResult(emptyList())

            coEvery { webStorage.stories("show") } returns webList
            every {
                apiToDomainMapper.toStoryDomainModel(
                    apiStory1
                )
            } returns Story(time = date)
            every {
                apiToDomainMapper.toStoryDomainModel(apiStory2)
            } returns Story(time = date)

            runBlocking {
                showStories = storiesRepository.stories(false, SHOW)
            }

            verifyOrder {
                apiToDomainMapper.toStoryDomainModel(apiStory1, false)
                apiToDomainMapper.toStoryDomainModel(apiStory2, false)
                localStorage.showStoryList = listOf(
                    Story(time = date),
                    Story(time = date)
                )
            }
            assertEquals(
                listOf(
                    Story(time = date),
                    Story(time = date)
                ), showStories.stories
            )
            verify(exactly = 0) { localStorage.topStoryList = any() }
            verify(exactly = 0) { localStorage.askStoryList = any() }
            verify(exactly = 0) { localStorage.jobsStoryList = any() }
            verify(exactly = 0) { localStorage.newStoryList = any() }
        }
    }

    @Nested
    inner class GetStory {

        @Nested
        inner class UnknownStoryType {

            @Test
            fun `when storyType is unknown then fetch from web`() {
                lateinit var storyResult: StoryResult

                runBlocking {
                    storyResult = storiesRepository.story(
                        id = 1,
                        useCachedVersion = false,
                        requireComments = false,
                        storiesListType = SHOW
                    )
                }

                verify(exactly = 0) { localStorage.showStoryList = any() }
                verify(exactly = 0) { localStorage.topStoryList }
                verify(exactly = 0) { localStorage.askStoryList }
                verify(exactly = 0) { localStorage.jobsStoryList }
                verify(exactly = 0) { localStorage.newStoryList }
                assertEquals(storedStory, storyResult.story)
                assertEquals(true, storyResult.networkFailure)
            }
        }

        @Nested
        inner class NetworkUnavailable {

            @Nested
            inner class CommentsRequiredAndRetrieved {

                @Test
                fun `when requesting a story from SHOW, the story is cached and not requiring comments then use local version with network failure as true`() {
                    lateinit var storyResult: StoryResult
                    val storedStory = Story(
                        id = 1,
                        time = DateTime.now(),
                        retrievedComments = false
                    )
                    val storedList = listOf(storedStory)

                    every { localStorage.showStoryList } returns storedList
                    every { networkUtils.isNetworkAvailable() } returns false

                    runBlocking {
                        storyResult = storiesRepository.story(
                            id = 1,
                            useCachedVersion = false,
                            requireComments = false,
                            storiesListType = SHOW
                        )
                    }

                    verify(exactly = 0) { localStorage.showStoryList = any() }
                    verify(exactly = 0) { localStorage.topStoryList }
                    verify(exactly = 0) { localStorage.askStoryList }
                    verify(exactly = 0) { localStorage.jobsStoryList }
                    verify(exactly = 0) { localStorage.newStoryList }
                    assertEquals(storedStory, storyResult.story)
                    assertEquals(true, storyResult.networkFailure)
                }

                @Test
                fun `when requesting a TOP story, the story is cached and is requiring comments and stored story has comments then use local version with network failure as true`() {
                    lateinit var storyResult: StoryResult
                    val storedStory = Story(
                        id = 1,
                        time = DateTime.now(),
                        retrievedComments = true
                    )
                    val storedList = listOf(storedStory)

                    every { localStorage.topStoryList } returns storedList
                    every { networkUtils.isNetworkAvailable() } returns false

                    runBlocking {
                        storyResult = storiesRepository.story(
                            id = 1,
                            useCachedVersion = false,
                            requireComments = true,
                            storiesListType = TOP
                        )
                    }

                    verify(exactly = 0) { localStorage.topStoryList = any() }
                    verify(exactly = 0) { localStorage.askStoryList }
                    verify(exactly = 0) { localStorage.jobsStoryList }
                    verify(exactly = 0) { localStorage.newStoryList }
                    verify(exactly = 0) { localStorage.showStoryList }
                    assertEquals(storedStory, storyResult.story)
                    assertEquals(true, storyResult.networkFailure)
                }

                @Test
                fun `when requesting a TOP story, the story is cached but does not have comments when required then return story with id as -1 and network failure as true`() {
                    lateinit var storyResult: StoryResult
                    val storedStory = Story(
                        id = 1,
                        time = DateTime.now(),
                        retrievedComments = false
                    )
                    val storedList = listOf(storedStory)

                    every { localStorage.topStoryList } returns storedList
                    every { networkUtils.isNetworkAvailable() } returns false

                    runBlocking {
                        storyResult = storiesRepository.story(
                            id = 1,
                            useCachedVersion = false,
                            requireComments = true,
                            storiesListType = TOP
                        )
                    }

                    verify(exactly = 0) { localStorage.topStoryList = any() }
                    assertEquals(-1, storyResult.story.id)
                    assertEquals(true, storyResult.networkFailure)
                }
            }

            @Nested
            inner class TextRequiredAndRetrieved {

                @Test
                fun `when requesting a story from SHOW, the story is cached and not requiring text then use local version with network failure as true`() {
                    lateinit var storyResult: StoryResult
                    val storedStory = Story(
                        id = 1,
                        time = DateTime.now(),
                        retrievedComments = true,
                        text = ""
                    )
                    val storedList = listOf(storedStory)

                    every { localStorage.showStoryList } returns storedList
                    every { networkUtils.isNetworkAvailable() } returns false

                    runBlocking {
                        storyResult = storiesRepository.story(
                            id = 1,
                            useCachedVersion = false,
                            requireComments = true,
                            storiesListType = SHOW
                        )
                    }

                    verify(exactly = 0) { localStorage.showStoryList = any() }
                    coVerify(exactly = 0) { officialClient.getStory(any()) }
                    verify(exactly = 0) { localStorage.topStoryList }
                    verify(exactly = 0) { localStorage.askStoryList }
                    verify(exactly = 0) { localStorage.jobsStoryList }
                    verify(exactly = 0) { localStorage.newStoryList }
                    assertEquals(storedStory, storyResult.story)
                    assertEquals(true, storyResult.networkFailure)
                }

                @Test
                fun `when requesting a TOP story, the story is cached and is requiring text and stored story has text then use local version with network failure as true`() {
                    lateinit var storyResult: StoryResult
                    val storedStory = Story(
                        id = 1,
                        time = DateTime.now(),
                        retrievedComments = true,
                        text = "Text"
                    )
                    val storedList = listOf(storedStory)

                    every { localStorage.topStoryList } returns storedList
                    every { networkUtils.isNetworkAvailable() } returns false

                    runBlocking {
                        storyResult = storiesRepository.story(
                            id = 1,
                            useCachedVersion = false,
                            requireComments = true,
                            storiesListType = TOP
                        )
                    }

                    verify(exactly = 0) { localStorage.topStoryList = any() }
                    coVerify(exactly = 0) { officialClient.getStory(any()) }
                    verify(exactly = 0) { localStorage.askStoryList }
                    verify(exactly = 0) { localStorage.jobsStoryList }
                    verify(exactly = 0) { localStorage.newStoryList }
                    verify(exactly = 0) { localStorage.showStoryList }
                    assertEquals(storedStory, storyResult.story)
                    assertEquals(true, storyResult.networkFailure)
                }

                @Test
                fun `when requesting a TOP story, the story is cached but does not have text when required then return story with id as -1 and network failure as true`() {
                    lateinit var storyResult: StoryResult
                    val storedStory = Story(
                        id = 1,
                        time = DateTime.now(),
                        retrievedComments = true,
                        text = ""
                    )
                    val storedList = listOf(storedStory)

                    every { localStorage.topStoryList } returns storedList
                    every { networkUtils.isNetworkAvailable() } returns false

                    runBlocking {
                        storyResult = storiesRepository.story(
                            id = 1,
                            useCachedVersion = false,
                            requireComments = true,
                            storiesListType = TOP
                        )
                    }

                    verify(exactly = 0) { localStorage.topStoryList = any() }
                    coVerify(exactly = 0) { officialClient.getStory(any()) }
                    assertEquals(-1, storyResult.story.id)
                    assertEquals(true, storyResult.networkFailure)
                }
            }
        }

        @Nested
        inner class UseCachedVersionTrue {
            @Test
            fun `when requesting a story from TOP, local storage is not null and requireComments is false then get story from local storage`() {
                val storedStory = Story(id = 1, time = DateTime.now())
                val storedList = listOf(storedStory)
                lateinit var story: StoryResult

                every { localStorage.topStoryList } returns storedList

                runBlocking {
                    story = storiesRepository.story(
                        id = 1,
                        useCachedVersion = true,
                        requireComments = false,
                        storiesListType = TOP
                    )
                }

                verify(exactly = 0) { localStorage.topStoryList = any() }
                verify(exactly = 0) { localStorage.askStoryList }
                verify(exactly = 0) { localStorage.jobsStoryList }
                verify(exactly = 0) { localStorage.newStoryList }
                verify(exactly = 0) { localStorage.showStoryList }
                assertEquals(storedStory, story.story)
            }

            @Test
            fun `when requesting a story from ASK, requireComments is true and localCopy has not retrieved comments then get the story from web storage and replace the local story`() {
                val storedStory = Story(
                    id = 1,
                    time = DateTime.now(),
                    retrievedComments = false
                )
                val newStoredStory = Story(
                    id = 1,
                    time = DateTime.now(),
                    retrievedComments = true
                )
                val storedList = listOf(storedStory)
                val apiStory = ApiStory(id = 1, time = DateTime.now().toString())
                lateinit var story: StoryResult

                every { localStorage.askStoryList } returns storedList
                every { apiToDomainMapper.toStoryDomainModel(any(), true) } returns newStoredStory
                coEvery { webStorage.story(1) } returns apiStory

                runBlocking {
                    story = storiesRepository.story(
                        id = 1,
                        useCachedVersion = true,
                        requireComments = true,
                        storiesListType = ASK
                    )
                }

                coVerifyOrder {
                    localStorage.askStoryList
                    webStorage.story(1)
                    apiToDomainMapper.toStoryDomainModel(apiStory, true)
                    localStorage.askStoryList = listOf(newStoredStory)
                }
                assertEquals(newStoredStory, story.story)
                verify(exactly = 0) { localStorage.topStoryList }
                verify(exactly = 0) { localStorage.jobsStoryList }
                verify(exactly = 0) { localStorage.newStoryList }
                verify(exactly = 0) { localStorage.showStoryList }
            }

            @Test
            fun `when requesting a story from JOBS, requireComments is true and localCopy has retrieved comments then get the story from local storage`() {
                lateinit var story: StoryResult
                val storedStory = Story(
                    id = 1,
                    time = DateTime.now(),
                    retrievedComments = true
                )
                val storedList = listOf(storedStory)

                every { localStorage.jobsStoryList } returns storedList

                runBlocking {
                    story = storiesRepository.story(
                        id = 1,
                        useCachedVersion = true,
                        requireComments = true,
                        storiesListType = JOBS
                    )
                }

                verify(exactly = 0) { localStorage.jobsStoryList = any() }
                verify(exactly = 0) { apiToDomainMapper.toStoryDomainModel(any(), any()) }
                verify(exactly = 0) { localStorage.topStoryList }
                verify(exactly = 0) { localStorage.askStoryList }
                verify(exactly = 0) { localStorage.newStoryList }
                verify(exactly = 0) { localStorage.showStoryList }
                assertEquals(storedStory, story.story)
            }

            @Test
            fun `when requesting a story from ASK, requireText is true and localCopy has not retrieved text then get the story from web storage and replace the local story`() {
                lateinit var story: StoryResult
                val storedStory = Story(
                    id = 1,
                    time = DateTime.now(),
                    retrievedComments = true
                )
                val newStoredStory = Story(
                    id = 1,
                    time = DateTime.now(),
                    retrievedComments = true,
                    text = "Text"
                )
                val storedList = listOf(storedStory)
                val apiAskText = ApiAskText("Text")
                val apiStory = ApiStory(id = 1, time = DateTime.now().toString())

                every { localStorage.askStoryList } returns storedList
                coEvery { officialClient.getStory(1) } returns apiAskText
                coEvery { webStorage.story(1) } returns apiStory
                every {
                    apiToDomainMapper.toStoryDomainModel(
                        any(),
                        true,
                        "Text"
                    )
                } returns newStoredStory

                runBlocking {
                    story = storiesRepository.story(
                        id = 1,
                        useCachedVersion = true,
                        requireComments = true,
                        storiesListType = ASK
                    )
                }

                coVerifyOrder {
                    localStorage.askStoryList
                    officialClient.getStory(1)
                    webStorage.story(1)
                    apiToDomainMapper.toStoryDomainModel(apiStory, true, "Text")
                    localStorage.askStoryList = listOf(newStoredStory)
                }
                assertEquals(newStoredStory, story.story)
                verify(exactly = 0) { localStorage.topStoryList }
                verify(exactly = 0) { localStorage.jobsStoryList }
                verify(exactly = 0) { localStorage.newStoryList }
                verify(exactly = 0) { localStorage.showStoryList }
            }

            @Test
            fun `when requesting a story from JOBS, requireText is true and localCopy has retrieved text then get the story from local storage`() {
                lateinit var story: StoryResult
                val storedStory = Story(
                    id = 1,
                    time = DateTime.now(),
                    retrievedComments = true,
                    text = "Retrieved text"
                )
                val storedList = listOf(storedStory)

                every { localStorage.jobsStoryList } returns storedList

                runBlocking {
                    story = storiesRepository.story(
                        id = 1,
                        useCachedVersion = true,
                        requireComments = true,
                        storiesListType = JOBS
                    )
                }

                verify(exactly = 0) { localStorage.jobsStoryList = any() }
                coVerify(exactly = 0) { officialClient.getStory(any()) }
                verify(exactly = 0) { apiToDomainMapper.toStoryDomainModel(any(), any()) }
                verify(exactly = 0) { localStorage.topStoryList }
                verify(exactly = 0) { localStorage.askStoryList }
                verify(exactly = 0) { localStorage.newStoryList }
                verify(exactly = 0) { localStorage.showStoryList }
                assertEquals(storedStory, story.story)
            }
        }

        @Nested
        inner class UseCachedVersionFalse {

            @Test
            fun `when requesting a story from NEW then get the story from web storage, update the local storage and return web copy`() {

                lateinit var story: StoryResult
                val apiStory = ApiStory(id = 1, time = DateTime.now().toString())
                val storedStory = Story(id = 1, time = DateTime.now())
                val storedList = listOf(storedStory)

                every { apiToDomainMapper.toStoryDomainModel(any(), true) } returns storedStory
                coEvery { webStorage.story(1) } returns apiStory
                every { localStorage.newStoryList } returns storedList

                runBlocking {
                    story = storiesRepository.story(
                        id = 1,
                        useCachedVersion = false,
                        requireComments = false,
                        storiesListType = NEW
                    )
                }

                coVerifyOrder {
                    localStorage.newStoryList
                    webStorage.story(1)
                    apiToDomainMapper.toStoryDomainModel(apiStory, true)
                    localStorage.newStoryList = listOf(storedStory)
                }
                verify(exactly = 0) { localStorage.topStoryList = any() }
                verify(exactly = 0) { localStorage.askStoryList = any() }
                verify(exactly = 0) { localStorage.jobsStoryList = any() }
                verify(exactly = 0) { localStorage.showStoryList = any() }
                assertEquals(storedStory, story.story)
            }
        }
    }
}
