package com.example.smartfeather

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
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
data class FeedPenApiRow(
    @SerialName("id")
    val id: Long,
    @SerialName("pen_name")
    val penName: String? = null
)

@Serializable
data class FeedContextResponse(
    @SerialName("success")
    val success: Boolean? = null,
    @SerialName("access_allowed")
    val accessAllowed: Boolean? = null,
    @SerialName("house_id")
    val houseId: Long? = null,
    @SerialName("house_number")
    val houseNumber: String? = null,
    @SerialName("pen_options")
    val penOptions: List<FeedPenApiRow> = emptyList(),
    @SerialName("message")
    val message: String? = null
)

@Serializable
data class FeedInventoryApiRow(
    @SerialName("id")
    val id: Int,
    @SerialName("item_name")
    val itemName: String? = null,
    @SerialName("remaining_stock")
    val remainingStock: Int? = null,
    @SerialName("unit")
    val unit: String? = null
)

@Serializable
data class FeedRefillRequest(
    @SerialName("employee_id")
    val employeeId: Int,
    @SerialName("inventory_id")
    val inventoryId: Int,
    @SerialName("house_id")
    val houseId: Long,
    @SerialName("pen_id")
    val penId: Long,
    @SerialName("feeder_number")
    val feederNumber: Int,
    @SerialName("kilograms")
    val kilograms: Int,
    @SerialName("recorded_at")
    val recordedAt: String
)

@Serializable
data class FeedApiMessageResponse(
    @SerialName("success")
    val success: Boolean? = null,
    @SerialName("message")
    val message: String? = null
)

data class FeedHouseOption(
    val id: Long,
    val houseNumber: String
)

data class FeedPenOption(
    val id: Long,
    val penName: String
)

data class FeedInventoryOption(
    val id: Int,
    val itemName: String,
    val remainingStock: Int,
    val unit: String
) {
    val selectionKey: String
        get() = "$id|$itemName"
}

data class FeedAccessContext(
    val accessAllowed: Boolean,
    val house: FeedHouseOption?,
    val pens: List<FeedPenOption>,
    val message: String?
)

class FeedsRefillBackendService(
    private val baseUrl: String = ApiConfig.BASE_URL
) {
    private val httpClient = HttpClient(Android)

    private val json = Json {
        ignoreUnknownKeys = true
    }

    suspend fun getFeedsContext(employeeId: Int): Result<FeedAccessContext> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val responseText = httpClient.get("$baseUrl/api/mobile/feed-refill/context?employee_id=$employeeId") {
                    accept(ContentType.Application.Json)
                }.bodyAsText()

                val parsed: JsonElement = json.parseToJsonElement(responseText)

                if (parsed is JsonObject && parsed["success"] == null && parsed["access_allowed"] == null) {
                    val errorResponse = json.decodeFromJsonElement<LaravelErrorResponse>(parsed)
                    error(errorResponse.message ?: "Failed to load feeds refill context.")
                }

                val response = json.decodeFromJsonElement<FeedContextResponse>(parsed)

                FeedAccessContext(
                    accessAllowed = response.accessAllowed == true,
                    house = if (response.houseId != null && !response.houseNumber.isNullOrBlank()) {
                        FeedHouseOption(
                            id = response.houseId,
                            houseNumber = response.houseNumber
                        )
                    } else {
                        null
                    },
                    pens = response.penOptions.map {
                        FeedPenOption(
                            id = it.id,
                            penName = it.penName?.ifBlank { "Unknown" } ?: "Unknown"
                        )
                    },
                    message = response.message
                )
            }
        }
    }

    suspend fun getFeedInventoryOptions(): Result<List<FeedInventoryOption>> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val responseText = httpClient.get("$baseUrl/api/mobile/feed-refill/feed-options") {
                    accept(ContentType.Application.Json)
                }.bodyAsText()

                val parsed: JsonElement = json.parseToJsonElement(responseText)

                if (parsed is JsonObject && parsed["message"] != null) {
                    val errorResponse = json.decodeFromJsonElement<LaravelErrorResponse>(parsed)
                    error(errorResponse.message ?: "Failed to load feed inventory.")
                }

                json.decodeFromJsonElement<List<FeedInventoryApiRow>>(parsed).map {
                    FeedInventoryOption(
                        id = it.id,
                        itemName = it.itemName?.ifBlank { "Unknown Feed" } ?: "Unknown Feed",
                        remainingStock = it.remainingStock ?: 0,
                        unit = it.unit?.ifBlank { "kg" } ?: "kg"
                    )
                }
            }
        }
    }

    suspend fun submitFeedRefill(
        employeeId: Int,
        inventoryId: Int,
        houseId: Long,
        penId: Long,
        feederNumber: Int,
        kilograms: Int,
        recordedAt: String
    ): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val requestBody = FeedRefillRequest(
                    employeeId = employeeId,
                    inventoryId = inventoryId,
                    houseId = houseId,
                    penId = penId,
                    feederNumber = feederNumber,
                    kilograms = kilograms,
                    recordedAt = recordedAt
                )

                val responseText = httpClient.post("$baseUrl/api/mobile/feed-refill") {
                    contentType(ContentType.Application.Json)
                    accept(ContentType.Application.Json)
                    setBody(json.encodeToString(requestBody))
                }.bodyAsText()

                val parsed: JsonElement = json.parseToJsonElement(responseText)

                if (parsed is JsonObject && parsed["success"] == null) {
                    val errorResponse = json.decodeFromJsonElement<LaravelErrorResponse>(parsed)
                    error(errorResponse.message ?: "Failed to submit feeds refill.")
                }

                val response = json.decodeFromJsonElement<FeedApiMessageResponse>(parsed)
                response.success == true
            }
        }
    }

    fun feederOptions(): List<String> = listOf("1", "2", "3")
}