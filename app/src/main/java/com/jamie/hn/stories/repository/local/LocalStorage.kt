package com.jamie.hn.stories.repository.local

import com.jamie.hn.stories.domain.model.Story

data class LocalStorage(
    override var topStoryList: List<Story> = listOf(),
    override var askStoryList: List<Story> = listOf(),
    override var jobsStoryList: List<Story> = listOf(),
    override var newStoryList: List<Story> = listOf(),
    override var showStoryList: List<Story> = listOf()
) : LocalSource
