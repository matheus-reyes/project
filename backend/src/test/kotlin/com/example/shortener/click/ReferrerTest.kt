package com.example.shortener.click

import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import kotlin.test.assertEquals

class ReferrerTest {
    @ParameterizedTest
    @CsvSource(
        "https://google.com/search?q=x, google.com",
        "http://news.ycombinator.com/item?id=1, news.ycombinator.com",
        "https://WWW.LinkedIn.com/feed, linkedin.com",
        "https://t.co, t.co",
    )
    fun `reduces a referer url to its host`(
        referer: String,
        expected: String,
    ) {
        assertEquals(expected, referrerOf(referer))
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "   ", "garbage", "not a url", "/relative/path", "https://"])
    fun `treats a blank or unparseable referer as direct`(referer: String) {
        assertEquals(DIRECT_REFERRER, referrerOf(referer))
    }

    @Test
    fun `treats a missing referer as direct`() {
        assertEquals(DIRECT_REFERRER, referrerOf(null))
    }
}
