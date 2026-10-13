package com.example.shortener.shortlink

import java.util.concurrent.ConcurrentHashMap

/** Test double for unit tests that do not need Postgres. */
class InMemoryShortLinkRepository : ShortLinkRepository {
    private val links = ConcurrentHashMap<String, ShortLink>()

    override fun saveIfAbsent(link: ShortLink): Boolean = links.putIfAbsent(link.shortCode, link) == null

    override fun findByCode(shortCode: String): ShortLink? = links[shortCode]

    override fun findAll(): List<ShortLink> = links.values.toList()
}
