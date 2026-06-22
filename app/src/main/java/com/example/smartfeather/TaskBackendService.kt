package com.example.smartfeather

import android.content.Context
import android.net.Uri
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.storage.uploadToSignedUrl
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
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale
import io.ktor.client.statement.bodyAsText

@Serializable
data class FlockmanTasksRequest(
    @SerialName("status")
    val status: String,
    @SerialName("page")
    val page: Int,
    @SerialName("per_page")
    val perPage: Int
)

@Serializable
data class TaskCountsApiRow(
    @SerialName("pending")
    val pending: Int = 0,
    @SerialName("for_approval")
    val forApproval: Int = 0,
    @SerialName("completed")
    val completed: Int = 0
)

@Serializable
data class TaskPaginationApiRow(
    @SerialName("current_page")
    val currentPage: Int = 1,
    @SerialName("per_page")
    val perPage: Int = 10,
    @SerialName("has_more")
    val hasMore: Boolean = false
)

@Serializable
data class FlockmanTasksResponse(
    @SerialName("success")
    val success: Boolean? = null,
    @SerialName("tasks")
    val tasks: List<TaskApiRow> = emptyList(),
    @SerialName("counts")
    val counts: TaskCountsApiRow = TaskCountsApiRow(),
    @SerialName("pagination")
    val pagination: TaskPaginationApiRow = TaskPaginationApiRow()
)

@Serializable
data class SubmittedTaskDetailRequest(
    @SerialName("task_id")
    val taskId: Int
)

@Serializable
data class SubmittedTaskDetailResponse(
    @SerialName("success")
    val success: Boolean? = null,
    @SerialName("submitted_fields")
    val submittedFields: List<TaskSubmittedFieldApiRow> = emptyList()
)

@Serializable
data class TaskSubmittedFieldApiRow(
    @SerialName("label")
    val label: String,
    @SerialName("value")
    val value: String? = null
)

@Serializable
data class TaskAccessCheckRequest(
    @SerialName("task_id")
    val taskId: Int
)

@Serializable
data class TaskAccessCheckResponse(
    @SerialName("success")
    val success: Boolean? = null,
    @SerialName("access_granted")
    val accessGranted: Boolean? = null,
    @SerialName("message")
    val message: String? = null
)

@Serializable
data class CompleteTaskRequest(
    @SerialName("task_id")
    val taskId: Int,
    @SerialName("notes")
    val notes: String? = null,
    @SerialName("photo_path")
    val photoPath: String? = null
)

@Serializable
data class CreateTaskPhotoUploadUrlRequest(
    @SerialName("task_id")
    val taskId: Int,
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
    val photoUrl: String? = null,
    @SerialName("house_number")
    val houseNumber: String? = null,
    @SerialName("pen_name")
    val penName: String? = null,
    @SerialName("submitted_at")
    val submittedAt: String? = null,
    @SerialName("submitted_fields")
    val submittedFields: List<TaskSubmittedFieldApiRow> = emptyList(),
    @SerialName("biosecurity_cleared")
    val biosecurityCleared: Boolean = false
)

@Serializable
data class ApiMessageResponse(
    @SerialName("success")
    val success: Boolean? = null,
    @SerialName("message")
    val message: String? = null
)

data class TaskCounts(
    val pending: Int = 0,
    val forApproval: Int = 0,
    val completed: Int = 0
)

data class TaskPageResult(
    val tasks: List<TaskItem>,
    val counts: TaskCounts,
    val hasMore: Boolean
)

data class CachedTaskPage(
    val tasks: List<TaskItem>,
    val counts: TaskCounts,
    val page: Int,
    val hasMore: Boolean,
    val cachedAtMillis: Long
)

