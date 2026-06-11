package com.example.smartfeather

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.request.accept
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement

@Serializable
data class MobileDeviceTokenRequest(
    @SerialName("fcm_token")
    val fcmToken: String,
    @SerialName("platform")
    val platform: String = "android",
    @SerialName("device_name")
    val deviceName: String? = null
)

@Serializable
data class MobileDeviceTokenResponse(
    @SerialName("success")
    val success: Boolean? = null,
    @SerialName("message")
    val message: String? = null
)

class MobileDeviceTokenBackendService(
    private val baseUrl: String = ApiConfig.BASE_URL
) {
    private val httpClient = HttpClient(Android)

    private val json = Json {
        ignoreUnknownKeys = true
    }

    suspend fun saveDeviceToken(
        accessToken: String,
        fcmToken: String,
        deviceName: String?
    ): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val responseText = httpClient.post("$baseUrl/api/mobile/device-token") {
                    contentType(ContentType.Application.Json)
                    accept(ContentType.Application.Json)
                    header(HttpHeaders.Authorization, "Bearer $accessToken")
                    setBody(
                        json.encodeToString(
                            MobileDeviceTokenRequest(
                                fcmToken = fcmToken,
                                deviceName = deviceName
                            )
                        )
                    )
                }.bodyAsText()

                println("FCM TOKEN SAVE RESPONSE: $responseText")

                val parsed: JsonElement = json.parseToJsonElement(responseText)

                if (parsed is JsonObject && parsed["success"] == null) {
                    val errorResponse = json.decodeFromJsonElement<LaravelErrorResponse>(parsed)
                    error(errorResponse.message ?: "Failed to save notification token.")
                }

                val response = json.decodeFromJsonElement<MobileDeviceTokenResponse>(parsed)
                response.success == true
            }
        }
    }
}