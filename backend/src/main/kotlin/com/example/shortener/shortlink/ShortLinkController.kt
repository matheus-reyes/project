package com.example.shortener.shortlink

import com.example.shortener.AppProperties
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.net.URI
import java.time.Instant

data class ShortenRequest(
    val url: String? = null,
    val expiresAt: Instant? = null,
)

data class ShortLinkResponse(
    val shortCode: String,
    val shortUrl: String,
    val originalUrl: String,
    val createdAt: Instant,
    val expiresAt: Instant?,
)

@RestController
class ShortLinkController(
    private val service: ShortLinkService,
    private val properties: AppProperties,
) {
    @PostMapping("/api/shorten")
    @ResponseStatus(HttpStatus.CREATED)
    fun shorten(
        @RequestBody request: ShortenRequest,
    ): ShortLinkResponse = service.create(request.url, request.expiresAt).toResponse()

    @GetMapping("/{shortCode:[A-Za-z0-9]+}")
    fun redirect(
        @PathVariable shortCode: String,
    ): ResponseEntity<Void> {
        val link = service.resolve(shortCode)
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(link.originalUrl)).build()
    }

    private fun ShortLink.toResponse() =
        ShortLinkResponse(
            shortCode = shortCode,
            shortUrl = "${properties.baseUrl.trimEnd('/')}/$shortCode",
            originalUrl = originalUrl,
            createdAt = createdAt,
            expiresAt = expiresAt,
        )
}
