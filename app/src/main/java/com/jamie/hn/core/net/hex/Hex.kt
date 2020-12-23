package com.jamie.hn.core.net.hex

import com.jamie.hn.stories.repository.model.ApiStory
import retrofit2.http.GET
import retrofit2.http.Path

interface Hex {
    @GET("stories/{storiesType}")
    suspend fun stories(@Path("storiesType") storiesType: String): List<ApiStory>

    @GET("story/{id}")
    suspend fun story(@Path("id") id: Int): ApiStory
}
