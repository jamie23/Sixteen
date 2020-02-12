package com.jamie.hn.core.di

import com.jamie.hn.articles.di.articlesModule

fun modules() = listOf(
    apiModule,
    articlesModule,
    coreModule
)
