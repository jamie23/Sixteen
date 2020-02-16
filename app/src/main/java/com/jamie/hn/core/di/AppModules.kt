package com.jamie.hn.core.di

import com.jamie.hn.articles.di.articlesModule
import com.jamie.hn.comments.di.commentsModule

fun modules() = listOf(
    apiModule,
    articlesModule,
    commentsModule,
    coreModule
)