class TaskBackendService(
    private val baseUrl: String = ApiConfig.BASE_URL
) {
    private val httpClient = HttpClient(Android)

    private val json = Json {
        ignoreUnknownKeys = true
    }

    companion object {
        private val taskCache = mutableMapOf<String, CachedTaskPage>()

        fun clearTaskCache(employeeId: Int? = null) {
            if (employeeId == null) {
                taskCache.clear()
                return
            }

            taskCache.keys
                .filter { it.startsWith("$employeeId|") }
                .forEach { taskCache.remove(it) }
        }
    }

    fun getCachedTaskPage(
        employeeId: Int,
        status: TaskStatus
    ): CachedTaskPage? {
        return taskCache[cacheKey(employeeId, status)]
    }

    suspend fun getTasksForFlockman(
        accessToken: String,
        employeeId: Int,
        status: TaskStatus,
        page: Int = 1,
        perPage: Int = 10,
        forceRefresh: Boolean = false
    ): Result<TaskPageResult> {
        return withContext(Dispatchers.IO) {
            runCatching {
                if (forceRefresh && page == 1) {
                    taskCache.remove(cacheKey(employeeId, status))
                }

                val requestBody = FlockmanTasksRequest(
                    status = status.toApiStatus(),
                    page = page,
                    perPage = perPage
                )

                val responseText = httpClient.post("$baseUrl/api/mobile/tasks") {
                    contentType(ContentType.Application.Json)
                    accept(ContentType.Application.Json)
                    header(HttpHeaders.Authorization, "Bearer $accessToken")
                    setBody(json.encodeToString(requestBody))
                }.mobileBodyAsText()

                val parsed: JsonElement = json.parseToJsonElement(responseText)

                if (parsed is JsonObject && parsed["tasks"] == null) {
                    val errorResponse = json.decodeFromJsonElement<LaravelErrorResponse>(parsed)
                    error(errorResponse.message ?: "Failed to load tasks.")
                }

                val response = json.decodeFromJsonElement<FlockmanTasksResponse>(parsed)
                val pageItems = response.tasks.map { it.toTaskItem() }

                val counts = TaskCounts(
                    pending = response.counts.pending,
                    forApproval = response.counts.forApproval,
                    completed = response.counts.completed
                )

                val key = cacheKey(employeeId, status)
                val existingCache = taskCache[key]
                val mergedTasks = if (page == 1 || existingCache == null) {
                    pageItems
                } else {
                    (existingCache.tasks + pageItems).distinctBy { it.id }
                }

                taskCache[key] = CachedTaskPage(
                    tasks = mergedTasks,
                    counts = counts,
                    page = page,
                    hasMore = response.pagination.hasMore,
                    cachedAtMillis = System.currentTimeMillis()
                )

                TaskPageResult(
                    tasks = pageItems,
                    counts = counts,
                    hasMore = response.pagination.hasMore
                )
            }
        }
    }

    suspend fun getSubmittedTaskFields(
        accessToken: String,
        taskId: Int
    ): Result<List<TaskSubmittedField>> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val responseText = httpClient.post("$baseUrl/api/mobile/tasks/submitted-detail") {
                    contentType(ContentType.Application.Json)
                    accept(ContentType.Application.Json)
                    header(HttpHeaders.Authorization, "Bearer $accessToken")
                    setBody(json.encodeToString(SubmittedTaskDetailRequest(taskId = taskId)))
                }.mobileBodyAsText()

                val parsed: JsonElement = json.parseToJsonElement(responseText)

                if (parsed is JsonObject && parsed["submitted_fields"] == null) {
                    val errorResponse = json.decodeFromJsonElement<LaravelErrorResponse>(parsed)
                    error(errorResponse.message ?: "Failed to load submitted task details.")
                }

                val response = json.decodeFromJsonElement<SubmittedTaskDetailResponse>(parsed)

                response.submittedFields.map {
                    TaskSubmittedField(
                        label = it.label,
                        value = it.value.orEmpty()
                    )
                }
            }
        }
    }

    suspend fun checkTaskAccess(
        accessToken: String,
        taskId: Int
    ): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val requestBody = TaskAccessCheckRequest(taskId = taskId)

                val responseText = httpClient.post("$baseUrl/api/mobile/tasks/access-check") {
                    contentType(ContentType.Application.Json)
                    accept(ContentType.Application.Json)
                    header(HttpHeaders.Authorization, "Bearer $accessToken")
                    setBody(json.encodeToString(requestBody))
                }.mobileBodyAsText()

                val parsed: JsonElement = json.parseToJsonElement(responseText)

                if (parsed is JsonObject && parsed["success"] == null) {
                    val errorResponse = json.decodeFromJsonElement<LaravelErrorResponse>(parsed)
                    error(
                        errorResponse.message
                            ?: "Biosecurity verification is required for this task. Please complete the biosecurity form before opening it."
                    )
                }

                val result = json.decodeFromJsonElement<TaskAccessCheckResponse>(parsed)

                if (result.accessGranted != true) {
                    error(
                        result.message
                            ?: "Biosecurity verification is required for this task. Please complete the biosecurity form before opening it."
                    )
                }

                true
            }
        }
    }

    suspend fun submitTaskForApproval(
        context: Context,
        accessToken: String,
        employeeId: Int,
        taskId: Int,
        notes: String,
        photoUri: Uri? = null
    ): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val uploadedPath = if (photoUri != null) {
                    uploadTaskPhoto(context, accessToken, taskId, photoUri)
                } else {
                    null
                }

                val requestBody = CompleteTaskRequest(
                    taskId = taskId,
                    notes = notes.ifBlank { null },
                    photoPath = uploadedPath
                )

                val responseText = httpClient.post("$baseUrl/api/mobile/tasks/submit") {
                    contentType(ContentType.Application.Json)
                    accept(ContentType.Application.Json)
                    header(HttpHeaders.Authorization, "Bearer $accessToken")
                    setBody(json.encodeToString(requestBody))
                }.mobileBodyAsText()

                val parsed: JsonElement = json.parseToJsonElement(responseText)

                if (parsed is JsonObject && parsed["success"] == null) {
                    val errorResponse = json.decodeFromJsonElement<LaravelErrorResponse>(parsed)
                    error(errorResponse.message ?: "Failed to submit task for approval.")
                }

                val result = json.decodeFromJsonElement<ApiMessageResponse>(parsed)
                val success = result.success == true

                if (success) {
                    clearTaskCache(employeeId)
                }

                success
            }
        }
    }

    private suspend fun uploadTaskPhoto(
        context: Context,
        accessToken: String,
        taskId: Int,
        photoUri: Uri
    ): String {
        val compressedPhotoUri = ImageCompressionUtils.compressImageForUpload(context, photoUri)
        val mimeType = "image/jpeg"

        val signedUrlResponseText = httpClient.post("$baseUrl/api/mobile/tasks/photo-upload-url") {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $accessToken")
            setBody(
                json.encodeToString(
                    CreateTaskPhotoUploadUrlRequest(
                        taskId = taskId,
                        mimeType = mimeType
                    )
                )
            )
        }.mobileBodyAsText()

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
                uri = compressedPhotoUri
            )

        return path
    }

    private fun cacheKey(employeeId: Int, status: TaskStatus): String {
        return "$employeeId|${status.name}"
    }

    private fun TaskStatus.toApiStatus(): String {
        return when (this) {
            TaskStatus.PENDING -> "Pending"
            TaskStatus.FOR_APPROVAL -> "For Approval"
            TaskStatus.COMPLETED -> "Completed"
        }
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

        return TaskItem(
            id = taskId,
            title = taskType,
            description = detailedTask?.takeIf { it.isNotBlank() } ?: "No task details provided.",
            houseId = houseId,
            penNumber = penNumber,
            houseLabel = houseNumber ?: "-",
            penLabel = penName ?: "-",
            assignedLabel = timeAssigned?.takeIf { it.isNotBlank() }?.let { formatTaskTimestamp(it) } ?: "",
            finishByLabel = finishBy?.takeIf { it.isNotBlank() }?.let { "Finish by: ${formatTaskTimestamp(it)}" } ?: "",
            submittedLabel = submittedAt?.takeIf { it.isNotBlank() }?.let { "Submitted: ${formatTaskTimestamp(it)}" }
                ?: timeCompleted?.takeIf { it.isNotBlank() }?.let { "Submitted: ${formatTaskTimestamp(it)}" }
                ?: "",
            completedLabel = timeCompleted?.takeIf { it.isNotBlank() }?.let { "Completed: ${formatTaskTimestamp(it)}" } ?: "",
            priority = priorityEnum,
            status = statusEnum,
            notes = notes ?: "",
            hasPhoto = !photoUrl.isNullOrBlank(),
            photoUrl = photoUrl,
            submittedFields = submittedFields.map {
                TaskSubmittedField(
                    label = it.label,
                    value = it.value.orEmpty()
                )
            },
            biosecurityCleared = biosecurityCleared
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