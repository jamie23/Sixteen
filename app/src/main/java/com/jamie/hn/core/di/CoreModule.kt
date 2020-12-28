package com.jamie.hn.core.di

import com.jamie.hn.hostactivity.ui.MainActivityResourceProvider
import com.jamie.hn.hostactivity.ui.MainActivityViewModel
import com.jamie.hn.core.net.NetworkUtils
import com.jamie.hn.core.ui.CoreDataMapper
import com.jamie.hn.core.ui.CoreResourceProvider
import com.jamie.hn.core.ui.SharedNavigationViewModel
import com.jamie.hn.hostactivity.repository.local.PreferencesRepository
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val coreModule = module {
    single { androidContext().resources }
    single { CoreDataMapper(get()) }
    single { CoreResourceProvider(get()) }
    single { MainActivityResourceProvider() }
    single { NetworkUtils(get()) }
    single { PreferencesRepository(get()) }
    viewModel { MainActivityViewModel(get(), get()) }
    viewModel { SharedNavigationViewModel() }
}
