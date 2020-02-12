package com.jamie.hn.articles.ui

import androidx.lifecycle.Observer
import com.jamie.hn.articles.domain.Article
import com.jamie.hn.articles.domain.ArticlesUseCase
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
class ArticlesListViewModelTest : BaseTest() {
    @RelaxedMockK
    private lateinit var articlesDataMapper: ArticleDataMapper
    @MockK
    private lateinit var articlesUseCase: ArticlesUseCase

    private lateinit var articlesListViewModel: ArticlesListViewModel

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        articlesListViewModel = ArticlesListViewModel(articlesDataMapper, articlesUseCase)
    }

    @Nested
    inner class Init {

        @Test
        fun `when use case returns multiple articles then map those to view items and post to the view`() {
            coEvery { articlesUseCase.getArticles() } returns listOf(
                Article(),
                Article(),
                Article()
            )

            val observer = mockk<Observer<List<ArticleViewItem>>>(relaxed = true)
            articlesListViewModel.articles().observeForever(observer)

            articlesListViewModel.init()

            verify(exactly = 3) { articlesDataMapper.toArticleViewItem(any()) }
            verify(exactly = 1) { observer.onChanged(any()) }
        }

        @Test
        fun `when use case returns throws exception then do not post items to view`() {
            coEvery { articlesUseCase.getArticles() } throws Exception()

            val observer = mockk<Observer<List<ArticleViewItem>>>()
            articlesListViewModel.articles().observeForever(observer)

            articlesListViewModel.init()

            verify(exactly = 0) { articlesDataMapper.toArticleViewItem(any()) }
            verify(exactly = 0) { observer.onChanged(any()) }
        }
    }
}
