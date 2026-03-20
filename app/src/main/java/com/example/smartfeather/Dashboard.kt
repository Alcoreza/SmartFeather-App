package com.example.smartfeather

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
    val bgColor: Color,
    val iconBg: Color
)

data class GaugeData(
    val label: String,
    val value: Float,
    val unit: String,
    val min: Float,
    val max: Float,
    val color: Color
)

data class ResourceData(
    val label: String,
    val value: Float,
    val unit: String,
    val max: Float,
    val color: Color
)

data class QuickAccessItem(
    val title: String,
    val icon: ImageVector,
    val tint: Color
)

data class DashboardUiState(
    val welcomeText: String,
    val stats: List<DashboardStat>,
    val gauges: List<GaugeData>,
    val resources: List<ResourceData>,
    val pendingTasks: String,
    val quickAccess: List<QuickAccessItem>
)

private val DashboardPoppins = FontFamily(
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_semibold, FontWeight.SemiBold),
    Font(R.font.poppins_bold, FontWeight.Bold)
)

private fun placeholderDashboardState(): DashboardUiState {
    return DashboardUiState(
        welcomeText = "Welcome!",
        stats = listOf(
            DashboardStat("Total Birds", "5462", Color(0xFFFDFDFD), Color(0xFFD92C2C)),
            DashboardStat("Total Eggs", "367", Color(0xFFFDFDFD), Color(0xFFFFA96E)),
            DashboardStat("Mortalities", "25", Color(0xFFFDFDFD), Color(0xFF808080))
        ),
        gauges = listOf(
            GaugeData("Temperature", 11f, "deg", 0f, 40f, Color(0xFF6ABF4B)),
            GaugeData("Ammonia", 15f, "ppm", 0f, 40f, Color(0xFFF4B43A))
        ),
        resources = listOf(
            ResourceData("Feed", 90f, "%", 100f, Color(0xFFC88A3D)),
            ResourceData("Water", 40f, "%", 100f, Color(0xFF6CDDE5))
        ),
        pendingTasks = "2",
        quickAccess = listOf(
            QuickAccessItem("Population", Icons.Outlined.CheckCircle, Color(0xFFD92C2C)),
            QuickAccessItem("Feeds Refill", Icons.AutoMirrored.Outlined.List, Color(0xFFCC8A2D)),
            QuickAccessItem("Biosecurity", Icons.Outlined.Edit, Color(0xFF2F8F45))
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
    val display = if (value % 1f == 0f) value.roundToInt().toString() else value.toString()
    return "$display$unit"
}

@Composable
fun DashboardScreen(
    onNavigateToTasks: () -> Unit,
    uiState: DashboardUiState = remember { placeholderDashboardState() }
) {
    Scaffold(
        containerColor = Color(0xFFF6F3EF),
        bottomBar = {
            BottomNavBar(onTasksClick = onNavigateToTasks)
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFF9F7F4), Color(0xFFF2EEEA))
                    )
                )
                .padding(innerPadding)
        ) {
            val isTablet = maxWidth >= 700.dp

            Box(
                modifier = Modifier
                    .size(if (isTablet) 320.dp else 220.dp)
                    .align(Alignment.TopEnd)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0x1438A34A), Color.Transparent)
                        ),
                        shape = CircleShape
                    )
            )

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
                    isTablet = isTablet
                )

                Spacer(modifier = Modifier.height(18.dp))

                MonitoringSection(
                    gauges = uiState.gauges,
                    resources = uiState.resources,
                    isTablet = isTablet
                )

                Spacer(modifier = Modifier.height(20.dp))

                PendingTaskBanner(
                    count = uiState.pendingTasks,
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
                    isTablet = isTablet
                )

                Spacer(modifier = Modifier.height(18.dp))
            }
        }
    }
}

