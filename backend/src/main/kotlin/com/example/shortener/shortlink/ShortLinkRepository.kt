package com.example.shortener.shortlink

import org.springframework.stereotype.Repository
import java.util.concurrent.ConcurrentHashMap

interface ShortLinkRepository {
    /** Stores the link unless its code is taken; returns whether it was stored. */
    fun saveIfAbsent(link: ShortLink): Boolean

    fun findByCode(shortCode: String): ShortLink?
}

@Repository
class InMemoryShortLinkRepository : ShortLinkRepository {
    private val links = ConcurrentHashMap<String, ShortLink>()

    override fun saveIfAbsent(link: ShortLink): Boolean = links.putIfAbsent(link.shortCode, link) == null

    override fun findByCode(shortCode: String): ShortLink? = links[shortCode]
}
