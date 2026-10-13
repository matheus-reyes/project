package com.example.shortener.shortlink

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ShortCodeGeneratorTest {
    private val generator = SecureRandomShortCodeGenerator()

    @Test
    fun `generates 7 alphanumeric characters`() {
        val codes = List(1_000) { generator.generate() }

        assertTrue(codes.all { it.matches(Regex("^[A-Za-z0-9]{7}$")) })
    }

    @Test
    fun `generates distinct codes`() {
        val codes = List(1_000) { generator.generate() }

        assertEquals(codes.size, codes.toSet().size)
    }
}
