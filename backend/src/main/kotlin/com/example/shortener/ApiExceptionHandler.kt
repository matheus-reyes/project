package com.example.shortener

import com.example.shortener.shortlink.InvalidShortLinkRequestException
import com.example.shortener.shortlink.ShortLinkExpiredException
import com.example.shortener.shortlink.ShortLinkNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

data class ErrorResponse(
    val error: String,
)

@RestControllerAdvice
class ApiExceptionHandler {
    @ExceptionHandler(InvalidShortLinkRequestException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun invalidRequest(e: InvalidShortLinkRequestException) = ErrorResponse(e.message.orEmpty())

    @ExceptionHandler(HttpMessageNotReadableException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun unreadableBody() = ErrorResponse("request body is malformed")

    @ExceptionHandler(ShortLinkNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun notFound(e: ShortLinkNotFoundException) = ErrorResponse(e.message.orEmpty())

    @ExceptionHandler(ShortLinkExpiredException::class)
    @ResponseStatus(HttpStatus.GONE)
    fun expired(e: ShortLinkExpiredException) = ErrorResponse(e.message.orEmpty())
}
