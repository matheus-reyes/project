package com.example.shortener.stats

import com.example.shortener.click.ClickRepository
import com.example.shortener.shortlink.ShortLink
import com.example.shortener.shortlink.ShortLinkNotFoundException
import com.example.shortener.shortlink.ShortLinkRepository
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneOffset

data class DayCount(
    val date: LocalDate,
    val count: Int,
)

data class ReferrerCount(
    val referrer: String,
    val count: Int,
)

data class LinkStats(
    val link: ShortLink,
    val totalClicks: Int,
    val clicksByDay: List<DayCount>,
    val topReferrers: List<ReferrerCount>,
)

@Service
class StatsService(
    private val links: ShortLinkRepository,
    private val clicks: ClickRepository,
    private val clock: Clock,
) {
    /** Works for Expired Links too; days are UTC calendar days. */
    fun stats(shortCode: String): LinkStats {
        val link = links.findByCode(shortCode) ?: throw ShortLinkNotFoundException(shortCode)
        val linkClicks = clicks.findByShortCode(shortCode)
        val today = LocalDate.ofInstant(clock.instant(), ZoneOffset.UTC)
        val countsByDay = linkClicks.groupingBy { LocalDate.ofInstant(it.timestamp, ZoneOffset.UTC) }.eachCount()
        val clicksByDay =
            (DAYS - 1 downTo 0).map { daysAgo ->
                val date = today.minusDays(daysAgo.toLong())
                DayCount(date, countsByDay[date] ?: 0)
            }
        val topReferrers =
            linkClicks
                .groupingBy { it.referrer }
                .eachCount()
                .map { (referrer, count) -> ReferrerCount(referrer, count) }
                .sortedWith(compareByDescending<ReferrerCount> { it.count }.thenBy { it.referrer })
                .take(TOP_REFERRERS)
        return LinkStats(link, linkClicks.size, clicksByDay, topReferrers)
    }

    private companion object {
        const val DAYS = 30
        const val TOP_REFERRERS = 5
    }
}
