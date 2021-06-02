package com.jamie.hn.core.net.official

import com.jamie.hn.stories.repository.model.ApiAskText
import retrofit2.http.GET
import retrofit2.http.Path

interface OfficialClient {
    @GET("item/{id}.json")
    suspend fun getStory(@Path("id") id: Int): ApiAskText
}
