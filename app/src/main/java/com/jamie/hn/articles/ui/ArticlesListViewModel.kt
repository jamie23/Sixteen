package com.jamie.hn.articles.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jamie.hn.articles.domain.ArticlesUseCase
import com.jamie.hn.comments.CommentsUseCase
import kotlinx.coroutines.launch

class ArticlesListViewModel(
    private val articleDataMapper: ArticleDataMapper,
    private val articlesUseCase: ArticlesUseCase,
    // TODO: REMOVE
    private val commentsUseCase: CommentsUseCase
) : ViewModel() {

    private val articles = MutableLiveData<List<ArticleViewItem>>()
    fun articles(): LiveData<List<ArticleViewItem>> = articles

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
            commentsUseCase.getComments(id)
        }
    }
}
