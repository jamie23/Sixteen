package com.jamie.hn.articles.repository

import com.jamie.hn.articles.domain.Article
import com.jamie.hn.articles.repository.local.LocalStorage
import com.jamie.hn.core.net.HackerNewsService

class ArticlesRepository(
    private val webStorage: HackerNewsService,
    private val localStorage: LocalStorage
) : Repository {

    override suspend fun topStories(useCachedVersion: Boolean): List<Long> {
        if (useCachedVersion) {
            val localCopy = localStorage.listArticleIds
            if (localCopy.isNotEmpty()) return localCopy
        }

        val newCopy = webStorage.topStories().take(20)
        localStorage.listArticleIds = newCopy
        return newCopy
    }

    override suspend fun story(id: Long, useCachedVersion: Boolean): Article {

        if (useCachedVersion) {
            val localCopy = localStorage.listArticles[id]
            if (localCopy != null) return localCopy
        }

        val newCopy = webStorage.getArticle(id)
        localStorage.listArticles[newCopy.id] = newCopy
        return newCopy
    }
}
