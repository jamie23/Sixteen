package com.jamie.hn.articles.net

import com.jamie.hn.articles.domain.Article
import com.jamie.hn.core.net.HackerNewsService

class ArticlesRepository(
    private val hnService: HackerNewsService
) {

    suspend fun topStories() = hnService.topStories().take(20)

    suspend fun story(id: Long): Article {
        return hnService.getArticle(id)
    }
}
