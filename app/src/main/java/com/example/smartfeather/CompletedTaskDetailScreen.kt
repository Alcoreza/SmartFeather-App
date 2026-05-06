package com.example.smartfeather

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class CompletedTaskDetailUiState(
    val id: Int,
    val title: String,
    val description: String,
    val timeAssigned: String,
    val statusLabel: String,
    val finishBy: String,
    val timeCompleted: String,
    val timeCompletedLabel: String = "Time Completed",
    val priorityLabel: String,
    val priority: TaskPriority,
    val notes: String,
    val hasPhoto: Boolean = true
)



private val CompletedPoppins = FontFamily(
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_semibold, FontWeight.SemiBold),
    Font(R.font.poppins_bold, FontWeight.Bold)
)

private fun completedPriorityChipColor(priority: TaskPriority): Color {
    return when (priority) {
        TaskPriority.LOW -> Color(0xFFFA7A1F)
        TaskPriority.MID -> Color(0xFFC4420B)
        TaskPriority.HIGH -> Color(0xFFC92222)
    }
}



@Composable
fun CompletedTaskDetailScreen(
    task: CompletedTaskDetailUiState,
    onBackClick: () -> Unit = {},
    onNavigateToDashboard: () -> Unit = {},
    onNavigateToTasks: () -> Unit = {}
) {
    Scaffold(
        containerColor = Color(0xFFF5F2EE),
        bottomBar = {
            CompletedDetailBottomNavBar(
                onDashboardClick = onNavigateToDashboard,
                onTasksClick = onNavigateToTasks
            )
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFF8F6F2), Color(0xFFF1EEEA))
                    )
                )
                .padding(innerPadding)
        ) {
            val isTablet = maxWidth >= 700.dp

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(
                        horizontal = if (isTablet) 28.dp else 18.dp,
                        vertical = 18.dp
                    )
            ) {
                TopHeader(
                    onBackClick = onBackClick,
                    title = "Tasks"
                )

                Spacer(modifier = Modifier.height(18.dp))

                if (isTablet) {
                    TabletStatusRow(task)
                } else {
                    MobileStatusRow(task)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp)) {
                        Text(
                            text = task.title,
                            fontFamily = CompletedPoppins,
                            fontWeight = FontWeight.Bold,
                            fontSize = if (isTablet) 24.sp else 20.sp,
                            color = Color(0xFF1B6A23)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = task.description,
                            fontFamily = CompletedPoppins,
                            fontWeight = FontWeight.Normal,
                            fontSize = if (isTablet) 15.sp else 14.sp,
                            lineHeight = if (isTablet) 23.sp else 21.sp,
                            color = Color(0xFF3F3F3F)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                if (isTablet) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        PhotoCard(
                            hasPhoto = task.hasPhoto,
                            modifier = Modifier.weight(1f)
                        )
                        NotesCard(
                            notes = task.notes,
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    PhotoCard(
                        hasPhoto = task.hasPhoto,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    NotesCard(
                        notes = task.notes,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun TopHeader(
    onBackClick: () -> Unit,
    title: String
) {
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .clip(CircleShape)
                .clickable { onBackClick() }
                .padding(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color(0xFF171717),
                modifier = Modifier.size(22.dp)
            )
        }

        Text(
            text = title,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontFamily = CompletedPoppins,
            fontWeight = FontWeight.SemiBold,
            fontSize = 22.sp,
            color = Color(0xFF171717)
        )
    }
}

@Composable
private fun MobileStatusRow(task: CompletedTaskDetailUiState) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DetailChip(task.statusLabel, if (task.statusLabel == "For Approval") Color(0xFFD88913) else Color(0xFF266F33))
            StatusChip("Time Assigned", task.timeAssigned, Color(0xFF103824))
            StatusChip("Finish By", task.finishBy, Color(0xFFD88913))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatusChip(
                task.timeCompletedLabel,
                task.timeCompleted,
                Color(0xFF52B84F)
            )

            StatusChip(
                "Priority",
                task.priorityLabel,
                completedPriorityChipColor(task.priority)
            )
        }
    }
}



