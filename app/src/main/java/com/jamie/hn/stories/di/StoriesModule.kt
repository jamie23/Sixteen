package com.jamie.hn.stories.di

import com.jamie.hn.stories.domain.StoriesUseCase
import com.jamie.hn.stories.repository.StoriesRepository
import com.jamie.hn.stories.repository.local.LocalStorage
import com.jamie.hn.stories.ui.StoryDataMapper
import com.jamie.hn.stories.ui.StoryListViewModel
import com.jamie.hn.stories.ui.StoryResourceProvider
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val storiesModule =
    module {
        viewModel { StoryListViewModel(get(), get()) }
        single { StoriesRepository(get(), get()) }
        single { StoryDataMapper(get(), get()) }
        single { StoryResourceProvider(get()) }
        single { StoriesUseCase(get()) }
        single { LocalStorage() }
    }
