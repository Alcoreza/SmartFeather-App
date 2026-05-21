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
data class WeightPenApiRow(
    @SerialName("id") val id: Long,
    @SerialName("pen_name") val penName: String? = null,
    @SerialName("current_batch_id") val currentBatchId: Int? = null,
    @SerialName("current_batch_code") val currentBatchCode: String? = null,
    @SerialName("current_batch_started_at") val currentBatchStartedAt: String? = null
)

@Serializable
data class WeightContextResponse(
    @SerialName("success") val success: Boolean? = null,
    @SerialName("access_allowed") val accessAllowed: Boolean? = null,
    @SerialName("house_id") val houseId: Long? = null,
    @SerialName("house_number") val houseNumber: String? = null,
    @SerialName("pen_options") val penOptions: List<WeightPenApiRow> = emptyList(),
    @SerialName("message") val message: String? = null
)

@Serializable
data class WeightSubmitRequest(
    @SerialName("employee_id") val employeeId: Int,
    @SerialName("house_id") val houseId: Long,
    @SerialName("pen_id") val penId: Long,
    @SerialName("number_of_flocks") val numberOfFlocks: Int,
    @SerialName("flocks_with_cases") val flocksWithCases: Int,
    @SerialName("target_weight") val targetWeight: Double,
    @SerialName("weights") val weights: List<Double>,
    @SerialName("recorded_date") val recordedDate: String,
    @SerialName("recorded_time") val recordedTime: String
)

@Serializable
data class WeightSubmitResponse(
    @SerialName("success") val success: Boolean? = null,
    @SerialName("message") val message: String? = null,
    @SerialName("average_weight") val averageWeight: Double? = null,
    @SerialName("target") val target: Double? = null,
    @SerialName("status") val status: String? = null,
    @SerialName("age_days") val ageDays: Int? = null,
    @SerialName("batch_code") val batchCode: String? = null
)

data class WeightHouseOption(
    val id: Long,
    val houseNumber: String
)

data class WeightPenOption(
    val id: Long,
    val penName: String,
    val currentBatchId: Int? = null,
    val currentBatchCode: String? = null,
    val currentBatchStartedAt: String? = null
)

data class WeightAccessContext(
    val accessAllowed: Boolean,
    val house: WeightHouseOption?,
    val pens: List<WeightPenOption>,
    val message: String?
)

class WeightBackendService(
    private val baseUrl: String = ApiConfig.BASE_URL
) {
    private val httpClient = HttpClient(Android)
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun getWeightContext(employeeId: Int): Result<WeightAccessContext> = withContext(Dispatchers.IO) {
        runCatching {
            val responseText = httpClient.get("$baseUrl/api/mobile/weight-sampling/context?employee_id=$employeeId") {
                accept(ContentType.Application.Json)
            }.bodyAsText()

            val parsed: JsonElement = json.parseToJsonElement(responseText)

            if (parsed is JsonObject && parsed["success"] == null && parsed["access_allowed"] == null) {
                val error = json.decodeFromJsonElement<LaravelErrorResponse>(parsed)
                error(error.message ?: "Failed to load weight sampling context.")
            }

            val response = json.decodeFromJsonElement<WeightContextResponse>(parsed)

            WeightAccessContext(
                accessAllowed = response.accessAllowed == true,
                house = if (response.houseId != null && !response.houseNumber.isNullOrBlank()) {
                    WeightHouseOption(
                        id = response.houseId,
                        houseNumber = response.houseNumber
                    )
                } else {
                    null
                },
                pens = response.penOptions.map {
                    WeightPenOption(
                        id = it.id,
                        penName = it.penName?.ifBlank { "Unknown" } ?: "Unknown",
                        currentBatchId = it.currentBatchId,
                        currentBatchCode = it.currentBatchCode,
                        currentBatchStartedAt = it.currentBatchStartedAt
                    )
                },
                message = response.message
            )
        }
    }

    suspend fun submitWeightSampling(
        employeeId: Int,
        houseId: Long,
        penId: Long,
        numberOfFlocks: Int,
        flocksWithCases: Int,
        targetWeight: Double,
        weights: List<Double>,
        recordedDate: String,
        recordedTime: String
    ): Result<WeightSubmitResponse> = withContext(Dispatchers.IO) {
        runCatching {
            val responseText = httpClient.post("$baseUrl/api/mobile/weight-sampling") {
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
                setBody(
                    json.encodeToString(
                        WeightSubmitRequest(
                            employeeId = employeeId,
                            houseId = houseId,
                            penId = penId,
                            numberOfFlocks = numberOfFlocks,
                            flocksWithCases = flocksWithCases,
                            targetWeight = targetWeight,
                            weights = weights,
                            recordedDate = recordedDate,
                            recordedTime = recordedTime
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