package com.example.shortener.click

interface ClickRepository {
    fun save(click: Click)

    fun findByShortCode(shortCode: String): List<Click>

    fun countByShortCode(shortCode: String): Int
}
