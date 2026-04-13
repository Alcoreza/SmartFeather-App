package com.example.smartfeather

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val FarmPoppins = FontFamily(
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_semibold, FontWeight.SemiBold),
    Font(R.font.poppins_bold, FontWeight.Bold)
)

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
            description = "Track the total number of birds in the flock.",
            icon = Icons.Outlined.Pets,
            iconTint = Color.White,
            iconBackground = Color(0xFFC5392F)
        ),
        FarmManagementItem(
            title = "Weight",
            description = "Log weight samples based on the birds.",
            icon = Icons.Outlined.MonitorWeight,
            iconTint = Color.White,
            iconBackground = Color(0xFF6A35F0)
        ),
        FarmManagementItem(
            title = "Feeds Refill",
            description = "Record feed refilling activities for the flock.",
            icon = Icons.Outlined.Inventory2,
            iconTint = Color.White,
            iconBackground = Color(0xFFC98A3D)
        ),
        FarmManagementItem(
            title = "Vitamins Refill",
            description = "Log vitamin refilling activities for the flock.",
            icon = Icons.Outlined.Science,
            iconTint = Color.White,
            iconBackground = Color(0xFF5D9E99)
        ),
        FarmManagementItem(
            title = "Biosecurity",
            description = "Monitor biosecurity measures.",
            icon = Icons.Outlined.FavoriteBorder,
            iconTint = Color.White,
            iconBackground = Color(0xFF80A66E)
        ),
        FarmManagementItem(
            title = "New Bird Batch",
            description = "Register a new batch of birds entering the poultry house.",
            icon = Icons.Outlined.Pets,
            iconTint = Color.White,
            iconBackground = Color(0xFFD14848)
        )
    )

    Scaffold(
        containerColor = Color(0xFFF1EFEC),
        bottomBar = {
            FarmManagementBottomNavBar(
                onDashboardClick = onNavigateToDashboard,
                onTasksClick = onNavigateToTasks,
                onProfileClick = onNavigateToProfile
            )

        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFF4F2EF), Color(0xFFECE9E6))
                    )
                )
                .padding(innerPadding)
                .padding(horizontal = 18.dp, vertical = 18.dp)
        ) {
            Text(
                text = "Record Data",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontFamily = FarmPoppins,
                fontWeight = FontWeight.SemiBold,
                fontSize = 24.sp,
                color = Color(0xFF111111)
            )

            Spacer(modifier = Modifier.height(18.dp))

            HorizontalDivider(
                thickness = 3.dp,
                color = Color(0xFF246B33),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(farmItems) { item ->
                    FarmManagementCard(
                        item = item,
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

@Composable
private fun FarmManagementCard(
    item: FarmManagementItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(115.dp)
            .shadow(8.dp, RoundedCornerShape(20.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F8F6)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(item.iconBackground, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    tint = item.iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = item.title,
                    fontFamily = FarmPoppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF121212),
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = item.description,
                    fontFamily = FarmPoppins,
                    fontWeight = FontWeight.Normal,
                    fontSize = 10.sp,
                    color = Color(0xFF6A6A6A),
                    lineHeight = 12.sp
                )
            }
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
                    colors = listOf(Color(0xFF06331D), Color(0xFF022816))
                )
            )
            .navigationBarsPadding()
            .padding(horizontal = 10.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        FarmManagementBottomNavItem(
            icon = Icons.Outlined.Home,
            label = "Dashboard",
            selected = false,
            onClick = onDashboardClick
        )

        FarmManagementBottomNavItem(
            icon = Icons.AutoMirrored.Outlined.List,
            label = "Tasks",
            selected = false,
            onClick = onTasksClick
        )

        FarmManagementBottomNavItem(
            icon = Icons.Outlined.Edit,
            label = "Farm Management",
            selected = true,
            onClick = {}
        )

        FarmManagementBottomNavItem(
            icon = Icons.Outlined.AccountCircle,
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
            fontFamily = FarmPoppins,
            fontSize = 10.sp,
            color = if (selected) Color.White else Color(0xFFD7ECD9),
            textAlign = TextAlign.Center,
            lineHeight = 11.sp
        )
    }
}