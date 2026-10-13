package com.example.shortener.stats

import com.example.shortener.click.Click
import com.example.shortener.click.InMemoryClickRepository
import com.example.shortener.shortlink.InMemoryShortLinkRepository
import com.example.shortener.shortlink.ShortLink
import com.example.shortener.shortlink.ShortLinkNotFoundException
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class StatsServiceTest {
    private val now = Instant.parse("2026-10-13T12:00:00Z")
    private val today = LocalDate.parse("2026-10-13")
    private val links = InMemoryShortLinkRepository()
    private val clicks = InMemoryClickRepository()
    private val service = StatsService(links, clicks, Clock.fixed(now, ZoneOffset.UTC))

    private fun link(
        shortCode: String = "abc1234",
        expiresAt: Instant? = null,
    ) = ShortLink(shortCode, "https://example.com", createdAt = now.minus(Duration.ofDays(60)), expiresAt = expiresAt)
        .also { links.saveIfAbsent(it) }

    private fun click(
        at: Instant,
        referrer: String = "direct",
        shortCode: String = "abc1234",
    ) = clicks.save(Click(shortCode, at, referrer, userAgent = "ua", ip = "127.0.0.1"))

    private fun daysAgo(days: Long): Instant = now.minus(Duration.ofDays(days))

    @Test
    fun `returns exactly 30 days ending today, oldest first`() {
        link()

        val days = service.stats("abc1234").clicksByDay

        assertEquals(30, days.size)
        assertEquals(today.minusDays(29), days.first().date)
        assertEquals(today, days.last().date)
        assertEquals(days.sortedBy { it.date }, days)
    }

    @Test
    fun `a click older than 30 days counts in the total but not per day`() {
        link()
        click(daysAgo(1))
        click(daysAgo(15))
        click(daysAgo(31))

        val stats = service.stats("abc1234")

        assertEquals(3, stats.totalClicks)
        assertEquals(2, stats.clicksByDay.sumOf { it.count })
        val counts = stats.clicksByDay.associate { it.date to it.count }
        assertEquals(1, counts[today.minusDays(1)])
        assertEquals(1, counts[today.minusDays(15)])
        assertEquals(0, counts[today])
        assertEquals(28, counts.values.count { it == 0 })
    }

    @Test
    fun `the oldest day of the window is included`() {
        link()
        click(LocalDate.parse("2026-09-14").atStartOfDay().toInstant(ZoneOffset.UTC))
        click(LocalDate.parse("2026-09-13").atTime(23, 59, 59).toInstant(ZoneOffset.UTC))

        val stats = service.stats("abc1234")

        assertEquals(1, stats.clicksByDay.first().count)
        assertEquals(1, stats.clicksByDay.sumOf { it.count })
    }

    @Test
    fun `clicks at 23 59 and 00 01 UTC land on different days`() {
        link()
        click(Instant.parse("2026-10-12T23:59:00Z"))
        click(Instant.parse("2026-10-13T00:01:00Z"))

        val counts = service.stats("abc1234").clicksByDay.associate { it.date to it.count }

        assertEquals(1, counts[LocalDate.parse("2026-10-12")])
        assertEquals(1, counts[LocalDate.parse("2026-10-13")])
    }

    @Test
    fun `top referrers are sorted by count and capped at 5`() {
        link()
        val referrerCounts =
            mapOf(
                "direct" to 7,
                "google.com" to 5,
                "twitter.com" to 4,
                "linkedin.com" to 3,
                "reddit.com" to 2,
                "bing.com" to 1,
            )
        for ((referrer, count) in referrerCounts) {
            repeat(count) { click(daysAgo(2), referrer = referrer) }
        }

        val top = service.stats("abc1234").topReferrers

        assertEquals(
            listOf(
                ReferrerCount("direct", 7),
                ReferrerCount("google.com", 5),
                ReferrerCount("twitter.com", 4),
                ReferrerCount("linkedin.com", 3),
                ReferrerCount("reddit.com", 2),
            ),
            top,
        )
    }

    @Test
    fun `only counts clicks of the requested link`() {
        link()
        link(shortCode = "other12")
        click(daysAgo(1))
        click(daysAgo(1), shortCode = "other12")

        assertEquals(1, service.stats("abc1234").totalClicks)
    }

    @Test
    fun `works for an expired link`() {
        val link = link(expiresAt = daysAgo(1))
        click(daysAgo(2))

        val stats = service.stats("abc1234")

        assertEquals(1, stats.totalClicks)
        assertEquals(link, stats.link)
    }

    @Test
    fun `an empty link has zero clicks and no referrers`() {
        link()

        val stats = service.stats("abc1234")

        assertEquals(0, stats.totalClicks)
        assertEquals(emptyList(), stats.topReferrers)
        assertEquals(30, stats.clicksByDay.count { it.count == 0 })
    }

    @Test
    fun `fails for an unknown code`() {
        assertFailsWith<ShortLinkNotFoundException> { service.stats("zzzzzzz") }
    }
}
