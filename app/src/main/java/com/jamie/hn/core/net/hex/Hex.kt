package com.jamie.hn.core.net.hex

import com.jamie.hn.stories.repository.model.ApiStory
import retrofit2.http.GET
import retrofit2.http.Path

interface Hex {
    @GET("stories/{storiesListType}")
    suspend fun stories(@Path("storiesListType") storiesListType: String): List<ApiStory>

    @GET("story/{id}")
    suspend fun story(@Path("id") id: Int): ApiStory
}
