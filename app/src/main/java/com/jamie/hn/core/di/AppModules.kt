package com.jamie.hn.core.di

import com.jamie.hn.stories.di.storiesModule
import com.jamie.hn.comments.di.commentsModule

fun modules() = listOf(
    apiModule,
    storiesModule,
    commentsModule,
    coreModule
)
