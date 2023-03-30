package ru.skillbox.humblr.data.entities

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class YouTubeVideo(val html:String, @Json(name="provider_url") val providerUrl:String,val url:String?) {
}