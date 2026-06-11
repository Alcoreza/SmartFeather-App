package com.example.smartfeather

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.request.accept
import io.ktor.client.request.get
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
import io.ktor.client.statement.bodyAsText

@Serializable
data class PersonnelHouseApiRow(
    @SerialName("id") val id: Long,
    @SerialName("house_number") val houseNumber: String? = null
)

@Serializable
data class PersonnelPreviousBiosecurityResponse(
    @SerialName("house_id") val houseId: Long? = null,
    @SerialName("pen_id") val penId: Long? = null,
    @SerialName("foot_bath") val footBath: Boolean = false,
    @SerialName("boots_changed") val bootsChanged: Boolean = false,
    @SerialName("protective_clothing") val protectiveClothing: Boolean = false
)

@Serializable
data class PersonnelLogsContextResponse(
    @SerialName("success") val success: Boolean? = null,
    @SerialName("employee_id") val employeeId: Int? = null,
    @SerialName("personnel_entry_log_id") val personnelEntryLogId: Long? = null,
    @SerialName("name") val name: String? = null,
    @SerialName("role") val role: String? = null,
    @SerialName("status") val status: String? = null,
    @SerialName("date") val date: String? = null,
    @SerialName("time") val time: String? = null,
    @SerialName("houses") val houses: List<PersonnelHouseApiRow> = emptyList(),
    @SerialName("previous_biosecurity") val previousBiosecurity: PersonnelPreviousBiosecurityResponse? = null,
    @SerialName("message") val message: String? = null
)

@Serializable
data class PersonnelLogsSubmitRequest(
    @SerialName("personnel_entry_log_id") val personnelEntryLogId: Long,
    @SerialName("task_id") val taskId: Int? = null,
    @SerialName("house_id") val houseId: Long,
    @SerialName("pen_id") val penId: Long? = null,
    @SerialName("foot_bath") val footBath: Boolean,
    @SerialName("boots_changed") val bootsChanged: Boolean,
    @SerialName("protective_clothing") val protectiveClothing: Boolean
)

@Serializable
data class PersonnelLogsSubmitResponse(
    @SerialName("success") val success: Boolean? = null,
    @SerialName("message") val message: String? = null
)

data class PersonnelHouseOption(
    val id: Long,
    val houseNumber: String
)

data class PersonnelPreviousBiosecurity(
    val houseId: Long?,
    val penId: Long?,
    val footBath: Boolean,
    val bootsChanged: Boolean,
    val protectiveClothing: Boolean
)

data class PersonnelLogsContext(
    val employeeId: Int,
    val personnelEntryLogId: Long,
    val name: String,
    val role: String,
    val status: String,
    val date: String,
    val time: String,
    val houses: List<PersonnelHouseOption>,
    val previousBiosecurity: PersonnelPreviousBiosecurity?
)

class PersonnelLogsBackendService(
    private val baseUrl: String = ApiConfig.BASE_URL
) {
    private val httpClient = HttpClient(Android)
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun getContext(
        employeeId: Int,
        accessToken: String = ""
    ): Result<PersonnelLogsContext> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val responseText = httpClient.get("$baseUrl/api/mobile/personnel-logs/context") {
                    accept(ContentType.Application.Json)
                    header(HttpHeaders.Authorization, "Bearer $accessToken")
                }.mobileBodyAsText()

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
                    status = result.status.orEmpty(),
                    date = result.date.orEmpty(),
                    time = result.time.orEmpty(),
                    houses = result.houses.map {
                        PersonnelHouseOption(
                            id = it.id,
                            houseNumber = it.houseNumber?.ifBlank { "Unknown" } ?: "Unknown"
                        )
                    },
                    previousBiosecurity = result.previousBiosecurity?.let {
                        PersonnelPreviousBiosecurity(
                            houseId = it.houseId,
                            penId = it.penId,
                            footBath = it.footBath,
                            bootsChanged = it.bootsChanged,
                            protectiveClothing = it.protectiveClothing
                        )
                    }
                )
            }
        }
    }

    suspend fun submit(
        employeeId: Int,
        accessToken: String = "",
        personnelEntryLogId: Long,
        taskId: Int? = null,
        houseId: Long,
        penId: Long? = null,
        footBath: Boolean,
        bootsChanged: Boolean,
        protectiveClothing: Boolean
    ): Result<String> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val requestBody = PersonnelLogsSubmitRequest(
                    personnelEntryLogId = personnelEntryLogId,
                    taskId = taskId,
                    houseId = houseId,
                    penId = penId,
                    footBath = footBath,
                    bootsChanged = bootsChanged,
                    protectiveClothing = protectiveClothing
                )

                val responseText = httpClient.post("$baseUrl/api/mobile/personnel-logs") {
                    contentType(ContentType.Application.Json)
                    accept(ContentType.Application.Json)
                    header(HttpHeaders.Authorization, "Bearer $accessToken")
                    setBody(json.encodeToString(requestBody))
                }.mobileBodyAsText()

                val parsed: JsonElement = json.parseToJsonElement(responseText)

                if (parsed is JsonObject && parsed["success"] == null) {
                    val errorResponse = json.decodeFromJsonElement<LaravelErrorResponse>(parsed)
                    error(errorResponse.message ?: "Failed to submit personnel biosecurity log.")
                }

                val result = json.decodeFromJsonElement<PersonnelLogsSubmitResponse>(parsed)
                result.message ?: "Personnel biosecurity log submitted successfully."
            }
        }
    }
}