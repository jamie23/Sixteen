package com.jamie.hn.comments.di

import com.jamie.hn.articles.domain.Article
import com.jamie.hn.comments.CommentsUseCase
import com.jamie.hn.comments.net.CommentsRepository
import com.jamie.hn.comments.ui.CommentsListViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val commentsModule =
    module {
        viewModel { (article: Article) -> CommentsListViewModel(get(), article) }
        single { CommentsRepository(get()) }
        single { CommentsUseCase(get()) }
    }
