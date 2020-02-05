package com.jamie.hn.articles.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jamie.hn.articles.domain.Article
import com.jamie.hn.articles.net.ArticlesRepository
import kotlinx.coroutines.*

class ArticleListViewModel(
    private val articlesRepository: ArticlesRepository,
    private val articleDataMapper: ArticleDataMapper
) : ViewModel() {

    private val articles = MutableLiveData<List<ArticleViewItem>>()
    fun articles(): LiveData<List<ArticleViewItem>> = articles

    fun init() {
        viewModelScope.launch {
            try {
                val storyIds = articlesRepository.topStories()
                val listStories = mutableListOf<Deferred<Article>>()

                storyIds.forEach {
                    listStories.add(
                        async {
                            println("Jamie id: $it")
                            articlesRepository.story(it)
                        }
                    )
                }

                val results = listStories.awaitAll()

                articles.value = results.map { articleDataMapper.toArticleViewItem(it) }
            } catch (e: Exception) {
                println(e)
            }
        }
    }
}
