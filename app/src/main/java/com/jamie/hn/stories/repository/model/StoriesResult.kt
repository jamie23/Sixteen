package com.jamie.hn.stories.repository.model

import com.jamie.hn.stories.domain.model.Story

data class StoriesResult(
    val stories: List<Story>,
    val networkFailure: Boolean = false
)
