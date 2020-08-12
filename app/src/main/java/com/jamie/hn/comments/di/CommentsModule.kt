package com.jamie.hn.comments.di

import com.jamie.hn.comments.domain.CommentsUseCase
import com.jamie.hn.comments.ui.CommentDataMapper
import com.jamie.hn.comments.ui.CommentsListViewModel
import com.jamie.hn.comments.ui.CommentsResourceProvider
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val commentsModule =
    module {
        viewModel { (storyId: Long) -> CommentsListViewModel(get(), storyId, get()) }
        single { CommentDataMapper(get(), get()) }
        single { CommentsResourceProvider(get()) }
        single { CommentsUseCase(get()) }
    }
