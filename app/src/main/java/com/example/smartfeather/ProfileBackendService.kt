package com.example.smartfeather

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
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

data class FlockmanProfileUiState(
    val firstName: String,
    val middleName: String,
    val lastName: String,
    val suffix: String,
    val employeeId: String,
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

                val profile = json.decodeFromJsonElement<MobileProfileResponse>(parsed)

                FlockmanProfileUiState(
                    firstName = profile.firstName.orEmpty(),
                    middleName = profile.middleName.orEmpty(),
                    lastName = profile.lastName.orEmpty(),
                    suffix = profile.suffix.orEmpty(),
                    employeeId = profile.employeeId.toString(),
                    role = profile.role.orEmpty(),
                    birthday = formatBirthday(profile.birthday),
                    phoneNumber = profile.phoneNumber.orEmpty(),
                    gender = profile.gender.orEmpty(),
                    address = profile.address.orEmpty()
                )
            }
        }
    }
}

private fun formatBirthday(value: String?): String {
    if (value.isNullOrBlank()) return ""
    return runCatching {
        LocalDate.parse(value).format(DateTimeFormatter.ofPattern("MM/dd/yyyy", Locale.getDefault()))
    }.getOrElse { value }
}
