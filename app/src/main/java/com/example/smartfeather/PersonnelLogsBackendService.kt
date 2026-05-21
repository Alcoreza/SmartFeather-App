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
data class PersonnelLogsContextResponse(
    @SerialName("success")
    val success: Boolean? = null,
    @SerialName("employee_id")
    val employeeId: Int? = null,
    @SerialName("personnel_entry_log_id")
    val personnelEntryLogId: Long? = null,
    @SerialName("name")
    val name: String? = null,
    @SerialName("role")
    val role: String? = null,
    @SerialName("house_id")
    val houseId: Long? = null,
    @SerialName("house")
    val house: String? = null,
    @SerialName("status")
    val status: String? = null,
    @SerialName("date")
    val date: String? = null,
    @SerialName("time")
    val time: String? = null,
    @SerialName("message")
    val message: String? = null
)

@Serializable
data class PersonnelLogsSubmitRequest(
    @SerialName("employee_id")
    val employeeId: Int,
    @SerialName("personnel_entry_log_id")
    val personnelEntryLogId: Long,
    @SerialName("foot_bath")
    val footBath: Boolean,
    @SerialName("boots_changed")
    val bootsChanged: Boolean,
    @SerialName("protective_clothing")
    val protectiveClothing: Boolean
)

@Serializable
data class PersonnelLogsSubmitResponse(
    @SerialName("success")
    val success: Boolean? = null,
    @SerialName("message")
    val message: String? = null
)

data class PersonnelLogsContext(
    val employeeId: Int,
    val personnelEntryLogId: Long,
    val name: String,
    val role: String,
    val houseId: Long?,
    val house: String,
    val status: String,
    val date: String,
    val time: String
)

class PersonnelLogsBackendService(
    private val baseUrl: String = ApiConfig.BASE_URL
) {
    private val httpClient = HttpClient(Android)
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun getContext(employeeId: Int): Result<PersonnelLogsContext> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val responseText = httpClient.get("$baseUrl/api/mobile/personnel-logs/context?employee_id=$employeeId") {
                    accept(ContentType.Application.Json)
                }.bodyAsText()

                val parsed: JsonElement = json.parseToJsonElement(responseText)

                if (parsed is JsonObject && parsed["success"] == null) {
                    val errorResponse = json.decodeFromJsonElement<LaravelErrorResponse>(parsed)
                    error(errorResponse.message ?: "Failed to load personnel log context.")
                }

                val result = json.decodeFromJsonElement<PersonnelLogsContextResponse>(parsed)

                PersonnelLogsContext(
                    employeeId = result.employeeId ?: error("Missing employee ID."),
                    personnelEntryLogId = result.personnelEntryLogId ?: error("Missing personnel entry log ID."),
                    name = result.name.orEmpty(),
                    role = result.role.orEmpty(),
                    houseId = result.houseId,
                    house = result.house.orEmpty(),
                    status = result.status.orEmpty(),
                    date = result.date.orEmpty(),
                    time = result.time.orEmpty()
                )
            }
        }
    }

    suspend fun submit(
        employeeId: Int,
        personnelEntryLogId: Long,
        footBath: Boolean,
        bootsChanged: Boolean,
        protectiveClothing: Boolean
    ): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val requestBody = PersonnelLogsSubmitRequest(
                    employeeId = employeeId,
                    personnelEntryLogId = personnelEntryLogId,
                    footBath = footBath,
                    bootsChanged = bootsChanged,
                    protectiveClothing = protectiveClothing
                )

                val responseText = httpClient.post("$baseUrl/api/mobile/personnel-logs") {
                    contentType(ContentType.Application.Json)
                    accept(ContentType.Application.Json)
                    setBody(json.encodeToString(requestBody))
                }.bodyAsText()

                val parsed: JsonElement = json.parseToJsonElement(responseText)

                if (parsed is JsonObject && parsed["success"] == null) {
                    val errorResponse = json.decodeFromJsonElement<LaravelErrorResponse>(parsed)
                    error(errorResponse.message ?: "Failed to submit personnel biosecurity log.")
                }

                val result = json.decodeFromJsonElement<PersonnelLogsSubmitResponse>(parsed)
                result.success == true
            }
        }
    }
}