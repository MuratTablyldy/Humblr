package ru.skillbox.humblr.data.repositories

import android.content.Context
import android.content.SharedPreferences
import ru.skillbox.humblr.R
import ru.skillbox.humblr.data.repositories.modules.TokenHolder

class FetchSessionManager(context: Context) {
    private var prefs: SharedPreferences =
        context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE)
    fun fetchAuthToken(): TokenHolder {
        synchronized(this){
            val map= TokenHolder.KEYS.values().map { key->
                val value =prefs.getString(key.toString(),null)
                key.toString() to value
            }.toMap()
            return TokenHolder(map)
        }
    }
    fun removeToken(){
        synchronized(this){
            val editor=prefs.edit()
            TokenHolder.KEYS.values().forEach { key->
                editor.putString(key.toString(),null)
            }
            editor.apply()
        }

    }
}