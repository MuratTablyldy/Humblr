package ru.skillbox.humblr.utils.richLink

import android.webkit.URLUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException
import java.net.URI
import java.net.URISyntaxException

class LinkHandler {
    companion object {
        suspend fun getLink(url: String): ru.skillbox.humblr.data.Result<MetaData> {
            return withContext(Dispatchers.IO) {
                val metaData = MetaData()
                var doc: Document? = null
                try {
                    val connection = Jsoup.connect(url)
                        .timeout(30 * 1000)

                    doc = connection.get()
                    val elements = doc.getElementsByTag("meta")

                    // getTitle doc.select("meta[property=og:title]")
                    var title = doc.select("meta[property=og:title]").attr("content")
                    if (title.isNullOrBlank()) {
                        title = doc.title()
                    }
                    metaData.title = title!!

                    //getDescription
                    var description = doc.select("meta[name=description]").attr("content")
                    if (description.isNullOrBlank()) {
                        description = doc.select("meta[name=Description]").attr("content")
                    }
                    if (description.isNullOrBlank()) {
                        description = doc.select("meta[property=og:description]").attr("content")
                    }
                    if (description.isNullOrBlank()) {
                        description = ""
                    }
                    metaData.description = description


                    // getMediaType
                    val mediaTypes = doc.select("meta[name=medium]")
                    var type: String? = ""
                    type = if (mediaTypes.size > 0) {
                        val media = mediaTypes.attr("content")
                        if (media == "image") "photo" else media
                    } else {
                        doc.select("meta[property=og:type]").attr("content")
                    }
                    metaData.mediatype = type!!


                    //getImages
                    val imageElements = doc.select("meta[property=og:image]")
                    if (imageElements.size > 0) {
                        val image = imageElements.attr("content")
                        if (!image.isEmpty()) {
                            metaData.imageurl = resolveURL(url, image)
                        }
                    }

                    //get image from meta[name=og:image]

                    if (metaData.imageurl.isEmpty()) {
                        val imageElements = doc.select("meta[name=og:image]")
                        if (imageElements.size > 0) {
                            val image = imageElements.attr("content")
                            if (!image.isEmpty()) {
                                metaData.imageurl = resolveURL(url, image)
                            }
                        }
                    }
                    if (metaData.imageurl.isEmpty()) {
                        var src = doc.select("link[rel=image_src]").attr("href")
                        if (!src.isNullOrBlank()) {
                            metaData.imageurl = resolveURL(url, src)
                        } else {
                            src = doc.select("link[rel=apple-touch-icon]").attr("href")
                            if (!src.isNullOrBlank()) {
                                metaData.imageurl = resolveURL(url, src)
                                metaData.favicon = resolveURL(url, src)
                            } else {
                                src = doc.select("link[rel=icon]").attr("href")
                                if (!src.isNullOrBlank()) {
                                    metaData.imageurl = resolveURL(url, src)
                                    metaData.favicon = resolveURL(url, src)
                                }
                            }
                        }
                    }

                    //Favicon
                    var src = doc.select("link[rel=apple-touch-icon]").attr("href")
                    if (!src.isNullOrBlank()) {
                        metaData.favicon = resolveURL(url, src)
                    } else {
                        src = doc.select("link[rel=icon]").attr("href")
                        if (!src.isNullOrBlank()) {
                            metaData.favicon = resolveURL(url, src)
                        }
                    }
                    for (element in elements) {
                        if (element.hasAttr("property")) {
                            val strProperty = element.attr("property").trim { it <= ' ' }
                            if (strProperty == "og:url") {
                                metaData.url = element.attr("content")
                            }
                            if (strProperty == "og:site_name") {
                                metaData.sitename = element.attr("content")
                            }
                        }
                    }
                    if (metaData.url == "" || metaData.url.isEmpty()) {
                        var uri: URI? = null
                        try {
                            uri = URI(url)
                        } catch (e: URISyntaxException) {
                            e.printStackTrace()
                        }
                        if (url == null || uri == null) {
                            metaData.url = url
                        } else {
                            metaData.url = uri.host
                        }
                    }
                    ru.skillbox.humblr.data.Result.Success(metaData)
                } catch (e: IOException) {
                    e.printStackTrace()
                    ru.skillbox.humblr.data.Result.Error(
                        Exception(
                            "No Html Received from " + url + " Check your Internet " + e.localizedMessage
                        )
                    )
                }
            }
        }

        private fun resolveURL(url: String?, part: String): String {
            return if (URLUtil.isValidUrl(part)) {
                part
            } else {
                var base_uri: URI? = null
                try {
                    base_uri = URI(url)
                    base_uri = base_uri.resolve(part)
                    return base_uri.toString()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                ""
            }
        }
    }
}