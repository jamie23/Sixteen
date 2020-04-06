package com.jamie.hn.articles.di

import com.jamie.hn.articles.domain.ArticlesUseCase
import com.jamie.hn.articles.repository.ArticlesRepository
import com.jamie.hn.articles.repository.local.LocalStorage
import com.jamie.hn.articles.ui.ArticleDataMapper
import com.jamie.hn.articles.ui.ArticlesListViewModel
import com.jamie.hn.articles.ui.ArticleResourceProvider
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val articlesModule =
    module {
        viewModel { ArticlesListViewModel(get(), get()) }
        single { ArticlesRepository(get(), get()) }
        single { ArticleDataMapper(get(), get()) }
        single { ArticleResourceProvider(get()) }
        single { ArticlesUseCase(get()) }
        single { LocalStorage() }
    }
