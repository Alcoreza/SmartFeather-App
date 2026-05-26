package com.example.smartfeather

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.put
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
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Serializable
data class MobileProfileResponse(
    @SerialName("employee_id")
    val employeeId: Int,
    @SerialName("username")
    val username: String? = null,
    @SerialName("first_name")
    val firstName: String? = null,
    @SerialName("middle_name")
    val middleName: String? = null,
    @SerialName("last_name")
    val lastName: String? = null,
    @SerialName("suffix")
    val suffix: String? = null,
    @SerialName("role")
    val role: String? = null,
    @SerialName("phone_number")
    val phoneNumber: String? = null,
    @SerialName("address")
    val address: String? = null,
    @SerialName("birthday")
    val birthday: String? = null,
    @SerialName("gender")
    val gender: String? = null
)

@Serializable
data class UpdateProfileRequest(
    @SerialName("phone_number")
    val phoneNumber: String,
    @SerialName("address")
    val address: String?
)

@Serializable
data class UpdateProfileResponse(
    @SerialName("success")
    val success: Boolean? = null,
    @SerialName("message")
    val message: String? = null,
    @SerialName("profile")
    val profile: MobileProfileResponse? = null
)

data class FlockmanProfileUiState(
    val firstName: String,
    val middleName: String,
    val lastName: String,
    val suffix: String,
    val username: String,
    val role: String,
    val birthday: String,
    val phoneNumber: String,
    val gender: String,
    val address: String
)

class ProfileBackendService(
    private val baseUrl: String = ApiConfig.BASE_URL
) {
    private val httpClient = HttpClient(Android)

    private val json = Json {
        ignoreUnknownKeys = true
    }

    suspend fun getFlockmanProfile(employeeId: Int): Result<FlockmanProfileUiState> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val responseText = httpClient.get("$baseUrl/api/mobile/profile/$employeeId") {
                    accept(ContentType.Application.Json)
                }.bodyAsText()

                val parsed: JsonElement = json.parseToJsonElement(responseText)

                if (parsed is JsonObject && parsed["employee_id"] == null) {
                    val errorResponse = json.decodeFromJsonElement<LaravelErrorResponse>(parsed)
                    error(errorResponse.message ?: "Failed to load profile.")
                }

                json.decodeFromJsonElement<MobileProfileResponse>(parsed).toUiState()
            }
        }
    }

    suspend fun updateFlockmanProfile(
        employeeId: Int,
        phoneNumber: String,
        address: String
    ): Result<Pair<String, FlockmanProfileUiState>> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val responseText = httpClient.put("$baseUrl/api/mobile/profile/$employeeId") {
                    contentType(ContentType.Application.Json)
                    accept(ContentType.Application.Json)
                    setBody(
                        json.encodeToString(
                            UpdateProfileRequest(
                                phoneNumber = phoneNumber,
                                address = address.ifBlank { "" }
                            )
                        )
                    )
                }.bodyAsText()

                val parsed: JsonElement = json.parseToJsonElement(responseText)

                if (parsed is JsonObject && parsed["success"] == null) {
                    val errorResponse = json.decodeFromJsonElement<LaravelErrorResponse>(parsed)
                    error(errorResponse.message ?: "Failed to update profile.")
                }

                val response = json.decodeFromJsonElement<UpdateProfileResponse>(parsed)
                val updatedProfile = response.profile?.toUiState()
                    ?: error("Updated profile was not returned.")

                (response.message ?: "Profile updated successfully.") to updatedProfile
            }
        }
    }
}

private fun MobileProfileResponse.toUiState(): FlockmanProfileUiState {
    return FlockmanProfileUiState(
        firstName = firstName.orEmpty(),
        middleName = middleName.orEmpty(),
        lastName = lastName.orEmpty(),
        suffix = suffix.orEmpty(),
        username = username.orEmpty(),
        role = role.orEmpty(),
        birthday = formatBirthday(birthday),
        phoneNumber = phoneNumber.orEmpty(),
        gender = gender.orEmpty(),
        address = address.orEmpty()
    )
}

private fun formatBirthday(value: String?): String {
    if (value.isNullOrBlank()) return ""
    return runCatching {
        LocalDate.parse(value).format(DateTimeFormatter.ofPattern("MM/dd/yyyy", Locale.getDefault()))
    }.getOrElse { value }
}