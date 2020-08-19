package com.jamie.hn.stories.repository

import com.jamie.hn.core.net.hex.Hex
import com.jamie.hn.stories.domain.model.Story
import com.jamie.hn.stories.repository.local.LocalStorage
import com.jamie.hn.stories.repository.model.ApiStory
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class StoriesRepositoryTest {

    @MockK
    private lateinit var webStorage: Hex

    @MockK
    private lateinit var apiToDomainMapper: ApiToDomainMapper

    @RelaxedMockK
    private lateinit var localStorage: LocalStorage

    private lateinit var storiesRepository: StoriesRepository

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        storiesRepository =
            StoriesRepository(webStorage, localStorage, apiToDomainMapper)
    }

    @Nested
    inner class GetTopStories {

        @Test
        fun `when useCachedVersion is true and local storage is not empty then get the list of stories from local storage`() {
            var topStories = listOf<Story>()
            val storedList = listOf(Story(time = DateTime.now()))

            every { localStorage.storyList } returns storedList

            runBlocking {
                topStories = storiesRepository.topStories(true)
            }

            assertEquals(storedList, topStories)
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
            var topStories = emptyList<Story>()

            every { localStorage.storyList } returns listOf()
            coEvery { webStorage.topStories() } returns webList
            every { apiToDomainMapper.toStoryDomainModel(apiStory) } returns Story(time = date)

            runBlocking {
                topStories = storiesRepository.topStories(true)
            }

            verify(exactly = 1) { localStorage.storyList }
            verify { localStorage.storyList = listOf(Story(time = date)) }
            assertEquals(listOf(Story(time = date)), topStories)
        }

        @Test
        fun `when useCachedVersion is false then get top stories from web and store them in local storage and then return them`() {
            val date = DateTime.parse(
                "23/08/2020 09:00:00",
                DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss")
            )
            val apiStory = ApiStory(time = date.toString())
            val webList = listOf(apiStory)
            var topStories = emptyList<Story>()

            coEvery { webStorage.topStories() } returns webList
            every { apiToDomainMapper.toStoryDomainModel(apiStory) } returns Story(time = date)

            runBlocking {
                topStories = storiesRepository.topStories(false)
            }

            verify(exactly = 0) { localStorage.storyList }
            verify { localStorage.storyList = listOf(Story(time = date)) }
            verify { apiToDomainMapper.toStoryDomainModel(any(), false) }
            assertEquals(listOf(Story(time = date)), topStories)
        }
    }

    @Nested
    inner class GetStory {

        @Test
        fun `when useCachedVersion is true and local storage is not null and requireComments is false then get story from local storage`() {
            val storedStory = Story(id = 1, time = DateTime.now())
            val storedList = listOf(storedStory)
            var story = Story(time = DateTime.now())

            every { localStorage.storyList } returns storedList

            runBlocking {
                story = storiesRepository.story(
                    id = 1,
                    useCachedVersion = true,
                    requireComments = false
                )
            }

            verify(exactly = 0) { localStorage.storyList = any() }
            assertEquals(storedStory, story)
        }

        @Test
        fun `when useCachedVersion is true and local storage doesn't contain the story then get the story from web storage, update the local storage and return web copy`() {
            val storedStory = Story(id = 1, time = DateTime.now())
            var story = Story(time = DateTime.now())
            val apiStory = ApiStory(time = DateTime.now().toString())

            every { localStorage.storyList } returns emptyList()
            every { apiToDomainMapper.toStoryDomainModel(any(), true) } returns storedStory
            coEvery { webStorage.story(1) } returns apiStory

            runBlocking {
                story = storiesRepository.story(
                    id = 1,
                    useCachedVersion = true,
                    requireComments = false
                )
            }

            coVerifyOrder {
                localStorage.storyList
                webStorage.story(1)
                apiToDomainMapper.toStoryDomainModel(apiStory, true)
                localStorage.storyList
                localStorage.storyList = any()
            }
            assertEquals(storedStory, story)
        }

        @Test
        fun `when useCachedVersion is true and requireComments is true and localCopy has not retrieved comments then get the story from web storage and replace the local story with the new one`() {
            val storedStory = Story(id = 1, time = DateTime.now(), retrievedComments = false)
            val newStoredStory = Story(id = 1, time = DateTime.now(), retrievedComments = true)
            val storedList = listOf(storedStory)
            val apiStory = ApiStory(time = DateTime.now().toString())
            var story = Story(time = DateTime.now())

            every { localStorage.storyList } returns storedList
            every { apiToDomainMapper.toStoryDomainModel(any(), true) } returns newStoredStory
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
                apiToDomainMapper.toStoryDomainModel(apiStory, true)
                localStorage.storyList
                localStorage.storyList = listOf(newStoredStory)
            }
            assertEquals(newStoredStory, story)
        }

        @Test
        fun `when useCachedVersion is true and requireComments is true and localCopy has retrieved comments then get the story from local storage`() {
            var story = Story(time = DateTime.now())
            val storedStory = Story(id = 1, time = DateTime.now(), retrievedComments = true)
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
            verify(exactly = 0) { apiToDomainMapper.toStoryDomainModel(any(), any()) }
            assertEquals(storedStory, story)
        }

        @Test
        fun `when useCachedVersion is false then get the story from web storage, update the local storage and return web copy`() {
            val apiStory = ApiStory(time = DateTime.now().toString())
            var story = Story(time = DateTime.now())
            val storedStory = Story(id = 1, time = DateTime.now())

            every { apiToDomainMapper.toStoryDomainModel(any(), true) } returns storedStory
            coEvery { webStorage.story(1) } returns apiStory

            runBlocking {
                story = storiesRepository.story(
                    id = 1,
                    useCachedVersion = false,
                    requireComments = false
                )
            }

            coVerifyOrder {
                webStorage.story(1)
                apiToDomainMapper.toStoryDomainModel(apiStory, true)
                localStorage.storyList
                localStorage.storyList = any()
            }
            assertEquals(storedStory, story)
        }
    }
}
