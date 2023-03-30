package ru.skillbox.humblr.data.entities

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Rule(
    val kind: String,
    val description: String,
    @Json(name = "short_name")
    val shortName: String,
    @Json(name = "violation_reason")
    val violationReason: String,
    @Json(name = "created_utc")
    val createdUtc: String,
    val priority: Int
)