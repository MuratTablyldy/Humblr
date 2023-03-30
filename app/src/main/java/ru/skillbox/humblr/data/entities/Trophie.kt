package ru.skillbox.humblr.data.entities

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Trophie(
    val icon_70:String,
    val granted_at:Long,
    val url:String?,
    val icon_40:String,
    val name:String,
    val award_id:String?,
    val id:String?,
    val descrition:String?
) {
}