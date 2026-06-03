package com.example.smartfeather

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import com.composables.icons.lucide.Bird
import com.composables.icons.lucide.ChevronDown
import com.composables.icons.lucide.ClipboardList
import com.composables.icons.lucide.Droplets
import com.composables.icons.lucide.Egg
import com.composables.icons.lucide.Gauge
import com.composables.icons.lucide.House
import com.composables.icons.lucide.LayoutDashboard
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MapPin
import com.composables.icons.lucide.Pencil
import com.composables.icons.lucide.ShieldCheck
import com.composables.icons.lucide.Skull
import com.composables.icons.lucide.Thermometer
import com.composables.icons.lucide.UserRound
import com.composables.icons.lucide.Wheat
import androidx.compose.ui.graphics.graphicsLayer
import com.composables.icons.lucide.Zap

data class DashboardStat(
    val title: String,
    val value: String,
    val icon: ImageVector,
    val bgColor: Color,
    val iconBg: Color
)

data class GaugeData(
    val label: String,
    val value: Float,
    val unit: String,
    val min: Float,
    val max: Float,
    val color: Color,
    val recordedAt: String? = null
)

data class ResourceData(
    val label: String,
    val value: Float,
    val unit: String,
    val max: Float,
    val color: Color,
    val recordedAt: String? = null
)

data class QuickAccessItem(
    val title: String,
    val icon: ImageVector,
    val tint: Color,
    val actionKey: String
)

private val DashboardPoppins = FontFamily(
    Font(R.font.manrope_extralight, FontWeight.ExtraLight),
    Font(R.font.manrope_light, FontWeight.Light),
    Font(R.font.manrope_regular, FontWeight.Normal),
    Font(R.font.manrope_medium, FontWeight.Medium),
    Font(R.font.manrope_semibold, FontWeight.SemiBold),
    Font(R.font.manrope_bold, FontWeight.Bold),
    Font(R.font.manrope_extrabold, FontWeight.ExtraBold),
    Font(R.font.manrope_variablefont_wght, FontWeight.Black)
)

private val FarmCream = Color(0xFFF6F3EC)
private val FarmSurface = Color(0xFFFFFCF7)
private val FarmSurfaceAlt = Color(0xFFF3EFE7)
private val FarmInk = Color(0xFF121A14)
private val FarmMuted = Color(0xFF677168)
private val FarmLine = Color(0xFFD8D0C3)
private val FarmGreen = Color(0xFF1F7A3A)
private val FarmDeepGreen = Color(0xFF062717)
private val FarmGreenTwo = Color(0xFF155C2D)
private val FarmSoftGreen = Color(0xFFEAF3EC)
private val FarmAmber = Color(0xFFE28622)

fun placeholderDashboardState(): DashboardUiState {
    return DashboardUiState(
        welcomeText = "Welcome!",
        overviewDateLabel = "for 5/11/26",
        stats = listOf(
            DashboardStat("Total Birds", "5462", Lucide.Bird, Color(0xFFFDFDFD), Color(0xFFD92C2C)),
            DashboardStat("Total Eggs", "367", Lucide.Egg, Color(0xFFFDFDFD), Color(0xFFFFA96E)),
            DashboardStat("Mortalities", "25", Lucide.Skull, Color(0xFFFDFDFD), Color(0xFF808080))
        ),
        gauges = listOf(
            GaugeData("Temperature", 11f, "deg", 0f, 40f, Color(0xFF6ABF4B)),
            GaugeData("Ammonia", 15f, "ppm", 0f, 40f, Color(0xFFF4B43A))
        ),
        resources = listOf(
            ResourceData("Feed", 90f, "%", 100f, Color(0xFFC88A3D)),
            ResourceData("Water", 40f, "%", 100f, Color(0xFF3EA7B3))
        ),
        environmentFilter = SensorFilterState(
            selectedHouseId = 1,
            selectedPenId = 1,
            options = listOf(
                SensorFilterOption(1, "House 1", 1, "Pen 1"),
                SensorFilterOption(1, "House 1", 2, "Pen 2")
            )
        ),
        resourceFilter = SensorFilterState(
            selectedHouseId = 2,
            selectedPenId = 3,
            options = listOf(
                SensorFilterOption(2, "House 2", 3, "Pen 3"),
                SensorFilterOption(2, "House 2", 4, "Pen 4")
            )
        ),
        pendingTaskCount = 1,
        pendingTask = PendingTaskSummary(
            title = "Cleaning",
            finishBy = "May 28, 4:30 PM",
            houseLabel = "House 11",
            penLabel = "Pen 25"
        ),
        quickAccess = listOf(
            QuickAccessItem("Population", Lucide.Bird, Color(0xFFD92C2C), "population"),
            QuickAccessItem("Feeds Refill", Lucide.Wheat, Color(0xFFCC8A2D), "feeds_refill"),
            QuickAccessItem("Biosecurity", Lucide.ShieldCheck, Color(0xFF2F8F45), "biosecurity")
        )
    )
}

