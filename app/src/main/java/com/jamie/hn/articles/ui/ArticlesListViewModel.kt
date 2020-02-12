package com.jamie.hn.articles.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jamie.hn.articles.domain.ArticlesUseCase
import kotlinx.coroutines.*

class ArticlesListViewModel(
    private val articleDataMapper: ArticleDataMapper,
    private val articlesUseCase: ArticlesUseCase
) : ViewModel() {

    private val articles = MutableLiveData<List<ArticleViewItem>>()
    fun articles(): LiveData<List<ArticleViewItem>> = articles

    fun init() {
        viewModelScope.launch {
            try {
                val results = articlesUseCase.getArticles()
                articles.value = results.map { articleDataMapper.toArticleViewItem(it) }
            } catch (e: Exception) {
                println(e)
            }
        }
    }
}
