package ru.skillbox.humblr.data.entities

import retrofit2.http.Header
import retrofit2.http.Query

data class SubredditCreator(
    var acceptFollowers: Boolean?,
    var overrideSpam: Boolean?,
    var overrideSpamLinks: Boolean?,
    var overrideSpamSelfPosts: Boolean?,
    var allOriginalContent: Boolean?,
    var allowChatPostCreation: Boolean?,
    var allowDiscovery: Boolean?,
    var allowGalleries: Boolean?,
    var allowImages: Boolean?,
    var allowPolls: Boolean?,
    var allowPostCrossposts: Boolean?,
    var allowPredictionContributors: Boolean?,
    var allowPredictions: Boolean?,
    var allowPredictionsTournament: Boolean?,
    var allowTalks: Boolean?,
    var allowTop: Boolean?,
    var allowVideos: Boolean?,
    //json
    var api_type: String?,
    //an integer between 0 and 3
    var banEvasionThreshold: Int?,
    var collapseDeletedComments: Boolean?,
    var comment_contribution_settings: ContrSettings?,
    var commentScoreHideMins: Int?,
    var crowdControlChatLevel: Int?,
    var crowdControlFilter: Boolean?,
    //an integer between 0 and 3
    var crowdControlLevel: Int?,
    var crowdControlMode: Int?,
    //an integer between 0 and 3
    var crowdControlPostLevel: Int?,
    //raw markdown text
    var description: String?,
    var disableContributorRequests: Boolean?,
    var excludeBannedModqueue: Boolean?,
    var freeFormReports: Boolean?,
    var gRecaptchaResponse: String?,
    //an integer between 0 and 3
    var hatefulContentThresholdAbuse: Int?,
    var hatefulContentThresholdIdentity: Int?,
    var headerTitle: String,
    var hideAds: Boolean?,
    //a 6-digit rgb hex color, e.g. #AABBCC
    var keyColor: String,
    //one of (any, link, self)
    var linkType: String,
    var modmailHarassmentFilterEnabled: Boolean?,
    //subreddit name
    var name: String?,
    var new_pinned_post_pns_enabled: Boolean?,
    var original_content_tag_enabled: Boolean?,
    var over_18: Boolean?,
    var sr: String?,
    var restrict_commenting: Boolean?,
    var restrict_posting: Boolean?,
    var title: String?,
    var submit_text: String?,
    var prediction_leaderboard_entry_type: Int?,
    var public_description: String?,
    var user_flair_pns_enabled: Boolean?,
    var welcome_message_enabled: Boolean?,
    var welcome_message_text: String?,
    var should_archive_posts: Boolean?,
    var show_media: Boolean?,
    var show_media_preview: Boolean?,
    var spoilers_enabled: Boolean?,
    //one of (low, high, all)
    var spam_comments: String?,
    var spam_links: String?,
    var spam_selfposts: String?,
    var submit_link_label: String?,
    var submit_text_label: String?,
    //one of (confidence, top, new, controversial, old, random, qa, live)
    var suggested_comment_sort: String?,
    //an integer between 0 and 1
    var toxicity_threshold_chat_level: Int?,
    //one of (gold_restricted, archived, restricted, private, employees_only, gold_only, public, user)
    var type: String?,
    //an integer between 0 and 36600 (default: 0)
    var wiki_edit_age: Int?,
    //an integer between 0 and 1000000000 (default: 0)
    var wiki_edit_karma: Int?,
    //one of (disabled, modonly, anyone)
    var wikimode: String?
)