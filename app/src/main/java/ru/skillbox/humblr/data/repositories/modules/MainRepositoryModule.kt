package ru.skillbox.humblr.data.repositories.modules

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create
import ru.skillbox.humblr.data.repositories.FetchSessionManager
import ru.skillbox.humblr.data.repositories.MainRepository
import ru.skillbox.humblr.data.repositories.Networking
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object MainRepositoryModule {
    @Provides
    @Singleton
    fun provideFetchSessionManager(@ApplicationContext context:Context):FetchSessionManager{
        return FetchSessionManager(context)
    }
    @Provides
    @Singleton
    fun getRepo(application: Application,sessionManage:FetchSessionManager):MainRepository{
        return MainRepository(sessionManage, Networking(application))
    }


}