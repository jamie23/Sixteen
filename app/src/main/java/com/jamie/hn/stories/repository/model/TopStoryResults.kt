package com.jamie.hn.stories.repository.model

import com.jamie.hn.stories.domain.model.Story

data class TopStoryResults(
    val stories: List<Story>,
    val networkFailure: Boolean = false
)