fun emptyDashboardState(): DashboardUiState {
    return DashboardUiState(
        welcomeText = "",
        overviewDateLabel = "",
        stats = emptyList(),
        gauges = emptyList(),
        resources = emptyList(),
        environmentFilter = SensorFilterState(
            selectedHouseId = null,
            selectedPenId = null,
            options = emptyList()
        ),
        resourceFilter = SensorFilterState(
            selectedHouseId = null,
            selectedPenId = null,
            options = emptyList()
        ),
        pendingTaskCount = 0,
        pendingTask = null,
        quickAccess = listOf(
            QuickAccessItem("Population", Lucide.Bird, Color(0xFFD92C2C), "population"),
            QuickAccessItem("Feeds Refill", Lucide.Wheat, Color(0xFFCC8A2D), "feeds_refill"),
            QuickAccessItem("Biosecurity", Lucide.ShieldCheck, Color(0xFF2F8F45), "biosecurity")
        )
    )
}
private fun gaugeProgress(value: Float, min: Float, max: Float): Float {
    val range = max - min
    if (range <= 0f) return 0f
    return ((value - min) / range).coerceIn(0f, 1f)
}

private fun resourceProgress(value: Float, max: Float): Float {
    if (max <= 0f) return 0f
    return (value / max).coerceIn(0f, 1f)
}

private fun formatValue(value: Float, unit: String): String {
    val display = if (value % 1f == 0f) value.roundToInt().toString() else String.format("%.1f", value)
    return "$display$unit"
}

