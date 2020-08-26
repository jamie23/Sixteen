package com.jamie.hn.stories.repository.model

import com.jamie.hn.stories.domain.model.Story

data class StoryResults(
    val story: Story,
    val networkFailure: Boolean = false
)
