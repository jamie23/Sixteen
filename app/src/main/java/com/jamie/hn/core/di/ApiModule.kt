package com.jamie.hn.core.di

import com.jamie.hn.core.web.HackerNewsService
import com.jamie.hn.core.web.IconService
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val apiModule = module {
    single<HackerNewsService> {
        Retrofit.Builder()
            .baseUrl("https://hacker-news.firebaseio.com/v0/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(HackerNewsService::class.java)
    }

    single<IconService> {
        Retrofit.Builder()
            .baseUrl("https://besticon-demo.herokuapp.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(IconService::class.java)
    }
}