@Composable
fun DashboardScreen(
    onNavigateToTasks: () -> Unit,
    onNavigateToFarmManagement: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onQuickAccessPopulation: () -> Unit,
    onQuickAccessFeedsRefill: () -> Unit,
    onQuickAccessBiosecurity: () -> Unit,
    uiState: DashboardUiState,
    isLoading: Boolean = false,
    onEnvironmentFilterChange: (SensorFilterOption) -> Unit = {},
    onResourceFilterChange: (SensorFilterOption) -> Unit = {}
) {
    var contentVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(120)
        contentVisible = true
    }

    Scaffold(
        containerColor = FarmCream,
        bottomBar = {
            BottomNavBar(
                onTasksClick = onNavigateToTasks,
                onFarmManagementClick = onNavigateToFarmManagement,
                onProfileClick = onNavigateToProfile
            )
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFFBF8F1), FarmCream, Color(0xFFEDE7DA))
                    )
                )
                .padding(innerPadding)
        ) {
            val isTablet = maxWidth >= 700.dp
            val pagePadding = if (isTablet) 30.dp else 18.dp

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = pagePadding, vertical = 18.dp)
            ) {
                if (isLoading || !contentVisible) {
                    DashboardSkeleton(isTablet = isTablet)
                    Spacer(modifier = Modifier.height(20.dp))
                    return@Column
                }

                AnimatedVisibility(
                    visible = contentVisible,
                    enter = fadeIn(animationSpec = tween(420)) + slideInVertically(
                        animationSpec = tween(420, easing = FastOutSlowInEasing),
                        initialOffsetY = { it / 18 }
                    )
                ) {
                    Column {
                        DashboardCommandCenter(
                            welcomeText = uiState.welcomeText,
                            overviewDateLabel = uiState.overviewDateLabel,
                            pendingTaskCount = uiState.pendingTaskCount,
                            isTablet = isTablet,
                            onTasksClick = onNavigateToTasks
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        FarmOverviewCard(
                            stats = uiState.stats,
                            overviewDateLabel = uiState.overviewDateLabel,
                            isTablet = isTablet
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        PendingTaskBanner(
                            count = uiState.pendingTaskCount,
                            task = uiState.pendingTask,
                            isTablet = isTablet,
                            onClick = onNavigateToTasks
                        )

                        Spacer(modifier = Modifier.height(22.dp))

                        SectionTitle(
                            title = "House Conditions",
                            icon = Lucide.Gauge,
                            isTablet = isTablet
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        MonitoringSection(
                            gauges = uiState.gauges,
                            resources = uiState.resources,
                            environmentFilter = uiState.environmentFilter,
                            resourceFilter = uiState.resourceFilter,
                            isTablet = isTablet,
                            onEnvironmentFilterChange = onEnvironmentFilterChange,
                            onResourceFilterChange = onResourceFilterChange
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        SectionTitle(
                            title = "Quick Access",
                            icon = Lucide.Zap,
                            isTablet = isTablet
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        QuickAccessSection(
                            items = uiState.quickAccess,
                            isTablet = isTablet,
                            onItemClick = { actionKey ->
                                when (actionKey) {
                                    "population" -> onQuickAccessPopulation()
                                    "feeds_refill" -> onQuickAccessFeedsRefill()
                                    "biosecurity" -> onQuickAccessBiosecurity()
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardCommandCenter(
    welcomeText: String,
    overviewDateLabel: String,
    pendingTaskCount: Int,
    isTablet: Boolean,
    onTasksClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(30.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(FarmDeepGreen, Color(0xFF0E4025), FarmGreenTwo)
                )
            )
            .padding(horizontal = if (isTablet) 28.dp else 20.dp, vertical = if (isTablet) 28.dp else 22.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = welcomeText,
                    fontFamily = DashboardPoppins,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = if (isTablet) 32.sp else 27.sp,
                    color = Color.White,
                    lineHeight = if (isTablet) 36.sp else 31.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Farm operations dashboard",
                    fontFamily = DashboardPoppins,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = if (isTablet) 15.sp else 13.sp,
                    color = Color.White.copy(alpha = 0.74f),
                    lineHeight = 18.sp
                )
            }

            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.16f))
                    .border(1.dp, Color.White.copy(alpha = 0.20f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.smartfeather_logo),
                    contentDescription = "SmartFeather logo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                )
            }
        }
    }
}

@Composable
private fun CommandPill(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.12f))
            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(18.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(9.dp)
                .clip(CircleShape)
                .background(Color(0xFF7AF28B))
        )

        Spacer(modifier = Modifier.width(10.dp))

        Column {
            Text(
                text = label,
                fontFamily = DashboardPoppins,
                fontWeight = FontWeight.Light,
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.72f)
            )

            Text(
                text = value,
                fontFamily = DashboardPoppins,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp,
                color = Color.White
            )
        }
    }
}

@Composable
private fun SectionTitle(
    title: String,
    icon: ImageVector,
    isTablet: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(FarmSoftGreen),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = FarmGreen,
                modifier = Modifier.size(21.dp)
            )
        }

        Spacer(modifier = Modifier.width(11.dp))

        Text(
            text = title,
            fontFamily = DashboardPoppins,
            fontSize = if (isTablet) 22.sp else 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = FarmInk
        )
    }
}

