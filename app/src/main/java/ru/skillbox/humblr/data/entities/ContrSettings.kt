package ru.skillbox.humblr.data.entities

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ContrSettings(@Json(name = "allowed_media_types") val allowedMediaTypes: List<Contr>) {

}

enum class Contr { giphy, unknown, animated, static }