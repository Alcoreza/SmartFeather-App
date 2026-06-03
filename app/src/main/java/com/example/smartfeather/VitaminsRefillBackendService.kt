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
data class VitaminPenApiRow(
    @SerialName("id")
    val id: Long,
    @SerialName("pen_name")
    val penName: String? = null
)

@Serializable
data class VitaminContextResponse(
    @SerialName("success")
    val success: Boolean? = null,
    @SerialName("access_allowed")
    val accessAllowed: Boolean? = null,
    @SerialName("house_id")
    val houseId: Long? = null,
    @SerialName("house_number")
    val houseNumber: String? = null,
    @SerialName("pen_options")
    val penOptions: List<VitaminPenApiRow> = emptyList(),
    @SerialName("message")
    val message: String? = null
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
    @SerialName("employee_id")
    val employeeId: Int,
    @SerialName("inventory_id")
    val inventoryId: Int,
    @SerialName("house_id")
    val houseId: Long,
    @SerialName("pen_id")
    val penId: Long,
    @SerialName("bottles")
    val bottles: Int,
    @SerialName("recorded_at")
    val recordedAt: String
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
) {
    val selectionKey: String
        get() = "$id|$itemName"
}

data class VitaminAccessContext(
    val accessAllowed: Boolean,
    val house: VitaminHouseOption?,
    val pens: List<VitaminPenOption>,
    val message: String?
)

class VitaminsRefillBackendService(
    private val baseUrl: String = ApiConfig.BASE_URL
) {
    private val httpClient = HttpClient(Android)

    private val json = Json {
        ignoreUnknownKeys = true
    }

    suspend fun getVitaminsContext(employeeId: Int): Result<VitaminAccessContext> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val responseText = httpClient.get("$baseUrl/api/mobile/vitamin-refill/context?employee_id=$employeeId") {
                    accept(ContentType.Application.Json)
                }.bodyAsText()

                val parsed: JsonElement = json.parseToJsonElement(responseText)

                if (parsed is JsonObject && parsed["success"] == null && parsed["access_allowed"] == null) {
                    val errorResponse = json.decodeFromJsonElement<LaravelErrorResponse>(parsed)
                    error(errorResponse.message ?: "Failed to load vitamins refill context.")
                }

                val response = json.decodeFromJsonElement<VitaminContextResponse>(parsed)

                VitaminAccessContext(
                    accessAllowed = response.accessAllowed == true,
                    house = if (response.houseId != null && !response.houseNumber.isNullOrBlank()) {
                        VitaminHouseOption(
                            id = response.houseId,
                            houseNumber = response.houseNumber
                        )
                    } else {
                        null
                    },
                    pens = response.penOptions.map {
                        VitaminPenOption(
                            id = it.id,
                            penName = it.penName?.ifBlank { "Unknown" } ?: "Unknown"
                        )
                    },
                    message = response.message
                )
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
        employeeId: Int,
        inventoryId: Int,
        houseId: Long,
        penId: Long,
        bottles: Int,
        recordedAt: String
    ): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val requestBody = VitaminRefillRequest(
                    employeeId = employeeId,
                    inventoryId = inventoryId,
                    houseId = houseId,
                    penId = penId,
                    bottles = bottles,
                    recordedAt = recordedAt
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