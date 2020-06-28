package com.jamie.hn.comments.di

import com.jamie.hn.comments.ui.CommentDataMapper
import com.jamie.hn.comments.ui.CommentsListViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val commentsModule =
    module {
        viewModel { (storyId: Long) -> CommentsListViewModel(get(), get(), storyId) }
        single { CommentDataMapper(get()) }
    }
