package ru.skillbox.humblr.data.repositories

data class
AuthProperties(
    val clientId: String,
    val scope: String,
    val redirectUri: String,
    val baseUri: String,
    val duration: String,
    val responseType: String,
    val uuid: String,
    val authUri: String,
    val tokenEndPoint: String,
    var state: String?,
    val revokePath: String
)