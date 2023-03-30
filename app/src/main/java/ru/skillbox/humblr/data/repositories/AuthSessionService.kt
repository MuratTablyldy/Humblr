package ru.skillbox.humblr.data.repositories

import android.util.Log
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import javax.inject.Inject

class AuthSessionService @Inject constructor() {
    val mediatype = "application/json; charset=utf-8".toMediaType()
    val client: OkHttpClient = OkHttpClient()
    private val TOKEN = "token"
    private val TOKEN_TYPE = "token_type_hint"

     fun revokeToken(url: String, token: String, tokenType: String):Result<String> {
        val body: RequestBody = createRequestBody(TOKEN to token, TOKEN_TYPE to tokenType)
        val request =
            Request.Builder()
                .url(url)
                .post(body)
                .build()
        client.newCall(request).execute().use { response ->
           return if(response.isSuccessful){
                Result.success("OK")
            } else{
                Result.failure(Exception( response.message))
            }
            }
        }
    private fun createRequestBody(vararg params: Pair<String, String>) =
        JSONObject(mapOf(*params)).toString().toRequestBody(mediatype)
    }


