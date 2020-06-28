package com.jamie.hn.stories.repository.local

import com.jamie.hn.stories.domain.model.Story

data class LocalStorage(
    var storyList: List<Story> = listOf()
)
