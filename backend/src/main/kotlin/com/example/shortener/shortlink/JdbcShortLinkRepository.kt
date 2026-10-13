package com.example.shortener.shortlink

import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime
import java.time.ZoneOffset

@Repository
class JdbcShortLinkRepository(
    private val jdbc: JdbcClient,
) : ShortLinkRepository {
    // ON CONFLICT keeps the check and the insert atomic, so two requests never share a code
    override fun saveIfAbsent(link: ShortLink): Boolean =
        jdbc
            .sql(
                """
                INSERT INTO short_links (short_code, original_url, created_at, expires_at)
                VALUES (:shortCode, :originalUrl, :createdAt, :expiresAt)
                ON CONFLICT (short_code) DO NOTHING
                """,
            ).param("shortCode", link.shortCode)
            .param("originalUrl", link.originalUrl)
            .param("createdAt", link.createdAt.atOffset(ZoneOffset.UTC))
            .param("expiresAt", link.expiresAt?.atOffset(ZoneOffset.UTC))
            .update() == 1

    override fun findByCode(shortCode: String): ShortLink? =
        jdbc
            .sql("SELECT * FROM short_links WHERE short_code = :shortCode")
            .param("shortCode", shortCode)
            .query(ROW_MAPPER)
            .optional()
            .orElse(null)

    override fun findAll(): List<ShortLink> =
        jdbc
            .sql("SELECT * FROM short_links")
            .query(ROW_MAPPER)
            .list()

    private companion object {
        val ROW_MAPPER =
            RowMapper { rs, _ ->
                ShortLink(
                    shortCode = rs.getString("short_code"),
                    originalUrl = rs.getString("original_url"),
                    createdAt = rs.getObject("created_at", OffsetDateTime::class.java).toInstant(),
                    expiresAt = rs.getObject("expires_at", OffsetDateTime::class.java)?.toInstant(),
                )
            }
    }
}
