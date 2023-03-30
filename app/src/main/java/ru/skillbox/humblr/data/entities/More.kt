package ru.skillbox.humblr.data.entities

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import ru.skillbox.humblr.data.interfaces.Created

@JsonClass(generateAdapter = true)
data class More(
    val name: String?,
    val id: String?,
    val count: Int?,
    @Json(name="parent_id")
    val parentId: String,
    val depth:Int?,
    val children: List<String>?
):Created {
     override val created: Long?=null
     override val createdUTC: Long?=null
    override fun getParent(): String? {
        return parentId
    }

    override fun getDepth2(): Int? {
        return depth
    }

    override fun getIds(): String? {
        return id
    }
    var parent:Comment?=null
}