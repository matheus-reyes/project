package com.example.shortener.shortlink

import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class ShortLinkServiceTest {
    private val now = Instant.parse("2026-10-13T12:00:00Z")
    private val repository = InMemoryShortLinkRepository()

    private fun serviceAt(
        instant: Instant,
        generator: ShortCodeGenerator = SecureRandomShortCodeGenerator(),
    ) = ShortLinkService(repository, generator, Clock.fixed(instant, ZoneOffset.UTC))

    @Test
    fun `creates a short link stamped with the clock`() {
        val link = serviceAt(now).create(url = "https://example.com", expiresAt = null)

        assertEquals("https://example.com", link.originalUrl)
        assertEquals(now, link.createdAt)
        assertNull(link.expiresAt)
        assertEquals(link, repository.findByCode(link.shortCode))
    }

    @Test
    fun `creates a new short link for a duplicate url`() {
        val service = serviceAt(now)

        val first = service.create(url = "https://example.com", expiresAt = null)
        val second = service.create(url = "https://example.com", expiresAt = null)

        assertEquals(2, setOf(first.shortCode, second.shortCode).size)
    }

    @Test
    fun `retries code generation on collision`() {
        val codes = ArrayDeque(listOf("aaaaaaa", "aaaaaaa", "bbbbbbb"))
        val service = serviceAt(now, generator = { codes.removeFirst() })

        service.create(url = "https://example.com", expiresAt = null)
        val second = service.create(url = "https://example.org", expiresAt = null)

        assertEquals("bbbbbbb", second.shortCode)
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "ftp://x.com",
            "javascript:alert(1)",
            "not a url",
            "example.com",
            "https://",
            "http:///path",
            "",
            "   ",
        ],
    )
    fun `rejects urls that are not http or https with a host`(url: String) {
        assertFailsWith<InvalidShortLinkRequestException> {
            serviceAt(now).create(url = url, expiresAt = null)
        }
    }

    @Test
    fun `rejects an expiresAt in the past`() {
        assertFailsWith<InvalidShortLinkRequestException> {
            serviceAt(now).create(url = "https://example.com", expiresAt = now.minusSeconds(1))
        }
    }

    @Test
    fun `rejects an expiresAt equal to now`() {
        assertFailsWith<InvalidShortLinkRequestException> {
            serviceAt(now).create(url = "https://example.com", expiresAt = now)
        }
    }

    @Test
    fun `resolves a link before it expires`() {
        val expiresAt = now.plus(Duration.ofHours(1))
        val link = serviceAt(now).create(url = "https://example.com", expiresAt = expiresAt)

        val resolved = serviceAt(expiresAt.minusSeconds(1)).resolve(link.shortCode)

        assertEquals(link, resolved)
    }

    @Test
    fun `treats a link as expired once the clock reaches expiresAt`() {
        val expiresAt = now.plus(Duration.ofHours(1))
        val link = serviceAt(now).create(url = "https://example.com", expiresAt = expiresAt)

        assertFailsWith<ShortLinkExpiredException> {
            serviceAt(expiresAt).resolve(link.shortCode)
        }
    }

    @Test
    fun `fails to resolve an unknown code`() {
        assertFailsWith<ShortLinkNotFoundException> {
            serviceAt(now).resolve("zzzzzzz")
        }
    }
}
