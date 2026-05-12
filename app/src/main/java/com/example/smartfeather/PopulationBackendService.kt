package com.example.smartfeather

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.request.accept
import io.ktor.client.request.get
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
data class PopulationHouseApiRow(
    @SerialName("id")
    val id: Long,
    @SerialName("house_number")
    val houseNumber: String? = null,
    @SerialName("number_of_pens")
    val numberOfPens: Long? = null
)

@Serializable
data class PopulationSubmitRequest(
    @SerialName("house_id")
    val houseId: Long,
    @SerialName("pen_name")
    val penName: String,
    @SerialName("eggs_hatched")
    val eggsHatched: Int,
    @SerialName("mortality")
    val mortality: Int,
    @SerialName("recorded_at")
    val recordedAt: String
)

@Serializable
data class PopulationApiMessageResponse(
    @SerialName("success")
    val success: Boolean? = null,
    @SerialName("message")
    val message: String? = null
)

data class PopulationHouseOption(
    val id: Long,
    val houseNumber: String,
    val numberOfPens: Int
)

class PopulationBackendService(
    private val baseUrl: String = ApiConfig.BASE_URL
) {
    private val httpClient = HttpClient(Android)

    private val json = Json {
        ignoreUnknownKeys = true
    }

    suspend fun getHouses(): Result<List<PopulationHouseOption>> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val responseText = httpClient.get("$baseUrl/api/mobile/houses") {
                    accept(ContentType.Application.Json)
                }.bodyAsText()

                val parsed: JsonElement = json.parseToJsonElement(responseText)

                if (parsed is JsonObject && parsed["message"] != null) {
                    val errorResponse = json.decodeFromJsonElement<LaravelErrorResponse>(parsed)
                    error(errorResponse.message ?: "Failed to load houses.")
                }

                json.decodeFromJsonElement<List<PopulationHouseApiRow>>(parsed).map {
                    PopulationHouseOption(
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
        mortality: Int,
        recordedAt: String
    ): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val requestBody = PopulationSubmitRequest(
                    houseId = houseId,
                    penName = "Pen $penNumber",
                    eggsHatched = eggsHatched,
                    mortality = mortality,
                    recordedAt = recordedAt
                )

                val responseText = httpClient.post("$baseUrl/api/mobile/population") {
                    contentType(ContentType.Application.Json)
                    accept(ContentType.Application.Json)
                    setBody(json.encodeToString(requestBody))
                }.bodyAsText()

                val parsed: JsonElement = json.parseToJsonElement(responseText)

                if (parsed is JsonObject && parsed["success"] == null) {
                    val errorResponse = json.decodeFromJsonElement<LaravelErrorResponse>(parsed)
                    error(errorResponse.message ?: "Failed to submit population data.")
                }

                val result = json.decodeFromJsonElement<PopulationApiMessageResponse>(parsed)
                result.success == true
            }
        }
    }

    fun buildPenOptions(selectedHouse: PopulationHouseOption?): List<String> {
        val count = selectedHouse?.numberOfPens ?: 0
        return (1..count).map { it.toString() }
    }
}
