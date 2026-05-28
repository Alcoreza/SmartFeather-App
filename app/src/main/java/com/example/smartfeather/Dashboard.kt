package com.example.smartfeather

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
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
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import kotlin.math.roundToInt

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
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_semibold, FontWeight.SemiBold),
    Font(R.font.poppins_bold, FontWeight.Bold)
)

fun placeholderDashboardState(): DashboardUiState {
    return DashboardUiState(
        welcomeText = "Welcome!",
        overviewDateLabel = "for 5/11/26",
        stats = listOf(
            DashboardStat("Total Birds", "5462", Icons.Outlined.Home, Color(0xFFFDFDFD), Color(0xFFD92C2C)),
            DashboardStat("Total Eggs", "367", Icons.Outlined.CheckCircle, Color(0xFFFDFDFD), Color(0xFFFFA96E)),
            DashboardStat("Mortalities", "25", Icons.Outlined.AccountCircle, Color(0xFFFDFDFD), Color(0xFF808080))
        ),
        gauges = listOf(
            GaugeData("Temperature", 11f, "deg", 0f, 40f, Color(0xFF6ABF4B)),
            GaugeData("Ammonia", 15f, "ppm", 0f, 40f, Color(0xFFF4B43A))
        ),
        resources = listOf(
            ResourceData("Feed", 90f, "%", 100f, Color(0xFFC88A3D)),
            ResourceData("Water", 40f, "%", 100f, Color(0xFF6CDDE5))
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
            QuickAccessItem("Population", Icons.Outlined.CheckCircle, Color(0xFFD92C2C), "population"),
            QuickAccessItem("Feeds Refill", Icons.AutoMirrored.Outlined.List, Color(0xFFCC8A2D), "feeds_refill"),
            QuickAccessItem("Biosecurity", Icons.Outlined.Edit, Color(0xFF2F8F45), "biosecurity")
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
    onEnvironmentFilterChange: (SensorFilterOption) -> Unit = {},
    onResourceFilterChange: (SensorFilterOption) -> Unit = {}
) {
    var contentVisible by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        contentVisible = true
    }

    Scaffold(
        containerColor = Color(0xFFF6F3EF),
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
                        colors = listOf(Color(0xFFF8F6F2), Color(0xFFF0ECE7))
                    )
                )
                .padding(innerPadding)
        ) {
            val isTablet = maxWidth >= 700.dp

            Box(
                modifier = Modifier
                    .size(if (isTablet) 360.dp else 240.dp)
                    .align(Alignment.TopEnd)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0x1636A24D), Color.Transparent)
                        ),
                        shape = CircleShape
                    )
            )

            AnimatedVisibility(
                visible = contentVisible,
                enter = fadeIn(animationSpec = tween(500)) + slideInVertically(
                    animationSpec = tween(500, easing = FastOutSlowInEasing),
                    initialOffsetY = { it / 8 }
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = if (isTablet) 28.dp else 20.dp, vertical = 18.dp)
                ) {
                    Text(
                        text = uiState.welcomeText,
                        fontFamily = DashboardPoppins,
                        fontSize = if (isTablet) 32.sp else 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF171717)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    FarmOverviewCard(
                        stats = uiState.stats,
                        overviewDateLabel = uiState.overviewDateLabel,
                        isTablet = isTablet
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    MonitoringSection(
                        gauges = uiState.gauges,
                        resources = uiState.resources,
                        environmentFilter = uiState.environmentFilter,
                        resourceFilter = uiState.resourceFilter,
                        isTablet = isTablet,
                        onEnvironmentFilterChange = onEnvironmentFilterChange,
                        onResourceFilterChange = onResourceFilterChange
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    PendingTaskBanner(
                        count = uiState.pendingTaskCount,
                        task = uiState.pendingTask,
                        isTablet = isTablet
                    )

                    Spacer(modifier = Modifier.height(22.dp))

                    Text(
                        text = "Quick Access",
                        fontFamily = DashboardPoppins,
                        fontSize = if (isTablet) 22.sp else 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF181818)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

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

                    Spacer(modifier = Modifier.height(18.dp))
                }
            }
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
                title = "Environmental Monitoring",
                filterState = environmentFilter,
                modifier = Modifier.weight(1f),
                cardHeight = 300.dp,
                onFilterChange = onEnvironmentFilterChange
            ) {
                GaugeBlock(gauges.getOrElse(0) { GaugeData("Temperature", 0f, "deg", 0f, 40f, Color(0xFF6ABF4B)) }, modifier = Modifier.weight(1f))
                DividerLine()
                GaugeBlock(gauges.getOrElse(1) { GaugeData("Ammonia", 0f, "ppm", 0f, 40f, Color(0xFFF4B43A)) }, modifier = Modifier.weight(1f))
            }

            MonitoringCard(
                title = "Feed and Water Monitoring",
                filterState = resourceFilter,
                modifier = Modifier.weight(1f),
                cardHeight = 300.dp,
                onFilterChange = onResourceFilterChange
            ) {
                ResourceBlock(resources.getOrElse(0) { ResourceData("Feed", 0f, "%", 100f, Color(0xFFC88A3D)) }, modifier = Modifier.weight(1f))
                DividerLine()
                ResourceBlock(resources.getOrElse(1) { ResourceData("Water", 0f, "%", 100f, Color(0xFF6CDDE5)) }, modifier = Modifier.weight(1f))
            }
        }
    } else {
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            MonitoringCard(
                title = "Environmental Monitoring",
                filterState = environmentFilter,
                modifier = Modifier.fillMaxWidth(),
                cardHeight = 290.dp,
                onFilterChange = onEnvironmentFilterChange
            ) {
                GaugeBlock(gauges.getOrElse(0) { GaugeData("Temperature", 0f, "deg", 0f, 40f, Color(0xFF6ABF4B)) }, modifier = Modifier.weight(1f))
                DividerLine()
                GaugeBlock(gauges.getOrElse(1) { GaugeData("Ammonia", 0f, "ppm", 0f, 40f, Color(0xFFF4B43A)) }, modifier = Modifier.weight(1f))
            }

            MonitoringCard(
                title = "Feed and Water Monitoring",
                filterState = resourceFilter,
                modifier = Modifier.fillMaxWidth(),
                cardHeight = 290.dp,
                onFilterChange = onResourceFilterChange
            ) {
                ResourceBlock(resources.getOrElse(0) { ResourceData("Feed", 0f, "%", 100f, Color(0xFFC88A3D)) }, modifier = Modifier.weight(1f))
                DividerLine()
                ResourceBlock(resources.getOrElse(1) { ResourceData("Water", 0f, "%", 100f, Color(0xFF6CDDE5)) }, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun MonitoringCard(
    title: String,
    filterState: SensorFilterState,
    modifier: Modifier = Modifier,
    cardHeight: Dp,
    onFilterChange: (SensorFilterOption) -> Unit,
    content: @Composable RowScope.() -> Unit
) {
    Card(
        modifier = modifier.height(cardHeight),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
            Text(
                text = title,
                fontFamily = DashboardPoppins,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF171717),
                lineHeight = 18.sp
            )

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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SensorFilterDropdown(
    filterState: SensorFilterState,
    onSelected: (SensorFilterOption) -> Unit
) {
    var showPicker by remember { mutableStateOf(false) }

    val selected = filterState.options.firstOrNull {
        it.houseId == filterState.selectedHouseId && it.penId == filterState.selectedPenId
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF8F7F4))
            .border(1.dp, Color(0xFFD7DDD6), RoundedCornerShape(16.dp))
            .clickable(enabled = filterState.options.isNotEmpty()) {
                showPicker = true
            }
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Column {
            Text(
                text = "House and Pen",
                fontFamily = DashboardPoppins,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF657064)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = selected?.displayLabel ?: "No sensor assignment",
                fontFamily = DashboardPoppins,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (selected == null) Color(0xFF8A8A8A) else Color(0xFF1D241E)
            )
        }
    }

    if (showPicker) {
        AlertDialog(
            onDismissRequest = { showPicker = false },
            title = {
                Text(
                    text = "Select Sensor Location",
                    fontFamily = DashboardPoppins,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Column {
                    filterState.options.forEach { option ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable {
                                    showPicker = false
                                    onSelected(option)
                                }
                                .padding(horizontal = 10.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = option.displayLabel,
                                fontFamily = DashboardPoppins,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF1E2A20)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text("Close", fontFamily = DashboardPoppins)
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
        animationSpec = tween(800),
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
            modifier = Modifier.size(88.dp),
            centerText = formatValue(data.value, data.unit)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = data.label,
            fontFamily = DashboardPoppins,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = Color(0xFF1B1B1B)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = data.recordedAt ?: "No recent reading",
            fontFamily = DashboardPoppins,
            fontSize = 10.sp,
            color = Color(0xFF6B6B6B),
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
        animationSpec = tween(800),
        label = "resourceProgress"
    )

    val containerHeight = 108.dp

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .width(68.dp)
                .height(containerHeight)
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFFF0F4EE)),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.72f)
                    .fillMaxHeight(animatedProgress)
                    .clip(RoundedCornerShape(14.dp))
                    .background(data.color)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = data.label,
            fontFamily = DashboardPoppins,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1B1B1B)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = formatValue(data.value, data.unit),
            fontFamily = DashboardPoppins,
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold,
            color = data.color
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = data.recordedAt ?: "No recent reading",
            fontFamily = DashboardPoppins,
            fontSize = 10.sp,
            color = Color(0xFF6B6B6B),
            textAlign = TextAlign.Center,
            lineHeight = 11.sp
        )
    }
}

