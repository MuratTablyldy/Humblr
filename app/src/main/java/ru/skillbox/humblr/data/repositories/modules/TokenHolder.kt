package ru.skillbox.humblr.data.repositories.modules

import org.threeten.bp.Instant

data class TokenHolder(val map: Map<String, String?>) {
    val access_token: String? by map
    val token_type: String? by map
    val state: String? by map
    val expires_in: String? by map
    val expires_when: String? = when {
        map.containsKey("expires_when") && map["expires_when"] != null -> {
            map["expires_when"]!!
        }
        else -> {
            if (expires_in != null) {
                val mutMap = map as MutableMap<String, String?>
                mutMap.set(
                    "expires_when",
                    Instant.now().plusSeconds(expires_in!!.toLong()).toEpochMilli().toString()
                )
                mutMap["expires_when"]
            } else null

        }
    }


    companion object {
    }

    enum class KEYS {
        access_token, token_type, state, expires_in, expires_when
    }
}
