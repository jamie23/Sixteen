package com.jamie.hn.core.di

import com.jamie.hn.core.net.hex.Hex
import com.jamie.hn.core.net.official.OfficialClient
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val apiModule = module {
    single<OfficialClient> {
        Retrofit.Builder()
            .baseUrl("https://hacker-news.firebaseio.com/v0/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OfficialClient::class.java)
    }

    single<Hex> {
        Retrofit.Builder()
            .baseUrl("http://api.hexforhn.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(Hex::class.java)
    }
}
