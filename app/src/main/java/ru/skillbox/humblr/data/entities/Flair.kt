package ru.skillbox.humblr.data.entities

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Flair(val e:String,val t:String) {
}