package com.example.smartfeather

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
data class MobileLoginRequest(
    @SerialName("username")
    val username: String,
    @SerialName("password")
    val password: String
)

@Serializable
data class MobileLoginUser(
    @SerialName("EmployeeId")
    val employeeId: Int,
    @SerialName("FirstName")
    val firstName: String? = null,
    @SerialName("LastName")
    val lastName: String? = null,
    @SerialName("Role")
    val role: String,
    @SerialName("Username")
    val username: String? = null
)

@Serializable
data class MobileLoginResponse(
    @SerialName("message")
    val message: String? = null,
    @SerialName("user")
    val user: MobileLoginUser? = null
)

@Serializable
data class LaravelErrorResponse(
    @SerialName("message")
    val message: String? = null,
    @SerialName("errors")
    val errors: Map<String, List<String>>? = null
)

@Serializable
data class SupabaseErrorResponse(
    val code: String? = null,
    val details: String? = null,
    val hint: String? = null,
    val message: String? = null
)

class SupabaseAuthService(
    private val baseUrl: String = ApiConfig.BASE_URL
) {
    private val httpClient = HttpClient(Android)

    private val json = Json {
        ignoreUnknownKeys = true
    }

    suspend fun signInFlockman(username: String, password: String): Result<Int> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val requestBody = MobileLoginRequest(
                    username = username,
                    password = password
                )

                val responseText = httpClient.post("$baseUrl/api/mobile/login") {
                    contentType(ContentType.Application.Json)
                    accept(ContentType.Application.Json)
                    setBody(json.encodeToString(requestBody))
                }.bodyAsText()

                val parsed: JsonElement = json.parseToJsonElement(responseText)

                if (parsed is JsonObject && parsed["user"] == null) {
                    val errorResponse = json.decodeFromJsonElement<LaravelErrorResponse>(parsed)
                    error(errorResponse.message ?: "Login failed.")
                }

                val loginResponse = json.decodeFromJsonElement<MobileLoginResponse>(parsed)
                val user = loginResponse.user ?: error("Invalid username or password.")

                if (!user.role.equals("Flockman", ignoreCase = true)) {
                    error("Only Flockman accounts can sign in on mobile.")
                }

                user.employeeId
            }
        }
    }
}