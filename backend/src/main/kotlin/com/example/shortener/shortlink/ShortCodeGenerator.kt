package com.example.shortener.shortlink

import org.springframework.stereotype.Component
import java.security.SecureRandom

fun interface ShortCodeGenerator {
    fun generate(): String
}

@Component
class SecureRandomShortCodeGenerator : ShortCodeGenerator {
    private val random = SecureRandom()

    override fun generate(): String = String(CharArray(LENGTH) { ALPHABET[random.nextInt(ALPHABET.length)] })

    private companion object {
        const val LENGTH = 7
        const val ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
    }
}
