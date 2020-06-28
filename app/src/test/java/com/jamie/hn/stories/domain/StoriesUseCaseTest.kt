package com.jamie.hn.stories.domain

import com.jamie.hn.stories.repository.StoriesRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerifyOrder
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class StoriesUseCaseTest {

    private lateinit var storiesUseCase: StoriesUseCase

    @RelaxedMockK
    private lateinit var storiesRepository: StoriesRepository

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        storiesUseCase = StoriesUseCase(storiesRepository)
    }

    @Test
    fun `when story ids are returned from the repository then get each individual story from the repository`() {
        coEvery { storiesRepository.topStories(false) } returns listOf(1, 2, 3)

        runBlocking {
            storiesUseCase.getStories(false)
        }

        coVerifyOrder {
            storiesRepository.story(1, false)
            storiesRepository.story(2, false)
            storiesRepository.story(3, false)
        }
    }
}
