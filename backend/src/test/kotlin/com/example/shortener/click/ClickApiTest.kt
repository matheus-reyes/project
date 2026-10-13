package com.example.shortener.click

import com.example.shortener.MutableClock
import com.jayway.jsonpath.JsonPath
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import java.time.Duration
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@SpringBootTest
@AutoConfigureMockMvc
@Import(ClickApiTest.ClockConfig::class)
class ClickApiTest {
    @TestConfiguration
    class ClockConfig {
        @Bean
        @Primary
        fun mutableClock() = MutableClock(Instant.parse("2026-10-13T12:00:00Z"))
    }

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var clock: MutableClock

    @Autowired
    private lateinit var clicks: ClickRepository

    private fun createCode(body: String): String {
        val response =
            mockMvc
                .post("/api/shorten") {
                    contentType = MediaType.APPLICATION_JSON
                    content = body
                }.andReturn()
                .response.contentAsString
        return JsonPath.read(response, "$.shortCode")
    }

    private fun listLinks(): String =
        mockMvc
            .get("/api/urls")
            .andExpect { status { isOk() } }
            .andReturn()
            .response.contentAsString

    @Test
    fun `a redirect records a click with referrer host, user agent and ip`() {
        val code = createCode("""{"url":"https://example.com"}""")

        mockMvc
            .get("/$code") {
                header("Referer", "https://google.com/search?q=x")
                header("User-Agent", "TestBrowser/1.0")
                with { request ->
                    request.remoteAddr = "203.0.113.7"
                    request
                }
            }.andExpect { status { isFound() } }

        val recorded = clicks.findByShortCode(code)
        assertEquals(1, recorded.size)
        val click = recorded.single()
        assertEquals("google.com", click.referrer)
        assertEquals("TestBrowser/1.0", click.userAgent)
        assertEquals("203.0.113.7", click.ip)
        assertEquals(clock.instant(), click.timestamp)
    }

    @Test
    fun `a redirect without referer records direct`() {
        val code = createCode("""{"url":"https://example.com"}""")

        mockMvc.get("/$code").andExpect { status { isFound() } }

        assertEquals("direct", clicks.findByShortCode(code).single().referrer)
    }

    @Test
    fun `an expired link records no click`() {
        val expiresAt = clock.instant().plus(Duration.ofMinutes(1))
        val code = createCode("""{"url":"https://example.com","expiresAt":"$expiresAt"}""")
        clock.advance(Duration.ofMinutes(2))

        mockMvc.get("/$code").andExpect { status { isGone() } }

        assertTrue(clicks.findByShortCode(code).isEmpty())
    }

    @Test
    fun `an unknown code records no click`() {
        mockMvc.get("/zzzzzzz").andExpect { status { isNotFound() } }

        assertTrue(clicks.findByShortCode("zzzzzzz").isEmpty())
    }

    @Test
    fun `GET urls lists links newest first with click counts`() {
        val older = createCode("""{"url":"https://example.com/older"}""")
        clock.advance(Duration.ofSeconds(1))
        val newer = createCode("""{"url":"https://example.com/newer"}""")
        repeat(3) { mockMvc.get("/$older") }

        val body = listLinks()

        val codes: List<String> = JsonPath.read(body, "$[*].shortCode")
        assertTrue(codes.indexOf(newer) < codes.indexOf(older))
        val createdAts: List<String> = JsonPath.read(body, "$[*].createdAt")
        assertEquals(createdAts.sortedDescending(), createdAts)
        assertEquals(listOf(3), JsonPath.read<List<Int>>(body, "$[?(@.shortCode == '$older')].clickCount"))
        assertEquals(listOf(0), JsonPath.read<List<Int>>(body, "$[?(@.shortCode == '$newer')].clickCount"))
        assertEquals(
            listOf("https://example.com/newer"),
            JsonPath.read<List<String>>(body, "$[?(@.shortCode == '$newer')].originalUrl"),
        )
    }

    @Test
    fun `GET urls keeps expired links`() {
        val expiresAt = clock.instant().plus(Duration.ofMinutes(1))
        val code = createCode("""{"url":"https://example.com","expiresAt":"$expiresAt"}""")
        clock.advance(Duration.ofMinutes(2))

        val codes: List<String> = JsonPath.read(listLinks(), "$[*].shortCode")

        assertTrue(code in codes)
    }
}
