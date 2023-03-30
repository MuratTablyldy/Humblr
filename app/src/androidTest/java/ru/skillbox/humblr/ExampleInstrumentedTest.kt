package ru.skillbox.humblr

import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import ru.skillbox.humblr.data.repositories.AuthRepository
import ru.skillbox.humblr.data.repositories.SessionManager
import ru.skillbox.humblr.data.repositories.modules.AuthPropertiesModule
import java.util.logging.Logger

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("ru.skillbox.humblr", appContext.packageName)
    }
    @Test
    fun authPropertiesTest(){
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val properties= AuthPropertiesModule.provideProperties(appContext)
        assertEquals(properties.authUri,"https://www.reddit.com/api/v1/authorize")
        assertEquals(properties.scope,"identity, edit, flair, history, modconfig, modflair, modlog, modposts, modwiki, mysubreddits, " +
                "privatemessages, read, report, save, submit, subscribe, vote, wikiedit, wikiread")
    }
    @Test
    fun cheHandler(){
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val properties= AuthPropertiesModule.provideProperties(appContext)
        val repo= AuthRepository(properties, SessionManager(appContext))
        repo.handleFragment("access_token=grgr&token_type=rgrg&state=fewewefw&expires_in=rgeg")

    }
}