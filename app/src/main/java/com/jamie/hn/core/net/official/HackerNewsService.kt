package com.jamie.hn.core.net.official

import com.jamie.hn.stories.domain.model.Story
import com.jamie.hn.comments.domain.model.Comment
import retrofit2.http.GET
import retrofit2.http.Path

interface HackerNewsService {
    @GET("topstories.json")
    suspend fun topStories(): List<Long>

    @GET("item/{id}.json")
    suspend fun getArticle(@Path("id") id: Long): Story

    @GET("item/{id}.json")
    suspend fun getComment(@Path("id") id: Long): Comment
}
