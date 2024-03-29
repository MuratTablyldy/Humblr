@file:Suppress(
    "DEPRECATION", "unused", "ClassName", "REDUNDANT_PROJECTION",
    "RedundantExplicitType", "LocalVariableName", "RedundantVisibilityModifier",
    "PLATFORM_CLASS_MAPPED_TO_KOTLIN", "IMPLICIT_NOTHING_TYPE_ARGUMENT_IN_RETURN_POSITION"
)

package ru.skillbox.humblr.data.entities


import androidx.core.text.HtmlCompat
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.lang.NullPointerException
import java.net.URLDecoder
import kotlin.Boolean
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.collections.emptySet
import kotlin.text.buildString

public class CommentJsonAdapter(
    moshi: Moshi
) : JsonAdapter<Comment>() {
    private val options: JsonReader.Options = JsonReader.Options.of(
        "id", "created", "created_utc",
        "ups", "downs", "likes", "approved_by", "author", "banned_by", "body_html", "gilded",
        "link_author", "link_id", "link_title", "edited", "link_url", "num_reports", "parent_id",
        "replies", "saved", "score", "score_hidden", "subreddit", "subreddit_id", "distinguished",
        "archived", "depth", "name", "author_fullname"
    )
    val regex = """(<div class="md">)|(</div>)""".toRegex()

    private val nullableStringAdapter: JsonAdapter<String?> = moshi.adapter(
        String::class.java,
        emptySet(), "id"
    )

    private val nullableLongAdapter: JsonAdapter<Long?> = moshi.adapter(
        Long::class.javaObjectType,
        emptySet(), "created"
    )

    private val nullableIntAdapter: JsonAdapter<Int?> = moshi.adapter(
        Int::class.javaObjectType,
        emptySet(), "ups"
    )

    private val nullableBooleanAdapter: JsonAdapter<Boolean?> =
        moshi.adapter(Boolean::class.javaObjectType, emptySet(), "likes")

    private val nullableThingOfListingOfThing2Adapter: JsonAdapter<Thing<Listing<Thing2>>?> =
        moshi.adapter(
            Types.newParameterizedType(
                Thing::class.java,
                Types.newParameterizedType(Listing::class.java, Thing2::class.java)
            ), emptySet(), "replies"
        )

    public override fun toString(): String = buildString(29) {
        append("GeneratedJsonAdapter(").append("Comment").append(')')
    }

    public override fun fromJson(reader: JsonReader): Comment {
        var id: String? = null
        var created: Long? = null
        var createdUTC: Long? = null
        var ups: Int? = null
        var downs: Int? = null
        var likes: Boolean? = null
        var approvedBy: String? = null
        var author: String? = null
        var bannedBy: String? = null
        var body: String? = null
        var gilded: Int? = null
        var linkAuthor: String? = null
        var linkId: String? = null
        var linkTitle: String? = null
        var edited: Long? = null
        var linkUrl: String? = null
        var numReports: Int? = null
        var parentId: String? = null
        var replies: Thing<Listing<Thing2>>? = null
        var saved: Boolean? = null
        var score: Int? = null
        var scoreHidden: Boolean? = null
        var subreddit: String? = null
        var subredditID: String? = null
        var distinguished: String? = null
        var archived: Boolean? = null
        var depth: Int? = null
        var name: String? = null
        var autorName: String? = null
        reader.beginObject()
        while (reader.hasNext()) {
            when (reader.selectName(options)) {
                0 -> id = nullableStringAdapter.fromJson(reader)
                1 -> created = nullableLongAdapter.fromJson(reader)
                2 -> createdUTC = nullableLongAdapter.fromJson(reader)
                3 -> ups = nullableIntAdapter.fromJson(reader)
                4 -> downs = nullableIntAdapter.fromJson(reader)
                5 -> likes = nullableBooleanAdapter.fromJson(reader)
                6 -> approvedBy = nullableStringAdapter.fromJson(reader)
                7 -> author = nullableStringAdapter.fromJson(reader)
                8 -> bannedBy = nullableStringAdapter.fromJson(reader)
                9 -> body = nullableStringAdapter.fromJson(reader)
                10 -> gilded = nullableIntAdapter.fromJson(reader)
                11 -> linkAuthor = nullableStringAdapter.fromJson(reader)
                12 -> linkId = nullableStringAdapter.fromJson(reader)
                13 -> linkTitle = nullableStringAdapter.fromJson(reader)
                14 -> {
                    val obj = reader.readJsonValue()
                    edited = if (obj is Double) {
                        obj.toLong()
                    } else {
                        null
                    }
                }
                15 -> linkUrl = nullableStringAdapter.fromJson(reader)
                16 -> numReports = nullableIntAdapter.fromJson(reader)
                17 -> parentId = nullableStringAdapter.fromJson(reader)
                18 -> {

                    val values = reader.readJsonValue()
                    replies = if (values == "") {
                        Thing(null, null, "t1", Listing(null, null, null, mutableListOf()))
                        //    reader.skipValue()
                    } else {
                        nullableThingOfListingOfThing2Adapter.fromJsonValue(values)
                    }
                }
                19 -> saved = nullableBooleanAdapter.fromJson(reader)
                20 -> score = nullableIntAdapter.fromJson(reader)
                21 -> scoreHidden = nullableBooleanAdapter.fromJson(reader)
                22 -> subreddit = nullableStringAdapter.fromJson(reader)
                23 -> subredditID = nullableStringAdapter.fromJson(reader)
                24 -> distinguished = nullableStringAdapter.fromJson(reader)
                25 -> archived = nullableBooleanAdapter.fromJson(reader)
                26 -> depth = nullableIntAdapter.fromJson(reader)
                27 -> name = nullableStringAdapter.fromJson(reader)
                28 -> autorName = nullableStringAdapter.fromJson(reader)
                -1 -> {
                    // Unknown name, skip it.
                    reader.skipName()
                    reader.skipValue()
                }
            }
        }
        reader.endObject()
        if (body != null) {
            val decoded = replacer(body)
            val html = HtmlCompat.fromHtml(decoded, HtmlCompat.FROM_HTML_MODE_LEGACY)
            body = html.replace(regex, "")

        }
        return Comment(
            id = id,
            created = created,
            createdUTC = createdUTC,
            ups = ups,
            downs = downs,
            likes = likes,
            approvedBy = approvedBy,
            author = author,
            bannedBy = bannedBy,
            body = body,
            gilded = gilded,
            linkAuthor = linkAuthor,
            linkId = linkId,
            linkTitle = linkTitle,
            edited = edited,
            linkUrl = linkUrl,
            numReports = numReports,
            parentId = parentId,
            replies = replies!!,
            saved = saved,
            score = score,
            scoreHidden = scoreHidden,
            subreddit = subreddit,
            subredditID = subredditID,
            distinguished = distinguished,
            archived = archived,
            depth = depth,
            account = null,
            name = name,
            autorName = autorName
        )
    }

    public override fun toJson(writer: JsonWriter, value_: Comment?) {
        if (value_ == null) {
            throw NullPointerException("value_ was null! Wrap in .nullSafe() to write nullable values.")
        }
        writer.beginObject()
        writer.name("id")
        nullableStringAdapter.toJson(writer, value_.id)
        writer.name("created")
        nullableLongAdapter.toJson(writer, value_.created)
        writer.name("created_utc")
        nullableLongAdapter.toJson(writer, value_.createdUTC)
        writer.name("ups")
        nullableIntAdapter.toJson(writer, value_.ups)
        writer.name("downs")
        nullableIntAdapter.toJson(writer, value_.downs)
        writer.name("likes")
        nullableBooleanAdapter.toJson(writer, value_.likes)
        writer.name("approved_by")
        nullableStringAdapter.toJson(writer, value_.approvedBy)
        writer.name("author")
        nullableStringAdapter.toJson(writer, value_.author)
        writer.name("banned_by")
        nullableStringAdapter.toJson(writer, value_.bannedBy)
        writer.name("body")
        nullableStringAdapter.toJson(writer, value_.body)
        writer.name("gilded")
        nullableIntAdapter.toJson(writer, value_.gilded)
        writer.name("link_author")
        nullableStringAdapter.toJson(writer, value_.linkAuthor)
        writer.name("link_id")
        nullableStringAdapter.toJson(writer, value_.linkId)
        writer.name("link_title")
        nullableStringAdapter.toJson(writer, value_.linkTitle)
        writer.name("edited")
        nullableLongAdapter.toJson(writer, value_.edited)
        writer.name("link_url")
        nullableStringAdapter.toJson(writer, value_.linkUrl)
        writer.name("num_reports")
        nullableIntAdapter.toJson(writer, value_.numReports)
        writer.name("parent_id")
        nullableStringAdapter.toJson(writer, value_.parentId)
        writer.name("replies")
        nullableThingOfListingOfThing2Adapter.toJson(writer, value_.replies)
        writer.name("saved")
        nullableBooleanAdapter.toJson(writer, value_.saved)
        writer.name("score")
        nullableIntAdapter.toJson(writer, value_.score)
        writer.name("score_hidden")
        nullableBooleanAdapter.toJson(writer, value_.scoreHidden)
        writer.name("subreddit")
        nullableStringAdapter.toJson(writer, value_.subreddit)
        writer.name("subreddit_id")
        nullableStringAdapter.toJson(writer, value_.subredditID)
        writer.name("distinguished")
        nullableStringAdapter.toJson(writer, value_.distinguished)
        writer.name("archived")
        nullableBooleanAdapter.toJson(writer, value_.archived)
        writer.endObject()
    }

    fun replacer(text: String): String {
        var data = text
        try {
            val tempBuffer = StringBuffer()
            var incrementor = 0
            val dataLength = data.length
            while (incrementor < dataLength) {
                val charecterAt = data[incrementor]
                if (charecterAt == '%') {
                    tempBuffer.append("<percentage>")
                } else if (charecterAt == '+') {
                    tempBuffer.append("<plus>")
                } else {
                    tempBuffer.append(charecterAt)
                }
                incrementor++
            }
            data = tempBuffer.toString()
            data = URLDecoder.decode(data, "utf-8")
            data = data.replace("<percentage>".toRegex(), "%")
            data = data.replace("<plus>".toRegex(), "+")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return data
    }
}
