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
data class FeedHouseApiRow(
    @SerialName("id")
    val id: Long,
    @SerialName("house_number")
    val houseNumber: String? = null,
    @SerialName("number_of_pens")
    val numberOfPens: Long? = null
)

@Serializable
data class FeedPenApiRow(
    @SerialName("id")
    val id: Long,
    @SerialName("pen_name")
    val penName: String? = null
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
    @SerialName("inventory_id")
    val inventoryId: Int,
    @SerialName("house_id")
    val houseId: Long,
    @SerialName("pen_id")
    val penId: Long,
    @SerialName("feeder_number")
    val feederNumber: Int,
    @SerialName("kilograms")
    val kilograms: Int
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
)

class FeedsRefillBackendService(
    private val baseUrl: String = ApiConfig.BASE_URL
) {
    private val httpClient = HttpClient(Android)

    private val json = Json {
        ignoreUnknownKeys = true
    }

    suspend fun getHouses(): Result<List<FeedHouseOption>> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val responseText = httpClient.get("$baseUrl/api/mobile/feed-refill/houses") {
                    accept(ContentType.Application.Json)
                }.bodyAsText()

                val parsed: JsonElement = json.parseToJsonElement(responseText)

                if (parsed is JsonObject && parsed["message"] != null) {
                    val errorResponse = json.decodeFromJsonElement<LaravelErrorResponse>(parsed)
                    error(errorResponse.message ?: "Failed to load houses.")
                }

                json.decodeFromJsonElement<List<FeedHouseApiRow>>(parsed).map {
                    FeedHouseOption(
                        id = it.id,
                        houseNumber = it.houseNumber?.ifBlank { "Unknown" } ?: "Unknown"
                    )
                }
            }
        }
    }

    suspend fun getPensByHouse(houseId: Long): Result<List<FeedPenOption>> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val responseText = httpClient.get("$baseUrl/api/mobile/feed-refill/houses/$houseId/pens") {
                    accept(ContentType.Application.Json)
                }.bodyAsText()

                val parsed: JsonElement = json.parseToJsonElement(responseText)

                if (parsed is JsonObject && parsed["message"] != null) {
                    val errorResponse = json.decodeFromJsonElement<LaravelErrorResponse>(parsed)
                    error(errorResponse.message ?: "Failed to load pens.")
                }

                json.decodeFromJsonElement<List<FeedPenApiRow>>(parsed).map {
                    FeedPenOption(
                        id = it.id,
                        penName = it.penName?.ifBlank { "Unknown" } ?: "Unknown"
                    )
                }
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
        inventoryId: Int,
        houseId: Long,
        penId: Long,
        feederNumber: Int,
        kilograms: Int
    ): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val requestBody = FeedRefillRequest(
                    inventoryId = inventoryId,
                    houseId = houseId,
                    penId = penId,
                    feederNumber = feederNumber,
                    kilograms = kilograms
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
