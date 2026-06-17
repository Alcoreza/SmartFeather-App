package com.example.smartfeather

import android.content.Context
import android.net.Uri
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.storage.uploadToSignedUrl
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
data class VisitorTimeInRequest(
    @SerialName("date")
    val date: String,
    @SerialName("time_in")
    val timeIn: String,
    @SerialName("name")
    val name: String,
    @SerialName("purpose")
    val purpose: String,
    @SerialName("foot_bath")
    val footBath: Boolean,
    @SerialName("sanitation")
    val sanitation: Boolean,
    @SerialName("ppe")
    val ppe: Boolean,
    @SerialName("photo_path")
    val photoPath: String? = null
)

@Serializable
data class VisitorTimeOutRequest(
    @SerialName("visitor_log_id")
    val visitorLogId: Int,
    @SerialName("time_out")
    val timeOut: String
)

@Serializable
data class CreateVisitorPhotoUploadUrlRequest(
    @SerialName("mime_type")
    val mimeType: String
)

@Serializable
data class VisitorPhotoUploadUrlResponse(
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
data class VisitorApiMessageResponse(
    @SerialName("success")
    val success: Boolean? = null,
    @SerialName("message")
    val message: String? = null
)

@Serializable
data class OpenVisitorApiRow(
    @SerialName("id")
    val id: Int,
    @SerialName("date")
    val date: String? = null,
    @SerialName("time_in")
    val timeIn: String? = null,
    @SerialName("name")
    val name: String? = null,
    @SerialName("purpose")
    val purpose: String? = null,
    @SerialName("foot_bath")
    val footBath: Boolean? = null,
    @SerialName("sanitation")
    val sanitation: Boolean? = null,
    @SerialName("ppe")
    val ppe: Boolean? = null,
    @SerialName("photo_url")
    val photoUrl: String? = null
)

@Serializable
data class OpenVisitorsResponse(
    @SerialName("success")
    val success: Boolean? = null,
    @SerialName("visitors")
    val visitors: List<OpenVisitorApiRow> = emptyList(),
    @SerialName("message")
    val message: String? = null
)

data class OpenVisitorUiState(
    val id: Int,
    val date: String,
    val timeIn: String,
    val name: String,
    val purpose: String,
    val footBath: Boolean,
    val sanitation: Boolean,
    val ppe: Boolean,
    val photoUrl: String?
)

class VisitorBackendService(
    private val baseUrl: String = ApiConfig.BASE_URL
) {
    private val httpClient = HttpClient(Android)

    private val json = Json {
        ignoreUnknownKeys = true
    }

    suspend fun submitVisitorTimeIn(
        context: Context,
        employeeId: Int,
        accessToken: String = "",
        date: String,
        timeIn: String,
        name: String,
        purpose: String,
        footBath: Boolean,
        sanitation: Boolean,
        ppe: Boolean,
        photoUri: Uri? = null
    ): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val uploadedPath = if (photoUri != null) {
                    uploadVisitorPhoto(context, accessToken, photoUri)
                } else {
                    null
                }

                val requestBody = VisitorTimeInRequest(
                    date = date,
                    timeIn = timeIn,
                    name = name,
                    purpose = purpose,
                    footBath = footBath,
                    sanitation = sanitation,
                    ppe = ppe,
                    photoPath = uploadedPath
                )

                val responseText = httpClient.post("$baseUrl/api/mobile/visitor/time-in") {
                    contentType(ContentType.Application.Json)
                    accept(ContentType.Application.Json)
                    header(HttpHeaders.Authorization, "Bearer $accessToken")
                    setBody(json.encodeToString(requestBody))
                }.mobileBodyAsText()

                val parsed: JsonElement = json.parseToJsonElement(responseText)

                if (parsed is JsonObject && parsed["success"] == null) {
                    val errorResponse = json.decodeFromJsonElement<LaravelErrorResponse>(parsed)
                    error(errorResponse.message ?: "Failed to submit visitor time in.")
                }

                val response = json.decodeFromJsonElement<VisitorApiMessageResponse>(parsed)
                response.success == true
            }
        }
    }

    suspend fun getOpenVisitors(
        employeeId: Int,
        accessToken: String = ""
    ): Result<List<OpenVisitorUiState>> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val responseText = httpClient.get("$baseUrl/api/mobile/visitor/open") {
                    accept(ContentType.Application.Json)
                    header(HttpHeaders.Authorization, "Bearer $accessToken")
                }.mobileBodyAsText()

                val parsed: JsonElement = json.parseToJsonElement(responseText)

                if (parsed is JsonObject && parsed["success"] == null) {
                    val errorResponse = json.decodeFromJsonElement<LaravelErrorResponse>(parsed)
                    error(errorResponse.message ?: "Failed to load open visitors.")
                }

                val response = json.decodeFromJsonElement<OpenVisitorsResponse>(parsed)

                response.visitors.map {
                    OpenVisitorUiState(
                        id = it.id,
                        date = it.date.orEmpty(),
                        timeIn = it.timeIn.orEmpty(),
                        name = it.name.orEmpty(),
                        purpose = it.purpose.orEmpty(),
                        footBath = it.footBath == true,
                        sanitation = it.sanitation == true,
                        ppe = it.ppe == true,
                        photoUrl = it.photoUrl
                    )
                }
            }
        }
    }

    suspend fun submitVisitorTimeOut(
        employeeId: Int,
        accessToken: String = "",
        visitorLogId: Int,
        timeOut: String
    ): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val requestBody = VisitorTimeOutRequest(
                    visitorLogId = visitorLogId,
                    timeOut = timeOut
                )

                val responseText = httpClient.post("$baseUrl/api/mobile/visitor/time-out") {
                    contentType(ContentType.Application.Json)
                    accept(ContentType.Application.Json)
                    header(HttpHeaders.Authorization, "Bearer $accessToken")
                    setBody(json.encodeToString(requestBody))
                }.mobileBodyAsText()

                val parsed: JsonElement = json.parseToJsonElement(responseText)

                if (parsed is JsonObject && parsed["success"] == null) {
                    val errorResponse = json.decodeFromJsonElement<LaravelErrorResponse>(parsed)
                    error(errorResponse.message ?: "Failed to submit visitor time out.")
                }

                val response = json.decodeFromJsonElement<VisitorApiMessageResponse>(parsed)
                response.success == true
            }
        }
    }

    private suspend fun uploadVisitorPhoto(
        context: Context,
        accessToken: String,
        photoUri: Uri
    ): String {
        val compressedPhotoUri = ImageCompressionUtils.compressImageForUpload(context, photoUri)
        val mimeType = "image/jpeg"

        val signedUrlResponseText = httpClient.post("$baseUrl/api/mobile/visitor/photo-upload-url") {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $accessToken")
            setBody(
                json.encodeToString(
                    CreateVisitorPhotoUploadUrlRequest(
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

        val uploadInfo = json.decodeFromJsonElement<VisitorPhotoUploadUrlResponse>(signedUrlParsed)
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
}