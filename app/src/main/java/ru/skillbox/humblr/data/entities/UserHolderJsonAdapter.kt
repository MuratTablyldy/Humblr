// Code generated by moshi-kotlin-codegen. Do not edit.
@file:Suppress(
    "DEPRECATION", "unused", "ClassName", "REDUNDANT_PROJECTION",
    "RedundantExplicitType", "LocalVariableName", "RedundantVisibilityModifier",
    "PLATFORM_CLASS_MAPPED_TO_KOTLIN", "IMPLICIT_NOTHING_TYPE_ARGUMENT_IN_RETURN_POSITION"
)

package ru.skillbox.humblr.data.entities

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.`internal`.Util
import java.lang.NullPointerException

import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.HashMap
import kotlin.collections.emptySet
import kotlin.text.buildString

public class UserHolderJsonAdapter(
    moshi: Moshi,
) : JsonAdapter<UserHolder>() {
    private val options: JsonReader.Options = JsonReader.Options.of("users")

    private val userInfoAdapter: JsonAdapter<UserInfo> = moshi.adapter(
        UserInfo::class.java,
        emptySet(), "data"
    )

    public override fun toString(): String = buildString(32) {
        append("GeneratedJsonAdapter(").append("UserHolder").append(')')
    }

    public override fun fromJson(reader: JsonReader): UserHolder {
        var users: HashMap<String, UserInfo>? = HashMap()
        reader.beginObject()
        while (reader.hasNext()) {
            val name = reader.nextName()
            val json = reader.readJsonValue()
          val info=userInfoAdapter.fromJsonValue(json)

          if(info!=null){
            info.id=name
            users?.put(name,info)
          }
        }
        reader.endObject()
        return UserHolder(
            users = users ?: throw Util.missingProperty("users", "users", reader)
        )
    }

    public override fun toJson(writer: JsonWriter, value_: UserHolder?): Unit {
        if (value_ == null) {
            throw NullPointerException("value_ was null! Wrap in .nullSafe() to write nullable values.")
        }
        writer.beginObject()
        writer.name("users")
        //userInfoAdapter.toJson(writer, value_.users)
        writer.endObject()
    }
}