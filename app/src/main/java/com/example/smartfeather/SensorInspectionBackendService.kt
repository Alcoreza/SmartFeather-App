package com.example.smartfeather

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.request.accept
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
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
data class SensorInspectionSubmitRequest(
    @SerialName("task_id")
    val taskId: Int,
    @SerialName("house_id")
    val houseId: Long,
    @SerialName("pen_id")
    val penId: Long,
    @SerialName("sensor_present")
    val sensorPresent: Boolean,
    @SerialName("sensor_clean_unblocked")
    val sensorCleanUnblocked: Boolean,
    @SerialName("no_visible_damage_or_loose_wiring")
    val noVisibleDamageOrLooseWiring: Boolean,
    @SerialName("power_status_on")
    val powerStatusOn: Boolean,
    @SerialName("placement_secure")
    val placementSecure: Boolean,
    @SerialName("recorded_at")
    val recordedAt: String
)

@Serializable
data class SensorInspectionApiMessageResponse(
    @SerialName("success")
    val success: Boolean? = null,
    @SerialName("message")
    val message: String? = null
)

class SensorInspectionBackendService(
    private val baseUrl: String = ApiConfig.BASE_URL
) {
    private val httpClient = HttpClient(Android)

    private val json = Json {
        ignoreUnknownKeys = true
    }

    suspend fun submitSensorInspectionTask(
        employeeId: Int,
        accessToken: String = "",
        taskId: Int,
        houseId: Long,
        penId: Long,
        checklist: SensorInspectionChecklistState,
        recordedAt: String
    ): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val requestBody = SensorInspectionSubmitRequest(
                    taskId = taskId,
                    houseId = houseId,
                    penId = penId,
                    sensorPresent = checklist.sensorPresent == true,
                    sensorCleanUnblocked = checklist.sensorCleanUnblocked == true,
                    noVisibleDamageOrLooseWiring = checklist.noVisibleDamageOrLooseWiring == true,
                    powerStatusOn = checklist.powerStatusOn == true,
                    placementSecure = checklist.placementSecure == true,
                    recordedAt = recordedAt
                )

                val responseText = httpClient.post("$baseUrl/api/mobile/sensor-inspection") {
                    contentType(ContentType.Application.Json)
                    accept(ContentType.Application.Json)
                    header(HttpHeaders.Authorization, "Bearer $accessToken")
                    setBody(json.encodeToString(requestBody))
                }.bodyAsText()

                val parsed: JsonElement = json.parseToJsonElement(responseText)

                if (parsed is JsonObject && parsed["success"] == null) {
                    val errorResponse = json.decodeFromJsonElement<LaravelErrorResponse>(parsed)
                    error(errorResponse.message ?: "Failed to submit sensor inspection.")
                }

                val response = json.decodeFromJsonElement<SensorInspectionApiMessageResponse>(parsed)
                response.success == true
            }
        }
    }
}