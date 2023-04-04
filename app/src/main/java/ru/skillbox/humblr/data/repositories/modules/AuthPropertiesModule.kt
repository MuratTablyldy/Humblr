package ru.skillbox.humblr.data.repositories.modules

import android.content.Context
import android.content.res.AssetManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ru.skillbox.humblr.data.repositories.AuthProperties
import ru.skillbox.humblr.data.repositories.SessionManager
import java.io.InputStream
import java.util.*
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object AuthPropertiesModule {
    private var authProperties: AuthProperties? = null

    @Provides
    @Singleton
    fun provideProperties(@ApplicationContext context: Context): AuthProperties {
        val properties = Properties()
        var inputStream: InputStream? = null
        val assetManager: AssetManager
        try {
            assetManager = context.assets
            inputStream = assetManager?.open("auth.properties")
            properties.load(inputStream)
            val clientId = properties.getProperty("client_id")
            val scope = properties.getProperty("scope")
            val redirectUri = properties.getProperty("redirect_uri")
            val baseUri = properties.getProperty("base_uri")
            val duration = properties.getProperty("duration")
            val responseType = properties.getProperty("response_type")
            val uuid = UUID.randomUUID().toString()
            val authUri = properties.getProperty("auth_uri")
            val tokenEndPint = properties.getProperty("token_uri")
            val pathRevokeToken = properties.getProperty("revoke_path")
            authProperties = AuthProperties(
                clientId = clientId,
                scope = scope,
                redirectUri = redirectUri,
                baseUri = baseUri,
                duration = duration,
                responseType = responseType,
                uuid = uuid,
                authUri = authUri,
                tokenEndPoint = tokenEndPint,
                state = UUID.randomUUID().toString(),
                revokePath = pathRevokeToken
            )
            return authProperties!!
        } finally {
            inputStream?.close()
        }
    }

    @Provides
    @Singleton
    fun provideSessionManager(@ApplicationContext context: Context): SessionManager {
        return SessionManager(context = context)
    }
}