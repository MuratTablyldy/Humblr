package ru.skillbox.humblr.`data`.entities

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import java.lang.NullPointerException
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.text.buildString

public class EmojisCollectionJsonAdapter(
  moshi: Moshi
) : JsonAdapter<EmojisCollection>() {

  private val map = HashMap<String, HashMap<String, Emoji?>>()
  private val nullableEmojiAdapter = EmojiMapAdapter(moshi)

  public override fun toString(): String = buildString(38) {
    append("GeneratedJsonAdapter(").append("EmojisCollection").append(')')
  }

  public override fun fromJson(reader: JsonReader): EmojisCollection {
    reader.beginObject()

    while (reader.hasNext()) {
      var i = 0
      val name = reader.nextName()
      val emoji = nullableEmojiAdapter.fromJson(reader)
      map[name] = emoji
    }
    reader.endObject()
    return EmojisCollection(
      map
    )
  }

  public override fun toJson(writer: JsonWriter, value_: EmojisCollection?): Unit {
    if (value_ == null) {
      throw NullPointerException("value_ was null! Wrap in .nullSafe() to write nullable values.")
    }
    writer.beginObject()
    writer.endObject()
  }
}
