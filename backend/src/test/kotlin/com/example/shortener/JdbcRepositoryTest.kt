package com.example.shortener

import com.example.shortener.click.Click
import com.example.shortener.click.JdbcClickRepository
import com.example.shortener.shortlink.JdbcShortLinkRepository
import com.example.shortener.shortlink.ShortLink
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest
import org.springframework.context.annotation.Import
import org.springframework.dao.DataIntegrityViolationException
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(PostgresTestConfiguration::class, JdbcShortLinkRepository::class, JdbcClickRepository::class)
class JdbcRepositoryTest {
    @Autowired
    private lateinit var links: JdbcShortLinkRepository

    @Autowired
    private lateinit var clicks: JdbcClickRepository

    private val now = Instant.parse("2026-10-13T12:00:00.123456Z")

    private fun link(
        shortCode: String = "abc1234",
        originalUrl: String = "https://example.com",
        expiresAt: Instant? = null,
    ) = ShortLink(shortCode, originalUrl, createdAt = now, expiresAt = expiresAt)

    private fun click(
        shortCode: String = "abc1234",
        at: Instant = now,
    ) = Click(shortCode, at, referrer = "google.com", userAgent = "ua", ip = "203.0.113.7")

    @Test
    fun `a stored link reads back unchanged, with and without expiry`() {
        val withExpiry = link(expiresAt = now.plusSeconds(3600))
        val withoutExpiry = link(shortCode = "nolimit")

        assertTrue(links.saveIfAbsent(withExpiry))
        assertTrue(links.saveIfAbsent(withoutExpiry))

        assertEquals(withExpiry, links.findByCode("abc1234"))
        assertEquals(withoutExpiry, links.findByCode("nolimit"))
        assertEquals(setOf(withExpiry, withoutExpiry), links.findAll().toSet())
    }

    @Test
    fun `saveIfAbsent refuses a taken code and keeps the first link`() {
        val first = link()

        assertTrue(links.saveIfAbsent(first))
        assertFalse(links.saveIfAbsent(link(originalUrl = "https://other.example")))
        assertEquals(first, links.findByCode("abc1234"))
    }

    @Test
    fun `findByCode returns null for an unknown code`() {
        assertNull(links.findByCode("zzzzzzz"))
    }

    @Test
    fun `clicks read back per link, oldest first, and are counted`() {
        links.saveIfAbsent(link())
        links.saveIfAbsent(link(shortCode = "other12"))
        val later = click(at = now.plusSeconds(60))
        val earlier = click()
        clicks.save(later)
        clicks.save(earlier)
        clicks.save(click(shortCode = "other12"))

        assertEquals(listOf(earlier, later), clicks.findByShortCode("abc1234"))
        assertEquals(2, clicks.countByShortCode("abc1234"))
        assertEquals(0, clicks.countByShortCode("zzzzzzz"))
    }

    @Test
    fun `a click for an unknown link is rejected`() {
        assertFailsWith<DataIntegrityViolationException> { clicks.save(click(shortCode = "zzzzzzz")) }
    }
}
