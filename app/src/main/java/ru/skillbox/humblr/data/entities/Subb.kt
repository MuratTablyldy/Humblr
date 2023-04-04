package ru.skillbox.humblr.data.entities

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true, generator = "ru.skillbox.humblr.data.entities.SubbJsonAdapter")
data class Subb(val comment: Thing<Comment>?)