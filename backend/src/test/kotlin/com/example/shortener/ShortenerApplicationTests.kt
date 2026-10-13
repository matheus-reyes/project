package com.example.shortener

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import

@SpringBootTest
@Import(PostgresTestConfiguration::class)
class ShortenerApplicationTests {
    @Test
    fun `context loads`() {
    }
}
