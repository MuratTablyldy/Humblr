package ru.skillbox.humblr.data.repositories

import android.app.Application
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class Networking @Inject constructor(val application: Application) {

    private val okHttpClient = OkHttpClient.Builder()
        .cache(
            Cache(
                directory = File(application.cacheDir, "http_cache"),
                maxSize = 50L * 1024L * 1024L
            )
        )
        .connectionPool(ConnectionPool(0, 5, TimeUnit.MINUTES))
        .protocols(listOf(Protocol.HTTP_1_1))
        .addNetworkInterceptor(
            HttpLoggingInterceptor(HttpLoggingInterceptor.Logger.DEFAULT).setLevel(
                HttpLoggingInterceptor.Level.BODY
            )
        )
        .build()
    private val retrofit = Retrofit.Builder()
        .client(okHttpClient)
        .baseUrl("https://oauth.reddit.com")
        .addConverterFactory(MoshiConverterFactory.create())
        .build()
    val repository: RedditApi
        get() = retrofit.create()
    private val retrofitSim = Retrofit.Builder()
        .client(okHttpClient)
        .baseUrl("https://reddit.com")
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    val repositorySearch: RedditApiSIMPLE
        get() = retrofitSim.create()

}