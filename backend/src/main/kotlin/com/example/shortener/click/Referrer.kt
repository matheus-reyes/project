package com.example.shortener.click

import java.net.URI
import java.net.URISyntaxException

const val DIRECT_REFERRER = "direct"

/** Reduces a `Referer` header to its host, so clicks group by site; anything unusable counts as direct. */
fun referrerOf(referer: String?): String {
    val host =
        try {
            referer?.trim()?.takeIf { it.isNotEmpty() }?.let { URI(it).host }
        } catch (e: URISyntaxException) {
            null
        }
    return host?.lowercase()?.removePrefix("www.")?.takeIf { it.isNotEmpty() } ?: DIRECT_REFERRER
}
