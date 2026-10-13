package com.example.shortener.seed

import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeedDataTest {
    private val now = Instant.parse("2026-10-13T12:00:00Z")
    private val seed = SeedData.build(now)

    @Test
    fun `builds three links, one of them expired`() {
        assertEquals(3, seed.links.size)
        assertEquals(1, seed.links.count { it.isExpiredAt(now) })
    }

    @Test
    fun `is deterministic for the same clock`() {
        assertEquals(seed, SeedData.build(now))
    }

    @Test
    fun `clicks fall between creation and expiry, within the last 30 days`() {
        val linksByCode = seed.links.associateBy { it.shortCode }
        val windowStart = now.minus(Duration.ofDays(30))

        for (click in seed.clicks) {
            val link = linksByCode.getValue(click.shortCode)
            assertTrue(click.timestamp >= link.createdAt, "click before creation: $click")
            assertTrue(click.timestamp > windowStart, "click outside the window: $click")
            assertTrue(click.timestamp <= now, "click in the future: $click")
            assertTrue(link.expiresAt == null || click.timestamp < link.expiresAt, "click after expiry: $click")
        }
    }

    @Test
    fun `mixes the four referrers and spreads clicks over many days`() {
        assertEquals(
            setOf("direct", "google.com", "twitter.com", "linkedin.com"),
            seed.clicks.map { it.referrer }.toSet(),
        )
        val busiest =
            seed.clicks
                .groupBy { it.shortCode }
                .maxBy { it.value.size }
                .value
        val distinctDays = busiest.map { it.timestamp.toString().substring(0, 10) }.toSet()
        assertTrue(distinctDays.size >= 15, "clicks on only ${distinctDays.size} days")
    }

    @Test
    fun `every link has clicks`() {
        val codesWithClicks = seed.clicks.map { it.shortCode }.toSet()

        assertTrue(seed.links.all { it.shortCode in codesWithClicks })
    }
}
