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
data class VitaminHouseApiRow(
    @SerialName("id")
    val id: Long,
    @SerialName("house_number")
    val houseNumber: String? = null,
    @SerialName("number_of_pens")
    val numberOfPens: Long? = null
)

@Serializable
data class VitaminPenApiRow(
    @SerialName("id")
    val id: Long,
    @SerialName("pen_name")
    val penName: String? = null
)

@Serializable
data class VitaminInventoryApiRow(
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
data class VitaminRefillRequest(
    @SerialName("inventory_id")
    val inventoryId: Int,
    @SerialName("house_id")
    val houseId: Long,
    @SerialName("pen_id")
    val penId: Long,
    @SerialName("bottles")
    val bottles: Int
)

@Serializable
data class VitaminApiMessageResponse(
    @SerialName("success")
    val success: Boolean? = null,
    @SerialName("message")
    val message: String? = null
)

data class VitaminHouseOption(
    val id: Long,
    val houseNumber: String
)

data class VitaminPenOption(
    val id: Long,
    val penName: String
)

data class VitaminInventoryOption(
    val id: Int,
    val itemName: String,
    val remainingStock: Int,
    val unit: String
)

class VitaminsRefillBackendService(
    private val baseUrl: String = ApiConfig.BASE_URL
) {
    private val httpClient = HttpClient(Android)

    private val json = Json {
        ignoreUnknownKeys = true
    }

    suspend fun getHouses(): Result<List<VitaminHouseOption>> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val responseText = httpClient.get("$baseUrl/api/mobile/vitamin-refill/houses") {
                    accept(ContentType.Application.Json)
                }.bodyAsText()

                val parsed: JsonElement = json.parseToJsonElement(responseText)

                if (parsed is JsonObject && parsed["message"] != null) {
                    val errorResponse = json.decodeFromJsonElement<LaravelErrorResponse>(parsed)
                    error(errorResponse.message ?: "Failed to load houses.")
                }

                json.decodeFromJsonElement<List<VitaminHouseApiRow>>(parsed).map {
                    VitaminHouseOption(
                        id = it.id,
                        houseNumber = it.houseNumber?.ifBlank { "Unknown" } ?: "Unknown"
                    )
                }
            }
        }
    }

    suspend fun getPensByHouse(houseId: Long): Result<List<VitaminPenOption>> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val responseText = httpClient.get("$baseUrl/api/mobile/vitamin-refill/houses/$houseId/pens") {
                    accept(ContentType.Application.Json)
                }.bodyAsText()

                val parsed: JsonElement = json.parseToJsonElement(responseText)

                if (parsed is JsonObject && parsed["message"] != null) {
                    val errorResponse = json.decodeFromJsonElement<LaravelErrorResponse>(parsed)
                    error(errorResponse.message ?: "Failed to load pens.")
                }

                json.decodeFromJsonElement<List<VitaminPenApiRow>>(parsed).map {
                    VitaminPenOption(
                        id = it.id,
                        penName = it.penName?.ifBlank { "Unknown" } ?: "Unknown"
                    )
                }
            }
        }
    }

    suspend fun getVitaminInventoryOptions(): Result<List<VitaminInventoryOption>> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val responseText = httpClient.get("$baseUrl/api/mobile/vitamin-refill/options") {
                    accept(ContentType.Application.Json)
                }.bodyAsText()

                val parsed: JsonElement = json.parseToJsonElement(responseText)

                if (parsed is JsonObject && parsed["message"] != null) {
                    val errorResponse = json.decodeFromJsonElement<LaravelErrorResponse>(parsed)
                    error(errorResponse.message ?: "Failed to load vitamin inventory.")
                }

                json.decodeFromJsonElement<List<VitaminInventoryApiRow>>(parsed).map {
                    VitaminInventoryOption(
                        id = it.id,
                        itemName = it.itemName?.ifBlank { "Unknown Vitamin" } ?: "Unknown Vitamin",
                        remainingStock = it.remainingStock ?: 0,
                        unit = it.unit?.ifBlank { "bottle" } ?: "bottle"
                    )
                }
            }
        }
    }

    suspend fun submitVitaminRefill(
        inventoryId: Int,
        houseId: Long,
        penId: Long,
        bottles: Int
    ): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val requestBody = VitaminRefillRequest(
                    inventoryId = inventoryId,
                    houseId = houseId,
                    penId = penId,
                    bottles = bottles
                )

                val responseText = httpClient.post("$baseUrl/api/mobile/vitamin-refill") {
                    contentType(ContentType.Application.Json)
                    accept(ContentType.Application.Json)
                    setBody(json.encodeToString(requestBody))
                }.bodyAsText()

                val parsed: JsonElement = json.parseToJsonElement(responseText)

                if (parsed is JsonObject && parsed["success"] == null) {
                    val errorResponse = json.decodeFromJsonElement<LaravelErrorResponse>(parsed)
                    error(errorResponse.message ?: "Failed to submit vitamins refill.")
                }

                val response = json.decodeFromJsonElement<VitaminApiMessageResponse>(parsed)
                response.success == true
            }
        }
    }
}
