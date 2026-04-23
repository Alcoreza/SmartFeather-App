package com.example.smartfeather

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.request.header
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
data class VitaminHouseRpcRow(
    @SerialName("id")
    val id: Long,
    @SerialName("house_number")
    val houseNumber: String? = null,
    @SerialName("number_of_pens")
    val numberOfPens: Long? = null
)

@Serializable
data class VitaminPenRpcRow(
    @SerialName("id")
    val id: Long,
    @SerialName("pen_name")
    val penName: String? = null
)

@Serializable
data class VitaminInventoryRpcRow(
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
data class VitaminPensByHouseRequest(
    @SerialName("p_house_id")
    val houseId: Long
)

@Serializable
data class VitaminRefillRequest(
    @SerialName("p_inventory_id")
    val inventoryId: Int,
    @SerialName("p_house_id")
    val houseId: Long,
    @SerialName("p_pen_id")
    val penId: Long,
    @SerialName("p_bottles")
    val bottles: Int
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
    private val baseUrl: String = ApiConfig.BASE_URL,
    private val publishableKey: String = ApiConfig.BASE_URL
) {
    private val httpClient = HttpClient(Android)

    private val json = Json {
        ignoreUnknownKeys = true
    }

    suspend fun getHouses(): Result<List<VitaminHouseOption>> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val responseText =
                    httpClient.post("$baseUrl/rest/v1/rpc/mobile_get_houses") {
                        header("apikey", publishableKey)
                        header("Authorization", "Bearer $publishableKey")
                        contentType(ContentType.Application.Json)
                        setBody("{}")
                    }.bodyAsText()

                val parsed: JsonElement = json.parseToJsonElement(responseText)
                if (parsed is JsonObject && parsed["message"] != null) {
                    val errorResponse = json.decodeFromJsonElement<SupabaseErrorResponse>(parsed)
                    error(errorResponse.message ?: "Failed to load houses.")
                }

                json.decodeFromJsonElement<List<VitaminHouseRpcRow>>(parsed).map {
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
                val responseText =
                    httpClient.post("$baseUrl/rest/v1/rpc/mobile_get_pens_by_house") {
                        header("apikey", publishableKey)
                        header("Authorization", "Bearer $publishableKey")
                        contentType(ContentType.Application.Json)
                        setBody(json.encodeToString(VitaminPensByHouseRequest(houseId)))
                    }.bodyAsText()

                val parsed: JsonElement = json.parseToJsonElement(responseText)
                if (parsed is JsonObject && parsed["message"] != null) {
                    val errorResponse = json.decodeFromJsonElement<SupabaseErrorResponse>(parsed)
                    error(errorResponse.message ?: "Failed to load pens.")
                }

                json.decodeFromJsonElement<List<VitaminPenRpcRow>>(parsed).map {
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
                val responseText =
                    httpClient.post("$baseUrl/rest/v1/rpc/mobile_get_vitamin_inventory_options") {
                        header("apikey", publishableKey)
                        header("Authorization", "Bearer $publishableKey")
                        contentType(ContentType.Application.Json)
                        setBody("{}")
                    }.bodyAsText()

                val parsed: JsonElement = json.parseToJsonElement(responseText)
                if (parsed is JsonObject && parsed["message"] != null) {
                    val errorResponse = json.decodeFromJsonElement<SupabaseErrorResponse>(parsed)
                    error(errorResponse.message ?: "Failed to load vitamin inventory.")
                }

                json.decodeFromJsonElement<List<VitaminInventoryRpcRow>>(parsed).map {
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
                val responseText =
                    httpClient.post("$baseUrl/rest/v1/rpc/mobile_submit_vitamin_refill") {
                        header("apikey", publishableKey)
                        header("Authorization", "Bearer $publishableKey")
                        contentType(ContentType.Application.Json)
                        setBody(
                            json.encodeToString(
                                VitaminRefillRequest(
                                    inventoryId = inventoryId,
                                    houseId = houseId,
                                    penId = penId,
                                    bottles = bottles
                                )
                            )
                        )
                    }.bodyAsText()

                val parsed: JsonElement = json.parseToJsonElement(responseText)
                if (parsed is JsonObject && parsed["message"] != null) {
                    val errorResponse = json.decodeFromJsonElement<SupabaseErrorResponse>(parsed)
                    error(errorResponse.message ?: "Failed to submit vitamins refill.")
                }

                parsed.toString().contains("true")
            }
        }
    }
}
