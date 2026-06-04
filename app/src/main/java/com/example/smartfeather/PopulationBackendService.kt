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
data class PopulationPenApiRow(
    @SerialName("id")
    val id: Long,
    @SerialName("pen_name")
    val penName: String? = null,
    @SerialName("pen_number")
    val penNumber: String? = null
)

@Serializable
data class PopulationContextResponse(
    @SerialName("success")
    val success: Boolean? = null,
    @SerialName("access_allowed")
    val accessAllowed: Boolean? = null,
    @SerialName("house_id")
    val houseId: Long? = null,
    @SerialName("house_number")
    val houseNumber: String? = null,
    @SerialName("pen_options")
    val penOptions: List<PopulationPenApiRow> = emptyList(),
    @SerialName("message")
    val message: String? = null
)

@Serializable
data class PopulationSubmitRequest(
    @SerialName("employee_id")
    val employeeId: Int,
    @SerialName("task_id")
    val taskId: Int? = null,
    @SerialName("house_id")
    val houseId: Long,
    @SerialName("pen_id")
    val penId: Long? = null,
    @SerialName("pen_name")
    val penName: String? = null,
    @SerialName("eggs_hatched")
    val eggsHatched: Int,
    @SerialName("mortality")
    val mortality: Int,
    @SerialName("recorded_at")
    val recordedAt: String
)

@Serializable
data class PopulationApiMessageResponse(
    @SerialName("success")
    val success: Boolean? = null,
    @SerialName("message")
    val message: String? = null
)

data class PopulationHouseOption(
    val id: Long,
    val houseNumber: String
)

data class PopulationAccessContext(
    val accessAllowed: Boolean,
    val house: PopulationHouseOption?,
    val penOptions: List<String>,
    val message: String?
)

class PopulationBackendService(
    private val baseUrl: String = ApiConfig.BASE_URL
) {
    private val httpClient = HttpClient(Android)

    private val json = Json {
        ignoreUnknownKeys = true
    }

    suspend fun getPopulationContext(employeeId: Int): Result<PopulationAccessContext> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val responseText = httpClient.get("$baseUrl/api/mobile/population/context?employee_id=$employeeId") {
                    accept(ContentType.Application.Json)
                }.bodyAsText()

                val parsed: JsonElement = json.parseToJsonElement(responseText)

                if (parsed is JsonObject && parsed["success"] == null && parsed["access_allowed"] == null) {
                    val errorResponse = json.decodeFromJsonElement<LaravelErrorResponse>(parsed)
                    error(errorResponse.message ?: "Failed to load population context.")
                }

                val response = json.decodeFromJsonElement<PopulationContextResponse>(parsed)

                PopulationAccessContext(
                    accessAllowed = response.accessAllowed == true,
                    house = if (response.houseId != null && !response.houseNumber.isNullOrBlank()) {
                        PopulationHouseOption(
                            id = response.houseId,
                            houseNumber = response.houseNumber
                        )
                    } else {
                        null
                    },
                    penOptions = response.penOptions.mapNotNull {
                        it.penNumber?.ifBlank { null }
                    },
                    message = response.message
                )
            }
        }
    }

    suspend fun submitPopulation(
        employeeId: Int,
        houseId: Long,
        penNumber: String? = null,
        eggsHatched: Int,
        mortality: Int,
        recordedAt: String,
        taskId: Int? = null,
        penId: Long? = null
    ): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val requestBody = PopulationSubmitRequest(
                    employeeId = employeeId,
                    taskId = taskId,
                    houseId = houseId,
                    penId = penId,
                    penName = penNumber?.let { "Pen $it" },
                    eggsHatched = eggsHatched,
                    mortality = mortality,
                    recordedAt = recordedAt
                )

                val responseText = httpClient.post("$baseUrl/api/mobile/population") {
                    contentType(ContentType.Application.Json)
                    accept(ContentType.Application.Json)
                    setBody(json.encodeToString(requestBody))
                }.bodyAsText()

                val parsed: JsonElement = json.parseToJsonElement(responseText)

                if (parsed is JsonObject && parsed["success"] == null) {
                    val errorResponse = json.decodeFromJsonElement<LaravelErrorResponse>(parsed)
                    error(errorResponse.message ?: "Failed to submit population data.")
                }

                val result = json.decodeFromJsonElement<PopulationApiMessageResponse>(parsed)
                result.success == true
            }
        }
    }
}