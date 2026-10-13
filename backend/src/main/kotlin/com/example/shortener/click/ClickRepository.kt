package com.example.shortener.click

import org.springframework.stereotype.Repository
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

interface ClickRepository {
    fun save(click: Click)

    fun findByShortCode(shortCode: String): List<Click>

    fun countByShortCode(shortCode: String): Int
}

@Repository
class InMemoryClickRepository : ClickRepository {
    private val clicksByCode = ConcurrentHashMap<String, ConcurrentLinkedQueue<Click>>()

    override fun save(click: Click) {
        clicksByCode.computeIfAbsent(click.shortCode) { ConcurrentLinkedQueue() }.add(click)
    }

    override fun findByShortCode(shortCode: String): List<Click> = clicksByCode[shortCode]?.toList().orEmpty()

    override fun countByShortCode(shortCode: String): Int = clicksByCode[shortCode]?.size ?: 0
}
