package com.jamie.hn.articles.repository.local

import com.jamie.hn.articles.domain.Article

data class LocalStorage(
    var listArticleIds: List<Long> = listOf(),
    val listArticles: HashMap<Long, Article> = hashMapOf()
)
