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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Serializable
data class PenCleaningPenApiRow(
    @SerialName("id")
    val id: Long,
    @SerialName("pen_name")
    val penName: String? = null
)

@Serializable
data class PenCleaningContextResponse(
    @SerialName("success")
    val success: Boolean? = null,
    @SerialName("access_allowed")
    val accessAllowed: Boolean? = null,
    @SerialName("house_id")
    val houseId: Long? = null,
    @SerialName("house_number")
    val houseNumber: String? = null,
    @SerialName("pen_options")
    val penOptions: List<PenCleaningPenApiRow> = emptyList(),
    @SerialName("message")
    val message: String? = null
)

@Serializable
data class PenCleaningSubmitRequest(
    @SerialName("employee_id")
    val employeeId: Int,
    @SerialName("task_id")
    val taskId: Int? = null,
    @SerialName("house_id")
    val houseId: Long,
    @SerialName("pen_id")
    val penId: Long,
    @SerialName("materials_used")
    val materialsUsed: String,
    @SerialName("recorded_date")
    val recordedDate: String,
    @SerialName("recorded_time")
    val recordedTime: String
)

@Serializable
data class PenCleaningApiMessageResponse(
    @SerialName("success")
    val success: Boolean? = null,
    @SerialName("message")
    val message: String? = null
)

data class PenCleaningHouseOption(
    val id: Long,
    val houseNumber: String
)

data class PenCleaningPenOption(
    val id: Long,
    val penName: String
)

data class PenCleaningAccessContext(
    val accessAllowed: Boolean,
    val house: PenCleaningHouseOption?,
    val pens: List<PenCleaningPenOption>,
    val message: String?
)

class PenCleaningBackendService(
    private val baseUrl: String = ApiConfig.BASE_URL
) {
    private val httpClient = HttpClient(Android)

    private val json = Json {
        ignoreUnknownKeys = true
    }

    suspend fun getPenCleaningContext(employeeId: Int): Result<PenCleaningAccessContext> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val responseText = httpClient.get("$baseUrl/api/mobile/pen-cleaning/context?employee_id=$employeeId") {
                    accept(ContentType.Application.Json)
                }.bodyAsText()

                val parsed: JsonElement = json.parseToJsonElement(responseText)

                if (parsed is JsonObject && parsed["success"] == null && parsed["access_allowed"] == null) {
                    val errorResponse = json.decodeFromJsonElement<LaravelErrorResponse>(parsed)
                    error(errorResponse.message ?: "Failed to load pen cleaning context.")
                }

                val response = json.decodeFromJsonElement<PenCleaningContextResponse>(parsed)

                PenCleaningAccessContext(
                    accessAllowed = response.accessAllowed == true,
                    house = if (response.houseId != null && !response.houseNumber.isNullOrBlank()) {
                        PenCleaningHouseOption(
                            id = response.houseId,
                            houseNumber = response.houseNumber
                        )
                    } else {
                        null
                    },
                    pens = response.penOptions.map {
                        PenCleaningPenOption(
                            id = it.id,
                            penName = it.penName?.ifBlank { "Unknown" } ?: "Unknown"
                        )
                    },
                    message = response.message
                )
            }
        }
    }

    suspend fun submitPenCleaning(
        employeeId: Int,
        houseId: Long,
        penId: Long,
        materialsUsed: String,
        recordedDate: String,
        recordedTime: String,
        taskId: Int? = null
    ): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val requestBody = PenCleaningSubmitRequest(
                    employeeId = employeeId,
                    taskId = taskId,
                    houseId = houseId,
                    penId = penId,
                    materialsUsed = materialsUsed,
                    recordedDate = recordedDate,
                    recordedTime = recordedTime
                )

                val responseText = httpClient.post("$baseUrl/api/mobile/pen-cleaning") {
                    contentType(ContentType.Application.Json)
                    accept(ContentType.Application.Json)
                    setBody(json.encodeToString(requestBody))
                }.bodyAsText()

                val parsed: JsonElement = json.parseToJsonElement(responseText)

                if (parsed is JsonObject && parsed["success"] == null) {
                    val errorResponse = json.decodeFromJsonElement<LaravelErrorResponse>(parsed)
                    error(errorResponse.message ?: "Failed to submit pen cleaning.")
                }

                val response = json.decodeFromJsonElement<PenCleaningApiMessageResponse>(parsed)
                response.success == true
            }
        }
    }

    suspend fun submitPenCleaningTask(
        employeeId: Int,
        taskId: Int,
        houseId: Long,
        penId: Long,
        materialsUsed: String,
        recordedAt: String
    ): Result<Boolean> {
        val parsedRecordedAt = LocalDateTime.parse(recordedAt, DateTimeFormatter.ISO_LOCAL_DATE_TIME)

        return submitPenCleaning(
            employeeId = employeeId,
            houseId = houseId,
            penId = penId,
            materialsUsed = materialsUsed,
            recordedDate = parsedRecordedAt.toLocalDate().toString(),
            recordedTime = parsedRecordedAt.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm:ss")),
            taskId = taskId
        )
    }
}