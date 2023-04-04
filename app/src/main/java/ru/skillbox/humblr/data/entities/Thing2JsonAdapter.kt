package ru.skillbox.humblr.data.entities

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.`internal`.Util
import java.lang.NullPointerException
import kotlin.Any
import kotlin.Long
import kotlin.String
import kotlin.Unit
import kotlin.collections.emptySet
import kotlin.text.buildString

class Thing2JsonAdapter(
    moshi: Moshi
) : JsonAdapter<Thing2>() {
    private val options: JsonReader.Options = JsonReader.Options.of(
        "loid", "loid_created", "kind",
        "data"
    )

    private val nullableStringAdapter: JsonAdapter<String?> = moshi.adapter(
        String::class.java,
        emptySet(), "id"
    )

    private val nullableLongAdapter: JsonAdapter<Long?> = moshi.adapter(
        Long::class.javaObjectType,
        emptySet(), "created"
    )

    private val stringAdapter: JsonAdapter<String> = moshi.adapter(
        String::class.java, emptySet(),
        "kind"
    )

    private val commentAdapter: JsonAdapter<Comment> =
        moshi.adapter(Comment::class.java, emptySet(), "data")

    private val linkAdapter: JsonAdapter<Link> = moshi.adapter(
        Link::class.java,
        emptySet(), "data"
    )
    private val moreAdapter: JsonAdapter<More> = moshi.adapter(More::class.java, emptySet(), "data")

    public override fun toString(): String = buildString(28) {
        append("GeneratedJsonAdapter(").append("Thing2").append(')')
    }

    public override fun fromJson(reader: JsonReader): Thing2 {
        var id: String? = null
        var created: Long? = null
        var kind: String? = null
        var data_: Any? = null
        reader.beginObject()
        while (reader.hasNext()) {
            when (reader.selectName(options)) {
                0 -> id = nullableStringAdapter.fromJson(reader)
                1 -> created = nullableLongAdapter.fromJson(reader)
                2 -> kind = stringAdapter.fromJson(reader) ?: throw Util.unexpectedNull(
                    "kind", "kind",
                    reader
                )
                3 -> {
                    when (kind) {
                        "t3" -> {
                            data_ = linkAdapter.fromJson(reader)
                        }
                        "t1" -> {
                            data_ = commentAdapter.fromJson(reader)
                        }
                        "more" -> {
                            data_ = moreAdapter.fromJson(reader)
                        }
                    }
                }
                -1 -> {
                    // Unknown name, skip it.
                    reader.skipName()
                    reader.skipValue()
                }
            }
        }
        reader.endObject()
        return Thing2(
            id = id,
            created = created,
            kind = kind ?: throw Util.missingProperty("kind", "kind", reader),
            `data` = data_ ?: throw Util.missingProperty("data_", "data", reader)
        )
    }

    public override fun toJson(writer: JsonWriter, value_: Thing2?): Unit {
        if (value_ == null) {
            throw NullPointerException("value_ was null! Wrap in .nullSafe() to write nullable values.")
        }
        writer.beginObject()
        writer.name("loid")
        nullableStringAdapter.toJson(writer, value_.id)
        writer.name("loid_created")
        nullableLongAdapter.toJson(writer, value_.created)
        writer.name("kind")
        stringAdapter.toJson(writer, value_.kind)
        writer.name("data")
        writer.endObject()
    }
}