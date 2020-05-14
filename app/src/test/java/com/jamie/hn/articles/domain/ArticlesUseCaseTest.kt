package com.jamie.hn.articles.domain

import com.jamie.hn.articles.repository.ArticlesRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerifyOrder
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ArticlesUseCaseTest {

    private lateinit var articlesUseCase: ArticlesUseCase

    @RelaxedMockK
    private lateinit var articlesRepository: ArticlesRepository

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        articlesUseCase = ArticlesUseCase(articlesRepository)
    }

    @Test
    fun `when story ids are returned from the repository then get each individual story from the repository`() {
        coEvery { articlesRepository.topStories(false) } returns listOf(1, 2, 3)

        runBlocking {
            articlesUseCase.getArticles(false)
        }

        coVerifyOrder {
            articlesRepository.story(1, false)
            articlesRepository.story(2, false)
            articlesRepository.story(3, false)
        }
    }
}
