package ru.skillbox.humblr.utils.richLink

class MetaData {
    var url = ""
    var imageurl = ""
    var title = ""
    var description = ""
    var sitename = ""
    var mediatype = ""
    var favicon = ""
    var originalUrl = ""
    override fun toString(): String {
        return "$url $imageurl,$title"
    }
}