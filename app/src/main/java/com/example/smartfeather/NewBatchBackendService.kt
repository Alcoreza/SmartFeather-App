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
data class NewBatchPenApiRow(
    @SerialName("id") val id: Long,
    @SerialName("pen_name") val penName: String? = null,
    @SerialName("current_batch_id") val currentBatchId: Int? = null,
    @SerialName("current_batch_code") val currentBatchCode: String? = null,
    @SerialName("current_batch_status") val currentBatchStatus: String? = null
)

@Serializable
data class NewBatchContextResponse(
    @SerialName("success") val success: Boolean? = null,
    @SerialName("access_allowed") val accessAllowed: Boolean? = null,
    @SerialName("house_id") val houseId: Long? = null,
    @SerialName("house_number") val houseNumber: String? = null,
    @SerialName("pen_options") val penOptions: List<NewBatchPenApiRow> = emptyList(),
    @SerialName("message") val message: String? = null
)

@Serializable
data class NewBatchSubmitRequest(
    @SerialName("employee_id") val employeeId: Int,
    @SerialName("batch_code") val batchCode: String,
    @SerialName("house_id") val houseId: Long,
    @SerialName("pen_id") val penId: Long,
    @SerialName("initial_population") val initialPopulation: Int,
    @SerialName("date") val date: String,
    @SerialName("time") val time: String
)

@Serializable
data class ExistingBatchInfo(
    @SerialName("batch_code") val batchCode: String? = null,
    @SerialName("started_at") val startedAt: String? = null
)

@Serializable
data class NewBatchSubmitResponse(
    @SerialName("success") val success: Boolean? = null,
    @SerialName("message") val message: String? = null,
    @SerialName("existing_batch") val existingBatch: ExistingBatchInfo? = null
)

data class NewBatchHouseOption(
    val id: Long,
    val houseNumber: String
)

data class NewBatchPenOption(
    val id: Long,
    val penName: String,
    val currentBatchId: Int? = null,
    val currentBatchCode: String? = null,
    val currentBatchStatus: String? = null
)

data class NewBatchAccessContext(
    val accessAllowed: Boolean,
    val house: NewBatchHouseOption?,
    val pens: List<NewBatchPenOption>,
    val message: String?
)

data class NewBatchSubmitResult(
    val success: Boolean,
    val message: String,
    val isConflict: Boolean = false,
    val existingBatchCode: String? = null,
    val existingStartedAt: String? = null
)

class NewBatchBackendService(
    private val baseUrl: String = ApiConfig.BASE_URL
) {
    private val httpClient = HttpClient(Android)
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun getNewBatchContext(employeeId: Int): Result<NewBatchAccessContext> = withContext(Dispatchers.IO) {
        runCatching {
            val responseText = httpClient.get("$baseUrl/api/mobile/new-batch/context?employee_id=$employeeId") {
                accept(ContentType.Application.Json)
            }.bodyAsText()

            val parsed: JsonElement = json.parseToJsonElement(responseText)
            if (parsed is JsonObject && parsed["success"] == null && parsed["access_allowed"] == null) {
                val error = json.decodeFromJsonElement<LaravelErrorResponse>(parsed)
                error(error.message ?: "Failed to load add new batch context.")
            }

            val response = json.decodeFromJsonElement<NewBatchContextResponse>(parsed)

            NewBatchAccessContext(
                accessAllowed = response.accessAllowed == true,
                house = if (response.houseId != null && !response.houseNumber.isNullOrBlank()) {
                    NewBatchHouseOption(
                        id = response.houseId,
                        houseNumber = response.houseNumber
                    )
                } else {
                    null
                },
                pens = response.penOptions.map {
                    NewBatchPenOption(
                        id = it.id,
                        penName = it.penName?.ifBlank { "Unknown" } ?: "Unknown",
                        currentBatchId = it.currentBatchId,
                        currentBatchCode = it.currentBatchCode,
                        currentBatchStatus = it.currentBatchStatus
                    )
                },
                message = response.message
            )
        }
    }

    suspend fun submitNewBatch(
        employeeId: Int,
        batchCode: String,
        houseId: Long,
        penId: Long,
        initialPopulation: Int,
        date: String,
        time: String
    ): Result<NewBatchSubmitResult> = withContext(Dispatchers.IO) {
        runCatching {
            val response = httpClient.post("$baseUrl/api/mobile/new-batch") {
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
                setBody(
                    json.encodeToString(
                        NewBatchSubmitRequest(
                            employeeId = employeeId,
                            batchCode = batchCode,
                            houseId = houseId,
                            penId = penId,
                            initialPopulation = initialPopulation,
                            date = date,
                            time = time
                        )
                    )
                )
            }

            val statusCode = response.status.value
            val responseText = response.bodyAsText()
            val parsed = json.parseToJsonElement(responseText)

            if (statusCode == 409) {
                val conflict = json.decodeFromJsonElement<NewBatchSubmitResponse>(parsed)
                NewBatchSubmitResult(
                    success = false,
                    message = conflict.message ?: "There is already a running batch in this pen.",
                    isConflict = true,
                    existingBatchCode = conflict.existingBatch?.batchCode,
                    existingStartedAt = conflict.existingBatch?.startedAt
                )
            } else {
                if (parsed is JsonObject && parsed["success"] == null) {
                    val error = json.decodeFromJsonElement<LaravelErrorResponse>(parsed)
                    error(error.message ?: "Failed to add new batch.")
                }

                val success = json.decodeFromJsonElement<NewBatchSubmitResponse>(parsed)
                NewBatchSubmitResult(
                    success = success.success == true,
                    message = success.message ?: "New batch added successfully."
                )
            }
        }
    }
}