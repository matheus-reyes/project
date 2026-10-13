package com.example.shortener.seed

import com.example.shortener.click.ClickRepository
import com.example.shortener.shortlink.ShortLinkRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.Clock

@Component
@ConditionalOnBooleanProperty("app.seed.enabled", matchIfMissing = true)
class SeedDataLoader(
    private val links: ShortLinkRepository,
    private val clicks: ClickRepository,
    private val clock: Clock,
) : ApplicationRunner {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    override fun run(args: ApplicationArguments) {
        val seed = SeedData.build(clock.instant())
        // Postgres keeps data across restarts: only links stored now get their clicks, so a rerun adds nothing
        val newCodes =
            seed.links
                .filter { links.saveIfAbsent(it) }
                .map { it.shortCode }
                .toSet()
        val newClicks = seed.clicks.filter { it.shortCode in newCodes }
        newClicks.forEach(clicks::save)
        log.info("Seeded {} short links and {} clicks", newCodes.size, newClicks.size)
    }
}
