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
data class FeedHouseRpcRow(
    @SerialName("id")
    val id: Long,
    @SerialName("house_number")
    val houseNumber: String? = null,
    @SerialName("number_of_pens")
    val numberOfPens: Long? = null
)

@Serializable
data class FeedPenRpcRow(
    @SerialName("id")
    val id: Long,
    @SerialName("pen_name")
    val penName: String? = null
)

@Serializable
data class FeedInventoryRpcRow(
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
data class FeedPensByHouseRequest(
    @SerialName("p_house_id")
    val houseId: Long
)

@Serializable
data class FeedRefillRequest(
    @SerialName("p_inventory_id")
    val inventoryId: Int,
    @SerialName("p_house_id")
    val houseId: Long,
    @SerialName("p_pen_id")
    val penId: Long,
    @SerialName("p_feeder_number")
    val feederNumber: Int,
    @SerialName("p_kilograms")
    val kilograms: Int
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
    private val baseUrl: String = SupabaseConfig.SUPABASE_URL,
    private val publishableKey: String = SupabaseConfig.SUPABASE_PUBLISHABLE_KEY
) {
    private val httpClient = HttpClient(Android)

    private val json = Json {
        ignoreUnknownKeys = true
    }

    suspend fun getHouses(): Result<List<FeedHouseOption>> {
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

                json.decodeFromJsonElement<List<FeedHouseRpcRow>>(parsed).map {
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
                val responseText =
                    httpClient.post("$baseUrl/rest/v1/rpc/mobile_get_pens_by_house") {
                        header("apikey", publishableKey)
                        header("Authorization", "Bearer $publishableKey")
                        contentType(ContentType.Application.Json)
                        setBody(json.encodeToString(FeedPensByHouseRequest(houseId)))
                    }.bodyAsText()

                val parsed: JsonElement = json.parseToJsonElement(responseText)
                if (parsed is JsonObject && parsed["message"] != null) {
                    val errorResponse = json.decodeFromJsonElement<SupabaseErrorResponse>(parsed)
                    error(errorResponse.message ?: "Failed to load pens.")
                }

                json.decodeFromJsonElement<List<FeedPenRpcRow>>(parsed).map {
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
                val responseText =
                    httpClient.post("$baseUrl/rest/v1/rpc/mobile_get_feed_inventory_options") {
                        header("apikey", publishableKey)
                        header("Authorization", "Bearer $publishableKey")
                        contentType(ContentType.Application.Json)
                        setBody("{}")
                    }.bodyAsText()

                val parsed: JsonElement = json.parseToJsonElement(responseText)
                if (parsed is JsonObject && parsed["message"] != null) {
                    val errorResponse = json.decodeFromJsonElement<SupabaseErrorResponse>(parsed)
                    error(errorResponse.message ?: "Failed to load feed inventory.")
                }

                json.decodeFromJsonElement<List<FeedInventoryRpcRow>>(parsed).map {
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
                val responseText =
                    httpClient.post("$baseUrl/rest/v1/rpc/mobile_submit_feed_refill") {
                        header("apikey", publishableKey)
                        header("Authorization", "Bearer $publishableKey")
                        contentType(ContentType.Application.Json)
                        setBody(
                            json.encodeToString(
                                FeedRefillRequest(
                                    inventoryId = inventoryId,
                                    houseId = houseId,
                                    penId = penId,
                                    feederNumber = feederNumber,
                                    kilograms = kilograms
                                )
                            )
                        )
                    }.bodyAsText()

                val parsed: JsonElement = json.parseToJsonElement(responseText)
                if (parsed is JsonObject && parsed["message"] != null) {
                    val errorResponse = json.decodeFromJsonElement<SupabaseErrorResponse>(parsed)
                    error(errorResponse.message ?: "Failed to submit feeds refill.")
                }

                parsed.toString().contains("true")
            }
        }
    }

    fun feederOptions(): List<String> = listOf("1", "2", "3")
}
