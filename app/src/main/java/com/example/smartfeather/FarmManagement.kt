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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.MonitorWeight
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.Science
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
import com.composables.icons.lucide.ClipboardList
import com.composables.icons.lucide.House
import com.composables.icons.lucide.LayoutDashboard
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.UserRound

private val FarmPoppins = FontFamily(
    Font(R.font.manrope_regular, FontWeight.Normal),
    Font(R.font.manrope_medium, FontWeight.Medium),
    Font(R.font.manrope_semibold, FontWeight.SemiBold),
    Font(R.font.manrope_bold, FontWeight.Bold),
    Font(R.font.manrope_extrabold, FontWeight.ExtraBold)
)

private val FarmBackground = Color(0xFFF6F3EC)
private val FarmSurface = Color(0xFFFFFCF7)
private val FarmInk = Color(0xFF121A14)
private val FarmMuted = Color(0xFF677168)
private val FarmLine = Color(0xFFD8D0C3)
private val FarmGreen = Color(0xFF1F7A3A)
private val FarmDeepGreen = Color(0xFF062717)
private val FarmGreenTwo = Color(0xFF155C2D)

data class FarmManagementItem(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val iconTint: Color,
    val iconBackground: Color
)

@Composable
fun FarmManagementScreen(
    onNavigateToDashboard: () -> Unit,
    onNavigateToTasks: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onPopulationClick: () -> Unit = {},
    onWeightClick: () -> Unit = {},
    onFeedsRefillClick: () -> Unit = {},
    onVitaminsRefillClick: () -> Unit = {},
    onBiosecurityClick: () -> Unit = {},
    onNewBirdBatchClick: () -> Unit = {}
) {
    val farmItems = listOf(
        FarmManagementItem(
            title = "Population",
            description = "Track bird counts and flock movement.",
            icon = Icons.Outlined.Pets,
            iconTint = Color.White,
            iconBackground = Color(0xFF2F7D46)
        ),
        FarmManagementItem(
            title = "Weight",
            description = "Record flock weight samples.",
            icon = Icons.Outlined.MonitorWeight,
            iconTint = Color.White,
            iconBackground = Color(0xFF466D8F)
        ),
        FarmManagementItem(
            title = "Feeds Refill",
            description = "Log feed refilling activities.",
            icon = Icons.Outlined.Inventory2,
            iconTint = Color.White,
            iconBackground = Color(0xFFC27A24)
        ),
        FarmManagementItem(
            title = "Vitamins Refill",
            description = "Track vitamin refill records.",
            icon = Icons.Outlined.Science,
            iconTint = Color.White,
            iconBackground = Color(0xFF3E8982)
        ),
        FarmManagementItem(
            title = "Biosecurity",
            description = "Monitor protection measures.",
            icon = Icons.Outlined.FavoriteBorder,
            iconTint = Color.White,
            iconBackground = Color(0xFF6B8F45)
        ),
        FarmManagementItem(
            title = "New Bird Batch",
            description = "Register incoming bird batches.",
            icon = Icons.Outlined.Pets,
            iconTint = Color.White,
            iconBackground = Color(0xFFB64A3C)
        )
    )

    Scaffold(
        containerColor = FarmBackground,
        bottomBar = {
            FarmManagementBottomNavBar(
                onDashboardClick = onNavigateToDashboard,
                onTasksClick = onNavigateToTasks,
                onProfileClick = onNavigateToProfile
            )
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFFBF8F1), FarmBackground, Color(0xFFEDE7DA))
                    )
                )
                .padding(innerPadding)
        ) {
            val isTablet = maxWidth >= 700.dp

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = if (isTablet) 28.dp else 18.dp, vertical = 18.dp)
            ) {
                FarmManagementHero()

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Farm records",
                            fontFamily = FarmPoppins,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = FarmInk
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = "Choose the workflow you need to update.",
                            fontFamily = FarmPoppins,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = FarmMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = if (isTablet) 190.dp else 148.dp),
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 18.dp)
                ) {
                    itemsIndexed(farmItems) { index, item ->
                        FarmManagementCard(
                            item = item,
                            index = index,
                            onClick = {
                                when (item.title) {
                                    "Population" -> onPopulationClick()
                                    "Weight" -> onWeightClick()
                                    "Feeds Refill" -> onFeedsRefillClick()
                                    "Vitamins Refill" -> onVitaminsRefillClick()
                                    "Biosecurity" -> onBiosecurityClick()
                                    "New Bird Batch" -> onNewBirdBatchClick()
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FarmManagementHero() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(30.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(FarmDeepGreen, Color(0xFF0E4025), FarmGreenTwo)
                )
            )
            .padding(horizontal = 20.dp, vertical = 22.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.12f))
                    .border(1.dp, Color.White.copy(alpha = 0.14f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.size(12.dp))

            Column {
                Text(
                    text = "Record Data",
                    fontFamily = FarmPoppins,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp,
                    color = Color.White,
                    lineHeight = 28.sp
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = "Choose a farm activity to update.",
                    fontFamily = FarmPoppins,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.72f),
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun FarmManagementCard(
    item: FarmManagementItem,
    index: Int,
    onClick: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(item.title) {
        delay(index * 55L)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(360)) +
                slideInVertically(
                    animationSpec = tween(420, easing = FastOutSlowInEasing),
                    initialOffsetY = { it / 5 }
                ) +
                scaleIn(
                    animationSpec = tween(360, easing = FastOutSlowInEasing),
                    initialScale = 0.96f
                )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(158.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.96f),
                            FarmSurface.copy(alpha = 0.98f)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    color = FarmLine.copy(alpha = 0.82f),
                    shape = RoundedCornerShape(24.dp)
                )
                .clickable { onClick() }
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(item.iconBackground),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        tint = item.iconTint,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Box(
                    modifier = Modifier
                        .padding(top = 3.dp)
                        .size(width = 28.dp, height = 4.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(item.iconBackground.copy(alpha = 0.32f))
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = item.title,
                fontFamily = FarmPoppins,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp,
                color = FarmInk,
                lineHeight = 19.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = item.description,
                fontFamily = FarmPoppins,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                color = FarmMuted,
                lineHeight = 15.sp
            )
        }
    }
}
@Composable
private fun FarmManagementBottomNavBar(
    onDashboardClick: () -> Unit,
    onTasksClick: () -> Unit,
    onProfileClick: () -> Unit
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
        FarmManagementBottomNavItem(
            icon = Lucide.LayoutDashboard,
            label = "Dashboard",
            selected = false,
            onClick = onDashboardClick
        )

        FarmManagementBottomNavItem(
            icon = Lucide.ClipboardList,
            label = "Tasks",
            selected = false,
            onClick = onTasksClick
        )

        FarmManagementBottomNavItem(
            icon = Lucide.House,
            label = "Farm Management",
            selected = true,
            onClick = {}
        )

        FarmManagementBottomNavItem(
            icon = Lucide.UserRound,
            label = "Profile",
            selected = false,
            onClick = onProfileClick
        )
    }
}

@Composable
private fun FarmManagementBottomNavItem(
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
            fontFamily = FarmPoppins,
            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold,
            fontSize = 9.sp,
            color = if (selected) Color.White else Color(0xFFCFE8D2),
            textAlign = TextAlign.Center,
            lineHeight = 10.sp
        )
    }
}