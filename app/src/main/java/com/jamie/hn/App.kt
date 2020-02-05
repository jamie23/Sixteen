package com.jamie.hn

import android.app.Application
import com.jamie.hn.core.di.modules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App: Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@App)
            modules(modules())
        }
    }
}