@Composable
private fun FarmOverviewCard(
    stats: List<DashboardStat>,
    overviewDateLabel: String,
    isTablet: Boolean
) {
    val hasStats = stats.isNotEmpty()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(FarmSurface)
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = "Daily Production",
                fontFamily = DashboardPoppins,
                fontSize = if (isTablet) 22.sp else 19.sp,
                fontWeight = FontWeight.ExtraBold,
                color = FarmInk
            )

            if (overviewDateLabel.isNotBlank()) {
                Text(
                    text = overviewDateLabel,
                    fontFamily = DashboardPoppins,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = FarmMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (!hasStats) {
            DashboardEmptyState(
                title = "No production data yet",
                subtitle = "Production totals will appear once today’s records are available."
            )
            return@Column
        }

        if (isTablet) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                stats.forEachIndexed { index, stat ->
                    StatTile(
                        stat = stat,
                        index = index,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                stats.forEachIndexed { index, stat ->
                    StatTile(
                        stat = stat,
                        index = index,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardEmptyState(
    title: String,
    subtitle: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xFFF8F5EF))
            .padding(horizontal = 16.dp, vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            fontFamily = DashboardPoppins,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 14.sp,
            color = FarmInk,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = subtitle,
            fontFamily = DashboardPoppins,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            color = FarmMuted,
            textAlign = TextAlign.Center,
            lineHeight = 16.sp
        )
    }
}

@Composable
private fun StatTile(
    stat: DashboardStat,
    modifier: Modifier,
    index: Int
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(stat.title) {
        delay(120L + index * 80L)
        visible = true
    }

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(420, easing = FastOutSlowInEasing),
        label = "statTileAlpha"
    )

    val lift by animateFloatAsState(
        targetValue = if (visible) 0f else 18f,
        animationSpec = tween(520, easing = FastOutSlowInEasing),
        label = "statTileLift"
    )

    Row(
        modifier = modifier
            .graphicsLayer {
                this.alpha = alpha
                translationY = lift
            }
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xFFF8F5EF))
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(stat.iconBg.copy(alpha = 0.13f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = stat.icon,
                contentDescription = stat.title,
                tint = stat.iconBg,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = stat.value,
                fontFamily = DashboardPoppins,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 23.sp,
                color = FarmInk,
                lineHeight = 26.sp
            )

            Spacer(modifier = Modifier.height(1.dp))

            Text(
                text = stat.title,
                fontFamily = DashboardPoppins,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = FarmMuted,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
private fun MonitoringSection(
    gauges: List<GaugeData>,
    resources: List<ResourceData>,
    environmentFilter: SensorFilterState,
    resourceFilter: SensorFilterState,
    isTablet: Boolean,
    onEnvironmentFilterChange: (SensorFilterOption) -> Unit,
    onResourceFilterChange: (SensorFilterOption) -> Unit
) {
    if (isTablet) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MonitoringCard(
                title = "Environment",
                description = "Temperature and ammonia",
                icon = Lucide.Thermometer,
                filterState = environmentFilter,
                modifier = Modifier.weight(1f),
                cardHeight = 350.dp,
                onFilterChange = onEnvironmentFilterChange
            ) {
                SensorReadingRow(
                    first = { gauges.getOrNull(0)?.let { GaugeBlock(it, Modifier.weight(1f)) } },
                    second = { gauges.getOrNull(1)?.let { GaugeBlock(it, Modifier.weight(1f)) } }
                )
            }

            MonitoringCard(
                title = "Resources",
                description = "Feed and water levels",
                icon = Lucide.Droplets,
                filterState = resourceFilter,
                modifier = Modifier.weight(1f),
                cardHeight = 350.dp,
                onFilterChange = onResourceFilterChange
            ) {
                SensorReadingRow(
                    first = { resources.getOrNull(0)?.let { ResourceBlock(it, Modifier.weight(1f)) } },
                    second = { resources.getOrNull(1)?.let { ResourceBlock(it, Modifier.weight(1f)) } }
                )
            }
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            MonitoringCard(
                title = "Environment",
                description = "Temperature and ammonia",
                icon = Lucide.Thermometer,
                filterState = environmentFilter,
                modifier = Modifier.fillMaxWidth(),
                cardHeight = 350.dp,
                onFilterChange = onEnvironmentFilterChange
            ) {
                SensorReadingRow(
                    first = { gauges.getOrNull(0)?.let { GaugeBlock(it, Modifier.weight(1f)) } },
                    second = { gauges.getOrNull(1)?.let { GaugeBlock(it, Modifier.weight(1f)) } }
                )
            }

            MonitoringCard(
                title = "Resources",
                description = "Feed and water levels",
                icon = Lucide.Droplets,
                filterState = resourceFilter,
                modifier = Modifier.fillMaxWidth(),
                cardHeight = 350.dp,
                onFilterChange = onResourceFilterChange
            ) {
                SensorReadingRow(
                    first = { resources.getOrNull(0)?.let { ResourceBlock(it, Modifier.weight(1f)) } },
                    second = { resources.getOrNull(1)?.let { ResourceBlock(it, Modifier.weight(1f)) } }
                )
            }
        }
    }
}

@Composable
private fun RowScope.SensorReadingRow(
    first: @Composable RowScope.() -> Unit,
    second: @Composable RowScope.() -> Unit
) {
    first()
    DividerLine()
    second()
}

@Composable
private fun MonitoringCard(
    title: String,
    description: String,
    icon: ImageVector,
    filterState: SensorFilterState,
    modifier: Modifier = Modifier,
    cardHeight: Dp,
    onFilterChange: (SensorFilterOption) -> Unit,
    content: @Composable RowScope.() -> Unit
) {
    Column(
        modifier = modifier
            .height(cardHeight)
            .clip(RoundedCornerShape(26.dp))
            .background(FarmSurface)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(if (title == "Environment") FarmSoftGreen else Color(0xFFFFF3E4)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (title == "Environment") FarmGreen else FarmAmber,
                    modifier = Modifier.size(21.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontFamily = DashboardPoppins,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = FarmInk
                )

                Text(
                    text = description,
                    fontFamily = DashboardPoppins,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = FarmMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        SensorFilterDropdown(
            filterState = filterState,
            onSelected = onFilterChange
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

@Composable
private fun SensorFilterDropdown(
    filterState: SensorFilterState,
    onSelected: (SensorFilterOption) -> Unit
) {
    var showPicker by remember { mutableStateOf(false) }

    val selected = filterState.options.firstOrNull {
        it.houseId == filterState.selectedHouseId && it.penId == filterState.selectedPenId
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(FarmSurfaceAlt)
            .border(1.dp, FarmLine, RoundedCornerShape(18.dp))
            .clickable(enabled = filterState.options.isNotEmpty()) {
                showPicker = true
            }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Lucide.MapPin,
                    contentDescription = "Location",
                    tint = FarmMuted,
                    modifier = Modifier.size(13.dp)
                )

                Spacer(modifier = Modifier.width(5.dp))

                Text(
                    text = "Location",
                    fontFamily = DashboardPoppins,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = FarmMuted
                )
            }

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = selected?.displayLabel ?: "No sensor assignment",
                fontFamily = DashboardPoppins,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (selected == null) Color(0xFF8A8A8A) else FarmInk
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Change",
                fontFamily = DashboardPoppins,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = FarmGreen
            )

            Spacer(modifier = Modifier.width(3.dp))

            Icon(
                imageVector = Lucide.ChevronDown,
                contentDescription = "Change location",
                tint = FarmGreen,
                modifier = Modifier.size(15.dp)
            )
        }
    }

    if (showPicker) {
        AlertDialog(
            onDismissRequest = { showPicker = false },
            containerColor = FarmSurface,
            shape = RoundedCornerShape(28.dp),
            title = {
                Text(
                    text = "Sensor Location",
                    fontFamily = DashboardPoppins,
                    fontWeight = FontWeight.ExtraBold,
                    color = FarmInk
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    filterState.options.forEach { option ->
                        val isSelected = option.houseId == filterState.selectedHouseId &&
                                option.penId == filterState.selectedPenId

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(if (isSelected) FarmSoftGreen else Color(0xFFF8F5EF))
                                .border(
                                    1.dp,
                                    if (isSelected) FarmGreen.copy(alpha = 0.35f) else FarmLine,
                                    RoundedCornerShape(18.dp)
                                )
                                .clickable {
                                    showPicker = false
                                    onSelected(option)
                                }
                                .padding(horizontal = 14.dp, vertical = 13.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Lucide.MapPin,
                                contentDescription = option.displayLabel,
                                tint = if (isSelected) FarmGreen else FarmMuted,
                                modifier = Modifier.size(16.dp)
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            Text(
                                text = option.displayLabel,
                                fontFamily = DashboardPoppins,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = FarmInk
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text(
                        text = "Close",
                        fontFamily = DashboardPoppins,
                        fontWeight = FontWeight.ExtraBold,
                        color = FarmGreen
                    )
                }
            }
        )
    }
}
@Composable
private fun GaugeBlock(
    data: GaugeData,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = gaugeProgress(data.value, data.min, data.max),
        animationSpec = tween(850, easing = FastOutSlowInEasing),
        label = "gaugeProgress"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularGauge(
            progress = animatedProgress,
            color = data.color,
            modifier = Modifier.size(122.dp),
            centerText = formatValue(data.value, data.unit)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = data.label,
            fontFamily = DashboardPoppins,
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            color = FarmInk
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = data.recordedAt ?: "No recent reading",
            fontFamily = DashboardPoppins,
            fontWeight = FontWeight.Medium,
            fontSize = 10.sp,
            color = FarmMuted,
            textAlign = TextAlign.Center,
            lineHeight = 11.sp
        )
    }
}

@Composable
private fun ResourceBlock(
    data: ResourceData,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = resourceProgress(data.value, data.max),
        animationSpec = tween(850, easing = FastOutSlowInEasing),
        label = "resourceProgress"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .width(66.dp)
                .height(106.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFFEFF3EC))
                .border(1.dp, Color(0xFFDDE5D9), RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.70f)
                    .fillMaxHeight(animatedProgress)
                    .clip(RoundedCornerShape(15.dp))
                    .background(data.color)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = data.label,
            fontFamily = DashboardPoppins,
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold,
            color = FarmInk
        )

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = formatValue(data.value, data.unit),
            fontFamily = DashboardPoppins,
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            color = data.color
        )

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = data.recordedAt ?: "No recent reading",
            fontFamily = DashboardPoppins,
            fontWeight = FontWeight.Medium,
            fontSize = 10.sp,
            color = FarmMuted,
            textAlign = TextAlign.Center,
            lineHeight = 11.sp
        )
    }
}

