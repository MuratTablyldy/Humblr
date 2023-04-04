package ru.skillbox.humblr.data.entities

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import ru.skillbox.humblr.data.interfaces.Votable

@JsonClass(generateAdapter = true, generator = "ru.skillbox.humblr.data.entities.Thing2JsonAdapter")
data class Thing2(
    val id: String?,
    @Json(name = "loid_created")
    val created: Long?,
    val kind: String,
    val data: Any
)