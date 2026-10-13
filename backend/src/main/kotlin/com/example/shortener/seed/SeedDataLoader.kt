package com.example.shortener.seed

import com.example.shortener.click.ClickRepository
import com.example.shortener.shortlink.ShortLinkRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty
import org.springframework.stereotype.Component
import java.time.Clock

@Component
@ConditionalOnBooleanProperty("app.seed.enabled", matchIfMissing = true)
class SeedDataLoader(
    private val links: ShortLinkRepository,
    private val clicks: ClickRepository,
    private val clock: Clock,
) : ApplicationRunner {
    private val log = LoggerFactory.getLogger(javaClass)

    override fun run(args: ApplicationArguments) {
        val seed = SeedData.build(clock.instant())
        for (link in seed.links) {
            links.saveIfAbsent(link)
        }
        for (click in seed.clicks) {
            clicks.save(click)
        }
        log.info("Seeded {} short links and {} clicks", seed.links.size, seed.clicks.size)
    }
}
