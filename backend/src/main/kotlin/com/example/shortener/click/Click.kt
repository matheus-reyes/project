package com.example.shortener.click

import java.time.Instant

data class Click(
    val shortCode: String,
    val timestamp: Instant,
    val referrer: String,
    val userAgent: String?,
    val ip: String?,
)
