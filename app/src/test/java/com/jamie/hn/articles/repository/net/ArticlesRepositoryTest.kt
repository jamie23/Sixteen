package com.jamie.hn.articles.repository.net

import com.jamie.hn.articles.domain.Article
import com.jamie.hn.articles.repository.ArticlesRepository
import com.jamie.hn.core.net.HackerNewsService
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ArticlesRepositoryTest {
    @MockK
    private lateinit var hnService: HackerNewsService

    private lateinit var articlesRepository: ArticlesRepository

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        articlesRepository =
            ArticlesRepository(hnService)
    }

    @Test
    fun `when topStories is called then we call topStories on the service`() {
        coEvery { hnService.topStories() } returns listOf(1, 2, 3)

        runBlocking {
            articlesRepository.topStories()
        }

        coVerify { hnService.topStories() }
    }

    @Test
    fun `when story is called then we return the article provided by the service `() {
        val article = Article()
        lateinit var resultArticle: Article

        coEvery { hnService.getArticle(1) } returns article

        runBlocking {
            resultArticle = articlesRepository.story(1)
        }

        coVerify { hnService.getArticle(1) }
        assertEquals(article, resultArticle)
    }
}
