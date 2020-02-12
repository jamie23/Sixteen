package com.jamie.hn.core.di

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val coreModule = module {
    single { androidContext().resources }
}
