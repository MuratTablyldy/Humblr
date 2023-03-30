package ru.skillbox.humblr.utils

import android.content.Context
import com.google.android.exoplayer2.DefaultLoadControl.*
import kohii.v1.exoplayer.ExoPlayerConfig
import kohii.v1.exoplayer.Kohii
import kohii.v1.exoplayer.createKohii
import kohii.v1.utils.Capsule

object KohiiProvider {

    private val capsule = Capsule<Kohii, Context>(creator = { context ->
        createKohii(context, ExoPlayerConfig(
            minBufferMs = DEFAULT_MIN_BUFFER_MS / 10,
            maxBufferMs = DEFAULT_MAX_BUFFER_MS / 10,
            bufferForPlaybackMs = DEFAULT_BUFFER_FOR_PLAYBACK_MS / 10,
            bufferForPlaybackAfterRebufferMs = DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS / 10
        ))
    })

    fun get(context: Context): Kohii = capsule.get(context)
}