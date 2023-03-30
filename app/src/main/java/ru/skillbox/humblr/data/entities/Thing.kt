package ru.skillbox.humblr.data.entities

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Thing<T>(
    @Json(name = "loid")
    val id: String?,
    @Json(name = "loid_created")
    val created: Long?,
    val kind: String,
    val data: T) {
}