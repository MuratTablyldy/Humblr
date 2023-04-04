package ru.skillbox.humblr.data.entities

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Comm(val jquery: List<Subb>, val success: Boolean) {
}