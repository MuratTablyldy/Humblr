package ru.skillbox.humblr.data.entities

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Emoji(
    val url: String,
    @Json(name = "user_flair_allowed")
    val userFlairAllowed: Boolean,
    @Json(name = "post_flair_allowed")
    val postFlairAllowed: Boolean,
    @Json(name = "mod_flair_only")
    val modFlairOnly: Boolean,
    @Json(name = "created_by")
    val createdBy: String
)