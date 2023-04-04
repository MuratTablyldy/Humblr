package ru.skillbox.humblr.data.repositories

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import ru.skillbox.humblr.R
import ru.skillbox.humblr.data.repositories.modules.TokenHolder

class SessionManager(context: Context) {
    private var prefs: SharedPreferences =
        context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE)

    fun saveAuthToken(tokenHolder: TokenHolder) {
        synchronized(this) {
            val editor = prefs.edit()
            tokenHolder.map.forEach { pair -> editor.putString(pair.key, pair.value) }
            editor.commit()
        }
    }

    fun fetchAuthToken(): TokenHolder {
        synchronized(this) {
            val map = TokenHolder.KEYS.values().map { key ->
                val value = prefs.getString(key.toString(), null)
                Log.d("pair", "$key $value")
                key.toString() to value
            }.toMap()
            return TokenHolder(map)
        }
    }

    fun removeToken() {
        val editor = prefs.edit()
        TokenHolder.KEYS.values().forEach { key ->
            editor.putString(key.toString(), null)
        }
        editor.apply()
    }

    fun isFirstTime(): Boolean {
        return prefs.getBoolean(FIRST_TIME, true)
    }

    fun setFirst() {
        val editor = prefs.edit()
        editor.putBoolean(FIRST_TIME, false)
        editor.apply()
    }

    companion object {
        const val FIRST_TIME = "first_time"
    }
}