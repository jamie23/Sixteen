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

    private val articles = MutableLiveData<List<ArticleViewItem>>()
    fun articles(): LiveData<List<ArticleViewItem>> = articles

    private val navigateToComments = MutableLiveData<Event<Article>>()
    fun navigateToComments(): LiveData<Event<Article>> = navigateToComments

    fun init() {
        viewModelScope.launch {
            try {
                val results = articlesUseCase.getArticles()
                articles.value = results.map { articleDataMapper.toArticleViewItem(it, ::comments) }
            } catch (e: Exception) {
                println(e)
            }
        }
    }

    private fun comments(id: Long) {
        viewModelScope.launch {
            navigateToComments.value = Event(articlesUseCase.getArticle(id))
        }
    }
}
