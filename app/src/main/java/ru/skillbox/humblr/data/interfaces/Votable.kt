package ru.skillbox.humblr.data.interfaces

interface Votable {
    val ups: Int?
    val downs: Int?
    val likes: Boolean?
}