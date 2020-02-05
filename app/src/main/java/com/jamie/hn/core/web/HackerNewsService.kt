package com.jamie.hn.core.web

import com.jamie.hn.articles.domain.Article
import retrofit2.http.GET
import retrofit2.http.Path

interface HackerNewsService {
    @GET("topstories.json")
    suspend fun topStories(): List<Int>

    @GET("item/{id}.json")
    suspend fun getStory(@Path("id") id: Int): Article
}