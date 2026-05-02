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
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement

@Serializable
data class WeightHouseApiRow(
    @SerialName("id") val id: Long,
    @SerialName("house_number") val houseNumber: String? = null
)

@Serializable
data class WeightPenApiRow(
    @SerialName("id") val id: Long,
    @SerialName("pen_name") val penName: String? = null
)

@Serializable
data class WeightSubmitRequest(
    @SerialName("house_id") val houseId: Long,
    @SerialName("pen_id") val penId: Long,
    @SerialName("age") val age: Int,
    @SerialName("number_of_flocks") val numberOfFlocks: Int,
    @SerialName("flocks_with_cases") val flocksWithCases: Int,
    @SerialName("target_weight") val targetWeight: Double,
    @SerialName("weights") val weights: List<Double>
)

@Serializable
data class WeightSubmitResponse(
    @SerialName("success") val success: Boolean? = null,
    @SerialName("message") val message: String? = null,
    @SerialName("average_weight") val averageWeight: Double? = null,
    @SerialName("target") val target: Double? = null,
    @SerialName("status") val status: String? = null
)

data class WeightHouseOption(
    val id: Long,
    val houseNumber: String
)

data class WeightPenOption(
    val id: Long,
    val penName: String
)

class WeightBackendService(
    private val baseUrl: String = ApiConfig.BASE_URL
) {
    private val httpClient = HttpClient(Android)
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun getHouses(): Result<List<WeightHouseOption>> = withContext(Dispatchers.IO) {
        runCatching {
            val responseText = httpClient.get("$baseUrl/api/mobile/weight-sampling/houses") {
                accept(ContentType.Application.Json)
            }.bodyAsText()

            val parsed = json.parseToJsonElement(responseText)
            if (parsed is JsonObject && parsed["message"] != null) {
                val error = json.decodeFromJsonElement<LaravelErrorResponse>(parsed)
                error(error.message ?: "Failed to load houses.")
            }

            json.decodeFromJsonElement<List<WeightHouseApiRow>>(parsed).map {
                WeightHouseOption(
                    id = it.id,
                    houseNumber = it.houseNumber?.ifBlank { "Unknown" } ?: "Unknown"
                )
            }
        }
    }

    suspend fun getPensByHouse(houseId: Long): Result<List<WeightPenOption>> = withContext(Dispatchers.IO) {
        runCatching {
            val responseText = httpClient.get("$baseUrl/api/mobile/weight-sampling/houses/$houseId/pens") {
                accept(ContentType.Application.Json)
            }.bodyAsText()

            val parsed = json.parseToJsonElement(responseText)
            if (parsed is JsonObject && parsed["message"] != null) {
                val error = json.decodeFromJsonElement<LaravelErrorResponse>(parsed)
                error(error.message ?: "Failed to load pens.")
            }

            json.decodeFromJsonElement<List<WeightPenApiRow>>(parsed).map {
                WeightPenOption(
                    id = it.id,
                    penName = it.penName?.ifBlank { "Unknown" } ?: "Unknown"
                )
            }
        }
    }

    suspend fun submitWeightSampling(
        houseId: Long,
        penId: Long,
        age: Int,
        numberOfFlocks: Int,
        flocksWithCases: Int,
        targetWeight: Double,
        weights: List<Double>
    ): Result<WeightSubmitResponse> = withContext(Dispatchers.IO) {
        runCatching {
            val responseText = httpClient.post("$baseUrl/api/mobile/weight-sampling") {
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
                setBody(
                    json.encodeToString(
                        WeightSubmitRequest(
                            houseId = houseId,
                            penId = penId,
                            age = age,
                            numberOfFlocks = numberOfFlocks,
                            flocksWithCases = flocksWithCases,
                            targetWeight = targetWeight,
                            weights = weights
                        )
                    )
                )
            }.bodyAsText()

            val parsed = json.parseToJsonElement(responseText)
            if (parsed is JsonObject && parsed["success"] == null) {
                val error = json.decodeFromJsonElement<LaravelErrorResponse>(parsed)
                error(error.message ?: "Failed to submit weight sampling.")
            }

            json.decodeFromJsonElement<WeightSubmitResponse>(parsed)
        }
    }
}
