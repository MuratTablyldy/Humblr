
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
import java.lang.NullPointerException
import kotlin.String
import kotlin.Suppress
import kotlin.collections.emptySet
import kotlin.text.buildString

public class SubbJsonAdapter(
    moshi: Moshi,
) : JsonAdapter<Subb>() {

    private val thingOfCommentAdapter: JsonAdapter<Thing<Comment>> =
        moshi.adapter(
            Types.newParameterizedType(Thing::class.java, Comment::class.java), emptySet(),
            "comment"
        )

    public override fun toString(): String = buildString(26) {
        append("GeneratedJsonAdapter(").append("Subb").append(')')
    }

    //{"jquery": [[0, 1, "call", ["body"]], [1, 2, "attr", "find"], [2, 3, "call", [".status"]], [3, 4, "attr", "hide"], [4, 5, "call", []], [5, 6, "attr", "html"], [6, 7, "call", [""]], [7, 8, "attr", "end"], [8, 9, "call", []], [1, 10, "attr", "find"], [10, 11, "call", ["textarea"]], [11, 12, "attr", "attr"], [12, 13, "call", ["rows", 3]], [13, 14, "attr", "html"], [14, 15, "call", [""]], [15, 16, "attr", "val"], [16, 17, "call", [""]], [0, 18, "attr", "insert_things"], [18, 19, "call", [[{"kind": "t1", "data": {"subreddit_id": "t5_2xxyj", "approved_at_utc": null, "author_is_blocked": false, "comment_type": null, "edited": false, "mod_reason_by": null, "banned_by": null, "ups": 1, "num_reports": null, "author_flair_type": "text", "total_awards_received": 0, "subreddit": "Damnthatsinteresting", "author_flair_template_id": null, "likes": true, "replies": "", "user_reports": [], "saved": false, "id": "j8mma1i", "banned_at_utc": null, "mod_reason_title": null, "gilded": 0, "archived": false, "collapsed_reason_code": null, "no_follow": false, "author": "Ok_Pressure3227", "can_mod_post": false, "created_utc": 1676465323.0, "send_replies": true, "parent_id": "t3_10uo367", "score": 1, "author_fullname": "t2_ashqg0oh", "report_reasons": null, "approved_by": null, "all_awardings": [], "collapsed": false, "body": "h", "awarders": [], "top_awarded_type": null, "author_flair_css_class": null, "author_patreon_flair": false, "downs": 0, "author_flair_richtext": [], "is_submitter": false, "body_html": "&lt;div class=\"md\"&gt;&lt;p&gt;h&lt;/p&gt;\n&lt;/div&gt;", "removal_reason": null, "collapsed_reason": null, "associated_award": null, "stickied": false, "author_premium": false, "can_gild": false, "gildings": {}, "unrepliable_reason": null, "author_flair_text_color": null, "score_hidden": false, "permalink": "/r/Damnthatsinteresting/comments/10uo367/battery_restoration_in_africa/j8mma1i/", "subreddit_type": "public", "locked": false, "name": "t1_j8mma1i", "created": 1676465323.0, "author_flair_text": null, "treatment_tags": [], "rte_mode": "markdown", "link_id": "t3_10uo367", "subreddit_name_prefixed": "r/Damnthatsinteresting", "controversiality": 0, "author_flair_background_color": null, "collapsed_because_crowd_control": null, "mod_reports": [], "mod_note": null, "distinguished": null}}], false]], [0, 20, "call", ["#noresults"]], [20, 21, "attr", "hide"], [21, 22, "call", []]], "success": true}
    public override fun fromJson(reader: JsonReader): Subb {
        var comment: Thing<Comment>? = null
        while (reader.hasNext()) {
            val value = reader.readJsonValue()
            if (value is ArrayList<*> && value.isNotEmpty()) {
                for (sub in value) {
                    if (sub is ArrayList<*> && sub.isNotEmpty()) {
                        for (para in sub) {
                            if (para is ArrayList<*> && para.isNotEmpty()) {
                                val data = para.first()
                                comment = thingOfCommentAdapter.fromJsonValue(data)
                            }
                        }
                    }
                }

            }
        }
        return Subb(
            comment = comment
        )
    }

    public override fun toJson(writer: JsonWriter, value_: Subb?) {
        if (value_ == null) {
            throw NullPointerException("value_ was null! Wrap in .nullSafe() to write nullable values.")
        }
        writer.beginObject()
        writer.name("comment")
        thingOfCommentAdapter.toJson(writer, value_.comment)
        writer.endObject()
    }
}
