package com.jamie.hn.articles.net

import com.jamie.hn.articles.domain.Article
import com.jamie.hn.core.web.HackerNewsService
import com.jamie.hn.core.web.IconService

class ArticlesRepository(
    private val hnService: HackerNewsService,
    private val iconService: IconService
) {

    suspend fun topStories() = hnService.topStories().take(20)

    suspend fun story(id: Long): Article {
        return hnService.getArticle(id)
    }

    suspend fun iconList(url: String) = iconService.icons(url)
}
