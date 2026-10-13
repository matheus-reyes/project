package com.example.shortener

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("app")
data class AppProperties(
    val baseUrl: String = "http://localhost:8080",
)
