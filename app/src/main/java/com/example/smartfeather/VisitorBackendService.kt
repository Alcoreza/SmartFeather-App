package com.example.smartfeather

import android.content.Context
import android.net.Uri
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.storage.uploadToSignedUrl
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.request.accept
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
data class VisitorSubmitRequest(
    @SerialName("employee_id")
    val employeeId: Int,
    @SerialName("date")
    val date: String,
    @SerialName("time_in")
    val timeIn: String,
    @SerialName("time_out")
    val timeOut: String,
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
data class CreateVisitorPhotoUploadUrlRequest(
    @SerialName("employee_id")
    val employeeId: Int,
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

class VisitorBackendService(
    private val baseUrl: String = ApiConfig.BASE_URL
) {
    private val httpClient = HttpClient(Android)

    private val json = Json {
        ignoreUnknownKeys = true
    }

    suspend fun submitVisitorLog(
        context: Context,
        employeeId: Int,
        date: String,
        timeIn: String,
        timeOut: String,
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
                    uploadVisitorPhoto(context, employeeId, photoUri)
                } else {
                    null
                }

                val requestBody = VisitorSubmitRequest(
                    employeeId = employeeId,
                    date = date,
                    timeIn = timeIn,
                    timeOut = timeOut,
                    name = name,
                    purpose = purpose,
                    footBath = footBath,
                    sanitation = sanitation,
                    ppe = ppe,
                    photoPath = uploadedPath
                )

                val responseText = httpClient.post("$baseUrl/api/mobile/visitor") {
                    contentType(ContentType.Application.Json)
                    accept(ContentType.Application.Json)
                    setBody(json.encodeToString(requestBody))
                }.bodyAsText()

                val parsed: JsonElement = json.parseToJsonElement(responseText)

                if (parsed is JsonObject && parsed["success"] == null) {
                    val errorResponse = json.decodeFromJsonElement<LaravelErrorResponse>(parsed)
                    error(errorResponse.message ?: "Failed to submit visitor log.")
                }

                val response = json.decodeFromJsonElement<VisitorApiMessageResponse>(parsed)
                response.success == true
            }
        }
    }

    private suspend fun uploadVisitorPhoto(
        context: Context,
        employeeId: Int,
        photoUri: Uri
    ): String {
        val mimeType = context.contentResolver.getType(photoUri) ?: "image/jpeg"

        val signedUrlResponseText = httpClient.post("$baseUrl/api/mobile/visitor/photo-upload-url") {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            setBody(
                json.encodeToString(
                    CreateVisitorPhotoUploadUrlRequest(
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

        val uploadInfo = json.decodeFromJsonElement<VisitorPhotoUploadUrlResponse>(signedUrlParsed)
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
}