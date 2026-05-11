package com.example.smartfeather

import android.content.Context
import android.net.Uri
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.request.accept
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.storage.uploadToSignedUrl


@Serializable
data class FlockmanTasksRequest(
    @SerialName("employee_id")
    val employeeId: Int
)

@Serializable
data class CompleteTaskRequest(
    @SerialName("task_id")
    val taskId: Int,
    @SerialName("employee_id")
    val employeeId: Int,
    @SerialName("notes")
    val notes: String? = null,
    @SerialName("photo_path")
    val photoPath: String? = null
)

@Serializable
data class CreateTaskPhotoUploadUrlRequest(
    @SerialName("task_id")
    val taskId: Int,
    @SerialName("employee_id")
    val employeeId: Int,
    @SerialName("mime_type")
    val mimeType: String
)

@Serializable
data class TaskPhotoUploadUrlResponse(
    @SerialName("success")
    val success: Boolean? = null,
    @SerialName("bucket")
    val bucket: String? = null,
    @SerialName("path")
    val path: String? = null,
    @SerialName("token")
    val token: String? = null,
    @SerialName("public_url")
    val publicUrl: String? = null,
    @SerialName("message")
    val message: String? = null
)


@Serializable
data class TaskApiRow(
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

@Serializable
data class ApiMessageResponse(
    @SerialName("success")
    val success: Boolean? = null,
    @SerialName("message")
    val message: String? = null
)

class TaskBackendService(
    private val baseUrl: String = ApiConfig.BASE_URL
) {
    private val httpClient = HttpClient(Android)

    private val json = Json {
        ignoreUnknownKeys = true
    }

    suspend fun getTasksForFlockman(employeeId: Int): Result<List<TaskItem>> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val requestBody = FlockmanTasksRequest(employeeId)

                val responseText = httpClient.post("$baseUrl/api/mobile/tasks") {
                    contentType(ContentType.Application.Json)
                    accept(ContentType.Application.Json)
                    setBody(json.encodeToString(requestBody))
                }.bodyAsText()

                val parsed: JsonElement = json.parseToJsonElement(responseText)

                if (parsed is JsonObject && parsed["message"] != null && parsed["taskid"] == null) {
                    val errorResponse = json.decodeFromJsonElement<LaravelErrorResponse>(parsed)
                    error(errorResponse.message ?: "Failed to load tasks.")
                }

                json.decodeFromJsonElement<List<TaskApiRow>>(parsed).map { it.toTaskItem() }
            }
        }
    }

    suspend fun submitTaskForApproval(
        context: Context,
        taskId: Int,
        employeeId: Int,
        notes: String,
        photoUri: Uri? = null
    ): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val uploadedPath = if (photoUri != null) {
                    uploadTaskPhoto(context, taskId, employeeId, photoUri)
                } else {
                    null
                }

                val requestBody = CompleteTaskRequest(
                    taskId = taskId,
                    employeeId = employeeId,
                    notes = notes.ifBlank { null },
                    photoPath = uploadedPath
                )

                val responseText = httpClient.post("$baseUrl/api/mobile/tasks/submit") {
                    contentType(ContentType.Application.Json)
                    accept(ContentType.Application.Json)
                    setBody(json.encodeToString(requestBody))
                }.bodyAsText()

                val parsed: JsonElement = json.parseToJsonElement(responseText)

                if (parsed is JsonObject && parsed["success"] == null) {
                    val errorResponse = json.decodeFromJsonElement<LaravelErrorResponse>(parsed)
                    error(errorResponse.message ?: "Failed to submit task for approval.")
                }

                val result = json.decodeFromJsonElement<ApiMessageResponse>(parsed)
                result.success == true
            }
        }
    }

    private suspend fun uploadTaskPhoto(
        context: Context,
        taskId: Int,
        employeeId: Int,
        photoUri: Uri
    ): String {
        val mimeType = context.contentResolver.getType(photoUri) ?: "image/jpeg"

        val signedUrlResponseText = httpClient.post("$baseUrl/api/mobile/tasks/photo-upload-url") {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            setBody(
                json.encodeToString(
                    CreateTaskPhotoUploadUrlRequest(
                        taskId = taskId,
                        employeeId = employeeId,
                        mimeType = mimeType
                    )
                )
            )
        }.bodyAsText()

        val signedUrlParsed = json.parseToJsonElement(signedUrlResponseText)
        if (signedUrlParsed is JsonObject && signedUrlParsed["success"] == null) {
            val errorResponse = json.decodeFromJsonElement<LaravelErrorResponse>(signedUrlParsed)
            error(errorResponse.message ?: "Failed to create signed upload URL.")
        }

        val uploadInfo = json.decodeFromJsonElement<TaskPhotoUploadUrlResponse>(signedUrlParsed)
        val bucket = uploadInfo.bucket ?: error("Missing upload bucket.")
        val path = uploadInfo.path ?: error("Missing upload path.")
        val token = uploadInfo.token ?: error("Missing upload token.")

        SupabaseProvider.client.storage
            .from(bucket)
            .uploadToSignedUrl(
                path = path,
                token = token,
                uri = photoUri
            )

        return path
    }


    private fun TaskApiRow.toTaskItem(): TaskItem {
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

        return TaskItem(
            id = taskId,
            title = taskType,
            description = descriptionText,
            houseLabel = "House: ${houseId ?: "-"}",
            penLabel = "Pen: ${penNumber ?: "-"}",
            assignedLabel = if (!timeAssigned.isNullOrBlank()) {
                formatTaskTimestamp(timeAssigned)
            } else {
                ""
            },
            finishByLabel = if (!finishBy.isNullOrBlank()) {
                "Finish by: ${formatTaskTimestamp(finishBy)}"
            } else {
                ""
            },
            submittedLabel = if (!timeCompleted.isNullOrBlank()) {
                "Submitted: ${formatTaskTimestamp(timeCompleted)}"
            } else {
                ""
            },
            completedLabel = if (!timeCompleted.isNullOrBlank()) {
                "Completed: ${formatTaskTimestamp(timeCompleted)}"
            } else {
                ""
            },
            priority = priorityEnum,
            status = statusEnum,
            notes = notes ?: "",
            hasPhoto = !photoUrl.isNullOrBlank(),
            photoUrl = photoUrl
        )

    }

    private fun formatTaskTimestamp(value: String?): String {
        if (value.isNullOrBlank()) return "-"

        val parsedDateTime = parseTaskDateTime(value) ?: return value

        val dateFormatter = DateTimeFormatter.ofPattern("M-d-yy", Locale.getDefault())
        val timeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())

        return "${parsedDateTime.format(dateFormatter)}\n${parsedDateTime.format(timeFormatter)}"
    }

    private fun parseTaskDateTime(value: String): LocalDateTime? {
        val normalized = value.trim()

        val dateTimePatterns = listOf(
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
        )

        for (formatter in dateTimePatterns) {
            try {
                return LocalDateTime.parse(normalized, formatter)
            } catch (_: DateTimeParseException) {
            }
        }

        return try {
            LocalDate.parse(normalized, DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay()
        } catch (_: DateTimeParseException) {
            null
        }
    }
}
