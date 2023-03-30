package ru.skillbox.humblr.data.interfaces

interface PictListener {
    fun onLeft(isLast:Boolean)
    fun onRight(isLast:Boolean)
    fun none()
}