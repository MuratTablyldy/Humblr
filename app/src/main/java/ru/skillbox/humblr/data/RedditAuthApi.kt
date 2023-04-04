package ru.skillbox.humblr.data

import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Headers
import retrofit2.http.POST

interface RedditAuthApi {
    @Headers("content-type: application/json")
    @POST(value = "/api/v1/access_token")
    @FormUrlEncoded
    fun postAuthCode(
        @Field("grant_type") grantType: String,
        @Field("code") code: String,
        @Field("redirect_uri") redirectUri: String
    )
}