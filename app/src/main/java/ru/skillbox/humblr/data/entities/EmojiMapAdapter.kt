package ru.skillbox.humblr.data.entities

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import java.lang.NullPointerException
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.text.buildString

public class EmojiMapAdapter(
    moshi: Moshi
) : JsonAdapter<HashMap<String, Emoji?>>() {

    val map = HashMap<String, Emoji?>()
    private val nullableEmojiAdapter: JsonAdapter<Emoji> = moshi.adapter(
        Emoji::class.java,
        emptySet(), "map"
    )

    public override fun toString(): String = buildString(38) {
        append("GeneratedJsonAdapter(").append("EmojisCollection").append(')')
    }

    public override fun fromJson(reader: JsonReader): HashMap<String, Emoji?> {
        reader.beginObject()

        while (reader.hasNext()) {
            val name = reader.nextName()
            val emoji = nullableEmojiAdapter.fromJson(reader)
            map[name] = emoji
        }
        reader.endObject()
        return map
    }

    override fun toJson(writer: JsonWriter, value: HashMap<String, Emoji?>?) {
    }


}