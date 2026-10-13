package com.example.shortener.shortlink

import com.example.shortener.click.Click
import com.example.shortener.click.ClickRepository
import com.example.shortener.click.referrerOf
import org.springframework.stereotype.Service
import java.net.URI
import java.net.URISyntaxException
import java.time.Clock
import java.time.Instant

data class ShortLinkSummary(
    val link: ShortLink,
    val clickCount: Int,
)

@Service
class ShortLinkService(
    private val repository: ShortLinkRepository,
    private val clicks: ClickRepository,
    private val generator: ShortCodeGenerator,
    private val clock: Clock,
) {
    fun create(
        url: String?,
        expiresAt: Instant?,
    ): ShortLink {
        val originalUrl = validUrl(url)
        val now = clock.instant()
        if (expiresAt != null && !expiresAt.isAfter(now)) {
            throw InvalidShortLinkRequestException("expiresAt must be in the future")
        }
        repeat(MAX_ATTEMPTS) {
            val link = ShortLink(generator.generate(), originalUrl, createdAt = now, expiresAt = expiresAt)
            if (repository.saveIfAbsent(link)) return link
        }
        throw IllegalStateException("could not generate a unique short code after $MAX_ATTEMPTS attempts")
    }

    fun resolve(shortCode: String): ShortLink {
        val link = repository.findByCode(shortCode) ?: throw ShortLinkNotFoundException(shortCode)
        if (link.isExpiredAt(clock.instant())) throw ShortLinkExpiredException(shortCode)
        return link
    }

    /** Resolves the link and records a Click; unknown or expired links record nothing. */
    fun follow(
        shortCode: String,
        referer: String?,
        userAgent: String?,
        ip: String?,
    ): ShortLink {
        val link = resolve(shortCode)
        clicks.save(
            Click(
                shortCode = shortCode,
                timestamp = clock.instant(),
                referrer = referrerOf(referer),
                userAgent = userAgent,
                ip = ip,
            ),
        )
        return link
    }

    fun list(): List<ShortLinkSummary> =
        repository
            .findAll()
            .sortedByDescending { it.createdAt }
            .map { ShortLinkSummary(it, clicks.countByShortCode(it.shortCode)) }

    private fun validUrl(url: String?): String {
        val trimmed = url?.trim().orEmpty()
        if (trimmed.isEmpty()) throw InvalidShortLinkRequestException("url is required")
        val uri =
            try {
                URI(trimmed)
            } catch (e: URISyntaxException) {
                throw InvalidShortLinkRequestException("url is malformed")
            }
        if (uri.scheme?.lowercase() !in ALLOWED_SCHEMES) {
            throw InvalidShortLinkRequestException("url must use http or https")
        }
        if (uri.host.isNullOrBlank()) throw InvalidShortLinkRequestException("url must have a host")
        return trimmed
    }

    private companion object {
        const val MAX_ATTEMPTS = 10
        val ALLOWED_SCHEMES = setOf("http", "https")
    }
}
