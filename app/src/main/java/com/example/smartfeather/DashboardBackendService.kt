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
import io.ktor.http.URLBuilder
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
    @SerialName("color") val color: String,
    @SerialName("recorded_at") val recordedAt: String? = null
)

@Serializable
data class ResourceApiRow(
    @SerialName("label") val label: String,
    @SerialName("value") val value: Float,
    @SerialName("unit") val unit: String,
    @SerialName("max") val max: Float,
    @SerialName("color") val color: String,
    @SerialName("recorded_at") val recordedAt: String? = null
)

@Serializable
data class QuickAccessApiRow(
    @SerialName("title") val title: String,
    @SerialName("icon_key") val iconKey: String,
    @SerialName("tint") val tint: String,
    @SerialName("action_key") val actionKey: String
)

@Serializable
data class SensorFilterOptionApiRow(
    @SerialName("house_id") val houseId: Int,
    @SerialName("house_number") val houseNumber: String,
    @SerialName("pen_id") val penId: Int,
    @SerialName("pen_name") val penName: String
)

@Serializable
data class SensorFilterApiRow(
    @SerialName("selected_house_id") val selectedHouseId: Int? = null,
    @SerialName("selected_pen_id") val selectedPenId: Int? = null,
    @SerialName("options") val options: List<SensorFilterOptionApiRow> = emptyList()
)

@Serializable
data class DashboardApiResponse(
    @SerialName("success") val success: Boolean? = null,
    @SerialName("welcome_text") val welcomeText: String? = null,
    @SerialName("overview_date_label") val overviewDateLabel: String? = null,
    @SerialName("stats") val stats: List<DashboardStatApiRow> = emptyList(),
    @SerialName("environment_filter") val environmentFilter: SensorFilterApiRow? = null,
    @SerialName("resource_filter") val resourceFilter: SensorFilterApiRow? = null,
    @SerialName("gauges") val gauges: List<GaugeApiRow> = emptyList(),
    @SerialName("resources") val resources: List<ResourceApiRow> = emptyList(),
    @SerialName("pending_tasks") val pendingTasks: String? = null,
    @SerialName("quick_access") val quickAccess: List<QuickAccessApiRow> = emptyList()
)

data class SensorFilterOption(
    val houseId: Int,
    val houseNumber: String,
    val penId: Int,
    val penName: String
) {
    val displayLabel: String
        get() = "$houseNumber | $penName"
}

data class SensorFilterState(
    val selectedHouseId: Int?,
    val selectedPenId: Int?,
    val options: List<SensorFilterOption>
)

data class DashboardUiState(
    val welcomeText: String,
    val overviewDateLabel: String,
    val stats: List<DashboardStat>,
    val gauges: List<GaugeData>,
    val resources: List<ResourceData>,
    val environmentFilter: SensorFilterState,
    val resourceFilter: SensorFilterState,
    val pendingTasks: String,
    val quickAccess: List<QuickAccessItem>
)

class DashboardBackendService(
    private val baseUrl: String = ApiConfig.BASE_URL
) {
    private val httpClient = HttpClient(Android)
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun getDashboard(
        employeeId: Int,
        environmentHouseId: Int? = null,
        environmentPenId: Int? = null,
        resourceHouseId: Int? = null,
        resourcePenId: Int? = null
    ): Result<DashboardUiState> = withContext(Dispatchers.IO) {
        runCatching {
            val url = URLBuilder("$baseUrl/api/mobile/dashboard").apply {
                parameters.append("employee_id", employeeId.toString())
                environmentHouseId?.let { parameters.append("environment_house_id", it.toString()) }
                environmentPenId?.let { parameters.append("environment_pen_id", it.toString()) }
                resourceHouseId?.let { parameters.append("resource_house_id", it.toString()) }
                resourcePenId?.let { parameters.append("resource_pen_id", it.toString()) }
            }.buildString()

            val responseText = httpClient.get(url) {
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
                        color = parseColor(it.color),
                        recordedAt = it.recordedAt
                    )
                },
                resources = response.resources.map {
                    ResourceData(
                        label = it.label,
                        value = it.value,
                        unit = it.unit,
                        max = it.max,
                        color = parseColor(it.color),
                        recordedAt = it.recordedAt
                    )
                },
                environmentFilter = response.environmentFilter.toUiState(),
                resourceFilter = response.resourceFilter.toUiState(),
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

    private fun SensorFilterApiRow?.toUiState(): SensorFilterState {
        return SensorFilterState(
            selectedHouseId = this?.selectedHouseId,
            selectedPenId = this?.selectedPenId,
            options = this?.options?.map {
                SensorFilterOption(
                    houseId = it.houseId,
                    houseNumber = it.houseNumber,
                    penId = it.penId,
                    penName = it.penName
                )
            } ?: emptyList()
        )
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