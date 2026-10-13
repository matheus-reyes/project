package com.example.shortener.shortlink

class InvalidShortLinkRequestException(
    message: String,
) : RuntimeException(message)

class ShortLinkNotFoundException(
    shortCode: String,
) : RuntimeException("short link '$shortCode' not found")

class ShortLinkExpiredException(
    shortCode: String,
) : RuntimeException("short link '$shortCode' has expired")
