package com.jamie.hn.stories.ui

import androidx.lifecycle.Observer
import com.jamie.hn.stories.domain.model.Story
import com.jamie.hn.stories.domain.StoriesUseCase
import com.jamie.hn.stories.ui.StoryListViewModel.ArticlesViewState
import com.jamie.hn.core.BaseTest
import com.jamie.hn.core.InstantExecutorExtension
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.lang.Exception

@ExtendWith(InstantExecutorExtension::class)
class ApiStoryListViewModelTest : BaseTest() {
    @RelaxedMockK
    private lateinit var articlesDataMapper: StoryDataMapper
    @MockK
    private lateinit var storiesUseCase: StoriesUseCase

    private lateinit var storyListViewModel: StoryListViewModel

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        storyListViewModel = StoryListViewModel(articlesDataMapper, storiesUseCase)
    }

    @Nested
    inner class Init {

        @Test
        fun `when use case returns multiple articles then map those to view items and post to the view`() {
            coEvery { storiesUseCase.getStories(false) } returns listOf(
                Story(),
                Story(),
                Story()
            )

            val observer = mockk<Observer<ArticlesViewState>>()
            storyListViewModel.articleViewState().observeForever(observer)

//            articlesListViewModel.init()

            verify(exactly = 3) { articlesDataMapper.toStoryViewItem(any(), any(), any()) }
            verify(exactly = 1) { observer.onChanged(any()) }
        }

        @Test
        fun `when use case returns throws exception then do not post items to view`() {
            coEvery { storiesUseCase.getStories(true) } throws Exception()

            val observer = mockk<Observer<ArticlesViewState>>()
            storyListViewModel.articleViewState().observeForever(observer)

//            articlesListViewModel.init()

            verify(exactly = 0) { articlesDataMapper.toStoryViewItem(any(), any(), any()) }
            verify(exactly = 0) { observer.onChanged(any()) }
        }
    }
}
