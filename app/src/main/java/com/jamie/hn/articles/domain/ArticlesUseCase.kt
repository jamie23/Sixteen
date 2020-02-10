package com.jamie.hn.articles.domain

import com.jamie.hn.articles.net.ArticlesRepository
import kotlinx.coroutines.*

class ArticlesUseCase(
    private val articlesRepository: ArticlesRepository
) {

    suspend fun getArticles(): List<Article> {
        val storyIds = articlesRepository.topStories()

        val listStories = mutableListOf<Deferred<Article>>()

        withContext(Dispatchers.IO) {
            storyIds.forEach {
                listStories.add(
                    async {
                        articlesRepository.story(it)
                    })
            }
        }
        return listStories.awaitAll()
    }
}