@Composable
private fun TabletStatusRow(task: CompletedTaskDetailUiState) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            DetailChip(task.statusLabel, if (task.statusLabel == "For Approval") Color(0xFFD88913) else Color(0xFF266F33))
            StatusChip("Time Assigned", task.timeAssigned, Color(0xFF103824))
            StatusChip("Finish By", task.finishBy, Color(0xFFD88913))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatusChip(task.timeCompletedLabel, task.timeCompleted, Color(0xFF52B84F))
            StatusChip("Priority", task.priorityLabel, completedPriorityChipColor(task.priority))
        }
    }
}


@Composable
private fun DetailChip(
    text: String,
    containerColor: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(containerColor)
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            fontFamily = CompletedPoppins,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = Color.White
        )
    }
}

@Composable
private fun StatusChip(
    title: String,
    value: String,
    containerColor: Color
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(containerColor)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            fontFamily = CompletedPoppins,
            fontSize = 9.sp,
            color = Color.White.copy(alpha = 0.9f),
            textAlign = TextAlign.Center,
            lineHeight = 10.sp
        )
        Text(
            text = value,
            fontFamily = CompletedPoppins,
            fontWeight = FontWeight.SemiBold,
            fontSize = 10.sp,
            color = Color.White,
            textAlign = TextAlign.Center,
            lineHeight = 11.sp
        )
    }
}


@Composable
private fun PhotoCard(
    hasPhoto: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp)) {
            Text(
                text = "Photo:",
                fontFamily = CompletedPoppins,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = Color(0xFF6B6B6B)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFFF8F8F8)),
                contentAlignment = Alignment.Center
            ) {
                if (hasPhoto) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "Proof photo",
                            tint = Color(0xFF4D4D4D),
                            modifier = Modifier.size(46.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Worker proof photo",
                            fontFamily = CompletedPoppins,
                            fontSize = 13.sp,
                            color = Color(0xFF777777)
                        )
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "No photo",
                            tint = Color(0xFF9A9A9A),
                            modifier = Modifier.size(42.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No photo provided",
                            fontFamily = CompletedPoppins,
                            fontSize = 13.sp,
                            color = Color(0xFF9A9A9A)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotesCard(
    notes: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp)) {
            Text(
                text = "Notes:",
                fontFamily = CompletedPoppins,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = Color(0xFF6B6B6B)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFFFDFDFD))
                    .padding(horizontal = 14.dp, vertical = 14.dp)
            ) {
                Text(
                    text = notes,
                    fontFamily = CompletedPoppins,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = Color(0xFF3F3F3F)
                )
            }
        }
    }
}

@Composable
private fun CompletedDetailBottomNavBar(
    onDashboardClick: () -> Unit,
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
        CompletedNavItem(Icons.Outlined.Home, "Dashboard", false, onDashboardClick)
        CompletedNavItem(Icons.AutoMirrored.Outlined.List, "Tasks", true, onTasksClick)
        CompletedNavItem(Icons.Outlined.Edit, "Farm Management", false, {})
        CompletedNavItem(Icons.Outlined.AccountCircle, "Profile", false, {})
    }
}

@Composable
private fun CompletedNavItem(
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
            fontFamily = CompletedPoppins,
            fontSize = 10.sp,
            color = if (selected) Color.White else Color(0xFFD7ECD9),
            textAlign = TextAlign.Center,
            lineHeight = 11.sp
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CompletedTaskDetailScreenPreview() {
    CompletedTaskDetailScreen(
        task = CompletedTaskDetailUiState(
            id = 1,
            title = "Water Refill",
            description = "Check the water containers or drinker system assigned to the pen and refill it with clean and sufficient water. Ensure that all drinkers are properly filled and accessible to the birds. Remove any visible dirt, debris, or contaminants around the water area. Observe the water flow to confirm that there are no leaks, blockages, or interruptions.",
            timeAssigned = "9:21 AM",
            statusLabel = "Completed",
            finishBy = "5:00 PM",
            timeCompleted = "2:53 PM",
            timeCompletedLabel = "Time Completed",
            priorityLabel = "Low",
            priority = TaskPriority.LOW,
            notes = "Water was refilled successfully. All drinkers are working properly and no leaks were observed in the assigned pen.",
            hasPhoto = true
        )
    )
}
