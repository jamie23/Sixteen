package com.jamie.hn.stories.repository.local

import com.jamie.hn.stories.domain.model.Story

interface LocalSource {
    var topStoryList: List<Story>
    var askStoryList: List<Story>
    var jobsStoryList: List<Story>
    var newStoryList: List<Story>
    var showStoryList: List<Story>
}
