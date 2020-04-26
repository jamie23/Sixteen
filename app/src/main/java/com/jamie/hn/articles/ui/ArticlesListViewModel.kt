package com.jamie.hn.articles.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jamie.hn.articles.domain.Article
import com.jamie.hn.articles.domain.ArticlesUseCase
import com.jamie.hn.core.Event
import kotlinx.coroutines.launch

class ArticlesListViewModel(
    private val articleDataMapper: ArticleDataMapper,
    private val articlesUseCase: ArticlesUseCase
) : ViewModel() {

    private val articleViewState = MutableLiveData<ArticlesViewState>()
    fun articleViewState(): LiveData<ArticlesViewState> = articleViewState

    private val navigateToComments = MutableLiveData<Event<Article>>()
    fun navigateToComments(): LiveData<Event<Article>> = navigateToComments

    private val refreshing = MutableLiveData<Boolean>()
    fun refreshing(): LiveData<Boolean> = refreshing

    fun init() {
        refreshList()
    }

    fun refreshList() {
        refreshing.value = true
        articleViewState.value = ArticlesViewState(emptyList(), false)

        viewModelScope.launch {
            try {
                val results = articlesUseCase.getArticles()
                articleViewState.value = ArticlesViewState(
                    results.map { articleDataMapper.toArticleViewItem(it, ::comments) },
                    true
                )
            } catch (e: Exception) {
                println(e)
            } finally {
                refreshing.value = false
            }
        }
    }

    private fun comments(id: Long) {
        viewModelScope.launch {
            navigateToComments.value = Event(articlesUseCase.getArticle(id))
        }
    }

    data class ArticlesViewState(
        val articles: List<ArticleViewItem>,
        val visible: Boolean
    )
}
