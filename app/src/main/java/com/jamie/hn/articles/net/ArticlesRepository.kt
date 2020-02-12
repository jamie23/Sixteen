package com.jamie.hn.articles.net

import com.jamie.hn.articles.domain.Article
import com.jamie.hn.core.web.HackerNewsService

class ArticlesRepository(
    private val service: HackerNewsService
) {

    suspend fun topStories() = service.topStories().take(20)

    suspend fun story(id: Int): Article {
        return service.getStory(id)
    }
}
