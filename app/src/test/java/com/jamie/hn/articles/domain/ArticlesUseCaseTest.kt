package com.jamie.hn.articles.domain

import com.jamie.hn.articles.net.ArticlesRepository
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
        coEvery { articlesRepository.topStories()} returns listOf(1, 2, 3)

        runBlocking {
            articlesUseCase.getArticles()
        }

        coVerifyOrder {
            articlesRepository.story(1)
            articlesRepository.story(2)
            articlesRepository.story(3)
        }
    }
}