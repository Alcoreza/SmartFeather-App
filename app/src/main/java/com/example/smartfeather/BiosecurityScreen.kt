package com.example.smartfeather

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private val BiosecurityManrope = FontFamily(
    Font(R.font.manrope_extralight, FontWeight.ExtraLight),
    Font(R.font.manrope_light, FontWeight.Light),
    Font(R.font.manrope_regular, FontWeight.Normal),
    Font(R.font.manrope_medium, FontWeight.Medium),
    Font(R.font.manrope_semibold, FontWeight.SemiBold),
    Font(R.font.manrope_bold, FontWeight.Bold),
    Font(R.font.manrope_extrabold, FontWeight.ExtraBold),
    Font(R.font.manrope_variablefont_wght, FontWeight.Black)
)

private val BioBackground = Color(0xFFF6F3EC)
private val BioSurface = Color(0xFFFFFCF7)
private val BioSurfaceAlt = Color(0xFFF3EFE7)
private val BioInk = Color(0xFF121A14)
private val BioMuted = Color(0xFF677168)
private val BioLine = Color(0xFFD8D0C3)
private val BioGreen = Color(0xFF1F7A3A)
private val BioDeepGreen = Color(0xFF062717)
private val BioForest = Color(0xFF103C28)
private val BioTeal = Color(0xFF2E7D6B)
private val BioAmber = Color(0xFFD78A2B)
private val BioBlue = Color(0xFF3F6F88)

data class BiosecurityMenuItem(
    val title: String,
    val description: String,
    val icon: ImageVector
)

@Composable
fun BiosecurityScreen(
    onBackToFarm: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    onNavigateToTasks: () -> Unit,
    onDisinfectionClick: () -> Unit,
    onPersonnelLogsClick: () -> Unit,
    onVisitorClick: () -> Unit
) {
    val menuItems = listOf(
        BiosecurityMenuItem(
            title = "Disinfection",
            description = "Log sanitation rounds and cleaning activity records.",
            icon = Icons.Outlined.BugReport
        ),
        BiosecurityMenuItem(
            title = "Personnel Logs",
            description = "Record facility entry and exit for assigned personnel.",
            icon = Icons.Outlined.Group
        ),
        BiosecurityMenuItem(
            title = "Visitor",
            description = "Manage visitor entries and scheduled facility access.",
            icon = Icons.Outlined.Badge
        )
    )

    val actions = listOf(
        onDisinfectionClick,
        onPersonnelLogsClick,
        onVisitorClick
    )

    Scaffold(
        containerColor = BioBackground,
        bottomBar = {
            BiosecurityBottomNavBar(
                onDashboardClick = onNavigateToDashboard,
                onTasksClick = onNavigateToTasks,
                onFarmClick = onBackToFarm
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFFBF8F1), BioBackground, Color(0xFFEDE7DA))
                    )
                )
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp, vertical = 18.dp)
            ) {
                BiosecurityHero(onBackToFarm = onBackToFarm)

                Spacer(modifier = Modifier.height(18.dp))

                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    menuItems.forEachIndexed { index, item ->
                        BiosecurityMenuCard(
                            item = item,
                            index = index,
                            accentColor = when (index) {
                                0 -> BioTeal
                                1 -> BioGreen
                                else -> BioBlue
                            },
                            onClick = actions[index]
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun BiosecurityHero(
    onBackToFarm: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(30.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(BioDeepGreen, BioForest, BioTeal)
                )
            )
            .padding(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.14f))
                    .border(1.dp, Color.White.copy(alpha = 0.16f), CircleShape)
                    .clickable { onBackToFarm() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(21.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "Biosecurity",
                fontFamily = BiosecurityManrope,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 26.sp,
                color = Color.White,
                lineHeight = 30.sp,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun BiosecurityMenuCard(
    item: BiosecurityMenuItem,
    index: Int,
    accentColor: Color,
    onClick: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(item.title) {
        delay(index * 80L)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(360)) +
                slideInVertically(
                    animationSpec = tween(430, easing = FastOutSlowInEasing),
                    initialOffsetY = { it / 5 }
                ) +
                scaleIn(
                    animationSpec = tween(360, easing = FastOutSlowInEasing),
                    initialScale = 0.97f
                )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(BioSurface)
                .border(1.dp, BioLine.copy(alpha = 0.72f), RoundedCornerShape(24.dp))
                .clickable { onClick() }
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    tint = accentColor,
                    modifier = Modifier.size(25.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    fontFamily = BiosecurityManrope,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp,
                    color = BioInk,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(5.dp))

                Text(
                    text = item.description,
                    fontFamily = BiosecurityManrope,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = BioMuted,
                    lineHeight = 17.sp
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(BioSurfaceAlt),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = ">",
                    fontFamily = BiosecurityManrope,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = accentColor
                )
            }
        }
    }
}

@Composable
private fun BiosecurityBottomNavBar(
    onDashboardClick: () -> Unit,
    onTasksClick: () -> Unit,
    onFarmClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF07381F), Color(0xFF022716))
                )
            )
            .navigationBarsPadding()
            .padding(horizontal = 8.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BiosecurityBottomNavItem(Icons.Outlined.Home, "Dashboard", false, onDashboardClick)
        BiosecurityBottomNavItem(Icons.AutoMirrored.Outlined.List, "Tasks", false, onTasksClick)
        BiosecurityBottomNavItem(Icons.Outlined.Edit, "Farm Management", true, onFarmClick)
        BiosecurityBottomNavItem(Icons.Outlined.AccountCircle, "Profile", false, {})
    }
}

@Composable
private fun BiosecurityBottomNavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
            .padding(horizontal = 8.dp, vertical = 5.dp)
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
            fontFamily = BiosecurityManrope,
            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold,
            fontSize = 9.sp,
            color = if (selected) Color.White else Color(0xFFCFE8D2),
            textAlign = TextAlign.Center,
            maxLines = 1,
            lineHeight = 10.sp
        )
    }
}