package com.jamie.hn.stories.repository

import com.jamie.hn.stories.repository.local.LocalStorage
import com.jamie.hn.core.net.official.HackerNewsService
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.random.Random

class ArticlesRepositoryTest {

    @MockK
    private lateinit var webStorage: HackerNewsService

    @RelaxedMockK
    private lateinit var localStorage: LocalStorage

    private lateinit var storiesRepository: StoriesRepository

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        storiesRepository =
            StoriesRepository(webStorage, localStorage)
    }

    @Nested
    inner class TopStories {

        @Test
        fun `when useCachedVersion is true and local storage is not empty then get the ids from local storage and return the local copy`() {
            var topStories = listOf<Long>()
            val storedList = listOf<Long>(1, 2, 3)

            every { localStorage.listArticleIds } returns storedList

            runBlocking {
                topStories = storiesRepository.topStories(true)
            }

            assertEquals(storedList, topStories)
        }

        @Test
        fun `when useCachedVersion is true and local storage is empty then get the ids from web storage, update the local storage and return web copy`() {
            var topStories = listOf<Long>()
            val webList = listOf<Long>(1, 2, 3)

            every { localStorage.listArticleIds } returns listOf()
            coEvery { webStorage.topStories() } returns webList

            runBlocking {
                topStories = storiesRepository.topStories(true)
            }

            verify { localStorage.listArticleIds = webList }
            assertEquals(webList, topStories)
        }

        @Test
        fun `when useCachedVersion is false then get top 20 stories from web and store them in local storage and then return them `() {
            var topStories = listOf<Long>()
            val storedList = listOf<Long>(1, 2, 3)
            val webList = List(30) { Random.nextLong() }

            every { localStorage.listArticleIds } returns storedList
            coEvery { webStorage.topStories() } returns webList

            runBlocking {
                topStories = storiesRepository.topStories(false)
            }

            val top20Results = webList.take(20)

            verify { localStorage.listArticleIds = top20Results }
            assertEquals(top20Results, topStories)
        }
    }

    @Nested
    inner class ApiStory {

        @Test
        fun `when useCachedVersion is true and local storage is not empty then get the article from local storage and return the local copy`() {
            var story = com.jamie.hn.stories.domain.model.Story()
            val storedStory = com.jamie.hn.stories.domain.model.Story()

            runBlocking {
                storiesRepository.story(0, true)
            }
        }
    }
}
