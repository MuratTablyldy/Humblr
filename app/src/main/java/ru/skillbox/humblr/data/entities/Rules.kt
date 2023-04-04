package ru.skillbox.humblr.data.entities

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Rules(
    val rules: List<Rule>,
    @Json(name = "site_rules")
    val siteRules: List<String>
)