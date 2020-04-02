package com.jamie.hn.articles.repository.local

import com.jamie.hn.articles.domain.Article

data class LocalStorage(
    val listArticles: HashMap<Long, Article> = hashMapOf()
)
