package ru.skillbox.humblr.data.entities

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Gildings(
    @Json(name = "gid_1")
    val silver: Int?,
    @Json(name = "gid_2")
    val gold: Int?,
    @Json(name = "gid_3")
    val platinum: Int?
)