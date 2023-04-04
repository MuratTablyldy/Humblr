package ru.skillbox.humblr.data.entities

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserInfo(val name: String, @Json(name = "profile_img") val profileImg: String) {
    var id: String? = null
}