@Composable
private fun CircularGauge(
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier,
    centerText: String
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 13.dp.toPx()

            drawArc(
                color = Color(0xFFE5ECE3),
                startAngle = -210f,
                sweepAngle = 240f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            drawArc(
                color = color,
                startAngle = -210f,
                sweepAngle = 240f * progress.coerceIn(0f, 1f),
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(1.dp, Color(0xFFE8EDE6), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = centerText,
                fontFamily = DashboardPoppins,
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                color = FarmInk,
                textAlign = TextAlign.Center,
                maxLines = 1,
                lineHeight = 11.sp
            )
        }
    }
}

@Composable
private fun DividerLine() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(138.dp)
            .background(FarmLine)
    )
}

@Composable
private fun PendingTaskBanner(
    count: Int,
    task: PendingTaskSummary?,
    isTablet: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(Color(0xFF0B3A20), Color(0xFF1F7A3A))
                )
            )
            .clickable { onClick() }
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = count.toString(),
                fontFamily = DashboardPoppins,
                fontSize = if (isTablet) 58.sp else 50.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )

            Spacer(modifier = Modifier.width(14.dp))

            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Lucide.ClipboardList,
                    contentDescription = "Pending tasks",
                    tint = Color.White,
                    modifier = Modifier.size(23.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (count <= 0 || task == null) "No Pending Task" else if (count == 1) "Pending Task" else "Pending Tasks",
                    fontFamily = DashboardPoppins,
                    fontSize = if (isTablet) 24.sp else 21.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(6.dp))

                if (count <= 0 || task == null) {
                    Text(
                        text = "You're clear for now.",
                        fontFamily = DashboardPoppins,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.78f)
                    )
                } else {
                    Text(
                        text = task.title.ifBlank { "Task" },
                        fontFamily = DashboardPoppins,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )

                    val meta = listOfNotNull(
                        task.finishBy.takeIf { it.isNotBlank() }?.let { "Due $it" },
                        listOf(task.houseLabel, task.penLabel)
                            .filter { it.isNotBlank() }
                            .joinToString(" | ")
                            .takeIf { it.isNotBlank() }
                    ).joinToString("  -  ")

                    if (meta.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = meta,
                            fontFamily = DashboardPoppins,
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.78f),
                            lineHeight = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickAccessSection(
    items: List<QuickAccessItem>,
    isTablet: Boolean,
    onItemClick: (String) -> Unit
) {
    if (isTablet) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items.forEachIndexed { index, item ->
                QuickAccessCard(
                    item = item,
                    index = index,
                    modifier = Modifier.weight(1f),
                    cardWidth = null,
                    cardHeight = 104.dp,
                    onClick = { onItemClick(item.actionKey) }
                )
            }
        }
    } else {
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items.forEachIndexed { index, item ->
                QuickAccessCard(
                    item = item,
                    index = index,
                    cardWidth = 132.dp,
                    cardHeight = 104.dp,
                    onClick = { onItemClick(item.actionKey) }
                )
            }
        }
    }
}

