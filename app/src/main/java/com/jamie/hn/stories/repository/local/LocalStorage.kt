package com.jamie.hn.stories.repository.local

import com.jamie.hn.stories.domain.model.Story

data class LocalStorage(
    var topStoryList: List<Story> = listOf(),
    var askStoryList: List<Story> = listOf(),
    var jobsStoryList: List<Story> = listOf(),
    var newStoryList: List<Story> = listOf(),
    var showStoryList: List<Story> = listOf()
)
