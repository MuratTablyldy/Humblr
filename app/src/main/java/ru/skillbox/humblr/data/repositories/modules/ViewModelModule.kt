package ru.skillbox.humblr.data.repositories.modules

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import net.openid.appauth.AuthorizationService

@InstallIn(ViewModelComponent::class)
@Module
class ViewModelModule {
    @Provides
    fun provideAuthService(@ApplicationContext context: Context): AuthorizationService {
        return AuthorizationService(context)
    }
}