@Composable
private fun QuickAccessCard(
    item: QuickAccessItem,
    index: Int,
    modifier: Modifier = Modifier,
    cardWidth: Dp? = 132.dp,
    cardHeight: Dp = 104.dp,
    onClick: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(item.title) {
        delay(90L + index * 85L)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(360)) +
                slideInVertically(
                    animationSpec = tween(500, easing = FastOutSlowInEasing),
                    initialOffsetY = { it / 2 }
                ) +
                scaleIn(
                    animationSpec = tween(420, easing = FastOutSlowInEasing),
                    initialScale = 0.97f
                )
    ) {
        Column(
            modifier = if (cardWidth != null) {
                modifier
                    .width(cardWidth)
                    .height(cardHeight)
            } else {
                modifier.height(cardHeight)
            }
                .clip(RoundedCornerShape(24.dp))
                .background(FarmSurface)
                .border(1.dp, FarmLine.copy(alpha = 0.55f), RoundedCornerShape(24.dp))
                .clickable { onClick() }
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                tint = item.tint,
                modifier = Modifier.size(24.dp)
            )

            Text(
                text = item.title,
                fontFamily = DashboardPoppins,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = FarmInk,
                lineHeight = 15.sp,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun DashboardSkeleton(isTablet: Boolean) {
    val alpha = dashboardSkeletonAlpha()

    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        DashboardSkeletonBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(156.dp),
            alpha = alpha,
            color = FarmDeepGreen,
            shape = RoundedCornerShape(30.dp)
        )

        DashboardSkeletonBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isTablet) 150.dp else 270.dp),
            alpha = alpha,
            shape = RoundedCornerShape(26.dp)
        )

        DashboardSkeletonBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(112.dp),
            alpha = alpha,
            color = FarmGreen,
            shape = RoundedCornerShape(26.dp)
        )

        DashboardSkeletonBox(
            modifier = Modifier
                .width(220.dp)
                .height(42.dp),
            alpha = alpha,
            shape = RoundedCornerShape(999.dp)
        )

        DashboardSkeletonBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(318.dp),
            alpha = alpha,
            shape = RoundedCornerShape(26.dp)
        )

        DashboardSkeletonBox(
            modifier = Modifier
                .width(196.dp)
                .height(42.dp),
            alpha = alpha,
            shape = RoundedCornerShape(999.dp)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            repeat(3) {
                DashboardSkeletonBox(
                    modifier = Modifier
                        .width(if (isTablet) 180.dp else 132.dp)
                        .height(108.dp),
                    alpha = alpha,
                    shape = RoundedCornerShape(24.dp)
                )
            }
        }
    }
}

