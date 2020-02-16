package com.jamie.hn.articles.domain

import com.jamie.hn.articles.net.ArticlesRepository
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext

class ArticlesUseCase(
    private val articlesRepository: ArticlesRepository
) {

    suspend fun getArticles(): List<Article> {
        val storyIds = articlesRepository.topStories()

        val listArticles = mutableListOf<Deferred<Article>>()

        withContext(Dispatchers.IO) {
            storyIds.forEach {
                listArticles.add(
                    async {
                        articlesRepository.story(it)
                    })
            }
        }

        return listArticles.awaitAll()
    }
}
