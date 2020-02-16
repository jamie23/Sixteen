package com.jamie.hn.core.web

import com.jamie.hn.articles.domain.Article
import com.jamie.hn.comments.Comment
import retrofit2.http.GET
import retrofit2.http.Path

interface HackerNewsService {
    @GET("topstories.json")
    suspend fun topStories(): List<Long>

    @GET("item/{id}.json")
    suspend fun getArticle(@Path("id") id: Long): Article

    @GET("item/{id}.json")
    suspend fun getComment(@Path("id") id: Long): Comment
}
