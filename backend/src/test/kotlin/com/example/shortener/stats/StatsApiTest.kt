package com.example.shortener.stats

import com.jayway.jsonpath.JsonPath
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.test.assertEquals

@SpringBootTest
@AutoConfigureMockMvc
class StatsApiTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `GET stats returns 200 with the expected shape`() {
        val response =
            mockMvc
                .post("/api/shorten") {
                    contentType = MediaType.APPLICATION_JSON
                    content = """{"url":"https://example.com"}"""
                }.andReturn()
                .response.contentAsString
        val code: String = JsonPath.read(response, "$.shortCode")
        mockMvc.get("/$code") { header("Referer", "https://google.com/search?q=x") }
        mockMvc.get("/$code")
        val today = LocalDate.now(ZoneOffset.UTC).toString()

        val body =
            mockMvc
                .get("/api/stats/$code")
                .andExpect {
                    status { isOk() }
                    jsonPath("$.shortCode") { value(code) }
                    jsonPath("$.originalUrl") { value("https://example.com") }
                    jsonPath("$.shortUrl") { value("http://localhost:8080/$code") }
                    jsonPath("$.expiresAt") { value(null) }
                    jsonPath("$.totalClicks") { value(2) }
                    jsonPath("$.clicksByDay.length()") { value(30) }
                    jsonPath("$.clicksByDay[29].date") { value(today) }
                    jsonPath("$.clicksByDay[29].count") { value(2) }
                    jsonPath("$.topReferrers.length()") { value(2) }
                }.andReturn()
                .response.contentAsString

        val referrers: List<String> = JsonPath.read(body, "$.topReferrers[*].referrer")
        val counts: List<Int> = JsonPath.read(body, "$.topReferrers[*].count")
        assertEquals(mapOf("google.com" to 1, "direct" to 1), referrers.zip(counts).toMap())
    }

    @Test
    fun `GET stats returns 404 for an unknown code`() {
        mockMvc.get("/api/stats/zzzzzzz").andExpect {
            status { isNotFound() }
            jsonPath("$.error") { exists() }
        }
    }
}
