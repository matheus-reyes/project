package com.example.shortener.shortlink

import java.time.Instant

data class ShortLink(
    val shortCode: String,
    val originalUrl: String,
    val createdAt: Instant,
    val expiresAt: Instant?,
) {
    fun isExpiredAt(instant: Instant): Boolean = expiresAt != null && !instant.isBefore(expiresAt)
}
