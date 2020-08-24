package com.jamie.hn.core.di

import com.jamie.hn.core.net.NetworkUtils
import com.jamie.hn.core.ui.CoreDataMapper
import com.jamie.hn.core.ui.CoreResourceProvider
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val coreModule = module {
    single { androidContext().resources }
    single { CoreDataMapper(get()) }
    single { CoreResourceProvider(get()) }
    single { NetworkUtils(get()) }
}
