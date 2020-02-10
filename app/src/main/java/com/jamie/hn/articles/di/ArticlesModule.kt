package com.jamie.hn.articles.di

import com.jamie.hn.articles.domain.ArticlesUseCase
import com.jamie.hn.articles.net.ArticlesRepository
import com.jamie.hn.articles.ui.ArticleDataMapper
import com.jamie.hn.articles.ui.ArticleListViewModel
import com.jamie.hn.articles.ui.ArticleResourceProvider
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val articlesModule =
    module {
        viewModel { ArticleListViewModel(get(), get()) }
        single { ArticlesRepository(get()) }
        single { ArticleDataMapper(get()) }
        single { ArticleResourceProvider(get()) }
        single { ArticlesUseCase(get()) }
    }