@Composable
private fun MonitoringSection(
    gauges: List<GaugeData>,
    resources: List<ResourceData>,
    isTablet: Boolean
) {
    if (isTablet) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MonitoringCard(
                title = "Environmental Monitoring",
                modifier = Modifier.weight(1f),
                cardHeight = 240.dp
            ) {
                GaugeBlock(gauges[0], modifier = Modifier.weight(1f))
                DividerLine()
                GaugeBlock(gauges[1], modifier = Modifier.weight(1f))
            }

            MonitoringCard(
                title = "Feed and Water Monitoring",
                modifier = Modifier.weight(1f),
                cardHeight = 240.dp
            ) {
                ResourceBlock(resources[0], modifier = Modifier.weight(1f))
                DividerLine()
                ResourceBlock(resources[1], modifier = Modifier.weight(1f))
            }
        }
    } else {
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            MonitoringCard(
                title = "Environmental Monitoring",
                modifier = Modifier.fillMaxWidth(),
                cardHeight = 220.dp
            ) {
                GaugeBlock(gauges[0], modifier = Modifier.weight(1f))
                DividerLine()
                GaugeBlock(gauges[1], modifier = Modifier.weight(1f))
            }

            MonitoringCard(
                title = "Feed and Water Monitoring",
                modifier = Modifier.fillMaxWidth(),
                cardHeight = 220.dp
            ) {
                ResourceBlock(resources[0], modifier = Modifier.weight(1f))
                DividerLine()
                ResourceBlock(resources[1], modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun QuickAccessSection(
    items: List<QuickAccessItem>,
    isTablet: Boolean
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
                    cardHeight = 118.dp
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
                        cardHeight = 104.dp
                    )
                }
            }
        }
    }
}

@Composable
private fun FarmOverviewCard(
    stats: List<DashboardStat>,
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
                                Text(
                                    text = stat.value.take(1),
                                    fontFamily = DashboardPoppins,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = if (isTablet) 12.sp else 11.sp
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
        }
    }
}

@Composable
private fun MonitoringCard(
    title: String,
    modifier: Modifier = Modifier,
    cardHeight: Dp,
    content: @Composable RowScope.() -> Unit
) {
    Card(
        modifier = modifier.height(cardHeight),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 9.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp)) {
            Text(
                text = title,
                fontFamily = DashboardPoppins,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF171717),
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                content = content
            )
        }
    }
}

@Composable
private fun GaugeBlock(
    data: GaugeData,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = gaugeProgress(data.value, data.min, data.max),
        animationSpec = tween(700),
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
            modifier = Modifier.size(86.dp),
            centerText = formatValue(data.value, data.unit)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = data.label,
            fontFamily = DashboardPoppins,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = Color(0xFF1B1B1B),
            lineHeight = 14.sp
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
        animationSpec = tween(700),
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
            text = formatValue(data.value, data.unit),
            fontFamily = DashboardPoppins,
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            color = data.color
        )

        Text(
            text = data.label,
            fontFamily = DashboardPoppins,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1B1B1B)
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
            .height(96.dp)
            .background(Color(0xFFDCDCDC))
    )
}

@Composable
private fun PendingTaskBanner(
    count: String,
    isTablet: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFF33944A), Color(0xFF256C36))
                )
            )
            .padding(horizontal = 20.dp, vertical = 22.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(if (isTablet) 110.dp else 88.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x1CFFFFFF), Color.Transparent)
                    ),
                    shape = CircleShape
                )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = count,
                fontFamily = DashboardPoppins,
                fontSize = if (isTablet) 62.sp else 54.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = "Pending Tasks",
                fontFamily = DashboardPoppins,
                fontSize = if (isTablet) 26.sp else 23.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun QuickAccessCard(
    item: QuickAccessItem,
    modifier: Modifier = Modifier,
    cardWidth: Dp? = 110.dp,
    cardHeight: Dp = 104.dp
) {
    Card(
        modifier = if (cardWidth != null) {
            modifier.width(cardWidth).height(cardHeight).clickable { }
        } else {
            modifier.height(cardHeight).clickable { }
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
private fun BottomNavBar(
    onTasksClick: () -> Unit
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
            onClick = {}
        )

        BottomNavItem(
            icon = Icons.Outlined.AccountCircle,
            label = "Profile",
            selected = false,
            onClick = {}
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
        onNavigateToTasks = {}
    )
}
