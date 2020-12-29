package com.jamie.hn.stories.repository.web

import com.jamie.hn.core.StoriesListType
import com.jamie.hn.core.StoriesListType.SHOW
import com.jamie.hn.core.StoriesListType.NEW
import com.jamie.hn.core.StoriesListType.JOBS
import com.jamie.hn.core.StoriesListType.ASK
import com.jamie.hn.core.StoriesListType.TOP

fun getWebPath(storiesListType: StoriesListType) =
    when (storiesListType) {
        TOP -> "top"
        ASK -> "ask"
        JOBS -> "jobs"
        NEW -> "new"
        SHOW -> "show"
    }
