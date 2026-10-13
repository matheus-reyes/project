package com.example.shortener.click

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

/** Test double for unit tests that do not need Postgres. */
class InMemoryClickRepository : ClickRepository {
    private val clicksByCode = ConcurrentHashMap<String, ConcurrentLinkedQueue<Click>>()

    override fun save(click: Click) {
        clicksByCode.computeIfAbsent(click.shortCode) { ConcurrentLinkedQueue() }.add(click)
    }

    override fun findByShortCode(shortCode: String): List<Click> = clicksByCode[shortCode]?.toList().orEmpty()

    override fun countByShortCode(shortCode: String): Int = clicksByCode[shortCode]?.size ?: 0
}
