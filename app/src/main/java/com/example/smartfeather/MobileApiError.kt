package com.example.smartfeather

import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText

class MobileSessionExpiredException(
    message: String = "Session expired. Please log in again."
) : Exception(message)

suspend fun HttpResponse.mobileBodyAsText(): String {
    val responseText = bodyAsText()

    if (
        status.value == 401 &&
        (
                responseText.contains("Invalid or expired mobile token", ignoreCase = true) ||
                        responseText.contains("Missing mobile authorization token", ignoreCase = true)
                )
    ) {
        throw MobileSessionExpiredException()
    }

    return responseText
}

fun Throwable.isMobileSessionExpired(): Boolean {
    return this is MobileSessionExpiredException ||
            message?.contains("Session expired", ignoreCase = true) == true ||
            message?.contains("Invalid or expired mobile token", ignoreCase = true) == true ||
            message?.contains("Missing mobile authorization token", ignoreCase = true) == true
}