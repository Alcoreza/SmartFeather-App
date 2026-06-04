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
data class DisinfectionPenApiRow(
    @SerialName("id")
    val id: Long,
    @SerialName("pen_name")
    val penName: String? = null
)

@Serializable
data class DisinfectionContextResponse(
    @SerialName("success")
    val success: Boolean? = null,
    @SerialName("access_allowed")
    val accessAllowed: Boolean? = null,
    @SerialName("house_id")
    val houseId: Long? = null,
    @SerialName("house_number")
    val houseNumber: String? = null,
    @SerialName("pen_options")
    val penOptions: List<DisinfectionPenApiRow> = emptyList(),
    @SerialName("message")
    val message: String? = null
)

@Serializable
data class DisinfectionSubmitRequest(
    @SerialName("employee_id")
    val employeeId: Int,
    @SerialName("task_id")
    val taskId: Int? = null,
    @SerialName("house_id")
    val houseId: Long,
    @SerialName("pen_id")
    val penId: Long,
    @SerialName("activity")
    val activity: String,
    @SerialName("disinfectant_used")
    val disinfectantUsed: String,
    @SerialName("recorded_date")
    val recordedDate: String,
    @SerialName("recorded_time")
    val recordedTime: String
)

@Serializable
data class DisinfectionApiMessageResponse(
    @SerialName("success")
    val success: Boolean? = null,
    @SerialName("message")
    val message: String? = null
)

data class DisinfectionHouseOption(
    val id: Long,
    val houseNumber: String
)

data class DisinfectionPenOption(
    val id: Long,
    val penName: String
)

data class DisinfectionAccessContext(
    val accessAllowed: Boolean,
    val house: DisinfectionHouseOption?,
    val pens: List<DisinfectionPenOption>,
    val message: String?
)

class DisinfectionBackendService(
    private val baseUrl: String = ApiConfig.BASE_URL
) {
    private val httpClient = HttpClient(Android)

    private val json = Json {
        ignoreUnknownKeys = true
    }

    suspend fun getDisinfectionContext(employeeId: Int): Result<DisinfectionAccessContext> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val responseText = httpClient.get("$baseUrl/api/mobile/disinfection/context?employee_id=$employeeId") {
                    accept(ContentType.Application.Json)
                }.bodyAsText()

                val parsed: JsonElement = json.parseToJsonElement(responseText)

                if (parsed is JsonObject && parsed["success"] == null && parsed["access_allowed"] == null) {
                    val errorResponse = json.decodeFromJsonElement<LaravelErrorResponse>(parsed)
                    error(errorResponse.message ?: "Failed to load disinfection context.")
                }

                val response = json.decodeFromJsonElement<DisinfectionContextResponse>(parsed)

                DisinfectionAccessContext(
                    accessAllowed = response.accessAllowed == true,
                    house = if (response.houseId != null && !response.houseNumber.isNullOrBlank()) {
                        DisinfectionHouseOption(
                            id = response.houseId,
                            houseNumber = response.houseNumber
                        )
                    } else {
                        null
                    },
                    pens = response.penOptions.map {
                        DisinfectionPenOption(
                            id = it.id,
                            penName = it.penName?.ifBlank { "Unknown" } ?: "Unknown"
                        )
                    },
                    message = response.message
                )
            }
        }
    }

    suspend fun submitDisinfection(
        employeeId: Int,
        houseId: Long,
        penId: Long,
        activity: String,
        disinfectantUsed: String,
        recordedDate: String,
        recordedTime: String,
        taskId: Int? = null
    ): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val requestBody = DisinfectionSubmitRequest(
                    employeeId = employeeId,
                    taskId = taskId,
                    houseId = houseId,
                    penId = penId,
                    activity = activity,
                    disinfectantUsed = disinfectantUsed,
                    recordedDate = recordedDate,
                    recordedTime = recordedTime
                )

                val responseText = httpClient.post("$baseUrl/api/mobile/disinfection") {
                    contentType(ContentType.Application.Json)
                    accept(ContentType.Application.Json)
                    setBody(json.encodeToString(requestBody))
                }.bodyAsText()

                val parsed: JsonElement = json.parseToJsonElement(responseText)

                if (parsed is JsonObject && parsed["success"] == null) {
                    val errorResponse = json.decodeFromJsonElement<LaravelErrorResponse>(parsed)
                    error(errorResponse.message ?: "Failed to submit disinfection.")
                }

                val response = json.decodeFromJsonElement<DisinfectionApiMessageResponse>(parsed)
                response.success == true
            }
        }
    }

    suspend fun submitPenDisinfectionTask(
        employeeId: Int,
        taskId: Int,
        houseId: Long,
        penId: Long,
        activity: String,
        disinfectantUsed: String,
        recordedAt: String
    ): Result<Boolean> {
        val parsedRecordedAt = LocalDateTime.parse(recordedAt, DateTimeFormatter.ISO_LOCAL_DATE_TIME)

        return submitDisinfection(
            employeeId = employeeId,
            houseId = houseId,
            penId = penId,
            activity = activity,
            disinfectantUsed = disinfectantUsed,
            recordedDate = parsedRecordedAt.toLocalDate().toString(),
            recordedTime = parsedRecordedAt.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm:ss")),
            taskId = taskId
        )
    }
}