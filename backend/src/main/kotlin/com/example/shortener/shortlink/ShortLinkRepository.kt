package com.example.shortener.shortlink

interface ShortLinkRepository {
    /** Stores the link unless its code is taken; returns whether it was stored. */
    fun saveIfAbsent(link: ShortLink): Boolean

    fun findByCode(shortCode: String): ShortLink?

    fun findAll(): List<ShortLink>
}
