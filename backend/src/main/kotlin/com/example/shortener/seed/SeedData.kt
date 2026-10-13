package com.example.shortener.seed

import com.example.shortener.click.Click
import com.example.shortener.click.DIRECT_REFERRER
import com.example.shortener.shortlink.ShortLink
import java.time.Duration
import java.time.Instant
import kotlin.random.Random

data class Seed(
    val links: List<ShortLink>,
    val clicks: List<Click>,
)

/** Demo data relative to `now`; a fixed random seed makes every run identical. */
object SeedData {
    private const val RANDOM_SEED = 42

    private data class SeedLink(
        val shortCode: String,
        val originalUrl: String,
        val createdDaysAgo: Long,
        val expiredDaysAgo: Long?,
        val clickCount: Int,
    )

    private val SEED_LINKS =
        listOf(
            SeedLink("kotlin1", "https://kotlinlang.org/docs/home.html", createdDaysAgo = 29, expiredDaysAgo = null, clickCount = 120),
            SeedLink("spring1", "https://spring.io/projects/spring-boot", createdDaysAgo = 12, expiredDaysAgo = null, clickCount = 40),
            SeedLink("expired", "https://example.com/summer-campaign", createdDaysAgo = 25, expiredDaysAgo = 6, clickCount = 30),
        )

    // Weighted: direct traffic is the most common, then search, then social
    private val REFERRERS =
        List(4) { DIRECT_REFERRER } + List(3) { "google.com" } + List(2) { "twitter.com" } + List(2) { "linkedin.com" }

    private val USER_AGENTS =
        listOf(
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0 Safari/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 14_6) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/18.0 Safari/605.1.15",
            "Mozilla/5.0 (iPhone; CPU iPhone OS 18_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Mobile/15E148",
        )

    fun build(now: Instant): Seed {
        val random = Random(RANDOM_SEED)
        val links = SEED_LINKS.map { it.toShortLink(now) }
        val clicks =
            SEED_LINKS.zip(links).flatMap { (seedLink, link) ->
                val end = link.expiresAt ?: now
                val spanSeconds = Duration.between(link.createdAt, end).seconds
                List(seedLink.clickCount) {
                    Click(
                        shortCode = link.shortCode,
                        timestamp = link.createdAt.plusSeconds(random.nextLong(spanSeconds)),
                        referrer = REFERRERS.random(random),
                        userAgent = USER_AGENTS.random(random),
                        // 203.0.113.0/24 is reserved for documentation, so these never match a real visitor
                        ip = "203.0.113.${random.nextInt(1, 255)}",
                    )
                }
            }
        return Seed(links, clicks.sortedBy { it.timestamp })
    }

    private fun SeedLink.toShortLink(now: Instant) =
        ShortLink(
            shortCode = shortCode,
            originalUrl = originalUrl,
            createdAt = now.minus(Duration.ofDays(createdDaysAgo)),
            expiresAt = expiredDaysAgo?.let { now.minus(Duration.ofDays(it)) },
        )
}
