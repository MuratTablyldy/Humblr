package ru.skillbox.humblr.data.entities

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FromMore(
    @Json(name = "json")
    val data:CommentsFromMore
    ) {

    @JsonClass(generateAdapter = true)
    data class CommentsFromMore(val errors:List<String>,val data:MoreThings)

    @JsonClass(generateAdapter = true )
    data class MoreThings(val things:List<Thing2>)
}