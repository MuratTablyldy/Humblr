package ru.skillbox.humblr.data.interfaces

import com.squareup.moshi.Json

interface Created {
    val created:Long?
    @Json(name = "created_utc")
    val createdUTC:Long?
    fun getParent():String?
    fun getDepth2():Int?
    fun getIds():String?
}