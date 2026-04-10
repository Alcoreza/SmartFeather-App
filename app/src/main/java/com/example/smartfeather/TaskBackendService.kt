package com.example.smartfeather

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.request.header
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
import java.util.Locale

@Serializable
data class FlockmanTasksRequest(
    @SerialName("p_employee_id")
    val employeeId: Int
)

@Serializable
data class CompleteTaskRequest(
    @SerialName("p_task_id")
    val taskId: Int,
    @SerialName("p_employee_id")
    val employeeId: Int,
    @SerialName("p_notes")
    val notes: String? = null,
    @SerialName("p_photo_url")
    val photoUrl: String? = null
)

@Serializable
data class TaskRpcRow(
    @SerialName("taskid")
    val taskId: Int,
    @SerialName("tasktype")
    val taskType: String,
    @SerialName("detailedtask")
    val detailedTask: String? = null,
    @SerialName("timeassigned")
    val timeAssigned: String? = null,
    @SerialName("finishby")
    val finishBy: String? = null,
    @SerialName("status")
    val status: String,
    @SerialName("notes")
    val notes: String? = null,
    @SerialName("time_completed")
    val timeCompleted: String? = null,
    @SerialName("user_employeeid")
    val userEmployeeId: Int,
    @SerialName("house_houseid")
    val houseId: Int? = null,
    @SerialName("pennumber")
    val penNumber: Int? = null,
    @SerialName("prioritylevel")
    val priorityLevel: String? = null,
    @SerialName("photourl")
    val photoUrl: String? = null
)

class TaskBackendService(
    private val baseUrl: String = SupabaseConfig.SUPABASE_URL,
    private val publishableKey: String = SupabaseConfig.SUPABASE_PUBLISHABLE_KEY
) {
    private val httpClient = HttpClient(Android)

    private val json = Json {
        ignoreUnknownKeys = true
    }

    suspend fun getTasksForFlockman(employeeId: Int): Result<List<TaskItem>> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val requestBody = FlockmanTasksRequest(employeeId)

                val responseText =
                    httpClient.post("$baseUrl/rest/v1/rpc/mobile_get_flockman_tasks") {
                        header("apikey", publishableKey)
                        header("Authorization", "Bearer $publishableKey")
                        contentType(ContentType.Application.Json)
                        setBody(json.encodeToString(requestBody))
                    }.bodyAsText()

                val parsed: JsonElement = json.parseToJsonElement(responseText)

                if (parsed is JsonObject && parsed["message"] != null) {
                    val errorResponse = json.decodeFromJsonElement<SupabaseErrorResponse>(parsed)
                    error(errorResponse.message ?: "Failed to load tasks.")
                }

                json.decodeFromJsonElement<List<TaskRpcRow>>(parsed).map { it.toTaskItem() }
            }
        }
    }

    suspend fun submitTaskForApproval(
        taskId: Int,
        employeeId: Int,
        notes: String,
        photoUrl: String? = null
    ): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val requestBody = CompleteTaskRequest(
                    taskId = taskId,
                    employeeId = employeeId,
                    notes = notes.ifBlank { null },
                    photoUrl = photoUrl
                )

                val responseText =
                    httpClient.post("$baseUrl/rest/v1/rpc/mobile_submit_task_for_approval") {
                        header("apikey", publishableKey)
                        header("Authorization", "Bearer $publishableKey")
                        contentType(ContentType.Application.Json)
                        setBody(json.encodeToString(requestBody))
                    }.bodyAsText()

                val parsed: JsonElement = json.parseToJsonElement(responseText)

                if (parsed is JsonObject && parsed["message"] != null) {
                    val errorResponse = json.decodeFromJsonElement<SupabaseErrorResponse>(parsed)
                    error(errorResponse.message ?: "Failed to submit task for approval.")
                }

                parsed.toString().contains("true")
            }
        }
    }


    private fun TaskRpcRow.toTaskItem(): TaskItem {
        val statusEnum = when (status.trim().lowercase(Locale.ROOT)) {
            "completed" -> TaskStatus.COMPLETED
            "for approval" -> TaskStatus.FOR_APPROVAL
            else -> TaskStatus.PENDING
        }


        val priorityEnum = when (priorityLevel?.trim()?.uppercase(Locale.ROOT)) {
            "LOW" -> TaskPriority.LOW
            "MEDIUM", "MID" -> TaskPriority.MID
            "HIGH", "URGENT" -> TaskPriority.HIGH
            else -> TaskPriority.MID
        }

        val descriptionText =
            detailedTask?.takeIf { it.isNotBlank() } ?: "No task details provided."

        val timeLabelText = when (statusEnum) {
            TaskStatus.COMPLETED -> "Completed: ${formatTaskTimestamp(timeCompleted)}"
            TaskStatus.FOR_APPROVAL -> "Submitted: ${formatTaskTimestamp(timeCompleted)}"
            TaskStatus.PENDING -> {
                val finishText = finishBy?.let { formatTaskTimestamp(it) }
                if (!finishText.isNullOrBlank()) {
                    "Finish by: $finishText"
                } else {
                    "Assigned: ${formatTaskTimestamp(timeAssigned)}"
                }
            }
        }


        return TaskItem(
            id = taskId,
            title = taskType,
            description = descriptionText,
            houseLabel = "House: ${houseId ?: "-"}",
            penLabel = "Pen: ${penNumber ?: "-"}",
            timeLabel = timeLabelText,
            priority = priorityEnum,
            status = statusEnum,
            notes = notes ?: "",
            hasPhoto = !photoUrl.isNullOrBlank()
        )
    }

    private fun formatTaskTimestamp(value: String?): String {
        if (value.isNullOrBlank()) return "-"

        return runCatching {
            val dateTime = LocalDateTime.parse(value.replace(" ", "T"))
            val dateFormatter = DateTimeFormatter.ofPattern("M-d-yy", Locale.getDefault())
            val timeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
            "${dateTime.format(dateFormatter)}\n${dateTime.format(timeFormatter)}"
        }.getOrElse { value }
    }
}
