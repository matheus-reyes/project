package com.example.shortener.stats

import com.example.shortener.AppProperties
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

data class StatsResponse(
    val shortCode: String,
    val shortUrl: String,
    val originalUrl: String,
    val expiresAt: Instant?,
    val totalClicks: Int,
    val clicksByDay: List<DayCount>,
    val topReferrers: List<ReferrerCount>,
)

@RestController
class StatsController(
    private val service: StatsService,
    private val properties: AppProperties,
) {
    @GetMapping("/api/stats/{shortCode}")
    fun stats(
        @PathVariable shortCode: String,
    ): StatsResponse {
        val stats = service.stats(shortCode)
        return StatsResponse(
            shortCode = stats.link.shortCode,
            shortUrl = properties.shortUrlFor(stats.link.shortCode),
            originalUrl = stats.link.originalUrl,
            expiresAt = stats.link.expiresAt,
            totalClicks = stats.totalClicks,
            clicksByDay = stats.clicksByDay,
            topReferrers = stats.topReferrers,
        )
    }
}
