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

@Serializable
data class HouseRpcRow(
    @SerialName("id")
    val id: Long,
    @SerialName("house_number")
    val houseNumber: String? = null,
    @SerialName("number_of_pens")
    val numberOfPens: Long? = null
)

@Serializable
data class SubmitPopulationRequest(
    @SerialName("p_house_id")
    val houseId: Long,
    @SerialName("p_pen_name")
    val penName: String,
    @SerialName("p_eggs_hatched")
    val eggsHatched: Int,
    @SerialName("p_mortality")
    val mortality: Int
)

data class HouseOption(
    val id: Long,
    val houseNumber: String,
    val numberOfPens: Int
)

class PopulationBackendService(
    private val baseUrl: String = SupabaseConfig.SUPABASE_URL,
    private val publishableKey: String = SupabaseConfig.SUPABASE_PUBLISHABLE_KEY
) {
    private val httpClient = HttpClient(Android)

    private val json = Json {
        ignoreUnknownKeys = true
    }

    suspend fun getHouses(): Result<List<HouseOption>> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val responseText =
                    httpClient.post("$baseUrl/rest/v1/rpc/mobile_get_houses") {
                        header("apikey", publishableKey)
                        header("Authorization", "Bearer $publishableKey")
                        contentType(ContentType.Application.Json)
                        setBody("{}")
                    }.bodyAsText()

                val parsed: JsonElement = json.parseToJsonElement(responseText)

                if (parsed is JsonObject && parsed["message"] != null) {
                    val errorResponse = json.decodeFromJsonElement<SupabaseErrorResponse>(parsed)
                    error(errorResponse.message ?: "Failed to load houses.")
                }

                json.decodeFromJsonElement<List<HouseRpcRow>>(parsed).map {
                    HouseOption(
                        id = it.id,
                        houseNumber = it.houseNumber?.ifBlank { "Unknown" } ?: "Unknown",
                        numberOfPens = (it.numberOfPens ?: 0L).toInt()
                    )
                }
            }
        }
    }

    suspend fun submitPopulation(
        houseId: Long,
        penNumber: String,
        eggsHatched: Int,
        mortality: Int
    ): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val requestBody = SubmitPopulationRequest(
                    houseId = houseId,
                    penName = "Pen $penNumber",
                    eggsHatched = eggsHatched,
                    mortality = mortality
                )

                val responseText =
                    httpClient.post("$baseUrl/rest/v1/rpc/mobile_submit_population") {
                        header("apikey", publishableKey)
                        header("Authorization", "Bearer $publishableKey")
                        contentType(ContentType.Application.Json)
                        setBody(json.encodeToString(requestBody))
                    }.bodyAsText()

                val parsed: JsonElement = json.parseToJsonElement(responseText)

                if (parsed is JsonObject && parsed["message"] != null) {
                    val errorResponse = json.decodeFromJsonElement<SupabaseErrorResponse>(parsed)
                    error(errorResponse.message ?: "Failed to submit population data.")
                }

                parsed.toString().contains("true")
            }
        }
    }

    fun buildPenOptions(selectedHouse: HouseOption?): List<String> {
        val count = selectedHouse?.numberOfPens ?: 0
        return (1..count).map { it.toString() }
    }
}
