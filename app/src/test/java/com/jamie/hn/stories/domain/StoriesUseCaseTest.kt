package com.jamie.hn.stories.domain

import com.jamie.hn.stories.repository.StoriesRepository
import io.mockk.MockKAnnotations
import io.mockk.coVerify
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
    fun `when getStories is called then call the repository passing in the cache variable`() {
        runBlocking {
            storiesUseCase.getStories(true)
        }

        coVerify { storiesRepository.topStories(true) }
    }

    @Test
    fun `when getStory is called then call the repository passing in the cache variable and correct id`() {
        runBlocking {
            storiesUseCase.getStory(1, true)
        }

        coVerify { storiesRepository.story(1, true) }
    }
}
