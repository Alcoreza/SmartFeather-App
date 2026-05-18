package com.example.smartfeather

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Home
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement

@Serializable
data class DashboardStatApiRow(
    @SerialName("title") val title: String,
    @SerialName("value") val value: String,
    @SerialName("icon_key") val iconKey: String,
    @SerialName("bg_color") val bgColor: String,
    @SerialName("icon_bg") val iconBg: String
)

@Serializable
data class GaugeApiRow(
    @SerialName("label") val label: String,
    @SerialName("value") val value: Float,
    @SerialName("unit") val unit: String,
    @SerialName("min") val min: Float,
    @SerialName("max") val max: Float,
    @SerialName("color") val color: String
)

@Serializable
data class ResourceApiRow(
    @SerialName("label") val label: String,
    @SerialName("value") val value: Float,
    @SerialName("unit") val unit: String,
    @SerialName("max") val max: Float,
    @SerialName("color") val color: String
)

@Serializable
data class QuickAccessApiRow(
    @SerialName("title") val title: String,
    @SerialName("icon_key") val iconKey: String,
    @SerialName("tint") val tint: String,
    @SerialName("action_key") val actionKey: String
)

@Serializable
data class DashboardApiResponse(
    @SerialName("success") val success: Boolean? = null,
    @SerialName("welcome_text") val welcomeText: String? = null,
    @SerialName("overview_date_label") val overviewDateLabel: String? = null,
    @SerialName("stats") val stats: List<DashboardStatApiRow> = emptyList(),
    @SerialName("gauges") val gauges: List<GaugeApiRow> = emptyList(),
    @SerialName("resources") val resources: List<ResourceApiRow> = emptyList(),
    @SerialName("pending_tasks") val pendingTasks: String? = null,
    @SerialName("quick_access") val quickAccess: List<QuickAccessApiRow> = emptyList()
)

class DashboardBackendService(
    private val baseUrl: String = ApiConfig.BASE_URL
) {
    private val httpClient = HttpClient(Android)
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun getDashboard(employeeId: Int): Result<DashboardUiState> = withContext(Dispatchers.IO) {
        runCatching {
            val responseText = httpClient.get("$baseUrl/api/mobile/dashboard?employee_id=$employeeId") {
                accept(ContentType.Application.Json)
            }.bodyAsText()

            val parsed = json.parseToJsonElement(responseText)
            if (parsed is JsonObject && parsed["success"] == null) {
                val error = json.decodeFromJsonElement<LaravelErrorResponse>(parsed)
                error(error.message ?: "Failed to load dashboard.")
            }

            val response = json.decodeFromJsonElement<DashboardApiResponse>(parsed)

            DashboardUiState(
                welcomeText = response.welcomeText ?: "Welcome!",
                overviewDateLabel = response.overviewDateLabel ?: "",
                stats = response.stats.map {
                    DashboardStat(
                        title = it.title,
                        value = it.value,
                        icon = dashboardStatIcon(it.iconKey),
                        bgColor = parseColor(it.bgColor),
                        iconBg = parseColor(it.iconBg)
                    )
                },
                gauges = response.gauges.map {
                    GaugeData(
                        label = it.label,
                        value = it.value,
                        unit = it.unit,
                        min = it.min,
                        max = it.max,
                        color = parseColor(it.color)
                    )
                },
                resources = response.resources.map {
                    ResourceData(
                        label = it.label,
                        value = it.value,
                        unit = it.unit,
                        max = it.max,
                        color = parseColor(it.color)
                    )
                },
                pendingTasks = response.pendingTasks ?: "0",
                quickAccess = response.quickAccess.map {
                    QuickAccessItem(
                        title = it.title,
                        icon = quickAccessIcon(it.iconKey),
                        tint = parseColor(it.tint),
                        actionKey = it.actionKey
                    )
                }
            )
        }
    }

    private fun parseColor(hex: String): Color {
        return try {
            Color(android.graphics.Color.parseColor(hex))
        } catch (_: Exception) {
            Color.Black
        }
    }

    private fun dashboardStatIcon(key: String): ImageVector {
        return when (key) {
            "birds" -> Icons.Outlined.Home
            "eggs" -> Icons.Outlined.CheckCircle
            "mortalities" -> Icons.Outlined.AccountCircle
            else -> Icons.Outlined.CheckCircle
        }
    }

    private fun quickAccessIcon(key: String): ImageVector {
        return when (key) {
            "population" -> Icons.Outlined.CheckCircle
            "feeds" -> Icons.AutoMirrored.Outlined.List
            "biosecurity" -> Icons.Outlined.Edit
            else -> Icons.Outlined.Edit
        }
    }
}
