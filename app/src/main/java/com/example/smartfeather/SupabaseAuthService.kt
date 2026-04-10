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

object SupabaseConfig {
    const val SUPABASE_URL = "https://fgtqbfmnehnzwanzqzyb.supabase.co"
    const val SUPABASE_PUBLISHABLE_KEY = "sb_publishable_hWs6UmOa8zQFICiImm4qvw_1HlUSJ1T"
}

@Serializable
data class MobileLoginRequest(
    @SerialName("p_employee_id")
    val employeeId: Int,
    @SerialName("p_password")
    val password: String
)

@Serializable
data class MobileLoginResponse(
    @SerialName("employee_id")
    val employeeId: Int,
    @SerialName("first_name")
    val firstName: String,
    @SerialName("last_name")
    val lastName: String,
    @SerialName("role")
    val role: String
)

@Serializable
data class SupabaseErrorResponse(
    val code: String? = null,
    val details: String? = null,
    val hint: String? = null,
    val message: String? = null
)

class SupabaseAuthService(
    private val baseUrl: String = SupabaseConfig.SUPABASE_URL,
    private val publishableKey: String = SupabaseConfig.SUPABASE_PUBLISHABLE_KEY
) {
    private val httpClient = HttpClient(Android)

    private val json = Json {
        ignoreUnknownKeys = true
    }

    suspend fun signInFlockman(employeeId: String, password: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val employeeIdValue = employeeId.toIntOrNull()
                    ?: error("Employee ID should contain numbers only.")

                val requestBody = MobileLoginRequest(
                    employeeId = employeeIdValue,
                    password = password
                )

                val responseText = httpClient.post("$baseUrl/rest/v1/rpc/mobile_flockman_login") {
                    header("apikey", publishableKey)
                    header("Authorization", "Bearer $publishableKey")
                    contentType(ContentType.Application.Json)
                    setBody(json.encodeToString(requestBody))
                }.bodyAsText()

                val parsed: JsonElement = json.parseToJsonElement(responseText)

                if (parsed is JsonObject && parsed["message"] != null) {
                    val errorResponse = json.decodeFromJsonElement<SupabaseErrorResponse>(parsed)
                    error(errorResponse.message ?: "Login failed.")
                }

                val users = json.decodeFromJsonElement<List<MobileLoginResponse>>(parsed)
                val user = users.firstOrNull()
                    ?: error("Invalid employee ID or password.")

                if (!user.role.equals("Flockman", ignoreCase = true)) {
                    error("Only Flockman accounts can sign in on mobile.")
                }

                Unit
            }
        }
    }
}
