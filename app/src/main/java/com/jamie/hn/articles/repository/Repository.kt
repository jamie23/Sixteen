package com.jamie.hn.articles.repository

import com.jamie.hn.articles.domain.Article

interface Repository {
    suspend fun topStories(): List<Long>
    suspend fun story(id: Long): Article
}