@Composable
private fun FarmOverviewCard(
    stats: List<DashboardStat>,
    overviewDateLabel: String,
    isTablet: Boolean
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF032C16)),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(24.dp))
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 16.dp)) {
            Text(
                text = "Daily Farm Overview",
                fontFamily = DashboardPoppins,
                color = Color(0xFF72F07C),
                fontSize = if (isTablet) 20.sp else 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                stats.forEach { stat ->
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = stat.bgColor),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(if (isTablet) 32.dp else 28.dp)
                                    .clip(CircleShape)
                                    .background(stat.iconBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = stat.icon,
                                    contentDescription = stat.title,
                                    tint = Color.White,
                                    modifier = Modifier.size(if (isTablet) 16.dp else 14.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Column {
                                Text(
                                    text = stat.value,
                                    fontFamily = DashboardPoppins,
                                    fontSize = if (isTablet) 13.sp else 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF111111)
                                )
                                Text(
                                    text = stat.title,
                                    fontFamily = DashboardPoppins,
                                    fontSize = if (isTablet) 10.sp else 9.sp,
                                    color = Color(0xFF444444),
                                    lineHeight = if (isTablet) 12.sp else 11.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = overviewDateLabel,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End,
                fontFamily = DashboardPoppins,
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.78f)
            )
        }
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
            val strokeWidth = 12.dp.toPx()

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
                .size(46.dp)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = centerText,
                fontFamily = DashboardPoppins,
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1A1A1A),
                textAlign = TextAlign.Center,
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
            .height(116.dp)
            .background(Color(0xFFDCDCDC))
    )
}