@Composable
private fun dashboardSkeletonAlpha(): Float {
    val transition = rememberInfiniteTransition(label = "dashboardSkeleton")
    val alpha by transition.animateFloat(
        initialValue = 0.28f,
        targetValue = 0.62f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dashboardSkeletonAlpha"
    )
    return alpha
}

@Composable
private fun DashboardSkeletonBox(
    modifier: Modifier,
    alpha: Float,
    color: Color = Color(0xFFD9D2C6),
    shape: Shape = RoundedCornerShape(16.dp)
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(color.copy(alpha = alpha))
    )
}

@Composable
fun BottomNavBar(
    onTasksClick: () -> Unit,
    onFarmManagementClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF07381F), Color(0xFF022716))
                )
            )
            .navigationBarsPadding()
            .padding(horizontal = 8.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomNavItem(Lucide.LayoutDashboard, "Dashboard", true, {})
        BottomNavItem(Lucide.ClipboardList, "Tasks", false, onTasksClick)
        BottomNavItem(Lucide.House, "Farm Management", false, onFarmManagementClick)
        BottomNavItem(Lucide.UserRound, "Profile", false, onProfileClick)
    }
}

@Composable
private fun BottomNavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) Color.White else Color(0xFFCFE8D2),
            modifier = Modifier.size(22.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            fontFamily = DashboardPoppins,
            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold,
            fontSize = 9.sp,
            color = if (selected) Color.White else Color(0xFFCFE8D2),
            textAlign = TextAlign.Center,
            lineHeight = 10.sp
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DashboardScreenPreview() {
    DashboardScreen(
        onNavigateToTasks = {},
        onNavigateToFarmManagement = {},
        onNavigateToProfile = {},
        onQuickAccessPopulation = {},
        onQuickAccessFeedsRefill = {},
        onQuickAccessBiosecurity = {},
        uiState = placeholderDashboardState()
    )
}