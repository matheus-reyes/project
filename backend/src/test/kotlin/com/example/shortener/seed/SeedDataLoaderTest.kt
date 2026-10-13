package com.example.shortener.seed

import com.jayway.jsonpath.JsonPath
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@SpringBootTest(properties = ["app.seed.enabled=true"])
@AutoConfigureMockMvc
class SeedDataLoaderTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

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
}
