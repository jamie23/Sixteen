package com.jamie.hn.stories.repository.web

import com.jamie.hn.core.StoriesType
import com.jamie.hn.core.StoriesType.SHOW
import com.jamie.hn.core.StoriesType.NEW
import com.jamie.hn.core.StoriesType.JOBS
import com.jamie.hn.core.StoriesType.ASK
import com.jamie.hn.core.StoriesType.TOP

fun getWebPath(storiesType: StoriesType) =
    when (storiesType) {
        TOP -> "top"
        ASK -> "ask"
        JOBS -> "jobs"
        NEW -> "new"
        SHOW -> "show"
    }
