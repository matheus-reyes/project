package com.example.shortener.shortlink

import com.example.shortener.MutableClock
import org.hamcrest.Matchers.matchesPattern
import org.hamcrest.Matchers.nullValue
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

@SpringBootTest
@AutoConfigureMockMvc
@Import(ShortLinkApiTest.ClockConfig::class)
class ShortLinkApiTest {
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

    private fun shorten(body: String) =
        mockMvc.post("/api/shorten") {
            contentType = MediaType.APPLICATION_JSON
            content = body
        }

    private fun createCode(body: String): String {
        val response = shorten(body).andReturn().response.contentAsString
        return Regex("\"shortCode\"\\s*:\\s*\"([A-Za-z0-9]+)\"").find(response)!!.groupValues[1]
    }

    @Test
    fun `POST shorten returns 201 with the full body`() {
        val now = clock.instant()

        shorten("""{"url":"https://example.com"}""").andExpect {
            status { isCreated() }
            jsonPath("$.shortCode") { value(matchesPattern("^[A-Za-z0-9]{7}$")) }
            jsonPath("$.shortUrl") { value(matchesPattern("^http://localhost:8080/[A-Za-z0-9]{7}$")) }
            jsonPath("$.originalUrl") { value("https://example.com") }
            jsonPath("$.createdAt") { value(now.toString()) }
            jsonPath("$.expiresAt") { value(nullValue()) }
        }
    }

    @Test
    fun `POST shorten echoes a future expiresAt`() {
        val expiresAt = clock.instant().plus(Duration.ofDays(1))

        shorten("""{"url":"https://example.com","expiresAt":"$expiresAt"}""").andExpect {
            status { isCreated() }
            jsonPath("$.expiresAt") { value(expiresAt.toString()) }
        }
    }

    @Test
    fun `POST shorten returns 400 for a url that is not http or https`() {
        shorten("""{"url":"ftp://x.com"}""").andExpect {
            status { isBadRequest() }
            jsonPath("$.error") { value("url must use http or https") }
        }
    }

    @Test
    fun `POST shorten returns 400 for an expiresAt in the past`() {
        val expiresAt = clock.instant().minusSeconds(60)

        shorten("""{"url":"https://example.com","expiresAt":"$expiresAt"}""").andExpect {
            status { isBadRequest() }
            jsonPath("$.error") { value("expiresAt must be in the future") }
        }
    }

    @Test
    fun `POST shorten returns 400 for a missing url`() {
        shorten("""{}""").andExpect {
            status { isBadRequest() }
            jsonPath("$.error") { exists() }
        }
    }

    @Test
    fun `POST shorten returns 400 for a malformed body`() {
        shorten("""{"url":"https://example.com","expiresAt":"tomorrow"}""").andExpect {
            status { isBadRequest() }
            jsonPath("$.error") { exists() }
        }
    }

    @Test
    fun `GET code redirects with 302 to the original url`() {
        val code = createCode("""{"url":"https://example.com/page?q=1"}""")

        mockMvc.get("/$code").andExpect {
            status { isFound() }
            header { string("Location", "https://example.com/page?q=1") }
        }
    }

    @Test
    fun `GET code returns 404 for an unknown code`() {
        mockMvc.get("/zzzzzzz").andExpect {
            status { isNotFound() }
            jsonPath("$.error") { exists() }
        }
    }

    @Test
    fun `GET code returns 410 for an expired link`() {
        val expiresAt = clock.instant().plus(Duration.ofMinutes(1))
        val code = createCode("""{"url":"https://example.com","expiresAt":"$expiresAt"}""")

        clock.advance(Duration.ofMinutes(2))

        mockMvc.get("/$code").andExpect {
            status { isGone() }
            jsonPath("$.error") { exists() }
        }
    }
}
