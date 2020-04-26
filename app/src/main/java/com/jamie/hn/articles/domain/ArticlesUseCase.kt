package com.jamie.hn.articles.domain

import com.jamie.hn.articles.repository.ArticlesRepository
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext

class ArticlesUseCase(
    private val articlesRepository: ArticlesRepository
) {

    suspend fun getArticles(): List<Article> {
        try {
            val storyIds = articlesRepository.topStories()
            val listArticles = mutableListOf<Deferred<Article>>()

            withContext(Dispatchers.IO) {
                storyIds.forEach {
                    listArticles.add(
                        async {
                            articlesRepository.story(it, false)
                        })
                }
            }

            return listArticles.awaitAll()
        } catch (e: Exception) {
            println(e)
        }

        return emptyList()
    }

    suspend fun getArticle(id: Long) =
        articlesRepository.story(id, true)
}