@Composable
private fun PendingTaskBanner(
    count: Int,
    task: PendingTaskSummary?,
    isTablet: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFF2E8B46), Color(0xFF215F31))
                )
            )
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(if (isTablet) 120.dp else 92.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x16FFFFFF), Color.Transparent)
                    ),
                    shape = CircleShape
                )
        )

        if (count <= 0 || task == null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "0",
                    fontFamily = DashboardPoppins,
                    fontSize = if (isTablet) 56.sp else 48.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "No Pending Task",
                        fontFamily = DashboardPoppins,
                        fontSize = if (isTablet) 24.sp else 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "You're clear for now.",
                        fontFamily = DashboardPoppins,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.82f)
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = count.toString(),
                    fontFamily = DashboardPoppins,
                    fontSize = if (isTablet) 56.sp else 48.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = if (count == 1) "Pending Task" else "Pending Tasks",
                        fontFamily = DashboardPoppins,
                        fontSize = if (isTablet) 24.sp else 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color.White.copy(alpha = 0.14f))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = task.title.ifBlank { "Task" },
                            fontFamily = DashboardPoppins,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }

                    val meta = listOfNotNull(
                        task.finishBy.takeIf { it.isNotBlank() }?.let { "Due $it" },
                        listOf(task.houseLabel, task.penLabel)
                            .filter { it.isNotBlank() }
                            .joinToString(" | ")
                            .takeIf { it.isNotBlank() }
                    ).joinToString("  •  ")

                    if (meta.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = meta,
                            fontFamily = DashboardPoppins,
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.80f),
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
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
        ) {
            items.forEach { item ->
                QuickAccessCard(
                    item = item,
                    modifier = Modifier.weight(1f),
                    cardWidth = null,
                    cardHeight = 118.dp,
                    onClick = { onItemClick(item.actionKey) }
                )
            }
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items.forEach { item ->
                    QuickAccessCard(
                        item = item,
                        cardWidth = 110.dp,
                        cardHeight = 104.dp,
                        onClick = { onItemClick(item.actionKey) }
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickAccessCard(
    item: QuickAccessItem,
    modifier: Modifier = Modifier,
    cardWidth: Dp? = 110.dp,
    cardHeight: Dp = 104.dp,
    onClick: () -> Unit
) {
    Card(
        modifier = if (cardWidth != null) {
            modifier.width(cardWidth).height(cardHeight).clickable { onClick() }
        } else {
            modifier.height(cardHeight).clickable { onClick() }
        },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 7.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(13.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(item.tint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    tint = item.tint,
                    modifier = Modifier.size(18.dp)
                )
            }

            Text(
                text = item.title,
                fontFamily = DashboardPoppins,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF171717),
                lineHeight = 15.sp
            )
        }
    }
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
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF06331D), Color(0xFF022816))
                )
            )
            .navigationBarsPadding()
            .padding(horizontal = 10.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomNavItem(
            icon = Icons.Outlined.Home,
            label = "Dashboard",
            selected = true,
            onClick = {}
        )

        BottomNavItem(
            icon = Icons.AutoMirrored.Outlined.List,
            label = "Tasks",
            selected = false,
            onClick = onTasksClick
        )

        BottomNavItem(
            icon = Icons.Outlined.Edit,
            label = "Farm Management",
            selected = false,
            onClick = onFarmManagementClick
        )

        BottomNavItem(
            icon = Icons.Outlined.AccountCircle,
            label = "Profile",
            selected = false,
            onClick = onProfileClick
        )
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
        modifier = Modifier.clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) Color.White else Color(0xFFD7ECD9),
            modifier = Modifier.size(22.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            fontFamily = DashboardPoppins,
            fontSize = 10.sp,
            color = if (selected) Color.White else Color(0xFFD7ECD9),
            textAlign = TextAlign.Center,
            lineHeight = 11.sp
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