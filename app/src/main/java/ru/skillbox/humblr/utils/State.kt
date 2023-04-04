package ru.skillbox.humblr.utils

import kotlinx.coroutines.flow.MutableStateFlow

object State {
    private var state: StateF? = null
    fun getInstance(): StateF {
        if (state == null) {
            state = StateF()
        }
        return state!!
    }

    class StateF {
        val expired = MutableStateFlow(false)
        var error = MutableStateFlow<Throwable?>(null)
    }
}