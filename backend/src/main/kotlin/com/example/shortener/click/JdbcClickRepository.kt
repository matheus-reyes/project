package com.example.shortener.click

import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime
import java.time.ZoneOffset

@Repository
class JdbcClickRepository(
    private val jdbc: JdbcClient,
) : ClickRepository {
    override fun save(click: Click) {
        jdbc
            .sql(
                """
                INSERT INTO clicks (short_code, clicked_at, referrer, user_agent, ip)
                VALUES (:shortCode, :clickedAt, :referrer, :userAgent, :ip)
                """,
            ).param("shortCode", click.shortCode)
            .param("clickedAt", click.timestamp.atOffset(ZoneOffset.UTC))
            .param("referrer", click.referrer)
            .param("userAgent", click.userAgent)
            .param("ip", click.ip)
            .update()
    }

    override fun findByShortCode(shortCode: String): List<Click> =
        jdbc
            .sql("SELECT * FROM clicks WHERE short_code = :shortCode ORDER BY clicked_at")
            .param("shortCode", shortCode)
            .query(ROW_MAPPER)
            .list()

    override fun countByShortCode(shortCode: String): Int =
        jdbc
            .sql("SELECT count(*) FROM clicks WHERE short_code = :shortCode")
            .param("shortCode", shortCode)
            .query(Int::class.java)
            .single()

    private companion object {
        val ROW_MAPPER =
            RowMapper { rs, _ ->
                Click(
                    shortCode = rs.getString("short_code"),
                    timestamp = rs.getObject("clicked_at", OffsetDateTime::class.java).toInstant(),
                    referrer = rs.getString("referrer"),
                    userAgent = rs.getString("user_agent"),
                    ip = rs.getString("ip"),
                )
            }
    }
}
