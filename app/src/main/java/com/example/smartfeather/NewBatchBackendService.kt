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
    @SerialName("message") val message: String? = null,
    @SerialName("error_code") val errorCode: String? = null
)

@Serializable
data class NewBatchSubmitRequest(
    @SerialName("employee_id") val employeeId: Int,
    @SerialName("task_id") val taskId: Int? = null,
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
    @SerialName("error_code") val errorCode: String? = null,
    @SerialName("existing_batch") val existingBatch: ExistingBatchInfo? = null,
    @SerialName("batch_code") val batchCode: String? = null,
    @SerialName("started_at") val startedAt: String? = null
)

data class NewBatchHouseOption(
    val id: Long,
    val houseNumber: String
)

data class NewBatchPenOption(
    val id: Long,
    val penName: String,
    val currentBatchId: Int?,
    val currentBatchCode: String?,
    val currentBatchStatus: String?
)

data class NewBatchAccessContext(
    val accessAllowed: Boolean,
    val house: NewBatchHouseOption?,
    val pens: List<NewBatchPenOption>,
    val message: String?,
    val errorCode: String? = null
)

data class NewBatchSubmitResult(
    val success: Boolean,
    val message: String,
    val errorCode: String? = null,
    val existingBatchCode: String? = null,
    val existingStartedAt: String? = null,
    val batchCode: String? = null,
    val startedAt: String? = null
)

class NewBatchBackendService(
    private val baseUrl: String = ApiConfig.BASE_URL
) {
    private val httpClient = HttpClient(Android)

    private val json = Json {
        ignoreUnknownKeys = true
    }

    suspend fun getNewBatchContext(employeeId: Int): Result<NewBatchAccessContext> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val responseText = httpClient.get("$baseUrl/api/mobile/new-batch/context?employee_id=$employeeId") {
                    accept(ContentType.Application.Json)
                }.bodyAsText()

                val parsed: JsonElement = json.parseToJsonElement(responseText)

                val response = if (parsed is JsonObject) {
                    json.decodeFromJsonElement<NewBatchContextResponse>(parsed)
                } else {
                    error("Invalid response while loading new batch context.")
                }

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
                    message = response.message,
                    errorCode = response.errorCode
                )
            }
        }
    }

    suspend fun submitNewBatch(
        employeeId: Int,
        batchCode: String,
        houseId: Long,
        penId: Long,
        initialPopulation: Int,
        date: String,
        time: String,
        taskId: Int? = null
    ): Result<NewBatchSubmitResult> = withContext(Dispatchers.IO) {
        runCatching {
            val responseText = httpClient.post("$baseUrl/api/mobile/new-batch") {
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
                setBody(
                    json.encodeToString(
                        NewBatchSubmitRequest(
                            employeeId = employeeId,
                            taskId = taskId,
                            batchCode = batchCode,
                            houseId = houseId,
                            penId = penId,
                            initialPopulation = initialPopulation,
                            date = date,
                            time = time
                        )
                    )
                )
            }.bodyAsText()

            val parsed: JsonElement = json.parseToJsonElement(responseText)

            val response = if (parsed is JsonObject) {
                json.decodeFromJsonElement<NewBatchSubmitResponse>(parsed)
            } else {
                error("Invalid response while submitting new batch.")
            }

            NewBatchSubmitResult(
                success = response.success == true,
                message = response.message ?: "Unable to submit new batch.",
                errorCode = response.errorCode,
                existingBatchCode = response.existingBatch?.batchCode,
                existingStartedAt = response.existingBatch?.startedAt,
                batchCode = response.batchCode,
                startedAt = response.startedAt
            )
        }
    }

    suspend fun submitChickPlacementTask(
        employeeId: Int,
        taskId: Int,
        batchCode: String,
        houseId: Long,
        penId: Long,
        initialPopulation: Int,
        recordedAt: String
    ): Result<Boolean> {
        return runCatching {
            val parsedRecordedAt = LocalDateTime.parse(
                recordedAt,
                DateTimeFormatter.ISO_LOCAL_DATE_TIME
            )

            val result = submitNewBatch(
                employeeId = employeeId,
                taskId = taskId,
                batchCode = batchCode,
                houseId = houseId,
                penId = penId,
                initialPopulation = initialPopulation,
                date = parsedRecordedAt.toLocalDate().toString(),
                time = parsedRecordedAt.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"))
            ).getOrThrow()

            if (!result.success) {
                error(result.message.ifBlank { "Unable to submit chick placement." })
            }

            true
        }
    }
}