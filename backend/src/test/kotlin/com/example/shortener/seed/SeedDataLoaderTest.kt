package com.example.shortener.seed

import com.example.shortener.PostgresTestConfiguration
import com.example.shortener.click.ClickRepository
import com.example.shortener.shortlink.ShortLinkRepository
import com.jayway.jsonpath.JsonPath
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.DefaultApplicationArguments
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@SpringBootTest(properties = ["app.seed.enabled=true"])
@Import(PostgresTestConfiguration::class)
@AutoConfigureMockMvc
class SeedDataLoaderTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var loader: SeedDataLoader

    @Autowired
    private lateinit var links: ShortLinkRepository

    @Autowired
    private lateinit var clicks: ClickRepository

    private fun getJson(path: String): String =
        mockMvc
            .get(path)
            .andExpect { status { isOk() } }
            .andReturn()
            .response.contentAsString

    @Test
    fun `seeds three links on startup and one of them has clicks`() {
        val body = getJson("/api/urls")

        val codes: List<String> = JsonPath.read(body, "$[*].shortCode")
        val clickCounts: List<Int> = JsonPath.read(body, "$[*].clickCount")
        assertEquals(setOf("kotlin1", "spring1", "expired"), codes.toSet())
        assertTrue(clickCounts.any { it > 0 })
    }

    @Test
    fun `a seeded link has stats with the four referrers`() {
        val body = getJson("/api/stats/kotlin1")

        val referrers: List<String> = JsonPath.read(body, "$.topReferrers[*].referrer")
        assertEquals(setOf("direct", "google.com", "twitter.com", "linkedin.com"), referrers.toSet())
    }

    @Test
    fun `running the loader again adds no links or clicks`() {
        val linksBefore = links.findAll().size
        val clicksBefore = clicks.countByShortCode("kotlin1")

        loader.run(DefaultApplicationArguments())

        assertEquals(linksBefore, links.findAll().size)
        assertEquals(clicksBefore, clicks.countByShortCode("kotlin1"))
    }
}
